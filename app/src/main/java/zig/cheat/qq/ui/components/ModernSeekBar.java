package zig.cheat.qq.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import zig.cheat.qq.ui.anim.AnimUtils;
import zig.cheat.qq.ui.theme.ThemeConstants;

public class ModernSeekBar extends View {
    private float progress = 0.5f;
    private int min, max;
    private OnProgressChangeListener listener;

    private Paint trackPaint;
    private Paint progressPaint;
    private Paint thumbPaint;
    private RectF trackRect;

    private float thumbRadius;
    private float trackHeight;

    public interface OnProgressChangeListener {
        void onProgressChanged(int value);
    }

    public ModernSeekBar(Context context, int min, int max, OnProgressChangeListener listener) {
        super(context);
        this.min = min;
        this.max = max;
        this.listener = listener;
        init();
    }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        trackHeight = 4 * d;
        thumbRadius = 7 * d;

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(36 * d)));

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setColor(0xFF333333);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(ThemeConstants.COLOR_ACCENT_BLUE);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setColor(0xFFFFFFFF);
        thumbPaint.setShadowLayer(4 * d, 0, 2 * d, 0x80000000);
        setLayerType(LAYER_TYPE_SOFTWARE, thumbPaint);

        trackRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float d = getResources().getDisplayMetrics().density;
        float w = getWidth();
        float h = getHeight();
        if (w == 0 || h == 0) return;

        float centerY = h / 2f;
        float padding = thumbRadius + (4 * d);
        float availableWidth = w - (2 * padding);

        trackRect.set(padding, centerY - (trackHeight / 2f), w - padding, centerY + (trackHeight / 2f));
        canvas.drawRoundRect(trackRect, trackHeight / 2f, trackHeight / 2f, trackPaint);

        float progressX = padding + (availableWidth * progress);
        RectF pRect = new RectF(padding, centerY - (trackHeight / 2f), progressX, centerY + (trackHeight / 2f));
        canvas.drawRoundRect(pRect, trackHeight / 2f, trackHeight / 2f, progressPaint);

        canvas.drawCircle(progressX, centerY, thumbRadius, thumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float d = getResources().getDisplayMetrics().density;
        float padding = thumbRadius + (4 * d);
        float w = getWidth();
        float availableWidth = w - (2 * padding);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
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
        }
        return super.onTouchEvent(event);
    }

    public void setProgress(int value) {
        if (max == min) return;
        this.progress = (float)(value - min) / (max - min);
        invalidate();
    }
}
