package cn.mutils.app.patch.tool;

import cn.mutils.app.patch.util.RsaUtil;

import java.io.File;

/**
 * Created by wenhua.ywh on 2016/12/7.
 */
public class KeyTool {

    public static void main(String[] args) {
        File publicKeyFile = new File("public.keystore");
        File privateKeyFile = new File("private.keystore");
        if (publicKeyFile.exists() || privateKeyFile.exists()) {
            Logger.e(KeyTool.class.getSimpleName(), "Key files exists");
            return;
        }
        RsaUtil.generateKeyPair(publicKeyFile, privateKeyFile);
    }

}
