package com.yjl.hotupdate.loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.yjl.hotupdate.entity.ServiceEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * 提供Service启动方法
 * @author yjl
 *
 */
public class ServiceActivator {
	
	private List<ServiceEntity> localService = new ArrayList<ServiceEntity>();
	private static ServiceActivator local = new ServiceActivator();
	private ServiceActivator() {
		
	}
	
	public static void registerService(Context context, ServiceEntity entity) {
		if("true".equals(entity.getKeep()))
			ServiceActivator.startKeepService(context, entity.getPluginName(), entity.getName());
		else
			ServiceActivator.startOnceService(context, entity.getPluginName(), entity.getName());
		local.localService.add(entity);
	}
	
	public static List<ServiceEntity> registerServices() {
		return local.localService;
	}
	
	/**
	 * 运行一次
	 * @param context
	 * @param pluginname
	 * @param classname
	 */
	private static void startOnceService(Context context, String pluginname, String classname) {
		Intent intent = getIntent(context, pluginname, classname);
		startService(context, intent);
	}

	/**
	 * 高频率运行
	 * @param context
	 * @param pluginname
	 * @param classname
	 */
	private static void startKeepService(Context context, String pluginname, String classname) {
		Intent intent = getIntent(context, pluginname, classname);
		intent.putExtra("keep", true);
		startService(context, intent);
	}


	/**
	 * 重新获取service实例
	 * @param context
	 * @param pluginname
	 * @param classname
	 */
	public static void restartService(Context context, String pluginname, String classname) {
		Intent intent = getIntent(context, pluginname, classname);
		intent.putExtra("reload", true);
		startService(context, intent);
	}
	
	private static void startService(Context context, Intent intent) {
		context.startService(intent);
		
	}

	private static Intent getIntent(Context context, String pluginname, String classname) {
		Intent intent = new Intent(context, ServiceProxy.class);
		intent.putExtra("classname", classname);
		intent.putExtra("pluginname", pluginname);
		return intent;
	}
	
	/**
	 * 绑定receiver
	 * @param context
	 * @param object
	 * @param priority
	 * @param actions
	 */
	public static void binderReceiver(Context context, BroadcastReceiver object, int priority, 
			String... actions) {
        IntentFilter filter = new IntentFilter();
        filter.setPriority(priority);
        for(String action:actions)
        	filter.addAction(action);
		context.registerReceiver(object, filter);
	}

	public static void binderReceiver(Context context, BroadcastReceiver object, String scheme, int priority, 
			String... actions) {
        IntentFilter filter = new IntentFilter();
        filter.setPriority(priority);
        filter.addDataScheme(scheme);
        for(String action:actions)
        	filter.addAction(action);
		context.registerReceiver(object, filter);
	}
	
	public static void unBinderReceiver(Context context,BroadcastReceiver object) {
		context.unregisterReceiver(object);
	}
}
