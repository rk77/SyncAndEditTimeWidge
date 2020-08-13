package com.rk.commonlib.widge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class RappleAnimationView extends View {

    private static final String TAG = RappleAnimationView.class.getSimpleName();

    private Paint mPaint;
    private int mColor;
    private int mStrokeWidth;
    private int mDiff = 0;

    public RappleAnimationView(Context context) {
        this(context, null);
    }

    public RappleAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RappleAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RappleAnimationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Log.i(TAG, "init");
        setBackgroundColor(Color.RED);
        mPaint = new Paint();
        mColor = Color.BLUE;
        mStrokeWidth = 10;
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw");
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        int diameter = Math.min(getWidth(), getHeight());
        int radius = diameter / 2;
        //canvas.drawCircle(getWidth() / 2, getHeight() / 2,diameter / 2, mPaint);

        RectF rectF = new RectF(w / 2 - (radius + mDiff), h / 2 - (radius + mDiff), w / 2 + radius + mDiff, h / 2 + radius + mDiff);

        canvas.drawArc(rectF, 0, 360, true, mPaint);


    }

    private boolean stop = true;
    public void startAnimation() {
        if (stop) {
            stop = false;
        } else {
            return;
        }
        while (true) {
            Log.i(TAG, "startAnimation");
            mDiff = mDiff + 2;
            post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
            if (stop) {
                break;
            }
        }

    }

    public void stop() {
        stop = true;
    }
}
