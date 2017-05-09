package com.yjl.hotupdate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import com.yjl.hotupdate.base.ICallback;
import com.yjl.hotupdate.loader.PluginEntity;
import com.yjl.hotupdate.loader.ServiceProxy;
import com.yjl.hotupdate.model.DownloadModel;
import com.yjl.hotupdate.utils.AppPreferenceHelper;
import com.yjl.hotupdate.utils.JsonUtil;
import com.yjl.hotupdate.utils.LogHelper;
import com.yjl.hotupdate.utils.NetworkUtil;
import com.yjl.hotupdate.utils.XmlPullHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PluginManager {
    // 存储本地插件信息
    private List<PluginEntity> localPlugins = new ArrayList<PluginEntity>();
    private boolean istart = false;
    private static PluginManager manager = new PluginManager();

    private PluginManager() {

    }

    // 插件管理启动
    public static void start(final Context context) {
        if (context == null) {
            LogHelper.showDebug("PluginManager", "init fail");
            return;
        }
        start(context, null);

    }

    // 插件管理启动，启动完成后调用callback
    public static void start(final Context context, final ICallback call) {
        clearPlugin(context);
        Intent service = new Intent(context, ServiceProxy.class);
        service.putExtra("action", "init");
        context.startService(service);
        startPlugin(context, call);
    }

    public static void startPlugin(final Context context, final ICallback call) {
        if (manager.localPlugins.size() == 0 && !manager.istart) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    manager.istart = true;
                    LogHelper.showInfo("initplugin", "Manager start init");
                    initPlugin(context.getApplicationContext());
                    // r如果不存在更新插件，自动添加
                    if (findPlugin("pluginUpgrade") == null) {
                        install("pluginUpgrade");
                        if ("WIFI".equals(NetworkUtil
                                .getNetworkTypeStr(context))) {
                            DownloadModel.getInstence().getLocalPlugin(
                                    context.getApplicationContext());
                        }
                    }
                    if (call != null)
                        call.onComplete(null);
                    manager.istart = false;
                }
            }).start();

        }
    }

    /**
     * 根据应用版本号是否更新来判断是否需要清理本地插件信息
     *
     * @return
     */
    private static void clearPlugin(Context context) {
        try {
            int version_code;
            String oldVer = AppPreferenceHelper.getInstance(context).getString(
                    BaseConfig.APP_VERSION, null);
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            version_code = pInfo.versionCode;
            String localVer = String.valueOf(version_code);
            if (!TextUtils.isEmpty(localVer) && !TextUtils.isEmpty(oldVer)) {
                if (!localVer.equals(oldVer)) {
                    AppPreferenceHelper.getInstance(context).putString(
                            AppPreferenceHelper.PreferenceKeys.APP_PLUGIN, null);
                    // 更新本地存的版本号
                    AppPreferenceHelper.getInstance(context).putString(
                            BaseConfig.APP_VERSION, localVer);
                }
            }
        } catch (NameNotFoundException e) {
            LogHelper.showDebug("PluginManager", e.getMessage());
        }
    }

    public static int size() {
        return manager.localPlugins.size();
    }

    // 查找可用插件
    public static PluginEntity findPlugin(String name) {
        if (name == null || name.trim().length() == 0)
            return null;

        // LogHelper.showInfo("finplugin", JsonUtil.toJsonString(localPlugins) +
        // " " +name);
        for (PluginEntity entity : manager.localPlugins)
            if (entity.getName().equals(name) && entity.isAvailable())
                return entity;
        return null;
    }

    // 查找插件，包括不可用
    public static PluginEntity findPluginAll(String name) {
        for (PluginEntity entity : manager.localPlugins)
            if (entity.getName().equals(name))
                return entity;
        return null;
    }

    // 安装
    public static void install(String name) {
        PluginEntity entity = findPluginAll(name);
        if (entity == null) {
            entity = new PluginEntity();
            entity.setName(name);
            entity.setVersion("0");
            manager.localPlugins.add(entity);
        }
    }

    public static List<PluginEntity> list() {
        return manager.localPlugins;
    }

    // 存储信息
    public static boolean save(Context context) {

        return AppPreferenceHelper.getInstance(context).putString(
                AppPreferenceHelper.PreferenceKeys.APP_PLUGIN,
                JsonUtil.toJsonString(manager.localPlugins));
    }

    // 初始化获得插件信息
    private static void initPlugin(Context context) {
        String plugins = AppPreferenceHelper.getInstance(context).getString(
                AppPreferenceHelper.PreferenceKeys.APP_PLUGIN, null);
        try {
            JSONArray jsonArray = null;
            if (plugins != null && plugins.trim().length() > 0) {
                jsonArray = new JSONArray(plugins);
            } else {
                jsonArray = getPluginfromXMl(context);
            }
            LogHelper.showInfo("PluginManager",
                    "plugin:" + jsonArray.toString());
            manager.localPlugins = getPluginEntityFromJson(context, jsonArray);
            for (PluginEntity entity : manager.localPlugins) {
                LogHelper.showInfo("initplugin", entity.getName());
                entity.init(context);
            }
        } catch (Exception e) {
            LogHelper.catchExceptions(e);
        }
    }

    /**
     * 初始化从xml中获取插件信息
     *
     * @param context
     * @return
     */
    private static JSONArray getPluginfromXMl(Context context)
            throws IOException {
        JSONArray array = XmlPullHelper.pullXml(context.getAssets().open(
                "plugin/pluginConfig.xml"));
        return array;
    }

    /**
     * 将jsonArray 转为 PluginEntity 对象
     *
     * @param jsonArray
     * @return
     */
    private static List<PluginEntity> getPluginEntityFromJson(Context context,
                                                              JSONArray jsonArray) {
        List<PluginEntity> list = new ArrayList<PluginEntity>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            PluginEntity entity = new PluginEntity();
            JsonUtil.fromJson(entity, jsonObject);
            if (entity.getName() == null
                    || entity.getName().trim().length() == 0)
                continue;
            list.add(entity);
        }
        return list;
    }
}
