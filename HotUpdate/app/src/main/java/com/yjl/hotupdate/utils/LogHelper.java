
package com.yjl.hotupdate.utils;

import android.util.Log;

/**
 * @author 
 * @note 可调节打印信息
 */
public class LogHelper {
    private static final String TAG = "PLUGINMANAGER";

    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int WARN = 2;

    private static int logLevel = WARN;

    public static void setLogLevel(int logLevel) {
        LogHelper.logLevel = logLevel;

    }

    /**
     * @note 打印出调试过程的LOG
     */
    public static void showDebug(String tag, String msg) {
        if (logLevel <= DEBUG) {

            Log.d(TAG, tag + "->" + msg);
        }
    }

    /**
     * @note 打印出程序中的警告
     */
    public static void showWarn(String tag, String msg) {
        if (logLevel <= WARN) {
            Log.w(TAG, tag + "->" + msg);
        }
    }

    /**
     * @note 打印出程序中的信息
     */
    public static void showInfo(String tag, String msg) {
        if (logLevel <= INFO) {
            Log.i(TAG, tag + "->" + msg);
        }
    }

    public static void catchExceptions(Throwable tr) {
        Log.w(TAG, Log.getStackTraceString(tr));
    }
}
