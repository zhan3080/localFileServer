package com.example.localfileserver;


import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

public class AssetsUtil {

    private static final String TAG = "AssetsUtil";
    private static AssetsUtil instance;
    private static final int SUCCESS = 1;
    private static final int FAILED = 0;
    private Context context;
    private FileOperateCallback callback;
    private volatile boolean isSuccess;
    private String errorStr;

    public static AssetsUtil getInstance(Context context) {
        if (instance == null)
            instance = new AssetsUtil(context);
        return instance;
    }

    private AssetsUtil(Context context) {
        this.context = context;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (callback != null) {
                if (msg.what == SUCCESS) {
                    callback.onSuccess();
                }
                if (msg.what == FAILED) {
                    callback.onFailed(msg.obj.toString());
                }
            }
        }
    };

    public AssetsUtil copyAssetsToSD(final String srcPath, final String sdPath) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.i(TAG, "copyAssetsToDst srcPath:" + srcPath + ", sdPath:" + sdPath);
                copyAssetsToDst(context, srcPath, sdPath);
                if (isSuccess) {
                    handler.obtainMessage(SUCCESS).sendToTarget();
                } else {
                    handler.obtainMessage(FAILED, errorStr).sendToTarget();
                }
            }

        }).start();
        return this;
    }

    public void setFileOperateCallback(FileOperateCallback callback) {
        this.callback = callback;
    }

    private void copyAssetsToDst(Context context, String srcPath, String dstPath) {
        try {
            String fileNames[] = context.getAssets().list(srcPath);
            if (fileNames.length > 0) {
                File file = new File(Environment.getExternalStorageDirectory(), dstPath);
                Log.i(TAG, "copyAssetsToDst dstPath:" + file.getAbsolutePath() + ",exists:" + file.exists());
                if (!file.exists()) {
                    Log.i(TAG,file.getAbsolutePath() + " issuccess:" + file.mkdirs());
                }
                Log.i(TAG, "copyAssetsToDst dstPath exists:" + file.exists());
                for (String fileName : fileNames) {
                    Log.i(TAG, "copyAssetsToDst fileName:" + fileName);
                    if (!srcPath.equals("")) { // assets ?????????????????????
                        copyAssetsToDst(context, srcPath + File.separator + fileName, dstPath + File.separator + fileName);
                    } else { // assets ?????????
                        copyAssetsToDst(context, fileName, dstPath + File.separator + fileName);
                    }
                }
            } else {
                Log.i(TAG, "copyAssetsToDst dstPath:" + dstPath);
                File outFile = new File(Environment.getExternalStorageDirectory(), dstPath);
                InputStream is = context.getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
            isSuccess = true;
        } catch (Exception e) {
            Log.w(TAG, e);
            errorStr = e.getMessage();
            isSuccess = false;
        }
    }


    public static String getRandomColor() {
        String red;
        String green;
        String blue;
        Random random = new Random();
        red = Integer.toHexString(random.nextInt(256)).toUpperCase();
        green = Integer.toHexString(random.nextInt(256)).toUpperCase();
        blue = Integer.toHexString(random.nextInt(256)).toUpperCase();
        red = red.length() == 1 ? "0" + red : red;
        green = green.length() == 1 ? "0" + green : green;
        blue = blue.length() == 1 ? "0" + blue : blue;
        String color = "#" + red + green + blue;
        return color;
    }


    public interface FileOperateCallback {
        void onSuccess();

        void onFailed(String error);
    }

}
