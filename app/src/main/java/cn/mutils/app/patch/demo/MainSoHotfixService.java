package cn.mutils.app.patch.demo;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import cn.mutils.app.patch.SoHotfix;
import cn.mutils.app.patch.SoHotfixService;

/**
 * Created by wenhua.ywh on 2016/12/20.
 */
public class MainSoHotfixService extends SoHotfixService {

    private static final String TAG = MainSoHotfixService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate:" + this.hashCode());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:" + this.hashCode());
        super.onDestroy();
    }

    @Override
    protected SoHotfix getSoHotfix() {
        return MainSoHotfix.getInstance();
    }

    @Override
    protected File doDownload(String url) {
        File dir = new File(Environment.getExternalStorageDirectory() + "/wavinsun/so_hotfix");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final File zipFile = new File(dir, "libTest.zip");
        if (MainUtil.copyAssetToSD(this, zipFile.getName(), zipFile)) {
            MainUtil.refreshFile(this, zipFile);
        }
        return zipFile;
    }
}
