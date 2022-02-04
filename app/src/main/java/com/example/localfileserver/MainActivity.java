package com.example.localfileserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FileServer";
    private EditText editView;
    private TextView textView;
    private String url = null;
    private Button transform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        editView = findViewById(R.id.uri_id);
        textView = findViewById(R.id.url_id);
        transform = findViewById(R.id.transform);
        transform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileServer.getInstance().startServer();
//                if (!TextUtils.isEmpty(editView.getText().toString())) {
//                    url = FileServer.getInstance().getFileDwonloadUrl(editView.getText().toString());
//                } else {
//                    url = FileServer.getInstance().getFileDwonloadUrl(editView.getHint().toString());
//                }
                if (!TextUtils.isEmpty(editView.getText().toString())) {
                    url = FileServer.getInstance().getFileDwonloadUrl(Environment.getExternalStorageDirectory() + File.separator + editView.getText().toString());
                }else {
                    url = FileServer.getInstance().getFileDwonloadUrl(Constant.DEFAULT_LOCAL_PHOTO);
                }
                Log.i(TAG, "onClick url:" + url);
                Log.i(TAG,"onClick isExternalStorageWritable:" + isExternalStorageWritable());
                textView.setText(url);
            }
        });
    }

    public boolean isExternalStorageWritable() {
        Log.i(TAG, "isExternalStorageWritable directory:" + Environment.getExternalStorageDirectory());
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void checkPermission() {
        String[] permissionsCheck = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        List<String> permissionList = new ArrayList<>();

        for (String permissionStr : permissionsCheck) {
            if (ContextCompat.checkSelfPermission(this,
                    permissionStr) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissionStr);
            }
        }
        String[] permissionArr = new String[]{};
        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(permissionArr), 100);
            return;
        }
        copyMediaToSDCard();
    }

    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 100) {
            Log.w(TAG, "onRequestPermissionsResult failed requestCode: " + requestCode);
            finish();
            return;
        }
        if (grantResults.length <= 0) {
            Log.w(TAG, "onRequestPermissionsResult grantResults.length: " + grantResults.length);
            finish();
            return;
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "onRequestPermissionsResult grantResults[0]: " + grantResults[0]);
            finish();
            return;
        }
        copyMediaToSDCard();
    }

    private void copyMediaToSDCard() {
        AssetsUtil.getInstance(getApplicationContext())
                .copyAssetsToSD("local_media", Constant.LOCAL_MEDIA_PATH)
                .setFileOperateCallback(new AssetsUtil.FileOperateCallback() {

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailed(String error) {
                        Log.e(TAG, error);
                    }
                });
    }

}