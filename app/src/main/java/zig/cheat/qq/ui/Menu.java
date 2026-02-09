package zig.cheat.qq.ui;

import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import zig.cheat.qq.jni.Jni;
import zig.cheat.qq.ui.Utils;
import zig.cheat.qq.ui.dialogs.DumpLibDialog;
import zig.cheat.qq.ui.dialogs.LuaScriptDialog;
import zig.cheat.qq.ui.dialogs.PatchOffsetDialog;
import zig.cheat.qq.ui.dialogs.SignatureManagerDialog;

public class Menu {
    public static final int COLOR_WINDOW_BG = 0xF0121212;
    public static final int COLOR_SIDEBAR_BG = 0x20121212;
    public static final int COLOR_CARD_BG = 0xFF252525;
    public static final int COLOR_ACCENT_BLUE = 0xFF3B82F6;
    public static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int COLOR_TEXT_SECONDARY = 0xFFA1A1AA;
    public static final int COLOR_DIVIDER = 0xFF333333;
    public static final int COLOR_BORDER = 0xFF404040;
    private static final int TRAFFIC_RED = 0xFFFF5F57;
    private static final int TRAFFIC_YELLOW = 0xFFFEBC2E;
    private static final int TRAFFIC_GREEN = 0xFF28C840;

    private static final TimeInterpolator EASE_IN_OUT = new AccelerateDecelerateInterpolator();
    private static final TimeInterpolator EASE_OUT = new DecelerateInterpolator(1.5f);

    private Context context;
    private WindowManager windowManager;
    private FrameLayout rootContainer;
    private WindowManager.LayoutParams windowParams;
    private LinearLayout menuLayout;
    private LinearLayout islandLayout;
    private boolean isExpanded = true;
    private ValueAnimator toggleAnimator;
    private ValueAnimator pulseAnimator;
    private List<View> trafficLights = new ArrayList<>();
    private Handler blinkHandler = new Handler(Looper.getMainLooper());
    private int blinkIndex = 0;
    private boolean isDestroyed = false;
    private View gestureHandle;
    private WindowManager.LayoutParams handleParams;
    private android.view.GestureDetector gestureDetector;
    private ContentPanel contentPanel;
    private List<LinearLayout> sidebarItems = new ArrayList<>();
    private View selectionPill;
    private int selectedTabIndex = 0;

    private Runnable blinkRunnable;
    
    private void initBlinkRunnable() {
        blinkRunnable = () -> {
            if (!isDestroyed && isExpanded && !trafficLights.isEmpty()) {
                for (int i = 0; i < trafficLights.size(); i++) {
                    View v = trafficLights.get(i);
                    boolean active = (i == blinkIndex);
                    v.animate()
                        .alpha(active ? 1.0f : 0.35f)
                        .scaleX(active ? 1.15f : 1.0f)
                        .scaleY(active ? 1.15f : 1.0f)
                        .setDuration(500)
                        .start();
                }
                blinkIndex = (blinkIndex + 1) % 3;
            }
            blinkHandler.postDelayed(blinkRunnable, 1000);
        };
    }

    public Menu(Context context) {
        this.context = context;
        Jni.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initBlinkRunnable();
    }

    public static void setHighRefreshRate(Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        Window window = activity.getWindow();
        if (window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        
        if (Build.VERSION.SDK_INT >= 30) {
            Display display = activity.getDisplay();
            if (display != null) {
                Display.Mode[] modes = display.getSupportedModes();
                if (modes != null && modes.length > 0) {
                    Display.Mode maxMode = null;
                    float maxRate = 0;
                    for (Display.Mode mode : modes) {
                        float rate = mode.getRefreshRate();
                        if (rate > maxRate) {
                            maxRate = rate;
                            maxMode = mode;
                        }
                    }
                    if (maxMode != null) {
                        params.preferredDisplayModeId = maxMode.getModeId();
                        params.preferredRefreshRate = maxRate;
                        window.setAttributes(params);
                    }
                }
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            Display display = window.getWindowManager().getDefaultDisplay();
            if (display != null) {
                params.preferredRefreshRate = display.getRefreshRate();
                window.setAttributes(params);
            }
        }
    }

    private static void hideSystemUI(Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        int flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= 19) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    public void show() {
        if (context instanceof Activity) {
            hideSystemUI((Activity) context);
        }
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        float d = metrics.density;

        rootContainer = new FrameLayout(context);
        createMenuLayout(d);
        createIslandLayout(d);
        createGestureHandle(d, metrics);

        int frameWidth = (int)(520 * d);
        FrameLayout.LayoutParams menuLp = new FrameLayout.LayoutParams(frameWidth, FrameLayout.LayoutParams.WRAP_CONTENT);
        menuLp.gravity = Gravity.CENTER_HORIZONTAL;
        rootContainer.addView(menuLayout, menuLp);

        FrameLayout.LayoutParams islandLp = new FrameLayout.LayoutParams((int)(120 * d), (int)(36 * d));
        islandLp.gravity = Gravity.CENTER_HORIZONTAL;
        rootContainer.addView(islandLayout, islandLp);

        islandLayout.setAlpha(0f);
        islandLayout.setVisibility(View.GONE);

        windowParams = new WindowManager.LayoutParams(
            frameWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        );
        windowParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        windowParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowParams.x = (metrics.widthPixels - frameWidth) / 2;
        windowParams.y = (metrics.heightPixels - (int)(380 * d)) / 2;

        if (Build.VERSION.SDK_INT >= 30) {
            Display display = windowManager.getDefaultDisplay();
            if (display != null) {
                Display.Mode[] modes = display.getSupportedModes();
                if (modes != null && modes.length > 0) {
                    float maxRate = 60.0f;
                    for (Display.Mode mode : modes) {
                        if (mode.getRefreshRate() > maxRate) {
                            maxRate = mode.getRefreshRate();
                        }
                    }
                    windowParams.preferredRefreshRate = maxRate;
                }
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            Display display = windowManager.getDefaultDisplay();
            if (display != null) {
                float rate = display.getRefreshRate();
                if (rate >= 60.0f) {
                    windowParams.preferredRefreshRate = rate;
                }
            }
        }

        windowManager.addView(rootContainer, windowParams);
        windowManager.addView(gestureHandle, handleParams);

        android.view.GestureDetector rootDetector = new android.view.GestureDetector(context, new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 != null && e2 != null && Math.abs(e2.getY() - e1.getY()) > 100 && Math.abs(vy) > 100) {
                    toggle();
                    return true;
                }
                return false;
            }
        });
        rootContainer.setOnTouchListener((v, event) -> {
            rootDetector.onTouchEvent(event);
            return false;
        });

        blinkHandler.post(blinkRunnable);
    }

