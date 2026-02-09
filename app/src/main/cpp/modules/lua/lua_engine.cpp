#include <jni.h>
#include <string>
#include <vector>
#include <sstream>
#include <iomanip>
#include <thread>
#include <chrono>
#include <mutex>
#include <map>
#include <android/log.h>
#include "Dobby/dobby.h"

extern "C" {
#include "lua.h"
#include "lauxlib.h"
#include "lualib.h"
}
#include "KittyMemory/KittyInclude.hpp"
#include "KittyMemory/KittyScanner.hpp"

#define TAG "LuaEngine"

static std::vector<uint8_t> hexToBytes(const std::string& hex) {
    std::vector<uint8_t> bytes;
    std::string clean;
    for (char c : hex) {
        if (std::isxdigit(static_cast<unsigned char>(c))) clean += c;
    }
    if (clean.length() % 2 != 0) return bytes;
    for (size_t i = 0; i < clean.length(); i += 2) {
        bytes.push_back(static_cast<uint8_t>(std::stoul(clean.substr(i, 2), nullptr, 16)));
    }
    return bytes;
}

static std::string bytesToHex(const uint8_t* bytes, size_t len) {
    std::stringstream ss;
    for (size_t i = 0; i < len; i++) {
        ss << std::hex << std::setw(2) << std::setfill('0') << static_cast<int>(bytes[i]);
        if (i < len - 1) ss << " ";
    }
    return ss.str();
}

static JavaVM* g_vm = nullptr;
static std::recursive_mutex g_mutex;
static lua_State* g_L = nullptr;
static std::map<void*, int> g_hooks;

static void showToast(const std::string& msg) {
    if (!g_vm) return;
    
    JNIEnv* env = nullptr;
    int attach = g_vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    bool attached = false;
    
    if (attach == JNI_EDETACHED) {
        if (g_vm->AttachCurrentThread(&env, nullptr) != 0) return;
        attached = true;
    } else if (attach != JNI_OK) {
        return;
    }
    
    jclass cls = env->FindClass("zig/cheat/qq/jni/Jni");
    if (cls) {
        jmethodID mid = env->GetStaticMethodID(cls, "onToast", "(Ljava/lang/String;)V");
        if (mid) {
            jstring jmsg = env->NewStringUTF(msg.c_str());
            if (jmsg) {
                env->CallStaticVoidMethod(cls, mid, jmsg);
                env->DeleteLocalRef(jmsg);
            }
        }
        env->DeleteLocalRef(cls);
    }
    
    if (attached) g_vm->DetachCurrentThread();
}

static int l_readMemory(lua_State* L) {
    if (!L) return 0;
    lua_Integer addr = luaL_checkinteger(L, 1);
    lua_Integer size = luaL_checkinteger(L, 2);
    if (addr <= 0 || size <= 0 || size > 256) {
        lua_pushstring(L, "ERROR");
        return 1;
    }
    std::vector<uint8_t> buffer(static_cast<size_t>(size));
    bool ok = KittyMemory::memRead(reinterpret_cast<const void*>(static_cast<uintptr_t>(addr)), buffer.data(), buffer.size());
    if (!ok) {
        lua_pushstring(L, "ERROR");
        return 1;
    }
    std::string hex = bytesToHex(buffer.data(), buffer.size());
    lua_pushstring(L, hex.c_str());
    return 1;
}

static bool writeMemSafe(uintptr_t addr, const void* data, size_t size) {
    if (addr == 0 || !data || size == 0) return false;
    auto map = KittyMemory::getAddressMap(addr);
    int oldProt = map.protection;
    bool needRestore = false;
    if (map.isValid() && !map.writeable) {
        if (KittyMemory::memProtect(reinterpret_cast<void*>(addr), size, PROT_READ | PROT_WRITE | PROT_EXEC) == 0) {
            needRestore = true;
        }
    }
    bool ok = KittyMemory::memWrite(reinterpret_cast<void*>(addr), data, size);
    if (needRestore) {
        KittyMemory::memProtect(reinterpret_cast<void*>(addr), size, oldProt);
    }
    return ok;
}

