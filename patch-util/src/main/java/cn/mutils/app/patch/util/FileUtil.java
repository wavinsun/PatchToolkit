package cn.mutils.app.patch.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by wenhua.ywh on 2016/12/7.
 */
public class FileUtil {

    public static String getString(File file) {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            int bufferIndex = -1;
            byte[] buffer = new byte[1024];
            while ((bufferIndex = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bufferIndex);
            }
            return bos.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtil.close(bos);
            IOUtil.close(fis);
        }
    }

    public static boolean writeString(File file, String string) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] bytes = string.getBytes("UTF-8");
            fos.write(bytes);
            fos.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            IOUtil.close(fos);
        }
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFile(f);
                }
            }
        }
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

}
