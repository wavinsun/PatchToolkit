package cn.mutils.app.patch.demo;

import android.content.Context;

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

}
