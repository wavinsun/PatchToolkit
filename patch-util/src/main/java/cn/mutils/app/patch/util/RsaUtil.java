package cn.mutils.app.patch.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by wenhua.ywh on 2016/12/7.
 */
public class RsaUtil {

    private static final String SIGN_ALGORITHM = "SHA1WithRSA";
    private static final String KEY_ALGORITHM = "RSA";

    public static boolean verify(File file, String sign, String publicKeyStr) {
        if (file == null || (sign == null || sign.length() == 0) || (publicKeyStr == null || publicKeyStr.length() == 0)) {
            return false;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] keyBytes = Base64Util.toBytes(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initVerify(publicKey);
            int bufferIndex = -1;
            byte[] buffer = new byte[1024];
            while ((bufferIndex = fis.read(buffer)) != -1) {
                signature.update(buffer, 0, bufferIndex);
            }
            byte[] signByte = Base64Util.toBytes(sign);
            return signature.verify(signByte);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            IOUtil.close(fis);
        }
    }

    public static String sign(File file, String privateKeyStr) {
        FileInputStream fis = null;
        try {
            byte[] keyBytes = Base64Util.toBytes(privateKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initSign(privateKey);
            int bufferIndex = -1;
            byte[] buffer = new byte[1024];
            fis = new FileInputStream(file);
            while ((bufferIndex = fis.read(buffer)) != -1) {
                signature.update(buffer, 0, bufferIndex);
            }
            byte[] signBytes = signature.sign();
            return Base64Util.fromBytes(signBytes);
        } catch (Exception e) {
            return null;
        } finally {
            IOUtil.close(fis);
        }
    }

    public static boolean generateKeyPair(File publicKeyFile, File privateKeyFile) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(1024, new SecureRandom());
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            String publicKeyString = Base64Util.fromBytes(publicKey.getEncoded());
            String privateKeyString = Base64Util.fromBytes(privateKey.getEncoded());
            if (!FileUtil.writeString(publicKeyFile, publicKeyString)) {
                return false;
            }
            if (!FileUtil.writeString(privateKeyFile, privateKeyString)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
