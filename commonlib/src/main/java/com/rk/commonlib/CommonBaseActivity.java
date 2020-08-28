package com.rk.commonlib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.rk.commonlib.widge.LoadingDialog;

public abstract class CommonBaseActivity extends Activity {
    private static final String TAG = CommonBaseActivity.class.getSimpleName();

    private LoadingDialog mLoadingDialog;

    private UiHandler mUiHandler;

    private class UiHandler extends Handler {

        public static final int SHOW_OR_DISMISS_LOADING_MSG = 0;

        public UiHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i(TAG, "UiHandler handleMessage, what: " + msg.what);
            switch (msg.what) {
                case SHOW_OR_DISMISS_LOADING_MSG:
                    boolean isShow = (boolean) msg.obj;
                    showLoading(isShow);
                    break;
                default:
                    break;
            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mUiHandler = new UiHandler(Looper.getMainLooper());
        handleIntent();
        initView();
        initEvent();
    }

    protected abstract int getLayoutId();
    protected abstract void handleIntent();
    protected abstract void initView();
    protected abstract void initEvent();

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.cancel();
        }
        mUiHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void setLoadingVisible(boolean show) {
        mUiHandler.removeMessages(UiHandler.SHOW_OR_DISMISS_LOADING_MSG);
        mUiHandler.sendMessage(mUiHandler.obtainMessage(UiHandler.SHOW_OR_DISMISS_LOADING_MSG, show));
    }

    private void showLoading(boolean isShow) {
        Log.i(TAG, "showLoading, isShow: " + isShow);
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
        }
        if (isShow) {
            mLoadingDialog.show();
        } else {
            mLoadingDialog.cancel();
        }

    }
}
