package cn.mutils.app.patch.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by wenhua.ywh on 2016/12/12.
 */
public class RestartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "重启成功", Toast.LENGTH_LONG).show();
    }

}
