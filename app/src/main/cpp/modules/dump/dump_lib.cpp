#include <jni.h>
#include <string>
#include <vector>
#include <set>
#include <fstream>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <mutex>
#include <cstring>
#include <cstdio>

struct LibInfo {
    std::string name;
    std::string path;
    size_t size;
};

static std::vector<LibInfo> scannedLibs;
static std::mutex scanMutex;

static bool dirExists(const char* path) {
    struct stat st;
    return stat(path, &st) == 0 && S_ISDIR(st.st_mode);
}

static bool isSystemPath(const char* path) {
    if (!path) return true;
    return strncmp(path, "/system/", 8) == 0 ||
           strncmp(path, "/vendor/", 8) == 0 ||
           strncmp(path, "/apex/", 6) == 0 ||
           strncmp(path, "/odm/", 5) == 0 ||
           strncmp(path, "/product/", 9) == 0 ||
           strncmp(path, "/sys/", 5) == 0 ||
           strncmp(path, "/proc/", 6) == 0;
}

static std::string getCurrentPackageName() {
    char cmdline[256] = {0};
    int fd = open("/proc/self/cmdline", O_RDONLY);
    if (fd < 0) return "";
    ssize_t n = read(fd, cmdline, sizeof(cmdline) - 1);
    close(fd);
    if (n <= 0) return "";
    std::string pkg(cmdline, n);
    size_t pos = pkg.find('\0');
    if (pos != std::string::npos) pkg = pkg.substr(0, pos);
    return pkg;
}

static void scanDir(const char* basePath, std::vector<LibInfo>& results, std::set<std::string>& seen, int depth) {
    if (depth > 5) return;
    if (!basePath || strlen(basePath) > 512 || isSystemPath(basePath)) return;
    
    DIR* dir = opendir(basePath);
    if (!dir) return;
    
    struct dirent* entry;
    char fullPath[1024];
    struct stat st;
    
    while ((entry = readdir(dir)) != nullptr) {
        if (entry->d_name[0] == '.') continue;
        
        int len = snprintf(fullPath, sizeof(fullPath), "%s/%s", basePath, entry->d_name);
        if (len <= 0 || len >= sizeof(fullPath)) continue;
        
        if (stat(fullPath, &st) != 0) continue;
        
        if (S_ISDIR(st.st_mode)) {
            scanDir(fullPath, results, seen, depth + 1);
        } else if (S_ISREG(st.st_mode)) {
            size_t nameLen = strlen(entry->d_name);
            if (nameLen > 3 && strcmp(entry->d_name + nameLen - 3, ".so") == 0) {
                std::string key = std::string(fullPath) + "|" + std::to_string(st.st_ino);
                if (seen.count(key)) continue;
                seen.insert(key);
                results.push_back({entry->d_name, fullPath, static_cast<size_t>(st.st_size)});
            }
        }
    }
    closedir(dir);
}

static std::vector<std::string> getGameLibPaths() {
    std::vector<std::string> paths;
    std::string pkg = getCurrentPackageName();
    if (pkg.empty()) return paths;
    
    std::string userPath = "/data/user/0/" + pkg;
    if (dirExists((userPath + "/files").c_str())) paths.push_back(userPath + "/files");
    if (dirExists((userPath + "/lib").c_str())) paths.push_back(userPath + "/lib");
    
    DIR* appDir = opendir("/data/app");
    if (appDir) {
        struct dirent* entry;
        while ((entry = readdir(appDir)) != nullptr) {
            if (entry->d_name[0] == '.') continue;
            if (strncmp(entry->d_name, pkg.c_str(), pkg.length()) == 0) {
                std::string base = std::string("/data/app/") + entry->d_name;
                std::string lib64 = base + "/lib/arm64-v8a";
                std::string lib32 = base + "/lib/armeabi-v7a";
                if (dirExists(lib64.c_str())) paths.push_back(lib64);
                if (dirExists(lib32.c_str())) paths.push_back(lib32);
            }
        }
        closedir(appDir);
    }
    return paths;
}

static std::vector<LibInfo> scanLibraries(const std::vector<std::string>& searchPaths) {
    std::lock_guard<std::mutex> lock(scanMutex);
    std::vector<LibInfo> libs;
    std::set<std::string> seen;
    
    for (const auto& path : searchPaths) {
        if (!path.empty() && !isSystemPath(path.c_str()) && dirExists(path.c_str())) {
            scanDir(path.c_str(), libs, seen, 0);
        }
    }
    return libs;
}

static bool copyFile(const char* src, const char* dst) {
    FILE* srcFile = fopen(src, "rb");
    if (!srcFile) return false;
    FILE* dstFile = fopen(dst, "wb");
    if (!dstFile) {
        fclose(srcFile);
        return false;
    }
    char buffer[8192];
    size_t n;
    bool ok = true;
    while ((n = fread(buffer, 1, sizeof(buffer), srcFile)) > 0) {
        if (fwrite(buffer, 1, n, dstFile) != n) {
            ok = false;
            break;
        }
    }
    fclose(srcFile);
    fclose(dstFile);
    if (!ok) unlink(dst);
    return ok;
}

