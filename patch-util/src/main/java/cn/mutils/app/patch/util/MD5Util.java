package cn.mutils.app.patch.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Created by wenhua.ywh on 2016/12/8.
 */
public class MD5Util {

    public static String getMD5(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int bufferIndex = -1;
            while ((bufferIndex = fis.read(buffer)) != -1) {
                md5.update(buffer, 0, bufferIndex);
            }
            return HexUtil.toHex(md5.digest());
        } catch (Exception e) {
            return "";
        } finally {
            IOUtil.close(fis);
        }
    }

}
