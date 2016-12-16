package cn.mutils.app.patch;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wenhua.ywh on 2016/12/8.
 */
class SoHotFixConf {

    private static final String PREF_FILE_NAME = "so_hotfix"; // 文件名
    private static final String PREF_KEY_TRANSACTION = "transaction"; // 加载SO全局标识
    private static final String PREF_KEY_VERSION = "version"; // 正在使用的版本
    private static final String PREF_KEY_VERSION_SUCCESS = "version_success"; // 上一次正确的版本
    private static final String PREF_KEY_VERSION_UPDATE = "version_update"; //  需要更新的版本
    private static final String PREF_KEY_VERSION_APP = "version_app";

    private SharedPreferences mSharedPref;
    private boolean mTransaction = false;
    private int mVersion = -1;
    private int mSuccessVersion = -1;
    private int mUpdateVersion = -1;
    private String mAppVersion = "";

    public SoHotFixConf(Context context) {
        mSharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        mTransaction = mSharedPref.getBoolean(PREF_KEY_TRANSACTION, false);
        mVersion = mSharedPref.getInt(PREF_KEY_VERSION, -1);
        mSuccessVersion = mSharedPref.getInt(PREF_KEY_VERSION_SUCCESS, -1);
        mUpdateVersion = mSharedPref.getInt(PREF_KEY_VERSION_UPDATE, -1);
        mAppVersion = mSharedPref.getString(PREF_KEY_VERSION_APP, "");
    }

    public String getAppVersion() {
        return mAppVersion;
    }

    public synchronized void setAppVersion(String appVersion) {
        if (appVersion == null || appVersion.isEmpty()) {
            return;
        }
        if (mAppVersion.equals(appVersion)) {
            return;
        }
        mAppVersion = appVersion;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PREF_KEY_VERSION_APP, mAppVersion);
        editor.commit();
    }

    public synchronized void resetForAppVersion(String appVersion) {
        if (appVersion == null || appVersion.isEmpty()) {
            return;
        }
        if (mAppVersion.equals(appVersion)) {
            return;
        }
        mAppVersion = appVersion;
        mTransaction = false;
        mVersion = -1;
        mSuccessVersion = -1;
        mUpdateVersion = -1;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PREF_KEY_VERSION_APP, mAppVersion);
        editor.putBoolean(PREF_KEY_TRANSACTION, false);
        editor.putInt(PREF_KEY_VERSION, -1);
        editor.putInt(PREF_KEY_VERSION_SUCCESS, -1);
        editor.putInt(PREF_KEY_VERSION_UPDATE, -1);
        editor.commit();
    }

    public boolean isTransaction() {
        return mTransaction;
    }

    public synchronized void setTransaction(boolean transaction) {
        if (mTransaction == transaction) {
            return;
        }
        mTransaction = transaction;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PREF_KEY_TRANSACTION, mTransaction);
        editor.commit();
    }

    public int getVersion() {
        return mVersion;
    }

    public synchronized void setVersion(int version) {
        if (mVersion == version) {
            return;
        }
        mVersion = version;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(PREF_KEY_VERSION, mVersion);
        editor.commit();
    }

    public int getSuccessVersion() {
        return mSuccessVersion;
    }

    public synchronized void setSuccessVersion(int successVersion) {
        if (mSuccessVersion == successVersion) {
            return;
        }
        mSuccessVersion = successVersion;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(PREF_KEY_VERSION_SUCCESS, mSuccessVersion);
        editor.commit();
    }

    public int getUpdateVersion() {
        return mUpdateVersion;
    }

    public synchronized void setUpdateVersion(int updateVersion) {
        if (mUpdateVersion == updateVersion) {
            return;
        }
        mUpdateVersion = updateVersion;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(PREF_KEY_VERSION_UPDATE, mUpdateVersion);
        editor.commit();
    }

    public void onLoadError() {
        mTransaction = false;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PREF_KEY_TRANSACTION, mTransaction);
        if (mSuccessVersion != -1 && mSuccessVersion < mVersion) {
            mVersion = mSuccessVersion; // 回滚上一次成功的so
        } else {
            mVersion = -1; // 回滚到原始so
        }
        editor.putInt(PREF_KEY_VERSION, mVersion);
        editor.commit();
    }

}
