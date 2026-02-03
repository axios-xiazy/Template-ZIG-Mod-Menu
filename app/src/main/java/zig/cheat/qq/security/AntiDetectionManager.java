package zig.cheat.qq.security;

import android.content.Context;
import zig.cheat.qq.native_bridge.StealthJNI;

public class AntiDetectionManager {
    public static void init(Context context) {
        StealthJNI.initSecurity();
    }
}
