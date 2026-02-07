package zig.cheat.qq.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import zig.cheat.qq.ui.Utils;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import zig.cheat.qq.jni.Jni;
import zig.cheat.qq.ui.Menu;

public class DumpLibDialog {
    private static final int MAX_LIBS_DISPLAY = 50;
    private static final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicLong counter = new AtomicLong(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "DumpLib-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    });

    private final Context context;
    private final Handler handler;
    private Dialog dialog;
    private LinearLayout contentLayout;
    private TextView statusText;
    private final List<CheckBox> libCheckboxes = new ArrayList<>();
    private final List<String> libNames = new ArrayList<>();
    private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
    private final AtomicInteger selectedCount = new AtomicInteger(0);

    public DumpLibDialog(Context ctx) {
        this.context = ctx != null ? ctx : null;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void show() {
        if (context == null || !(context instanceof Activity) || ((Activity)context).isFinishing()) {
            return;
        }

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        try {
            createDialog();
        } catch (Exception e) {
            safeToast("Dialog error");
        }
    }

    private void createDialog() {
        dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setOnDismissListener(d -> cleanup());

        final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);
        final int width = (int)(520 * d);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding((int)(12*d), (int)(12*d), (int)(12*d), (int)(12*d));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Menu.COLOR_WINDOW_BG);
        bg.setCornerRadius(12*d);
        bg.setStroke((int)(1*d), Menu.COLOR_BORDER);
        root.setBackground(bg);

        TextView title = new TextView(context);
        title.setText("DUMP LIBRARY");
        title.setTextColor(Menu.COLOR_TEXT_PRIMARY);
        title.setTextSize(12);
        title.setTypeface(Utils.getFont(context));
        title.setLetterSpacing(0.1f);
        root.addView(title);

        View div = new View(context);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(-1, (int)(1*d));
        divLp.topMargin = (int)(8*d);
        divLp.bottomMargin = (int)(8*d);
        div.setLayoutParams(divLp);
        div.setBackgroundColor(Menu.COLOR_DIVIDER);
        root.addView(div);

        ScrollView scroll = new ScrollView(context);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, (int)(200*d)));

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(contentLayout);
        root.addView(scroll);

        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams btnRowLp = new LinearLayout.LayoutParams(-1, (int)(36*d));
        btnRowLp.topMargin = (int)(12*d);
        btnRow.setLayoutParams(btnRowLp);

        Button selectAll = createMiniButton("ALL", 0xFF3B82F6, d);
        selectAll.setOnClickListener(v -> safeExecute(this::selectAllLibs));
        btnRow.addView(selectAll);

        btnRow.addView(createSpacer(d));

        Button scanBtn = createMiniButton("SCAN", 0xFF10B981, d);
        scanBtn.setOnClickListener(v -> safeExecute(this::refreshLibList));
        btnRow.addView(scanBtn);

        btnRow.addView(createSpacer(d));

        Button dumpBtn = createMiniButton("DUMP", 0xFFF59E0B, d);
        dumpBtn.setOnClickListener(v -> safeExecute(this::performDump));
        btnRow.addView(dumpBtn);

        root.addView(btnRow);

        Button dumpMeta = createMetaButton(d);
        dumpMeta.setOnClickListener(v -> safeExecute(this::dumpGlobalMetadata));
        root.addView(dumpMeta);

        statusText = new TextView(context);
        statusText.setTextColor(Menu.COLOR_TEXT_SECONDARY);
        statusText.setTextSize(9);
        statusText.setTypeface(Utils.getFont(context));
        statusText.setPadding(0, (int)(8*d), 0, 0);
        root.addView(statusText);

        dialog.setContentView(root);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
        refreshLibList();
    }

    private Button createMiniButton(String text, int color, float d) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(9);
        btn.setTypeface(Utils.getFont(context));
        btn.setAllCaps(false);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(6*d);
        btn.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1);
        btn.setLayoutParams(lp);
        return btn;
    }

    private Button createMetaButton(float d) {
        Button btn = new Button(context);
        btn.setText("DUMP GLOBAL-METADATA");
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(9);
        btn.setTypeface(Utils.getFont(context));
        btn.setAllCaps(false);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF8B5CF6);
        bg.setCornerRadius(6*d);
        btn.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, (int)(32*d));
        lp.topMargin = (int)(8*d);
        btn.setLayoutParams(lp);
        return btn;
    }

    private View createSpacer(float d) {
        View v = new View(context);
        v.setLayoutParams(new LinearLayout.LayoutParams((int)(4*d), 0));
        return v;
    }

    private void safeExecute(Runnable r) {
        if (isDestroyed.get()) return;
        try {
            r.run();
        } catch (Exception e) {
            safeToast("Error");
        }
    }

    private void safeToast(String msg) {
        if (context != null && msg != null && msg.length() < 50) {
            handler.post(() -> {
                if (!isDestroyed.get()) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void safeSetStatus(String msg) {
        if (msg == null || msg.length() > 100) msg = "...";
        final String finalMsg = msg;
        handler.post(() -> {
            if (statusText != null && !isDestroyed.get()) {
                statusText.setText(finalMsg);
            }
        });
    }

    private void selectAllLibs() {
        if (libCheckboxes == null) return;
        for (CheckBox cb : libCheckboxes) {
            if (cb != null) {
                cb.setChecked(true);
            }
        }
    }

    private void refreshLibList() {
        if (isDestroyed.get() || contentLayout == null) return;

        clearLibList();
        safeSetStatus("Scanning...");

        executor.execute(() -> {
            try {
                String[] libs = Jni.getLoadedLibs();
                int count = Jni.getLibCount();

                handler.post(() -> {
                    if (isDestroyed.get() || contentLayout == null) return;

                    if (libs != null && libs.length > 0) {
                        int limit = Math.min(libs.length, MAX_LIBS_DISPLAY);
                        for (int i = 0; i < limit; i++) {
                            if (libs[i] != null) {
                                addLibCheckbox(libs[i], i);
                            }
                        }
                    }
                    safeSetStatus("Found " + count + " libs");
                });
            } catch (Exception e) {
                safeSetStatus("Error scanning");
            }
        });
    }

    private void clearLibList() {
        if (contentLayout == null) return;
        contentLayout.removeAllViews();
        libCheckboxes.clear();
        libNames.clear();
        selectedCount.set(0);
    }

    private void addLibCheckbox(String libInfo, int index) {
        if (libInfo == null || libInfo.length() > 256) return;

        final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);

        String[] parts = libInfo.split("\\|");
        if (parts.length < 2) return;

        String name = parts[0];
        String base = parts[1];

        if (name.length() > 128) name = name.substring(0, 128);
        if (base.length() > 32) base = base.substring(0, 32);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, (int)(2*d), 0, (int)(2*d));

        CheckBox cb = new CheckBox(context);
        cb.setTextColor(Menu.COLOR_TEXT_PRIMARY);
        cb.setTypeface(Utils.getFont(context));
        cb.setTextSize(10);
        cb.setText(name);

        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCount.incrementAndGet();
            } else {
                selectedCount.decrementAndGet();
            }
        });

        libCheckboxes.add(cb);
        libNames.add(name);
        row.addView(cb);

        TextView addr = new TextView(context);
        addr.setText(base);
        addr.setTextColor(Menu.COLOR_TEXT_SECONDARY);
        addr.setTextSize(8);
        addr.setTypeface(Utils.getFont(context));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.leftMargin = (int)(6*d);
        addr.setLayoutParams(lp);
        row.addView(addr);

        contentLayout.addView(row);
    }

    private void performDump() {
        if (libCheckboxes == null || libCheckboxes.isEmpty()) {
            safeToast("No libraries");
            return;
        }

        int selected = selectedCount.get();
        if (selected == 0) {
            safeToast("Select at least one");
            return;
        }

        if (selected > 10) {
            safeToast("Max 10 libs at once");
            return;
        }

        List<Integer> selectedIndices = new ArrayList<>();
        for (int i = 0; i < libCheckboxes.size(); i++) {
            CheckBox cb = libCheckboxes.get(i);
            if (cb != null && cb.isChecked()) {
                selectedIndices.add(i);
            }
        }

        String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Palladium/Dumps";

        new ExportPathPickerDialog(context, defaultPath, new PathSelectCallback() {
            public void onPathSelected(String outPath) {
                executeDump(selectedIndices, outPath);
            }

            public void onCancelled() {}
        }).show();
    }

    private void executeDump(List<Integer> indices, String outPath) {
        safeSetStatus("Dumping " + indices.size() + " libs...");

        executor.execute(() -> {
            int success = 0;
            for (int idx : indices) {
                if (idx >= 0 && idx < libNames.size()) {
                    try {
                        if (Jni.dumpLib(idx, outPath)) {
                            success++;
                        }
                    } catch (Exception ignored) {}
                }
            }
            final int s = success;
            safeSetStatus("Dumped: " + s + "/" + indices.size());
            safeToast("Saved to: " + outPath);
        });
    }

    private void dumpGlobalMetadata() {
        String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Palladium/Dumps";

        new ExportPathPickerDialog(context, defaultPath, new PathSelectCallback() {
            public void onPathSelected(String outPath) {
                executeDumpMetadata(outPath);
            }

            public void onCancelled() {}
        }).show();
    }

    private void executeDumpMetadata(String outPath) {
        safeSetStatus("Dumping metadata...");

        executor.execute(() -> {
            try {
                boolean success = Jni.dumpGlobalMetadata(outPath);
                if (success) {
                    safeSetStatus("Metadata dumped!");
                    safeToast("Success!");
                } else {
                    safeSetStatus("Failed (not found)");
                }
            } catch (Exception e) {
                safeSetStatus("Error");
            }
        });
    }

    private void cleanup() {
        isDestroyed.set(true);
        handler.removeCallbacksAndMessages(null);
        executor.shutdown();
        if (libCheckboxes != null) {
            for (CheckBox cb : libCheckboxes) {
                if (cb != null) {
                    cb.setOnCheckedChangeListener(null);
                }
            }
            libCheckboxes.clear();
        }
        if (libNames != null) {
            libNames.clear();
        }
        if (contentLayout != null) {
            contentLayout.removeAllViews();
        }
        contentLayout = null;
        statusText = null;
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception ignored) {}
        }
        cleanup();
    }

    public interface PathSelectCallback {
        void onPathSelected(String path);
        void onCancelled();
    }

    public static class ExportPathPickerDialog {
        private final Context context;
        private final Handler handler;
        private final String initialPath;
        private final PathSelectCallback callback;
        private Dialog dialog;
        private LinearLayout folderList;
        private TextView currentPathText;
        private HorizontalScrollView breadcrumbScroll;
        private LinearLayout breadcrumbLayout;
        private File currentDirectory;
        private final AtomicBoolean isDestroyed = new AtomicBoolean(false);

        public ExportPathPickerDialog(Context ctx, String initPath, PathSelectCallback cb) {
            this.context = ctx;
            this.handler = new Handler(Looper.getMainLooper());
            this.initialPath = initPath;
            this.callback = cb;

            if (initPath != null && !initPath.isEmpty()) {
                File init = new File(initPath);
                if (init.exists() && init.isDirectory() && init.canWrite()) {
                    currentDirectory = init;
                }
            }

            if (currentDirectory == null) {
                currentDirectory = Environment.getExternalStorageDirectory();
            }
        }

        public void show() {
            if (context == null || !(context instanceof Activity) || ((Activity)context).isFinishing()) {
                return;
            }

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }

            createDialog();
        }

        private void createDialog() {
            dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setOnDismissListener(d -> {
                if (callback != null && !isDestroyed.get()) {
                    callback.onCancelled();
                }
                cleanup();
            });

            final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);
            final int width = (int)(340 * d);

            LinearLayout root = new LinearLayout(context);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding((int)(16*d), (int)(16*d), (int)(16*d), (int)(16*d));

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Menu.COLOR_WINDOW_BG);
            bg.setCornerRadius(16*d);
            bg.setStroke((int)(1*d), Menu.COLOR_BORDER);
            root.setBackground(bg);

            TextView title = new TextView(context);
            title.setText("SELECT EXPORT PATH");
            title.setTextColor(Menu.COLOR_TEXT_PRIMARY);
            title.setTextSize(14);
            title.setTypeface(Utils.getFont(context));
            title.setLetterSpacing(0.08f);
            root.addView(title);

            View div1 = createDivider(d);
            div1.setPadding(0, (int)(12*d), 0, (int)(8*d));
            root.addView(div1);

            breadcrumbScroll = new HorizontalScrollView(context);
            breadcrumbScroll.setHorizontalScrollBarEnabled(false);
            breadcrumbScroll.setPadding(0, 0, 0, (int)(8*d));

            breadcrumbLayout = new LinearLayout(context);
            breadcrumbLayout.setOrientation(LinearLayout.HORIZONTAL);
            breadcrumbLayout.setGravity(Gravity.CENTER_VERTICAL);
            breadcrumbScroll.addView(breadcrumbLayout);
            root.addView(breadcrumbScroll);

            currentPathText = new TextView(context);
            currentPathText.setTextColor(Menu.COLOR_TEXT_SECONDARY);
            currentPathText.setTextSize(10);
            currentPathText.setTypeface(Utils.getFont(context));
            currentPathText.setPadding(0, 0, 0, (int)(8*d));
            root.addView(currentPathText);

            View div2 = createDivider(d);
            div2.setPadding(0, 0, 0, (int)(8*d));
            root.addView(div2);

            ScrollView scroll = new ScrollView(context);
            scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, (int)(240*d)));

            folderList = new LinearLayout(context);
            folderList.setOrientation(LinearLayout.VERTICAL);
            scroll.addView(folderList);
            root.addView(scroll);

            LinearLayout btnRow = new LinearLayout(context);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams btnRowLp = new LinearLayout.LayoutParams(-1, (int)(40*d));
            btnRowLp.topMargin = (int)(12*d);
            btnRow.setLayoutParams(btnRowLp);

            Button newFolderBtn = createActionButton("NEW FOLDER", 0xFF3B82F6, d);
            newFolderBtn.setOnClickListener(v -> showCreateFolderDialog());
            btnRow.addView(newFolderBtn);

            btnRow.addView(createSpacer(d));

            Button cancelBtn = createActionButton("CANCEL", 0xFF6B7280, d);
            cancelBtn.setOnClickListener(v -> dismiss());
            btnRow.addView(cancelBtn);

            btnRow.addView(createSpacer(d));

            Button selectBtn = createActionButton("SELECT", 0xFF10B981, d);
            selectBtn.setOnClickListener(v -> confirmSelection());
            btnRow.addView(selectBtn);

            root.addView(btnRow);

            dialog.setContentView(root);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.show();
            refreshFolderList();
        }

        private Button createActionButton(String text, int color, float d) {
            Button btn = new Button(context);
            btn.setText(text);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(11);
            btn.setTypeface(Utils.getFont(context));
            btn.setAllCaps(false);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(color);
            bg.setCornerRadius(8*d);
            btn.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1);
            btn.setLayoutParams(lp);
            return btn;
        }

        private View createDivider(float d) {
            View v = new View(context);
            v.setLayoutParams(new LinearLayout.LayoutParams(-1, (int)(1*d)));
            v.setBackgroundColor(Menu.COLOR_DIVIDER);
            return v;
        }

        private View createSpacer(float d) {
            View v = new View(context);
            v.setLayoutParams(new LinearLayout.LayoutParams((int)(8*d), 0));
            return v;
        }

        private void refreshFolderList() {
            if (isDestroyed.get() || folderList == null) return;

            folderList.removeAllViews();
            updateBreadcrumb();
            updateCurrentPathText();

            if (currentDirectory == null || !currentDirectory.exists()) {
                return;
            }

            File[] files = currentDirectory.listFiles();
            if (files == null) {
                return;
            }

            List<File> directories = new ArrayList<>();
            for (File f : files) {
                if (f.isDirectory() && !f.isHidden() && f.canRead()) {
                    directories.add(f);
                }
            }

            Collections.sort(directories, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

            if (currentDirectory.getParentFile() != null && currentDirectory.getParentFile().canRead()) {
                addFolderItem("..", true, v -> navigateUp());
            }

            for (File dir : directories) {
                addFolderItem(dir.getName(), false, v -> navigateTo(dir));
            }

            if (directories.isEmpty() && currentDirectory.getParentFile() == null) {
                final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);
                TextView empty = new TextView(context);
                empty.setText("No subdirectories");
                empty.setTextColor(Menu.COLOR_TEXT_SECONDARY);
                empty.setTextSize(12);
                empty.setTypeface(Utils.getFont(context));
                empty.setPadding(0, (int)(20*d), 0, 0);
                empty.setGravity(Gravity.CENTER);
                folderList.addView(empty);
            }
        }

        private void addFolderItem(String name, boolean isParent, View.OnClickListener clickListener) {
            final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding((int)(12*d), (int)(10*d), (int)(12*d), (int)(10*d));
            row.setClickable(true);
            row.setFocusable(true);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0x00FFFFFF);
            row.setBackground(bg);

            row.setOnClickListener(clickListener);

            row.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        bg.setColor(0x22FFFFFF);
                        row.invalidate();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        bg.setColor(0x00FFFFFF);
                        row.invalidate();
                        break;
                }
                return false;
            });

            TextView icon = new TextView(context);
            icon.setText(isParent ? "⬆" : "📁");
            icon.setTextSize(18);
            icon.setPadding(0, 0, (int)(12*d), 0);
            row.addView(icon);

            TextView label = new TextView(context);
            label.setText(name);
            label.setTextColor(Menu.COLOR_TEXT_PRIMARY);
            label.setTextSize(13);
            label.setTypeface(Utils.getFont(context));
            row.addView(label);

            folderList.addView(row);

            View divider = new View(context);
            divider.setLayoutParams(new LinearLayout.LayoutParams(-1, (int)(1*d)));
            divider.setBackgroundColor(0x11FFFFFF);
            folderList.addView(divider);
        }

        private void updateBreadcrumb() {
            if (breadcrumbLayout == null) return;

            breadcrumbLayout.removeAllViews();
            final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);

            File temp = currentDirectory;
            List<String> parts = new ArrayList<>();

            while (temp != null && !temp.getPath().equals("/")) {
                parts.add(0, temp.getName());
                temp = temp.getParentFile();
            }

            if (parts.isEmpty()) parts.add("Root");

            int count = 0;
            for (String part : parts) {
                if (count > 0) {
                    TextView sep = new TextView(context);
                    sep.setText(" > ");
                    sep.setTextColor(Menu.COLOR_TEXT_SECONDARY);
                    sep.setTextSize(11);
                    sep.setTypeface(Utils.getFont(context));
                    breadcrumbLayout.addView(sep);
                }

                final int index = count;
                TextView tv = new TextView(context);
                tv.setText(part);
                tv.setTextColor(count == parts.size() - 1 ? 0xFF10B981 : Menu.COLOR_TEXT_SECONDARY);
                tv.setTextSize(11);
                tv.setTypeface(Utils.getFont(context));
                tv.setPadding((int)(4*d), (int)(2*d), (int)(4*d), (int)(2*d));

                if (count < parts.size() - 1) {
                    tv.setClickable(true);
                    tv.setOnClickListener(v -> navigateToBreadcrumb(index));
                }

                breadcrumbLayout.addView(tv);
                count++;
            }

            handler.post(() -> breadcrumbScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT));
        }

        private void updateCurrentPathText() {
            if (currentPathText != null) {
                String path = currentDirectory != null ? currentDirectory.getAbsolutePath() : "";
                if (path.length() > 50) {
                    path = "..." + path.substring(path.length() - 47);
                }
                currentPathText.setText(path);
            }
        }

        private void navigateTo(File dir) {
            if (dir != null && dir.exists() && dir.isDirectory() && dir.canRead()) {
                currentDirectory = dir;
                refreshFolderList();
            }
        }

        private void navigateUp() {
            if (currentDirectory != null && currentDirectory.getParentFile() != null) {
                navigateTo(currentDirectory.getParentFile());
            }
        }

        private void navigateToBreadcrumb(int index) {
            File temp = currentDirectory;
            int steps = 0;

            while (temp != null && steps < (getBreadcrumbDepth() - index - 1)) {
                temp = temp.getParentFile();
                steps++;
            }

            if (temp != null && temp.exists()) {
                navigateTo(temp);
            }
        }

        private int getBreadcrumbDepth() {
            int depth = 0;
            File temp = currentDirectory;
            while (temp != null && !temp.getPath().equals("/")) {
                depth++;
                temp = temp.getParentFile();
            }
            return depth;
        }

        private void showCreateFolderDialog() {
            if (isDestroyed.get() || currentDirectory == null || !currentDirectory.canWrite()) {
                return;
            }

            final float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);

            Dialog newFolderDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
            newFolderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding((int)(20*d), (int)(20*d), (int)(20*d), (int)(20*d));

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Menu.COLOR_WINDOW_BG);
            bg.setCornerRadius(12*d);
            bg.setStroke((int)(1*d), Menu.COLOR_BORDER);
            layout.setBackground(bg);

            TextView title = new TextView(context);
            title.setText("New Folder Name");
            title.setTextColor(Menu.COLOR_TEXT_PRIMARY);
            title.setTextSize(14);
            title.setTypeface(Utils.getFont(context));
            title.setPadding(0, 0, 0, (int)(12*d));
            layout.addView(title);

            EditText input = new EditText(context);
            input.setHint("folder_name");
            input.setTextColor(Menu.COLOR_TEXT_PRIMARY);
            input.setHintTextColor(Menu.COLOR_TEXT_SECONDARY);
            input.setTextSize(13);
            input.setTypeface(Utils.getFont(context));
            input.setBackgroundColor(0x22FFFFFF);
            input.setPadding((int)(12*d), (int)(8*d), (int)(12*d), (int)(8*d));
            layout.addView(input);

            LinearLayout btnRow = new LinearLayout(context);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setPadding(0, (int)(16*d), 0, 0);

            Button cancel = new Button(context);
            cancel.setText("Cancel");
            cancel.setTextColor(Menu.COLOR_TEXT_PRIMARY);
            cancel.setBackgroundColor(0x00FFFFFF);
            cancel.setOnClickListener(v -> newFolderDialog.dismiss());
            btnRow.addView(cancel);

            Button create = new Button(context);
            create.setText("Create");
            create.setTextColor(0xFF10B981);
            create.setBackgroundColor(0x00FFFFFF);
            create.setOnClickListener(v -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.contains("/") || name.contains("\\")) {
                    return;
                }

                File newDir = new File(currentDirectory, name);
                if (newDir.exists()) {
                    return;
                }

                if (newDir.mkdir()) {
                    navigateTo(newDir);
                    newFolderDialog.dismiss();
                }
            });
            btnRow.addView(create);

            layout.addView(btnRow);
            newFolderDialog.setContentView(layout);
            newFolderDialog.show();
        }

        private void confirmSelection() {
            if (currentDirectory == null || !currentDirectory.exists() || !currentDirectory.canWrite()) {
                return;
            }

            isDestroyed.set(true);
            if (callback != null) {
                callback.onPathSelected(currentDirectory.getAbsolutePath());
            }
            dismiss();
        }

        private void cleanup() {
            isDestroyed.set(true);
            if (folderList != null) {
                folderList.removeAllViews();
            }
            folderList = null;
            currentPathText = null;
            breadcrumbLayout = null;
            breadcrumbScroll = null;
            currentDirectory = null;
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
}
