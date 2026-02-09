cmake_minimum_required(VERSION 3.24.1)

project("PALLADIUM" VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(PAGE_SIZE 4096)
math(EXPR PAGE_MASK "${PAGE_SIZE} - 1")
math(EXPR PAGE_MASK "~${PAGE_MASK}")

if(NOT ANDROID_ABI STREQUAL "arm64-v8a")
    message(FATAL_ERROR "Requires arm64-v8a")
endif()

file(GLOB_RECURSE SOURCES CONFIGURE_DEPENDS
    "core/*.cpp"
    "modules/*.cpp"
    "include/Tools/Tools.cpp"
    "include/oxorany/oxorany.cpp"
    "include/KittyMemory/*.cpp"
    "include/lua/*.c"
)
list(REMOVE_ITEM SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/include/lua/lua.c")
list(REMOVE_ITEM SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/include/lua/luac.c")

macro(import_lib name path)
    add_library(${name} STATIC IMPORTED)
    set_target_properties(${name} PROPERTIES IMPORTED_LOCATION ${path})
endmacro()

import_lib(libssl "${CMAKE_CURRENT_SOURCE_DIR}/include/openssl/lib/libssl.a")
import_lib(libcrypto "${CMAKE_CURRENT_SOURCE_DIR}/include/openssl/lib/libcrypto.a")
import_lib(libdobby "${CMAKE_CURRENT_SOURCE_DIR}/include/Dobby/libraries/${ANDROID_ABI}/libdobby.a")
import_lib(libkeystone "${CMAKE_CURRENT_SOURCE_DIR}/include/KittyMemory/Deps/Keystone/libs-android/${ANDROID_ABI}/libkeystone.a")

add_library(lib-name SHARED ${SOURCES})

target_include_directories(lib-name PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/core
    ${CMAKE_CURRENT_SOURCE_DIR}/modules
    ${CMAKE_CURRENT_SOURCE_DIR}/modules/dump
    ${CMAKE_CURRENT_SOURCE_DIR}/modules/patcher
    ${CMAKE_CURRENT_SOURCE_DIR}/modules/signature
    ${CMAKE_CURRENT_SOURCE_DIR}/modules/lua
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/include/lua
    ${CMAKE_CURRENT_SOURCE_DIR}/include/openssl/include
)

target_compile_options(lib-name PRIVATE
    -O3
    -fno-rtti
    -fPIC
    -fvisibility=hidden
    -fexceptions
    -fno-inline-functions
    -ffunction-sections
    -fdata-sections
    -fno-unwind-tables
    -fno-asynchronous-unwind-tables
    
    # Warnings as Errors
    -Werror=format-security
    -Werror=return-type
    -Werror=implicit-function-declaration
    -Werror=int-conversion
    -Werror=incompatible-pointer-types
    -Wno-multichar
)

target_link_options(lib-name PRIVATE
    "LINKER:--strip-all"
    "LINKER:--gc-sections"
    "LINKER:--icf=all"
    "LINKER:--exclude-libs,ALL"
    "LINKER:--build-id=none"
    "LINKER:-z,noexecstack"
    "LINKER:--hash-style=sysv"
    "LINKER:-z,stack-size=2097152"
    "LINKER:-z,max-page-size=16384"
)

target_link_libraries(lib-name PRIVATE
    libssl
    libcrypto
    libdobby
    libkeystone
    log
    android
    z
)

set_target_properties(lib-name PROPERTIES
    C_VISIBILITY_PRESET hidden
    CXX_VISIBILITY_PRESET hidden
    VISIBILITY_INLINES_HIDDEN ON
)