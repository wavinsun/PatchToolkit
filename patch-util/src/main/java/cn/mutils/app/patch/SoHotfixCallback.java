package cn.mutils.app.patch;

/**
 * Created by wenhua.ywh on 2016/12/9.
 */
public interface SoHotfixCallback {

    public static final int ERROR_UNKNOWN = -1; // 未知错误
    public static final int ERROR_OK = 0; // 成功
    public static final int ERROR_SIGN = 1; // 签名校验失败
    public static final int ERROR_PATCH = 2; // 增量打包合并so失败
    public static final int ERROR_UNZIP = 3; //  解压压缩包失败
    public static final int ERROR_CHECK_MD5 = 4; // 校验MD5

    public void onHotfixCallback(int errorCode);

}
