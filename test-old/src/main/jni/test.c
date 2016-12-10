#include <jni.h>
#include <stdio.h>
#include <android/log.h>

#define TAG "Test"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__)

static jstring JNICALL getMessageJniCall(JNIEnv *env, jobject jobj) {
    LOGD("getMessage jni call");
    return (*env)->NewStringUTF(env, "Hello old version");
}

static const char *const pathUtilClassName = "cn/mutils/app/lib/test/Test";
static const JNINativeMethod gMethods[] = {
        {"getMessage", "()Ljava/lang/String;", (jstring) getMessageJniCall},
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