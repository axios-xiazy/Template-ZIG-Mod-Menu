package zig.cheat.qq.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zig.cheat.qq.native_bridge.StealthJNI;

public class SignatureManagerDialog {

    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Dialog dialog;
    private TextView statusText;
    private Switch bypassSwitch;
    private EditText sigInput;
    private EditText pathInput;
    private CheckBox autoExtractCheck;
    private TextView logText;

    private static final String DEFAULT_SIGNATURE =
        "MIIBnTCCAQagAwIBAgIEYyV6PzANBgkqhkiG9w0BAQUFADASMRAwDgYDVQQKDAdUZW5jZW50MCAXDTE2MDMxNTA5MDQxM1oYDzIwNjYwMzAzMDkwNDEzWjASMRAwDgYDVQQKDAdUZW5jZW50MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1PmQPhu41bfpZ6hRZxGAlC4g9leU5qIh9z6sw2DbxDlzNkMc6K5MexISK/Qw3OTuHozqKZunl9WYX0f6GzicQI4NDleierF7X3R8LOWTJGX1tUxxi+zPJ244kTqO+tdoKpzcBvruq3pKAr/i4sq8pTWXOJZocgYQv25xcof6hRwIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAJmjJ9FJIgrBmf7TanxvwCYjf5WJtHO58kVCZiWfepJxixe1cf/qfNTAx+REeBQqwyc71uWvfrICUFTSN5dQXkzRfR9pvoul+BTNa0rp7dRd/jUToiGQf7Lv8QpPXu/kgY38CTqxligjEGh5DMO2FaXRAzQeariMZDjT8C9dlH1V";

    public SignatureManagerDialog(Context ctx) {
        this.context = ctx;
    }

    public void show() {
        if (context == null || !(context instanceof Activity)) return;
        final Activity act = (Activity) context;
        if (act.isFinishing()) return;

        handler.post(() -> {
            try {
                if (dialog != null && dialog.isShowing()) dialog.dismiss();
                createDialog(act);
            } catch (Exception e) {
                showToast("Error: " + e.getMessage());
            }
        });
    }

    private void createDialog(Activity act) {
        dialog = new Dialog(act);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        float d = act.getResources().getDisplayMetrics().density;
        int width = (int)(720 * d);
        int height = (int)(340 * d);
        int padV = (int)(6 * d);
        int padH = (int)(12 * d);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setPadding(padH, padV, padH, padV);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FF252525"));
        bg.setCornerRadius(12 * d);
        root.setBackground(bg);

        LinearLayout leftPanel = createLeftPanel(d);
        View divider = createDivider(d);
        ScrollView rightPanel = createLogPanel(d);

        root.addView(leftPanel);
        root.addView(divider);
        root.addView(rightPanel);

        dialog.setContentView(root);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(width, height);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
        logInfo("Signature Manager initialized");
        logInfo("Waiting for user input...");
    }

