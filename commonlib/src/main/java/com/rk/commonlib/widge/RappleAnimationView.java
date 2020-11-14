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
    private int mDiff0 = 0;
    private int mDiff1 = 0;
    private int mDiff2 = 0;
    private int mAlpha = 255;
    private boolean stop = true;

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
        setBackgroundColor(Color.TRANSPARENT);
        mPaint = new Paint();
        mColor = Color.BLUE;
        mStrokeWidth = 30;
        mPaint.setAntiAlias(true);
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
        if (radius > diameter / 2 - mBy * mStrokeWidth) {
            radius = diameter / 2 - mBy * mStrokeWidth;
        }
        //mPaint.setColor(Color.argb(mAlpha, 0, 0, 255));
        //canvas.drawCircle(getWidth() / 2, getHeight() / 2,diameter / 2, mPaint);
        if (mDiff0 > 0) {
            int alpha = 255 - (255  * mDiff0) / (mBy * mStrokeWidth);
            if (alpha < 0) {
                alpha = 0;
            }

            mPaint.setColor(Color.argb(alpha, 195, 195, 195));
            Log.i(TAG, "onDraw, alpha: " + (255 - (255 * mDiff0) / (mBy * mStrokeWidth)) + ", mDiff: " + mDiff0);
            RectF rectF0 = new RectF(w / 2 - (radius + mDiff0), h / 2 - (radius + mDiff0), w / 2 + radius + mDiff0, h / 2 + radius + mDiff0);
            canvas.drawArc(rectF0, 0, 360, true, mPaint);
        }
        if (mDiff1 > 0) {
            //mPaint.setColor(Color.YELLOW);
            int alpha = 255 - (255  * mDiff1) / (mBy * mStrokeWidth);
            if (alpha < 0) {
                alpha = 0;
            }
            mPaint.setColor(Color.argb(alpha, 195, 195, 195));
            RectF rectF1 = new RectF(w / 2 - (radius + mDiff1), h / 2 - (radius + mDiff1), w / 2 + radius + mDiff1, h / 2 + radius + mDiff1);
            canvas.drawArc(rectF1, 0, 360, true, mPaint);
        }
        if (mDiff2 > 0) {
            //mPaint.setColor(Color.BLUE);
            int alpha = 255 - (255  * mDiff2) / (mBy * mStrokeWidth);
            if (alpha < 0) {
                alpha = 0;
            }
            mPaint.setColor(Color.argb(alpha, 195, 195, 195));
            RectF rectF2 = new RectF(w / 2 - (radius + mDiff2), h / 2 - (radius + mDiff2), w / 2 + radius + mDiff2, h / 2 + radius + mDiff2);
            canvas.drawArc(rectF2, 0, 360, true, mPaint);
        }

    }

    private int mBy = 3;

    public void startAnimation() {
        if (!stop) {
            return;
        }
        stop = false;
        mDiff0 = 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.i(TAG, "startAnimation, stop: " + stop);
                    if (stop) {
                        break;
                    }

                    if (mDiff1 > mBy * mStrokeWidth) {
                        mDiff1 = 0;
                    } else if (mDiff0 >= mStrokeWidth) {
                        mDiff1 = mDiff1 + 1;
                    } else if (mDiff1 > 0) {
                        mDiff1 = mDiff1 + 1;
                    }



                    if (mDiff2 > mBy * mStrokeWidth) {
                        mDiff2 = 0;
                    } else if (mDiff1 == mStrokeWidth) {
                        mDiff2 = 0;
                    } else if (mDiff1 > mStrokeWidth){
                        mDiff2 = mDiff2 + 1;
                    } else if (mDiff2 > 0) {
                        mDiff2 = mDiff2 + 1;
                    }

                    if (mDiff0 >= mBy * mStrokeWidth) {
                        mDiff0 = 0;
                    } else if (mDiff2 == mStrokeWidth) {
                        mDiff0 = 0;
                    } else if (mDiff2 > mStrokeWidth) {
                        mDiff0 = mDiff0 + 1;
                    } else if (mDiff0 > 0) {
                        mDiff0 = mDiff0 + 1;
                    }

                    if (mDiff0 == mStrokeWidth) {
                        mDiff1 = 0;
                    }
                    if (mDiff1 == mStrokeWidth) {
                        mDiff2 = 0;
                    }
                    if (mDiff2 == mStrokeWidth) {
                        mDiff0 = 0;
                    }

                    Log.i(TAG, "startAnimation, mDiff0: " + mDiff0 + ", mDiff1: " + mDiff1 + ", mDiff2: " + mDiff2);

                    //mAlpha = mAlpha - 255 / (2 * mStrokeWidth);

                    post(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                    try {
                        Thread.sleep(20);
                    } catch (Exception e) {

                    }
                }
            }
        }).start();

    }

    public void stop() {
        Log.i(TAG, "stopAnimation");
        mDiff0 = 0;
        mDiff1 = 0;
        mDiff2 = 0;
        stop = true;
        mAlpha = 255;
        post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }
}
