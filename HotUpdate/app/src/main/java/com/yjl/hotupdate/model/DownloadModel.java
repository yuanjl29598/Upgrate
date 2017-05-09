package com.yjl.hotupdate.model;

import android.content.Context;

import com.yjl.hotupdate.BaseConfig;
import com.yjl.hotupdate.PluginManager;
import com.yjl.hotupdate.http.HttpRequester;
import com.yjl.hotupdate.http.NetworkCallback;
import com.yjl.hotupdate.loader.PluginEntity;
import com.yjl.hotupdate.utils.FileUtil;
import com.yjl.hotupdate.utils.JsonUtil;
import com.yjl.hotupdate.utils.LogHelper;
import com.yjl.hotupdate.utils.XmlPullHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;


/**
 * 插件下载model
 *
 * @author yjl
 */
public class DownloadModel {

    private static DownloadModel dm;

    private DownloadModel() {
    }

    public static DownloadModel getInstence() {
        if (dm == null) {
            dm = new DownloadModel();
        }
        return dm;
    }

    /**
     * 获取本地插件信息上传
     *
     * @param context
     */
    public void getLocalPlugin(Context context) {
        StringBuilder names = new StringBuilder();
        StringBuilder versions = new StringBuilder();
        for (PluginEntity entity : PluginManager.list()) {
            if (names.length() > 0) {
                names.append(",");
                versions.append(",");
            }
            names.append(entity.getName());
            versions.append(entity.getVersion());
        }
        try {
            HttpRequester request = new HttpRequester();
            final StringBuilder result = null;
            request.requestGET(new StringBuilder()
                    .append(BaseConfig.SERVER_URL).append("?name=")
                    .append(names).append("&version=").append(versions)
                    .toString(), new NetworkCallback() {
                @Override
                public void onSuccess(int tag, Object data) {
                    result.append(data);
                }

                @Override
                public void onFail(int tag, Object data) {
                    result.append(data);
                }
            });
            if (result != null) {
                JSONArray array = new JSONArray(result.toString());
                PluginEntity entity = null;
                JSONObject obj = null;
                for (int i = 0; i < array.length(); i++) {
                    try {
                        obj = (JSONObject) array.get(i);
                        entity = PluginManager.findPluginAll(String.valueOf(obj
                                .get("name")));
                        if (entity != null) {
                            File cacheFile = new File(BaseConfig.CACHE_PATH);
                            if (!cacheFile.exists())
                                cacheFile.mkdirs();
                            entity.setUrl(String.valueOf(obj.get("url")));
                            // 如果是新增加的apk,读取xml文件
                            if (!entity.isAvailable()
                                    && obj.get("xmlfile") != null) {
                                boolean complete = FileUtil.download(
                                        (String) obj.get("xmlfile"),
                                        BaseConfig.CACHE_PATH
                                                + entity.getName() + ".xml");
                                if (complete) {
                                    FileInputStream fi = new FileInputStream(
                                            new File(BaseConfig.CACHE_PATH
                                                    + entity.getName() + ".xml"));
                                    JSONArray ea = XmlPullHelper.pullXml(fi);
                                    for (int e = 0; e < ea.length(); e++) {
                                        JSONObject jsonObject = ea
                                                .optJSONObject(e);
                                        if (entity.getName().equals(
                                                jsonObject.getString("name"))) {
                                            JsonUtil.fromJson(entity,
                                                    jsonObject);
                                            break;
                                        }
                                    }
                                    fi.close();
                                    new File(BaseConfig.CACHE_PATH
                                            + entity.getName() + ".xml")
                                            .deleteOnExit();
                                }
                            }
                            boolean complete = FileUtil.download(
                                    entity.getUrl(), BaseConfig.CACHE_PATH
                                            + entity.getName());
                            if (complete) {
                                FileUtil.copyFile(
                                        new File(BaseConfig.CACHE_PATH
                                                + entity.getName()),
                                        new File(
                                                BaseConfig
                                                        .getPluginPath(
                                                                context,
                                                                entity.getName())));
                                if (!entity.isAvailable())
                                    entity.init(context);
                                else
                                    entity.reload(context);
                                entity.setVersion(String.valueOf(obj
                                        .get("version")));
                                new File(BaseConfig.CACHE_PATH
                                        + entity.getName()).deleteOnExit();
                            }
                        }
                    } catch (Exception ex) {
                        LogHelper.catchExceptions(ex);
                    }
                }
                if (array.length() > 0)
                    PluginManager.save(context);
            }
        } catch (Exception e) {
            LogHelper.catchExceptions(e);
        }
    }

}
