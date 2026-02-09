#include "StealthPatch.h"
#include <fcntl.h>
#include <sys/syscall.h>
#include <linux/mman.h>
#include <sys/mman.h>
#include <unistd.h>
#include <cstring>
#include <cerrno>
#include <dlfcn.h>
#include <vector>

#include "KittyUtils.hpp"
#include "KittyMemory.hpp"

#define MAX_GADGETS 16

extern "C" {
    __attribute__((visibility("hidden"))) __attribute__((used)) uintptr_t g_gadget_pool[MAX_GADGETS] = {0};
    __attribute__((visibility("hidden"))) __attribute__((used)) int g_gadget_count = 0;
    __attribute__((visibility("hidden"))) __attribute__((used)) int g_current_gadget_index = 0;
}

static const uintptr_t XOR_KEY = 0xDEADBEEFCAFEBABE; 

bool IsGadgetSafe(uintptr_t addr) {
    if (addr == 0) return false;
    uint32_t instruction = 0;
    if (KittyMemory::memRead((void*)addr, &instruction, sizeof(instruction))) {
        if (instruction == 0xD4000001) {
            return true;
        }
    }
    return false;
}

void FindPolymorphicGadgets() {
    if (g_gadget_count >= MAX_GADGETS) return;

    void* handle = dlopen("libc.so", RTLD_NOW);
    if (!handle) return;

    const char* symbols[] = { 
        "read", "write", "openat", "mprotect", "sendto", "recvfrom", 
        "faccessat", "fchmod", "ioctl", "mmap", "close" 
    };
    
    for (const char* sym : symbols) {
        if (g_gadget_count >= MAX_GADGETS) break;
        
        uintptr_t func = (uintptr_t)dlsym(handle, sym);
        if (!func) continue;

        uint8_t* ptr = (uint8_t*)func;
        for (int i = 0; i < 200; i++) {
            if (ptr[i] == 0x01 && ptr[i+1] == 0x00 && ptr[i+2] == 0x00 && ptr[i+3] == 0xD4) {
                uintptr_t candidate = (uintptr_t)(ptr + i);
                
                if (IsGadgetSafe(candidate)) {
                    bool exists = false;
                    for(int k=0; k<g_gadget_count; k++) {
                        if ((g_gadget_pool[k] ^ XOR_KEY) == candidate) {
                            exists = true; break;
                        }
                    }
                    
                    if (!exists) {
                        g_gadget_pool[g_gadget_count] = candidate ^ XOR_KEY;
                        g_gadget_count++;
                    }
                    break;
                }
            }
        }
    }
    dlclose(handle);
}

#if defined(__aarch64__)

__attribute__((naked))
static long raw_syscall_proxy_l6(long number, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6) {
    __asm__ __volatile__(
        "mov x8, x0\n"
        "mov x0, x1\n"
        "mov x1, x2\n"
        "mov x2, x3\n"
        "mov x3, x4\n"
        "mov x4, x5\n"
        "mov x5, x6\n"

        "adrp x16, g_gadget_count\n"
        "ldr w16, [x16, :lo12:g_gadget_count]\n"
        "cbz w16, 2f\n"

        "adrp x17, g_current_gadget_index\n"
        "ldr w17, [x17, :lo12:g_current_gadget_index]\n"
        
        "add w17, w17, #1\n"
        "cmp w17, w16\n"
        "csel w17, wzr, w17, ge\n"

        "adrp x9, g_current_gadget_index\n"
        "str w17, [x9, :lo12:g_current_gadget_index]\n"

        "adrp x9, g_gadget_pool\n" 
        "add x9, x9, :lo12:g_gadget_pool\n"
        "lsl w17, w17, #3\n" 
        "ldr x16, [x9, w17, uxtw]\n" 

        "mov x17, 0xBABE\n"
        "movk x17, 0xCAFE, lsl #16\n"
        "movk x17, 0xBEEF, lsl #32\n"
        "movk x17, 0xDEAD, lsl #48\n"
        "eor x16, x16, x17\n"

        "mov x9, #0\n"
        "mov x17, #0\n"

        "mov x29, #0\n"
        
        "br x16\n"

        "2:\n"
        "mov x0, #-1\n"
        "ret\n"
    );
}

#else
static __inline__ long raw_syscall_proxy_l6(long __number, long __arg1, long __arg2, long __arg3, long __arg4, long __arg5, long __arg6) {
    return syscall(__number, __arg1, __arg2, __arg3, __arg4, __arg5, __arg6);
}
#endif

StealthPatch::StealthPatch() : _address(0), _size(0) {}

StealthPatch::~StealthPatch() {
    _orig_code.clear();
    _patch_code.clear();
}

void StealthPatch::SetupSyscall() {
    FindPolymorphicGadgets();
}

__attribute__((constructor))
void AutoSetupStealthPatch() {
    StealthPatch::SetupSyscall();
}

bool StealthPatch::WriteKernelStealth(uintptr_t address, const void* buffer, size_t size) {
    size_t page_size = sysconf(_SC_PAGESIZE);
    uintptr_t page_start = address & (~(page_size - 1));
    size_t page_len = (address + size - page_start + page_size - 1) & (~(page_size - 1));

    long ret;
    int retry = 0;
    while (retry < 5) {
        ret = raw_syscall_proxy_l6(__NR_mprotect, (long)page_start, (long)page_len, PROT_READ | PROT_WRITE | PROT_EXEC, 0, 0, 0);
        if (ret == 0) break;
        usleep(1000);
        retry++;
    }
    
    if (ret != 0) return false;

    memcpy((void*)address, buffer, size);

    raw_syscall_proxy_l6(__NR_mprotect, (long)page_start, (long)page_len, PROT_READ | PROT_EXEC, 0, 0, 0);
    
    __builtin___clear_cache((char*)address, (char*)address + size);
    return true;
}

bool StealthPatch::ReadKernelStealth(uintptr_t address, void* buffer, size_t size) {
    return KittyMemory::memRead((void*)address, buffer, size);
}

StealthPatch StealthPatch::createWithHex(uintptr_t absolute_address, std::string hex) {
    StealthPatch patch;
    if (absolute_address == 0 || !KittyUtils::String::ValidateHex(hex)) return patch;
    patch._address = absolute_address;
    patch._size = hex.length() / 2;
    patch._orig_code.resize(patch._size);
    patch._patch_code.resize(patch._size);
    KittyUtils::dataFromHex(hex, &patch._patch_code[0]);
    if (!patch.ReadKernelStealth(patch._address, &patch._orig_code[0], patch._size)) patch._address = 0;
    return patch;
}

StealthPatch StealthPatch::createWithHex(const char* libraryName, uintptr_t offset, std::string hex) {
    uintptr_t base = 0;
    std::vector<KittyMemory::ProcMap> maps = KittyMemory::getMaps(KittyMemory::EProcMapFilter::EndWith, libraryName);
    if (!maps.empty()) base = maps[0].startAddress;
    if (base == 0) return StealthPatch();
    return createWithHex(base + offset, hex);
}

bool StealthPatch::Modify() {
    if (!isValid()) return false;
    return WriteKernelStealth(_address, &_patch_code[0], _size);
}

bool StealthPatch::Restore() {
    if (!isValid()) return false;
    return WriteKernelStealth(_address, &_orig_code[0], _size);
}

bool StealthPatch::isValid() const {
    return (_address != 0 && _size > 0 && _orig_code.size() == _size);
}

size_t StealthPatch::get_PatchSize() const {
    return _size;
}

uintptr_t StealthPatch::get_TargetAddress() const {
    return _address;
}