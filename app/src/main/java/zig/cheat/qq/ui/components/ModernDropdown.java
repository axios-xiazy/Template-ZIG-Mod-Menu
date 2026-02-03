package zig.cheat.qq.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import zig.cheat.qq.ui.theme.FontManager;
import zig.cheat.qq.ui.theme.ThemeConstants;

public class ModernDropdown extends LinearLayout {
    private TextView selectedText;
    private ListPopupWindow popup;
    private String[] items;
    private OnItemSelectedListener listener;
    private GradientDrawable normalBg;
    private GradientDrawable activeBg;
    private int currentSelection = 0;

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }

    public ModernDropdown(Context context, String[] items, OnItemSelectedListener listener) {
        super(context);
        this.items = items;
        this.listener = listener;
        init();
    }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding((int)(14 * d), 0, (int)(14 * d), 0);
        setClickable(true);
        setFocusable(true);

        int menuColor = ThemeConstants.COLOR_WINDOW_BG;
        normalBg = new GradientDrawable();
        normalBg.setColor(menuColor);
        normalBg.setCornerRadius(8 * d);
        normalBg.setStroke((int)(1 * d), 0xFF333333);
        activeBg = new GradientDrawable();
        activeBg.setColor(menuColor);
        activeBg.setCornerRadius(8 * d);
        activeBg.setStroke((int)(1.5f * d), ThemeConstants.COLOR_ACCENT_BLUE);

        setBackground(normalBg);
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(40 * d)));

        selectedText = new TextView(getContext());
        selectedText.setText(items.length > 0 ? items[0] : "Select option");
        selectedText.setTextColor(0xFFE5E7EB);
        selectedText.setTextSize(13);
        selectedText.setTypeface(FontManager.getFont(getContext()));
        selectedText.setLayoutParams(new LayoutParams(0, -2, 1f));
        addView(selectedText);

        TextView arrow = new TextView(getContext());
        arrow.setText("▾"); 
        arrow.setTextColor(0xFF9CA3AF);
        arrow.setTextSize(12);
        addView(arrow);

        popup = new ListPopupWindow(getContext());
        popup.setAdapter(new DropdownAdapter());
        popup.setAnchorView(this);
        popup.setModal(true);
        popup.setVerticalOffset((int)(4 * d));
        post(() -> popup.setWidth(getWidth()));
        
        GradientDrawable popBg = new GradientDrawable();
        popBg.setColor(menuColor);
        popBg.setCornerRadius(10 * d);
        popBg.setStroke((int)(1 * d), 0xFF404040);
        popup.setBackgroundDrawable(popBg);

        popup.setOnItemClickListener((parent, view, position, id) -> {
            setSelection(position);
            if (listener != null) listener.onItemSelected(position);
            popup.dismiss();
        });
        popup.setOnDismissListener(() -> setBackground(normalBg));
        setOnClickListener(v -> {
            setBackground(activeBg);
            popup.show();
        });
    }

    public void setSelection(int position) {
        if (position >= 0 && position < items.length) {
            this.currentSelection = position;
            selectedText.setText(items[position]);
        }
    }

    private class DropdownAdapter extends BaseAdapter {
        @Override public int getCount() { return items.length; }
        @Override public Object getItem(int pos) { return items[pos]; }
        @Override public long getItemId(int pos) { return pos; }
        @Override public View getView(int pos, View cv, ViewGroup p) {
            float d = getResources().getDisplayMetrics().density;
            LinearLayout itemBox = new LinearLayout(getContext());
            itemBox.setOrientation(HORIZONTAL);
            itemBox.setPadding((int)(16 * d), (int)(12 * d), (int)(16 * d), (int)(12 * d));
            StateListDrawable selector = new StateListDrawable();
            GradientDrawable hoverBg = new GradientDrawable();
            hoverBg.setColor(0x20FFFFFF);
            selector.addState(new int[]{android.R.attr.state_pressed}, hoverBg);
            itemBox.setBackground(selector);
            TextView tv = new TextView(getContext());
            tv.setText(items[pos]);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(13);
            tv.setTypeface(FontManager.getFont(getContext()));
            itemBox.addView(tv);
            return itemBox;
        }
    }
}