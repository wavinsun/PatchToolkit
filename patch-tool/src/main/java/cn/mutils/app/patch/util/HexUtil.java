package cn.mutils.app.patch.util;

/**
 * Created by wenhua.ywh on 2016/12/7.
 */
public class HexUtil {

    protected static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String toHex(byte[] data) {
        char[] str = new char[data.length + data.length];
        for (int i = data.length - 1, strIndex = str.length - 1; i >= 0; i--) {
            byte byte4i = data[i];
            str[strIndex--] = hexDigits[byte4i & 0xF];
            str[strIndex--] = hexDigits[byte4i >>> 4 & 0xF];
        }
        return new String(str);
    }

}