    private LinearLayout createLeftPanel(float d) {
        LinearLayout panel = new LinearLayout(context);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 0.65f));

        TextView title = new TextView(context);
        title.setText("Signature Manager");
        title.setTextColor(Color.WHITE);
        title.setTextSize(14);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, (int)(8*d));
        panel.addView(title);

        panel.addView(createStatusRow(d));
        panel.addView(createConfigSection(d));
        panel.addView(createButtonSection(d));

        return panel;
    }

    private LinearLayout createStatusRow(float d) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, (int)(8*d));

        bypassSwitch = new Switch(context);
        bypassSwitch.setText("Signature Bypass");
        bypassSwitch.setTextColor(Color.WHITE);
        bypassSwitch.setTextSize(11);
        bypassSwitch.setOnCheckedChangeListener((v, checked) -> {
            if (checked) doApply();
            else doDisable();
        });
        row.addView(bypassSwitch);

        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1f));
        row.addView(spacer);

        statusText = new TextView(context);
        statusText.setText("Standby");
        statusText.setTextColor(Color.GRAY);
        statusText.setTextSize(10);
        statusText.setTypeface(null, Typeface.BOLD);
        row.addView(statusText);

        return row;
    }

    private LinearLayout createConfigSection(float d) {
        LinearLayout section = new LinearLayout(context);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(0, 0, 0, (int)(10*d));

        TextView lbl = new TextView(context);
        lbl.setText("Signature (Base64)");
        lbl.setTextColor(Color.LTGRAY);
        lbl.setTextSize(9);
        lbl.setPadding(0, 0, 0, (int)(3*d));
        section.addView(lbl);

        sigInput = new EditText(context);
        sigInput.setText(DEFAULT_SIGNATURE);
        sigInput.setTextColor(Color.WHITE);
        sigInput.setTextSize(9);
        sigInput.setTypeface(Typeface.MONOSPACE);

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.parseColor("#15FFFFFF"));
        inputBg.setCornerRadius(8 * d);
        sigInput.setBackground(inputBg);
        sigInput.setPadding((int)(10*d), (int)(8*d), (int)(10*d), (int)(8*d));
        sigInput.setLayoutParams(new LinearLayout.LayoutParams(-1, (int)(48*d)));
        sigInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        section.addView(sigInput);

        LinearLayout pathRow = new LinearLayout(context);
        pathRow.setOrientation(LinearLayout.HORIZONTAL);
        pathRow.setGravity(Gravity.CENTER_VERTICAL);
        pathRow.setPadding(0, (int)(8*d), 0, 0);

        pathInput = new EditText(context);
        pathInput.setHint("Fake APK Path");
        pathInput.setTextColor(Color.WHITE);
        pathInput.setHintTextColor(Color.DKGRAY);
        pathInput.setTextSize(10);
        pathInput.setBackground(inputBg);
        pathInput.setPadding((int)(10*d), (int)(8*d), (int)(10*d), (int)(8*d));
        LinearLayout.LayoutParams pathParams = new LinearLayout.LayoutParams(0, -2, 0.7f);
        pathParams.rightMargin = (int)(8*d);
        pathInput.setLayoutParams(pathParams);
        pathRow.addView(pathInput);

        autoExtractCheck = new CheckBox(context);
        autoExtractCheck.setText("Extract");
        autoExtractCheck.setTextColor(Color.LTGRAY);
        autoExtractCheck.setTextSize(9);
        autoExtractCheck.setChecked(true);
        autoExtractCheck.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 0.3f));
        pathRow.addView(autoExtractCheck);

        section.addView(pathRow);

        return section;
    }

    private LinearLayout createButtonSection(float d) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row1 = new LinearLayout(context);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setPadding(0, 0, 0, (int)(8*d));

        int gap = (int)(8 * d);

        row1.addView(createBtn("Apply", 0xFF34C759, d));
        View s1 = new View(context);
        s1.setLayoutParams(new LinearLayout.LayoutParams(gap, 0));
        row1.addView(s1);
        row1.addView(createBtn("Verify", 0xFF007AFF, d));

        LinearLayout row2 = new LinearLayout(context);
        row2.setOrientation(LinearLayout.HORIZONTAL);

        row2.addView(createBtn("Copy", 0xFF5856D6, d));
        View s2 = new View(context);
        s2.setLayoutParams(new LinearLayout.LayoutParams(gap, 0));
        row2.addView(s2);
        row2.addView(createBtn("Reset", 0xFFFF9500, d));

        container.addView(row1);
        container.addView(row2);

        View.OnClickListener applyListener = v -> doApply();
        View.OnClickListener verifyListener = v -> doVerify();
        View.OnClickListener copyListener = v -> doCopy();
        View.OnClickListener resetListener = v -> doReset();

        row1.getChildAt(0).setOnClickListener(applyListener);
        row1.getChildAt(2).setOnClickListener(verifyListener);
        row2.getChildAt(0).setOnClickListener(copyListener);
        row2.getChildAt(2).setOnClickListener(resetListener);

        return container;
    }

    private Button createBtn(String text, int color, float d) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(11);
        btn.setAllCaps(false);
        btn.setMinHeight(0);
        btn.setMinimumHeight(0);
        btn.setPadding(0, (int)(12*d), 0, (int)(12*d));
        btn.setTypeface(null, Typeface.BOLD);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(10 * d);
        btn.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
        btn.setLayoutParams(lp);
        return btn;
    }

    private View createDivider(float d) {
        View v = new View(context);
        v.setLayoutParams(new LinearLayout.LayoutParams((int)(1*d), -1));
        v.setBackgroundColor(Color.parseColor("#30FFFFFF"));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.leftMargin = (int)(12*d);
        lp.rightMargin = (int)(12*d);
        v.setLayoutParams(lp);
        return v;
    }

    private ScrollView createLogPanel(float d) {
        ScrollView scroll = new ScrollView(context);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 0.35f));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#0A0A0A"));
        bg.setCornerRadius(10 * d);
        scroll.setBackground(bg);

        logText = new TextView(context);
        logText.setTextColor(Color.LTGRAY);
        logText.setTextSize(9);
        logText.setPadding((int)(10*d), (int)(10*d), (int)(10*d), (int)(10*d));
        logText.setLineSpacing(4*d, 1.0f);
        logText.setTypeface(Typeface.MONOSPACE);
        scroll.addView(logText);

        return scroll;
    }

    private void log(String message, int color) {
        if (message == null || logText == null) return;
        handler.post(() -> {
            try {
                String time = timeFormat.format(new Date());
                String line = "[" + time + "] " + message;

                SpannableString spannable = new SpannableString(line + "\n");
                spannable.setSpan(new ForegroundColorSpan(color), 0, line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                logText.append(spannable);

                View parent = (View) logText.getParent();
                if (parent instanceof ScrollView) {
                    ((ScrollView) parent).post(() -> {
                        ((ScrollView) parent).fullScroll(ScrollView.FOCUS_DOWN);
                    });
                }
            } catch (Exception ignored) {}
        });
    }

    private void logInfo(String message) {
        log(message, Color.LTGRAY);
    }

    private void logSuccess(String message) {
        log(message, Color.GREEN);
    }

    private void logError(String message) {
        log(message, 0xFFFF6B6B);
    }

    private void logWarn(String message) {
        log(message, 0xFFFFD93D);
    }

    private void logHighlight(String message) {
        log(message, 0xFF4DABF7);
    }

    private String getAppSignature() {
        try {
            String packageName = context.getPackageName();
            PackageManager pm = context.getPackageManager();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                if (pi.signingInfo != null) {
                    Signature[] signatures = pi.signingInfo.getApkContentsSigners();
                    if (signatures != null && signatures.length > 0) {
                        return signatureToBase64(signatures[0]);
                    }
                }
            } else {
                @SuppressLint("PackageManagerGetSignatures")
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (pi.signatures != null && pi.signatures.length > 0) {
                    return signatureToBase64(pi.signatures[0]);
                }
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return "No signature found";
    }

    private String signatureToBase64(Signature signature) {
        return Base64.encodeToString(signature.toByteArray(), Base64.NO_WRAP);
    }

    private String getSignatureHash(Signature signature) {
        try {
            byte[] cert = signature.toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(cert);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "N/A";
        }
    }

    private void doApply() {
        logInfo("Applying signature bypass...");
        String sig = sigInput.getText().toString().trim();
        if (sig.isEmpty()) {
            logError("Error: Signature is empty");
            return;
        }
        executor.execute(() -> {
            try {
                logInfo("Setting custom signature...");
                StealthJNI.setCustomSignature(sig);

                String path = pathInput.getText().toString().trim();
                if (!path.isEmpty()) {
                    logInfo("Setting fake APK path: " + path);
                    StealthJNI.setFakeApkPath(path);
                }

                if (autoExtractCheck.isChecked()) {
                    logInfo("Extracting original APK...");
                    StealthJNI.extractOriginApk();
                }

                logInfo("Initializing bypass...");
                boolean success = StealthJNI.initSignatureBypass();
                handler.post(() -> {
                    if (success) {
                        logSuccess("✓ Bypass activated successfully!");
                        statusText.setText("Active");
                        statusText.setTextColor(Color.GREEN);
                        bypassSwitch.setChecked(true);
                    } else {
                        logError("✗ Activation failed");
                    }
                });
            } catch (Exception e) {
                handler.post(() -> logError("Error: " + e.getMessage()));
            }
        });
    }

    private void doDisable() {
        logWarn("Disabling bypass...");
        executor.execute(() -> {
            try {
                boolean success = StealthJNI.disableBypass();
                handler.post(() -> {
                    if (success) {
                        logWarn("✓ Bypass disabled");
                        statusText.setText("Standby");
                        statusText.setTextColor(Color.GRAY);
                    } else {
                        logError("✗ Disable failed");
                    }
                });
            } catch (Exception e) {
                handler.post(() -> logError("Error: " + e.getMessage()));
            }
        });
    }

    private void doVerify() {
        logInfo("Verifying signature status...");
        executor.execute(() -> {
            try {
                String jniResult = StealthJNI.verifySignatureStatus();

                handler.post(() -> {
                    logHighlight("=== Current App Signature ===");
                    String currentSig = getAppSignature();
                    logInfo("Package: " + context.getPackageName());
                    logInfo("Signature:");

                    if (currentSig.length() > 60) {
                        logInfo(currentSig.substring(0, 60) + "...");
                    } else {
                        logInfo(currentSig);
                    }

                    logHighlight("=== JNI Status ===");
                    logInfo(jniResult != null ? jniResult : "No response from native layer");

                    logHighlight("=== Verification Complete ===");
                });
            } catch (Exception e) {
                handler.post(() -> logError("Error: " + e.getMessage()));
            }
        });
    }

    private void doCopy() {
        String sig = sigInput.getText().toString().trim();
        if (sig.isEmpty()) {
            logError("Nothing to copy");
            return;
        }
        try {
            ClipboardManager cb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) {
                cb.setPrimaryClip(ClipData.newPlainText("Signature", sig));
                logSuccess("Copied to clipboard");
            }
        } catch (Exception e) {
            logError("Error: " + e.getMessage());
        }
    }

    private void doReset() {
        sigInput.setText(DEFAULT_SIGNATURE);
        pathInput.setText("");
        autoExtractCheck.setChecked(true);
        logInfo("Reset to defaults");
    }

    private void showToast(String msg) {
        try {
            if (context != null) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {}
    }

    public void dismiss() {
        handler.removeCallbacksAndMessages(null);
        executor.shutdown();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
