package zig.cheat.qq.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import zig.cheat.qq.ui.anim.AnimUtils;
import zig.cheat.qq.ui.components.ContentPanel;
import zig.cheat.qq.ui.components.Sidebar;
import zig.cheat.qq.ui.components.TrafficLightButton;
import zig.cheat.qq.ui.theme.FontManager;
import zig.cheat.qq.ui.theme.ThemeConstants;

public class FloatingMenu {
    private Context context;
    private WindowManager windowManager;
    private FrameLayout rootContainer;
    private WindowManager.LayoutParams windowParams;
    private LinearLayout menuLayout;
    private LinearLayout islandLayout;
    private View headerView;
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

    private static class BlinkRunnable implements Runnable {
        private final WeakReference<FloatingMenu> ref;

        BlinkRunnable(FloatingMenu menu) {
            this.ref = new WeakReference<>(menu);
        }

        @Override
        public void run() {
            FloatingMenu menu = ref.get();
            if (menu == null) return;

            try {
                if (!menu.trafficLights.isEmpty() && menu.isExpanded) {
                    for (int i = 0; i < menu.trafficLights.size(); i++) {
                        View v = menu.trafficLights.get(i);
                        boolean isActive = (i == menu.blinkIndex);
                        v.animate()
                            .alpha(isActive ? 1.0f : 0.35f)
                            .scaleX(isActive ? 1.15f : 1.0f)
                            .scaleY(isActive ? 1.15f : 1.0f)
                            .setDuration(500)
                            .start();
                    }
                    menu.blinkIndex = (menu.blinkIndex + 1) % 3;
                }
            } catch (Throwable t) {}

            if (menu.blinkHandler != null) {
                menu.blinkHandler.postDelayed(this, 1000);
            }
        }
    }

    private Runnable blinkRunnable = new BlinkRunnable(this);

