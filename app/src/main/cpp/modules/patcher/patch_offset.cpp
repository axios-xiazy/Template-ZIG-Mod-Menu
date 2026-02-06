#include <jni.h>
#include <string>
#include <vector>
#include <set>
#include <map>
#include <algorithm>
#include <sstream>
#include <iomanip>
#include <cstring>
#include <mutex>
#include <limits>
#include <cctype>
#include <fstream>
#include "../../include/KittyMemory/KittyInclude.hpp"

#define MAX_PATCHES 100
#define MAX_HEX_LEN 256
#define MAX_DESC_LEN 128
#define MAX_READ_SIZE 256
#define MAX_PATTERN_LEN 512

struct PatchEntry {
    int id;
    std::string libName;
    uintptr_t baseAddress;
    uintptr_t absoluteAddress;
    std::string originalBytes;
    std::string patchedBytes;
    std::string description;
    bool isActive;
    size_t size;
    std::vector<uint8_t> backupData;
};

static PatchEntry patchRegistry[MAX_PATCHES];
static int patchCount = 0;
static int patchIdCounter = 1000;
static std::mutex patchMutex;

static bool isSystemPath(const std::string& path) {
    return path.find("/system/") == 0 ||
           path.find("/vendor/") == 0 ||
           path.find("/apex/") == 0 ||
           path.find("/odm/") == 0 ||
           path.find("/product/") == 0 ||
           path.find("/sys/") == 0 ||
           path.find("/proc/") == 0 ||
           path.find("/dev/") == 0;
}

static std::vector<uint8_t> hexStringToBytes(const std::string& hex) {
    std::vector<uint8_t> bytes;
    bytes.reserve(hex.length() / 2);
    
    std::string cleanHex;
    cleanHex.reserve(hex.length());
    
    for (char c : hex) {
        if (isxdigit(static_cast<unsigned char>(c))) {
            cleanHex += c;
        }
    }
    
    if (cleanHex.length() % 2 != 0 || cleanHex.empty() || cleanHex.length() > MAX_HEX_LEN) {
        return bytes;
    }
    
    for (size_t i = 0; i < cleanHex.length(); i += 2) {
        std::string byteString = cleanHex.substr(i, 2);
        char* endptr = nullptr;
        unsigned long val = strtoul(byteString.c_str(), &endptr, 16);
        if (endptr != byteString.c_str() + 2) {
            bytes.clear();
            return bytes;
        }
        bytes.push_back(static_cast<uint8_t>(val));
    }
    return bytes;
}

static std::string bytesToHexString(const uint8_t* bytes, size_t len) {
    if (!bytes || len == 0 || len > MAX_READ_SIZE) return "";
    
    std::stringstream ss;
    for (size_t i = 0; i < len; i++) {
        ss << std::hex << std::setw(2) << std::setfill('0') << static_cast<int>(bytes[i]);
        if (i < len - 1) ss << " ";
    }
    return ss.str();
}

static std::string escapeJsonString(const std::string& str) {
    std::string result;
    for (char c : str) {
        switch (c) {
            case '\"': result += "\\\""; break;
            case '\\': result += "\\\\"; break;
            case '\b': result += "\\b"; break;
            case '\f': result += "\\f"; break;
            case '\n': result += "\\n"; break;
            case '\r': result += "\\r"; break;
            case '\t': result += "\\t"; break;
            default:
                if (static_cast<unsigned char>(c) < 0x20) {
                    char buf[7];
                    snprintf(buf, sizeof(buf), "\\u%04x", c);
                    result += buf;
                } else {
                    result += c;
                }
        }
    }
    return result;
}

static bool getLibraryMemoryRange(const std::string& libName, uintptr_t& start, uintptr_t& end) {
    start = 0;
    end = 0;
    
    auto maps = KittyMemory::getAllMaps();
    if (maps.empty()) return false;
    
    for (const auto& map : maps) {
        if (map.pathname.find(libName) != std::string::npos) {
            if (start == 0 || map.startAddress < start) {
                start = map.startAddress;
            }
            if (map.endAddress > end) {
                end = map.endAddress;
            }
        }
    }
    
    return (start != 0 && end > start);
}

