package zig.cheat.qq.ui.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import zig.cheat.qq.ui.theme.ThemeConstants;
import zig.cheat.qq.ui.theme.FontManager;

public class SwitchRow extends LinearLayout {
    public SwitchRow(Context context, String text, SmoothSwitch.OnCheckedChangeListener listener, boolean isChecked) {
        super(context);
        float d = context.getResources().getDisplayMetrics().density;
        
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding((int)(16 * d), (int)(12 * d), (int)(12 * d), (int)(12 * d));
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF1A1A1A);
        bg.setCornerRadius(10 * d);
        bg.setStroke((int)(1 * d), 0xFF333333);
        setBackground(bg);
        
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = (int)(10 * d);
        setLayoutParams(lp);

        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(ThemeConstants.COLOR_TEXT_PRIMARY);
        tv.setTextSize(14);
        tv.setTypeface(FontManager.getFont(context));
        tv.setLayoutParams(new LayoutParams(0, -2, 1f));
        addView(tv);

        SmoothSwitch sw = new SmoothSwitch(context);
        sw.setChecked(isChecked);
        sw.setOnCheckedChangeListener(listener);
        addView(sw);
    }
}
