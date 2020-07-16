package com.rk.syncclock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.rk.commonlib.IWatchService;
import com.rk.commonlib.service.SoundPlayerService;

public class MainActivity extends AppCompatActivity {

    private String TAG = "AX";
    private Button mTest1Btn;
    private Button mTest2Btn;

    private SoundPlayerService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mBoundService = ((SoundPlayerService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG, "onServiceDisconnected");
            mBoundService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindService(new Intent(MainActivity.this, SoundPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);

        mTest1Btn = findViewById(R.id.test1);
        mTest2Btn = findViewById(R.id.test2);

        mTest1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBoundService != null) {
                    mBoundService.play(SoundPlayerService.SOUND_TYPE.SUCC);
                }
            }
        });

        mTest2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBoundService != null) {
                    mBoundService.play(SoundPlayerService.SOUND_TYPE.FAIL);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        unbindService(mConnection);
        super.onDestroy();
    }
}
