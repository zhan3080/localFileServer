package com.example.localfileserver;

import android.os.Environment;

import java.io.File;

public class Constant {

    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";

//    public static final String LOCAL_MEDIA_PATH = "/local_media/";
    public static final String LOCAL_MEDIA_PATH = "/demo/local_media/";
    public static final String SDCARD_LOCAL_MEDIA_PATH = Environment.getExternalStorageDirectory()
            + LOCAL_MEDIA_PATH ;
    public static final String DEFAULT_LOCAL_PHOTO = SDCARD_LOCAL_MEDIA_PATH + "ic_launcher-web.png";
}
