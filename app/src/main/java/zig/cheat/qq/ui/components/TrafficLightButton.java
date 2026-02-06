package zig.cheat.qq.ui.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;

public class TrafficLightButton extends View {
    public TrafficLightButton(Context context, int color, int sizeDp) {
        super(context);
        float d = context.getResources().getDisplayMetrics().density;
        setLayoutParams(new LinearLayout.LayoutParams((int)(sizeDp * d), (int)(sizeDp * d)));
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        setBackground(bg);
    }
}
