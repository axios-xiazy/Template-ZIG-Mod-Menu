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
    private TextView labelView;
    private SmoothSwitch switchView;

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

        labelView = new TextView(context);
        labelView.setText(text);
        labelView.setTextColor(ThemeConstants.COLOR_TEXT_PRIMARY);
        labelView.setTextSize(14);
        labelView.setTypeface(FontManager.getFont(context));
        labelView.setLayoutParams(new LayoutParams(0, -2, 1f));
        addView(labelView);

        switchView = new SmoothSwitch(context);
        switchView.setChecked(isChecked);
        switchView.setOnCheckedChangeListener(listener);
        addView(switchView);
    }

    public void setLabel(String text) {
        labelView.setText(text);
    }

    public void setChecked(boolean checked) {
        switchView.setChecked(checked);
    }

    public void setOnCheckedChangeListener(SmoothSwitch.OnCheckedChangeListener listener) {
        switchView.setOnCheckedChangeListener(listener);
    }
}
