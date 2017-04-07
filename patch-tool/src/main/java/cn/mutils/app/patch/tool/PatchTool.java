package cn.mutils.app.patch.tool;

import cn.mutils.app.patch.util.FileUtil;
import cn.mutils.app.patch.util.MD5Util;
import cn.mutils.app.patch.util.RsaUtil;
import cn.mutils.app.patch.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import io.sigpipe.jbsdiff.ui.FileUI;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatchTool {

    private static final String TAG = PatchTool.class.getSimpleName();

    private static final String KEY_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDC4tg1qvDeskWjX42kzZF6rbLexcePSCCjCTb0qSJuUE61Vuiflms/OQBAQH1n07+tQ/yeMG8yfredhNgeMSHv59vTBvUmZOlnukIM1ZioiUBZ6BxNYOS8lqyh5zVDCMhU8V20MAhaRnV0xeAw/a0sV8lL53Y2jTKvmr7VYnV+QIDAQAB";
    private static final String KEY_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMMLi2DWq8N6yRaNfjaTNkXqtst7Fx49IIKMJNvSpIm5QTrVW6J+Waz85AEBAfWfTv61D/J4wbzJ+t52E2B4xIe/n29MG9SZk6We6QgzVmKiJQFnoHE1g5LyWrKHnNUMIyFTxXbQwCFpGdXTF4DD9rSxXyUvndjaNMq+avtVidX5AgMBAAECgYBkt6dngDL+JH+GZ9ZO9EHIZOWzuYI8mTAaeafm46UXgVRPvzEsfbZs/8H1SsHqRjOSj4kGmpjgliQ3kB19aZ+B2fiqrBCwfu+keLSLrjLmxFf+dN2/4Q8y4xvM+t6GXJg8z0rokNLtZH9AXjJv5ElTtr8y/YICOo1xUsbtWakJoQJBAPXztE6kd8IV/bYgkehAJsCr2bDX9GbD7xWx1pKgd2s88xV7SI3G8TjQh8DMgR17TnwcMxwqsqnabHEpBcoCu30CQQDLA23bNB/jqzL1YL+9Vm9D1kyTwfYTm7V0bglW3CGiDz7fsIAFfhDUjbZcWA3tPzNQJ3kc/R0bbpESz9qzljUtAkA5Bl8o2LM3mdewUY7i1XTmuTGI8hkldopJcmk4p+HoSEJoGaRx0s19CcRf7EqHZl6FIhirkC7KeO0ps4Q3GTkVAkA6ud03gdaPt2BgVwJgNPauuvkf7QXQGkTdT09oTvlztdFMR/Rgol0f/3Z3NAmjTZr8Xs7MMfQPkWZp+LKdLKBpAkEAp4DwpNsHFZJWqlMHhAFAMJBYUwRCSev0UtaonF8EcNJKlXZm/RpZ5/fg0kWIPhXB0/YZ5sjeEivr1NOOdhkaUg==";

    public static void main(String[] args) {
        Options opts = new Options();
        Option opt = new Option("t", false, "Test Mode");
        opt.setRequired(false);
        opts.addOption(opt);
        try {
            CommandLine cl = new BasicParser().parse(opts, args);
            if (cl.hasOption("t")) {
                doPatch(cl.getArgs(), true);
            } else {
                doPatch(cl.getArgs(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void doPatch(String[] args, boolean testMode) {
        if (args == null || args.length != 3) {
            Logger.e(TAG, "Usage: OldSoDir NewSoDir PatchZipFile");
            return;
        }
        File oldDir = new File(args[0]);
        List<File> soFiles = new ArrayList<>();
        File newDir = new File(args[1]);
        List<File> newSoFiles = new ArrayList<>();
        for (File f : oldDir.listFiles()) {
            if (f.getPath().endsWith(".so")) {
                File newSoFile = new File(newDir, f.getName());
                if (newSoFile.isFile()) {
                    if (testMode) {
                        soFiles.add(f);
                        newSoFiles.add(newSoFile);
                    } else {
                        String oldMD5 = MD5Util.getMD5(f);
                        String newMD5 = MD5Util.getMD5(newSoFile);
                        if (oldMD5 != null && !oldMD5.equals(newMD5)) {
                            soFiles.add(f);
                            newSoFiles.add(newSoFile);
                        }
                    }
                }
            }
        }
        if (soFiles.size() == 0) {
            Logger.e(TAG, "No match so files in " + newDir.getAbsolutePath() + " for " + oldDir.getAbsolutePath());
            return;
        }
        HashMap<String, String> newSoMD5Map = new HashMap<>();
        List<File> patchFiles = new ArrayList<>();
        for (int i = 0, size = soFiles.size(); i < size; i++) {
            File oldSoFile = soFiles.get(i);
            File newSoFile = newSoFiles.get(i);
            File patchFile = new File(newSoFile.getAbsolutePath() + ".patch");
            patchFiles.add(patchFile);
            try {
                FileUI.diff(oldSoFile, newSoFile, patchFile);
            } catch (Exception e) {
                Logger.e(TAG, e);
                return;
            }
            File patchSoFile = new File(patchFile.getAbsolutePath() + ".so");
            try {
                FileUI.patch(oldSoFile, patchSoFile, patchFile);
            } catch (Exception e) {
                Logger.e(TAG, e);
                return;
            }
            String newSoMD5 = MD5Util.getMD5(newSoFile);
            String patchSoMD5 = MD5Util.getMD5(patchSoFile);
            if (newSoMD5.length() == 0 || !newSoMD5.equals(patchSoMD5)) {
                Logger.e(TAG, "Patch file check failed for " + patchFile.getAbsolutePath());
            }
            newSoMD5Map.put(newSoFile.getName(), newSoMD5);
        }
        File md5MapFile = new File(newDir, "md5.json");
        if (!FileUtil.writeString(md5MapFile, JSON.toJSONString(newSoMD5Map))) {
            Logger.e(TAG, "MD5 write json file failed");
            return;
        }
        File zipFile = new File(args[2]);
        List<File> zipFileItems = new ArrayList<>();
        zipFileItems.add(md5MapFile);
        zipFileItems.addAll(patchFiles);
        if (!ZipUtil.writeFilesToZip(zipFile, zipFileItems)) {
            Logger.e(TAG, "Zip file generate failed");
            return;
        }
        String sign = RsaUtil.sign(zipFile, KEY_PRIVATE);
        if (!RsaUtil.verify(zipFile, sign, KEY_PUBLIC)) {
            Logger.e(TAG, "RSA error");
            return;
        }
        File signFile = new File(zipFile.getAbsolutePath() + ".sign");
        if (!FileUtil.writeString(signFile, sign)) {
            Logger.e(TAG, "Sign file generate failed");
            return;
        }
        Logger.d(TAG, "Patch zip success");
    }

}
