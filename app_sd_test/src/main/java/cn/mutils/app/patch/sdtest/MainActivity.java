package cn.mutils.app.patch.sdtest;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.mutils.app.patch.SoHotfix;
import cn.mutils.app.patch.SoHotfixInvoker;
import cn.mutils.app.patch.util.FileUtil;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String REMOVE_PACKAGE = "cn.mutils.app.patch.demo";

    private static final ConcurrentHashMap<Integer, String> mErrorMap = new ConcurrentHashMap<Integer, String>();

    static {
        mErrorMap.put(SoHotfix.ERROR_CHECK_MD5, "MD5校验错误");
        mErrorMap.put(SoHotfix.ERROR_DOWNLOAD, "下载错误");
        mErrorMap.put(SoHotfix.ERROR_EXITS, "已存在该版本");
        mErrorMap.put(SoHotfix.ERROR_NOT_WIFI, "没有WIFI");
        mErrorMap.put(SoHotfix.ERROR_PATCH, "补丁合并错误");
        mErrorMap.put(SoHotfix.ERROR_SIGN, "签名错误");
        mErrorMap.put(SoHotfix.ERROR_TIMEOUT, "操作超时");
        mErrorMap.put(SoHotfix.ERROR_UNKNOWN, "未知错误");
        mErrorMap.put(SoHotfix.ERROR_UNZIP, "解压错误");
    }

    private HandlerThread mHandlerThread;
    private TextView mMessageTextView;
    private Button mInvokeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessageTextView = (TextView) findViewById(R.id.message);
        mInvokeButton = (Button) findViewById(R.id.so_hotfix_invoke);
        mInvokeButton.setOnClickListener(this);

        mHandlerThread = new HandlerThread("MainActivityThread");
        mHandlerThread.start();
        if (isPatchFilesOK()) {
            mMessageTextView.setText("准备就绪");
        } else {
            mMessageTextView.setText("未发现补丁文件");
        }
    }

    @Override
    protected void onDestroy() {
        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.so_hotfix_invoke: {
                if (!isPatchFilesOK()) {
                    mMessageTextView.setText("测试失败，未发现补丁文件");
                    return;
                }
                new Handler(mHandlerThread.getLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        File sdCard = Environment.getExternalStorageDirectory();
                        File signFile = new File(sdCard.getPath() + "/patch/patch.sign");
                        String signStr = FileUtil.getString(signFile);
                        if (signStr == null) {
                            setResultMessage("测试失败，无法读取签名");
                            return;
                        }
                        File versionFile = new File(sdCard.getPath() + "/patch/patch.version");
                        String versionStr = FileUtil.getString(versionFile);
                        if (versionStr == null) {
                            setResultMessage("测试失败，无法读取版本信息");
                            return;
                        }
                        try {
                            Integer.parseInt(versionStr);
                        } catch (Exception e) {
                            setResultMessage("测试失败，版本错误");
                            e.printStackTrace();
                            return;
                        }
                        String remoteName = "";
                        try {
                            PackageManager pm = getPackageManager();
                            ApplicationInfo appInfo = pm.getPackageInfo(REMOVE_PACKAGE, PackageManager.GET_META_DATA).applicationInfo;
                            remoteName = pm.getApplicationLabel(appInfo).toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                            setResultMessage("测试失败，无法调用" + REMOVE_PACKAGE);
                            return;
                        }
                        String zipFilePath = sdCard.getPath() + "/patch/patch.zip";
                        SoHotfixInvoker invoker = new SoHotfixInvoker(zipFilePath, signStr, versionStr);
                        int errorCode = invoker.invoke(MainActivity.this, REMOVE_PACKAGE);
                        if (errorCode == SoHotfix.ERROR_OK) {
                            setResultMessage("测试成功，请杀进程重启目标应用" + remoteName);
                            return;
                        }
                        String errorStr = mErrorMap.get(errorCode);
                        if (errorStr == null) {
                            errorStr = "未知错误";
                        }
                        setResultMessage("测试失败，" + errorStr);
                    }
                });
                mMessageTextView.setText("正在打补丁... ...");
                mInvokeButton.setEnabled(false);
                break;
            }
        }
    }

    private boolean isPatchFilesOK() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return false;
        }
        File sdCard = Environment.getExternalStorageDirectory();
        File patchDir = new File(sdCard, "patch");
        if (!patchDir.isDirectory()) {
            return false;
        }
        File zipFile = new File(patchDir, "patch.zip");
        if (!zipFile.isFile()) {
            return false;
        }
        File signFile = new File(patchDir, "patch.sign");
        if (!signFile.isFile()) {
            return false;
        }
        File versionFile = new File(patchDir, "patch.version");
        if (!versionFile.isFile()) {
            return false;
        }
        return true;
    }

    private void setResultMessage(final String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mMessageTextView.setText(message);
            if (!mInvokeButton.isEnabled()) {
                mInvokeButton.setEnabled(true);
            }
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mMessageTextView.setText(message);
                if (!mInvokeButton.isEnabled()) {
                    mInvokeButton.setEnabled(true);
                }
            }
        });
    }

}