static bool ensureDir(const char* path) {
    if (dirExists(path)) return true;
    char cmd[512];
    snprintf(cmd, sizeof(cmd), "mkdir -p \"%s\" 2>/dev/null", path);
    system(cmd);
    return dirExists(path);
}

extern "C" {

JNIEXPORT jobjectArray JNICALL
Java_zig_cheat_qq_jni_Jni_getLoadedLibs(JNIEnv* env, jclass clazz) {
    if (!env) return nullptr;
    
    std::vector<std::string> paths = getGameLibPaths();
    scannedLibs = scanLibraries(paths);
    
    jclass stringClass = env->FindClass("java/lang/String");
    if (!stringClass) return nullptr;
    jobjectArray ret = env->NewObjectArray(static_cast<jsize>(scannedLibs.size()), stringClass, nullptr);
    
    for (size_t i = 0; i < scannedLibs.size(); i++) {
        std::string entry = scannedLibs[i].name + "|" + scannedLibs[i].path + "|" + std::to_string(scannedLibs[i].size / 1024) + "KB";
        jstring js = env->NewStringUTF(entry.c_str());
        env->SetObjectArrayElement(ret, static_cast<jsize>(i), js);
        env->DeleteLocalRef(js);
    }
    env->DeleteLocalRef(stringClass);
    return ret;
}

JNIEXPORT jint JNICALL
Java_zig_cheat_qq_jni_Jni_getLibCount(JNIEnv* env, jclass clazz) {
    return static_cast<jint>(scannedLibs.size());
}

JNIEXPORT jobjectArray JNICALL
Java_zig_cheat_qq_jni_Jni_getLoadedLibNames(JNIEnv* env, jclass clazz) {
    if (!env) return nullptr;
    
    std::vector<std::string> paths = getGameLibPaths();
    std::vector<LibInfo> libs = scanLibraries(paths);
    
    jclass stringClass = env->FindClass("java/lang/String");
    if (!stringClass) return nullptr;
    jobjectArray ret = env->NewObjectArray(static_cast<jsize>(libs.size()), stringClass, nullptr);
    
    for (size_t i = 0; i < libs.size(); i++) {
        jstring js = env->NewStringUTF(libs[i].name.c_str());
        env->SetObjectArrayElement(ret, static_cast<jsize>(i), js);
        env->DeleteLocalRef(js);
    }
    env->DeleteLocalRef(stringClass);
    return ret;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_dumpLib(JNIEnv* env, jclass clazz, jint index, jstring outPath) {
    if (!env || !outPath) return JNI_FALSE;
    if (index < 0 || index >= static_cast<jint>(scannedLibs.size())) return JNI_FALSE;
    
    const char* path = env->GetStringUTFChars(outPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string outDir(path);
    env->ReleaseStringUTFChars(outPath, path);
    
    if (outDir.empty() || !ensureDir(outDir.c_str())) return JNI_FALSE;
    return copyFile(scannedLibs[index].path.c_str(), (outDir + "/" + scannedLibs[index].name).c_str()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_dumpAllLibs(JNIEnv* env, jclass clazz, jstring outPath) {
    if (!env || !outPath) return JNI_FALSE;
    
    const char* path = env->GetStringUTFChars(outPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string outDir(path);
    env->ReleaseStringUTFChars(outPath, path);
    
    if (outDir.empty() || !ensureDir(outDir.c_str())) return JNI_FALSE;
    
    bool success = true;
    for (const auto& lib : scannedLibs) {
        if (!copyFile(lib.path.c_str(), (outDir + "/" + lib.name).c_str())) success = false;
    }
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_jni_Jni_dumpGlobalMetadata(JNIEnv* env, jclass clazz, jstring outPath) {
    if (!env || !outPath) return JNI_FALSE;
    
    const char* path = env->GetStringUTFChars(outPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string outDir(path);
    env->ReleaseStringUTFChars(outPath, path);
    
    if (outDir.empty() || !ensureDir(outDir.c_str())) return JNI_FALSE;
    
    FILE* maps = fopen("/proc/self/maps", "r");
    if (!maps) return JNI_FALSE;
    
    uintptr_t start = 0, end = 0;
    char line[512];
    while (fgets(line, sizeof(line), maps)) {
        if (strstr(line, "global-metadata.dat")) {
            sscanf(line, "%lx-%lx", &start, &end);
            break;
        }
    }
    fclose(maps);
    
    if (start == 0 || end <= start || end - start > 0x10000000) return JNI_FALSE;
    
    std::string dest = outDir + "/global-metadata.dat";
    FILE* out = fopen(dest.c_str(), "wb");
    if (!out) return JNI_FALSE;
    
    size_t size = end - start;
    size_t written = 0;
    bool ok = true;
    while (written < size && ok) {
        size_t chunk = (size - written > 4096) ? 4096 : (size - written);
        if (fwrite(reinterpret_cast<const void*>(start + written), 1, chunk, out) != chunk) ok = false;
        written += chunk;
    }
    fclose(out);
    if (!ok) unlink(dest.c_str());
    return ok ? JNI_TRUE : JNI_FALSE;
}

}
