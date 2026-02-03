#include <jni.h>
#include <string>
#include <vector>
#include <set>
#include <utility>
#include <fstream>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <mutex>
#include <cstring>
#include <cstdio>
#include <cstdlib>

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

static bool isSafeToScan(const char* path) {
    if (!path || strlen(path) > 512) return false;
    if (isSystemPath(path)) return false;
    return true;
}

static void scanDirRecursive(const char* basePath, std::vector<LibInfo>& results, 
                             std::set<std::pair<dev_t, ino_t>>& seenInodes, int depth) {
    if (depth > 5) return;
    if (!isSafeToScan(basePath)) return;
    
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
            if (depth < 5 && isSafeToScan(fullPath)) {
                scanDirRecursive(fullPath, results, seenInodes, depth + 1);
            }
        } else if (S_ISREG(st.st_mode)) {
            size_t nameLen = strlen(entry->d_name);
            if (nameLen > 3 && strcmp(entry->d_name + nameLen - 3, ".so") == 0) {
                auto inodeKey = std::make_pair(st.st_dev, st.st_ino);
                if (seenInodes.count(inodeKey)) continue;
                seenInodes.insert(inodeKey);
                
                LibInfo info;
                info.name = entry->d_name;
                info.path = fullPath;
                info.size = st.st_size;
                results.push_back(info);
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
                std::string base = "/data/app/";
                base += entry->d_name;
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

static std::vector<LibInfo> scanLibrariesFromPaths(const std::vector<std::string>& searchPaths) {
    std::lock_guard<std::mutex> lock(scanMutex);
    std::vector<LibInfo> libs;
    std::set<std::pair<dev_t, ino_t>> seenInodes;
    
    for (const auto& path : searchPaths) {
        if (path.empty() || isSystemPath(path.c_str())) continue;
        if (dirExists(path.c_str())) {
            scanDirRecursive(path.c_str(), libs, seenInodes, 0);
        }
    }
    
    return libs;
}

static bool copyFileNative(const char* srcPath, const char* destPath) {
    if (!srcPath || !destPath) return false;
    
    FILE* src = fopen(srcPath, "rb");
    if (!src) return false;
    
    FILE* dst = fopen(destPath, "wb");
    if (!dst) {
        fclose(src);
        return false;
    }
    
    char buffer[8192];
    size_t bytesRead;
    bool success = true;
    
    while ((bytesRead = fread(buffer, 1, sizeof(buffer), src)) > 0) {
        if (fwrite(buffer, 1, bytesRead, dst) != bytesRead) {
            success = false;
            break;
        }
    }
    
    fclose(src);
    fclose(dst);
    
    if (!success) unlink(destPath);
    return success;
}

static bool ensureDirExists(const char* path) {
    if (!path) return false;
    if (dirExists(path)) return true;
    
    char cmd[512];
    snprintf(cmd, sizeof(cmd), "mkdir -p \"%s\" 2>/dev/null", path);
    system(cmd);
    return dirExists(path);
}

extern "C" {

JNIEXPORT jobjectArray JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_getLoadedLibs(JNIEnv* env, jclass clazz) {
    if (!env) return nullptr;
    
    try {
        std::vector<std::string> paths = getGameLibPaths();
        scannedLibs = scanLibrariesFromPaths(paths);
        
        jclass stringClass = env->FindClass("java/lang/String");
        if (!stringClass) return nullptr;
        
        jobjectArray ret = env->NewObjectArray(static_cast<jsize>(scannedLibs.size()), stringClass, nullptr);
        if (!ret) {
            env->DeleteLocalRef(stringClass);
            return nullptr;
        }
        
        for (size_t i = 0; i < scannedLibs.size(); i++) {
            std::string entry = scannedLibs[i].name + "|" + scannedLibs[i].path + "|" + std::to_string(scannedLibs[i].size / 1024) + "KB";
            jstring js = env->NewStringUTF(entry.c_str());
            if (js) {
                env->SetObjectArrayElement(ret, static_cast<jsize>(i), js);
                env->DeleteLocalRef(js);
            }
        }
        
        env->DeleteLocalRef(stringClass);
        return ret;
    } catch (...) {
        return nullptr;
    }
}

JNIEXPORT jint JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_getLibCount(JNIEnv* env, jclass clazz) {
    return static_cast<jint>(scannedLibs.size());
}

JNIEXPORT jobjectArray JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_getLoadedLibNames(JNIEnv* env, jclass clazz) {
    if (!env) return nullptr;
    
    try {
        std::vector<std::string> paths = getGameLibPaths();
        std::vector<LibInfo> libs = scanLibrariesFromPaths(paths);
        
        jclass stringClass = env->FindClass("java/lang/String");
        if (!stringClass) return nullptr;
        
        jobjectArray ret = env->NewObjectArray(static_cast<jsize>(libs.size()), stringClass, nullptr);
        if (!ret) {
            env->DeleteLocalRef(stringClass);
            return nullptr;
        }
        
        for (size_t i = 0; i < libs.size(); i++) {
            jstring js = env->NewStringUTF(libs[i].name.c_str());
            if (js) {
                env->SetObjectArrayElement(ret, static_cast<jsize>(i), js);
                env->DeleteLocalRef(js);
            }
        }
        
        env->DeleteLocalRef(stringClass);
        return ret;
    } catch (...) {
        return nullptr;
    }
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_dumpLib(JNIEnv* env, jclass clazz, jint index, jstring outPath) {
    if (!env || !outPath) return JNI_FALSE;
    if (index < 0 || index >= static_cast<jint>(scannedLibs.size())) return JNI_FALSE;
    
    const char* path = env->GetStringUTFChars(outPath, nullptr);
    if (!path) return JNI_FALSE;
    
    std::string outDir(path);
    env->ReleaseStringUTFChars(outPath, path);
    
    if (outDir.empty()) return JNI_FALSE;
    if (!ensureDirExists(outDir.c_str())) return JNI_FALSE;
    
    std::string destPath = outDir + "/" + scannedLibs[index].name;
    return copyFileNative(scannedLibs[index].path.c_str(), destPath.c_str()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_dumpAllLibs(JNIEnv* env, jclass clazz, jstring outPath) {
    if (!env || !outPath) return JNI_FALSE;
    
    const char* path = env->GetStringUTFChars(outPath, nullptr);
    if (!path) return JNI_FALSE;
    
    std::string outDir(path);
    env->ReleaseStringUTFChars(outPath, path);
    
    if (outDir.empty()) return JNI_FALSE;
    if (!ensureDirExists(outDir.c_str())) return JNI_FALSE;
    
    bool success = true;
    for (const auto& lib : scannedLibs) {
        std::string destPath = outDir + "/" + lib.name;
        if (!copyFileNative(lib.path.c_str(), destPath.c_str())) {
            success = false;
        }
    }
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_dumpGlobalMetadata(JNIEnv* env, jclass clazz, jstring outPath) {
    if (!env || !outPath) return JNI_FALSE;
    
    const char* path = env->GetStringUTFChars(outPath, nullptr);
    if (!path) return JNI_FALSE;
    
    std::string outDir(path);
    env->ReleaseStringUTFChars(outPath, path);
    
    if (outDir.empty()) return JNI_FALSE;
    if (!ensureDirExists(outDir.c_str())) return JNI_FALSE;
    
    FILE* maps = fopen("/proc/self/maps", "r");
    if (!maps) return JNI_FALSE;
    
    uintptr_t start = 0, end = 0;
    bool found = false;
    char line[512];
    
    while (fgets(line, sizeof(line), maps)) {
        if (strstr(line, "global-metadata.dat")) {
            if (sscanf(line, "%lx-%lx", &start, &end) == 2) {
                found = true;
                break;
            }
        }
    }
    fclose(maps);
    
    if (!found || start == 0 || end <= start) return JNI_FALSE;
    
    size_t size = end - start;
    if (size > 0x10000000) return JNI_FALSE;
    
    std::string dest = outDir + "/global-metadata.dat";
    FILE* out = fopen(dest.c_str(), "wb");
    if (!out) return JNI_FALSE;
    
    bool ok = true;
    size_t written = 0;
    
    while (written < size && ok) {
        size_t chunk = (size - written > 4096) ? 4096 : (size - written);
        if (fwrite(reinterpret_cast<const void*>(start + written), 1, chunk, out) != chunk) {
            ok = false;
        }
        written += chunk;
    }
    
    fclose(out);
    if (!ok) unlink(dest.c_str());
    return ok ? JNI_TRUE : JNI_FALSE;
}

}