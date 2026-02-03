cmake_minimum_required(VERSION 3.24.1)

#===============================================================================
# Project Setup
#===============================================================================
project(
	"PALLADIUM_CORE_X" 
    VERSION 1.0.0 
    LANGUAGES C CXX ASM
)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

#===============================================================================
# Configuration Constants
#===============================================================================
set(PAGE_SIZE 4096)
math(EXPR PAGE_MASK "${PAGE_SIZE} - 1")
math(EXPR PAGE_MASK "~${PAGE_MASK}")

#===============================================================================
# Architecture Validation
#===============================================================================
if(NOT ANDROID_ABI STREQUAL "arm64-v8a")
    message(FATAL_ERROR "PALLADIUM_CORE_X requires arm64-v8a architecture only. Current: ${ANDROID_ABI}")
endif()

#===============================================================================
# Build Options
#===============================================================================
option(BUILD_WITH_SANITIZERS "Enable AddressSanitizer and UBSan (Debug only)" OFF)

#===============================================================================
# Source Files
#===============================================================================
file(GLOB_RECURSE PALLADIUM_SOURCES CONFIGURE_DEPENDS
    "core/*.cpp"
    "modules/*.cpp"
    "hooks/*.cpp"
    "security/*.cpp"
    "include/Tools/Tools.cpp"
    "include/oxorany/oxorany.cpp"
    "include/KittyMemory/*.cpp"
)

#===============================================================================
# Helper Macros
#===============================================================================
macro(import_static_lib name path)
    add_library(${name} STATIC IMPORTED)
    set_target_properties(${name} PROPERTIES IMPORTED_LOCATION ${path})
endmacro()

#===============================================================================
# Import Static Libraries
#===============================================================================
import_static_lib(libssl        "${CMAKE_CURRENT_SOURCE_DIR}/include/openssl/lib/libssl.a")
import_static_lib(libcrypto     "${CMAKE_CURRENT_SOURCE_DIR}/include/openssl/lib/libcrypto.a")
import_static_lib(libdobby      "${CMAKE_CURRENT_SOURCE_DIR}/include/Dobby/libraries/${ANDROID_ABI}/libdobby.a")
import_static_lib(libkeystone   "${CMAKE_CURRENT_SOURCE_DIR}/include/KittyMemory/Deps/Keystone/libs-android/${ANDROID_ABI}/libkeystone.a")

#===============================================================================
# Main Target
#===============================================================================
add_library(lib-name SHARED ${PALLADIUM_SOURCES})

#===============================================================================
# Include Directories
#===============================================================================
target_include_directories(lib-name PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/core
    ${CMAKE_CURRENT_SOURCE_DIR}/modules
    ${CMAKE_CURRENT_SOURCE_DIR}/modules/dump
    ${CMAKE_CURRENT_SOURCE_DIR}/modules/patcher
    ${CMAKE_CURRENT_SOURCE_DIR}/hooks
    ${CMAKE_CURRENT_SOURCE_DIR}/security
    ${CMAKE_CURRENT_SOURCE_DIR}/include/openssl/include
)

#===============================================================================
# Compile Options
#===============================================================================
target_compile_options(lib-name PRIVATE
    # Optimization
    -O3
    -fno-strict-aliasing
    -fno-strict-overflow
    
    # Security
    -fstack-protector-strong
    -fstack-clash-protection
    -ftrivial-auto-var-init=zero
    -fzero-call-used-regs=used-gpr
    -mbranch-protection=standard
    -msign-return-address=all
    
    # Code Generation
    -fPIC
    -fno-omit-frame-pointer
    
    # Warnings as Errors
    -Werror=format-security
    -Werror=return-type
    -Werror=implicit-function-declaration
    -Werror=int-conversion
    -Werror=incompatible-pointer-types
    -Wno-multichar
    
    # Language Features
    -fno-rtti
    -fexceptions
    -fno-inline-functions
    -fno-optimize-sibling-calls
    -fno-strict-aliasing
    
    # Fortify
    -U_FORTIFY_SOURCE
    -D_FORTIFY_SOURCE=3
    -DANDROID_FORTIFY_SOURCE=3
    
    # Sanitizers (Conditional)
    $<$<BOOL:${BUILD_WITH_SANITIZERS}>:-fsanitize=address>
    $<$<BOOL:${BUILD_WITH_SANITIZERS}>:-fsanitize=undefined>
)

#===============================================================================
# Link Options
#===============================================================================
target_link_options(lib-name PRIVATE
    # Security
    "LINKER:-z,relro"
    "LINKER:-z,now"
    "LINKER:-z,noexecstack"
    "LINKER:-z,defs"
    "LINKER:--no-undefined"
    
    # Optimization & Size
    "LINKER:--gc-sections"
    "LINKER:--icf=safe"
    "LINKER:--exclude-libs,ALL"
    "LINKER:--strip-all"
    "LINKER:--build-id=none"
    
    # Android Relocations
    "LINKER:--pack-dyn-relocs=android+relr"
    "LINKER:-z,pack-relative-relocs"
    "LINKER:--hash-style=gnu"
    
    # Memory & Page Size
    "LINKER:-O3"
    "LINKER:-z,stack-size=2097152"
    "LINKER:-z,max-page-size=16384"
    
    # Modern Features
    -flto=thin
    -fuse-ld=lld
    
    # Sanitizers (Conditional)
    $<$<BOOL:${BUILD_WITH_SANITIZERS}>:-fsanitize=address>
    $<$<BOOL:${BUILD_WITH_SANITIZERS}>:-fsanitize=undefined>
)

#===============================================================================
# Link Libraries
#===============================================================================
target_link_libraries(lib-name PRIVATE
    # Static Libraries
    libssl
    libcrypto
    libdobby
    libkeystone
    
    # System Libraries
    log
    android
    EGL
    GLESv3
    GLESv2
    GLESv1_CM
    z
    
    # Sanitizers (Conditional)
    $<$<BOOL:${BUILD_WITH_SANITIZERS}>:asan>
    $<$<BOOL:${BUILD_WITH_SANITIZERS}>:ubsan>
)

#===============================================================================
# Target Properties
#===============================================================================
set_target_properties(lib-name PROPERTIES
    C_VISIBILITY_PRESET hidden
    CXX_VISIBILITY_PRESET hidden
    VISIBILITY_INLINES_HIDDEN ON
    INTERPROCEDURAL_OPTIMIZATION TRUE
)