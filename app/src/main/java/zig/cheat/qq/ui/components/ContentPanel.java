package zig.cheat.qq.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.regex.Pattern;
import zig.cheat.qq.ui.anim.AnimUtils;
import zig.cheat.qq.ui.dialogs.DumpLibDialog;
import zig.cheat.qq.ui.dialogs.PatchOffsetDialog;
import zig.cheat.qq.ui.dialogs.SignatureManagerDialog;
import zig.cheat.qq.ui.theme.ThemeConstants;
import zig.cheat.qq.ui.theme.FontManager;
import zig.cheat.qq.native_bridge.StealthJNI;

public class ContentPanel extends ScrollView {
    private LinearLayout contentLayout;
    private Context context;

    private final SparseArray<Boolean> checkStates = new SparseArray<>();
    private final SparseArray<Integer> valueStates = new SparseArray<>();
    private static final Pattern PIPE_PATTERN = Pattern.compile(Pattern.quote("|"));

    public ContentPanel(Context context) {
        super(context);
        this.context = context;
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setFillViewport(true);
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
        
        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        float d = context.getResources().getDisplayMetrics().density;
        contentLayout.setPadding((int)(20 * d), (int)(12 * d), (int)(20 * d), (int)(20 * d));
        contentLayout.setLayerType(LAYER_TYPE_HARDWARE, null);
        addView(contentLayout);
        
        setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            contentLayout.setLayerType(LAYER_TYPE_HARDWARE, null);
        });
    }

    public void loadDynamicContent(int pageIndex) {
        animate().alpha(0f).translationY(20).setDuration(150).withEndAction(() -> {
            contentLayout.removeAllViews();
            try {
                String[] features = StealthJNI.getFeatures();
                if (features != null) {
                    for (String f : features) {
                        if (f == null || !f.contains("|")) continue;
                        safeParseAndAdd(f, pageIndex);
                    }
                }
            } catch (Throwable t) {}
            setAlpha(0f);
            setTranslationY(-20);
            animate().alpha(1f).translationY(0).setDuration(350).setInterpolator(AnimUtils.EASE_OUT).start();
        }).start();
    }

    private void safeParseAndAdd(String data, int targetPage) {
        try {
            String[] p = PIPE_PATTERN.split(data);
            if (p.length < 3) return;
            String type = p[0].trim();
            int pIdx = Integer.parseInt(p[1].trim());
            if (pIdx != targetPage && !type.equals("PAGE")) return;

            if (type.equals("TITLE")) {
                addSection(p[2]);
            } else if (type.equals("CHECK") && p.length >= 4) {
                addSwitch(p[2], Integer.parseInt(p[3].trim()));
            } else if (type.equals("SLIDER") && p.length >= 6) {
                addSlider(p[2], Integer.parseInt(p[3].trim()), Integer.parseInt(p[4].trim()), Integer.parseInt(p[5].trim()));
            } else if (type.equals("SPINNER") && p.length >= 5) {
                addDropdown(p[2], p[3].split(","), Integer.parseInt(p[4].trim()));
            } else if (type.equals("BUTTON") && p.length >= 4) {
                addButton(p[2], p[3]);
            }
        } catch (Throwable t) {}
    }

    private void addSection(String text) {
        float d = context.getResources().getDisplayMetrics().density;
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(ThemeConstants.COLOR_TEXT_SECONDARY);
        tv.setTextSize(10);
        tv.setTypeface(FontManager.getFont(context), Typeface.BOLD);
        tv.setLetterSpacing(0.1f);
        int topPad = contentLayout.getChildCount() == 0 ? (int)(4 * d) : (int)(20 * d);
        tv.setPadding((int)(4 * d), topPad, 0, (int)(8 * d));
        contentLayout.addView(tv);
    }

    private void addSwitch(String label, final int id) {
        boolean currentState = checkStates.get(id, false);

        contentLayout.addView(new SwitchRow(context, label, (c) -> {
            checkStates.put(id, c);
            try { StealthJNI.Callback(id, c, 0, 0.0f, ""); } catch (Throwable t) {}
        }, currentState));
    }

    private void addDropdown(String label, String[] items, final int id) {
        float d = context.getResources().getDisplayMetrics().density;
        LinearLayout card = createReactCard();
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextColor(ThemeConstants.COLOR_TEXT_PRIMARY);
        tv.setTypeface(FontManager.getFont(context));
        tv.setTextSize(13);
        tv.setPadding(0, 0, 0, (int)(8 * d));
        card.addView(tv);

        int lastPos = valueStates.get(id, 0);

        ModernDropdown dropdown = new ModernDropdown(context, items, pos -> {
            valueStates.put(id, pos);
            try { StealthJNI.Callback(id, false, pos, 0.0f, ""); } catch (Throwable t) {}
        });
        dropdown.setSelection(lastPos);
        card.addView(dropdown);
        contentLayout.addView(card);
    }

    private void addSlider(String label, int min, int max, final int id) {
        float d = context.getResources().getDisplayMetrics().density;
        LinearLayout card = createReactCard();

        int currentVal = valueStates.get(id, min);

        TextView tv = new TextView(context);
        tv.setText(label + ": " + currentVal);
        tv.setTextColor(ThemeConstants.COLOR_TEXT_PRIMARY);
        tv.setTypeface(FontManager.getFont(context));
        tv.setTextSize(13);
        card.addView(tv);

        ModernSeekBar sb = new ModernSeekBar(context, min, max, val -> {
            tv.setText(label + ": " + val);
            valueStates.put(id, val);
            try { StealthJNI.Callback(id, false, val, 0.0f, ""); } catch (Throwable t) {}
        });
        sb.setProgress(currentVal);

        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(36 * d));
        slp.topMargin = (int)(8 * d);
        sb.setLayoutParams(slp);
        card.addView(sb);
        contentLayout.addView(card);
    }

    private DumpLibDialog dumpDialog;
    private PatchOffsetDialog patchDialog;
    private SignatureManagerDialog signatureManagerDialog;

    private void addButton(String label, String action) {
        if (label == null || action == null || label.length() > 100 || action.length() > 50) return;

        float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);

        Button btn = new Button(context);
        btn.setText(label);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(12);
        btn.setTypeface(FontManager.getFont(context));
        btn.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        int color = 0xFF3B82F6;
        if (action.contains("dump") || action.contains("Dump")) color = 0xFF8B5CF6;
        else if (action.contains("patch") || action.contains("Patch")) color = 0xFFF59E0B;
        bg.setColor(color);
        bg.setCornerRadius(10 * d);
        btn.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(44 * d));
        lp.bottomMargin = (int)(12 * d);
        btn.setLayoutParams(lp);

        btn.setOnClickListener(v -> {
            try {
                if (!(context instanceof android.app.Activity)) {
                    return;
                }
                android.app.Activity activity = (android.app.Activity) context;
                if (activity.isFinishing()) return;

                if (action.equals("dump_lib_dialog")) {
                    if (dumpDialog != null) dumpDialog.dismiss();
                    dumpDialog = new DumpLibDialog(activity);
                    dumpDialog.show();
                } else if (action.equals("patch_offset_dialog")) {
                    if (patchDialog != null) patchDialog.dismiss();
                    patchDialog = new PatchOffsetDialog(activity);
                    patchDialog.show();
                } else if (action.equals("signature_manager_dialog")) {
                    try {
                        SignatureManagerDialog sigDialog = new SignatureManagerDialog(activity);
                        sigDialog.show();
                    } catch (Exception e) {}
                }
            } catch (Throwable t) {}
        });

        contentLayout.addView(btn);
    }

    private LinearLayout createReactCard() {
        float d = context.getResources().getDisplayMetrics().density;
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding((int)(16 * d), (int)(12 * d), (int)(16 * d), (int)(12 * d));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF1A1A1A);
        bg.setCornerRadius(10 * d);
        bg.setStroke((int)(1 * d), 0xFF333333);
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = (int)(12 * d);
        card.setLayoutParams(lp);
        return card;
    }
}