static int l_writeMemory(lua_State* L) {
    if (!L) return 0;
    lua_Integer addr = luaL_checkinteger(L, 1);
    const char* hex = luaL_checkstring(L, 2);
    if (addr <= 0 || !hex) {
        lua_pushboolean(L, false);
        return 1;
    }
    auto bytes = hexToBytes(hex);
    if (bytes.empty()) {
        lua_pushboolean(L, false);
        return 1;
    }
    bool ok = writeMemSafe(static_cast<uintptr_t>(addr), bytes.data(), bytes.size());
    lua_pushboolean(L, ok);
    return 1;
}

static int l_getModuleBase(lua_State* L) {
    if (!L) return 0;
    const char* libName = luaL_checkstring(L, 1);
    if (!libName) {
        lua_pushinteger(L, 0);
        return 1;
    }
    uintptr_t base = KittyScanner::ElfScanner::findElf(libName).base();
    lua_pushinteger(L, static_cast<lua_Integer>(base));
    return 1;
}

static int l_readInt(lua_State* L) {
    if (!L) return 0;
    lua_Integer addr = luaL_checkinteger(L, 1);
    int value = 0;
    if (KittyMemory::memRead(reinterpret_cast<const void*>(static_cast<uintptr_t>(addr)), &value, sizeof(value))) {
        lua_pushinteger(L, value);
    } else {
        lua_pushnil(L);
    }
    return 1;
}

static int l_writeInt(lua_State* L) {
    if (!L) return 0;
    lua_Integer addr = luaL_checkinteger(L, 1);
    int value = static_cast<int>(luaL_checkinteger(L, 2));
    bool ok = writeMemSafe(static_cast<uintptr_t>(addr), &value, sizeof(value));
    lua_pushboolean(L, ok);
    return 1;
}

static int l_readFloat(lua_State* L) {
    if (!L) return 0;
    lua_Integer addr = luaL_checkinteger(L, 1);
    float value = 0.0f;
    if (KittyMemory::memRead(reinterpret_cast<const void*>(static_cast<uintptr_t>(addr)), &value, sizeof(value))) {
        lua_pushnumber(L, value);
    } else {
        lua_pushnil(L);
    }
    return 1;
}

static int l_writeFloat(lua_State* L) {
    if (!L) return 0;
    lua_Integer addr = luaL_checkinteger(L, 1);
    float value = static_cast<float>(luaL_checknumber(L, 2));
    bool ok = writeMemSafe(static_cast<uintptr_t>(addr), &value, sizeof(value));
    lua_pushboolean(L, ok);
    return 1;
}

static int l_readPointer(lua_State* L) {
    if (!L) return 0;
    int n = lua_gettop(L);
    if (n < 1) {
        lua_pushinteger(L, 0);
        return 1;
    }

    lua_Integer addr = luaL_checkinteger(L, 1);
    uintptr_t currentAddr = static_cast<uintptr_t>(addr);

    for (int i = 2; i <= n; i++) {
        uintptr_t ptrVal = 0;
        if (!KittyMemory::memRead(reinterpret_cast<const void*>(currentAddr), &ptrVal, sizeof(void*))) {
            lua_pushinteger(L, 0);
            return 1;
        }
        lua_Integer offset = luaL_checkinteger(L, i);
        currentAddr = ptrVal + static_cast<uintptr_t>(offset);
    }

    lua_pushinteger(L, static_cast<lua_Integer>(currentAddr));
    return 1;
}

