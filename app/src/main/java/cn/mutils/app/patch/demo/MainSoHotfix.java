package cn.mutils.app.patch.demo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import cn.mutils.app.patch.SoHotfix;

/**
 * Created by wenhua.ywh on 2016/12/9.
 */
public class MainSoHotfix extends SoHotfix {

    private static final String KEY_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDC4tg1qvDeskWjX42kzZF6rbLexcePSCCjCTb0qSJuUE61Vuiflms/OQBAQH1n07+tQ/yeMG8yfredhNgeMSHv59vTBvUmZOlnukIM1ZioiUBZ6BxNYOS8lqyh5zVDCMhU8V20MAhaRnV0xeAw/a0sV8lL53Y2jTKvmr7VYnV+QIDAQAB";
	private static volatile MainSoHotfix sInstance;

    private MainSoHotfix(Context context) {
        super(context);
        this.setPublicKey(KEY_PUBLIC);
        this.setKillDelaySec(5);
        this.setKillAppOnHotfixOK(true);
        this.addLibrary("Test");
    }

    public static MainSoHotfix getInstance() {
        if (sInstance == null) {
            synchronized (MainSoHotfix.class) {
                if (sInstance == null) {
                    sInstance = new MainSoHotfix(MainApplication.getApplication());
                }
            }
        }
        return sInstance;
    }

    @Override
    protected boolean onInterceptKillApp() {
        Context context = getSoContext().getContext();
        Intent intent = new Intent();
        intent.setAction(MainApplication.INTENT_ACTION_RESTART);
        PendingIntent restartIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, restartIntent);
        return super.onInterceptKillApp();
    }

    @Override
    public boolean isAppWorking() {
        return super.isAppWorking();
    }
}
