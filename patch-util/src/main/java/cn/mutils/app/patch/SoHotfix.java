package cn.mutils.app.patch;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import cn.mutils.app.patch.util.RsaUtil;

import java.io.File;

/**
 * Created by wenhua.ywh on 2016/12/8.
 */
public class SoHotfix {

    public static final int ERROR_INVALID = -1; // 无效错误
    public static final int ERROR_UNKNOWN = 0; // 未知错误
    public static final int ERROR_OK = 1; // 成功
    public static final int ERROR_TIMEOUT = 2; // 超时
    public static final int ERROR_SIGN = 3; // 签名校验失败
    public static final int ERROR_PATCH = 4; // 增量打包合并so失败
    public static final int ERROR_UNZIP = 5; //  解压压缩包失败
    public static final int ERROR_CHECK_MD5 = 6; // 校验MD5
    public static final int ERROR_EXITS = 7; //  已存在更新
    public static final int ERROR_DOWNLOAD = 8; // 下载出错
    public static final int ERROR_NOT_WIFI = 9; // 非wifi网络

    private static final int DURATION_OF_TRIAL = 180000; //  补丁试用期 3分钟运行正常设置补丁永久生效

    private SoHotfixContext mContext; // 上下文
    private SoHotfixFileMgr mFileMgr; // 文件管理
    private String mPublicKey; // 公钥
    private String mAppCrashFile; // 程序崩溃文件
    private SoHotfixInjector mInjector; // 文件夹注入
    private SoHotfixLibPath mInjectLibPath; // 插入的目录

    public SoHotfix(Context context) {
        mContext = new SoHotfixContext(context);
        mFileMgr = new SoHotfixFileMgr(mContext);
        mInjector = new SoHotfixInjector(mContext);
    }

    public String getAppCrashFile() {
        return mAppCrashFile;
    }

    public void setAppCrashFile(String appCrashFile) {
        mAppCrashFile = appCrashFile;
    }

    public String getPublicKey() {
        return mPublicKey;
    }

    public void setPublicKey(String publicKey) {
        mPublicKey = publicKey;
    }

    public SoHotfixContext getSoContext() {
        return mContext;
    }

    private long getAppCrashTime() {
        long appCrashTime = 0;
        if (mAppCrashFile != null && mAppCrashFile.length() > 0) {
            appCrashTime = new File(mAppCrashFile).lastModified();
        }
        return appCrashTime;
    }

    public synchronized void injectHotfix() {
        if (mInjectLibPath != null) {
            return;
        }
        final SoHotfixLibPath libPath = mFileMgr.getLibPathOfMaxVersion();
        if (libPath == null) {
            return;
        }
        final int version = libPath.getVersion();
        File crashTag = mFileMgr.getHotfixCrashTag(version);
        if (crashTag != null) { // 补丁已经被标记试用期产生崩溃
            return;
        }
        final long time = System.currentTimeMillis();
        File okTag = null;
        File firstRunTag = mFileMgr.getHotfixFirstRunTag(version);
        if (firstRunTag == null) { // 首次运行补丁
            mFileMgr.setHotfixFirstRunTag(version, time);
        } else {
            okTag = mFileMgr.getHotfixOKTag(version);
            if (okTag == null) {
                long appCrashTime = getAppCrashTime();
                if (appCrashTime > 0) { // 程序产生崩溃
                    if (appCrashTime - firstRunTag.lastModified() < DURATION_OF_TRIAL) { // 补丁在试用期内产生崩溃
                        mFileMgr.setHotfixCrashTag(version, time);
                        return;
                    }
                }
            }
        }
        mInjectLibPath = libPath;
        mInjector.injectNativeLib(mInjectLibPath);
        if (okTag != null) {
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!SoHotfixUtil.isMainProcess(mContext.getContext())) {
                    return;
                }
                File crashTag = mFileMgr.getHotfixCrashTag(version);
                if (crashTag != null) { // 补丁已经产生崩溃
                    return;
                }
                long appCrashTime = getAppCrashTime();
                if (appCrashTime > 0) { // 程序产生崩溃
                    if (appCrashTime - time < DURATION_OF_TRIAL) {
                        return;
                    }
                }
                mFileMgr.setHotfixOKTag(version, System.currentTimeMillis());
            }
        }, DURATION_OF_TRIAL);
    }

    public synchronized void onLoadLibraryError() {
        if (mInjectLibPath == null) {
            return;
        }
        mFileMgr.setHotfixCrashTag(mInjectLibPath.getVersion(), System.currentTimeMillis());
    }

    public synchronized int hotfix(File zipFile, String sign, int version) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("You can not call on main thread");
        }
        if (mFileMgr.isHotfixExists(version)) {
            return ERROR_EXITS;
        }
        if (!RsaUtil.verify(zipFile, sign, getPublicKey())) {
            return ERROR_SIGN;
        }
        if (!mFileMgr.unzipSo(zipFile, version)) {
            return ERROR_UNZIP;
        }
        if (!mFileMgr.patchSo(version)) {
            return ERROR_PATCH;
        }
        if (!mFileMgr.checkMD5(version)) {
            return ERROR_CHECK_MD5;
        }
        mFileMgr.cleanUpZip(version);
        mFileMgr.cleanUpOldVersions();
        mFileMgr.moveTempToLibPath(version);
        return ERROR_OK;
    }

}
