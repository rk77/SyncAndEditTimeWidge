package com.rk.commonlib.widge;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.rk.commonlib.R;

public class RappleButtonWidge extends FrameLayout {
    private static final String TAG = RappleButtonWidge.class.getSimpleName();
    private Context mContext;
    private RappleAnimationView mRappleAnimationView;

    public RappleButtonWidge(Context context) {
        this(context, null);
    }

    public RappleButtonWidge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RappleButtonWidge(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RappleButtonWidge(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        Log.i(TAG, "initView");
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rapple_btn_layout, this, true);
        mRappleAnimationView = view.findViewById(R.id.rapple_view);
        setClipChildren(false);
    }

    public void start() {
        if (mRappleAnimationView != null) {
            mRappleAnimationView.startAnimation();
        }
    }

    public void stop() {
        if (mRappleAnimationView != null) {
            mRappleAnimationView.stop();
        }
    }
}
