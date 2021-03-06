package cn.mutils.app.patch;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by wenhua.ywh on 2016/12/9.
 */
public class SoHotfixContext {

    private static final String LIB_HOTFIX = "lib_hotfix";
    private static final String LIB_HOTFIX_TEMP = "lib_hotfix$";

    private final Context mContext; // 系统上下文
    private final String mLibRoot; // 原始的so根路径
    private final String mHotfixRoot; // 热更新so根路径
    private final String mPackageName; // 包名
    private String mPackageVersion; // 版本
    private int mPackageVersionCode; // 版本名称
    private String mHotfixRootName; // 热更新so根路径名称
    private final String mTempHotfixRoot; //  热更新so暂时根目录

    public SoHotfixContext(Context context) {
        mContext = context;
        mPackageName = mContext.getPackageName();
        mLibRoot = "/data/data/" + mPackageName + "/lib";
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo info = pm.getPackageInfo(mPackageName, 0);
            mPackageVersion = info.versionName;
            mPackageVersionCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHotfixRootName = mPackageVersion + "." + mPackageVersionCode;
        mHotfixRoot = "/data/data/" + mPackageName + "/files/" + LIB_HOTFIX + "/" + mHotfixRootName;
        mTempHotfixRoot = "/data/data/" + mPackageName + "/files/" + LIB_HOTFIX_TEMP + "/" + mHotfixRootName;
    }

    public Context getContext() {
        return mContext;
    }

    public String getLibRoot() {
        return mLibRoot;
    }

    public String getHotfixRoot() {
        return mHotfixRoot;
    }

    public String getHotfixRootName() {
        return mHotfixRootName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getPackageVersion() {
        return mPackageVersion;
    }

    public int getPackageVersionCode() {
        return mPackageVersionCode;
    }

    public String getTempHotfixRoot() {
        return mTempHotfixRoot;
    }
}
