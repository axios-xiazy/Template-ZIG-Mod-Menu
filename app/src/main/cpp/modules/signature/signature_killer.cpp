#include <jni.h>
#include <string>

extern "C" {

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_initSignatureBypass(JNIEnv* env, jclass clazz) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_disableBypass(JNIEnv* env, jclass clazz) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_isBypassActive(JNIEnv* env, jclass clazz) {
    return JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_zig_cheat_qq_jni_Jni_getTargetPackage(JNIEnv* env, jclass clazz) {
    return env->NewStringUTF("com.garena.game.kgth");
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_reActivateBypass(JNIEnv* env, jclass clazz) {
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_zig_cheat_qq_jni_Jni_verifySignatureStatus(JNIEnv* env, jclass clazz) {
    return env->NewStringUTF("Signature: SPOOFED | Bypass: ACTIVE");
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_extractOriginApk(JNIEnv* env, jclass clazz) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_setCustomSignature(JNIEnv* env, jclass clazz, jstring signature) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_setFakeApkPath(JNIEnv* env, jclass clazz, jstring path) {
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_zig_cheat_qq_jni_Jni_clearPMCache(JNIEnv* env, jclass clazz) {
}

}
