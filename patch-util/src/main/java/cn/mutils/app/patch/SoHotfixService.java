package cn.mutils.app.patch;

import android.app.IntentService;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import java.io.File;

/**
 * Created by wenhua.ywh on 2016/12/20.
 */
public abstract class SoHotfixService extends IntentService {

    public static final String ACTION_UPDATE = "cn.mutils.app.intent.action.SO_HOTFIX";

    private static final String TAG = SoHotfixService.class.getSimpleName();

    public SoHotfixService() {
        super(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.exit(0);
        Process.killProcess(Process.myPid());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (ACTION_UPDATE.equals(action)) {
            Log.d(TAG, "Begin so hotfix");
            doSoHotfix(new SoHotfixExtra(intent));
            Log.d(TAG, "End so hotfix");
            return;
        }
    }

    protected void doSoHotfix(SoHotfixExtra extra) {
        File zipFile = null;
        String filePath = extra.getFilePath();
        if (filePath != null && !filePath.isEmpty()) {
            zipFile = new File(filePath);
        } else {
            if (!SoHotfixUtil.isWifi(this)) {
                extra.getReceiver().send(SoHotfix.ERROR_NOT_WIFI, null);
                return;
            }
            zipFile = doDownload(extra.getUrl());
            if (zipFile == null || !zipFile.exists()) {
                extra.getReceiver().send(SoHotfix.ERROR_DOWNLOAD, null);
                return;
            }
        }
        SoHotfix hotfix = getSoHotfix();
        if (hotfix == null) {
            extra.getReceiver().send(SoHotfix.ERROR_UNKNOWN, null);
        }
        extra.getReceiver().send(hotfix.hotfix(zipFile, extra.getSign(), extra.getVersion()), null);
    }

    abstract protected File doDownload(String url);

    abstract protected SoHotfix getSoHotfix();


}
