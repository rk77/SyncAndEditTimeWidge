package com.rk.commonlib;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rk.commonlib.util.LogUtils;

public abstract class CommonBaseFragment extends Fragment {

    private static final String TAG = CommonBaseFragment.class.getSimpleName();

    protected NonUIHandler mNonUIHandler;
    private HandlerThread mHandlerThread;
    protected UIHandler mUIHandler;

    protected static Activity sParentActivity;

    public class NonUIHandler extends Handler {
        public NonUIHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            handleNonUIMessage(msg);
        }
    }

    public final static int SHOW_TOAST_MSG = 0;
    public class UIHandler extends Handler {

        public UIHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_TOAST_MSG:
                    String s = (String) msg.obj;
                    Toast.makeText(sParentActivity, s, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    handleUIMessage(msg);
            }
        }
    }

    protected abstract void initEvent();
    protected abstract void initView(View view);
    protected abstract void handleUIMessage(Message msg);
    protected abstract void handleNonUIMessage(Message msg);
    protected abstract int getLayoutId();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        initView(view);
        initEvent();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandlerThread = new HandlerThread("Non UI Thread");
        mHandlerThread.start();
        mNonUIHandler = new NonUIHandler(mHandlerThread.getLooper());
        mUIHandler = new UIHandler();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mNonUIHandler.removeCallbacksAndMessages(null);
        mUIHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandlerThread.quitSafely();
    }

}
