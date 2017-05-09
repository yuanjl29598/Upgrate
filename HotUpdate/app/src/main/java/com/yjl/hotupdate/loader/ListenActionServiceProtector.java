package com.yjl.hotupdate.loader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import com.yjl.hotupdate.BaseConfig;
import com.yjl.hotupdate.utils.LogHelper;


/**
 * @author
 * @note 不断开启Service，防止Service挂掉(起到循环作用)
 */
public class ListenActionServiceProtector {
	private AlarmManager am;
	private PendingIntent pendingIntent;

	/**
	 * 利用系统的AlarmManger，每秒都监听启动一次我们的服务，如果服务已经开启 只是走一次onStartCommand，如果没有开启，那么就启动
	 * 这样能保证我们的服务一直在，除非用户手动停止进程
	 * 
	 * @param context
	 */
	public void startPendingIntent(Context context) {
		am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, ServiceProxy.class);
		pendingIntent = PendingIntent.getService(context, 0, intent, 0);
		long interval = DateUtils.SECOND_IN_MILLIS * BaseConfig.SERVICE_REPEAT_INTERVAL; // 1秒钟一次
		
		//TODO 经测试，小米对ELAPSED_REALTIME_WAKEUP支持不够友好，RTC在荣耀上运行状态不好，其他类型有待测试
		
//		long firstWake = SystemClock.elapsedRealtime();
//		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstWake,
//				interval, pendingIntent);

		long firstWake = System.currentTimeMillis() + interval;
		am.setRepeating(AlarmManager.RTC, firstWake, interval, pendingIntent);
		LogHelper.showInfo("ProxyServiceProtector-守护进程", "初始化");
	}

	public void stopPendingTintent() {
		if (null != pendingIntent) {
			pendingIntent.cancel();
		}
	}
}