static void InstrumentationHandler(RegisterContext *ctx, const HookEntryInfo *info) {
    std::lock_guard<std::recursive_mutex> lock(g_mutex);
    if (!g_L) return;

    auto it = g_hooks.find(info->target_address);
    if (it != g_hooks.end()) {
        lua_rawgeti(g_L, LUA_REGISTRYINDEX, it->second);
        
        lua_newtable(g_L);
        #if defined(__aarch64__)
        for (int i = 0; i < 29; i++) {
            lua_pushinteger(g_L, ctx->general.x[i]);
            lua_rawseti(g_L, -2, i);
        }
        lua_pushinteger(g_L, ctx->lr);
        lua_setfield(g_L, -2, "lr");
        lua_pushinteger(g_L, ctx->sp);
        lua_setfield(g_L, -2, "sp");
        #endif

        if (lua_pcall(g_L, 1, 1, 0) == LUA_OK) {
             if (lua_istable(g_L, -1)) {
                 #if defined(__aarch64__)
                 for (int i = 0; i < 29; i++) {
                     lua_rawgeti(g_L, -1, i);
                     if (lua_isnumber(g_L, -1)) {
                         ctx->general.x[i] = static_cast<uint64_t>(lua_tointeger(g_L, -1));
                     }
                     lua_pop(g_L, 1);
                 }
                 #endif
             }
             lua_pop(g_L, 1);
        } else {
            lua_pop(g_L, 1);
        }
    }
}

static int l_hook(lua_State* L) {
    lua_Integer addr = luaL_checkinteger(L, 1);
    if (lua_isfunction(L, 2)) {
        lua_pushvalue(L, 2);
        int ref = luaL_ref(L, LUA_REGISTRYINDEX);
        g_hooks[(void*)addr] = ref;
        DobbyInstrument((void*)addr, InstrumentationHandler);
    }
    return 0;
}

static int l_toast(lua_State* L) {
    if (!L) return 0;
    const char* msg = luaL_checkstring(L, 1);
    if (msg) showToast(msg);
    return 0;
}

static int l_sleep(lua_State* L) {
    if (!L) return 0;
    lua_Integer ms = luaL_checkinteger(L, 1);
    if (ms > 0 && ms <= 60000) {
        std::this_thread::sleep_for(std::chrono::milliseconds(ms));
    }
    return 0;
}

static const luaL_Reg modApi[] = {
    {"ReadHex", l_readMemory},
    {"WriteHex", l_writeMemory},
    {"FindLib", l_getModuleBase},
    {"ReadInt", l_readInt},
    {"WriteInt", l_writeInt},
    {"ReadFloat", l_readFloat},
    {"WriteFloat", l_writeFloat},
    {"Pointer", l_readPointer},
    {"Toast", l_toast},
    {"Sleep", l_sleep},
    {"Hook", l_hook},
    {nullptr, nullptr}
};

extern "C" {

JNIEXPORT jstring JNICALL
Java_zig_cheat_qq_jni_Jni_executeLuaScript(JNIEnv* env, jclass clazz, jstring script) {
    std::lock_guard<std::recursive_mutex> lock(g_mutex);
    
    if (!env) return nullptr;
    env->GetJavaVM(&g_vm);
    
    if (!script) return env->NewStringUTF("Error: null script");

    const char* code = nullptr;
    
    try {
        code = env->GetStringUTFChars(script, nullptr);
        if (!code) {
            return env->NewStringUTF("Error: failed to get script content");
        }
        
        if (!g_L) {
            g_L = luaL_newstate();
            luaL_openlibs(g_L);
            lua_newtable(g_L);
            luaL_setfuncs(g_L, modApi, 0);
            lua_setglobal(g_L, "palladium");
        }
        
        int ret = luaL_dostring(g_L, code);
        std::string result;
        
        if (ret != LUA_OK) {
            const char* err = lua_tostring(g_L, -1);
            result = "Error: ";
            result += err ? err : "unknown";
            lua_pop(g_L, 1);
        } else {
            result = "Success";
        }
        
        env->ReleaseStringUTFChars(script, code);
        return env->NewStringUTF(result.c_str());
        
    } catch (const std::exception& e) {
        if (code) env->ReleaseStringUTFChars(script, code);
        return env->NewStringUTF((std::string("Error: exception - ") + e.what()).c_str());
    } catch (...) {
        if (code) env->ReleaseStringUTFChars(script, code);
        return env->NewStringUTF("Error: unknown exception");
    }
}

JNIEXPORT void JNICALL
Java_zig_cheat_qq_jni_Jni_cleanupLua(JNIEnv* env, jclass clazz) {
    std::lock_guard<std::recursive_mutex> lock(g_mutex);
    if (g_L) {
        lua_close(g_L);
        g_L = nullptr;
    }
    g_hooks.clear();
}

}
