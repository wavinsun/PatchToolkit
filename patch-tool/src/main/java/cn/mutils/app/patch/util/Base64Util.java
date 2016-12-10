package cn.mutils.app.patch.util;


import org.apache.commons.codec.binary.Base64;

/**
 * Created by wenhua.ywh on 2016/12/10.
 */
public class Base64Util {

    private static final String CHARSET = "UTF-8";

    public static byte[] toBytes(String base64) {
        try {
            return Base64.decodeBase64(base64);
        } catch (Exception e) {
            return null;
        }
    }

    public static String fromBytes(byte[] bytes) {
        try {
            return Base64.encodeBase64String(bytes);
        } catch (Exception e) {
            return null;
        }
    }

}
