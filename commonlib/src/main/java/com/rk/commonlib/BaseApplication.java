package com.rk.commonlib;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class BaseApplication extends Application {
    private final static String TAG = BaseApplication.class.getSimpleName();
    private IBinder mBinder = new Binder();
    private IWatchService mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected:" + name);
            mService = IWatchService.Stub.asInterface(service);
            try {
                mService.setBinder(mBinder);
            } catch (RemoteException e) {
                Log.e(TAG, "onServiceConnected, set binder error: " + e.getMessage());
            }
        }
    };

    @Override
    public void onCreate () {
        super.onCreate ();
        Log.i(TAG, "onCreate");
        if (isPrimaryProcess()) {
            Intent intent = new Intent(this, WatchDogService.class);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }

    }


    private boolean isPrimaryProcess() {
        String processName = getCurrentProcessName();
        Log.i(TAG, "isPrimaryProcess, process Name: " + processName);
        boolean result = getApplicationContext().getPackageName().equals(processName);
        return result;
    }

    private String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService
                (Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }
}
