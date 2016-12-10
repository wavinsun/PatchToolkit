package cn.mutils.app.patch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

/**
 * Created by wenhua.ywh on 2016/12/9.
 */
public class RestartAppThread extends Thread {

    private Context mContext;
    private int mDelaySeconds;

    public RestartAppThread(Context context, int delaySeconds) {
        super();
        mContext = context;
        mDelaySeconds = delaySeconds;
    }

    @Override
    public void run() {
        while (true) {
            boolean isAlwaysBackground = true;
            for (int i = 0; i < mDelaySeconds; i++) {
                Log.d(RestartAppThread.class.getSimpleName(), "run:" + i);
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!SoHotfixUtil.isAppInBackground(mContext)) {
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
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, restartIntent);
        System.exit(0);
        Process.killProcess(Process.myPid());
    }

}
