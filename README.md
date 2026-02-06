<div align="center">

<img width="140" src="https://lh3.googleusercontent.com/pw/AP1GczM-WZ2-Up5Ku1boFsdARseI7d5K6uo2nGFVghMTkF2Jpxr2jRflupPRh4YXjs9aAiaJxnV6xlXZBzmrosoxhIXVreXeBx8WFqMXiWGeqDLIxPY80VhDrTqQm9VGwe6scksRyrGbNhdhEN5ONIkwTypT=w320-h320-s-no-gm?authuser=0" style="border-radius: 28px;">

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

[🚀 Quick Start](#quick-start) • [✨ Features](#-feature-showcase) • [🏗️ Architecture](#-architecture) • [🔒 Security](#-security-system)

</div>

---

## 🎯 What is PALLADIUM?

**PALLADIUM** is a complete, production-ready framework for building floating mod menus on Android. Designed for professional developers who demand performance, security, and flexibility.

### Why Choose PALLADIUM?

<table>
<tr>
<td width="50%" valign="top">

**🚫 Without PALLADIUM:**
- Spend weeks building UI from scratch
- Handle JNI crashes and memory leaks
- Worry about detection and bans
- Write obfuscation code manually
- Debug performance issues

</td>
<td width="50%" valign="top">

**✅ With PALLADIUM:**
- Ready-to-use 120 FPS UI out of the box
- Stable JNI bridge with error handling
- Multi-layer anti-detection built-in
- Automatic string obfuscation
- Optimized native performance

</td>
</tr>
</table>

---

## ✨ Feature Showcase

### 🎨 User Interface

| Feature | Description | Benefit |
|---------|-------------|---------|
| **Dynamic Island Design** | Collapsible floating menu with smooth morphing animations | Modern look, minimal screen obstruction |
| **120 FPS Rendering** | Hardware-accelerated animations at 120 frames per second | Butter-smooth user experience |
| **Custom Components** | Switch, Slider, Dropdown, Button - all custom-drawn | Consistent premium feel |
| **Custom Typography** | Support for custom TTF fonts throughout the UI | Brand identity and readability |
| **Theme System** | Centralized color and dimension constants | Easy customization |

### 🛠️ Core Capabilities

| Feature | Description | Use Case |
|---------|-------------|----------|
| **Memory Patching** | Read/write process memory with KittyMemory | Modify game values in real-time |
| **Function Hooking** | Hook native functions using Dobby | Intercept and modify game logic |
| **ELF Scanning** | Find loaded libraries and their base addresses | Locate target code dynamically |
| **Pattern Scanning** | Search memory for byte patterns | Find offsets automatically |
| **Library Dumper** | Extract loaded `.so` files from memory | Reverse engineering analysis |
| **Offset Patcher** | Apply/restore patches without recompiling | Rapid testing and iteration |

### 🔒 Security & Anti-Detection

| Layer | Mechanism | Protection Against |
|-------|-----------|-------------------|
| **String Obfuscation** | XOR encryption at compile time | String analysis, signature detection |
| **Library Name Obfuscation** | XOR-encoded library name | Library detection, pattern matching |
| **Anti-Debug** | PTRACE_TRACEME + TracerPID check | GDB, LLDB, strace attachment |
| **Frida Detection** | Memory scan for Frida signatures | Dynamic instrumentation |
| **Xposed Detection** | Check for Xposed framework presence | Method hooking frameworks |
| **Emulator Detection** | Hardware fingerprint analysis | Bluestacks, LDPlayer, etc. |
| **Network Obfuscation** | HTTPS + randomized headers | Traffic analysis, MITM detection |
| **Build Obfuscation** | ProGuard + symbol stripping | Decompilation, reverse engineering |

### 🧰 Built-in Developer Tools

```
┌─────────────────────────────────────────────────────────────┐
│                    🔧 PALLADIUM TOOLKIT                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📤 Library Dumper                                          │
│  ├── List all loaded libraries                              │
│  ├── Dump individual .so files                              │
│  ├── Dump global-metadata.dat (IL2CPP)                      │
│  └── Export to user-selected directory                      │
│                                                             │
│  🔧 Offset Patcher                                          │
│  ├── Create patches with hex bytes                          │
│  ├── Apply/restore patches on-the-fly                       │
│  ├── Memory hex viewer                                      │
│  ├── Pattern scanner                                        │
│  ├── Export/Import JSON configs                             │
│  └── Real-time patch status monitoring                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 System Requirements

### 💻 Development Machine

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| **OS** | Windows 10 / macOS 10.15 / Linux | Windows 11 / macOS 14 / Ubuntu 22.04 |
| **RAM** | 8 GB | 16 GB+ |
| **Storage** | 10 GB free | 50 GB+ (with emulator) |
| **CPU** | 4 cores | 8 cores+ |

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

## 🏗️ Architecture

### Three-Layer Design

```
┌─────────────────────────────────────────────────────────────────┐
│                     🎨 PRESENTATION LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ FloatingMenu │  │   Sidebar    │  │   Content Panels     │  │
│  │  (120 FPS)   │  │ (Navigation) │  │ • SwitchRow          │  │
│  └──────────────┘  └──────────────┘  │ • ModernSeekBar      │  │
│                                      │ • ModernDropdown     │  │
│  Theme: Deep Charcoal + Blue Accent  │ • TrafficLightButton │  │
│  Font: Custom TTF via FontManager    └──────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │ JNI
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      🔌 BRIDGE LAYER                             │
│                                                                  │
│   StealthJNI.java                                                │
│   ├── Static native methods (optimized performance)             │
│   ├── XOR-obfuscated library loading                            │
│   ├── Type-safe JNI marshalling                                 │
│   └── Local reference management                                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                              │ Native
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     ⚙️ NATIVE CORE (C++20)                       │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────┐  │
│  │ palladium   │  │ KittyMemory  │  │         Dobby          │  │
│  │    .cpp     │  │ • Read/Write │  │    • Function Hook     │  │
│  │             │  │ • ELF Scan   │  │    • ARM64/x86_64      │  │
│  │  Features   │  │ • Pattern    │  │    • Zero Overhead     │  │
│  │  Config     │  │   Scanner    │  │                        │  │
│  │  Callbacks  │  │ • Patching   │  │                        │  │
│  └─────────────┘  └──────────────┘  └────────────────────────┘  │
│                                                                  │
│  Security: oxorany │ Anti-debug │ PTRACE │ Frida Detection      │
└─────────────────────────────────────────────────────────────────┘
```

### Initialization Flow

```
1. Library Loaded ──────▶ __attribute__((constructor)) triggers
            │
            ▼
2. Auto-Init ───────────▶ Creates detached thread (main_thread)
            │
            ▼
3. Wait for Target ─────▶ Polls for libil2cpp.so (or target lib)
            │
            ▼
4. Initialize ──────────▶ Gets base address, applies patches
            │
            ▼
5. UI Ready ────────────▶ Java side creates floating window
            │
            ▼
6. User Interaction ────▶ JNI callbacks update config & patches
```

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

Best for games without integrity checks.

```bash
# 1. Decompile target APK
apktool d target-game.apk -o game-folder

# 2. Copy PALLADIUM smali classes
cp -r Template-ZIG-Mod-Menu/app/smali/zig game-folder/smali/

# 3. Copy native library
cp Template-ZIG-Mod-Menu/app/build/intermediates/cmake/release/obj/arm64-v8a/liblib-name.so \
   game-folder/lib/arm64-v8a/

# 4. Add permissions to AndroidManifest.xml
cat >> game-folder/AndroidManifest.xml << 'EOF'
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.INTERNET" />
EOF

# 5. Inject startup in MainActivity.smali
# Find onCreate method and add:
# invoke-static {p0}, Lzig/cheat/qq/ϟ;->ϟ(Landroid/content/Context;)V

# 6. Rebuild
apktool b game-folder -o modded-game.apk

# 7. Generate keystore (if needed)
keytool -genkey -v -keystore mykey.keystore -alias alias_name \
  -keyalg RSA -keysize 2048 -validity 10000

# 8. Sign APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore mykey.keystore modded-game.apk alias_name

# 9. Align
zipalign -v 4 modded-game.apk final-game.apk
```

#### Method B: Dynamic Injection (Advanced)

**Using Frida:**

```javascript
// palladium_loader.js
const moduleName = "liblib-name.so";
const libraryPath = "/data/local/tmp/" + moduleName;

// Push library first
// adb push liblib-name.so /data/local/tmp/

Interceptor.attach(Module.findExportByName(null, "android_dlopen_ext"), {
    onEnter: function(args) {
        this.path = Memory.readCString(args[0]);
    },
    onLeave: function(retval) {
        if (this.path && this.path.includes("libil2cpp.so")) {
            console.log("[+] Target loaded, injecting PALLADIUM...");
            var load = new NativeFunction(
                Module.findExportByName(null, "android_dlopen_ext"),
                'pointer', ['pointer', 'int', 'pointer']
            );
            var path = Memory.allocUtf8String(libraryPath);
            load(path, 0x2, ptr(0));
        }
    }
});
```

Run: `frida -U -f com.game.package -l palladium_loader.js --no-pause`

#### Method C: Xposed Module

```java
public class PALLADIUMModule implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.game.package")) return;
        
        XposedHelpers.findAndHookMethod(
            "android.app.Application",
            lpparam.classLoader,
            "onCreate",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Context ctx = (Context) param.thisObject;
                    System.loadLibrary("lib-name");
                    zig.cheat.qq.ϟ.ϟ(ctx);
                }
            }
        );
    }
}
```
</details>

<details>
<summary><h3>🎨 Customization Guide</h3></summary>

**Changing Colors:**

Edit `app/src/main/java/zig/cheat/qq/ui/theme/ThemeConstants.java`:

```java
public class ThemeConstants {
    // Primary accent colors
    public static final int COLOR_ACCENT_BLUE = 0xFF3B82F6;
    public static final int COLOR_ACCENT_PURPLE = 0xFF8B5CF6;
    
