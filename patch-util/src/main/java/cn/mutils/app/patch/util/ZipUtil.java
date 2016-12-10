package cn.mutils.app.patch.util;

import java.io.*;
import java.util.List;
import java.util.zip.*;

/**
 * Created by wenhua.ywh on 2016/12/8.
 */
public class ZipUtil {

    public static boolean writeFilesToZip(File zipFile, List<File> files) {
        ZipOutputStream out = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fos, new CRC32());
            out = new ZipOutputStream(cos);
            for (File file : files) {
                BufferedInputStream bis = null;
                try {
                    bis = new BufferedInputStream(new FileInputStream(file));
                    ZipEntry entry = new ZipEntry(file.getName());
                    out.putNextEntry(entry);
                    int count = -1;
                    byte data[] = new byte[1024];
                    while ((count = bis.read(data)) != -1) {
                        out.write(data, 0, count);
                    }
                } catch (Exception e) {
                    return false;
                } finally {
                    IOUtil.close(bis);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            IOUtil.close(out);
            IOUtil.close(fos);
        }
    }

    public static boolean unzipToDir(File zipFile, File dir) {
        if (!zipFile.isFile()) {
            return false;
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null && !entry.isDirectory()) {
                File target = new File(dir, entry.getName());
                if (!target.getParentFile().exists()) {
                    target.getParentFile().mkdirs();
                }
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(target));
                    int read = -1;
                    byte[] buffer = new byte[1024];
                    while ((read = zis.read(buffer)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    bos.flush();
                } catch (Exception e) {
                    return false;
                } finally {
                    IOUtil.close(bos);
                }
            }
            zis.closeEntry();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            IOUtil.close(zis);
        }
    }

}
