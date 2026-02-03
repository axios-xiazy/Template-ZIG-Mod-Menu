package zig.cheat.qq;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
        
        AntiDetectionManager.init(this);

        floatingMenu = new FloatingMenu(this);
        floatingMenu.show();
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (floatingMenu != null) {
            floatingMenu.destroy();
        }
    }
    
}