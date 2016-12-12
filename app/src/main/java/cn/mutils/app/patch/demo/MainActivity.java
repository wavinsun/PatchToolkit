package cn.mutils.app.patch.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import cn.mutils.app.lib.test.Test;
import cn.mutils.app.patch.SoHotfixCallback;
import cn.mutils.app.patch.util.FileUtil;
import cn.mutils.app.patch.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.get_message).setOnClickListener(this);
        findViewById(R.id.hotfix).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_message: {
                Toast.makeText(this, Test.getMessage(), Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.hotfix: {
                File dir = new File(Environment.getExternalStorageDirectory() + "/wavinsun/so_hotfix");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                final File zipFile = new File(dir, "libTest.zip");
                if (copyAssetToSD(zipFile.getName(), zipFile)) {
                    refreshFile(zipFile);
                } else {
                    toast("Copy asset error");
                }
                File signFile = new File(dir, "libTest.zip.sign");
                if (copyAssetToSD(signFile.getName(), signFile)) {
                    refreshFile(zipFile);
                } else {
                    toast("Copy asset error");
                }
                final String sign = FileUtil.getString(signFile);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainSoHotfix.getInstance().hotfix(new SoHotfixCallback() {

                            @Override
                            public void onHotfixCallback(int errorCode) {
                                if (errorCode == SoHotfixCallback.ERROR_OK) {
                                    toast("Hotfix success, Application will restart while in background for 5s!");
                                } else {
                                    toast("Hotfix error : " + errorCode);
                                }
                            }
                        }, zipFile, sign, 0);
                    }
                }).start();
                break;
            }
        }
    }

    private void toast(final String message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    toast(message);
                }
            });
            return;
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private boolean copyAssetToSD(String assetFileName, File sdFile) {
        FileOutputStream fos = null;
        try {
            InputStream is = this.getAssets().open(assetFileName);
            fos = new FileOutputStream(sdFile);
            IOUtil.copy(is, fos);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            IOUtil.close(fos);
        }
    }

    public void refreshFile(File newFile) {
        Uri data = Uri.parse("file:///" + newFile.getAbsolutePath());
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
    }
}
