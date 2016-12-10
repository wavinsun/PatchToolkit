package cn.mutils.app.lib.test;

import proguard.annotation.Keep;
import proguard.annotation.KeepClassMembers;


/**
 * Created by wenhua.ywh on 2016/12/5.
 */
@Keep
@KeepClassMembers
public class Test {

    public static native String getMessage();

}
