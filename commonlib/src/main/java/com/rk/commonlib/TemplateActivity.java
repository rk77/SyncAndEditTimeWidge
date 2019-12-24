package com.rk.commonlib;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class TemplateActivity extends Activity {
    private static final String TAG = TemplateActivity.class.getSimpleName();

    private HandlerThread mHandlerThread;
    private NonUiHandler mNonUiHandler;
    private UiHandler mUiHandler;

    private class NonUiHandler extends Handler {
        public static final int LOAD_DATA_MSG = 0;
        public NonUiHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i(TAG, "NonUiHandler, handleMessage, what: " + msg.what);
            switch (msg.what) {
                case LOAD_DATA_MSG:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class UiHandler extends Handler {
        private Context mContext;
        public static final int UPDATE_LIST_MSG = 0;
        public  UiHandler(Looper looper, Context context) {
            super(looper);
            mContext = context;
        }

        public void handleMessage(Message msg) {
            Log.i(TAG, "UiHandler, handleMessage, what: " + msg.what);
            switch (msg.what) {
                case UPDATE_LIST_MSG:

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandlerThread = new HandlerThread(TAG + ":LoadDataThread");
        mHandlerThread.start();
        mNonUiHandler = new NonUiHandler(mHandlerThread.getLooper());
        mUiHandler = new UiHandler(getMainLooper(), this);

        initEvent();
    }

    protected void initEvent() {
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        mNonUiHandler.removeMessages(NonUiHandler.LOAD_DATA_MSG);
        mNonUiHandler.sendMessage(mNonUiHandler.obtainMessage(NonUiHandler.LOAD_DATA_MSG));
    }

    @Override
    protected void onStop() {
        mNonUiHandler.removeCallbacksAndMessages(null);
        mUiHandler.removeCallbacksAndMessages(null);
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        mHandlerThread.quitSafely();
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

}
