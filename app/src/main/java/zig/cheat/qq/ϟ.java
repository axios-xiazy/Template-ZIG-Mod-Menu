package zig.cheat.qq;

import android.app.Activity;
import android.content.Context;
import java.lang.ref.WeakReference;
import zig.cheat.qq.ui.Menu;

public class ϟ {
    private static Menu menu;

    static {
        System.loadLibrary("lib-name");
    }

    public static void ϟ(Context context) {
        WeakReference<Context> ref = new WeakReference<>(context);
        Context ctx = ref.get();
        if (ctx == null) return;
        if (ctx instanceof Activity) {
            Menu.setHighRefreshRate((Activity) ctx);
        }
        if (menu != null) menu.destroy();
        menu = new Menu(ctx);
        menu.show();
    }
}
