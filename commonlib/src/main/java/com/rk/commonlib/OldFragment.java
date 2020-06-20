package com.rk.commonlib;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rk.commonlib.widge.LoadingDialog;
import com.rk.commonmodule.channel.channelmanager.ChannelManager;

public class OldFragment extends Fragment {
    private static final String TAG = OldFragment.class.getSimpleName();

    private LoadingDialog mLoadingDialog;

    private UiHandler mUiHandler;

    protected static Activity sParentActivity;

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
            setLoadingVisible(false);
        }

        @Override
        public void onOpenSuccess() {
            Log.i(TAG, "tty open successfully");
            setLoadingVisible(false);
            sParentActivity.runOnUiThread(new Runnable() {
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
            sParentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doAfterPowerOff();
                }
            });
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        initView(view);
        initEvent();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUiHandler = new UiHandler(Looper.getMainLooper());
    }

    public int getLayoutId(){
        return 0;
    }
    public void initView(View view){

    }
    public void initEvent(){

    }
    public void doAfterPowerOn(){

    }
    public void doAfterPowerOff(){

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        setLoadingVisible(true);
        ChannelManager.getInstance(sParentActivity).setChannelOpenAndCloseListener(mChannelOpenAndCloseListener);
        ChannelManager.getInstance(sParentActivity).openTty();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.cancel();
        }
        mUiHandler.removeCallbacksAndMessages(null);
        ChannelManager.getInstance(sParentActivity).setChannelOpenAndCloseListener(null);
        ChannelManager.getInstance(sParentActivity).closeTty();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setLoadingVisible(boolean show) {
        mUiHandler.removeMessages(UiHandler.SHOW_OR_DISMISS_LOADING_MSG);
        mUiHandler.sendMessage(mUiHandler.obtainMessage(UiHandler.SHOW_OR_DISMISS_LOADING_MSG, show));
    }

    private void showLoading(boolean isShow) {
        Log.i(TAG, "showLoading, isShow: " + isShow);
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(sParentActivity);
        }
        if (isShow) {
            mLoadingDialog.show();
        } else {
            mLoadingDialog.cancel();
        }

    }
}
