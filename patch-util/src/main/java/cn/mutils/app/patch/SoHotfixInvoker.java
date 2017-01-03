package cn.mutils.app.patch;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

/**
 * Created by wenhua.ywh on 2016/12/21.
 */
public class SoHotfixInvoker {

    public static final long TIMEOUT_DEFAULT = 360000;

    private Object mLock = new Object();
    private volatile int mResultCode = SoHotfix.ERROR_INVALID;
    private SoHotfixExtra mRequestExtra;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (SoHotfixInvoker.this) {
                mResultCode = SoHotfix.ERROR_TIMEOUT;
            }
            synchronized (mLock) {
                mLock.notify();
            }
        }
    };

    public SoHotfixInvoker(String url, String sign, int version) {
        mRequestExtra = new SoHotfixExtra();
        mRequestExtra.setUrl(url);
        mRequestExtra.setSign(sign);
        mRequestExtra.setVersion(version);
        mRequestExtra.setReceiver(new InvokerReceiver());
    }

    public SoHotfixInvoker(String filePath, String sign, String version) {
        mRequestExtra = new SoHotfixExtra();
        mRequestExtra.setFilePath(filePath);
        mRequestExtra.setSign(sign);
        try {
            mRequestExtra.setVersion(Integer.parseInt(version));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRequestExtra.setReceiver(new InvokerReceiver());
    }

    public int invoke(Context context, String remoteServicePackage) {
        return invoke(context, remoteServicePackage, TIMEOUT_DEFAULT);
    }

    public int invoke(Context context, String remoteServicePackage, long timeout) {
        Intent intent = new Intent();
        intent.setPackage(remoteServicePackage);
        intent.setAction(SoHotfixService.ACTION_UPDATE);
        return invoke(context, intent, timeout);
    }

    public int invoke(Context context, Class<? extends SoHotfixService> service) {
        return invoke(context, service, TIMEOUT_DEFAULT);
    }

    public int invoke(Context context, Class<? extends SoHotfixService> service, long timeout) {
        Intent intent = new Intent(context, service);
        intent.setAction(SoHotfixService.ACTION_UPDATE);
        return invoke(context, intent, timeout);
    }

    private int invoke(Context context, Intent intent, long timeout) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("You can not call on main thread");
        }
        mRequestExtra.putTo(intent);
        ComponentName component = context.startService(intent);
        if (component == null) {
            return SoHotfix.ERROR_UNKNOWN;
        }
        mHandler.postDelayed(mTimeoutRunnable, timeout);
        synchronized (mLock) {
            try {
                mLock.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mResultCode;
    }

    @SuppressLint("ParcelCreator")
    class InvokerReceiver extends ResultReceiver {

        public InvokerReceiver() {
            super(null);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mHandler.removeCallbacks(mTimeoutRunnable);
            synchronized (SoHotfixInvoker.this) {
                if (mResultCode == SoHotfix.ERROR_TIMEOUT) {
                    return;
                }
                mResultCode = resultCode;
            }
            synchronized (mLock) {
                mLock.notify();
            }
        }
    }

}
