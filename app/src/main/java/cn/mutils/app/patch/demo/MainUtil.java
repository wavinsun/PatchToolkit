package cn.mutils.app.patch.demo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import cn.mutils.app.patch.util.IOUtil;

/**
 * Created by wenhua.ywh on 2016/12/22.
 */
public class MainUtil {

    public static boolean copyAssetToSD(Context context, String assetFileName, File sdFile) {
        FileOutputStream fos = null;
        try {
            InputStream is = context.getAssets().open(assetFileName);
            fos = new FileOutputStream(sdFile);
            IOUtil.copy(is, fos);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            IOUtil.close(fos);
        }
    }

    public static void refreshFile(Context context, File newFile) {
        Uri data = Uri.parse("file:///" + newFile.getAbsolutePath());
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
    }

}
