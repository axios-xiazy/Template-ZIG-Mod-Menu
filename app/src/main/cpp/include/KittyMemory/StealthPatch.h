#pragma once
#include <vector>
#include <string>
#include <unistd.h>
#include <sys/mman.h>
#include <stdint.h>

class StealthPatch {
private:
    uintptr_t _address;
    size_t _size;
    std::vector<uint8_t> _orig_code;
    std::vector<uint8_t> _patch_code;

    bool WriteKernelStealth(uintptr_t address, const void* buffer, size_t size);
    bool ReadKernelStealth(uintptr_t address, void* buffer, size_t size);

public:
    StealthPatch();
    ~StealthPatch();

    static void SetupSyscall();

    static StealthPatch createWithHex(uintptr_t absolute_address, std::string hex);
    static StealthPatch createWithHex(const char* libraryName, uintptr_t offset, std::string hex);

    bool isValid() const;
    size_t get_PatchSize() const;
    uintptr_t get_TargetAddress() const;

    bool Modify();
    bool Restore();
};