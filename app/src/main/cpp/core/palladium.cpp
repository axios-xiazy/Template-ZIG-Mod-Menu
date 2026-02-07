#include <jni.h>
#include <string>
#include <thread>
#include <unistd.h>
#include "../include/Tools/obfuscate.h"
#include "../include/oxorany/oxorany_include.h"
#include "../include/KittyMemory/KittyInclude.hpp"
#include "../include/KittyMemory/StealthPatch.h"

struct GameConfig {
    bool Hackmap = false;
    bool ShowCoolDown = false;
    int AimbotRange = 50;
    bool DrawLine = false;
    bool DrawBox = false;
    int EspMode = 0;
    bool AntiReport = false;
    bool BypassIntegrity = false;
} config;

ElfScanner g_il2cppElf;

void main_thread() {
    sleep(2);
    while (!(g_il2cppElf = ElfScanner::findElf("libil2cpp.so")).isValid()) {
        usleep(100000);
    }
    uintptr_t il2cppBase = g_il2cppElf.base();
    StealthPatch::createWithHex("libil2cpp.so", 0x100000, "00 00 00 00").Modify();
    MemoryPatch::createWithHex(il2cppBase + 0x100000, "00 00 00 00").Modify();
}

extern "C" {

JNIEXPORT jobjectArray JNICALL
Java_zig_cheat_qq_jni_Jni_getFeatures(JNIEnv* env, jclass clazz) {
    const char* features[] = {
        oxorany("PAGE|0|icons/func1.png|Main"),
        oxorany("TITLE|0|PLAYER MODS"),
        oxorany("CHECK|0|Hack Map|1"),
        oxorany("CHECK|0|Show Cooldown|2"),
        oxorany("SLIDER|0|Aimbot Range|0|100|3"),
        oxorany("PAGE|1|icons/func2.png|Visual"),
        oxorany("TITLE|1|ESP CONFIG"),
        oxorany("CHECK|1|Draw Line|4"),
        oxorany("CHECK|1|Draw Box|5"),
        oxorany("SPINNER|1|ESP Style|Classic,Modern,Enterprise|6"),
        oxorany("PAGE|2|icons/func3.png|System"),
        oxorany("TITLE|2|SECURITY"),
        oxorany("CHECK|2|Anti-Report Active|7"),
        oxorany("CHECK|2|Bypass Integrity|8"),
        oxorany("PAGE|3|icons/tools.png|Tools"),
        oxorany("TITLE|3|MEMORY TOOLS"),
        oxorany("BUTTON|3|Dump LIB Files|dump_lib_dialog"),
        oxorany("BUTTON|3|Patch Offset Manager|patch_offset_dialog"),
        oxorany("TITLE|3|SIGNATURE BYPASS"),
        oxorany("BUTTON|3|Signature Manager|signature_manager_dialog")
    };

    int total = sizeof(features) / sizeof(features[0]);
    jclass stringClass = env->FindClass(OBFUSCATE("java/lang/String"));
    jobjectArray ret = env->NewObjectArray(total, stringClass, nullptr);
    for (int i = 0; i < total; i++) {
        jstring js = env->NewStringUTF(features[i]);
        env->SetObjectArrayElement(ret, i, js);
        env->DeleteLocalRef(js);
    }
    env->DeleteLocalRef(stringClass);
    return ret;
}

JNIEXPORT void JNICALL
Java_zig_cheat_qq_jni_Jni_Callback(JNIEnv* env, jclass clazz, jint id, jboolean check, jint value, jfloat value2, jstring value3) {
    switch (id) {
        case 1: config.Hackmap = check; break;
        case 2: config.ShowCoolDown = check; break;
        case 3: config.AimbotRange = value; break;
        case 4: config.DrawLine = check; break;
        case 5: config.DrawBox = check; break;
        case 6: config.EspMode = value; break;
        case 7: config.AntiReport = check; break;
        case 8: config.BypassIntegrity = check; break;
    }
}

}

__attribute__((constructor)) void init() {
    std::thread(main_thread).detach();
}
