package com.rk.commonlib.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.rk.commonlib.R;

import java.util.HashMap;
import java.util.Map;

public class SoundPlayerService extends Service {

    public enum SOUND_TYPE {
        SUCC, FAIL,
    }
    private static final String TAG = SoundPlayerService.class.getSimpleName();

    private SoundPool mSoundPool;
    private Map<Integer, Integer> mSoundMap;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SoundPlayerService getService() {
            return SoundPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        AudioAttributes attr = new AudioAttributes.Builder()           //设置音效相关属性
                .setUsage(AudioAttributes.USAGE_GAME)                  // 设置音效使用场景
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)    // 设置音效的类型
                .build();
        mSoundPool = new SoundPool.Builder()           // 创建SoundPool对象
                .setAudioAttributes(attr)              // 设置音效池的属性
                .setMaxStreams(10)                     // 设置最多可容纳10个音频流，
                .build();
        mSoundMap = new HashMap<Integer, Integer>();
        mSoundMap.put(0, mSoundPool.load(this, R.raw.read_success, 1));
        mSoundMap.put(1, mSoundPool.load(this, R.raw.read_fail, 1));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand, Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
        if (mSoundMap != null) {
            mSoundMap.clear();
            mSoundMap = null;
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mBinder;
    }

    public void play(SOUND_TYPE type) {
        if (mSoundPool != null) {
            switch (type) {
                case SUCC:
                    mSoundPool.play(mSoundMap.get(0), 1, 1, 0, 0, 1);
                    break;
                case FAIL:
                    mSoundPool.play(mSoundMap.get(1), 1, 1, 0, 0, 1);
                    break;
            }
        }

    }
}