    public FloatingMenu(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public static void setHighRefreshRate(Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        if (Build.VERSION.SDK_INT >= 23) {
            Window window = activity.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                if (params != null) {
                    params.preferredDisplayModeId = 0;
                    window.setAttributes(params);
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 30) {
            Display display = activity.getDisplay();
            if (display != null) {
                Display.Mode[] modes = display.getSupportedModes();
                if (modes != null && modes.length > 0) {
                    Display.Mode maxMode = null;
                    float maxRate = 0;
                    for (Display.Mode mode : modes) {
                        if (mode != null && mode.getRefreshRate() > maxRate) {
                            maxRate = mode.getRefreshRate();
                            maxMode = mode;
                        }
                    }
                    if (maxMode != null) {
                        Window window = activity.getWindow();
                        if (window != null) {
                            WindowManager.LayoutParams params = window.getAttributes();
                            if (params != null) {
                                params.preferredDisplayModeId = maxMode.getModeId();
                                window.setAttributes(params);
                            }
                        }
                    }
                }
            }
        }
    }

    public void show() {
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

        if (Build.VERSION.SDK_INT >= 23) {
            Display display = windowManager.getDefaultDisplay();
            if (display != null) {
                float rate = display.getRefreshRate();
                if (rate >= 60.0f) {
                    windowParams.preferredRefreshRate = rate;
                }
            }
        }

        try {
            windowManager.addView(rootContainer, windowParams);
            windowManager.addView(gestureHandle, handleParams);
        } catch (Exception e) {}

        setupMainGestures();

        if (headerView != null) setupDragListener(headerView);

        blinkHandler.removeCallbacks(blinkRunnable);
        blinkHandler.post(blinkRunnable);
    }

    private void setupMainGestures() {
        gestureDetector = new android.view.GestureDetector(context, new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 == null || e2 == null) return false;
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > 100 && Math.abs(vy) > 100) {
                    toggle();
                    return true;
                }
                return false;
            }
        });

        rootContainer.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
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
        bg.setColor(ThemeConstants.COLOR_WINDOW_BG);
        bg.setCornerRadius(14 * d);
        bg.setStroke((int)(1 * d), ThemeConstants.COLOR_BORDER);
        menuLayout.setBackground(bg);
        menuLayout.setElevation(20 * d);
        menuLayout.setClipToOutline(true);
        LinearLayout header = createHeader(d);
        this.headerView = header;
        menuLayout.addView(header);
        View hDiv = new View(context);
        hDiv.setLayoutParams(new LinearLayout.LayoutParams(-1, (int)(1 * d)));
        hDiv.setBackgroundColor(ThemeConstants.COLOR_DIVIDER);
        menuLayout.addView(hDiv);
        LinearLayout contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.HORIZONTAL);
        contentContainer.setLayoutParams(new LinearLayout.LayoutParams((int)(520 * d), (int)(340 * d)));
        ContentPanel contentPanel = new ContentPanel(context);
        Sidebar sidebar = new Sidebar(context, index -> contentPanel.loadDynamicContent(index));
        contentContainer.addView(sidebar);
        View vDiv = new View(context);
        vDiv.setLayoutParams(new LinearLayout.LayoutParams((int)(1 * d), -1));
        vDiv.setBackgroundColor(ThemeConstants.COLOR_DIVIDER);
        contentContainer.addView(vDiv);
        contentContainer.addView(contentPanel);
        menuLayout.addView(contentContainer);
        contentPanel.loadDynamicContent(0);
    }

    private LinearLayout createHeader(float d) {
        trafficLights.clear();
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding((int)(16 * d), (int)(14 * d), (int)(16 * d), (int)(14 * d));
        TrafficLightButton red = new TrafficLightButton(context, ThemeConstants.TRAFFIC_RED, 12);
        red.setOnClickListener(v -> animateToIsland());
        header.addView(red);
        trafficLights.add(red);
        View s1 = new View(context); s1.setLayoutParams(new LinearLayout.LayoutParams((int)(8 * d), 0)); header.addView(s1);
        TrafficLightButton yellow = new TrafficLightButton(context, ThemeConstants.TRAFFIC_YELLOW, 12);
        header.addView(yellow);
        trafficLights.add(yellow);
        View s2 = new View(context); s2.setLayoutParams(new LinearLayout.LayoutParams((int)(8 * d), 0)); header.addView(s2);
        TrafficLightButton green = new TrafficLightButton(context, ThemeConstants.TRAFFIC_GREEN, 12);
        header.addView(green);
        trafficLights.add(green);
        TextView title = new TextView(context);
        title.setText("PALLADIUM CONTROL");
        title.setTextColor(ThemeConstants.COLOR_TEXT_PRIMARY);
        title.setTextSize(12);
        title.setTypeface(FontManager.getFont(context));
        title.setLetterSpacing(0.15f);
        title.setPadding((int)(20 * d), 0, 0, 0);
        header.addView(title);
        return header;
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
        dotBg.setColor(ThemeConstants.COLOR_ACCENT_BLUE);
        dot.setBackground(dotBg);
        dot.setLayoutParams(new LinearLayout.LayoutParams((int)(8 * d), (int)(8 * d)));
        pulseAnimator = ValueAnimator.ofFloat(0.5f, 1f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(a -> {
            if (!isDestroyed && dot != null) dot.setAlpha((float)a.getAnimatedValue());
        });
        pulseAnimator.start();
        islandLayout.addView(dot);
        TextView label = new TextView(context);
        label.setText("Active");
        label.setTextColor(ThemeConstants.COLOR_TEXT_SECONDARY);
        label.setTextSize(13);
        label.setTypeface(FontManager.getFont(context));
        label.setPadding((int)(8 * d), 0, 0, 0);
        islandLayout.addView(label);
        islandLayout.setPadding((int)(12 * d), 0, (int)(12 * d), 0);
        islandLayout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isClick = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = windowParams.x;
                        initialY = windowParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isClick = true;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                            isClick = false;
                        }
                        if (!isClick) {
                            windowParams.x = initialX + deltaX;
                            windowParams.y = initialY + deltaY;
                            try {
                                windowManager.updateViewLayout(rootContainer, windowParams);
                            } catch (Exception e) {}
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isClick) {
                            animateToMenu();
                        } else {
                            float d = context.getResources().getDisplayMetrics().density;
                            int targetY = (int)(12 * d);
                            ValueAnimator snapAnim = ValueAnimator.ofInt(windowParams.y, targetY);
                            snapAnim.setDuration(300);
                            snapAnim.setInterpolator(new android.view.animation.DecelerateInterpolator());
                            snapAnim.addUpdateListener(animation -> {
                                windowParams.y = (int) animation.getAnimatedValue();
                                try {
                                    windowManager.updateViewLayout(rootContainer, windowParams);
                                } catch (Exception e) {}
                            });
                            snapAnim.start();
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

        float d = m.density;
        int targetY = (int)(10 * d);
        startSmoothToggle(windowParams.y, targetY, false);
    }

    private void animateToMenu() {
        if (isExpanded) return;
        isExpanded = true;
        DisplayMetrics m = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(m);

        float d = m.density;
        int targetY = (m.heightPixels - (int)(380 * d)) / 2;
        startSmoothToggle(windowParams.y, targetY, true);
    }

    private void startSmoothToggle(int startY, int targetY, boolean expanding) {
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
        toggleAnimator.setInterpolator(AnimUtils.EASE_IN_OUT);
        toggleAnimator.addUpdateListener(animation -> {
            float f = (float) animation.getAnimatedValue();
            windowParams.y = (int) (startY + (targetY - startY) * f);
            if (expanding) {
                 menuLayout.setAlpha(f);
                 menuLayout.setScaleX(0.2f + (0.8f * f));
                 menuLayout.setScaleY(0.2f + (0.8f * f));
                 islandLayout.setAlpha(1f - f);
                 islandLayout.setScaleX(1.0f - (0.5f * f));
                 islandLayout.setScaleY(1.0f - (0.5f * f));
            } else {
                 menuLayout.setAlpha(1f - f);
                 menuLayout.setScaleX(1.0f - (0.2f * f));
                 menuLayout.setScaleY(1.0f - (0.2f * f));
                 islandLayout.setAlpha(f);
                 islandLayout.setScaleX(0.5f + (0.5f * f));
                 islandLayout.setScaleY(0.5f + (0.5f * f));
            }
            try {
                windowManager.updateViewLayout(rootContainer, windowParams);
            } catch (Exception e) {}
        });
        toggleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (expanding) islandLayout.setVisibility(View.GONE);
                else menuLayout.setVisibility(View.GONE);
                windowParams.y = targetY;
                try {
                    windowManager.updateViewLayout(rootContainer, windowParams);
                } catch (Exception e) {}
            }
        });
        toggleAnimator.start();
    }

    private void setupDragListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isExpanded) return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = windowParams.x; initialY = windowParams.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        windowParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        windowParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        try {
                            windowManager.updateViewLayout(rootContainer, windowParams);
                        } catch (Exception e) {}
                        return true;
                }
                return false;
            }
        });
    }

    public void destroy() {
        isDestroyed = true;
        blinkHandler.removeCallbacksAndMessages(null);
        if (toggleAnimator != null && toggleAnimator.isRunning()) {
            toggleAnimator.cancel();
            toggleAnimator = null;
        }
        if (pulseAnimator != null && pulseAnimator.isRunning()) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        for (View v : trafficLights) {
            v.clearAnimation();
        }
        try {
            if (gestureHandle != null && gestureHandle.getWindowToken() != null) {
                windowManager.removeView(gestureHandle);
            }
        } catch (Exception e) {}
        try {
            if (rootContainer != null && rootContainer.getWindowToken() != null) {
                windowManager.removeView(rootContainer);
            }
        } catch (Exception e) {}
        rootContainer = null;
        gestureHandle = null;
        trafficLights.clear();
    }
}
