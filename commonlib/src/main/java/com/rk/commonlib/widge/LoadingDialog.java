package com.rk.commonlib.widge;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.rk.commonlib.R;

public class LoadingDialog extends Dialog {
    private static final String TAG = LoadingDialog.class.getSimpleName();

    private Context mContext;
    private SpinKitView mSpinKitView;

    public LoadingDialog(Context context) {
        super(context);
        mContext = context;
    }

    public LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected LoadingDialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(mContext, R.layout.loading_layout, null);
        setContentView(view);
        setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawable(null);
        mSpinKitView = view.findViewById(R.id.spin_kit);
        mSpinKitView.setColor(mContext.getResources().getColor(android.R.color.white));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
