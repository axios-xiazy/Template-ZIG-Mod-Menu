#include <jni.h>
#include <string>
#include "../../include/oxorany/oxorany_include.h"

extern "C" {

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_initSignatureBypass(JNIEnv *env, jclass clazz) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_disableBypass(JNIEnv *env, jclass clazz) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_isBypassActive(JNIEnv *env, jclass clazz) {
    return JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_getTargetPackage(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(oxorany("com.garena.game.kgth"));
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_reActivateBypass(JNIEnv *env, jclass clazz) {
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_verifySignatureStatus(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(oxorany("Signature: SPOOFED | Bypass: ACTIVE"));
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_extractOriginApk(JNIEnv *env, jclass clazz) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_setCustomSignature(JNIEnv *env, jclass clazz, jstring signature) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_setFakeApkPath(JNIEnv *env, jclass clazz, jstring path) {
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_clearPMCache(JNIEnv *env, jclass clazz) {
}

}
