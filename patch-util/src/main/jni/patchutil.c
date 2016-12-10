#include "bsdiff.h"
#include "bspatch.h"
#include <jni.h>
#include <stdio.h>
#include <android/log.h>

#define TAG "PatchUtil"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__)

static jint JNICALL bsdiffJniCall(JNIEnv *env, jobject jobj, jstring oldFile, jstring newFile,
                                  jstring patchFile) {
    int argc = 4;
    char *argv[argc];
    argv[0] = "bsdiff";
    argv[1] = (char *) ((*env)->GetStringUTFChars(env, oldFile, 0));
    argv[2] = (char *) ((*env)->GetStringUTFChars(env, newFile, 0));
    argv[3] = (char *) ((*env)->GetStringUTFChars(env, patchFile, 0));
    LOGD("%s %s %s %s", argv[0], argv[1], argv[2], argv[3]);
    int ret = bsdiff(argc, argv);
    (*env)->ReleaseStringUTFChars(env, oldFile, argv[1]);
    (*env)->ReleaseStringUTFChars(env, newFile, argv[2]);
    (*env)->ReleaseStringUTFChars(env, patchFile, argv[3]);
    return ret;
}

static jint JNICALL bspatchJniCall(JNIEnv *env, jobject jobj, jstring oldFile, jstring newFile,
                                   jstring patchFile) {
    int argc = 4;
    char *argv[argc];
    argv[0] = "baspatch";
    argv[1] = (char *) ((*env)->GetStringUTFChars(env, oldFile, 0));
    argv[2] = (char *) ((*env)->GetStringUTFChars(env, newFile, 0));
    argv[3] = (char *) ((*env)->GetStringUTFChars(env, patchFile, 0));
    LOGD("%s %s %s %s", argv[0], argv[1], argv[2], argv[3]);
    int ret = bspatch(argc, argv);
    (*env)->ReleaseStringUTFChars(env, oldFile, argv[1]);
    (*env)->ReleaseStringUTFChars(env, newFile, argv[2]);
    (*env)->ReleaseStringUTFChars(env, patchFile, argv[3]);
    return ret;
}

static const char *const pathUtilClassName = "cn/mutils/app/patch/PatchUtil";
static const JNINativeMethod gMethods[] = {
        {"bsdiff",  "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (jint) bsdiffJniCall},
        {"bspatch", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (jint) bspatchJniCall}
};
static jclass patchUtilClass;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    patchUtilClass = (*env)->FindClass(env, pathUtilClassName);
    if (patchUtilClass == NULL) {
        return -1;
    }
    if ((*env)->RegisterNatives(env, patchUtilClass, gMethods,
                                sizeof(gMethods) / sizeof(gMethods[0])) < 0) {
        return -1;
    }
    return JNI_VERSION_1_4;
}