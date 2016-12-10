package cn.mutils.app.patch;

import proguard.annotation.Keep;
import proguard.annotation.KeepClassMembers;

/**
 * Created by wenhua.ywh on 2016/12/5.
 */
@Keep
@KeepClassMembers
public class PatchUtil {

    static {
        System.loadLibrary("PatchUtil");
    }

    public static native int bsdiff(String oldFile, String newFile, String patchFile);

    public static native int bspatch(String oldFile, String newFile, String patchFile);

}
