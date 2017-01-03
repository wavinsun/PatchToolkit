package cn.mutils.app.patch.demo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import cn.mutils.app.lib.test.Test;
import cn.mutils.app.patch.SoHotfixInvoker;
import cn.mutils.app.patch.util.FileUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static {
        System.loadLibrary("Test");
    }

    private static final String TAG = MainActivity.class.getSimpleName();

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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File dir = new File(Environment.getExternalStorageDirectory() + "/wavinsun/so_hotfix");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File signFile = new File(dir, "libTest.zip.sign");
                        if (MainUtil.copyAssetToSD(MainActivity.this, signFile.getName(), signFile)) {
                            MainUtil.refreshFile(MainActivity.this, signFile);
                        }
                        final String sign = FileUtil.getString(signFile);
                        SoHotfixInvoker invoker = new SoHotfixInvoker("", sign, 0);
                        Log.d(TAG, "begin hotfix: " + System.currentTimeMillis());
                        int resultCode = invoker.invoke(MainActivity.this, MainSoHotfixService.class);
                        Log.d(TAG, "end hotfix:" + System.currentTimeMillis());
                        Log.d(TAG, "resultCode:" + resultCode);
                    }
                }).start();
                break;
            }
        }
    }

}
