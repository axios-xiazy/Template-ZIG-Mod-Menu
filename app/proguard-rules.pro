-repackageclasses ''
-allowaccessmodification

-renamesourcefileattribute ''

-keep public class zig.cheat.qq.ϟ {
    public static void ϟ(android.content.Context);
}

-keep class zig.cheat.qq.jni.Jni {
    <methods>;
    <fields>;
}

-obfuscationdictionary obfuscation-dictionary.txt
-classobfuscationdictionary obfuscation-dictionary.txt
-packageobfuscationdictionary obfuscation-dictionary.txt

-dontnote **
-dontwarn **
