package com.rk.commonlib.widge;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rk.commonlib.R;

public class RoundMeterClockView extends FrameLayout {

    private static final String TAG = RoundMeterClockView.class.getSimpleName();

    private Context mContext;
    private TextView mDateView;
    private TextView mTimeView;

    public RoundMeterClockView(Context context) {
        this(context, null);
    }

    public RoundMeterClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundMeterClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        Log.i(TAG, "initView");
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.meter_round_clock_layout, this, true);
        mDateView = view.findViewById(R.id.date);
        mTimeView = view.findViewById(R.id.time);
    }

    public void setDate(String date) {
        mDateView.setText(date);
    }

    public void setTime(String time) {
        mTimeView.setText(time);
    }

    public void reset() {
        mDateView.setText(null);
        mTimeView.setText("电表时钟");
    }

}
