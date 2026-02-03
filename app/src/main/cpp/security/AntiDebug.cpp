#include <jni.h>
#include <string>
#include <unistd.h>
#include <sys/ptrace.h>
#include <fstream>
#include <thread>
#include <chrono>

bool isFridaPresent() {
    std::ifstream maps("/proc/self/maps");
    std::string line;
    while (std::getline(maps, line)) {
        if (line.find("frida") != std::string::npos || line.find("gadget") != std::string::npos) return true;
    }
    return false;
}

void securityLoop() {
    while(true) {
        if (isFridaPresent()) exit(0);
        std::this_thread::sleep_for(std::chrono::milliseconds(500));
    }
}

extern "C" JNIEXPORT void JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_initSecurity(JNIEnv* env, jclass clazz) {
    std::thread(securityLoop).detach();
}