    // Background colors
    public static final int COLOR_BG_DARK = 0xFF0F0F0F;
    public static final int COLOR_BG_CARD = 0x1A1A1A;
    public static final int COLOR_BG_SIDEBAR = 0x1E1E1E;
    
    // Text colors
    public static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int COLOR_TEXT_SECONDARY = 0xB3FFFFFF;
    
    // Border and divider
    public static final int COLOR_BORDER = 0xFF333333;
}
```

**Changing Fonts:**

1. Replace `app/src/main/assets/fonts/font_main.ttf` with your font
2. Or modify `FontManager.java` to load multiple fonts

**Changing Package Name:**

1. Rename directory: `java/zig/cheat/qq/` → `java/com/yourname/mod/`
2. Update `app/build.gradle`:
   ```gradle
   defaultConfig {
       applicationId 'com.yourname.mod'
   }
   ```
3. Update JNI signatures in `palladium.cpp`:
   ```cpp
   // Old: Java_zig_cheat_qq_native_1bridge_StealthJNI
   // New: Java_com_yourname_mod_native_1bridge_StealthJNI
   ```
4. Update all Java package declarations
5. Rebuild project

**Custom Components:**

Create new component by extending existing ones:

```java
public class CustomButton extends LinearLayout {
    public CustomButton(Context context, String label, int id) {
        super(context);
        // Your custom implementation
    }
}
```

Then add to `ContentPanel.java`:

```java
else if (type.equals("CUSTOM")) {
    CustomButton btn = new CustomButton(context, label, id);
    contentLayout.addView(btn);
}
```
</details>

---

## 🔒 Security System

### Multi-Layer Protection

```
┌─────────────────────────────────────────────────────────────────┐
│                    🔒 PALLADIUM SECURITY                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  LAYER 1: Build-Time Protection                                  │
│  ├── String Obfuscation (oxorany)                               │
│  │   └── XOR encrypt strings at compile time                    │
│  ├── Library Name Obfuscation                                    │
│  │   └── XOR-encoded name in integer array                      │
│  └── ProGuard/R8 obfuscation                                     │
│      └── Minify and obfuscate Java code                         │
│                                                                  │
│  LAYER 2: Runtime Protection                                     │
│  ├── PTRACE_TRACEME                                              │
│  │   └── Blocks external debuggers                              │
│  ├── TracerPID Check                                             │
│  │   └── Monitors /proc/self/status                             │
│  └── Frida/Xposed Detection                                      │
│      └── Scans /proc/self/maps for signatures                   │
│                                                                  │
│  LAYER 3: Communication Protection                               │
│  ├── HTTPS Enforcement                                           │
│  │   └── TLS 1.3 for all network calls                          │
│  ├── Header Masking                                              │
│  │   └── Randomized User-Agent per request                      │
│  └── Traffic Obfuscation                                         │
│      └── Fake requests to mask patterns                         │
│                                                                  │
│  LAYER 4: Memory Protection                                      │
│  ├── Syscall Obfuscation                                         │
│  │   └── Direct syscalls bypass libc hooks                      │
│  ├── Library Hiding                                              │
│  │   └── Removes from /proc/self/maps                           │
│  └── Anti-Dumping                                                │
│      └── Prevents memory extraction                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📂 Project Structure

