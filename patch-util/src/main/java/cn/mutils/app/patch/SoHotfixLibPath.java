package cn.mutils.app.patch;

import java.io.File;

/**
 * Created by wenhua.ywh on 2016/12/19.
 */
class SoHotfixLibPath implements Comparable<SoHotfixLibPath> {

    private int mVersion = -1;
    private String mPath = "";

    public SoHotfixLibPath(String path) {
        this(new File(path));
    }

    public SoHotfixLibPath(File dir) {
        mPath = dir.getPath();
        if (dir.isDirectory()) {
            try {
                mVersion = Integer.parseInt(dir.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getPath() {
        return mPath;
    }

    public int getVersion() {
        return mVersion;
    }

    public boolean isInvalidVersion() {
        return mVersion == -1;
    }

    @Override
    public int compareTo(SoHotfixLibPath another) {
        if (another == null) {
            return -1;
        }
        return mVersion - another.getVersion();
    }

    @Override
    public String toString() {
        return mPath != null ? mPath : "";
    }
}
