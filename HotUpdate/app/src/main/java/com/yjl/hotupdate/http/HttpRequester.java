package com.yjl.hotupdate.http;

import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class HttpRequester {
    private final static String TAG = "HttpRequester";
    private final static String REQUEST_POST = "POST";
    private final static String REQUEST_GET = "GET";
    private final int REQUEST_SUCCESS = 200;
    public static final int REQUEST_TIMEOUT = 5 * 1000; // 设置请求超时5秒钟
    public static final int SO_TIMEOUT = 8 * 1000; // 设置等待数据超时时间8秒钟
    public static final int REQUEST_READ_TIMEOUT = 5 * 1000; // 设置等待数据超时时间8秒钟
    public static final String UTF8_ENCODING = "UTF-8";
    /**
     * 网络请求失败
     */
    public static final int REPORT_MSG_FAIL = 0x111;
    public static final String REPORT_FAIL = "fail";

    // json数据传输时的标志
    public static final String JSONOBJECT_DATA = "data";


    public void requestPost(String url, String body, NetworkCallback cb) {
        Log.i(TAG, "requestPost");
        WeakReference<NetworkCallback> cbWeak = new WeakReference<NetworkCallback>(cb);
        try {
            Log.i(TAG, "url=" + url);
            // LogUtil.e(TAG, "requestPost:" + url + "\n body:" + body);
            URL mUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
            conn.setRequestMethod(REQUEST_POST);
            conn.setDoOutput(true);
            conn.setConnectTimeout(REQUEST_TIMEOUT);
            conn.setReadTimeout(REQUEST_READ_TIMEOUT);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            if (!TextUtils.isEmpty(body)) {
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.write(body.getBytes(UTF8_ENCODING));
                wr.flush();
                wr.close();
            }
            Log.i(TAG, "conn.getResponseCode():" + conn.getResponseCode());
            if (conn.getResponseCode() == REQUEST_SUCCESS && cbWeak.get() != null) {
                cbWeak.get().onSuccess(conn.getResponseCode(),
                        readInputStream(conn.getInputStream()));
            } else {
                if (cbWeak.get() != null) {
                    cbWeak.get().onFail(conn.getResponseCode(),
                            readInputStream(conn.getErrorStream()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            if (cbWeak.get() != null) {
                cbWeak.get().onFail(REPORT_MSG_FAIL,
                        REPORT_FAIL);
            } else {
                Log.e(TAG, "callback is null!");
            }
            e.printStackTrace();
        }
    }

    public void requestGET(String url, NetworkCallback cb) {
        Log.i(TAG, "requestGET");
        WeakReference<NetworkCallback> cbWeak = new WeakReference<NetworkCallback>(cb);
        try {
            URL mUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
            Log.i(TAG, "requestGET url=" + url);
            // LogUtil.i(TAG, "url=" + conn.getURL().toString());
            conn.setRequestMethod(REQUEST_GET);
            conn.setConnectTimeout(REQUEST_TIMEOUT);
            conn.setReadTimeout(REQUEST_READ_TIMEOUT);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            if (conn.getResponseCode() == REQUEST_SUCCESS && cbWeak.get() != null) {
                cbWeak.get().onSuccess(conn.getResponseCode(),
                        readInputStream(conn.getInputStream()));
            } else {
                if (cbWeak.get() != null) {
                    cbWeak.get().onFail(conn.getResponseCode(),
                            readInputStream(conn.getErrorStream()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            if (cbWeak.get() != null) {
                cbWeak.get().onFail(REPORT_MSG_FAIL,
                        REPORT_FAIL);
            } else {
                Log.e(TAG, "callback is null!");
            }
        }
    }

    /**
     * 从输入流中读取数据
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public static String readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        String data = outStream.toString(UTF8_ENCODING);
        outStream.close();
        inStream.close();
        return data;
    }

    /**
     * 配置url 格式
     *
     * @param url
     * @param queries
     * @return
     */
    public static URL propertURL(String url, Map<String, String> queries) {
        Log.w("yjl", url);
        StringBuilder sb = new StringBuilder();
        URL u = null;
        sb.append(url);

        if (queries != null && queries.size() > 0) {
            sb.append("?");
            for (Map.Entry<String, String> entry : queries.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue())
                        .append("&");
            }
            sb.setLength(sb.length() - 1);
        }

        try {
            u = new URL(sb.toString());
        } catch (MalformedURLException e) {
        }

        return u;
    }

}
