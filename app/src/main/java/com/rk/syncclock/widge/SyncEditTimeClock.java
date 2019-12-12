package com.rk.syncclock.widge;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.rk.syncclock.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class SyncEditTimeClock extends RelativeLayout {
    private static final String TAG = SyncEditTimeClock.class.getSimpleName();
    private Context mContext;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private NumberPicker mSecondPicker;
    private CheckBox mCheckbox;

    private RelativeLayout mContainer;

    private boolean mStopTicking = false;

    private final Runnable mTicker = new Runnable() {
        public void run() {
            if (mStopTicking) {
                return; // Test disabled the clock ticks
            }
            onTimeChanged();

            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);

            getHandler().postAtTime(mTicker, next);
        }
    };

    private void onTimeChanged() {
        Calendar calendars = Calendar.getInstance();

        calendars.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));


        int year = calendars.get(Calendar.YEAR);

        int month = calendars.get(Calendar.MONTH);

        int day = calendars.get(Calendar.DATE);

        int hour = calendars.get(Calendar.HOUR);

        int min = calendars.get(Calendar.MINUTE);

        int second = calendars.get(Calendar.SECOND);

        setDate(year, month, day);
        setTime(hour, min, second);
    }

    public SyncEditTimeClock(Context context) {
        this(context, null);
    }

    public SyncEditTimeClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SyncEditTimeClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
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
        mContainer = view.findViewById(R.id.time_picker_container);
        resizeDatePikcer(mDatePicker);
        resizeTimePikcer(mTimePicker);
        //resizeNumberPicker1(mSecondPicker);
        mTimePicker.getChildAt(0).setPadding(0, 0, 0, 0);

        mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "onCheckedChanged, isChecked: " + isChecked);
                if (isChecked) {
                    mStopTicking = false;
                    getHandler().postAtTime(mTicker, 0);
                    setEnabled(false);
                } else {
                    mStopTicking = true;
                    getHandler().removeCallbacks(mTicker);
                    setEnabled(true);
                }
            }
        });
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(95, LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 0, 0);
        np.setLayoutParams(params);
    }

    private void resizeDatePikcer(FrameLayout tp){
        List<NumberPicker> npList = findNumberPicker(tp);
        for(NumberPicker np:npList){
            resizeNumberPicker(np);
        }
    }
    private void resizeTimePikcer(FrameLayout tp){
        List<NumberPicker> npList = findNumberPicker(tp);
        for(NumberPicker np:npList){
            resizeNumberPicker(np);
        }
    }

    public void setEnabled(boolean enable) {
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            mContainer.getChildAt(i).setEnabled(enable);
        }
    }

    //YYYYMMdd
    public String getDate() {
        StringBuilder sb = new StringBuilder();
        int year = mDatePicker.getYear();
        int month = mDatePicker.getMonth();
        int day = mDatePicker.getDayOfMonth();
        sb.append(year);
        if (month < 10) {
            sb.append("0" + month);
        } else {
            sb.append(month);
        }
        if (day < 10) {
            sb.append("0" + day);
        } else {
            sb.append(day);
        }
        return sb.toString();
    }

    public void setDate(int year, int month, int day) {
        mDatePicker.updateDate(year, month, day);
    }

    public String getTime() {
        StringBuilder sb = new StringBuilder();
        int hour = mTimePicker.getCurrentHour();
        int minute = mTimePicker.getCurrentMinute();
        int second = mSecondPicker.getValue();
        if (hour < 10) {
            sb.append("0" + hour);
        } else {
            sb.append(hour);
        }
        if (second < 10) {
            sb.append("0" + second);
        } else {
            sb.append(second);
        }
        return sb.toString();
    }

    public void setTime(int hour, int minute, int second) {
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
        mSecondPicker.setValue(second);
    }




}
