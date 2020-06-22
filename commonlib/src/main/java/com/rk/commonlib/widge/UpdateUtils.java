package com.rk.commonlib.widge;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.rk.commonlib.UpdateChecker;

import static com.rk.commonlib.download.AppVersion.NEW_VERSION;

public class UpdateUtils {
    private static final String TAG = UpdateUtils.class.getSimpleName();
    private static UpdateChecker mUpdateChecker;

    public static void checkUpdate(Context context, String updateUrl) {

        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                Log.i(TAG, "checkUpdate, handleMessage, what: " + what);
                if (msg.obj != null && msg.obj instanceof String) {
                    Log.i(TAG, "checkUpdate, handleMessage, msg obj: " + msg.obj);
                }
                switch (what){
                    case NEW_VERSION://url
                        if (mUpdateChecker != null) {
                            mUpdateChecker.showUpdateDialog();
                        }
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
        UpdateChecker.apkFileName = "ChargeNewVersion.apk";
        mUpdateChecker = new UpdateChecker(context, handler);
        String checkUrl = updateUrl;
        mUpdateChecker.setCheckUrl(checkUrl);
        mUpdateChecker.setShowAlert(true);
        mUpdateChecker.setCheckMessage("已是最新");
        mUpdateChecker.checkUpdates();
    }
}
