package cn.mutils.app.patch.util;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by wenhua.ywh on 2016/12/7.
 */
public class IOUtil {

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copy(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[4096];// 4K
        int bufferIndex = -1;
        while ((bufferIndex = in.read(buffer)) != -1) {
            out.write(buffer, 0, bufferIndex);
        }
    }

}
