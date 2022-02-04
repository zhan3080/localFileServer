package com.example.localfileserver;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.concurrent.Executors;
import android.util.Log;
        
public class FileServer {

    private static final String TAG = "LelinkServerInstance";

    private LelinkFileServer mFileServer;
    private static FileServer mLelinkServerInstance;
    private Context mContext;

    private static final String HTTP_URL_HEADER = "http://";
    private String mLocalIp;
    private int mHttpPort = 8010;
    private boolean isInit;

    public static FileServer getInstance() {
        if (mLelinkServerInstance == null) {
            mLelinkServerInstance = new FileServer();
        }
        return mLelinkServerInstance;
    }


    public boolean isInit() {
        return isInit;
    }

    public void init(Context context) {
        mContext = context;
        isInit = true;
    }


    public boolean isAlive() {
        if (mFileServer != null) {
            return mFileServer.isAlive();
        }
        return false;
    }

    public void startServer() {
        if (mFileServer == null || !mFileServer.isAlive()) {
            PortCheckTask portCheckTask = new PortCheckTask();
            portCheckTask.executeOnExecutor(Executors.newCachedThreadPool());
        } else {
            Log.d(TAG, "  already start");
        }
    }


    public void stopServer() {
        if (mFileServer != null) {
            mFileServer.stop();
            mFileServer = null;
        }
        Log.d(TAG, "stop server");
    }

    /**
     * 组装文件下载地址包含任何形式的文件下载
     *
     * @return
     */
    public String getFileDwonloadUrl(String localPath) {
        String currentIP = Utils.getLoaclIp();
        Log.i(TAG, " local ip " + mLocalIp + "  current ip " + currentIP);
        if (mFileServer != null && !mFileServer.wasStarted()) {
            Log.i(TAG, " server dei restart server  ");
            startServer();
        } else if (!TextUtils.isEmpty(mLocalIp) && !mLocalIp.equals(currentIP)) {
            Log.i(TAG, "wifi change restart server  ");
            restartServer();
        }
        if (!TextUtils.isEmpty(localPath)) {
            try {
                if (localPath.startsWith(File.separator)) {
                    localPath = localPath.substring(1);
                }
                localPath = URLEncoder.encode(localPath, "utf-8");
            } catch (UnsupportedEncodingException e) {
                Log.w(TAG, e);
            }
            return HTTP_URL_HEADER + currentIP + ":" + mHttpPort + File.separator + localPath;
        }
        return "";
    }

    public void restartServer() {
        if (mFileServer != null) {
            stopServer();
        }
        startServer();
    }


    private class PortCheckTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            if (Utils.checkLoaclPort(mHttpPort)) {
                mHttpPort = mHttpPort + new Random().nextInt(10);
                Log.d(TAG, "port is use ,new port is :" + mHttpPort);
            } else {
                Log.d(TAG, "port not use");
            }
            return mHttpPort;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mHttpPort = result;
            if (mFileServer == null) {
                mLocalIp = Utils.getLoaclIp();
                mFileServer = new LelinkFileServer(mLocalIp, mHttpPort);
                try {
                    mFileServer.start();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
                Log.i(TAG, "start server " + mLocalIp + "  mHttpPort " + mHttpPort);
            } else {
                if (mFileServer.isAlive()) {
                    Log.d(TAG, "server is start");
                } else {
                    try {
                        mFileServer.stop();
                        mFileServer = new LelinkFileServer(Utils.getLoaclIp(), mHttpPort);
                        mFileServer.start();
                    } catch (Exception e) {
                        Log.w(TAG, e);
                    }

                }
            }
            super.onPostExecute(result);
        }
    }

}
