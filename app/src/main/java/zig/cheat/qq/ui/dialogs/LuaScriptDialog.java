package zig.cheat.qq.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import zig.cheat.qq.jni.Jni;
import zig.cheat.qq.ui.Menu;
import zig.cheat.qq.ui.Utils;

public class LuaScriptDialog {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String EXAMPLE_SCRIPT = 
        "-- Palladium Lua Engine Script\n" +
        "palladium.Toast(\"palladium...\")\n" +
        "\n" +
        "local libName = \"libil2cpp.so\"\n" +
        "local base = palladium.FindLib(libName)\n" +
        "\n" +
        "if base > 0 then\n" +
        "    palladium.Toast(\"Base Found: \" .. string.format(\"%X\", base))\n" +
        "    \n" +
        "    -- Example: Read Pointer Chain\n" +
        "    -- local hpAddr = palladium.Pointer(base, 0x123456, 0x18)\n" +
        "    -- palladium.WriteInt(hpAddr, 999)\n" +
        "else\n" +
        "    palladium.Toast(\"Library not found\")\n" +
        "end";

    private final Context context;
    private final Handler handler;
    private Dialog dialog;
    private EditText scriptInput;
    private TextView statusText;
    private Button runButton;
    private final AtomicBoolean isDestroyed = new AtomicBoolean(false);

    public LuaScriptDialog(Context ctx) {
        this.context = ctx;
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
        final int width = (int)(380 * d);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding((int)(12 * d), (int)(12 * d), (int)(12 * d), (int)(12 * d));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Menu.COLOR_WINDOW_BG);
        bg.setCornerRadius(12 * d);
        bg.setStroke((int)(1 * d), Menu.COLOR_BORDER);
        root.setBackground(bg);

        TextView title = new TextView(context);
        title.setText("LUA SCRIPT ENGINE");
        title.setTextColor(Menu.COLOR_TEXT_PRIMARY);
        title.setTextSize(11);
        title.setTypeface(Utils.getFont(context));
        title.setLetterSpacing(0.1f);
        root.addView(title);

        View div = new View(context);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(-1, (int)(1 * d));
        divLp.topMargin = (int)(8 * d);
        divLp.bottomMargin = (int)(8 * d);
        div.setLayoutParams(divLp);
        div.setBackgroundColor(Menu.COLOR_DIVIDER);
        root.addView(div);

        ScrollView scroll = new ScrollView(context);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(-1, (int)(180 * d));
        scroll.setLayoutParams(scrollLp);

        scriptInput = new EditText(context);
        scriptInput.setText(EXAMPLE_SCRIPT);
        scriptInput.setTextColor(Menu.COLOR_TEXT_PRIMARY);
        scriptInput.setTextSize(10);
        scriptInput.setTypeface(Typeface.MONOSPACE);
        scriptInput.setBackgroundColor(0xFF1A1A1A);
        scriptInput.setPadding((int)(8 * d), (int)(8 * d), (int)(8 * d), (int)(8 * d));
        scriptInput.setGravity(Gravity.TOP | Gravity.START);
        scriptInput.setMinLines(8);
        scroll.addView(scriptInput);
        root.addView(scroll);

        runButton = createRunButton(d);
        runButton.setOnClickListener(v -> executeScript());
        LinearLayout.LayoutParams runBtnLp = new LinearLayout.LayoutParams(-1, (int)(36 * d));
        runBtnLp.topMargin = (int)(8 * d);
        runButton.setLayoutParams(runBtnLp);
        root.addView(runButton);

        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams btnRowLp = new LinearLayout.LayoutParams(-1, (int)(32 * d));
        btnRowLp.topMargin = (int)(6 * d);
        btnRow.setLayoutParams(btnRowLp);

        Button clearBtn = createSecondaryButton("CLEAR", 0xFF6B7280, d);
        clearBtn.setOnClickListener(v -> scriptInput.setText(""));
        btnRow.addView(clearBtn);

        btnRow.addView(createSpacer(d));

