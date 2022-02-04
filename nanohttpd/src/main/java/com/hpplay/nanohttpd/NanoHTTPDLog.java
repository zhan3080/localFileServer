package com.hpplay.nanohttpd;


import android.util.Log;

/**
 * Created by Zippo on 2018/3/24.
 * Date: 2018/3/24
 * Time: 15:59:12
 */

public class NanoHTTPDLog {

    private static final String NANOHTTPD_TAG = "NanoHTTPD";
    private static final String LEBO_TAG = "hpplay-java";

    private static boolean sDebugMode = false;

    public static void setDebug(boolean debugMode) {
        sDebugMode = debugMode;
    }

    /**
     * 大写表示不打印循环之类的信息
     *
     * @param tag
     * @param msg
     */
    public static void V(String tag, String msg) {
        if (sDebugMode) {
            msg = formatMessage(tag, msg);
            Log.v(LEBO_TAG, NANOHTTPD_TAG + msg);
        }
    }

    public static void v(String tag, String msg) {
        msg = formatMessage(tag, msg);
        Log.v(LEBO_TAG, NANOHTTPD_TAG + msg);
    }

    public static void V(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.v(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void v(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.v(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void D(String tag, String msg) {
        if (sDebugMode) {
            msg = formatMessage(tag, msg);
            Log.d(LEBO_TAG, NANOHTTPD_TAG + msg);
        }
    }

    public static void d(String tag, String msg) {
        msg = formatMessage(tag, msg);
        Log.d(LEBO_TAG, NANOHTTPD_TAG + msg);
    }

    public static void D(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.d(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void d(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.d(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void I(String tag, String msg) {
        msg = formatMessage(tag, msg);
        Log.i(LEBO_TAG, NANOHTTPD_TAG + msg);
    }

    public static void i(String tag, String msg) {
        msg = formatMessage(tag, msg);
        Log.i(LEBO_TAG, NANOHTTPD_TAG + msg);
    }

    public static void I(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.i(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void i(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.i(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void W(String tag, String msg) {
        msg = formatMessage(tag, msg);
        Log.w(LEBO_TAG, NANOHTTPD_TAG + msg);
    }

    public static void w(String tag, String msg) {
        msg = formatMessage(tag, msg);
        Log.w(LEBO_TAG, NANOHTTPD_TAG + msg);
    }

    public static void W(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.w(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void w(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.w(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void W(String tag, Throwable tr) {
        String msg = formatMessage(tag, null);
        Log.w(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void w(String tag, Throwable tr) {
        String msg = formatMessage(tag, null);
        Log.w(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void E(String tag, String msg) {
        msg = formatMessage(tag, msg);
        Log.e(LEBO_TAG, NANOHTTPD_TAG + msg);
    }

    public static void e(String tag, String msg) {
        msg = formatMessage(tag, msg);
        Log.e(LEBO_TAG, NANOHTTPD_TAG + msg);
    }

    public static void E(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.e(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        msg = formatMessage(tag, msg);
        Log.e(LEBO_TAG, NANOHTTPD_TAG + msg, tr);
    }

    private static String formatMessage(String tag, String msg) {
        if (tag == null) {
            tag = "";
        }
        if (msg == null) {
            msg = "";
        }
        String ret = tag + ":" + msg;
        return ret;
    }
}
