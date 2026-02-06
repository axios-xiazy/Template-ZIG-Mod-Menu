package zig.cheat.qq.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import zig.cheat.qq.ui.anim.AnimUtils;
import zig.cheat.qq.ui.theme.ThemeConstants;
import zig.cheat.qq.ui.theme.FontManager;
import zig.cheat.qq.native_bridge.StealthJNI;

public class Sidebar extends FrameLayout {
    private LinearLayout itemContainer;
    private View selectionPill;
    private List<LinearLayout> items = new ArrayList<>();
    private OnTabSelectedListener listener;
    private int selectedInternalIndex = 0;

    public interface OnTabSelectedListener { void onTabSelected(int pageId); }

    public Sidebar(Context context, OnTabSelectedListener listener) {
        super(context);
        this.listener = listener;
        float d = context.getResources().getDisplayMetrics().density;
        setLayoutParams(new LinearLayout.LayoutParams((int)(160 * d), -1));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ThemeConstants.COLOR_SIDEBAR_BG);
        bg.setCornerRadii(new float[] { 0,0, 0,0, 0,0, 14 * d, 14 * d });
        setBackground(bg);

        selectionPill = new View(context);
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(ThemeConstants.COLOR_CARD_BG);
        pillBg.setCornerRadius(6 * d);
        pillBg.setStroke((int)(1 * d), ThemeConstants.COLOR_BORDER);
        selectionPill.setBackground(pillBg);
        selectionPill.setLayoutParams(new LayoutParams(0, 0));
        selectionPill.setVisibility(View.INVISIBLE);
        addView(selectionPill);

        itemContainer = new LinearLayout(context);
        itemContainer.setOrientation(LinearLayout.VERTICAL);

        itemContainer.setPadding((int)(6 * d), (int)(16 * d), (int)(16 * d), 0);
        addView(itemContainer);

        parseTabs();
        post(() -> { try { if (!items.isEmpty()) selectItem(0, false); } catch (Throwable t) {} });
    }

    private void parseTabs() {
        try {
            String[] f = StealthJNI.getFeatures();
            if (f == null) return;
            for (String s : f) {
                if (s != null && s.startsWith("PAGE|")) {
                    String[] p = s.split(Pattern.quote("|"));
                    if (p.length >= 4) {
                        try {
                            int pageId = Integer.parseInt(p[1].trim());
                            addItem(p[3], p[2], pageId);
                        } catch (Throwable t) {}
                    }
                }
            }
        } catch (Throwable t) {}
    }

    private void addItem(String text, String iconPath, final int pageId) {
        try {
            float d = getContext().getResources().getDisplayMetrics().density;
            final int currentInternalIdx = items.size();

            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setPadding((int)(12 * d), (int)(10 * d), (int)(12 * d), (int)(10 * d));
            itemLayout.setClickable(true);

            FrameLayout iconContainer = new FrameLayout(getContext());
            LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams((int)(18 * d), (int)(18 * d));
            icLp.rightMargin = (int)(10 * d);
            iconContainer.setLayoutParams(icLp);

            ImageView iconView = new ImageView(getContext());
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iconView.setVisibility(View.GONE);

            View fallbackView = new View(getContext());
            GradientDrawable fbBg = new GradientDrawable();
            fbBg.setShape(GradientDrawable.OVAL);
            int[] colors = {0xFF3B82F6, 0xFF10B981, 0xFFF59E0B, 0xFFEF4444};
            fbBg.setColor(colors[currentInternalIdx % colors.length]);
            fallbackView.setBackground(fbBg);
            FrameLayout.LayoutParams fbLp = new FrameLayout.LayoutParams((int)(10 * d), (int)(10 * d), Gravity.CENTER);
            fallbackView.setLayoutParams(fbLp);

            iconContainer.addView(fallbackView);
            iconContainer.addView(iconView);

            if (iconPath != null && !iconPath.trim().isEmpty()) {
                if (loadIntoImageView(iconPath.trim(), iconView)) {
                    fallbackView.setVisibility(View.GONE);
                    iconView.setVisibility(View.VISIBLE);
                }
            }

            itemLayout.addView(iconContainer);

            TextView tv = new TextView(getContext());
            tv.setText(text);
            tv.setTextSize(12);
            tv.setTextColor(ThemeConstants.COLOR_TEXT_SECONDARY);
            tv.setTypeface(FontManager.getFont(getContext()));
            itemLayout.addView(tv);

            itemLayout.setOnClickListener(v -> {
                try {
                    if (selectedInternalIndex != currentInternalIdx) {
                        selectItem(currentInternalIdx, true);
                        if (listener != null) listener.onTabSelected(pageId);
                    }
                } catch (Throwable t) {}
            });

            items.add(itemLayout);
            itemContainer.addView(itemLayout);
        } catch (Throwable t) {}
    }

    private boolean loadIntoImageView(String path, ImageView iv) {
        InputStream is = null;
        try {
            is = getContext().getAssets().open(path);
            Drawable drw = Drawable.createFromStream(is, null);
            if (drw != null) {
                iv.setImageDrawable(drw);
                is.close();
                return true;
            }
            is.close();
        } catch (Throwable t) {
            try { if (is != null) is.close(); } catch (Exception e) {}
        }
        return false;
    }

    private void selectItem(int internalIndex, boolean animate) {
        if (internalIndex < 0 || internalIndex >= items.size()) return;
        selectedInternalIndex = internalIndex;
        LinearLayout target = items.get(internalIndex);
        float targetY = itemContainer.getTop() + target.getTop();

        if (target.getHeight() > 0) {
            selectionPill.setVisibility(View.VISIBLE);
            if (animate) {
                selectionPill.setLayerType(LAYER_TYPE_HARDWARE, null);
                selectionPill.animate().y(targetY).setDuration(250).setInterpolator(AnimUtils.EASE_IN_OUT).withEndAction(() -> {
                    selectionPill.setLayerType(LAYER_TYPE_NONE, null);
                }).start();
            } else {
                selectionPill.setY(targetY);
                LayoutParams lp = (LayoutParams) selectionPill.getLayoutParams();
                float d = getContext().getResources().getDisplayMetrics().density;
				lp.width = itemContainer.getWidth() - itemContainer.getPaddingLeft() - itemContainer.getPaddingRight() - (int)(12 * d);
                lp.height = target.getHeight();
                lp.leftMargin = itemContainer.getPaddingLeft();
                selectionPill.setLayoutParams(lp);
                selectionPill.setX(itemContainer.getPaddingLeft());
            }
        }

        for (int i = 0; i < items.size(); i++) {
            try {
                LinearLayout item = items.get(i);
                FrameLayout iconContainer = (FrameLayout) item.getChildAt(0);
                View fallback = iconContainer.getChildAt(0);
                ImageView icon = (ImageView) iconContainer.getChildAt(1);
                TextView tv = (TextView) item.getChildAt(1);
                boolean isSel = (i == internalIndex);

                int targetColor = isSel ? ThemeConstants.COLOR_TEXT_PRIMARY : ThemeConstants.COLOR_TEXT_SECONDARY;
                tv.setTextColor(targetColor);
                if (icon.getVisibility() == View.VISIBLE) {
                    icon.setAlpha(isSel ? 1.0f : 0.6f);
                } else {
                    fallback.setAlpha(isSel ? 1.0f : 0.5f);
                }
                tv.setTypeface(FontManager.getFont(getContext()), isSel ? Typeface.BOLD : Typeface.NORMAL);
            } catch (Throwable t) {}
        }
    }
}