package zig.cheat.qq;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import zig.cheat.qq.security.AntiDetectionManager;
import zig.cheat.qq.ui.FloatingMenu;

public class ϟ {
	static {
        System.loadLibrary(o(new int[]{37, 32, 43, 100, 39, 40, 36, 44}));
    }

    private static String o(int[] d) {
        StringBuilder s = new StringBuilder();
        for (int i : d) s.append((char) (i ^ 0x49));
        return s.toString();
    }
    
    public static void ϟ(final Context context) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                AntiDetectionManager.init(context);
                FloatingMenu ϟ = new FloatingMenu(context);
                ϟ.show();
            }
        });
    }	
}