package cn.mutils.app.patch.demo;

import android.app.Application;

/**
 * Created by wenhua.ywh on 2016/12/5.
 */
public class MainApplication extends Application {

    private static Application sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        MainSoHotfix.getInstance().injectHotfix();
    }

    public static Application getApplication() {
        return sApplication;
    }

}
