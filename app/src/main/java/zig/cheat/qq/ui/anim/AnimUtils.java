package zig.cheat.qq.ui.anim;

import android.animation.TimeInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class AnimUtils {
    public static final TimeInterpolator EASE_IN_OUT = new AccelerateDecelerateInterpolator();
    public static final TimeInterpolator EASE_OUT = new DecelerateInterpolator(1.5f);
    public static final TimeInterpolator OVERSHOOT = new OvershootInterpolator(1.2f);
}
