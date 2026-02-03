<div align="center">

<img src="https://lh3.googleusercontent.com/pw/AP1GczM-WZ2-Up5Ku1boFsdARseI7d5K6uo2nGFVghMTkF2Jpxr2jRflupPRh4YXjs9aAiaJxnV6xlXZBzmrosoxhIXVreXeBx8WFqMXiWGeqDLIxPY80VhDrTqQm9VGwe6scksRyrGbNhdhEN5ONIkwTypT=w320-h320-s-no-gm?authuser=0" width="140" height="140" alt="PALLADIUM Logo">

# ⚡ PALLADIUM 
### Enterprise-Grade Android Mod Menu Framework

[![Android](https://img.shields.io/badge/Android-9.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![C++](https://img.shields.io/badge/C%2B%2B-20-00599C?style=for-the-badge&logo=c%2B%2B&logoColor=white)](https://isocpp.org/)
[![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![NDK](https://img.shields.io/badge/NDK-r26c-8CA1AF?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/ndk)
[![License](https://img.shields.io/badge/License-Educational-FF6B6B?style=for-the-badge)](LICENSE)

<p align="center">
  <b>🎨 Dynamic Island UI | 🔒 Multi-Layer Anti-Detection | 🚀 Zero-Latency Hooking | 📦 Modular Architecture</b>
</p>

<p align="center">
  <a href="#overview">ภาพรวม</a> •
  <a href="#structure">โครงสร้าง</a> •
  <a href="#setup">ติดตั้ง</a> •
  <ba>
  <a href="#building">Build</a> •
  <a href="#injection">Inject</a> •
  <a href="#development">พัฒนา</a> •
  <a href="#roadmap">Roadmap</a>
</p>

</div>

---

## 📋 สารบัญ

1. [ภาพรวมโครงการ](#-ภาพรวมโครงการ)
2. [โครงสร้างโปรเจค](#-โครงสร้างโปรเจค)
3. [การติดตั้งและเตรียม Environment](#-การติดตั้งและเตรียม-environment)
4. [การ Build โปรเจค](#-การ-build-โปรเจค)
5. [เทคนิคการ Inject](#-เทคนิคการ-inject)
6. [การแก้ไขและปรับแต่งโค้ด](#-การแก้ไขและปรับแต่งโค้ด)
7. [การปรับแต่งประสิทธิภาพ](#-การปรับแต่งประสิทธิภาพ)
8. [Offset และ Patching](#-offset-และ-patching)
9. [การใช้งาน KittyMemory และ Dobby](#-การใช้งาน-kittymemory-และ-dobby)
10. [การพัฒนาต่อในอนาคต](#-การพัฒนาต่อในอนาคต)
11. [การแก้ไขปัญหา](#-การแก้ไขปัญหา)
12. [หมายเหตุและข้อควรระวัง](#-หมายเหตุและข้อควรระวัง)

---

## 🎯 ภาพรวมโครงการ

### PALLADIUM คืออะไร?

**PALLADIUM** เป็นเฟรมเวิร์คครบวงจรสำหรับการพัฒนา Mod Menu บนระบบปฏิบัติการ Android ที่ออกแบบมาสำหรับนักพัฒนาระดับมืออาชีพ โดยมีจุดมุ่งหมายเพื่อลดความซับซ้อนในการสร้างเมนูมอดเกม แต่ยังคงไว้ซึ่งความยืดหยุ่นและประสิทธิภาพสูงสุด

### สถาปัตยกรรมหลัก

```
┌─────────────────────────────────────────────────────────────┐
│                    PALLADIUM ARCHITECTURE                   │
├─────────────────────────────────────────────────────────────┤
│  LAYER 3: UI Layer (Java/Kotlin)                           │
│  ├── Floating Menu (Dynamic Island Design)                 │
│  ├── Sidebar Navigation                                     │
│  ├── Content Panels (Switch, Slider, Dropdown)             │
│  └── Animation System (120 FPS)                            │
├─────────────────────────────────────────────────────────────┤
│  LAYER 2: Bridge Layer (JNI)                               │
│  ├── StealthJNI (Native Bridge)                            │
│  └── Type Conversion & Safety                              │
├─────────────────────────────────────────────────────────────┤
│  LAYER 1: Native Core (C/C++)                              │
│  ├── Memory Patching (KittyMemory)                         │
│  ├── Function Hooking (Dobby)                              │
│  ├── ELF Scanner & Parser                                  │
│  └── Security & Obfuscation                                │
├─────────────────────────────────────────────────────────────┤
│  LAYER 0: Target Application                               │
│  └── Game/App Process Space                                │
└─────────────────────────────────────────────────────────────┘
```

### จุดเด่นที่สำคัญ

| ฟีเจอร์ | รายละเอียด | ประโยชน์ |
|---------|------------|----------|
| 🎨 **Dynamic Island UI** | ดีไซน์แบบย่อ/ขยายได้ พร้อมอนิเมชั่นลื่นไหล 120 FPS | ดูทันสมัย ใช้งานสะดวก ไม่บดบังเกม |
| 🛡️ **Multi-Layer Anti-Detection** | ป้องกันทั้งฝั่ง Java (Reflection Detection) และ Native (Library Hiding) | ลดความเสี่ยงถูกแบน |
| 🔧 **Modular Component System** | แยกส่วน UI เป็น独立 Components ที่ผสมผสานได้ | แก้ไขง่าย ต่อยอดสะดวก |
| ⚡ **Zero-Latency Hooking** | ใช้ Dobby Hook ที่มี overhead ต่ำที่สุด | ไม่กระทบประสิทธิภาพเกม |
| 🧩 **Auto-Initialization** | ระบบ initialize อัตโนมัติเมื่อ inject เข้า target | ไม่ต้อง config ซับซ้อน |
| 📦 **One-Shot Build** | Build ครั้งเดียวได้ทั้ง APK และ Library | ลดขั้นตอนการ deploy |

### กลุ่มเป้าหมาย

- **Mod Developers**: ผู้ที่ต้องการสร้างเมนูมอดอย่างรวดเร็วโดยไม่ต้องเขียน UI เอง
- **Reverse Engineers**: นักวิเคราะห์ที่ต้องการเครื่องมือในการทดสอบแอปพลิเคชัน
- **Security Researchers**: ผู้ศึกษาด้านความปลอดภัยบน Android

> ⚠️ **คำเตือนทางกฎหมาย**: โครงการนี้สร้างขึ้นเพื่อวัตถุประสงค์ทางการศึกษาและการวิจัยด้านความปลอดภัยเท่านั้น ผู้ใช้ต้องปฏิบัติตามข้อกำหนดของแอปพลิเคชันเป้าหมายและกฎหมายท้องถิ่น ผู้พัฒนาไม่รับผิดชอบต่อการใช้งานที่ผิดกฎหมาย

---

## 🏗️ โครงสร้างโปรเจค

### แผนผังโครงสร้างไฟล์ (Project Tree)

```
PALLADIUM/
├── 📁 app/
│   ├── 📁 src/
│   │   ├── 📁 main/
│   │   │   ├── 📁 java/zig/cheat/qq/           # Java Source Code
│   │   │   │   ├── 📁 native_bridge/           # JNI Bridge Layer
│   │   │   │   │   └── StealthJNI.java         # Main JNI Bridge
│   │   │   │   ├── 📁 security/                # Anti-Detection Module
│   │   │   │   │   └── AntiDetectionManager.java
│   │   │   │   ├── 📁 ui/                      # UI Controllers
│   │   │   │   │   ├── FloatingMenu.java       # Main Menu Controller
│   │   │   │   │   └── ϟ.java                  # Entry Point (Launcher)
│   │   │   │   ├── 📁 ui/components/           # UI Components
│   │   │   │   │   ├── ContentPanel.java       # Menu Content Container
│   │   │   │   │   ├── ModernDropdown.java     # Custom Dropdown
│   │   │   │   │   ├── ModernSeekBar.java      # Custom Slider
│   │   │   │   │   └── Sidebar.java            # Navigation Sidebar
│   │   │   │   ├── 📁 ui/rows/                 # Menu Item Rows
│   │   │   │   │   ├── SwitchRow.java          # Toggle Switch
│   │   │   │   │   ├── SeekBarRow.java         # Slider Row
│   │   │   │   │   └── ValueBarRow.java        # Value Display
│   │   │   │   ├── 📁 ui/theme/                # Theme & Styling
│   │   │   │   │   └── ThemeConstants.java     # Color & Dimension Constants
│   │   │   │   └── 📁 utils/                   # Utilities
│   │   │   │       ├── AnimUtils.java          # Animation Helpers
│   │   │   │       └── FontManager.java        # Custom Font Loader
│   │   │   ├── 📁 cpp/                         # Native C++ Code
│   │   │   │   ├── 📁 core/                    # Core Implementation
│   │   │   │   │   └── palladium.cpp           # Main Native Logic
│   │   │   │   ├── 📁 include/                 # Header Files
│   │   │   │   │   ├── KittyMemory/            # Memory Manipulation
│   │   │   │   │   ├── Dobby/                  # Hooking Framework
│   │   │   │   │   └── oxorany/                # String Obfuscation
│   │   │   │   └── CMakeLists.txt              # Native Build Config
│   │   │   ├── 📁 assets/                      # Static Assets
│   │   │   │   └── fonts/                      # Custom Fonts
│   │   │   ├── 📁 res/                         # Android Resources
│   │   │   │   ├── drawable/                   # Icons & Shapes
│   │   │   │   ├── layout/                     # XML Layouts
│   │   │   │   └── values/                     # Strings & Colors
│   │   │   └── AndroidManifest.xml             # App Manifest
│   │   └── 📁 test/                            # Unit Tests
│   ├── build.gradle                            # App Build Configuration
│   └── proguard-rules.pro                      # Obfuscation Rules
├── 📁 gradle/
│   └── wrapper/                                # Gradle Wrapper
├── build.gradle                                # Root Build Config
├── settings.gradle                             # Project Settings
├── gradle.properties                           # Gradle Properties
├── gradlew                                     # Gradle Wrapper Script
├── README.md                                   # This File
```

### อธิบายแต่ละส่วนโดยละเอียด

#### 1. Native Bridge Layer (`native_bridge/`)

**ไฟล์หลัก: `StealthJNI.java`**

นี่คือสะพานเชื่อมระหว่าง Java World และ Native World มีหน้าที่:
- โหลด Native Library (`.so`) เมื่อ Class ถูกโหลดครั้งแรก
- แปลงชื่อ Library จาก XOR Obfuscation (ป้องกันการค้นหา string ง่ายๆ)
- ให้ Java เรียกใช้ Native Methods ผ่าน JNI

```java
public class StealthJNI {
    static {
        // โหลด library โดยแปลงชื่อจาก XOR
        System.loadLibrary(o(new int[]{37, 32, 43, 100, 39, 40, 36, 44}));
        // ผลลัพธ์คือ "lib-name" หลัง XOR กับ 0x49
    }
    
    // Native methods ที่ C++ จะ implement
    public static native String[] getFeatures();
    public static native void Callback(int id, boolean check, int value, float value2, String value3);
}
```

#### 2. Security Module (`security/`)

**ไฟล์หลัก: `AntiDetectionManager.java`**

จัดการระบบป้องกันการตรวจจับทั้งหมด:
- ตรวจสอบว่าอยู่ใน Emulator หรือไม่
- ซ่อนการมีอยู่ของ Xposed/Frida
- ตรวจจับ Debugger Attachment
- ป้องกัน Memory Dump

#### 3. UI Layer (`ui/`)

##### 3.1 FloatingMenu.java
ตัวควบคุมหลักของเมนูลอยบนหน้าจอ มีความสามารถ:
- สร้าง Window แบบ TYPE_APPLICATION (ลอยเหนือแอปอื่น)
- จัดการ Touch Events (Drag, Tap, Swipe)
- ควบคุม Animation (Expand/Collapse)
- จัดการ Lifecycle (Show/Hide/Destroy)

##### 3.2 Sidebar.java
แถบนำทางด้านข้าง:
- แสดงรายการ Category (เมนูหลัก, ฟีเจอร์, ตั้งค่า)
- โหลดไอคอนจาก Assets
- Animation ตัวเลือกที่ถูกเลือก (Selection Pill)

##### 3.3 ContentPanel.java
พื้นที่แสดงเนื้อหาของแต่ละหน้า:
- สร้าง UI Components ตามข้อมูลจาก Native
- เก็บ State ของแต่ละ Control (ค่าที่ผู้ใช้ตั้งไว้)
- ส่ง Callback กลับไป Native เมื่อมีการเปลี่ยนแปลง

##### 3.4 UI Components ย่อย
- **ModernDropdown**: Dropdown แบบกำหนดเอง รองรับ PopupWindow
- **ModernSeekBar**: Slider ปรับค่าได้ พร้อม Label แสดงค่า
- **SwitchRow**: Toggle Switch สไตล์ iOS

#### 4. Native Core (`cpp/core/`)

**ไฟล์หลัก: `palladium.cpp`**

จุดเริ่มต้นของ Native Code:
- `__attribute__((constructor))` - รันอัตโนมัติเมื่อ library ถูกโหลด
- สร้าง Thread แยกสำหรับทำงานใน background
- รอให้ Target Library (เช่น libil2cpp.so) โหลดก่อน
- ทำ Memory Patching และ Hooking

```cpp
// โครงสร้างข้อมูล Config
struct GameConfig {
    bool Hackmap = false;
    int AimbotRange = 50;
    // ... ตัวแปรอื่นๆ
} config;

// JNI Implementation
extern "C" {
    JNIEXPORT jobjectArray JNICALL
    Java_zig_cheat_qq_native_1bridge_StealthJNI_getFeatures(JNIEnv *env, jclass clazz) {
        // ส่งรายการเมนูกลับไป Java
    }
    
    JNIEXPORT void JNICALL
    Java_zig_cheat_qq_native_1bridge_StealthJNI_Callback(...) {
        // รับค่าจาก UI และ update config
    }
}

// Constructor - รันอัตโนมัติ
__attribute__((constructor))
void init() {
    std::thread(main_thread).detach();
}
```

#### 5. Third-Party Libraries (`cpp/include/`)

##### KittyMemory
ไลบรารีสำหรับจัดการ Memory ใน Linux/Android:
- `ElfScanner` - ค้นหา Base Address ของ shared library
- `MemoryPatch` - แก้ไข bytes ใน memory
- `KittyMemory::read/write` - อ่าน/เขียนข้อมูล

##### Dobby
Hooking Framework ที่มีประสิทธิภาพสูง:
- `DobbyHook()` - Hook function เป้าหมาย
- `DobbyDestroy()` - ยกเลิก Hook
- รองรับ ARM, ARM64, x86, x86_64

##### oxorany
String Obfuscation:
- XOR encode strings ตอน compile time
- Decode ตอน runtime
- ป้องกันการค้นหา string สำคัญใน binary

---

## 🔧 การติดตั้งและเตรียม Environment

### ความต้องการของระบบ

#### สำหรับ Development Machine

| รายการ | เวอร์ชันขั้นต่ำ | แนะนำ |
|---------|---------------|--------|
| **Operating System** | Windows 10 / macOS 10.15 / Linux | Windows 11 / macOS 14 / Ubuntu 22.04 |
| **RAM** | 8 GB | 16 GB+ |
| **Disk Space** | 10 GB | 50 GB+ (รวม Emulator) |
| **CPU** | 4 Cores | 8 Cores+ |

#### สำหรับ Target Device

| รายการ | ข้อกำหนด |
|---------|----------|
| **Android Version** | Android 9.0 (API 28) ขึ้นไป |
| **Architecture** | ARM64 (arm64-v8a) |
| **RAM** | 4 GB+ |
| **Root** | ไม่จำเป็น (สำหรับ APK Modding) |

### ขั้นตอนการติดตั้งโดยละเอียด

#### ขั้นตอนที่ 1: ติดตั้ง Android Studio

1. ดาวน์โหลด Android Studio จาก [developer.android.com/studio](https://developer.android.com/studio)
2. ติดตั้งตาม Wizard โดยเลือก:
   - ☑️ Android SDK
   - ☑️ Android SDK Platform
   - ☑️ Android Virtual Device (ถ้าต้องการใช้ Emulator)
   - ☑️ Android NDK
   - ☑️ CMake

#### ขั้นตอนที่ 2: ตั้งค่า Environment Variables

**Windows:**
```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Users\YourName\AppData\Local\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("ANDROID_NDK", "C:\Users\YourName\AppData\Local\Android\Sdk\ndk\26.2.11394342", "User")
```

**macOS/Linux:**
```bash
echo 'export ANDROID_HOME=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export ANDROID_NDK=$ANDROID_HOME/ndk/26.2.11394342' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools' >> ~/.bashrc
source ~/.bashrc
```

#### ขั้นตอนที่ 3: ติดตั้ง NDK และ CMake

เปิด Android Studio → SDK Manager → SDK Tools:
- ☑️ NDK (Side by side) → เลือกเวอร์ชัน 26.2.11394342
- ☑️ CMake → เลือก 3.22.1 หรือสูงกว่า
- คลิก Apply และรอติดตั้ง

#### ขั้นตอนที่ 4: Clone และตั้งค่าโปรเจค

```bash
# Clone repository
git clone https://github.com/yourusername/PALLADIUM.git
cd PALLADIUM

# สร้างไฟล์ local.properties (ถ้าไม่มี)
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
echo "ndk.dir=/path/to/Android/Sdk/ndk/26.2.11394342" >> local.properties

# ให้สิทธิ์ execute สำหรับ gradlew
chmod +x gradlew

# ทดสอบ build
./gradlew build
```

---

## 🏗️ การ Build โปรเจค

### Build Configurations

โปรเจคนี้มี 3 โหมดการ Build:

#### 1. Debug Build

เหมาะสำหรับการพัฒนาและทดสอบ มีข้อมูล Debug ครบถ้วน

```bash
./gradlew assembleDebug
```

**ผลลัพธ์:**
- ไฟล์: `app/build/outputs/apk/debug/app-debug.apk`
- Debug Symbols: มีครบ
- Optimization: ปิด
- ProGuard: ปิด

#### 2. Release Build

สำหรับการใช้งานจริง มีการปรับแต่งและปกปิดข้อมูล

```bash
./gradlew assembleRelease
```

**ผลลัพธ์:**
- ไฟล์: `app/build/outputs/apk/release/app-release.apk`
- Minification: เปิด (ลบโค้ดที่ไม่ใช้)
- Obfuscation: เปิด (ProGuard/R8)
- Native Strip: เปิด (ลบ symbols จาก .so)

**การตั้งค่า ProGuard:**
```proguard
# เก็บคลาสสำคัญไม่ให้ถูกยุบ
-keep public class zig.cheat.qq.native_bridge.** { *; }
-keep public class zig.cheat.qq.ui.** { *; }

# เก็บ Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
```

#### 3. Custom Build (Library Only)

หากต้องการ build แค่ Native Library (`.so`) ไม่ต้องการ APK:

```bash
./gradlew :app:externalNativeBuildDebug
```

Library จะอยู่ที่: `app/build/intermediates/cmake/debug/obj/arm64-v8a/liblib-name.so`

### การแก้ไข Build Configuration

#### เปลี่ยน Package Name

แก้ไขที่ `app/build.gradle`:

```gradle
android {
    defaultConfig {
        applicationId 'com.yourcompany.yourmod'
        minSdk 28
        targetSdk 36
        versionCode 1
        versionName "1.0.0"
    }
}
```

อย่าลืมแก้ไข package name ในไฟล์ Java ทั้งหมดด้วย!

#### เปลี่ยนชื่อ Native Library

1. แก้ไข XOR array ใน `StealthJNI.java`:
```java
// ตัวอย่าง: แปลง "my-lib" เป็น XOR
// 'm' ^ 0x49 = 0x2D = 45
// 'y' ^ 0x49 = 0x30 = 48
// ...
System.loadLibrary(o(new int[]{45, 48, ...}));
```

2. แก้ไข `CMakeLists.txt`:
```cmake
add_library(my-lib SHARED ${SOURCES})
```

#### เพิ่ม Architecture อื่น (ไม่แนะนำ)

```gradle
ndk {
    abiFilters "arm64-v8a", "armeabi-v7a"
}
```

> ⚠️ **คำเตือน**: การเพิ่ม armeabi-v7a จะทำให้ APK ใหญ่ขึ้นและเพิ่มความซับซ้อน แนะนำให้ใช้ arm64-v8a เท่านั้น

---

## 💉 เทคนิคการ Inject

การ Inject คือกระบวนการนำโค้ดของเราเข้าไปรันใน Process ของแอปพลิเคชันเป้าหมาย มีหลายวิธีที่ใช้ได้:

### วิธีที่ 1: APK Modding (แนะนำสำหรับมือใหม่)

วิธีนี้เหมาะสำหรับเกมที่ไม่มีการตรวจสอบ integrity ของ APK

#### ขั้นตอนที่ 1: แตกไฟล์ APK

```bash
# ใช้ apktool
apktool d target-game.apk -o game-folder

# หรือเปลี่ยนนามสกุลเป็น .zip แล้วแตก
mv target-game.apk target-game.zip
unzip target-game.zip -d game-folder
```

#### ขั้นตอนที่ 2: นำไฟล์ PALLADIUM ใส่เข้าไป

```bash
# 1. คัดลอก Smali classes
cp -r PALLADIUM/app/smali/zig game-folder/smali/

# 2. คัดลอก Native Libraries
cp PALLADIUM/app/build/intermediates/cmake/release/obj/arm64-v8a/liblib-name.so game-folder/lib/arm64-v8a/

# 3. คัดลอก Assets (ถ้ามี)
cp -r PALLADIUM/app/src/main/assets/* game-folder/assets/
```

#### ขั้นตอนที่ 3: แก้ไข AndroidManifest.xml

เพิ่ม Permissions:
```xml
<manifest>
    <!-- Permissions ที่จำเป็น -->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    
    <!-- ... โค้ดเดิม ... -->
</manifest>
```

#### ขั้นตอนที่ 4: แก้ไข MainActivity

หาไฟล์ MainActivity ของเกม (มักอยู่ใน `smali/com/game/package/MainActivity.smali`)

เพิ่มโค้ดต่อไปนี้ใน method `onCreate`:

```smali
# หลังจาก invoke-super และ setContentView
# แทรกโค้ดนี้เข้าไป:

# โหลด parameter p0 (this) ลง stack
# เรียก method เริ่มต้นของ PALLADIUM
invoke-static {p0}, Lzig/cheat/qq/ϟ;->ϟ(Landroid/content/Context;)V
```

ตัวอย่างผลลัพธ์:
```smali
.method protected onCreate(Landroid/os/Bundle;)V
    .locals 2
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .prologue
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    # ==== PALLADIUM INJECTION ====
    invoke-static {p0}, Lzig/cheat/qq/ϟ;->ϟ(Landroid/content/Context;)V
    # =============================

    const v1, 0x7f030004
    invoke-virtual {p0, v1}, Lcom/game/MainActivity;->setContentView(I)V
    
    # ... โค้ดต่อไป ...
.end method
```

#### ขั้นตอนที่ 5: แพ็คและเซ็น APK

```bash
# แพ็คกลับเป็น APK
apktool b game-folder -o modded-game.apk

# สร้าง Keystore (ถ้ายังไม่มี)
keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000

# เซ็น APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.keystore modded-game.apk alias_name

# จัด ZIP Align
zipalign -v 4 modded-game.apk modded-game-aligned.apk
```

### วิธีที่ 2: Dynamic Injection (สำหรับผู้มีประสบการณ์)

ใช้เครื่องมือ external ในการ inject library เข้าไปใน process ที่กำลังรันอยู่

#### ใช้ Frida

```javascript
// palladium_loader.js
var moduleName = "liblib-name.so";
var libraryPath = "/data/local/tmp/" + moduleName;

// Push library ไปยัง device ก่อน
// adb push app/build/intermediates/cmake/release/obj/arm64-v8a/liblib-name.so /data/local/tmp/

Interceptor.attach(Module.findExportByName(null, "android_dlopen_ext"), {
    onEnter: function(args) {
        this.path = Memory.readCString(args[0]);
        console.log("Loading:", this.path);
    },
    onLeave: function(retval) {
        if (this.path && this.path.includes("libil2cpp.so")) {
            // il2cpp โหลดแล้ว ให้โหลด PALLADIUM ตาม
            console.log("Target library loaded, injecting PALLADIUM...");
            
            var loadLibrary = new NativeFunction(
                Module.findExportByName(null, "android_dlopen_ext"),
                'pointer', ['pointer', 'int', 'pointer']
            );
            
            var path = Memory.allocUtf8String(libraryPath);
            loadLibrary(path, 0x2, ptr(0)); // RTLD_NOW
        }
    }
});
```

รัน:
```bash
frida -U -f com.game.package -l palladium_loader.js --no-pause
```

#### ใช้ Xposed Framework

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
                    Context context = (Context) param.thisObject;
                    // โหลด PALLADIUM
                    System.loadLibrary("lib-name");
                    // เรียก entry point
                    zig.cheat.qq.ϟ.ϟ(context);
                }
            }
        );
    }
}
```

### วิธีที่ 3: Magisk Module (สำหรับเครื่อง Root)

สร้าง Magisk Module ที่จะ inject library เข้าไปใน target app โดยอัตโนมัติ

**ไฟล์: customize.sh**
```bash
#!/system/bin/sh

# คัดลอก library ไปยัง system
mkdir -p $MODDIR/system/lib64
cp $MODDIR/liblib-name.so $MODDIR/system/lib64/

# สร้าง prop สำหรับเปิดใช้งาน
mkdir -p $MODDIR/data/adb/service.d
cat > $MODDIR/data/adb/service.d/palladium_inject.sh << 'EOF'
#!/system/bin/sh
# รอให้ boot เสร็จ
sleep 30

# Inject เข้าไปใน target process
am start -n com.game.package/.MainActivity
sleep 5

# ใช้ ptrace หรือ similar technique
# (ต้อง implement เองตามความเหมาะสม)
EOF

chmod +x $MODDIR/data/adb/service.d/palladium_inject.sh
```

---

## ✏️ การแก้ไขและปรับแต่งโค้ด

### การเพิ่มเมนูใหม่

#### ขั้นตอนที่ 1: เพิ่มใน Native (C++)

แก้ไข `app/src/main/cpp/core/palladium.cpp`:

```cpp
// 1. เพิ่มตัวแปรใน struct
struct GameConfig {
    bool Hackmap = false;
    int AimbotRange = 50;
    float SpeedMultiplier = 1.0f;
    // เพิ่มใหม่:
    bool InfiniteAmmo = false;
    int GodModeLevel = 0;
    char CustomName[32] = "Player";
} config;

// 2. เพิ่มใน getFeatures
const char *features[] = {
    oxorany("PAGE|0|icons/main.png|Main"),
    oxorany("CHECK|0|Hack Map|1"),
    oxorany("SLIDER|0|Aimbot Range|2|0|100"),
    
    // เพิ่มใหม่:
    oxorany("CHECK|0|Infinite Ammo|10"),
    oxorany("SLIDER|0|God Mode Level|11|0|5"),
    oxorany("INPUT|0|Custom Name|12"),
};

// 3. เพิ่มใน Callback
JNIEXPORT void JNICALL
Java_zig_cheat_qq_native_1bridge_StealthJNI_Callback(...) {
    switch (id) {
        case 1: config.Hackmap = (bool)check; break;
        case 2: config.AimbotRange = value; break;
        
        // เพิ่มใหม่:
        case 10: 
            config.InfiniteAmmo = (bool)check;
            if (check) {
                // ทำ patch ที่ address ที่เกี่ยวข้อง
                applyInfiniteAmmoPatch();
            } else {
                restoreAmmoPatch();
            }
            break;
            
        case 11:
            config.GodModeLevel = value;
            updateGodMode(value);
            break;
            
        case 12:
            // value3 คือ string input
            const char* name = env->GetStringUTFChars(value3, nullptr);
            strncpy(config.CustomName, name, sizeof(config.CustomName) - 1);
            env->ReleaseStringUTFChars(value3, name);
            break;
    }
}
```

#### ขั้นตอนที่ 2: อธิบายรูปแบบ Feature String

รูปแบบ: `TYPE|PAGE|LABEL|ID|EXTRA...`

| Type | Format | ตัวอย่าง | คำอธิบาย |
|------|--------|---------|----------|
| PAGE | `PAGE|PAGE_ID|ICON_PATH|LABEL` | `PAGE|0|icons/func.png|Main` | สร้างหน้าใหม่ |
| CHECK | `CHECK|PAGE_ID|LABEL|ID` | `CHECK|0|Hack Map|1` | Toggle Switch |
| SLIDER | `SLIDER|PAGE_ID|LABEL|ID|MIN|MAX` | `SLIDER|0|Speed|2|0|500` | แถบเลื่อน |
| DROPDOWN | `DROPDOWN|PAGE_ID|LABEL|ID|OPT1,OPT2,OPT3` | `DROPDOWN|0|Weapon|3|AK,M4,AWP` | เลือกจากรายการ |
| INPUT | `INPUT|PAGE_ID|LABEL|ID` | `INPUT|0|Name|4` | กล่องข้อความ |
| BUTTON | `BUTTON|PAGE_ID|LABEL|ID` | `BUTTON|0|Apply|5` | ปุ่มกด |

#### ขั้นตอนที่ 3: แก้ไข UI (ถ้าจำเป็น)

หากต้องการ UI ที่ซับซ้อนกว่าที่มีอยู่ สามารถแก้ไขที่:

**`ContentPanel.java`** - เพิ่ม method สร้าง component ใหม่:

```java
private void addCustomComponent(String label, final int id) {
    // สร้าง layout ของคุณเอง
    LinearLayout customLayout = new LinearLayout(context);
    customLayout.setOrientation(LinearLayout.HORIZONTAL);
    
    // เพิ่ม views ต่างๆ
    TextView tv = new TextView(context);
    tv.setText(label);
    
    Button btn = new Button(context);
    btn.setText("Action");
    btn.setOnClickListener(v -> {
        // ส่ง callback
        StealthJNI.Callback(id, true, 0, 0, "button_clicked");
    });
    
    customLayout.addView(tv);
    customLayout.addView(btn);
    contentLayout.addView(customLayout);
}
```

### การแก้ไขธีมและสี

แก้ไขที่ `ThemeConstants.java`:

```java
public class ThemeConstants {
    // สีหลัก
    public static final int COLOR_ACCENT_BLUE = 0xFF3B82F6;  // น้ำเงิน
    public static final int COLOR_ACCENT_PURPLE = 0xFF8B5CF6; // ม่วง
    
    // สีพื้นหลัง
    public static final int COLOR_BG_DARK = 0xFF0F0F0F;
    public static final int COLOR_BG_CARD = 0x1A1A1A;
    
    // สีข้อความ
    public static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int COLOR_TEXT_SECONDARY = 0xB3FFFFFF;
    
    // ขนาดตัวอักษร
    public static final float TEXT_SIZE_TITLE = 16f;
    public static final float TEXT_SIZE_BODY = 13f;
    
    // มิติ
    public static final int PADDING_DEFAULT = 16;
    public static final int CORNER_RADIUS = 24;
    
    // Animation Durations
    public static final long ANIM_DURATION_SHORT = 150L;
    public static final long ANIM_DURATION_MEDIUM = 250L;
    public static final long ANIM_DURATION_LONG = 350L;
}
```

### การแก้ไข Animation

แก้ไขที่ `AnimUtils.java`:

```java
public class AnimUtils {
    // Interpolator แบบต่างๆ
    public static final TimeInterpolator EASE_IN_OUT = 
        new AccelerateDecelerateInterpolator();
    
    public static final TimeInterpolator BOUNCE = 
        new BounceInterpolator();
    
    public static final TimeInterpolator ELASTIC = 
        new OvershootInterpolator(1.5f);
    
    // สร้าง animation แบบกำหนดเอง
    public static ValueAnimator createPulseAnimation(View target) {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 1.1f, 1f);
        animator.setDuration(600);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            target.setScaleX(scale);
            target.setScaleY(scale);
        });
        return animator;
    }
}
```

---

## ⚡ การปรับแต่งประสิทธิภาพ

### การลดขนาด APK

#### 1. Enable ProGuard/R8

```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 
                         'proguard-rules.pro'
        }
    }
}
```

#### 2. Split APK ตาม Architecture

```gradle
android {
    splits {
        abi {
            enable true
            reset()
            include 'arm64-v8a'
            universalApk false
        }
    }
}
```

#### 3. Compress Assets

- แปลงรูป PNG → WebP (สูญเสียคุณภาพน้อยกว่า)
- ใช้ Vector Drawable แทนรูป Bitmap
- ลบ Font ที่ไม่ใช้ออก

### การจัดการ Memory

#### Java Layer

```java
// ใช้ WeakReference สำหรับ Handler (ป้องกัน Memory Leak)
private static class WeakHandler extends Handler {
    private final WeakReference<FloatingMenu> menuRef;
    
    WeakHandler(FloatingMenu menu) {
        super(Looper.getMainLooper());
        this.menuRef = new WeakReference<>(menu);
    }
}

// ล้าง Bitmap ที่ไม่ใช้
@Override
protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (bitmap != null && !bitmap.isRecycled()) {
        bitmap.recycle();
        bitmap = null;
    }
}

// ใช้ SparseArray แทน HashMap สำหรับ primitive keys
SparseArray<Boolean> states = new SparseArray<>(); // ดีกว่า HashMap<Integer, Boolean>
```

#### Native Layer

```cpp
// ใช้ Smart Pointers
std::unique_ptr<MemoryPatch> patch = std::make_unique<MemoryPatch>(...);

// ล้าง Local References ใน JNI
JNIEXPORT jobjectArray JNICALL
Java_getFeatures(JNIEnv *env, jclass clazz) {
    jobjectArray result = env->NewObjectArray(size, stringClass, nullptr);
    // ... ทำงาน ...
    env->DeleteLocalRef(stringClass); // ล้างเมื่อเสร็จ
    return result;
}

// ใช้ Thread Pool แทนการสร้าง Thread ใหม่ทุกครั้ง
std::thread::hardware_concurrency(); // หาจำนวน Cores
```

### การปรับแต่ง Native Library

```cmake
# CMakeLists.txt - Optimization Flags
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -DNDEBUG")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -fvisibility=hidden")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -ffunction-sections -fdata-sections")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Wl,--gc-sections") # Remove unused sections

# Strip symbols ออกจาก .so
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")
```

---

## 🔍 Offset และ Patching

### ความเข้าใจพื้นฐาน

**Offset** คือตำแหน่งที่เทียบกับจุดเริ่มต้นของส่วนหนึ่งๆ ใน Memory

```
Library Base Address: 0x7600000000
Target Function Offset: 0x123456
Absolute Address: 0x7600123456
```

### การหา Offset ด้วย IDA Pro

#### ขั้นตอนที่ 1: ดึง Library จาก Device

```bash
# หา path ของ library
adb shell "cat /proc/$(pidof com.game.package)/maps | grep libil2cpp"

# ดึงออกมา
adb pull /data/app/com.game.package/lib/arm64/libil2cpp.so
```

#### ขั้นตอนที่ 2: เปิดใน IDA Pro

1. File → Open → เลือก libil2cpp.so
2. รอให้ Analyze เสร็จ
3. ใช้ Shift+F12 เปิด String Table
4. ค้นหา string ที่เกี่ยวข้องกับฟีเจอร์ที่ต้องการ
5. ดับเบิลคลิกที่ string → ดู cross-reference
6. กด F5 เพื่อดู pseudocode
7. คัดลอก Address ที่เห็น (ลบ Image Base ออก)

#### ขั้นตอนที่ 3: คำนวณ Offset

```cpp
// ถ้า IDA แสดง Address เป็น 0x76123456
// และ Image Base คือ 0x76000000
// Offset = 0x76123456 - 0x76000000 = 0x123456

uintptr_t base = ElfScanner::findElf("libil2cpp.so").base();
uintptr_t target = base + 0x123456;
```

### การเขียน Patch

#### Patch แบบ NOP (ลบ instruction)

```cpp
// ARM64 NOP = 0xD503201F
// ใช้ KittyMemory
MemoryPatch::createWithHex(targetAddress, "D503201F").Modify();
```

#### Patch แบบเปลี่ยนค่า

```cpp
// เปลี่ยนค่า float ที่ address
float newDamage = 9999.0f;
KittyMemory::writeMemory(targetAddress, &newDamage, sizeof(float));

// เปลี่ยนค่า int
int newHealth = 9999;
KittyMemory::writeMemory(healthAddress, &newHealth, sizeof(int));

// เปลี่ยน String (ต้องระวังขนาด)
const char* newName = "HACKER";
KittyMemory::writeMemory(nameAddress, newName, strlen(newName) + 1);
```

#### Patch แบบ Redirect (Branch)

```cpp
// คำนวณ offset สำหรับ branch
// B instruction: 26-bit signed offset (shifted left by 2)
int32_t offset = (hookAddress - targetAddress) / 4;
int32_t branchInstr = 0x14000000 | (offset & 0x3FFFFFF);

KittyMemory::writeMemory(targetAddress, &branchInstr, sizeof(int32_t));
```

### การตรวจสอบว่า Patch สำเร็จ

```cpp
bool verifyPatch(uintptr_t address, const std::vector<uint8_t>& expectedBytes) {
    std::vector<uint8_t> current(expectedBytes.size());
    KittyMemory::readMemory(address, current.data(), current.size());
    return current == expectedBytes;
}

// ใช้งาน
if (verifyPatch(targetAddress, {0x1F, 0x20, 0x03, 0xD5})) {
    LOG("Patch applied successfully!");
} else {
    LOG("Patch failed!");
}
```

---

## 🛠️ การใช้งาน KittyMemory และ Dobby

### KittyMemory - รายละเอียดทั้งหมด

#### 1. ElfScanner

```cpp
// หา library ตามชื่อ
ElfScanner elf = ElfScanner::findElf("libil2cpp.so");

if (elf.isValid()) {
    uintptr_t base = elf.base();      // Base address
    size_t size = elf.size();          // Size of library
    std::string path = elf.path();     // Full path
    
    // หา section ต่างๆ
    uintptr_t textSection = elf.findSection(".text");
    uintptr_t bssSection = elf.findSection(".bss");
}

// รอจนกว่าจะเจอ
ElfScanner elf;
while(!(elf = ElfScanner::findElf("libil2cpp.so")).isValid()) {
    std::this_thread::sleep_for(100ms);
}
```

#### 2. MemoryPatch

```cpp
// วิธีสร้าง Patch

// จาก Hex String
auto patch1 = MemoryPatch::createWithHex(targetAddress, "00 01 02 03");

// จาก Bytes Array
std::vector<uint8_t> bytes = {0x00, 0x01, 0x02, 0x03};
auto patch2 = MemoryPatch::createWithBytes(targetAddress, bytes);

// จาก Pattern
auto patch3 = MemoryPatch::createWithPattern(base, size, "12 34 ?? 56", "xx?x", "00 00 00 00");

// จัดการ Patch
patch1.Modify();     // เปิดใช้งาน
patch1.Restore();    // ย้อนกลับ
patch1.get_TargetAddress();  // ดู address
```

#### 3. Memory Read/Write

```cpp
// อ่านหลายแบบ
int32_t intValue;
float floatValue;
double doubleValue;
uintptr_t pointerValue;

KittyMemory::readMemory(address, &intValue, sizeof(int32_t));
KittyMemory::readMemory(address, &floatValue, sizeof(float));

// เขียนหลายแบบ
KittyMemory::writeMemory(address, &newValue, sizeof(newValue));

// อ่าน String
size_t len = KittyMemory::readMemoryStr(address, buffer, bufferSize);

// เขียน String
KittyMemory::writeMemoryStr(address, "Hello", 5);
```

#### 4. Memory Protection

```cpp
// สถานะที่เป็นไปได้
// KPROT_NO_ACCESS  = ไม่มีสิทธิ์
// KPROT_READ       = อ่านได้
// KPROT_WRITE      = เขียนได้
// KPROT_EXEC       = Execute ได้
// KPROT_READ_WRITE = อ่าน+เขียน
// KPROT_RWX        = อ่าน+เขียน+Execute

// เปลี่ยนสิทธิ์
KittyMemory::setAddressProtection(address, size, KPROT_READ_WRITE_EXEC);

// ตรวจสอบสิทธิ์ปัจจุบัน
int currentProt = KittyMemory::getMemoryProtection(address);
```

### Dobby - รายละเอียดทั้งหมด

#### 1. Inline Hook

```cpp
// ประกาศ function ต้นฉบับ
typedef void (*OriginalFunction)(int arg1, float arg2);
OriginalFunction origFunc = nullptr;

// Hook function
void myHook(int arg1, float arg2) {
    LOG("Hook called with: %d, %f", arg1, arg2);
    
    // แก้ไขพารามิเตอร์ก่อนส่งต่อ
    arg1 = 999;
    
    // เรียกต้นฉบับ
    origFunc(arg1, arg2);
    
    // ทำอะไรหลังเรียกต้นฉบับ
    LOG("Original completed");
}

// ติดตั้ง Hook
void installHook(uintptr_t targetAddress) {
    DobbyHook(
        reinterpret_cast<void*>(targetAddress),
        reinterpret_cast<void*>(myHook),
        reinterpret_cast<void**>(&origFunc)
    );
}

// ลบ Hook
void removeHook(uintptr_t targetAddress) {
    DobbyDestroy(reinterpret_cast<void*>(targetAddress));
}
```

#### 2. Register Hook

```cpp
// ตรวจจับเมื่อมี library ใหม่ถูกโหลด
void onImageLoad(const char* imageName, void* imageBase) {
    LOG("Library loaded: %s at %p", imageName, imageBase);
    
    if (strstr(imageName, "libtarget.so")) {
        // Target library โหลดแล้ว
        uintptr_t base = reinterpret_cast<uintptr_t>(imageBase);
        
        // ทำการ Hook
        DobbyHook(
            reinterpret_cast<void*>(base + 0x123456),
            reinterpret_cast<void*>(myTargetHook),
            reinterpret_cast<void**>(&origTarget)
        );
    }
}

// ลงทะเบียน
dobby_register_image_load_callback(onImageLoad);
```

#### 3. Symbol Resolver

```cpp
// หา address ของ symbol จากชื่อ
void* symbolAddr = DobbySymbolResolver(
    "libil2cpp.so",           // Image name (nullptr = main executable)
    "il2cpp_string_new"       // Symbol name
);

if (symbolAddr) {
    LOG("Found symbol at: %p", symbolAddr);
}
```

---

## 🚀 การพัฒนาต่อในอนาคต

### 🗓️ Roadmap 2025

<div align="center">

| Phase | ไตรมาส | ฟีเจอร์ | สถานะ |
|-------|--------|---------|--------|
| **1** | Q1 | Core Stability | ✅ เสร็จสิ้น |
| **2** | Q2 | Feature Expansion | 🔨 กำลังพัฒนา |
| **3** | Q3 | Game Support | 📋 วางแผน |
| **4** | Q4 | Security Hardening | 📋 วางแผน |

</div>

### Phase 1: Core Stability ✅ (มกราคม - มีนาคม 2025)

ฟีเจอร์พื้นฐานที่จำเป็นทั้งหมดเสร็จสมบูรณ์แล้ว:

- [x] **Dynamic Island UI** - ระบบเมนูหลักที่สามารถย่อ/ขยายได้
- [x] **Anti-Detection Foundation** - ระบบป้องกันการตรวจจับพื้นฐาน
- [x] **Memory Patching System** - การแก้ไข memory แบบ real-time
- [x] **JNI Bridge** - การเชื่อมต่อระหว่าง Java และ Native
- [x] **Thread Safety** - จัดการ thread อย่างปลอดภัย

### Phase 2: Feature Expansion 🔨 (เมษายน - มิถุนายน 2025)

กำลังพัฒนาฟีเจอร์ใหม่ที่จะทำให้การใช้งานง่ายขึ้น:

#### 2.1 Script Engine 📝
```lua
-- ตัวอย่าง Lua Script ที่จะรองรับ
function onPlayerSpawn(player)
    player.health = 999
    player.speed = 2.0
end

function onUpdate()
    if isKeyPressed(KEY_F1) then
        toggleFeature("aimbot")
    end
end
```

รองรับ:
- **Lua 5.4** - สำหรับ scripting ง่ายๆ
- **JavaScript V8** - สำหรับผู้ที่ถนัด JS
- **Python (MicroPython)** - สำหรับ AI/ML integration

#### 2.2 ESP System 👁️
- วาดกรอบรอบศัตรู (Box ESP)
- แสดงเส้นไปยังศัตรู (Line ESP)
- แสดง HP/ระยะห่าง (Info ESP)
- Skeleton ESP สำหรับเกม 3D
- Radar Mini-map

#### 2.3 Config System 💾
```json
{
  "profile": "default",
  "features": {
    "aimbot": {
      "enabled": true,
      "fov": 90,
      "smooth": 0.5
    },
    "esp": {
      "enabled": false,
      "color": "#FF0000"
    }
  },
  "hotkeys": {
    "toggle_menu": "INSERT",
    "panic": "DELETE"
  }
}
```

#### 2.4 Theme Store 🎨
- ธีมสำเร็จรูป 10+ แบบ
- Theme Editor แบบ Visual
- แชร์ธีมกับชุมชน
- Dynamic Theme (เปลี่ยนตามเวลา)

### Phase 3: Game Support 🎮 (กรกฎาคม - กันยายน 2025)

#### 3.1 Unity IL2CPP Dumper 🔧
```cpp
// รวมเครื่องมือ dump ไว้ในแอพ
Il2CppDumper dumper;
dumper.dump("libil2cpp.so");
auto classes = dumper.getClasses();

// ค้นหา class และ method อัตโนมัติ
auto playerClass = dumper.findClass("PlayerController");
auto updateMethod = playerClass->findMethod("Update");
```

#### 3.2 Unreal Engine Support 🏗️
- รองรับ UE4 และ UE5
- UObject iteration
- FName  decryption
- GWorld pattern scanning

#### 3.3 Auto-Offset Finder 🤖
```cpp
// ระบบหา offset อัตโนมัติจาก signature
SignatureFinder finder;
finder.addPattern("PlayerHealth", "48 8B ?? ?? ?? ?? ?? 8B ?? ?? ?? ?? ?? 89 ?? ??");
finder.addPattern("PlayerSpeed", "F3 0F ?? ?? ?? ?? ?? ?? F3 0F ?? ?? ?? ?? ?? ??");

auto results = finder.scan();
// บันทึกผลลง config file
```

#### 3.4 Network Modding 🌐
- Packet sniffing และ modification
- WebSocket proxy
- HTTPS MITM (สำหรับ testing)

### Phase 4: Security Hardening 🔒 (ตุลาคม - ธันวาคม 2025)

#### 4.1 Virtualization 🛡️
- ซ่อน hook ด้วย VM-based obfuscation
- Code virtualization สำหรับส่วนสำคัญ
- Anti-tampering

#### 4.2 Integrity Check Bypass 🔓
- Bypass CRC checks
- Bypass signature verification
- Bypass library hash checks

#### 4.3 Anti-Emulator 📱
- ตรวจจับ emulator ที่แอบแฝง
- Bypass emulator detection ของเกม
- Support real device only mode

#### 4.4 Root Hiding 🌳
- Hide Magisk/KernelSU
- Hide superuser binaries
- Hide su paths

### 💡 Ideas ที่กำลังพิจารณา

<div align="center">

| ไอเดีย | ความเป็นไปได้ | ความยาก | ประโยชน์ |
|--------|--------------|---------|----------|
| 🖼️ Screenshot Protection | สูง | ปานกลาง | ป้องกันการถูก report |
| 🎵 Audio Visualization | สูง | ง่าย | ตกแต่งเมนู |
| 🤖 AI Aimbot | ปานกลาง | ยาก | ประสิทธิภาพสูง |
| 📊 Performance Monitor | สูง | ง่าย | Debug และ optimize |
| 🌐 Multi-Language | สูง | ง่าย | รองรับผู้ใช้ทั่วโลก |
| ☁️ Cloud Config | ปานกลาง | ปานกลาง | Sync การตั้งค่า |
| 🔄 Auto-Update | สูง | ปานกลาง | อัปเดต offset อัตโนมัติ |
| 📹 Replay System | ต่ำ | ยากมาก | บันทึก/เล่นซ้ำ |

</div>

### การมีส่วนร่วม

หากคุณมีไอเดียอยากเพิ่ม:
1. เปิด **GitHub Issue** พร้อมอธิบายรายละเอียด
2. ใช้ Label `feature-request`
3. รอทีมงานพิจารณา

หรือหากต้องการ contribute โค้ด:
1. Fork repository
2. สร้าง branch ใหม่ (`feature/your-feature`)
3. Commit การเปลี่ยนแปลง
4. ส่ง Pull Request

---

## 🐛 การแก้ไขปัญหา

### ปัญหาทั่วไปและวิธีแก้ไข

#### ❌ Build Failed: CMake Error

**อาการ:**
```
CMake Error: CMake was unable to find a build program corresponding to "Ninja"
```

**สาเหตุ:** CMake หรือ Ninja ไม่ถูกต้อง

**แก้ไข:**
```bash
# Windows
setx PATH "%PATH%;C:\Users\YourName\AppData\Local\Android\Sdk\cmake\3.22.1\bin"

# หรือใน Android Studio
# Tools > SDK Manager > SDK Tools > ติดตั้ง CMake
```

#### ❌ Build Failed: NDK Version Mismatch

**อาการ:**
```
No version of NDK matched the requested version 26.2.11394342
```

**แก้ไข:**
```gradle
// ใน build.gradle ใช้ ndkVersion ที่มีอยู่
android {
    ndkVersion "26.1.10909125"  // หรือเวอร์ชันที่มี
}
```

#### ❌ App Crash: Library Not Found

**อาการ:**
```
java.lang.UnsatisfiedLinkError: dalvik.system.PathClassLoader[...] couldn't find "liblib-name.so"
```

**สาเหตุ:** 
1. Library ไม่ได้ build สำหรับ architecture นี้
2. Library ไม่ได้อยู่ใน APK

**แก้ไข:**
```bash
# ตรวจสอบว่ามี .so ใน APK หรือไม่
unzip -l app-debug.apk | grep liblib-name.so

# ถ้าไม่มี ตรวจสอบ abiFilters
// ใน build.gradle
ndk {
    abiFilters "arm64-v8a"  // ตรงกับ device หรือไม่
}
```

#### ❌ App Crash: JNI Method Not Found

**อาการ:**
```
java.lang.UnsatisfiedLinkError: No implementation found for void 
zig.cheat.qq.native_bridge.StealthJNI.getFeatures()
```

**สาเหตุ:** ชื่อ method ใน Java และ C++ ไม่ตรงกัน

**แก้ไข:**
```cpp
// ตรวจสอบว่าใช้ extern "C" และชื่อตรงกัน
extern "C" {
    JNIEXPORT jobjectArray JNICALL
    Java_zig_cheat_qq_native_1bridge_StealthJNI_getFeatures(JNIEnv *env, jclass clazz) {
        // ต้องตรงกับ Java:
        // public static native String[] getFeatures();
    }
}
```

#### ❌ Menu Not Showing

**อาการ:** แอปรันได้แต่ไม่เห็นเมนู

**สาเหตุ:**
1. ไม่มี Permission SYSTEM_ALERT_WINDOW
2. ใช้ Context ผิดประเภท

**แก้ไข:**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

```java
// ต้องใช้ Activity Context
// ผิด:
FloatingMenu menu = new FloatingMenu(getApplicationContext());

// ถูก:
FloatingMenu menu = new FloatingMenu(activity);
```

#### ❌ Offset Not Working

**อาการ:** Patch ไม่มีผล

**สาเหตุ:**
1. Offset คำนวณผิด
2. Library ยังไม่โหลด
3. Memory protection ไม่ได้เปลี่ยน

**แก้ไข:**
```cpp
// 1. รอให้ library โหลดก่อน
ElfScanner elf;
while(!(elf = ElfScanner::findElf("libtarget.so")).isValid()) {
    std::this_thread::sleep_for(100ms);
}

// 2. เปลี่ยน protection ก่อน patch
KittyMemory::setAddressProtection(address, size, KPROT_READ_WRITE_EXEC);

// 3. ตรวจสอบว่า patch ติด
auto patch = MemoryPatch::createWithHex(address, "D503201F");
if (patch.Modify()) {
    LOG("Patch success");
} else {
    LOG("Patch failed");
}
```

### การ Debug

#### ดู Log จาก Native

```cpp
// ใช้ Android Logging
#include <android/log.h>

#define LOG_TAG "PALLADIUM"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ใช้งาน
LOGI("Library loaded at: %p", (void*)base);
LOGE("Failed to patch: %d", errno);
```

```bash
# ดู log
adb logcat -s PALLADIUM:D
```

#### ใช้ GDB/LLDB Debug Native

```bash
# Attach ไปยัง process
adb shell gdbserver :5039 --attach $(pidof com.game.package)

# ในอีก terminal
adb forward tcp:5039 tcp:5039
arm-linux-androideabi-gdb liblib-name.so
(gdb) target remote :5039
```

---

## ⚠️ หมายเหตุและข้อควรระวัง

### 🔐 ความปลอดภัย

#### Keystore Management
- ไฟล์ `real.keystore` ในโปรเจคนี้เป็น **ตัวอย่างเท่านั้น**
- ห้ามใช้ Keystore นี้ในโปรเจคจริง
- สร้าง Keystore ใหม่ด้วยคำสั่ง:
```bash
keytool -genkey -v -keystore my-app.keystore -alias release \
  -keyalg RSA -keysize 2048 -validity 10000
```

#### ProGuard Mapping
- เก็บไฟล์ `mapping.txt` จากทุก Release Build
- ใช้สำหรับ deobfuscate stack trace ตอนเกิด crash
- อย่าแชร์ mapping.txt สู่ public

### 🧪 การพัฒนา

#### Best Practices
1. **ทดสอบบนอุปกรณ์จริงเสมอ** - Emulator ไม่สามารถจำลองทุกอย่างได้
2. **ใช้ Version Control** - Commit บ่อยๆ และเขียน commit message ที่มีความหมาย
3. **Document การเปลี่ยนแปลง** - เขียน comment ในโค้ดที่ซับซ้อน
4. **Test ทุก Architecture** - ถ้ารองรับหลาย ABI

#### ข้อจำกัด
- **Android 9.0+ เท่านั้น** - ไม่รองรับ Android 8.1 หรือต่ำกว่า
- **ARM64 เป็นหลัก** - armeabi-v7a อาจมีปัญหาบางอย่าง
- **Root ไม่จำเป็น** - แต่บาง feature ต้องการ root

### 📜 ข้อกฎหมายและจริยธรรม

#### ข้อตกลงการใช้งาน
- โปรเจคนี้สร้างขึ้นเพื่อ **การศึกษาและวิจัย**
- ผู้ใช้ต้องรับผิดชอบต่อการใช้งานเอง
- ห้ามใช้ในการทำลายระบบหรือละเมิดสิทธิผู้อื่น
- เคารพ Terms of Service ของแอปพลิเคชันเป้าหมาย

#### การรายงานปัญหาด้านความปลอดภัย
หากพบช่องโหว่ด้านความปลอดภัยใน PALLADIUM:
1. **อย่า** เปิด Issue สาธารณะ
2. ส่งอีเมลไปที่ security@palladium.dev พร้อมรายละเอียด
3. รอการยืนยันและ patch ภายใน 90 วัน

---

<div align="center">

## 🌟 มาร่วมพัฒนาด้วยกัน!

เรายินดีต้อนรับทุกการมีส่วนร่วมไม่ว่าจะเป็น:

- 🐛 **Bug Reports** - รายงานปัญหาที่พบ
- 💡 **Feature Requests** - เสนอไอเดียใหม่
- 🔧 **Pull Requests** - ส่งโค้ดมาเพิ่ม
- 📖 **Documentation** - ช่วยเขียนเอกสาร
- 🌍 **Translations** - แปลเป็นภาษาอื่น

### ติดต่อเรา

[![Discord](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/palladium)
[![Twitter](https://img.shields.io/badge/Twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com/palladium_mod)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/yourusername/PALLADIUM)

---

**Made with ❤️ by PALLADIUM Team**

© 2026 PALLADIUM Project. All rights reserved.

</div>
