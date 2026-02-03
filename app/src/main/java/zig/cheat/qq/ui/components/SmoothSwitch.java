package zig.cheat.qq.ui.components;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import zig.cheat.qq.ui.anim.AnimUtils;
import zig.cheat.qq.ui.theme.ThemeConstants;

public class SmoothSwitch extends View {
    private boolean isChecked = false;
    private ValueAnimator animator;
    private float currentAnimValue = 0f; 

    private static final int WIDTH_DP = 40;
    private static final int HEIGHT_DP = 22;
    private static final int PADDING_DP = 2;
    private static final int THUMB_SIZE_DP = 18;

    private Paint trackPaint;
    private Paint thumbPaint;
    private RectF trackRect;
    private OnCheckedChangeListener listener;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean isChecked);
    }

    public SmoothSwitch(Context context) {
        super(context);
        init();
    }
    
    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        this.listener = l;
    }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        setLayoutParams(new android.view.ViewGroup.LayoutParams((int)(WIDTH_DP * d), (int)(HEIGHT_DP * d)));
        
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
        animator.setInterpolator(AnimUtils.EASE_IN_OUT);
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
        int onColor = ThemeConstants.COLOR_ACCENT_BLUE;
        int currentColor = (int) new ArgbEvaluator().evaluate(currentAnimValue, offColor, onColor);
        
        trackPaint.setColor(currentColor);
        canvas.drawRoundRect(trackRect, getHeight()/2, getHeight()/2, trackPaint);
        
        float padding = PADDING_DP * d;
        float thumbSize = THUMB_SIZE_DP * d;
        float startX = padding;
        float endX = getWidth() - thumbSize - padding;
        
        float currentX = startX + (endX - startX) * currentAnimValue;
        float currentY = padding;
        
        canvas.drawCircle(currentX + thumbSize/2, currentY + thumbSize/2, thumbSize/2, thumbPaint);
    }
}