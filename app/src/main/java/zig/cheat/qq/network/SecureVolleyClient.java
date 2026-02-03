package zig.cheat.qq.network;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import zig.cheat.qq.security.TrafficObfuscator;

public class SecureVolleyClient {
    public static RequestQueue createSecureQueue(Context context) {
        HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpURLConnection conn = super.createConnection(url);
                if (conn instanceof HttpsURLConnection) {
                    try {
                        TrafficObfuscator.addFakeHeaders(conn);
                    } catch (Exception e) {
                        throw new IOException("Security check failed", e);
                    }
                }
                return conn;
            }
        };
        return Volley.newRequestQueue(context, hurlStack);
    }
}
