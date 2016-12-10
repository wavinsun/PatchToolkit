package cn.mutils.app.patch;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by wenhua.ywh on 2016/12/9.
 */
public class SoHotfixUtil {

    public static String getPath(SoHotfixContext context, int version) {
        return context.getHotfixRoot() + "/" + version;
    }

    public static boolean isAppInBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

}
