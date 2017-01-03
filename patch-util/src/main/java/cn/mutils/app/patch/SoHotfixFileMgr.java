package cn.mutils.app.patch;

import cn.mutils.app.patch.util.FileUtil;
import cn.mutils.app.patch.util.MD5Util;
import cn.mutils.app.patch.util.ZipUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by wenhua.ywh on 2016/12/9.
 */
class SoHotfixFileMgr {

    private final String UNZIP_DIR = "zip";
    private final String TAG_OK = "ok";
    private final String TAG_CRASH = "crash";
    private final String TAG_FIRST_RUN = "first-run";

    private SoHotfixContext mContext;

    public SoHotfixFileMgr(SoHotfixContext context) {
        mContext = context;
    }

    public SoHotfixContext getSoContext() {
        return mContext;
    }

    public File getHotfixSo(String libName, int version) {
        File file = new File(SoHotfixUtil.getPath(mContext, version), "lib" + libName + ".so");
        if (!file.isFile()) {
            return null;
        }
        return file;
    }

    public File getHotfixOKTag(int version) {
        File file = new File(SoHotfixUtil.getPath(mContext, version), TAG_OK);
        if (!file.isFile()) {
            return null;
        }
        return file;
    }

    public void setHotfixOKTag(int version, long time) {
        File file = new File(SoHotfixUtil.getPath(mContext, version), TAG_OK);
        SoHotfixUtil.setLastModified(file, time);
    }

    public File getHotfixCrashTag(int version) {
        File file = new File(SoHotfixUtil.getPath(mContext, version), TAG_CRASH);
        if (!file.isFile()) {
            return null;
        }
        return file;
    }

    public void setHotfixCrashTag(int version, long time) {
        File file = new File(SoHotfixUtil.getPath(mContext, version), TAG_CRASH);
        SoHotfixUtil.setLastModified(file, time);
    }

    public File getHotfixFirstRunTag(int version) {
        File file = new File(SoHotfixUtil.getPath(mContext, version), TAG_FIRST_RUN);
        if (!file.isFile()) {
            return null;
        }
        return file;
    }

    public void setHotfixFirstRunTag(int version, long time) {
        File file = new File(SoHotfixUtil.getPath(mContext, version), TAG_FIRST_RUN);
        SoHotfixUtil.setLastModified(file, time);
    }

    public boolean isHotfixRootExists() {
        return new File(mContext.getHotfixRoot()).exists();
    }

    public boolean isHotfixExists(int version) {
        File dir = new File(SoHotfixUtil.getPath(mContext, version));
        return dir.exists();
    }

    public boolean unzipSo(File zipFile, int version) {
        File dir = new File(SoHotfixUtil.getTempPath(mContext, version), UNZIP_DIR);
        return ZipUtil.unzipToDir(zipFile, dir);
    }

    public boolean patchSo(int version) {
        File zipDir = new File(SoHotfixUtil.getTempPath(mContext, version), UNZIP_DIR);
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
            File newLibFile = new File(SoHotfixUtil.getTempPath(mContext, version) + "/" + soFileName);
            if (PatchUtil.bspatch(oldLibFile.getAbsolutePath(), newLibFile.getAbsolutePath(), f.getAbsolutePath()) != 0) {
                return false;
            }
            patchCount++;
        }
        return patchCount > 0;
    }

    public boolean checkMD5(int version) {
        File zipDir = new File(SoHotfixUtil.getTempPath(mContext, version), UNZIP_DIR);
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
                File soFile = new File(SoHotfixUtil.getTempPath(mContext, version) + "/" + soFileName);
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

    public void moveTempToLibPath(int version) {
        File libRoot = new File(mContext.getHotfixRoot());
        if (!libRoot.exists()) {
            libRoot.mkdirs();
        }
        File tempFile = new File(SoHotfixUtil.getTempPath(mContext, version));
        File libFile = new File(SoHotfixUtil.getPath(mContext, version));
        tempFile.renameTo(libFile);
    }

    public void cleanUpZip(int version) {
        File zipDir = new File(SoHotfixUtil.getTempPath(mContext, version), UNZIP_DIR);
        if (zipDir.exists()) {
            FileUtil.deleteFile(zipDir);
        }
    }

    public void cleanUpOldVersions() {
        File hotfixRoot = new File(mContext.getHotfixRoot());
        File hotfixRootParent = hotfixRoot.getParentFile();
        if (hotfixRootParent == null) {
            return;
        }
        File[] appVersions = hotfixRootParent.listFiles();
        if (appVersions == null) {
            return;
        }
        String hotfixRootName = mContext.getHotfixRootName();
        for (File appVersion : appVersions) {
            if (!hotfixRootName.equals(appVersion.getName())) {
                FileUtil.deleteFile(appVersion);
            }
        }
    }

    // 返回排序好的版本列表[升序]
    public SoHotfixLibPath[] getLibPaths(int minVersion) {
        File root = new File(mContext.getHotfixRoot());
        File[] libDirs = root.listFiles();
        if (libDirs == null) {
            return new SoHotfixLibPath[0];
        }
        int libDirsLength = libDirs.length;
        if (libDirsLength == 0) {
            return new SoHotfixLibPath[0];
        }
        SoHotfixLibPath[] libPaths = new SoHotfixLibPath[libDirsLength];
        for (int i = libDirsLength - 1; i >= 0; i--) {
            libPaths[i] = new SoHotfixLibPath(libDirs[i]);
        }
        Arrays.sort(libPaths);
        int invalidIndex = -1;
        for (int i = 0, size = libDirsLength; i < size; i++) {
            int version = libPaths[i].getVersion();
            if (version > minVersion) {
                break;
            }
            invalidIndex = i;
        }
        if (invalidIndex == -1) {
            return libPaths;
        }
        return Arrays.copyOfRange(libPaths, invalidIndex + 1, libDirsLength);
    }

    public SoHotfixLibPath getLibPathOfMaxVersion() {
        File root = new File(mContext.getHotfixRoot());
        File[] libDirs = root.listFiles();
        if (libDirs == null) {
            return null;
        }
        int libDirsLength = libDirs.length;
        if (libDirsLength == 0) {
            return null;
        }
        SoHotfixLibPath[] libPaths = new SoHotfixLibPath[libDirsLength];
        int maxIndex = -1;
        int maxVersion = -1;
        for (int i = libDirsLength - 1; i >= 0; i--) {
            SoHotfixLibPath libPath = new SoHotfixLibPath(libDirs[i]);
            int libVersion = libPath.getVersion();
            if (libVersion > maxVersion) {
                maxVersion = libVersion;
                maxIndex = i;
            }
            libPaths[i] = libPath;
        }
        return maxIndex != -1 ? libPaths[maxIndex] : null;
    }

}
