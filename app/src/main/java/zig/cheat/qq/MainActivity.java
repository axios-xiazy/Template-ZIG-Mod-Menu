package zig.cheat.qq;

import android.app.Activity;
import android.os.Bundle;
import zig.cheat.qq.ui.Menu;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("lib-name");
    }

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Menu.setHighRefreshRate(this);
        menu = new Menu(this);
        menu.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (menu != null) menu.destroy();
    }
}
