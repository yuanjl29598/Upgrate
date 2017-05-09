package com.yjl.hotupdate.loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yjl.hotupdate.PluginManager;
import com.yjl.hotupdate.utils.LogHelper;


public class StartPluginReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LogHelper.showDebug("StartPluginReceiver", "进入解锁广播接收器！");
		PluginManager.start(context);
	}

}
