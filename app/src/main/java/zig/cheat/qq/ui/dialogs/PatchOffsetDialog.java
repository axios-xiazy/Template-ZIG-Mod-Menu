package zig.cheat.qq.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import zig.cheat.qq.native_bridge.StealthJNI;
import zig.cheat.qq.ui.theme.FontManager;
import zig.cheat.qq.ui.dialogs.DumpLibDialog;
import zig.cheat.qq.ui.theme.ThemeConstants;

public class PatchOffsetDialog {
    private static final int MAX_PATCHES = 100;
    private static final int MAX_SCAN_RESULTS = 20;
    private static final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicLong counter = new AtomicLong(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "PatchDialog-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    });

    private final Context context;
    private final Handler handler;
    private final Map<Integer, Runnable> freezeRunnables = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> freezeStates = new ConcurrentHashMap<>();
    private Dialog dialog;
    private Spinner libSpinner;
    private EditText offsetInput;
    private EditText hexInput;
    private EditText descInput;
    private EditText scanPatternInput;
    private TextView previewText;
    private TextView scanResultText;
    private LinearLayout patchListLayout;
    private LinearLayout scannerSection;
    private CheckBox freezeCheckBox;
    private final AtomicBoolean isDestroyed = new AtomicBoolean(false);

    private static final Set<String> GAME_LIBS = new HashSet<String>() {{
        add("libil2cpp.so"); add("libanogs.so"); add("libanort.so"); add("libanhook.so");
        add("libunity.so"); add("libmain.so"); add("libmono.so"); add("libxlua.so");
        add("libtolua.so"); add("libcocos2dcpp.so"); add("libUE4.so"); add("libgamedata.so");
    }};

    public PatchOffsetDialog(Context ctx) {
        this.context = ctx;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void show() {
        if (context == null || !(context instanceof Activity) || ((Activity)context).isFinishing()) return;

        if (dialog != null && dialog.isShowing()) dialog.dismiss();

        try {
            createDialog();
        } catch (Exception e) {
            safeToast("Error: " + e.getMessage());
        }
    }

    private void createDialog() {
        dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setOnDismissListener(d -> {
            stopAllFreezes();
            cleanup();
        });

        final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);
        final int width = (int)(600 * d);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding((int)(16*d), (int)(8*d), (int)(16*d), (int)(8*d));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ThemeConstants.COLOR_WINDOW_BG);
        bg.setCornerRadius(12*d);
        bg.setStroke((int)(1*d), ThemeConstants.COLOR_BORDER);
        root.setBackground(bg);

        root.addView(createTitle("PATCH MANAGER", d));
        root.addView(createDivider(d));

        root.addView(createSectionTitle("TARGET", d));
        libSpinner = new Spinner(context);
        libSpinner.setLayoutParams(createLP(-1, (int)(28*d), 0, (int)(2*d)));
        root.addView(libSpinner);

        LinearLayout inputRow = new LinearLayout(context);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setLayoutParams(createLP(-1, -2, (int)(6*d), 0));

        LinearLayout offsetCol = new LinearLayout(context);
        offsetCol.setOrientation(LinearLayout.VERTICAL);
        offsetCol.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        TextView offsetLabel = createMiniLabel("OFFSET", d);
        offsetCol.addView(offsetLabel);
        offsetInput = createHexInput("0x123456", d, (int)(32*d));
        offsetCol.addView(offsetInput);
        inputRow.addView(offsetCol);

        inputRow.addView(createSpacer(d));

        LinearLayout hexCol = new LinearLayout(context);
        hexCol.setOrientation(LinearLayout.VERTICAL);
        hexCol.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.2f));

        TextView hexLabel = createMiniLabel("BYTES (HEX)", d);
        hexCol.addView(hexLabel);
        hexInput = createHexInput("00 00 80 D2", d, (int)(32*d));
        hexInput.addTextChangedListener(new HexTextWatcher());
        hexCol.addView(hexInput);
        inputRow.addView(hexCol);

        root.addView(inputRow);

        LinearLayout freezeRow = new LinearLayout(context);
        freezeRow.setOrientation(LinearLayout.HORIZONTAL);
        freezeRow.setLayoutParams(createLP(-1, -2, (int)(3*d), 0));

        freezeCheckBox = new CheckBox(context);
        freezeCheckBox.setText("Freeze");
        freezeCheckBox.setTextColor(0xFF10B981);
        freezeCheckBox.setTextSize(9);
        freezeCheckBox.setTypeface(FontManager.getFont(context));
        freezeCheckBox.setPadding((int)(4*d), 0, 0, 0);
        freezeCheckBox.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        freezeRow.addView(freezeCheckBox);

        descInput = createTextInput("Desc...", d);
        descInput.setLayoutParams(new LinearLayout.LayoutParams(0, (int)(28*d), 1));
        freezeRow.addView(descInput);

        root.addView(freezeRow);

        LinearLayout actionRow = new LinearLayout(context);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setLayoutParams(createLP(-1, (int)(28*d), (int)(6*d), 0));

        actionRow.addView(createActionButton("READ", 0xFF3B82F6, d, v -> readMemory()));
        actionRow.addView(createSpacer(d));
        actionRow.addView(createActionButton("SCAN", 0xFF8B5CF6, d, v -> toggleScanner()));
        actionRow.addView(createSpacer(d));
        actionRow.addView(createActionButton("ADD", 0xFFF59E0B, d, v -> addPatch()));

        root.addView(actionRow);

        previewText = createInfoText("Ready", d);
        root.addView(previewText);

        scannerSection = (LinearLayout) createScannerSection(d);
        scannerSection.setVisibility(View.GONE);
        root.addView(scannerSection);

        root.addView(createDivider(d));

        LinearLayout batchRow = new LinearLayout(context);
        batchRow.setOrientation(LinearLayout.HORIZONTAL);
        batchRow.setLayoutParams(createLP(-1, (int)(24*d), (int)(5*d), 0));

        batchRow.addView(createMiniButton("ON", 0xFF10B981, d, v -> batchEnable(true)));
        batchRow.addView(createSpacer(d));
        batchRow.addView(createMiniButton("OFF", 0xFFEF4444, d, v -> batchEnable(false)));
        batchRow.addView(createSpacer(d));
        batchRow.addView(createMiniButton("EXP", 0xFF3B82F6, d, v -> exportPatches()));
        batchRow.addView(createSpacer(d));
        batchRow.addView(createMiniButton("IMP", 0xFF8B5CF6, d, v -> importPatches()));

        root.addView(batchRow);

        ScrollView scroll = new ScrollView(context);
        scroll.setLayoutParams(createLP(-1, (int)(130*d), (int)(4*d), 0));

        patchListLayout = new LinearLayout(context);
        patchListLayout.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(patchListLayout);
        root.addView(scroll);

        dialog.setContentView(root);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
        loadLibList();
        refreshPatchList();
    }

    private View createScannerSection(float d) {
        LinearLayout section = new LinearLayout(context);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setTag("scanner_section");

        section.addView(createSectionTitle("AOB SCANNER", d));

        scanPatternInput = createHexInput("E8 ? ? ? ? 48 8B 05", d, (int)(32*d));
        section.addView(scanPatternInput);

        Button scanBtn = createActionButton("SEARCH", 0xFF10B981, d, v -> scanPattern());
        scanBtn.setLayoutParams(createLP(-1, (int)(30*d), (int)(4*d), 0));
        section.addView(scanBtn);

        scanResultText = createInfoText("", d);
        scanResultText.setTextIsSelectable(true);
        section.addView(scanResultText);

        return section;
    }

    private void toggleScanner() {
        if (scannerSection != null) {
            scannerSection.setVisibility(scannerSection.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }

    private TextView createTitle(String text, float d) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(ThemeConstants.COLOR_TEXT_PRIMARY);
        tv.setTextSize(12);
        tv.setTypeface(FontManager.getFont(context), Typeface.BOLD);
        tv.setLetterSpacing(0.1f);
        return tv;
    }

    private TextView createSectionTitle(String text, float d) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(ThemeConstants.COLOR_TEXT_SECONDARY);
        tv.setTextSize(8);
        tv.setTypeface(FontManager.getFont(context));
        tv.setPadding(0, (int)(8*d), 0, (int)(2*d));
        tv.setLetterSpacing(0.08f);
        return tv;
    }

    private TextView createMiniLabel(String text, float d) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(ThemeConstants.COLOR_TEXT_SECONDARY);
        tv.setTextSize(7);
        tv.setTypeface(FontManager.getFont(context));
        tv.setPadding(0, 0, 0, (int)(2*d));
        return tv;
    }

    private TextView createInfoText(String text, float d) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(ThemeConstants.COLOR_TEXT_SECONDARY);
        tv.setTextSize(9);
        tv.setTypeface(FontManager.getFont(context));
        tv.setPadding(0, (int)(4*d), 0, 0);
        tv.setTextIsSelectable(true);
        return tv;
    }

    private View createDivider(float d) {
        View v = new View(context);
        v.setLayoutParams(createLP(-1, (int)(1*d), (int)(8*d), 0));
        v.setBackgroundColor(ThemeConstants.COLOR_DIVIDER);
        return v;
    }

    private View createSpacer(float d) {
        View v = new View(context);
        v.setLayoutParams(new LinearLayout.LayoutParams((int)(4*d), 0));
        return v;
    }

    private LinearLayout.LayoutParams createLP(int w, int h, int top, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        lp.topMargin = top;
        lp.bottomMargin = bottom;
        return lp;
    }

    private EditText createHexInput(String hint, float d, int height) {
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setTextColor(ThemeConstants.COLOR_TEXT_PRIMARY);
        et.setHintTextColor(0xFF666666);
        et.setTextSize(11);
        et.setTypeface(Typeface.MONOSPACE);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        et.setBackground(createInputBg(d));
        et.setPadding((int)(8*d), (int)(6*d), (int)(8*d), (int)(6*d));
        et.setLayoutParams(createLP(-1, height, 0, 0));
        return et;
    }

    private EditText createTextInput(String hint, float d) {
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setTextColor(ThemeConstants.COLOR_TEXT_PRIMARY);
        et.setHintTextColor(0xFF666666);
        et.setTextSize(10);
        et.setTypeface(FontManager.getFont(context));
        et.setBackground(createInputBg(d));
        et.setPadding((int)(8*d), (int)(4*d), (int)(8*d), (int)(4*d));
        et.setLayoutParams(createLP(-1, (int)(32*d), 0, 0));
        return et;
    }

    private GradientDrawable createInputBg(float d) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF0F0F0F);
        bg.setCornerRadius(6*d);
        bg.setStroke((int)(1*d), 0xFF333333);
        return bg;
    }

    private Button createActionButton(String text, int color, float d, View.OnClickListener listener) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(11);
        btn.setTypeface(FontManager.getFont(context), Typeface.BOLD);
        btn.setAllCaps(false);
        btn.setPadding((int)(8*d), (int)(6*d), (int)(8*d), (int)(6*d));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(6*d);
        btn.setBackground(bg);
        btn.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1));
        btn.setOnClickListener(v -> safeExecute(() -> listener.onClick(v)));
        return btn;
    }

    private Button createMiniButton(String text, int color, float d, View.OnClickListener listener) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(10);
        btn.setTypeface(FontManager.getFont(context));
        btn.setAllCaps(false);
        btn.setPadding((int)(6*d), (int)(4*d), (int)(6*d), (int)(4*d));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(4*d);
        btn.setBackground(bg);
        btn.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1));
        btn.setOnClickListener(v -> safeExecute(() -> listener.onClick(v)));
        return btn;
    }

    private class HexTextWatcher implements TextWatcher {
        private boolean isFormatting = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isFormatting) return;
            isFormatting = true;

            String hex = s.toString().replace(" ", "").replace("0x", "").replace("0X", "");
            StringBuilder formatted = new StringBuilder();

            for (int i = 0; i < hex.length(); i += 2) {
                if (i > 0) formatted.append(" ");
                if (i + 2 <= hex.length()) {
                    formatted.append(hex.substring(i, i + 2));
                } else {
                    formatted.append(hex.substring(i));
                }
            }

            s.replace(0, s.length(), formatted.toString().toUpperCase());
            isFormatting = false;
        }
    }

    private void safeExecute(Runnable r) {
        if (isDestroyed.get()) return;
        try {
            r.run();
        } catch (Exception e) {
            safeToast("Error: " + e.getMessage());
        }
    }

    private void safeToast(String msg) {
        if (context != null && msg != null) {
            handler.post(() -> {
                if (!isDestroyed.get()) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadLibList() {
        executor.execute(() -> {
            try {
                List<String> libs = new ArrayList<>();
                libs.add("libil2cpp.so");

                String[] loaded = StealthJNI.getLoadedLibNames();
                if (loaded != null) {
                    for (String lib : loaded) {
                        if (lib != null && !libs.contains(lib) && isGameLib(lib)) {
                            libs.add(lib);
                        }
                    }
                }

                handler.post(() -> {
                    if (isDestroyed.get() || libSpinner == null) return;
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_item, libs);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    libSpinner.setAdapter(adapter);
                });
            } catch (Exception ignored) {}
        });
    }

    private boolean isGameLib(String name) {
        if (name == null) return false;
        if (GAME_LIBS.contains(name)) return true;
        return name.contains("il2cpp") || name.contains("unity") || name.contains("main");
    }

    private void readMemory() {
        if (libSpinner == null || offsetInput == null || isDestroyed.get()) return;

        Object sel = libSpinner.getSelectedItem();
        CharSequence offText = offsetInput.getText();
        String offStr = offText != null ? offText.toString().trim() : "";

        if (sel == null) {
            safeToast("No library selected");
            return;
        }

        if (offStr.isEmpty()) {
            safeToast("Enter offset");
            return;
        }

        executor.execute(() -> {
            try {
                String libName = sel.toString();

                long base = StealthJNI.findLibBase(libName);
                if (base == 0) {
                    setPreview("Library not loaded: " + libName, 0xFFEF4444);
                    return;
                }

                long offset = parseOffset(offStr);
                if (offset < 0) {
                    setPreview("Invalid offset: [" + offStr + "]", 0xFFEF4444);
                    return;
                }

                long addr = base + offset;
                String hexDump = StealthJNI.readMemoryHexDump(addr, 64);

                if (hexDump == null || hexDump.isEmpty()) {
                    setPreview("Cannot read at 0x" + Long.toHexString(addr), 0xFFEF4444);
                    return;
                }

                handler.post(() -> {
                    if (!isDestroyed.get()) {
                        previewText.setTextColor(0xFF10B981);
                        previewText.setText("0x" + Long.toHexString(addr) + ":\n" + hexDump);
                    }
                });
            } catch (Exception e) {
                setPreview("Error: " + e.getMessage(), 0xFFEF4444);
            }
        });
    }

    private long parseOffset(String str) {
        if (str == null || str.isEmpty()) return -1;

        str = str.trim();

        boolean isHex = str.startsWith("0x") || str.startsWith("0X");

        str = str.replace("0x", "").replace("0X", "").replace(" ", "").replace("_", "");

        if (str.isEmpty()) return -1;

        try {
            if (isHex) {
                return Long.parseLong(str, 16);
            } else {
                return Long.parseLong(str);
            }
        } catch (NumberFormatException e) {
            try {
                return Long.parseLong(str, 16);
            } catch (NumberFormatException e2) {
                return -1;
            }
        }
    }

    private void setPreview(String text, int color) {
        handler.post(() -> {
            if (previewText != null && !isDestroyed.get()) {
                previewText.setTextColor(color);
                previewText.setText(text);
            }
        });
    }

    private void scanPattern() {
        Object sel = libSpinner.getSelectedItem();
        String pattern = scanPatternInput.getText().toString().trim();

        if (sel == null || pattern.isEmpty()) {
            safeToast("Enter pattern to scan");
            return;
        }

        safeToast("Scanning...");

        executor.execute(() -> {
            try {
                long[] results = StealthJNI.scanPatternAll(sel.toString(), pattern, MAX_SCAN_RESULTS);

                handler.post(() -> {
                    if (isDestroyed.get()) return;

                    if (results == null || results.length == 0) {
                        scanResultText.setText("Pattern not found");
                        scanResultText.setTextColor(0xFFEF4444);
                    } else {
                        StringBuilder sb = new StringBuilder("Found " + results.length + " results:\n");
                        for (int i = 0; i < Math.min(results.length, 5); i++) {
                            sb.append("0x").append(Long.toHexString(results[i])).append("\n");
                        }
                        if (results.length > 5) sb.append("...");

                        scanResultText.setTextColor(0xFF10B981);
                        scanResultText.setText(sb.toString());

                        if (results.length > 0) {
                            offsetInput.setText("0x" + Long.toHexString(results[0]));
                        }
                    }
                });
            } catch (Exception e) {
                safeToast("Scan error");
            }
        });
    }

    private void addPatch() {
        Object sel = libSpinner.getSelectedItem();
        String offStr = offsetInput.getText().toString().trim();
        String hex = hexInput.getText().toString().trim();
        String desc = descInput.getText().toString().trim();
        boolean freeze = freezeCheckBox.isChecked();

        if (sel == null || offStr.isEmpty() || hex.isEmpty()) {
            safeToast("Fill all required fields");
            return;
        }

        executor.execute(() -> {
            try {
                String libName = sel.toString();
                long offset = parseOffset(offStr);
                if (offset < 0) {
                    safeToast("Invalid offset: [" + offStr + "]");
                    return;
                }

                long base = StealthJNI.findLibBase(libName);
                if (base == 0) {
                    safeToast("Library not loaded: " + libName);
                    return;
                }

                String cleanHex = hex.replace(" ", "");
                if (cleanHex.length() % 2 != 0) {
                    safeToast("Invalid hex: odd length");
                    return;
                }

                try {
                    for (int i = 0; i < cleanHex.length(); i += 2) {
                        Integer.parseInt(cleanHex.substring(i, i + 2), 16);
                    }
                } catch (NumberFormatException e) {
                    safeToast("Invalid hex characters");
                    return;
                }

                int id = StealthJNI.createPatch(libName, offset, hex, desc.isEmpty() ? "Patch" : desc);

                if (id >= 0) {
                    boolean applied = StealthJNI.applyPatch(id);

                    if (applied && freeze) {
                        startFreeze(id);
                    }

                    handler.post(() -> {
                        refreshPatchList();
                        safeToast(freeze ? "Patch added & frozen!" : "Patch applied!");
                        hexInput.setText("");
                        descInput.setText("");
                    });
                } else {
                    String errMsg;
                    long finalAddr = base + offset;
                    switch (id) {
                        case -2: errMsg = "Invalid offset (negative)"; break;
                        case -3: errMsg = "Max patches reached"; break;
                        case -4: errMsg = "Library not found: " + libName; break;
                        case -5: errMsg = "Hex too long"; break;
                        case -6: errMsg = "Invalid hex format"; break;
                        case -7: errMsg = "Hex too large"; break;
                        case -8: errMsg = "Address overflow"; break;
                        case -9: errMsg = "Offset out of range: 0x" + Long.toHexString(offset); break;
                        case -10: errMsg = "Memory not readable: 0x" + Long.toHexString(finalAddr); break;
                        case -11: errMsg = "Read failed: 0x" + Long.toHexString(finalAddr); break;
                        default: errMsg = "Unknown error (" + id + ")"; break;
                    }
                    safeToast(errMsg);
                }
            } catch (Exception e) {
                safeToast("Error: " + e.getMessage());
            }
        });
    }

    private static class FreezeRunnable implements Runnable {
        private final WeakReference<PatchOffsetDialog> ref;
        private final int patchId;

        FreezeRunnable(PatchOffsetDialog dialog, int patchId) {
            this.ref = new WeakReference<>(dialog);
            this.patchId = patchId;
        }

        @Override
        public void run() {
            PatchOffsetDialog dialog = ref.get();
            if (dialog == null) return;

            if (dialog.isDestroyed.get()) return;
            if (!dialog.freezeStates.getOrDefault(patchId, false)) return;

            StealthJNI.applyPatch(patchId);

            if (!dialog.isDestroyed.get() && dialog.freezeStates.getOrDefault(patchId, false)) {
                dialog.handler.postDelayed(this, 100);
            }
        }
    }

    private void startFreeze(int patchId) {
        freezeStates.put(patchId, true);

        Runnable r = new FreezeRunnable(this, patchId);
        freezeRunnables.put(patchId, r);
        handler.post(r);
    }

    private void stopFreeze(int patchId) {
        freezeStates.put(patchId, false);
        freezeRunnables.remove(patchId);
    }

    private void stopAllFreezes() {
        for (int id : new ArrayList<>(freezeStates.keySet())) {
            stopFreeze(id);
        }
    }

    private void batchEnable(boolean enable) {
        executor.execute(() -> {
            try {
                int[] ids = StealthJNI.getAllPatchIds();
                if (ids == null) return;

                for (int id : ids) {
                    if (enable) {
                        StealthJNI.applyPatch(id);
                    } else {
                        StealthJNI.restorePatch(id);
                        stopFreeze(id);
                    }
                }

                handler.post(() -> {
                    refreshPatchList();
                    safeToast(enable ? "All enabled" : "All disabled");
                });
            } catch (Exception ignored) {}
        });
    }

    private void exportPatches() {
        new DumpLibDialog.ExportPathPickerDialog(context,
            android.os.Environment.getExternalStorageDirectory() + "/Palladium/Patches",
            new DumpLibDialog.PathSelectCallback() {
                public void onPathSelected(String path) {
                    doExport(path);
                }
                public void onCancelled() {}
            }).show();
    }

    private void doExport(String dir) {
        executor.execute(() -> {
            try {
                String json = StealthJNI.exportPatchesJson();
                if (json == null || json.isEmpty()) {
                    safeToast("No patches to export");
                    return;
                }

                File file = new File(dir, "patches_" + System.currentTimeMillis() + ".json");
                file.getParentFile().mkdirs();

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json);
                }

                safeToast("Exported to " + file.getName());
            } catch (Exception e) {
                safeToast("Export failed");
            }
        });
    }

    private void importPatches() {
        new DumpLibDialog.ExportPathPickerDialog(context,
            android.os.Environment.getExternalStorageDirectory() + "/Palladium/Patches",
            new DumpLibDialog.PathSelectCallback() {
                public void onPathSelected(String path) {
                    showImportFilePicker(path);
                }
                public void onCancelled() {}
            }).show();
    }

    private void showImportFilePicker(String dir) {
        File[] files = new File(dir).listFiles((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            safeToast("No patch files found");
            return;
        }

        final String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) names[i] = files[i].getName();

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Select patch file");
        builder.setItems(names, (d, which) -> doImport(files[which].getAbsolutePath()));
        builder.show();
    }

    private void doImport(String filePath) {
        executor.execute(() -> {
            try (FileReader reader = new FileReader(filePath)) {
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[1024];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, read);
                }

                int count = StealthJNI.importPatchesJson(sb.toString());
                handler.post(() -> {
                    refreshPatchList();
                    safeToast("Imported " + count + " patches");
                });
            } catch (Exception e) {
                safeToast("Import failed");
            }
        });
    }

    private void refreshPatchList() {
        if (isDestroyed.get() || patchListLayout == null) return;

        executor.execute(() -> {
            try {
                String[] patches = StealthJNI.getPatchList();
                handler.post(() -> {
                    if (isDestroyed.get() || patchListLayout == null) return;
                    patchListLayout.removeAllViews();

                    if (patches == null || patches.length == 0) {
                        addEmptyView();
                        return;
                    }

                    String currentLib = "";
                    for (String patch : patches) {
                        if (patch == null) continue;
                        String[] parts = patch.split("\\|", -1);
                        if (parts.length < 6) continue;

                        if (!parts[1].equals(currentLib)) {
                            currentLib = parts[1];
                            addLibHeader(currentLib);
                        }
                        addPatchCard(parts);
                    }
                });
            } catch (Exception ignored) {}
        });
    }

    private void addEmptyView() {
        final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);
        TextView tv = new TextView(context);
        tv.setText("No active patches");
        tv.setTextColor(ThemeConstants.COLOR_TEXT_SECONDARY);
        tv.setTextSize(10);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, (int)(20*d), 0, (int)(20*d));
        patchListLayout.addView(tv);
    }

    private void addLibHeader(String libName) {
        final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);
        TextView tv = new TextView(context);
        tv.setText(libName);
        tv.setTextColor(0xFF888888);
        tv.setTextSize(8);
        tv.setTypeface(FontManager.getFont(context), Typeface.BOLD);
        tv.setPadding(0, (int)(10*d), 0, (int)(2*d));
        patchListLayout.addView(tv);
    }

    private void addPatchCard(String[] parts) {
        final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);

        int id = Integer.parseInt(parts[0]);
        String desc = parts[2];
        String addr = parts[3];
        String bytes = parts[4];
        boolean active = "1".equals(parts[5]);
        boolean frozen = freezeStates.getOrDefault(id, false);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding((int)(8*d), (int)(6*d), (int)(8*d), (int)(6*d));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(active ? 0xFF0F2415 : 0xFF1A1A1A);
        bg.setCornerRadius(8*d);
        bg.setStroke((int)(1*d), active ? 0xFF10B981 : 0xFF333333);
        card.setBackground(bg);
        card.setLayoutParams(createLP(-1, -2, 0, (int)(4*d)));

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);

        View indicator = new View(context);
        indicator.setLayoutParams(new LinearLayout.LayoutParams((int)(3*d), (int)(3*d)));
        GradientDrawable indBg = new GradientDrawable();
        indBg.setShape(GradientDrawable.OVAL);
        indBg.setColor(active ? (frozen ? 0xFFF59E0B : 0xFF10B981) : 0xFF666666);
        indicator.setBackground(indBg);
        header.addView(indicator);

        TextView title = new TextView(context);
        title.setText((frozen ? "❄️ " : "") + desc);
        title.setTextColor(active ? 0xFF10B981 : ThemeConstants.COLOR_TEXT_PRIMARY);
        title.setTextSize(10);
        title.setTypeface(FontManager.getFont(context), Typeface.BOLD);
        title.setPadding((int)(6*d), 0, 0, 0);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(title);

        card.addView(header);

        TextView details = new TextView(context);
        details.setText(addr + " → " + bytes);
        details.setTextColor(ThemeConstants.COLOR_TEXT_SECONDARY);
        details.setTextSize(8);
        details.setTypeface(Typeface.MONOSPACE);
        details.setPadding(0, (int)(3*d), 0, 0);
        card.addView(details);

        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, (int)(6*d), 0, 0);

        actions.addView(createIconButton(active ? "✓" : "✗", active ? 0xFFEF4444 : 0xFF10B981, d, v -> {
            executor.execute(() -> {
                if (active) {
                    StealthJNI.restorePatch(id);
                    stopFreeze(id);
                } else {
                    StealthJNI.applyPatch(id);
                    if (freezeStates.getOrDefault(id, false)) startFreeze(id);
                }
                handler.post(this::refreshPatchList);
            });
        }));

        actions.addView(createSpacer(d));

        actions.addView(createIconButton("❄️", frozen ? 0xFF3B82F6 : 0xFF666666, d, v -> {
            if (frozen) {
                stopFreeze(id);
                StealthJNI.restorePatch(id);
            } else {
                StealthJNI.applyPatch(id);
                startFreeze(id);
            }
            handler.post(this::refreshPatchList);
        }));

        actions.addView(createSpacer(d));

        actions.addView(createIconButton("📋", 0xFF888888, d, v -> {
            ClipboardManager cb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) {
                cb.setPrimaryClip(ClipData.newPlainText("Address", addr.replace("0x", "")));
                safeToast("Copied");
            }
        }));

        actions.addView(createSpacer(d));

        actions.addView(createIconButton("🗑️", 0xFFEF4444, d, v -> {
            executor.execute(() -> {
                stopFreeze(id);
                StealthJNI.removePatch(id);
                handler.post(this::refreshPatchList);
            });
        }));

        card.addView(actions);
        patchListLayout.addView(card);
    }

    private Button createIconButton(String text, int color, float d, View.OnClickListener listener) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(10);
        btn.setPadding(0, (int)(4*d), 0, (int)(4*d));
        btn.setMinHeight(0);
        btn.setMinimumHeight(0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(4*d);
        btn.setBackground(bg);
        btn.setLayoutParams(new LinearLayout.LayoutParams(0, (int)(32*d), 1));
        btn.setOnClickListener(v -> safeExecute(() -> listener.onClick(v)));
        return btn;
    }

    private void cleanup() {
        isDestroyed.set(true);
        handler.removeCallbacksAndMessages(null);
        executor.shutdown();
        stopAllFreezes();
        freezeRunnables.clear();
        freezeStates.clear();
        if (patchListLayout != null) patchListLayout.removeAllViews();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception ignored) {}
        }
        cleanup();
    }
}
