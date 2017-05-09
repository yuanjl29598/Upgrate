package com.yjl.hotupdate.loader;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

import com.yjl.hotupdate.BaseConfig;
import com.yjl.hotupdate.PluginManager;
import com.yjl.hotupdate.entity.ServiceEntity;
import com.yjl.hotupdate.utils.LogHelper;

import java.util.Map;


/**
 * 代理service,始终运行，代理运行所有插件中的service
 * 
 * @author yjl
 * 
 */
public class ServiceProxy extends Service {

	// 注册过的service
	private Map<String, Service> map = new java.util.concurrent.ConcurrentHashMap<String, Service>();
	// 高频率运行的service
	private Map<String, Service> keepmap = new java.util.concurrent.ConcurrentHashMap<String, Service>();
	// 保护service的方法
	private ListenActionServiceProtector protector = null;

	@Override
	public void onCreate() {
		super.onCreate();
		LogHelper.showInfo("ServiceProxy", "create");
		if (protector == null && BaseConfig.SERVICE_REPEAT_ORNOT) {
			protector = new ListenActionServiceProtector();
			protector.startPendingIntent(this); // 循环启动Service(起到守护作用)
		}
		//startForeground(1120, new Notification());

		// PluginManager.startPlugin(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogHelper.showInfo("ServiceProxy", "ServiceProxy-size:" + keepmap.size());
		if (intent != null) {
			String name = intent.getStringExtra("classname");
			LogHelper.showInfo("ServiceProxy", name);
			if (name != null) {

				if (map.containsKey(name) && !reRegister(intent)) {
					try {
						map.get(name).onStartCommand(intent, flags, startId);
					} catch (Exception e) {
						LogHelper.catchExceptions(e);
					}
					return START_STICKY;
				}

				// 获取service实例
				String pluginname = intent.getStringExtra("pluginname");
				try {
					PluginEntity plugin = PluginManager.findPlugin(pluginname);
					if (plugin != null) {
						Service service = createService(plugin, name,
								intent.getBooleanExtra("keep", false));
						service.onStartCommand(intent, flags, startId);
						return START_STICKY;
					} else if (PluginManager.size() == 0) {
						PluginManager.startPlugin(this, null);
					}
				} catch (Exception e) {
					LogHelper.catchExceptions(e);
				}
			} else {
				LogHelper.showInfo("ServiceProxy->action",
						intent.getStringExtra("action"));
				LogHelper.showInfo("ServiceProxy->pluginsize", ""
						+ ServiceActivator.registerServices().size());
				if (!"init".equals(intent.getStringExtra("action"))
						&& map.size() == 0) {
					if (ServiceActivator.registerServices().size() > 0) {
						for (ServiceEntity entity : ServiceActivator
								.registerServices()) {
							PluginEntity plugin = PluginManager
									.findPlugin(entity.getPluginName());
							if (plugin != null)
								createService(plugin, entity.getName(),
										"true".equals(entity.getKeep()));
						}
					} else
						PluginManager.startPlugin(this, null);
				}
			}
		}
		for (Service mPluginService : keepmap.values())
			try {
				mPluginService.onStartCommand(intent, flags, startId);
			} catch (Exception e) {
				LogHelper.catchExceptions(e);
			}
		return START_STICKY;
	}

	private Service createService(PluginEntity plugin, String name, boolean keep) {
		if (map.containsKey(name))
			return map.get(name);
		LogHelper.showInfo("ServiceProxy-plugin", plugin.getName());
		PluginClassLoader loader = plugin.getClassloader();
		try {
			Service service = (Service) loader.getObject(name);
			try {
			loader.invokeMethod(service, "onCreate", new Class<?>[] {
					Context.class, Class.class }, new Object[] { this,
					ServiceProxy.class });
			}catch(Exception e){}
			map.put(name, service);
			if (keep || keepmap.containsKey(name))
				keepmap.put(name, service);

			return service;
		} catch (Exception e) {
			LogHelper.catchExceptions(e);
		}
		return null;
	}

	// 是否需要重新获取service实例
	private boolean reRegister(Intent intent) {
		return intent.getBooleanExtra("reLoad", false);
	}

	@Override
	public void onDestroy() {
		for (Service mPluginService : map.values())
			mPluginService.onDestroy();
		map.clear();
		keepmap.clear();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		for (Service mPluginService : map.values())
			mPluginService.onConfigurationChanged(newConfig);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onLowMemory() {
		for (Service mPluginService : map.values())
			mPluginService.onLowMemory();
		super.onLowMemory();
	}

	@Override
	@SuppressLint("NewApi")
	public void onTrimMemory(int level) {
		for (Service mPluginService : map.values())
			mPluginService.onTrimMemory(level);
		super.onTrimMemory(level);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
