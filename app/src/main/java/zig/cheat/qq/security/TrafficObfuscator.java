package zig.cheat.qq.security;

import java.net.HttpURLConnection;
import java.util.Random;

public class TrafficObfuscator {
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0.4472.124 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Version/14.1.1 Safari/605.1.15"
    };

    public static void addFakeHeaders(HttpURLConnection conn) {
        Random random = new Random();
        conn.setRequestProperty("User-Agent", USER_AGENTS[random.nextInt(USER_AGENTS.length)]);
        conn.setRequestProperty("X-Request-ID", java.util.UUID.randomUUID().toString());
    }
}
