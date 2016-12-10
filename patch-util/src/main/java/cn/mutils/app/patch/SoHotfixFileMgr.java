package cn.mutils.app.patch;

import cn.mutils.app.patch.util.FileUtil;
import cn.mutils.app.patch.util.MD5Util;
import cn.mutils.app.patch.util.ZipUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

/**
 * Created by wenhua.ywh on 2016/12/9.
 */
public class SoHotfixFileMgr {

    private SoHotfixContext mContext;

    public SoHotfixFileMgr(SoHotfixContext context) {
        mContext = context;
    }

    public File getHotfixSo(String libName, int version) {
        File file = new File(SoHotfixUtil.getPath(mContext, version), "lib" + libName + ".so");
        if (!file.isFile()) {
            return null;
        }
        return file;
    }

    public boolean isHotfixRootExists() {
        return new File(mContext.getHotfixRoot()).exists();
    }

    public boolean unzipSo(File zipFile, int version) {
        File dir = new File(SoHotfixUtil.getPath(mContext, version), "zip");
        return ZipUtil.unzipToDir(zipFile, dir);
    }

    public boolean patchSo(int version) {
        File zipDir = new File(SoHotfixUtil.getPath(mContext, version), "zip");
        File[] files = zipDir.listFiles();
        if (files == null) {
            return false;
        }
        int patchCount = 0;
        for (File f : files) {
            if (!f.isFile()) {
                continue;
            }
            String fileName = f.getName();
            int indexPatch = fileName.lastIndexOf(".patch");
            if (indexPatch == -1) {
                continue;
            }
            String soFileName = fileName.substring(0, indexPatch);
            if (!soFileName.endsWith(".so")) {
                continue;
            }
            File oldLibFile = new File(mContext.getLibRoot() + "/" + soFileName);
            if (!oldLibFile.exists()) {
                continue;
            }
            File newLibFile = new File(SoHotfixUtil.getPath(mContext, version) + "/" + soFileName);
            if (PatchUtil.bspatch(oldLibFile.getAbsolutePath(), newLibFile.getAbsolutePath(), f.getAbsolutePath()) != 0) {
                return false;
            }
            patchCount++;
        }
        return patchCount > 0;
    }

    public boolean checkMD5(int version) {
        File zipDir = new File(SoHotfixUtil.getPath(mContext, version), "zip");
        File md5File = new File(zipDir, "md5.json");
        if (!md5File.isFile()) {
            return false;
        }
        String md5Json = FileUtil.getString(md5File);
        if (md5Json == null) {
            return false;
        }
        try {
            JSONObject json = new JSONObject(md5Json);
            Iterator<String> iterator = json.keys();
            while (iterator.hasNext()) {
                String soFileName = iterator.next();
                String soRightMD5 = json.getString(soFileName);
                if (soRightMD5 == null) {
                    return false;
                }
                File soFile = new File(SoHotfixUtil.getPath(mContext, version) + "/" + soFileName);
                String soFileMD5 = MD5Util.getMD5(soFile);
                if (!soRightMD5.equals(soFileMD5)) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
