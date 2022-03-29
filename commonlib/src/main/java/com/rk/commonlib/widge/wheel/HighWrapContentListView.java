package com.rk.commonlib.widge.wheel;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class HighWrapContentListView extends ListView {
    public HighWrapContentListView(Context context) {
        super(context);
    }

    public HighWrapContentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HighWrapContentListView(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
