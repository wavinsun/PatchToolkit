package cn.mutils.app.patch;

import android.content.Context;
import android.os.Looper;

import cn.mutils.app.patch.util.RsaUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by wenhua.ywh on 2016/12/8.
 */
public class SoHotfix {

    private SoHotfixContext mContext; // 上下文
    private SoHotfixFileMgr mFileMgr; // 文件管理
    private SoHotFixConf mConf; // 配置
    private String mPublicKey; // 公钥
    private int mKillDelaySec = 30; // 杀应用延时
    private boolean mKillAppOnHotfixOK = false; // 热修复成功时开始杀进程
    private volatile boolean mHotfixOK; // 是否热修复成功
    private boolean mKillAppStarted; // 杀进程线程是否启动

    private List<String> mLibraries = new CopyOnWriteArrayList<String>();

    public SoHotfix(Context context) {
        mContext = new SoHotfixContext(context);
        mFileMgr = new SoHotfixFileMgr(mContext);
    }

    public boolean isHotfixOK() {
        return mHotfixOK;
    }

    public boolean isKillAppOnHotfixOK() {
        return mKillAppOnHotfixOK;
    }

    public void setKillAppOnHotfixOK(boolean killAppOnHotfixOK) {
        mKillAppOnHotfixOK = killAppOnHotfixOK;
    }

    public String getPublicKey() {
        return mPublicKey;
    }

    public void setPublicKey(String publicKey) {
        mPublicKey = publicKey;
    }

    public int getKillDelaySec() {
        return mKillDelaySec;
    }

    public void setKillDelaySec(int restartDelaySec) {
        mKillDelaySec = restartDelaySec;
    }

    private synchronized void tryToLoadConf() {
        if (mConf != null) {
            return;
        }
        mConf = new SoHotFixConf(mContext.getContext());
    }

    public synchronized void checkLoadConf() {
        tryToLoadConf();
        String appVersion = mContext.getPackageVersion();
        if (!mConf.getAppVersion().equals(appVersion)) {
            mConf.resetForAppVersion(appVersion);
        }
    }

    public SoHotfixContext getSoContext() {
        return mContext;
    }

    public void addLibrary(String libName) {
        if (libName == null || libName.isEmpty()) {
            return;
        }
        if (!mLibraries.contains(libName)) {
            mLibraries.add(libName);
        }
    }

    public void loadLibraries() {
        if (!mFileMgr.isHotfixRootExists()) {
            for (String libName : mLibraries) {
                System.loadLibrary(libName);
            }
            return;
        }
        tryToLoadConf();
        int soVersion = mConf.getVersion();
        if (soVersion == -1) {
            for (String libName : mLibraries) {
                System.loadLibrary(libName);
            }
            return;
        }
        if (mConf.isTransaction()) { // 上一次加载so崩溃
            for (String libName : mLibraries) {
                System.loadLibrary(libName);
            }
            mConf.onLoadError();
            return;
        }
        mConf.setTransaction(true); // 准备加载，设置全局标识
        if (loadLibraries(soVersion)) {
            mConf.setTransaction(false); // 加载成功，重置全局标识
            mConf.setSuccessVersion(soVersion);
        } else { // 加载出错
            mConf.onLoadError();
        }
    }

    private boolean loadLibraries(int soVersion) {
        if (soVersion == -1) {
            return false;
        }
        boolean success = true;
        int hotfixCount = 0;
        for (String libName : mLibraries) {
            File soFile = mFileMgr.getHotfixSo(libName, soVersion);
            if (soFile == null) {
                System.loadLibrary(libName);
            } else {
                hotfixCount++;
                try {
                    System.load(soFile.getPath());
                } catch (Throwable e) {
                    success = false;
                    System.loadLibrary(libName);
                }
            }
        }
        return success && hotfixCount != 0;
    }

    public void hotfix(SoHotfixCallback callback, File zipFile, String sign, int version) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("You can not call on main thread");
        }
        if (mHotfixOK) {
            callback.onHotfixCallback(SoHotfixCallback.ERROR_RESTART_APP);
            return;
        }
        checkLoadConf();
        if (isInvalidVersionForHotfix(version)) {
            callback.onHotfixCallback(SoHotfixCallback.ERROR_UNKNOWN);
            return;
        } else {
            mConf.setUpdateVersion(version);
        }
        if (!RsaUtil.verify(zipFile, sign, getPublicKey())) {
            callback.onHotfixCallback(SoHotfixCallback.ERROR_SIGN);
            saveHotfixErrorVersion(version);
            return;
        }
        if (!mFileMgr.unzipSo(zipFile, version)) {
            callback.onHotfixCallback(SoHotfixCallback.ERROR_UNZIP);
            saveHotfixErrorVersion(version);
            return;
        }
        if (!mFileMgr.patchSo(version)) {
            callback.onHotfixCallback(SoHotfixCallback.ERROR_PATCH);
            saveHotfixErrorVersion(version);
            return;
        }
        if (!mFileMgr.checkMD5(version)) {
            callback.onHotfixCallback(SoHotfixCallback.ERROR_CHECK_MD5);
            saveHotfixErrorVersion(version);
            return;
        }
        mHotfixOK = true;
        callback.onHotfixCallback(SoHotfixCallback.ERROR_OK);
        mConf.setVersion(version);
        if (mKillAppOnHotfixOK) {
            killApp();
        }
    }

    private boolean isInvalidVersionForHotfix(int version) {
        if (mConf == null) {
            return false;
        }
        int update = mConf.getUpdateVersion();
        int current = mConf.getVersion();
        int error = mConf.getErrorVersion();
        return version <= update && ((current != -1 && version <= current) || (error != -1 && version <= error));
    }

    private void saveHotfixErrorVersion(int version) {
        if (version > mConf.getErrorVersion()) {
            mConf.setErrorVersion(version);
        }
    }

    private void killApp() {
        if (mKillAppStarted) {
            return;
        }
        mKillAppStarted = true;
        new KillAppThread(this, mKillDelaySec).start();
    }

    protected boolean onInterceptKillApp() {
        return false;
    }

    public boolean isAppWorking() {
        return false;
    }

}
