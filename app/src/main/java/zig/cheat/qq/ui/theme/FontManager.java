package zig.cheat.qq.ui.theme;

import android.content.Context;
import android.graphics.Typeface;

public class FontManager {
    private static Typeface mainFont;

    public static Typeface getFont(Context context) {
        if (mainFont == null) {
            try {
                mainFont = Typeface.createFromAsset(context.getAssets(), "fonts/font_main.ttf");
            } catch (Throwable t) {
                mainFont = Typeface.DEFAULT; 
            }
        }
        return mainFont;
    }
}