        Button exampleBtn = createSecondaryButton("LOAD EXAMPLE", 0xFF3B82F6, d);
        exampleBtn.setOnClickListener(v -> scriptInput.setText(EXAMPLE_SCRIPT));
        btnRow.addView(exampleBtn);

        root.addView(btnRow);

        statusText = new TextView(context);
        statusText.setText("Ready to execute");
        statusText.setTextColor(Menu.COLOR_TEXT_SECONDARY);
        statusText.setTextSize(10);
        statusText.setTypeface(Utils.getFont(context));
        statusText.setPadding(0, (int)(6 * d), 0, 0);
        root.addView(statusText);

        dialog.setContentView(root);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    private Button createRunButton(float d) {
        Button btn = new Button(context);
        btn.setText("▶ RUN SCRIPT");
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(11);
        btn.setTypeface(Utils.getFont(context), Typeface.BOLD);
        btn.setAllCaps(false);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF22C55E);
        bg.setCornerRadius(8 * d);
        btn.setBackground(bg);
        btn.setElevation(2 * d);
        return btn;
    }

    private Button createSecondaryButton(String text, int color, float d) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(9);
        btn.setTypeface(Utils.getFont(context));
        btn.setAllCaps(false);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(4 * d);
        btn.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1);
        btn.setLayoutParams(lp);
        return btn;
    }

    private View createSpacer(float d) {
        View v = new View(context);
        v.setLayoutParams(new LinearLayout.LayoutParams((int)(8 * d), 0));
        return v;
    }

    private void executeScript() {
        if (isDestroyed.get() || scriptInput == null) return;
        
        String script = scriptInput.getText().toString();
        if (script.trim().isEmpty()) {
            safeToast("Empty script");
            safeSetStatus("Error: Empty script", true);
            return;
        }

        safeSetStatus("Running...", false);
        setRunButtonState(false);
        
        executor.execute(() -> {
            String result = null;
            boolean error = false;
            
            try {
                result = Jni.executeLuaScript(script);
            } catch (UnsatisfiedLinkError e) {
                result = "Error: Native method not found";
                error = true;
            } catch (Exception e) {
                result = "Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown");
                error = true;
            } catch (Throwable t) {
                result = "Error: Native crash prevented";
                error = true;
            }
            
            final String finalResult = result != null ? result : "Error: null result";
            final boolean isError = error || finalResult.startsWith("Error");
            
            handler.post(() -> {
                if (isDestroyed.get()) return;
                
                setRunButtonState(true);
                if (isError) {
                    safeSetStatus(finalResult, true);
                    safeToast("Script failed");
                } else {
                    safeSetStatus(finalResult, false);
                }
            });
        });
    }

    private void setRunButtonState(boolean enabled) {
        if (runButton == null) return;
        runButton.setEnabled(enabled);
        float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(4 * d);
        if (enabled) {
            bg.setColor(0xFF22C55E);
            runButton.setText("▶ RUN SCRIPT");
        } else {
            bg.setColor(0xFF16A34A);
            runButton.setText("⏳ RUNNING...");
        }
        runButton.setBackground(bg);
    }

    private void safeToast(String msg) {
        if (context != null && msg != null && msg.length() < 100) {
            handler.post(() -> {
                if (!isDestroyed.get()) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void safeSetStatus(String msg, boolean isError) {
        if (msg == null) msg = "";
        if (msg.length() > 250) msg = msg.substring(0, 250) + "...";
        final String finalMsg = msg;
        handler.post(() -> {
            if (statusText != null && !isDestroyed.get()) {
                statusText.setText(finalMsg);
                statusText.setTextColor(isError ? 0xFFEF4444 : 0xFF22C55E);
            }
        });
    }

    private void cleanup() {
        isDestroyed.set(true);
        handler.removeCallbacksAndMessages(null);
        try {
            Jni.cleanupLua();
        } catch (Exception ignored) {}
        scriptInput = null;
        statusText = null;
        runButton = null;
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