static uintptr_t getLibraryBase(const std::string& libName) {
    auto maps = KittyMemory::getAllMaps();
    if (maps.empty()) return 0;
    
    for (const auto& map : maps) {
        if (map.pathname.find(libName) != std::string::npos && map.isValid()) {
            if (map.offset == 0 && map.readable) {
                return map.startAddress;
            }
        }
    }
    
    for (const auto& map : maps) {
        if (map.pathname.find(libName) != std::string::npos && map.isValid()) {
            return map.startAddress;
        }
    }
    
    return 0;
}

extern "C" {

JNIEXPORT jobjectArray JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_getAllLoadedLibs(JNIEnv* env, jclass clazz) {
    if (!env) return nullptr;

    jclass stringClass = nullptr;
    jobjectArray ret = nullptr;

    try {
        auto maps = KittyMemory::getAllMaps();
        std::set<std::string> uniqueLibs;

        for (const auto& map : maps) {
            if (!map.isValid() || map.pathname.empty()) continue;
            if (map.pathname.find(".so") == std::string::npos) continue;
            if (isSystemPath(map.pathname)) continue;

            size_t slashPos = map.pathname.rfind('/');
            std::string libName = (slashPos != std::string::npos) ?
                                  map.pathname.substr(slashPos + 1) : map.pathname;

            if (!libName.empty() && libName.length() < 256) {
                uniqueLibs.insert(libName);
            }
        }

        stringClass = env->FindClass("java/lang/String");
        if (!stringClass) return nullptr;
        
        ret = env->NewObjectArray(static_cast<jsize>(uniqueLibs.size()), stringClass, nullptr);
        if (!ret) {
            env->DeleteLocalRef(stringClass);
            return nullptr;
        }
        
        int i = 0;
        for (const auto& lib : uniqueLibs) {
            jstring js = env->NewStringUTF(lib.c_str());
            if (js) {
                env->SetObjectArrayElement(ret, i++, js);
                env->DeleteLocalRef(js);
            }
        }
        
        env->DeleteLocalRef(stringClass);
        return ret;
    } catch (...) {
        if (ret) env->DeleteLocalRef(ret);
        if (stringClass) env->DeleteLocalRef(stringClass);
        return nullptr;
    }
}

JNIEXPORT jlong JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_findLibBase(JNIEnv* env, jclass clazz, jstring libName) {
    if (!env || !libName) return 0;
    
    const char* name = env->GetStringUTFChars(libName, nullptr);
    if (!name) return 0;
    
    std::string libStr(name);
    env->ReleaseStringUTFChars(libName, name);
    
    uintptr_t base = getLibraryBase(libStr);
    return static_cast<jlong>(base);
}

JNIEXPORT jint JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_createPatch(JNIEnv* env, jclass clazz, jstring libName, jlong offset, jstring hexBytes, jstring description) {
    if (!env || !libName || !hexBytes) return -1;
    if (offset < 0) return -2;
    
    std::lock_guard<std::mutex> lock(patchMutex);
    
    if (patchCount >= MAX_PATCHES) return -3;
    
    const char* lib = env->GetStringUTFChars(libName, nullptr);
    if (!lib) return -1;
    
    std::string libNameStr(lib);
    env->ReleaseStringUTFChars(libName, lib);
    
    uintptr_t base = getLibraryBase(libNameStr);
    if (base == 0) return -4;
    
    const char* hex = env->GetStringUTFChars(hexBytes, nullptr);
    if (!hex) return -1;
    
    std::string hexStr(hex);
    env->ReleaseStringUTFChars(hexBytes, hex);
    
    if (hexStr.length() > MAX_HEX_LEN) return -5;
    
    auto bytes = hexStringToBytes(hexStr);
    if (bytes.empty()) return -6;
    if (bytes.size() > MAX_READ_SIZE) return -7;
    
    std::string descStr = "Patch";
    if (description) {
        const char* desc = env->GetStringUTFChars(description, nullptr);
        if (desc) {
            descStr = desc;
            if (descStr.length() > MAX_DESC_LEN) {
                descStr = descStr.substr(0, MAX_DESC_LEN);
            }
            env->ReleaseStringUTFChars(description, desc);
        }
    }
    
    uintptr_t absAddr = base + static_cast<uintptr_t>(offset);
    if (absAddr < base) return -8;
    
    auto addrMap = KittyMemory::getAddressMap(absAddr);
    if (!addrMap.isValid()) {
        return -9;
    }
    
    if ((absAddr + bytes.size()) > addrMap.endAddress) {
        return -10;
    }
    
    std::vector<uint8_t> origBytes(bytes.size());
    if (!KittyMemory::memRead(reinterpret_cast<const void*>(absAddr), origBytes.data(), bytes.size())) {
        return -11;
    }
    
    PatchEntry& entry = patchRegistry[patchCount];
    entry.id = patchIdCounter++;
    entry.libName = libNameStr;
    entry.baseAddress = base;
    entry.absoluteAddress = absAddr;
    entry.originalBytes = bytesToHexString(origBytes.data(), origBytes.size());
    entry.patchedBytes = hexStr;
    entry.description = descStr;
    entry.isActive = false;
    entry.size = bytes.size();
    entry.backupData = std::move(origBytes);
    
    int resultId = entry.id;
    patchCount++;
    return resultId;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_applyPatch(JNIEnv* env, jclass clazz, jint patchId) {
    if (!env || patchId < 0) return JNI_FALSE;
    
    std::lock_guard<std::mutex> lock(patchMutex);
    
    PatchEntry* entry = nullptr;
    for (int i = 0; i < patchCount; i++) {
        if (patchRegistry[i].id == patchId) {
            entry = &patchRegistry[i];
            break;
        }
    }
    
    if (!entry || entry->isActive) return entry ? JNI_TRUE : JNI_FALSE;
    
    auto addrMap = KittyMemory::getAddressMap(entry->absoluteAddress);
    if (!addrMap.isValid()) return JNI_FALSE;
    
    bool needRestoreProt = false;
    int oldProt = addrMap.protection;
    
    if (!addrMap.writeable) {
        int newProt = PROT_READ | PROT_WRITE | PROT_EXEC;
        if (KittyMemory::memProtect(reinterpret_cast<void*>(entry->absoluteAddress), entry->size, newProt) != 0) {
            return JNI_FALSE;
        }
        needRestoreProt = true;
    }
    
    auto bytes = hexStringToBytes(entry->patchedBytes);
    if (bytes.empty() || bytes.size() != entry->size) {
        if (needRestoreProt) {
            KittyMemory::memProtect(reinterpret_cast<void*>(entry->absoluteAddress), entry->size, oldProt);
        }
        return JNI_FALSE;
    }
    
    bool success = KittyMemory::memWrite(reinterpret_cast<void*>(entry->absoluteAddress), 
                                         bytes.data(), bytes.size());
    
    if (needRestoreProt) {
        KittyMemory::memProtect(reinterpret_cast<void*>(entry->absoluteAddress), entry->size, oldProt);
    }
    
    if (success) {
        entry->isActive = true;
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_restorePatch(JNIEnv* env, jclass clazz, jint patchId) {
    if (!env || patchId < 0) return JNI_FALSE;
    
    std::lock_guard<std::mutex> lock(patchMutex);
    
    PatchEntry* entry = nullptr;
    for (int i = 0; i < patchCount; i++) {
        if (patchRegistry[i].id == patchId) {
            entry = &patchRegistry[i];
            break;
        }
    }
    
    if (!entry || !entry->isActive) return JNI_FALSE;
    
    auto addrMap = KittyMemory::getAddressMap(entry->absoluteAddress);
    if (!addrMap.isValid()) return JNI_FALSE;
    
    bool needRestoreProt = false;
    int oldProt = addrMap.protection;
    
    if (!addrMap.writeable) {
        int newProt = PROT_READ | PROT_WRITE | PROT_EXEC;
        if (KittyMemory::memProtect(reinterpret_cast<void*>(entry->absoluteAddress), entry->size, newProt) != 0) {
            return JNI_FALSE;
        }
        needRestoreProt = true;
    }
    
    bool success = KittyMemory::memWrite(reinterpret_cast<void*>(entry->absoluteAddress), 
                                         entry->backupData.data(), entry->backupData.size());
    
    if (needRestoreProt) {
        KittyMemory::memProtect(reinterpret_cast<void*>(entry->absoluteAddress), entry->size, oldProt);
    }
    
    if (success) {
        entry->isActive = false;
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_removePatch(JNIEnv* env, jclass clazz, jint patchId) {
    if (!env || patchId < 0) return;
    
    std::lock_guard<std::mutex> lock(patchMutex);
    
    int idx = -1;
    for (int i = 0; i < patchCount; i++) {
        if (patchRegistry[i].id == patchId) {
            idx = i;
            break;
        }
    }
    
    if (idx < 0) return;
    
    if (patchRegistry[idx].isActive) {
        auto addrMap = KittyMemory::getAddressMap(patchRegistry[idx].absoluteAddress);
        if (addrMap.isValid()) {
            bool needRestoreProt = false;
            int oldProt = addrMap.protection;
            
            if (!addrMap.writeable) {
                int newProt = PROT_READ | PROT_WRITE | PROT_EXEC;
                if (KittyMemory::memProtect(reinterpret_cast<void*>(patchRegistry[idx].absoluteAddress), 
                                            patchRegistry[idx].size, newProt) == 0) {
                    needRestoreProt = true;
                }
            }
            
            KittyMemory::memWrite(reinterpret_cast<void*>(patchRegistry[idx].absoluteAddress), 
                                  patchRegistry[idx].backupData.data(), 
                                  patchRegistry[idx].backupData.size());
            
            if (needRestoreProt) {
                KittyMemory::memProtect(reinterpret_cast<void*>(patchRegistry[idx].absoluteAddress), 
                                        patchRegistry[idx].size, oldProt);
            }
        }
        patchRegistry[idx].isActive = false;
    }
    
    patchRegistry[idx].backupData.clear();
    patchRegistry[idx].backupData.shrink_to_fit();
    
    for (int i = idx; i < patchCount - 1; i++) {
        patchRegistry[i] = patchRegistry[i + 1];
    }
    patchCount--;
}

JNIEXPORT jobjectArray JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_getPatchList(JNIEnv* env, jclass clazz) {
    if (!env) return nullptr;
    
    std::lock_guard<std::mutex> lock(patchMutex);
    
    jclass stringClass = env->FindClass("java/lang/String");
    if (!stringClass) return nullptr;
    
    jobjectArray ret = env->NewObjectArray(patchCount, stringClass, nullptr);
    if (!ret) {
        env->DeleteLocalRef(stringClass);
        return nullptr;
    }
    
    for (int i = 0; i < patchCount; i++) {
        std::stringstream ss;
        ss << patchRegistry[i].id << "|" 
           << patchRegistry[i].libName << "|"
           << patchRegistry[i].description << "|0x" 
           << std::hex << patchRegistry[i].absoluteAddress << "|" 
           << patchRegistry[i].patchedBytes << "|" 
           << (patchRegistry[i].isActive ? "1" : "0");
        
        jstring js = env->NewStringUTF(ss.str().c_str());
        if (js) {
            env->SetObjectArrayElement(ret, i, js);
            env->DeleteLocalRef(js);
        }
    }
    
    env->DeleteLocalRef(stringClass);
    return ret;
}

JNIEXPORT jintArray JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_getAllPatchIds(JNIEnv* env, jclass clazz) {
    if (!env) return nullptr;
    
    std::lock_guard<std::mutex> lock(patchMutex);
    
    jintArray arr = env->NewIntArray(patchCount);
    if (!arr) return nullptr;
    
    if (patchCount > 0) {
        std::vector<jint> ids;
        ids.reserve(patchCount);
        for (int i = 0; i < patchCount; i++) {
            ids.push_back(patchRegistry[i].id);
        }
        env->SetIntArrayRegion(arr, 0, patchCount, ids.data());
    }
    
    return arr;
}

JNIEXPORT jstring JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_readMemoryHex(JNIEnv* env, jclass clazz, jlong address, jint size) {
    if (!env) return nullptr;
    if (size <= 0 || size > MAX_READ_SIZE || address <= 0) {
        return env->NewStringUTF("");
    }
    
    std::vector<uint8_t> buffer(static_cast<size_t>(size));
    bool success = KittyMemory::memRead(reinterpret_cast<const void*>(static_cast<uintptr_t>(address)), 
                                        buffer.data(), 
                                        static_cast<size_t>(size));
    
    if (!success) return env->NewStringUTF("ERROR");
    
    std::string hex = bytesToHexString(buffer.data(), buffer.size());
    return env->NewStringUTF(hex.c_str());
}

JNIEXPORT jstring JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_readMemoryHexDump(JNIEnv* env, jclass clazz, jlong address, jint size) {
    if (!env || size <= 0 || size > 256 || address <= 0) return nullptr;
    
    std::vector<uint8_t> buffer(size);
    if (!KittyMemory::memRead(reinterpret_cast<const void*>(address), buffer.data(), size)) {
        return env->NewStringUTF("READ ERROR");
    }
    
    std::stringstream ss;
    for (int i = 0; i < size; i += 16) {
        ss << std::hex << std::setw(8) << std::setfill('0') << (address + i) << "  ";
        
        for (int j = 0; j < 16 && (i + j) < size; j++) {
            ss << std::setw(2) << std::setfill('0') << (int)buffer[i + j] << " ";
        }
        
        ss << " |";
        for (int j = 0; j < 16 && (i + j) < size; j++) {
            char c = buffer[i + j];
            ss << (isprint(c) ? c : '.');
        }
        ss << "|";
        
        if (i + 16 < size) ss << "\n";
    }
    
    return env->NewStringUTF(ss.str().c_str());
}

JNIEXPORT jlongArray JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_scanPatternAll(JNIEnv* env, jclass clazz, jstring libName, jstring pattern, jint maxResults) {
    if (!env || !libName || !pattern || maxResults <= 0) return nullptr;
    
    const char* lib = env->GetStringUTFChars(libName, nullptr);
    if (!lib) return nullptr;
    
    std::string libStr(lib);
    env->ReleaseStringUTFChars(libName, lib);
    
    const char* pat = env->GetStringUTFChars(pattern, nullptr);
    std::string patStr(pat);
    env->ReleaseStringUTFChars(pattern, pat);
    
    uintptr_t start = 0, end = 0;
    if (!getLibraryMemoryRange(libStr, start, end)) {
        return nullptr;
    }
    
    std::vector<uintptr_t> results;
    uintptr_t current = start;
    int count = 0;
    
    while (current < end && count < maxResults) {
        uintptr_t found = KittyScanner::findHexFirst(current, end, patStr, "");
        if (found == 0 || found < current) break;

        results.push_back(found);
        if (found >= end - 1) break;
        current = found + 1;
        count++;
    }
    
    jlongArray arr = env->NewLongArray(results.size());
    if (arr && !results.empty()) {
        std::vector<jlong> jlongs;
        jlongs.reserve(results.size());
        for (auto r : results) jlongs.push_back(static_cast<jlong>(r));
        env->SetLongArrayRegion(arr, 0, results.size(), jlongs.data());
    }
    
    return arr;
}

JNIEXPORT jstring JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_exportPatchesJson(JNIEnv* env, jclass clazz) {
    if (!env) return nullptr;
    
    std::lock_guard<std::mutex> lock(patchMutex);
    
    if (patchCount == 0) return env->NewStringUTF("[]");
    
    std::stringstream json;
    json << "[\n";
    
    for (int i = 0; i < patchCount; i++) {
        const PatchEntry& p = patchRegistry[i];
        if (i > 0) json << ",\n";
        
        json << "  {\n";
        json << "    \"id\": " << p.id << ",\n";
        json << "    \"lib\": \"" << escapeJsonString(p.libName) << "\",\n";
        json << "    \"offset\": " << (p.absoluteAddress - p.baseAddress) << ",\n";
        json << "    \"bytes\": \"" << p.patchedBytes << "\",\n";
        json << "    \"desc\": \"" << escapeJsonString(p.description) << "\",\n";
        json << "    \"active\": " << (p.isActive ? "true" : "false") << "\n";
        json << "  }";
    }
    
    json << "\n]";
    return env->NewStringUTF(json.str().c_str());
}

JNIEXPORT jint JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_importPatchesJson(JNIEnv* env, jclass clazz, jstring jsonStr) {
    if (!env || !jsonStr) return 0;
    
    const char* str = env->GetStringUTFChars(jsonStr, nullptr);
    if (!str) return 0;
    
    std::string json(str);
    env->ReleaseStringUTFChars(jsonStr, str);
    
    std::lock_guard<std::mutex> lock(patchMutex);
    
    int imported = 0;
    size_t pos = 0;
    
    while ((pos = json.find("{", pos)) != std::string::npos) {
        size_t end = json.find("}", pos);
        if (end == std::string::npos) break;
        
        std::string obj = json.substr(pos, end - pos + 1);
        
        auto extractString = [&](const std::string& key) -> std::string {
            size_t k = json.find("\"" + key + "\"", pos);
            if (k == std::string::npos || k > end) return "";
            size_t colon = json.find(":", k);
            if (colon == std::string::npos || colon > end) return "";
            size_t q1 = json.find("\"", colon);
            if (q1 == std::string::npos || q1 > end) return "";
            size_t q2 = json.find("\"", q1 + 1);
            if (q2 == std::string::npos || q2 > end) return "";
            return json.substr(q1 + 1, q2 - q1 - 1);
        };
        
        auto extractLong = [&](const std::string& key) -> long {
            size_t k = json.find("\"" + key + "\"", pos);
            if (k == std::string::npos || k > end) return 0;
            size_t colon = json.find(":", k);
            if (colon == std::string::npos || colon > end) return 0;
            size_t comma = json.find_first_of(",}", colon);
            if (comma == std::string::npos || comma > end) return 0;
            try {
                return std::stol(json.substr(colon + 1, comma - colon - 1));
            } catch (...) {
                return 0;
            }
        };
        
        std::string lib = extractString("lib");
        long offset = extractLong("offset");
        std::string bytes = extractString("bytes");
        std::string desc = extractString("desc");
        
        if (!lib.empty() && offset > 0 && !bytes.empty() && patchCount < MAX_PATCHES) {
            uintptr_t base = getLibraryBase(lib);
            if (base != 0) {
                auto patchBytes = hexStringToBytes(bytes);
                if (!patchBytes.empty()) {
                    uintptr_t absAddr = base + offset;
                    std::vector<uint8_t> orig(patchBytes.size());
                    
                    if (KittyMemory::memRead(reinterpret_cast<const void*>(absAddr), orig.data(), patchBytes.size())) {
                        PatchEntry& entry = patchRegistry[patchCount];
                        entry.id = patchIdCounter++;
                        entry.libName = lib;
                        entry.baseAddress = base;
                        entry.absoluteAddress = absAddr;
                        entry.originalBytes = bytesToHexString(orig.data(), orig.size());
                        entry.patchedBytes = bytes;
                        entry.description = desc.empty() ? "Imported" : desc;
                        entry.isActive = false;
                        entry.size = patchBytes.size();
                        entry.backupData = std::move(orig);
                        patchCount++;
                        imported++;
                    }
                }
            }
        }
        
        pos = end + 1;
    }
    
    return imported;
}

}
