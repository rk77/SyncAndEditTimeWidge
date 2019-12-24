package com.rk.commonlib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.rk.commonlib.widge.LoadingDialog;
import com.rk.commonmodule.channel.ChannelConstant;
import com.rk.commonmodule.channel.channelmanager.ChannelManager;

public abstract class BaseActivity extends Activity {
    private static final String TAG = BaseActivity.class.getSimpleName();

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

    private ChannelManager.IChannelOpenAndCloseListener mChannelOpenAndCloseListener = new ChannelManager.IChannelOpenAndCloseListener() {
        @Override
        public void onOpenFail() {

        }

        @Override
        public void onOpenSuccess() {
            Log.i(TAG, "tty open successfully");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doAfterPowerOn();
                }
            });
        }

        @Override
        public void onCloseFail() {

        }

        @Override
        public void onCloseSuccess() {
            Log.i(TAG, "tty close successfully");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doAfterPowerOff();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mUiHandler = new UiHandler(Looper.getMainLooper());
        initView();
        initEvent();
    }

    protected abstract int getLayoutId();
    protected abstract void initView();
    protected abstract void initEvent();
    protected abstract void doAfterPowerOn();
    protected abstract void doAfterPowerOff();

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        ChannelManager.getInstance(this).setChannelOpenAndCloseListener(mChannelOpenAndCloseListener);
        ChannelManager.getInstance(this).openTty();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        mUiHandler.removeCallbacksAndMessages(null);
        ChannelManager.getInstance(this).setChannelOpenAndCloseListener(null);
        ChannelManager.getInstance(this).closeTty();
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
