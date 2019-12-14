package com.rk.commonlib;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.rk.commonlib.IWatchService;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WatchDogService extends Service {

    private static final String TAG = WatchDogService.class.getSimpleName();

    private IBinder mClient;

    private WatchService mWatchService = new WatchService();

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mWatchService;
    }

    private class ClientDeathRecipient implements IBinder.DeathRecipient {
        @Override
        public void binderDied() {
            Log.d(TAG, "client has died");
            doAfterClientDied();
        }
    }

    private void doAfterClientDied() {
        Log.i(TAG, "doAfterClientDied");
        try {
        } catch (Exception e) {
            Log.i(TAG, "doAfterClientDied, error: " + e.getMessage());
        }
    }

    private class WatchService extends IWatchService.Stub{
        @Override
        public int getPid() {
            return (int)Thread.currentThread().getId();
        }
        @Override
        public void setBinder(IBinder client) throws RemoteException {
            Log.i(TAG, "setBinder");
            mClient = client;
            mClient.linkToDeath(new ClientDeathRecipient(), 0);
        }

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.i(TAG, "onTrimMemory");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        return super.onUnbind(intent);

    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(fd, writer, args);
    }
}
