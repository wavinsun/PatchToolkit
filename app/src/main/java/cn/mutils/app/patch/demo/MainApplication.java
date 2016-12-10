package cn.mutils.app.patch.demo;

import android.app.Application;

/**
 * Created by wenhua.ywh on 2016/12/5.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new MainSoHotfix(this).loadLibraries();
    }

}
