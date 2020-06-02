package com.rk.commonlib.widge;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.rk.commonlib.R;

public class RoundClockView extends FrameLayout {

    private static final String TAG = RoundClockView.class.getSimpleName();

    private Context mContext;

    public RoundClockView(Context context) {
        this(context, null);
    }

    public RoundClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        Log.i(TAG, "initView");
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.round_clock_layout, this, true);
    }

}
