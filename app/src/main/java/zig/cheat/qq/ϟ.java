package zig.cheat.qq;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.lang.ref.WeakReference;
import zig.cheat.qq.security.AntiDetectionManager;
import zig.cheat.qq.ui.FloatingMenu;

public class ϟ {
    private static FloatingMenu ϟϟ;

    static {
        System.loadLibrary(o(new int[]{37, 32, 43, 100, 39, 40, 36, 44}));
    }

    private static String o(int[] d) {
        StringBuilder s = new StringBuilder();
        for (int i : d) s.append((char) (i ^ 0x49));
        return s.toString();
    }

    public static void ϟ(final Context context) {
        if (context instanceof Activity) {
            FloatingMenu.setHighRefreshRate((Activity) context);
        }
        final WeakReference<Context> ref = new WeakReference<>(context);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context ctx = ref.get();
                if (ctx == null) return;
                AntiDetectionManager.init(ctx);
                if (ϟϟ != null) {
                    ϟϟ.destroy();
                }
                ϟϟ = new FloatingMenu(ctx);
                ϟϟ.show();
            }
        });
    }
}
