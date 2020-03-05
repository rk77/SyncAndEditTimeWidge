package com.rk.commonmodule.channel;

import android.util.Log;

import com.rk.commonmodule.jni.JniMethods;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;

public class Channel {
    private static final String TAG = Channel.class.getSimpleName();


    protected byte[] sendAndReceiveFrameSync(byte[] frame) {

        Log.i(TAG, "sendAndReceiveFrameSync, send data: " + DataConvertUtils.convertByteArrayToString(frame, false));
        int ret = -1;
        try {
            for (int i = 0; i < 30; i++) {
                ret = JniMethods.writeMGR(frame, frame.length);
                if (ret >= 0) {
                    break;
                }
                Thread.sleep(100);
            }
            if (ret < 0) {
                return null;
            } else {
                byte[] data = new byte[1024];
                ArrayList<Byte> frameByteList = new ArrayList<>();
                int tryTime = 5;
                for (int i = 0; i < 30; i++) {
                    int length = JniMethods.readMGR(data, data.length);
                    Log.i(TAG, "sendAndReceiveFrameSync, length: " + length + ", i: " + i);
                    if (length <= 0) {
                        if (tryTime > 0) {
                            tryTime = tryTime - 1;
                            Thread.sleep(200);
                            continue;
                        } else {
                            Log.i(TAG, "sendAndReceiveFrameSync, i: " + i);
                            break;
                        }
                    }
                    for (int index = 0; index < length; index++) {
                        frameByteList.add(data[index]);
                    }
                    Thread.sleep(200);
                }
                byte[] recvFrame = null;
                if (frameByteList.size() > 0) {
                    recvFrame = new byte[frameByteList.size()];
                    for (int i = 0; i < frameByteList.size(); i++) {
                        recvFrame[i] = frameByteList.get(i);
                    }
                }
                if (recvFrame != null) {

                    return recvFrame;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "sendAndReceiveFrameSync, error: " + e.getMessage());
        }
        return null;
    }
}
