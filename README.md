<div align="center">

<img width="140" src="https://lh3.googleusercontent.com/pw/AP1GczOIozmNJFs-sqFM723fx8NhTNpdkAft8tc9VPUQ1L5A-7QdztmG8bNKt_x3aaGvJzbB2-8vzrD4DT9_n-PKjQB4Wl9kuL8e5h0IH3gsNUxCmQadv479LgJ1tz2XFLwmwIomSBonxmF0hIzB3MQiCxgL=w1024-h1024-s-no-gm" style="border-radius: 28px;">

# ⚡ PALLADIUM

**Android Mod Menu Framework**

<p>
  <img src="https://img.shields.io/badge/Android-9.0+-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/C++-20-00599C?logo=c%2B%2B&logoColor=white" alt="C++">
  <img src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/NDK-r29-8CA1AF?logo=android&logoColor=white" alt="NDK">
</p>

<p>
  🎨 Dynamic Island UI &nbsp;•&nbsp; 🔒 Anti-Detection &nbsp;•&nbsp; ⚡ Zero-Latency
</p>
</div>

---

## PALLADIUM

## Features

- [x] Memory Patching
- [x] Pattern Scanning
- [x] Library Dump
- [x] Signature Bypass
- [x] Lua 5.4 Scripting Engine
- [x] Script Encryption (AES-256-CBC)

---

### 📱 Target Device

- **Android Version:** 9.0+ (API 28)
- **Architecture:** ARM64 (arm64-v8a) only
- **RAM:** 4 GB minimum
- **Root:** Optional (for APK modding)

---

## 🛠️ Required Tools & Downloads

<div align="center">

<table>
<tr>
<td align="center" width="25%">

<img src="https://uxwing.com/wp-content/themes/uxwing/download/brands-and-social-media/android-studio-icon.png" width="48"><br><br>
<b>Android Studio</b><br>
<small>Ladybug+</small><br><br>
<a href="https://developer.android.com/studio">
<img src="https://img.shields.io/badge/Download-3DDC84?logo=android&logoColor=white&style=for-the-badge">
</a>

</td>
<td align="center" width="25%">

<img src="https://sensiml.com/documentation/_images/android-developers.png" width="48"><br><br>
<b>NDK</b><br>
<small>r29</small><br><br>
<a href="https://developer.android.com/ndk/downloads">
<img src="https://img.shields.io/badge/Download-8CA1AF?logo=android&logoColor=white&style=for-the-badge">
</a>

</td>
<td align="center" width="25%">

<img src="https://cmake.org/wp-content/uploads/2023/08/CMake-Logo.svg" width="48"><br><br>
<b>CMake</b><br>
<small>4.1.2+</small><br><br>
<a href="https://cmake.org/download/">
<img src="https://img.shields.io/badge/Download-064F8C?logo=cmake&logoColor=white&style=for-the-badge">
</a>

</td>
<td align="center" width="25%">

<img src="https://upload.wikimedia.org/wikipedia/en/thumb/3/30/Java_programming_language_logo.svg/1200px-Java_programming_language_logo.svg.png" width="48"><br><br>
<b>JDK</b><br>
<small>17+</small><br><br>
<a href="https://adoptium.net/">
<img src="https://img.shields.io/badge/Download-007396?logo=openjdk&logoColor=white&style=for-the-badge">
</a>

</td>
</tr>
</table>

</div>

### Installation Notes

- **Android Studio:** Install via JetBrains Toolbox or official installer
- **NDK:** Install through Android Studio → SDK Manager → SDK Tools
- **CMake:** Usually bundled with NDK, but can install separately
- **JDK:** Eclipse Temurin (Adoptium) recommended for best compatibility

---

## 🚀 Quick Start

### Step 1: Clone Repository

```bash
git clone --recursive https://github.com/axios-xiazy/Template-ZIG-Mod-Menu.git
cd Template-ZIG-Mod-Menu
```

### Step 2: Configure SDK Paths

Create `local.properties` in project root:

**macOS / Linux:**
```properties
sdk.dir=/home/username/Android/Sdk
ndk.dir=/home/username/Android/Sdk/ndk/29.0.14033849
```

**Windows:**
```properties
sdk.dir=C:\\Users\\Username\\AppData\\Local\\Android\\Sdk
ndk.dir=C:\\Users\\Username\\AppData\\Local\\Android\\Sdk\\ndk\\29.0.14033849
```

### Step 3: Build Project

```bash
# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Or build release (requires keystore setup)
./gradlew assembleRelease
```

