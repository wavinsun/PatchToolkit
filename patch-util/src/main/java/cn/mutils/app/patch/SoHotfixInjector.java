package cn.mutils.app.patch;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.Log;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Created by wenhua.ywh on 2016/12/20.
 */
public class SoHotfixInjector {

    private static final String TAG = SoHotfixInjector.class.getSimpleName();

    private SoHotfixContext mContext;
    private PathClassLoader mPathClassLoader;
    private Object mPathListObj;
    private Field mPathListField;
    private Field mPathListLibsField;

    public SoHotfixInjector(SoHotfixContext context) {
        mContext = context;
        init();
    }

    private void init() {
        try {
            Context baseContext = ((ContextWrapper) mContext.getContext()).getBaseContext();
            if (baseContext == null) {
                return;
            }
            Field infoField = baseContext.getClass().getDeclaredField("mPackageInfo");
            infoField.setAccessible(true);
            Object infoObject = infoField.get(baseContext);
            Field loaderField = infoObject.getClass().getDeclaredField("mClassLoader");
            loaderField.setAccessible(true);
            mPathClassLoader = (PathClassLoader) loaderField.get(infoObject);
            String findLibraryFieldName = "nativeLibraryPathElements";
            if (Build.VERSION.SDK_INT < 23) {
                findLibraryFieldName = "nativeLibraryDirectories";
            }
            mPathListField = getDeclaredField(mPathClassLoader.getClass(), "pathList");
            mPathListField.setAccessible(true);
            mPathListObj = mPathListField.get(mPathClassLoader);
            mPathListLibsField = mPathListObj.getClass().getDeclaredField(findLibraryFieldName);
            mPathListLibsField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean injectNativeLib(SoHotfixLibPath libraryDir) {
        return injectNativeLib(new SoHotfixLibPath[]{libraryDir});
    }

    public boolean injectNativeLib(SoHotfixLibPath[] libraryDirs) {
        if (libraryDirs == null) {
            return false;
        }
        int injectCount = libraryDirs.length;
        if (injectCount == 0) {
            return false;
        }
        if (mPathListLibsField == null) {
            init();
        }
        try {
            Log.i(TAG, "native lib inject process start...");
            Object[] oldLibPathElements = (Object[]) mPathListLibsField.get(mPathListObj);
            int oldLibPathCount = oldLibPathElements.length;
            Object[] newLibPathElements = (Object[]) Array.newInstance(mPathListLibsField.getType().getComponentType(), oldLibPathCount + injectCount);
            System.arraycopy(oldLibPathElements, 0, newLibPathElements, injectCount, oldLibPathCount);
            for (int i = 0; i < injectCount; i++) {
                String libraryDir = libraryDirs[i].getPath();
                DexClassLoader dexClassLoader = new DexClassLoader("", libraryDir, libraryDir, mPathClassLoader.getParent());
                Object libPathElement = ((Object[]) mPathListLibsField.get(mPathListField.get(dexClassLoader)))[0];
                newLibPathElements[injectCount - 1 - i] = libPathElement;
            }
            mPathListLibsField.set(mPathListObj, newLibPathElements);
            Log.i(TAG, "inject native lib success");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "inject native lib failed", e);
            return false;
        }
    }

    private static Field getDeclaredField(Class c, String fieldName) throws NoSuchFieldException {
        while (c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f;
            } catch (Exception e) {
            } finally {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException();
    }

}
