package zig.cheat.qq.native_bridge;

public class StealthJNI {
    // existing methods
    public static native void initSecurity();
    public static native String[] getFeatures();
    public static native void Callback(int id, boolean check, int value, float value2, String value3);
    
    // Dump Lib methods
    public static native String[] getLoadedLibs();
    public static native int getLibCount();
    public static native boolean dumpLib(int index, String outPath);
    public static native boolean dumpAllLibs(String outPath);
    public static native boolean dumpGlobalMetadata(String outPath);
    public static native String[] getLoadedLibNames();
    public static native String[] getAllLoadedLibs();
    
    // Patch methods
    public static native long findLibBase(String libName);
    public static native int createPatch(String libName, long offset, String hexBytes, String description);
    public static native boolean applyPatch(int patchId);
    public static native boolean restorePatch(int patchId);
    public static native void removePatch(int patchId);
    public static native String[] getPatchList();
    public static native int[] getAllPatchIds();
    public static native String readMemoryHex(long address, int size);
    public static native String readMemoryHexDump(long address, int size);
    public static native long[] scanPatternAll(String libName, String pattern, int maxResults);
    public static native String exportPatchesJson();
    public static native int importPatchesJson(String json);
}