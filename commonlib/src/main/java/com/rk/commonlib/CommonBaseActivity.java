package com.rk.commonlib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.rk.commonlib.bluetooth.BluetoothInstance;
import com.rk.commonlib.widge.LoadingDialog;

public abstract class CommonBaseActivity extends Activity {
    private static final String TAG = CommonBaseActivity.class.getSimpleName();

    private LoadingDialog mLoadingDialog;

    public UiHandler mUiHandler;

    protected static final int SHOW_OR_DISMISS_LOADING_MSG = 0;
    protected static final int SHOW_TOAST_MSG = 1;

    protected class UiHandler extends Handler {

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
                case SHOW_TOAST_MSG:
                    showToast(msg.obj);
                    break;
                default:
                    handleUiMessage(msg);
                    break;
            }

        }

    }

    protected class NonUiHandler extends Handler {
        public NonUiHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            handleNonUiMessage(msg);
        }
    }

    public NonUiHandler mNonUiHandler;
    private HandlerThread mHandlerThread;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleBleBroadcast(intent);
        }
    };

    protected abstract void handleBleBroadcast(Intent intent);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mUiHandler = new UiHandler(Looper.getMainLooper());
        mHandlerThread = new HandlerThread("Non_UI_Thread");
        mHandlerThread.start();
        mNonUiHandler = new NonUiHandler(mHandlerThread.getLooper());
        registerReceiver(mBroadcastReceiver, makeGattUpdateIntentFilter());
        handleIntent();
        initView();
        initEvent();
    }

    protected abstract int getLayoutId();
    protected abstract void handleIntent();
    protected abstract void initView();
    protected abstract void initEvent();
    protected abstract void handleNonUiMessage(Message msg);
    protected abstract void handleUiMessage(Message msg);

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
        mNonUiHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        mHandlerThread.quitSafely();
        mNonUiHandler = null;
        mUiHandler = null;
        super.onDestroy();
    }

    protected void setLoadingVisible(boolean show) {
        mUiHandler.removeMessages(SHOW_OR_DISMISS_LOADING_MSG);
        mUiHandler.sendMessage(mUiHandler.obtainMessage(SHOW_OR_DISMISS_LOADING_MSG, show));
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

    protected void showToast(Object text) {
        if (text == null) {
            return;
        }
        if (text instanceof String) {
            Toast.makeText(this, (String)text, Toast.LENGTH_SHORT).show();
        } else if (text instanceof Integer) {
            try {
                Toast.makeText(this, getResources().getText((int) text), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "showToast, error: " + e.getMessage());
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothInstance.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothInstance.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothInstance.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothInstance.ACTION_GATT_CHARACTERISTIC_READ);
        intentFilter.addAction(BluetoothInstance.ACTION_GATT_CHARACTERISTIC_WRITE);
        intentFilter.addAction(BluetoothInstance.ACTION_GATT_DESCRIPTOR_READ);
        intentFilter.addAction(BluetoothInstance.ACTION_GATT_DESCRIPTOR_WRITE);
        intentFilter.addAction(BluetoothInstance.ACTION_GATT_MTU_WRITE);
        return intentFilter;
    }
}
