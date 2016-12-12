package cn.mutils.app.patch;

import android.os.Process;
import android.util.Log;

/**
 * Created by wenhua.ywh on 2016/12/9.
 */
public class KillAppThread extends Thread {

    private SoHotfix mSoHotfix;
    private int mDelaySeconds;

    public KillAppThread(SoHotfix soHotfix, int delaySeconds) {
        super();
        mSoHotfix = soHotfix;
        mDelaySeconds = delaySeconds;
    }

    @Override
    public void run() {
        while (true) {
            boolean isAlwaysBackground = true;
            for (int i = 0; i < mDelaySeconds; i++) {
                Log.d(KillAppThread.class.getSimpleName(), "run " + i);
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!SoHotfixUtil.isAppInBackground(mSoHotfix.getSoContext().getContext()) && !mSoHotfix.isAppWorking()) {
                    isAlwaysBackground = false;
                    break;
                }
            }
            if (isAlwaysBackground) {
                restartApp();
                return;
            }
        }
    }

    private void restartApp() {
        if (mSoHotfix.onInterceptKillApp()) {
            return;
        }
        System.exit(0);
        Process.killProcess(Process.myPid());
    }

}
