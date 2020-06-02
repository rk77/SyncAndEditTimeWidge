package com.rk.commonlib.widge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.rk.commonlib.R;

import java.util.Timer;
import java.util.TimerTask;

public class XiMiClock extends View {
    private static final String TAG = XiMiClock.class.getSimpleName();
    private Paint textPaint,paint;
    private Path mTriangle;
    private Timer mTimer;
    private float agree = 1;
    private Shader mshader;
    private PathEffect mEffect;

    private int mWidth;
    private int mHeight;

    private int mMinWidth;

    private boolean mIsAmimation = true;

    public XiMiClock(Context context) {
        super(context);
    }

    public XiMiClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        mWidth = getWidth();
        mHeight = getHeight();
        mMinWidth = Math.min(mWidth, mHeight);
        Log.i(TAG, "min: " + mMinWidth);
        //mEffect = new DashPathEffect(new float[]{1,2,5,10,50,20}, 0);    // float[]{ 虚线的厚度, 虚线的间距,虚线的厚度, 虚线的间距 ......}
        mEffect = new DashPathEffect(new float[]{5,0}, 0);    // float[]{ 虚线的厚度, 虚线的间距,虚线的厚度, 虚线的间距 ......}
        mshader = new SweepGradient(500, 500, Color.parseColor("#3b3b3b"), Color.parseColor("#ffffff"));    //渐变遮罩样式


        textPaint = new Paint();
        textPaint.setStrokeWidth(16);
        textPaint.setColor(Color.GRAY);
        textPaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setAntiAlias(true);

        paint = new Paint();
        paint.setAntiAlias(true);

        mTriangle = new Path();
        mTriangle.moveTo((int)(mMinWidth / 1.0417), mMinWidth / 2);// 此点为多边形的顶点【三角形（指针）】
        //下面两个x 相等，表示底边的位置
        mTriangle.lineTo(mMinWidth, (int)(mMinWidth / 1.9));  // y：底边宽的其中一个顶点
        mTriangle.lineTo(mMinWidth, (int)(mMinWidth / 2.105));  //y：底边宽的其中一个顶点
        mTriangle.close();

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.XiMiClock, 0, 0);
        mIsAmimation = array.getBoolean(R.styleable.XiMiClock_animation, true);

        if (mIsAmimation) {
            setmTimer();
        }
        //System.out.println("度数" + ((float) 6.0 / 10));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘画手表盘
        drawbeauty(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置宽高

        super.onMeasure(widthMeasureSpec,heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mMinWidth = Math.min(mWidth, mHeight);
        Log.i(TAG, "min: " + mMinWidth + ", widthMeasureSpec: " + widthMeasureSpec + ", heightMeasureSpec: " + heightMeasureSpec);
        mTriangle = new Path();
        mTriangle.moveTo((int)(mMinWidth / 1.0417), mMinWidth / 2);// 此点为多边形的顶点【三角形（指针）】
        //下面两个x 相等，表示底边的位置
        mTriangle.lineTo(mMinWidth, (int)(mMinWidth / 1.9));  // y：底边宽的其中一个顶点
        mTriangle.lineTo(mMinWidth, (int)(mMinWidth / 2.105));  //y：底边宽的其中一个顶点
        mTriangle.close();
        mshader = new SweepGradient(mMinWidth / 2, mMinWidth / 2, Color.parseColor("#3b3b3b"), Color.parseColor("#ffffff"));    //渐变遮罩样式

        //setMeasuredDimension(mMinWidth, mMinWidth);  //画布大小
    }

    public void drawbeauty(Canvas canvas) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        //设置缓存层，因为下面要实用xfermode，使用xfemode必须使用缓存层，否则会出现黑色背景
        int layerId = canvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

        //初始化画笔，因为上下两层做画需要的画笔属性不一样，所以只能每次重新设置一次
        paint.setStyle(Paint.Style.STROKE);     //设置画笔为不填充模式
        paint.setPathEffect(mEffect);           //设置笔画样式，这里设置的是虚线样式
        paint.setStrokeWidth(mMinWidth / 20);               //设置笔画宽度
        canvas.drawCircle(mMinWidth / 2, mMinWidth / 2, (int)(mMinWidth / 2.381), paint);    //画一个纯色表盘，虚线，空心圆形 【表盘位置和大小】

        //设置画笔属性，SRC_IN属性，让第二个图案只能花在第一个图案上面，也就是只能画在上面所说那个纯色表盘里面
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //把画笔虚线属性去掉，因为我要的是一个实心圆形，然后让这个实心但是颜色不一样圆形画在上面所说表盘上面，因为设置了xfermode，所以显示的一样会是虚线圆形表盘，但是颜色会变成你现在的颜色
        paint.setPathEffect(null);

        //设置画笔shader属性，这里设置的是SweepGradient模式，可以让颜色过渡泾渭分明，以圆形为中心开始变化
        paint.setShader(mshader);
        paint.setStyle(Paint.Style.FILL);

        canvas.save();      //保存画布

        //旋转画布，然后你就会发现时钟表盘开始动了
        canvas.rotate(agree, mMinWidth / 2, mMinWidth / 2);    //画布旋转的中心点
        canvas.drawRect(mMinWidth / 100, mMinWidth / 100, mMinWidth, mMinWidth + mMinWidth / 10, paint);   //渐变矩形绘制
        if (mIsAmimation) {
            canvas.drawPath(mTriangle, textPaint);    //绘制小三角形
        }
        canvas.restore();

        //最后将画笔去除Xfermode
        paint.setXfermode(null);
        canvas.restoreToCount(layerId);
    }

    private void setmTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                agree = agree + 0.022f;//数值越大，旋转速度越快
                if (agree > 360)
                    agree = 1;
                postInvalidate();
            }
        }, 1000, 3);  //延时/周期
    }
}

