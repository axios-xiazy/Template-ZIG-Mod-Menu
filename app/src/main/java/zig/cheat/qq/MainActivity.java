package zig.cheat.qq;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import zig.cheat.qq.security.AntiDetectionManager;
import zig.cheat.qq.ui.FloatingMenu;

public class MainActivity extends Activity {

    private FloatingMenu floatingMenu;

    static {
        System.loadLibrary(o(new int[]{37, 32, 43, 100, 39, 40, 36, 44}));
    }

    private static String o(int[] d) {
        StringBuilder s = new StringBuilder();
        for (int i : d) s.append((char) (i ^ 0x49));
        return s.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI();
        setHighRefreshRate();
        AntiDetectionManager.init(this);

        floatingMenu = new FloatingMenu(this);
        floatingMenu.show();
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void setHighRefreshRate() {
        FloatingMenu.setHighRefreshRate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (floatingMenu != null) {
            floatingMenu.destroy();
        }
    }
}
