package com.rk.syncclock.widge;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.rk.syncclock.R;

import java.util.ArrayList;
import java.util.List;

public class SyncEditTimeClock extends RelativeLayout {
    private static final String TAG = SyncEditTimeClock.class.getSimpleName();
    private Context mContext;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private NumberPicker mSecondPicker;
    private CheckBox mCheckbox;

    public SyncEditTimeClock(Context context) {
        this(context, null);
    }

    public SyncEditTimeClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SyncEditTimeClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        resizePikcer(mDatePicker);
        resizePikcer(mTimePicker);
        resizeNumberPicker(mSecondPicker);
    }


    private void init(Context context, AttributeSet attrs) {
        Log.i(TAG, "init");
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.sync_edit_time_clock_layout, this, true);
        mDatePicker = view.findViewById(R.id.date_picker);
        mTimePicker = view.findViewById(R.id.time_picker);
        mTimePicker.setIs24HourView(true);
        mSecondPicker = view.findViewById(R.id.time_second_picker);
        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(59);
        mCheckbox = view.findViewById(R.id.sync_checkbox);
    }

    private List<NumberPicker> findNumberPicker(ViewGroup viewGroup) {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;
        if (null != viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                child = viewGroup.getChildAt(i);
                if (child instanceof NumberPicker) {
                    npList.add((NumberPicker) child);
                } else if (child instanceof LinearLayout) {
                    List<NumberPicker> result = findNumberPicker((ViewGroup) child);
                    if (result.size() > 0) {
                        return result;
                    }
                }
            }
        }
        return npList;
    }

    private void resizeNumberPicker(NumberPicker np) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
        np.setLayoutParams(params);
    }

    private void resizePikcer(FrameLayout tp){
        List<NumberPicker> npList = findNumberPicker(tp);
        for(NumberPicker np:npList){
            resizeNumberPicker(np);
        }
    }
}