    private void createGestureHandle(float d, DisplayMetrics m) {
        FrameLayout handleWrapper = new FrameLayout(context);
        handleWrapper.setClipChildren(false);
        handleWrapper.setClipToPadding(false);

        gestureHandle = new View(context);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xAAFFFFFF);
        gd.setCornerRadius(3 * d);
        gestureHandle.setBackground(gd);

        FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams((int)(200 * d), (int)(4 * d), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        barLp.bottomMargin = (int)(5 * d);
        handleWrapper.addView(gestureHandle, barLp);

        handleParams = new WindowManager.LayoutParams(
            (int)(200 * d), (int)(20 * d),
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        );
        handleParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        handleParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        handleParams.y = 0;

        if (Build.VERSION.SDK_INT >= 23) {
            Display display = windowManager.getDefaultDisplay();
            if (display != null) {
                float rate = display.getRefreshRate();
                if (rate >= 60.0f) {
                    handleParams.preferredRefreshRate = rate;
                }
            }
        }

        gestureDetector = new android.view.GestureDetector(context, new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 != null && e2 != null) {
                    float diffY = e1.getY() - e2.getY();
                    if (diffY > 10 && vy < -120) {
                        toggle();
                        return true;
                    }
                }
                return false;
            }
        });

        handleWrapper.setOnTouchListener(new View.OnTouchListener() {
            private float initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaY = initialTouchY - event.getRawY();
                        if (deltaY > 0) {
                            float limit = 10 * d;
                            float moveY = -Math.min(deltaY, limit);
                            gestureHandle.setTranslationY(moveY);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        gestureHandle.animate()
                            .translationY(0)
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(400)
                            .setInterpolator(new android.view.animation.OvershootInterpolator(1.5f))
                            .start();
                        return true;
                }
                return false;
            }
        });

        this.gestureHandle = handleWrapper;
    }

    private void createMenuLayout(float d) {
        menuLayout = new LinearLayout(context);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COLOR_WINDOW_BG);
        bg.setCornerRadius(14 * d);
        bg.setStroke((int)(1 * d), COLOR_BORDER);
        menuLayout.setBackground(bg);
        menuLayout.setElevation(20 * d);
        menuLayout.setClipToOutline(true);

        LinearLayout header = createHeader(d);
        menuLayout.addView(header);

        View hDiv = new View(context);
        hDiv.setLayoutParams(new LinearLayout.LayoutParams(-1, (int)(1 * d)));
        hDiv.setBackgroundColor(COLOR_DIVIDER);
        menuLayout.addView(hDiv);

        LinearLayout contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.HORIZONTAL);
        contentContainer.setLayoutParams(new LinearLayout.LayoutParams((int)(520 * d), (int)(340 * d)));

        FrameLayout sidebar = createSidebar(d);
        contentContainer.addView(sidebar);

        View vDiv = new View(context);
        vDiv.setLayoutParams(new LinearLayout.LayoutParams((int)(1 * d), -1));
        vDiv.setBackgroundColor(COLOR_DIVIDER);
        contentContainer.addView(vDiv);

        contentPanel = new ContentPanel(context);
        contentContainer.addView(contentPanel);
        menuLayout.addView(contentContainer);

        loadDynamicContent(0);
        setupDragListener(header);
    }

    private LinearLayout createHeader(float d) {
        trafficLights.clear();
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding((int)(16 * d), (int)(14 * d), (int)(16 * d), (int)(14 * d));

        View red = createTrafficLight(d, TRAFFIC_RED);
        red.setOnClickListener(v -> animateToIsland());
        header.addView(red);
        trafficLights.add(red);

        View s1 = new View(context);
        s1.setLayoutParams(new LinearLayout.LayoutParams((int)(8 * d), 0));
        header.addView(s1);

        View yellow = createTrafficLight(d, TRAFFIC_YELLOW);
        header.addView(yellow);
        trafficLights.add(yellow);

        View s2 = new View(context);
        s2.setLayoutParams(new LinearLayout.LayoutParams((int)(8 * d), 0));
        header.addView(s2);

        View green = createTrafficLight(d, TRAFFIC_GREEN);
        header.addView(green);
        trafficLights.add(green);

        TextView title = new TextView(context);
        title.setText("PALLADIUM CONTROL");
        title.setTextColor(COLOR_TEXT_PRIMARY);
        title.setTextSize(12);
        title.setTypeface(Utils.getFont(context));
        title.setLetterSpacing(0.15f);
        title.setPadding((int)(20 * d), 0, 0, 0);
        header.addView(title);

        return header;
    }

    private View createTrafficLight(float d, int color) {
        View v = new View(context);
        int size = (int)(12 * d);
        v.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        v.setBackground(bg);
        return v;
    }

    private FrameLayout createSidebar(float d) {
        FrameLayout sidebar = new FrameLayout(context);
        sidebar.setLayoutParams(new LinearLayout.LayoutParams((int)(160 * d), -1));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COLOR_SIDEBAR_BG);
        bg.setCornerRadii(new float[] { 0,0, 0,0, 0,0, 14 * d, 14 * d });
        sidebar.setBackground(bg);

        selectionPill = new View(context);
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(COLOR_CARD_BG);
        pillBg.setCornerRadius(6 * d);
        pillBg.setStroke((int)(1 * d), COLOR_BORDER);
        selectionPill.setBackground(pillBg);
        selectionPill.setLayoutParams(new FrameLayout.LayoutParams(0, 0));
        selectionPill.setVisibility(View.INVISIBLE);
        sidebar.addView(selectionPill);

        LinearLayout itemContainer = new LinearLayout(context);
        itemContainer.setOrientation(LinearLayout.VERTICAL);
        itemContainer.setPadding((int)(6 * d), (int)(16 * d), (int)(16 * d), 0);
        sidebar.addView(itemContainer);

        parseTabs(itemContainer, d);
        sidebar.post(() -> { if (!sidebarItems.isEmpty()) selectTab(0, false); });

        return sidebar;
    }

    private void parseTabs(LinearLayout container, float d) {
        try {
            String[] f = Jni.getFeatures();
            if (f == null) return;
            for (String s : f) {
                if (s != null && s.startsWith("PAGE|")) {
                    String[] p = s.split(Pattern.quote("|"));
                    if (p.length >= 4) {
                        try {
                            int pageId = Integer.parseInt(p[1].trim());
                            addTabItem(container, p[3], p[2], pageId, d);
                        } catch (Throwable t) {}
                    }
                }
            }
        } catch (Throwable t) {}
    }

    private void addTabItem(LinearLayout container, String text, String iconPath, final int pageId, float d) {
        try {
            final int idx = sidebarItems.size();
            LinearLayout item = new LinearLayout(context);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding((int)(12 * d), (int)(10 * d), (int)(12 * d), (int)(10 * d));
            item.setClickable(true);
            item.setBackgroundColor(Color.TRANSPARENT);

            FrameLayout iconContainer = new FrameLayout(context);
            LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams((int)(18 * d), (int)(18 * d));
            icLp.rightMargin = (int)(10 * d);
            iconContainer.setLayoutParams(icLp);

            ImageView iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iconView.setVisibility(View.GONE);

            View fallbackView = new View(context);
            int[] colors = {0xFF3B82F6, 0xFF10B981, 0xFFF59E0B, 0xFFEF4444};
            fallbackView.setBackground(Utils.createCircleBg(colors[idx % colors.length]));
            FrameLayout.LayoutParams fbLp = new FrameLayout.LayoutParams((int)(10 * d), (int)(10 * d), Gravity.CENTER);
            fallbackView.setLayoutParams(fbLp);

            iconContainer.addView(fallbackView);
            iconContainer.addView(iconView);

            if (iconPath != null && !iconPath.trim().isEmpty()) {
                if (loadIcon(iconPath.trim(), iconView)) {
                    fallbackView.setVisibility(View.GONE);
                    iconView.setVisibility(View.VISIBLE);
                }
            }

            item.addView(iconContainer);

            TextView tv = new TextView(context);
            tv.setText(text);
            tv.setTextSize(12);
            tv.setTextColor(COLOR_TEXT_SECONDARY);
            tv.setTypeface(Utils.getFont(context));
            item.addView(tv);

            item.setOnClickListener(v -> {
                if (selectedTabIndex != idx) {
                    selectTab(idx, true);
                    loadDynamicContent(pageId);
                }
            });

            sidebarItems.add(item);
            container.addView(item);
        } catch (Throwable t) {}
    }

    private boolean loadIcon(String path, ImageView iv) {
        try (InputStream is = context.getAssets().open(path)) {
            Drawable drw = Drawable.createFromStream(is, null);
            if (drw != null) {
                iv.setImageDrawable(drw);
                return true;
            }
        } catch (Throwable t) {}
        return false;
    }

    private void selectTab(int idx, boolean animate) {
        if (idx < 0 || idx >= sidebarItems.size()) return;
        selectedTabIndex = idx;
        LinearLayout target = sidebarItems.get(idx);

        if (target.getHeight() > 0) {
            updateSelectionPill(target, animate);
        } else {
            target.post(() -> updateSelectionPill(target, animate));
        }

        for (int i = 0; i < sidebarItems.size(); i++) {
            try {
                LinearLayout item = sidebarItems.get(i);
                FrameLayout iconContainer = (FrameLayout) item.getChildAt(0);
                View fallback = iconContainer.getChildAt(0);
                ImageView icon = (ImageView) iconContainer.getChildAt(1);
                TextView tv = (TextView) item.getChildAt(1);
                boolean isSel = (i == idx);

                tv.setTextColor(isSel ? COLOR_TEXT_PRIMARY : COLOR_TEXT_SECONDARY);
                if (icon.getVisibility() == View.VISIBLE) {
                    icon.setAlpha(isSel ? 1.0f : 0.6f);
                } else {
                    fallback.setAlpha(isSel ? 1.0f : 0.5f);
                }
                tv.setTypeface(Utils.getFont(context), isSel ? Typeface.BOLD : Typeface.NORMAL);
            } catch (Throwable t) {}
        }
    }

    private void updateSelectionPill(LinearLayout target, boolean animate) {
        View parent = (View) target.getParent();
        float targetY = parent.getTop() + target.getTop();
        float d = context.getResources().getDisplayMetrics().density;
        
        float startWidth = selectionPill.getWidth();
        float targetWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight() - (12 * d);
        float startHeight = selectionPill.getHeight();
        float targetHeight = target.getHeight();
        float startY = selectionPill.getY();

        selectionPill.setVisibility(View.VISIBLE);
        
        if (animate) {
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(400);
            anim.setInterpolator(new android.view.animation.OvershootInterpolator(1.2f));
            anim.addUpdateListener(a -> {
                float f = (float) a.getAnimatedValue();
                selectionPill.setY(startY + (targetY - startY) * f);
                
                float w = targetWidth;
                float stretchFactor = (float) Math.sin(f * Math.PI); 
                if (Math.abs(targetY - startY) > 50 * d) {
                     w = targetWidth - (10 * d * stretchFactor); 
                }
                
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) selectionPill.getLayoutParams();
                lp.width = (int) w;
                lp.height = (int) targetHeight;
                lp.leftMargin = parent.getPaddingLeft();
                selectionPill.setLayoutParams(lp);
            });
            anim.start();
        } else {
            selectionPill.setY(targetY);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) selectionPill.getLayoutParams();
            lp.width = (int) targetWidth;
            lp.height = (int) targetHeight;
            lp.leftMargin = parent.getPaddingLeft();
            selectionPill.setLayoutParams(lp);
            selectionPill.setX(parent.getPaddingLeft());
        }
    }

    private void loadDynamicContent(int pageIndex) {
        contentPanel.loadContent(pageIndex);
    }

    private void createIslandLayout(float d) {
        islandLayout = new LinearLayout(context);
        islandLayout.setOrientation(LinearLayout.HORIZONTAL);
        islandLayout.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF000000);
        bg.setCornerRadius(18 * d);
        bg.setStroke((int)(1 * d), 0xFF333333);
        islandLayout.setBackground(bg);
        islandLayout.setElevation(10 * d);

        View dot = new View(context);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(COLOR_ACCENT_BLUE);
        dot.setBackground(dotBg);
        dot.setLayoutParams(new LinearLayout.LayoutParams((int)(8 * d), (int)(8 * d)));
        islandLayout.addView(dot);

        pulseAnimator = ValueAnimator.ofFloat(0.5f, 1f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(a -> {
            if (!isDestroyed) dot.setAlpha((float)a.getAnimatedValue());
        });
        pulseAnimator.start();

        TextView label = new TextView(context);
        label.setText("Active");
        label.setTextColor(COLOR_TEXT_SECONDARY);
        label.setTextSize(13);
        label.setTypeface(Utils.getFont(context));
        label.setPadding((int)(8 * d), 0, 0, 0);
        islandLayout.addView(label);

        islandLayout.setPadding((int)(12 * d), 0, (int)(12 * d), 0);

        islandLayout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float touchX, touchY;
            private boolean isClick;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = windowParams.x;
                        initialY = windowParams.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        isClick = true;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int)(event.getRawX() - touchX);
                        int dy = (int)(event.getRawY() - touchY);
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) isClick = false;
                        if (!isClick) {
                            windowParams.x = initialX + dx;
                            windowParams.y = initialY + dy;
                            windowManager.updateViewLayout(rootContainer, windowParams);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isClick) {
                            animateToMenu();
                        } else {
                            int targetY = (int)(12 * d);
                            ValueAnimator snap = ValueAnimator.ofInt(windowParams.y, targetY);
                            snap.setDuration(300);
                            snap.setInterpolator(new android.view.animation.DecelerateInterpolator());
                            snap.addUpdateListener(a -> {
                                windowParams.y = (int)a.getAnimatedValue();
                                windowManager.updateViewLayout(rootContainer, windowParams);
                            });
                            snap.start();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    public void toggle() {
        if (isExpanded) animateToIsland();
        else animateToMenu();
    }

    private void animateToIsland() {
        if (!isExpanded) return;
        isExpanded = false;
        DisplayMetrics m = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(m);
        startToggleAnim(windowParams.y, (int)(10 * m.density), false);
    }

    private void animateToMenu() {
        if (isExpanded) return;
        isExpanded = true;
        DisplayMetrics m = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(m);
        int targetY = (m.heightPixels - (int)(380 * m.density)) / 2;
        startToggleAnim(windowParams.y, targetY, true);
    }

    private void startToggleAnim(int startY, int targetY, boolean expanding) {
        if (toggleAnimator != null) toggleAnimator.cancel();
        float d = context.getResources().getDisplayMetrics().density;
        menuLayout.setPivotX((int)(520 * d) / 2f);
        menuLayout.setPivotY(0);
        islandLayout.setPivotX((int)(120 * d) / 2f);
        islandLayout.setPivotY((int)(36 * d) / 2f);

        if (expanding) {
            islandLayout.setVisibility(View.VISIBLE);
            menuLayout.setVisibility(View.VISIBLE);
            menuLayout.setAlpha(0f);
            menuLayout.setScaleX(0.2f);
            menuLayout.setScaleY(0.2f);
        } else {
            islandLayout.setVisibility(View.VISIBLE);
            islandLayout.setAlpha(0f);
            islandLayout.setScaleX(0.5f);
            islandLayout.setScaleY(0.5f);
            menuLayout.setVisibility(View.VISIBLE);
        }

        toggleAnimator = ValueAnimator.ofFloat(0f, 1f);
        toggleAnimator.setDuration(450);
        toggleAnimator.setInterpolator(EASE_IN_OUT);
        toggleAnimator.addUpdateListener(a -> {
            float f = (float)a.getAnimatedValue();
            windowParams.y = (int)(startY + (targetY - startY) * f);
            if (expanding) {
                menuLayout.setAlpha(f);
                menuLayout.setScaleX(0.2f + 0.8f * f);
                menuLayout.setScaleY(0.2f + 0.8f * f);
                islandLayout.setAlpha(1f - f);
                islandLayout.setScaleX(1.0f - 0.5f * f);
                islandLayout.setScaleY(1.0f - 0.5f * f);
            } else {
                menuLayout.setAlpha(1f - f);
                menuLayout.setScaleX(1.0f - 0.2f * f);
                menuLayout.setScaleY(1.0f - 0.2f * f);
                islandLayout.setAlpha(f);
                islandLayout.setScaleX(0.5f + 0.5f * f);
                islandLayout.setScaleY(0.5f + 0.5f * f);
            }
            windowManager.updateViewLayout(rootContainer, windowParams);
        });
        toggleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator a) {
                if (expanding) islandLayout.setVisibility(View.GONE);
                else menuLayout.setVisibility(View.GONE);
                windowParams.y = targetY;
                windowManager.updateViewLayout(rootContainer, windowParams);
            }
        });
        toggleAnimator.start();
    }

    private void setupDragListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float touchX, touchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isExpanded) return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = windowParams.x;
                        initialY = windowParams.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        windowParams.x = initialX + (int)(event.getRawX() - touchX);
                        windowParams.y = initialY + (int)(event.getRawY() - touchY);
                        windowManager.updateViewLayout(rootContainer, windowParams);
                        return true;
                }
                return false;
            }
        });
    }

    public void destroy() {
        isDestroyed = true;
        blinkHandler.removeCallbacksAndMessages(null);
        if (toggleAnimator != null) {
            toggleAnimator.cancel();
            toggleAnimator = null;
        }
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        for (View v : trafficLights) v.clearAnimation();
        if (gestureHandle != null && gestureHandle.getWindowToken() != null) {
            windowManager.removeView(gestureHandle);
        }
        if (rootContainer != null && rootContainer.getWindowToken() != null) {
            windowManager.removeView(rootContainer);
        }
        rootContainer = null;
        gestureHandle = null;
        trafficLights.clear();
    }

    private class ContentPanel extends ScrollView {
        private LinearLayout contentLayout;
        private final SparseArray<Boolean> checkStates = new SparseArray<>();
        private final SparseArray<Integer> valueStates = new SparseArray<>();
        private static final Pattern PIPE_PATTERN = Pattern.compile(Pattern.quote("|"));

        private DumpLibDialog dumpDialog;
        private PatchOffsetDialog patchDialog;
        private LuaScriptDialog luaDialog;

        public ContentPanel(Context context) {
            super(context);
            setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            setFillViewport(true);
            setVerticalScrollBarEnabled(false);
            setOverScrollMode(OVER_SCROLL_NEVER);
            contentLayout = new LinearLayout(context);
            contentLayout.setOrientation(LinearLayout.VERTICAL);
            float d = context.getResources().getDisplayMetrics().density;
            contentLayout.setPadding((int)(20 * d), (int)(12 * d), (int)(20 * d), (int)(20 * d));
            addView(contentLayout);
        }

        public void loadContent(int pageIndex) {
            animate().alpha(0f).translationY(20).setDuration(150).withEndAction(() -> {
                contentLayout.removeAllViews();
                try {
                    String[] features = Jni.getFeatures();
                    if (features != null) {
                        for (String f : features) {
                            if (f == null || !f.contains("|")) continue;
                            parseFeature(f, pageIndex);
                        }
                    }
                } catch (Throwable t) {}
                setAlpha(0f);
                setTranslationY(-20);
                animate().alpha(1f).translationY(0).setDuration(350).setInterpolator(EASE_OUT).start();
            }).start();
        }

        private void parseFeature(String data, int targetPage) {
            try {
                String[] p = PIPE_PATTERN.split(data);
                if (p.length < 3) return;
                String type = p[0].trim();
                int pIdx = Integer.parseInt(p[1].trim());
                if (pIdx != targetPage) return;

                switch (type) {
                    case "TITLE":
                        addTitle(p[2]);
                        break;
                    case "CHECK":
                        if (p.length >= 4) addSwitch(p[2], Integer.parseInt(p[3].trim()));
                        break;
                    case "SLIDER":
                        if (p.length >= 6) addSlider(p[2], Integer.parseInt(p[3].trim()), Integer.parseInt(p[4].trim()), Integer.parseInt(p[5].trim()));
                        break;
                    case "SPINNER":
                        if (p.length >= 5) addDropdown(p[2], p[3].split(","), Integer.parseInt(p[4].trim()));
                        break;
                    case "BUTTON":
                        if (p.length >= 4) addButton(p[2], p[3]);
                        break;
                }
            } catch (Throwable t) {}
        }

        private void addTitle(String text) {
            float d = context.getResources().getDisplayMetrics().density;
            TextView tv = new TextView(context);
            tv.setText(text);
            tv.setTextColor(COLOR_TEXT_SECONDARY);
            tv.setTextSize(10);
            tv.setTypeface(Utils.getFont(context), Typeface.BOLD);
            tv.setLetterSpacing(0.1f);
            int topPad = contentLayout.getChildCount() == 0 ? (int)(4 * d) : (int)(20 * d);
            tv.setPadding((int)(4 * d), topPad, 0, (int)(8 * d));
            contentLayout.addView(tv);
        }

        private void addSwitch(String label, final int id) {
            boolean currentState = checkStates.get(id, false);
            LinearLayout card = createCard();
            SwitchRow sw = new SwitchRow(context, label, currentState, (checked) -> {
                checkStates.put(id, checked);
                try { Jni.Callback(id, checked, 0, 0.0f, ""); } catch (Throwable t) {}
            });
            card.addView(sw);
            contentLayout.addView(card);
        }

        private void addDropdown(String label, String[] items, final int id) {
            float d = context.getResources().getDisplayMetrics().density;
            LinearLayout card = createCard();
            TextView tv = new TextView(context);
            tv.setText(label);
            tv.setTextColor(COLOR_TEXT_PRIMARY);
            tv.setTypeface(Utils.getFont(context));
            tv.setTextSize(13);
            tv.setPadding(0, 0, 0, (int)(8 * d));
            card.addView(tv);
            int lastPos = valueStates.get(id, 0);
            Dropdown dropdown = new Dropdown(context, items, pos -> {
                valueStates.put(id, pos);
                try { Jni.Callback(id, false, pos, 0.0f, ""); } catch (Throwable t) {}
            });
            dropdown.setSelection(lastPos);
            card.addView(dropdown);
            contentLayout.addView(card);
        }

        private void addSlider(String label, int min, int max, final int id) {
            float d = context.getResources().getDisplayMetrics().density;
            LinearLayout card = createCard();
            int currentVal = valueStates.get(id, min);
            TextView tv = new TextView(context);
            tv.setText(label + ": " + currentVal);
            tv.setTextColor(COLOR_TEXT_PRIMARY);
            tv.setTypeface(Utils.getFont(context));
            tv.setTextSize(13);
            card.addView(tv);
            SeekBarView sb = new SeekBarView(context, min, max, val -> {
                tv.setText(label + ": " + val);
                valueStates.put(id, val);
                try { Jni.Callback(id, false, val, 0.0f, ""); } catch (Throwable t) {}
            });
            sb.setProgress(currentVal);
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(36 * d));
            slp.topMargin = (int)(8 * d);
            sb.setLayoutParams(slp);
            card.addView(sb);
            contentLayout.addView(card);
        }

        private void addButton(String label, String action) {
            if (label == null || action == null || label.length() > 100 || action.length() > 50) return;
            float d = Math.max(1.0f, context.getResources().getDisplayMetrics().density);
            Button btn = new Button(context);
            btn.setText(label);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(12);
            btn.setTypeface(Utils.getFont(context));
            btn.setAllCaps(false);
            GradientDrawable bg = new GradientDrawable();
            int color = 0xFF3B82F6;
            if (action.contains("dump") || action.contains("Dump")) color = 0xFF8B5CF6;
            else if (action.contains("patch") || action.contains("Patch")) color = 0xFFF59E0B;
            else if (action.contains("lua") || action.contains("Lua")) color = 0xFF10B981;
            bg.setColor(color);
            bg.setCornerRadius(10 * d);
            btn.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(44 * d));
            lp.bottomMargin = (int)(12 * d);
            btn.setLayoutParams(lp);
            btn.setOnClickListener(v -> {
                try {
                    if (!(context instanceof android.app.Activity)) return;
                    android.app.Activity activity = (android.app.Activity) context;
                    if (activity.isFinishing()) return;
                    switch (action) {
                        case "dump_lib_dialog":
                            if (dumpDialog != null) dumpDialog.dismiss();
                            dumpDialog = new DumpLibDialog(activity);
                            dumpDialog.show();
                            break;
                        case "patch_offset_dialog":
                            if (patchDialog != null) patchDialog.dismiss();
                            patchDialog = new PatchOffsetDialog(activity);
                            patchDialog.show();
                            break;
                        case "signature_manager_dialog":
                            try {
                                SignatureManagerDialog sigDialog = new SignatureManagerDialog(activity);
                                sigDialog.show();
                            } catch (Exception e) {}
                            break;
                        case "lua_script_dialog":
                            try {
                                if (luaDialog != null) luaDialog.dismiss();
                                luaDialog = new LuaScriptDialog(activity);
                                luaDialog.show();
                            } catch (Exception e) {}
                            break;
                    }
                } catch (Throwable t) {}
            });
            contentLayout.addView(btn);
        }

        private LinearLayout createCard() {
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

    private class SwitchRow extends LinearLayout {
        public SwitchRow(Context context, String text, boolean isChecked, SwitchView.OnCheckedChangeListener listener) {
            super(context);
            float d = context.getResources().getDisplayMetrics().density;

            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(0, 0, 0, 0);

            TextView label = new TextView(context);
            label.setText(text);
            label.setTextColor(COLOR_TEXT_PRIMARY);
            label.setTextSize(13);
            label.setTypeface(Utils.getFont(context));
            label.setLayoutParams(new LayoutParams(0, -2, 1f));
            addView(label);

            SwitchView sw = new SwitchView(context);
            sw.setChecked(isChecked);
            sw.setOnCheckedChangeListener(listener);
            addView(sw);
        }
    }



    private class SwitchView extends View {
        private boolean isChecked = false;
        private ValueAnimator animator;
        private float currentAnimValue = 0f;
        private OnCheckedChangeListener listener;

        private Paint trackPaint;
        private Paint thumbPaint;
        private RectF trackRect;

        public interface OnCheckedChangeListener {
            void onCheckedChanged(boolean isChecked);
        }

        public SwitchView(Context context) {
            super(context);
            init();
        }

        public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
            this.listener = l;
        }

        private void init() {
            float d = getResources().getDisplayMetrics().density;
            setLayoutParams(new ViewGroup.LayoutParams((int)(40 * d), (int)(22 * d)));

            trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            thumbPaint.setColor(Color.WHITE);
            thumbPaint.setShadowLayer(3 * d, 0, 1 * d, 0x66000000);

            setLayerType(LAYER_TYPE_SOFTWARE, thumbPaint);

            trackRect = new RectF();
            setOnClickListener(v -> toggle());
        }

        public void setChecked(boolean checked) {
            if (this.isChecked != checked) {
                this.isChecked = checked;
                animateState();
            }
        }

        private void toggle() {
            isChecked = !isChecked;
            if (listener != null) listener.onCheckedChanged(isChecked);
            animateState();
        }

        private void animateState() {
            if (animator != null) animator.cancel();
            animator = ValueAnimator.ofFloat(currentAnimValue, isChecked ? 1f : 0f);
            animator.setDuration(350);
            animator.setInterpolator(EASE_IN_OUT);
            animator.addUpdateListener(a -> {
                currentAnimValue = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float d = getResources().getDisplayMetrics().density;
            trackRect.set(0, 0, getWidth(), getHeight());

            int offColor = 0xFF404040;
            int onColor = COLOR_ACCENT_BLUE;
            int currentColor = (int) new ArgbEvaluator().evaluate(currentAnimValue, offColor, onColor);

            trackPaint.setColor(currentColor);
            canvas.drawRoundRect(trackRect, getHeight()/2, getHeight()/2, trackPaint);

            float padding = 2 * d;
            float thumbSize = 18 * d;
            float startX = padding;
            float endX = getWidth() - thumbSize - padding;

            float currentX = startX + (endX - startX) * currentAnimValue;
            float currentY = padding;

            canvas.drawCircle(currentX + thumbSize/2, currentY + thumbSize/2, thumbSize/2, thumbPaint);
        }

        @Override
        protected void onDetachedFromWindow() {
            if (animator != null && animator.isRunning()) animator.cancel();
            super.onDetachedFromWindow();
        }
    }

    private class SeekBarView extends View {
        private float progress = 0.5f;
        private int min, max;
        private OnProgressChangeListener listener;

        private Paint trackPaint;
        private Paint progressPaint;
        private Paint thumbPaint;
        private RectF trackRect;
        
        private float thumbScale = 1.0f;
        private ValueAnimator touchAnimator;

        public interface OnProgressChangeListener {
            void onProgressChanged(int value);
        }

        public SeekBarView(Context context, int min, int max, OnProgressChangeListener listener) {
            super(context);
            this.min = min;
            this.max = max;
            this.listener = listener;
            init();
        }

        private void init() {
            float d = getResources().getDisplayMetrics().density;
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(40 * d)));

            trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            trackPaint.setColor(0xFF252525);
            trackPaint.setStyle(Paint.Style.FILL);

            progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            progressPaint.setColor(COLOR_ACCENT_BLUE);
            progressPaint.setStyle(Paint.Style.FILL);
            progressPaint.setStrokeCap(Paint.Cap.ROUND);

            thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            thumbPaint.setColor(0xFFFFFFFF);
            thumbPaint.setShadowLayer(6 * d, 0, 2 * d, 0x40000000);
            setLayerType(LAYER_TYPE_SOFTWARE, thumbPaint);

            trackRect = new RectF();
        }
        
        private void animateThumb(boolean pressed) {
            if (touchAnimator != null) touchAnimator.cancel();
            touchAnimator = ValueAnimator.ofFloat(thumbScale, pressed ? 1.4f : 1.0f);
            touchAnimator.setDuration(200);
            touchAnimator.setInterpolator(new android.view.animation.OvershootInterpolator());
            touchAnimator.addUpdateListener(a -> {
                thumbScale = (float) a.getAnimatedValue();
                invalidate();
            });
            touchAnimator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float d = getResources().getDisplayMetrics().density;
            float w = getWidth();
            float h = getHeight();
            if (w == 0 || h == 0) return;

            float centerY = h / 2f;
            float padding = 12 * d;
            float availableWidth = w - (2 * padding);
            float trackHeight = 6 * d;

            trackRect.set(padding, centerY - (trackHeight / 2), w - padding, centerY + (trackHeight / 2));
            canvas.drawRoundRect(trackRect, trackHeight / 2, trackHeight / 2, trackPaint);

            float progressX = padding + (availableWidth * progress);
            RectF pRect = new RectF(padding, centerY - (trackHeight / 2), progressX, centerY + (trackHeight / 2));
            canvas.drawRoundRect(pRect, trackHeight / 2, trackHeight / 2, progressPaint);

            float currentThumbSize = 8 * d * thumbScale;
            canvas.drawCircle(progressX, centerY, currentThumbSize, thumbPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float d = getResources().getDisplayMetrics().density;
            float padding = 12 * d;
            float w = getWidth();
            float availableWidth = w - (2 * padding);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animateThumb(true);
                case MotionEvent.ACTION_MOVE:
                    if (max <= min) return false;
                    float x = event.getX();
                    progress = (x - padding) / availableWidth;
                    if (progress < 0) progress = 0;
                    if (progress > 1) progress = 1;
                    invalidate();
                    if (listener != null) {
                        listener.onProgressChanged(min + (int)(progress * (max - min)));
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animateThumb(false);
                    return true;
            }
            return super.onTouchEvent(event);
        }

        public void setProgress(int value) {
            if (max == min) return;
            this.progress = (float)(value - min) / (max - min);
            invalidate();
        }
    }

    private class Dropdown extends LinearLayout {
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

        public Dropdown(Context context, String[] items, OnItemSelectedListener listener) {
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

            int menuColor = COLOR_WINDOW_BG;
            normalBg = new GradientDrawable();
            normalBg.setColor(menuColor);
            normalBg.setCornerRadius(8 * d);
            normalBg.setStroke((int)(1 * d), 0xFF333333);
            activeBg = new GradientDrawable();
            activeBg.setColor(menuColor);
            activeBg.setCornerRadius(8 * d);
            activeBg.setStroke((int)(1.5f * d), COLOR_ACCENT_BLUE);

            setBackground(normalBg);
            setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(40 * d)));

            selectedText = new TextView(getContext());
            selectedText.setText(items.length > 0 ? items[0] : "Select option");
            selectedText.setTextColor(0xFFE5E7EB);
            selectedText.setTextSize(13);
            selectedText.setTypeface(Utils.getFont(getContext()));
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
                tv.setTypeface(Utils.getFont(context));
                itemBox.addView(tv);
                return itemBox;
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            if (popup != null && popup.isShowing()) popup.dismiss();
            listener = null;
            super.onDetachedFromWindow();
        }
    }
}