```
Template-ZIG-Mod-Menu/
│
├── 📁 app/
│   ├── 📁 src/main/
│   │   │
│   │   ├── 📁 java/zig/cheat/qq/              🚀 Entry & UI Layer
│   │   │   │
│   │   │   ├── ϟ.java                         # Entry point (launcher)
│   │   │   │   └── Loads native library
│   │   │   │   └── Initializes FloatingMenu
│   │   │   │
│   │   │   ├── MainActivity.java              # Demo activity
│   │   │   │
│   │   │   ├── 📁 native_bridge/              🔌 JNI Bridge
│   │   │   │   └── StealthJNI.java
│   │   │   │       ├── initSecurity()
│   │   │   │       ├── getFeatures()
│   │   │   │       ├── Callback()
│   │   │   │       ├── getLoadedLibs()
│   │   │   │       ├── dumpLib()
│   │   │   │       ├── createPatch()
│   │   │   │       └── scanPatternAll()
│   │   │   │
│   │   │   ├── 📁 security/                   🛡️ Anti-Detection
│   │   │   │   ├── AntiDetectionManager.java  # Main coordinator
│   │   │   │   ├── CryptoUtils.java           # Encryption helpers
│   │   │   │   └── TrafficObfuscator.java     # Network masking
│   │   │   │
│   │   │   ├── 📁 ui/                         🎨 User Interface
│   │   │   │   ├── FloatingMenu.java          # Main floating window
│   │   │   │   ├── components/
│   │   │   │   │   ├── ContentPanel.java      # Menu content container
│   │   │   │   │   ├── Sidebar.java           # Navigation sidebar
│   │   │   │   │   ├── SwitchRow.java         # Toggle control
│   │   │   │   │   ├── ModernSeekBar.java     # Slider control
│   │   │   │   │   ├── ModernDropdown.java    # Dropdown control
│   │   │   │   │   └── SmoothSwitch.java      # Animated switch
│   │   │   │   ├── dialogs/
│   │   │   │   │   ├── DumpLibDialog.java     # Library dumper UI
│   │   │   │   │   └── PatchOffsetDialog.java # Offset patcher UI
│   │   │   │   └── theme/
│   │   │   │       ├── ThemeConstants.java    # Colors & dimensions
│   │   │   │       └── FontManager.java       # Custom font loader
│   │   │   │
│   │   │   └── 📁 network/                    🌐 Network Layer
│   │   │       └── SecureVolleyClient.java    # HTTPS client
│   │   │
│   │   ├── 📁 cpp/                            ⚙️ Native Core
│   │   │   ├── 📁 core/
│   │   │   │   └── palladium.cpp              # Main implementation
│   │   │   │       ├── Feature definitions
│   │   │   │       ├── JNI implementations
│   │   │   │       ├── GameConfig struct
│   │   │   │       └── main_thread()
│   │   │   │
│   │   │   └── 📁 include/
│   │   │       ├── KittyMemory/               💾 Memory manipulation
│   │   │       │   ├── ElfScanner.hpp         # ELF parsing
│   │   │       │   ├── MemoryPatch.hpp        # Byte patching
│   │   │       │   └── KittyUtils.hpp         # Utilities
│   │   │       │
│   │   │       ├── Dobby/                     🪝 Hooking framework
│   │   │       │   └── dobby.h                # Hook API
│   │   │       │
│   │   │       ├── oxorany/                   🔤 String obfuscation
│   │   │       │   └── oxorany_include.h      # XOR encryption
│   │   │       │
│   │   │       └── Tools/                     🛠️ Helper utilities
│   │   │           └── Tools.h
│   │   │
│   │   ├── 📁 assets/                         📦 Static Assets
│   │   │   └── 📁 fonts/
│   │   │       └── font_main.ttf              # Custom typography
│   │   │
│   │   ├── 📁 res/                            📱 Android Resources
│   │   │   ├── drawable/                      # Icons & shapes
│   │   │   ├── values/                        # Strings & colors
│   │   │   └── xml/                           # Config files
│   │   │
│   │   └── AndroidManifest.xml                # App configuration
│   │
│   ├── build.gradle                           # App build config
│   └── proguard-rules.pro                     # Obfuscation rules
│
├── 📁 gradle/                                 # Gradle wrapper files
│
├── build.gradle                               # Root build configuration
├── settings.gradle                            # Project settings
├── gradle.properties                          # Gradle properties
└── README.md                                  # This file
```

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
