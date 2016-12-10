package cn.mutils.app.patch.util;

import android.util.Base64;

/**
 * Created by wenhua.ywh on 2016/12/10.
 */
public class Base64Util {

    private static final String CHARSET = "UTF-8";

    public static byte[] toBytes(String base64) {
        try {
            return Base64.decode(base64.getBytes(CHARSET), Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    public static String fromBytes(byte[] bytes) {
        try {
            return new String(Base64.encode(bytes, Base64.DEFAULT), CHARSET);
        } catch (Exception e) {
            return null;
        }
    }

}
