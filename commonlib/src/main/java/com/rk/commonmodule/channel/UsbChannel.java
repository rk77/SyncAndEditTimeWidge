package com.rk.commonmodule.channel;

import android.util.Log;

import com.rk.commonmodule.jni.JniMethods;
import com.rk.commonmodule.transfer.TransferManager;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.Map;

public class UsbChannel extends Channel implements IChannel {
    private static final String TAG = UsbChannel.class.getSimpleName();

    @Override
    public boolean channelOpen(int flag) {
        Log.i(TAG, "openPLCSync");
        int result = JniMethods.ttyUSBOpen(9600);
        if (result > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean channelClose(int flag) {
        return false;
    }

    @Override
    public int channelSend(byte[] data, int length) {
        Log.i(TAG, "channelSend, data: " + DataConvertUtils.convertByteArrayToString(data, false));
        int ret = -1;
        try {
            for (int i = 0; i < 30; i++) {
                ret = JniMethods.ttyUSBWrite(data, length);
                if (ret >= 0) {
                    Log.i(TAG, "channelSend, i: " + i + ", ret: " + ret);
                    break;
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public byte[] channelReceive() {
        byte[] recvFrame = null;
        try {
            byte[] data = new byte[1024];
            ArrayList<Byte> frameByteList = new ArrayList<>();
            int tryTime = 5;
            for (int i = 0; i < 30; i++) {
                int length = JniMethods.ttyUSBRead(data, data.length);
                Log.i(TAG, "channelReceive, length: " + length + "， i: " + i);
                if (length <= 0) {
                    if (tryTime > 0) {
                        tryTime = tryTime - 1;
                        Thread.sleep(200);
                        continue;
                    } else {
                        Log.i(TAG, "channelReceive, i: " + i);
                        break;
                    }
                }
                for (int index = 0; index < length; index++) {
                    frameByteList.add(data[index]);
                }
                Thread.sleep(200);
            }

            if (frameByteList.size() > 0) {
                recvFrame = new byte[frameByteList.size()];
                for (int i = 0; i < frameByteList.size(); i++) {
                    recvFrame[i] = frameByteList.get(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recvFrame;
    }

    @Override
    public int setChannelParams(Map map) {
        return 0;
    }

    @Override
    public Map getChannelParams() {
        return null;
    }

    @Override
    public ChannelConstant.Channel getChannelType() {
        return ChannelConstant.Channel.CHANNEL_PLC;
    }
}