### Build Outputs

| Variant | Location |
|---------|----------|
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `app/build/outputs/apk/release/app-release.apk` |
| Native Library | `app/build/intermediates/cmake/release/obj/arm64-v8a/liblib-name.so` |

---

## 📖 Documentation

<details>
<summary><h3>🎨 Feature System (Pipe Protocol)</h3></summary>

Features are defined as strings in `palladium.cpp` using the pipe-delimited format:

```cpp
const char *features[] = {
    // TYPE|PAGE|LABEL|ID|EXTRA
    
    // Create a new page tab
    "PAGE|0|icons/main.png|Main Menu",
    
    // Section header
    "TITLE|0|Player Features",
    
    // Toggle switch (returns boolean)
    "CHECK|0|Enable Hack Map|1",
    
    // Slider with range 0-100 (returns integer)
    "SLIDER|0|Aimbot Range|0|100|2",
    
    // Dropdown with options (returns selected index)
    "SPINNER|0|ESP Style|Classic,Modern,Minimal|3",
    
    // Text input (returns string)
    "INPUT|0|Player Name|4",
    
    // Button trigger (calls native function)
    "BUTTON|0|Dump Libraries|dump_lib_dialog"
};
```

**Format Specification:**

| Type | Format | Parameters | Returns |
|------|--------|------------|---------|
| `PAGE` | `PAGE|idx|icon_path|title` | page index, icon, title | N/A |
| `CHECK` | `CHECK|page|label|id` | page, label, unique id | boolean |
| `SLIDER` | `SLIDER|page|label|min|max|id` | page, label, min, max, id | integer |
| `SPINNER` | `SPINNER|page|label|opt1,opt2|id` | page, label, options, id | index |
| `INPUT` | `INPUT|page|label|id` | page, label, id | string |
| `BUTTON` | `BUTTON|page|label|callback` | page, label, callback name | trigger |

**Handling Callbacks:**

```cpp
JNIEXPORT void JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_Callback(
    JNIEnv *env, 
    jclass clazz,
    jint id,           // Feature ID
    jboolean check,    // Toggle value
    jint value,        // Slider/Spinner integer
    jfloat value2,     // Float value (optional)
    jstring value3     // String value (optional)
) {
    switch (id) {
        case 1: config.Hackmap = (bool)check; break;
        case 2: config.AimbotRange = (int)value; break;
        case 3: config.EspStyle = (int)value; break;
        case 4: {
            const char* name = env->GetStringUTFChars(value3, nullptr);
            strncpy(config.PlayerName, name, sizeof(config.PlayerName));
            env->ReleaseStringUTFChars(value3, name);
            break;
        }
    }
}
```
</details>

<details>
<summary><h3>💾 Memory Patching Guide</h3></summary>

**Basic Setup:**

```cpp
#include "KittyMemory/KittyInclude.hpp"

ElfScanner g_il2cppElf;

void main_thread() {
    // Wait for target library with retry
    while(!(g_il2cppElf = ElfScanner::findElf("libil2cpp.so")).isValid()) {
        std::this_thread::sleep_for(100ms);
    }
    
    uintptr_t base = g_il2cppElf.base();
    
    // Your patches here
    apply_patches(base);
}
```

**Patch Types:**

```cpp
// 1. NOP Instruction (ARM64)
// Replace instruction with NOP (0xD503201F)
MemoryPatch::createWithHex(base + 0x123456, "D503201F").Modify();

// 2. NOP Multiple Instructions
MemoryPatch::createWithHex(base + 0x123456, "D503201F D503201F D503201F").Modify();

// 3. Modify Float Value
float newDamage = 9999.0f;
KittyMemory::writeMemory(base + 0x789ABC, &newDamage, sizeof(float));

// 4. Modify Integer
int32_t newAmmo = 999;
KittyMemory::writeMemory(base + 0xDEF000, &newAmmo, sizeof(int32_t));

// 5. Hook Function
void* orig_function = nullptr;

void my_hook_function() {
    // Your code here
    
    // Call original
    ((void(*)())orig_function)();
}

DobbyHook(
    (void*)(base + 0xFEDCB0),
    (void*)my_hook_function,
    (void**)&orig_function
);

// 6. Pattern Scan
uintptr_t patternAddr = KittyMemory::findHexFirst(
    "libil2cpp.so",
    "00 00 00 00 ?? ?? ?? ?? FF FF",  // ? = wildcard
    base,
    size
);
```

