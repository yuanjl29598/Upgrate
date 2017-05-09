package com.yjl.hotupdate;

import android.content.Context;

import com.yjl.hotupdate.utils.FileUtil;

import java.io.File;


public class BaseConfig {
	//插件版本号
	public static final String PLUGIN_MANAGER_VERSION = "0.0.1";
	//应用的版本号
	public static final String APP_VERSION  = "app_version";
	//更新地址
	public static final String SERVER_URL = "http://s2.90123.com/hawaii/checkreplace";
	
	public static final String DEFAULT_PATH = FileUtil.getSDCardPath();
	
	public static final String CACHE_PATH = DEFAULT_PATH+"/temp/";
	
	public static long SERVICE_REPEAT_INTERVAL = 3;
	
	public static boolean SERVICE_REPEAT_ORNOT = true;
	
	public static String NETWORK_STATE_WIFI = "WIFI";
	
//	public static final String PLUGIN_PATH = DEFAULT_PATH + "plugin";
	
	//插件内部展开地址
	public static final String getPluginPath(Context context, String name) {
		File file = new File(context.getFilesDir().getAbsolutePath()+"/plugin");
		if(!file.exists())
			file.mkdirs();
		return context.getFilesDir().getAbsolutePath()+"/plugin/"+name+".apk";
	}
}
