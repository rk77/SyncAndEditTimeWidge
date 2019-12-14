package com.rk.commonlib.widge.pagertransformer;

import android.util.Log;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class OverlayPageTransformer implements ViewPager.PageTransformer{

    private static final String TAG = OverlayPageTransformer.class.getSimpleName();

    private static final float MIN_SCALE = 0.75F; // 最小缩放比例
    private int mOffset = 70;
    @Override
    public void transformPage(View page, float position) {
        Log.i(TAG, "transformPage, page: " + page + ", position: " + position);

        //设置透明度

        // page.setAlpha(0.5f);

        //设置每个View在中间，即设置相对原位置偏移量

        if (position >= 0) {
            page.setTranslationX((-page.getWidth() * position) + (-page.getWidth() / 8) + position * page.getWidth() / 8 / 3);
        }

        //float scale = (page.getWidth() - mOffset * position) / (float) (page.getWidth());

        //设置水平方向缩放

        //page.setScaleX(1.3F * position);

        //设置竖直方向缩放

        page.setScaleY((float) Math.pow(0.9, position * 1.0));
        /*
        if (position < 0) {
            page.setAlpha(0);
        } else {
            page.setAlpha(1);
        }
        */

                /*

        //设置竖直方向偏移量

        page.setTranslationX(mOffset * position);
        */

    }
}