**Restoring Patches:**

```cpp
// Store patches for restoration
std::vector<MemoryPatch> patches;

patches.push_back(MemoryPatch::createWithHex(base + 0x123456, "D503201F"));
patches[0].Modify();  // Apply

// Later...
patches[0].Restore();  // Restore original
```

**Auto-Initialization:**

```cpp
// This runs automatically when library loads
__attribute__((constructor))
void init() {
    std::thread(main_thread).detach();
}
```
</details>

<details>
<summary><h3>💉 Injection Methods</h3></summary>

#### Method A: APK Modding (Recommended for Beginners)

1. Inject smali code
Find onCreate method and add : 
```smali
invoke-static {p0}, Lzig/cheat/qq/ϟ;->ϟ(Landroid/content/Context;)V
```
</details>

---

## ❓ Troubleshooting

<details>
<summary><b>🔴 Build Error: "NDK not found"</b></summary>

**Cause:** NDK path not configured or wrong version

**Solution:**
1. Verify `local.properties`:
   ```properties
   ndk.dir=/path/to/Android/Sdk/ndk/29.0.14033849
   ```
2. Must be exactly version **r29 (29.0.14033849)**
3. Install via Android Studio → SDK Manager → SDK Tools
</details>

<details>
<summary><b>🔴 Runtime Error: UnsatisfiedLinkError</b></summary>

**Causes:**
1. JNI signature mismatch between Java and C++
2. Wrong library name in `ϟ.java`
3. Architecture mismatch (not ARM64)

**Solution:**
```bash
# Check library exports
objdump -T liblib-name.so | grep Java_

# Verify in Java:
System.loadLibrary("lib-name");  // Without "lib" prefix and ".so" suffix

# Check APK contains arm64-v8a libs:
unzip -l app.apk | grep lib/
```
</details>

<details>
<summary><b>🔴 Menu Not Appearing</b></summary>

**Causes:**
1. Missing `SYSTEM_ALERT_WINDOW` permission
2. App blocks overlay permissions
3. Menu initialized before permission granted

**Solution:**
```java
// Check and request permission
if (!Settings.canDrawOverlays(context)) {
    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:" + context.getPackageName()));
    context.startActivity(intent);
}
```

Check logcat: `adb logcat | grep -i "palladium\|floatingmenu"`
</details>

<details>
<summary><b>🔴 Patches Not Applying</b></summary>

**Causes:**
1. Offset is absolute instead of base-relative
2. Target library not yet loaded
3. Wrong permission on `/proc/self/mem`

**Solution:**
```cpp
// Correct: base-relative offset
uintptr_t base = ElfScanner::findElf("libil2cpp.so").base();
MemoryPatch::createWithHex(base + 0x123456, "00").Modify();

// Wait for library
while(!ElfScanner::findElf("libil2cpp.so").isValid()) {
    std::this_thread::sleep_for(100ms);
}
```
</details>

<details>
<summary><b>🔴 App Detects Modding</b></summary>

**Enable all anti-detection features:**
```cpp
// In palladium.cpp main_thread()
AntiDetectionManager.init(context);

// Add to C++:
// - Frida scanner loop
// - PTRACE blocker
// - Syscall obfuscation
```

Check if target app uses:
- SafetyNet / Play Integrity API
- Custom anti-cheat (EasyAntiCheat, BattlEye)
- Server-side validation
</details>

---

## 🙏 Credits & Third-Party Libraries

| Library | Author | Purpose | License |
|---------|--------|---------|---------|
| **KittyMemory** | CyberCats | Memory manipulation, ELF scanning | MIT |
| **Dobby** | jmpews | Dynamic function hooking | Apache 2.0 |
| **oxorany** | - | Compile-time string obfuscation | - |

---

<div align="center">

## 📄 License

⚠️ This project is provided for **educational and research purposes only**.

By using this software, you agree to:
- Comply with all applicable laws in your jurisdiction
- Respect the terms of service of target applications
- Use responsibly and ethically

The authors assume no liability for misuse of this software.

<br>

[![Stars](https://img.shields.io/github/stars/axios-xiazy/Template-ZIG-Mod-Menu?style=social)](https://github.com/axios-xiazy/Template-ZIG-Mod-Menu)
[![Forks](https://img.shields.io/github/forks/axios-xiazy/Template-ZIG-Mod-Menu?style=social)](https://github.com/axios-xiazy/Template-ZIG-Mod-Menu)

<br>

**Built with precision. Use with responsibility.**

</div>
