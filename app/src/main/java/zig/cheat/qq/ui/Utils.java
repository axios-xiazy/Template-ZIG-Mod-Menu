package zig.cheat.qq.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class Utils {
    private static Typeface cachedFont;
    
    public static Typeface getFont(Context context) {
        if (cachedFont == null) {
            try {
                cachedFont = Typeface.createFromAsset(context.getAssets(), "fonts/font_main.ttf");
            } catch (Throwable t) {
                cachedFont = Typeface.DEFAULT;
            }
        }
        return cachedFont;
    }
    
    public static GradientDrawable createCardBg(int color, float radiusDp, Context context) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radiusDp * context.getResources().getDisplayMetrics().density);
        return gd;
    }
    
    public static GradientDrawable createStrokeBg(int color, float radiusDp, int strokeWidthDp, int strokeColor, Context context) {
        float d = context.getResources().getDisplayMetrics().density;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radiusDp * d);
        gd.setStroke((int)(strokeWidthDp * d), strokeColor);
        return gd;
    }
    
    public static int dpToPx(float dp, Context context) {
        return (int)(dp * context.getResources().getDisplayMetrics().density);
    }
    
    public static Drawable getIcon(Context context, String name) {
        try {
            return Drawable.createFromStream(context.getAssets().open(name), null);
        } catch (Throwable t) {
            return null;
        }
    }
}
