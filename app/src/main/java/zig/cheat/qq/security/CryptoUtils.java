package zig.cheat.qq.security;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "ENIGMA_ENTERPRISE_KEY_CHANGE_ME_!!";

    public static String encryptXOR(String input) {
        char[] key = {'K', 'E', 'Y', 'X'};
        StringBuilder output = new StringBuilder();
        for(int i = 0; i < input.length(); i++) {
            output.append((char) (input.charAt(i) ^ key[i % key.length]));
        }
        return Base64.encodeToString(output.toString().getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }
}
