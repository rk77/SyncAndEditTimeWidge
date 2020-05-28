package com.rk.commonmodule.channel;

import android.util.Log;

import com.rk.commonmodule.channel.channelmanager.ChannelManagerProtocolUtils;
import com.rk.commonmodule.jni.JniMethods;
import com.rk.commonmodule.transfer.TransferManager;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.Map;

public class Channel485 extends Channel implements IChannel {
    private static final String TAG = Channel485.class.getSimpleName();

    @Override
    public boolean channelOpen(int flag) {
        Log.i(TAG, "channelOpen");
        byte[] frame = ChannelManagerProtocolUtils.makeFrame(ChannelConstant.Channel.CHANNEL_485, ChannelConstant.ChannelCtrl.CHANNEL_SET_CROL);
        if (frame == null) {
            Log.i(TAG, "channelOpen, no frame, not send");
            return false;
        }
        byte[] recv =  sendAndReceiveFrameSync(frame);
        if (recv == null || recv.length <= 0) {
            return false;
        } else {
            String dataString = DataConvertUtils.convertByteArrayToString(recv, false);
            Log.i(TAG, "channelOpen, recv frame: " + dataString);
            return true;
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
                ret = JniMethods.write485(data, length);
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
                int length = JniMethods.read485(data, data.length);
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

        int baudRate = 2400;
        if (map != null && map.containsKey(ChannelConstant.BAUD_RATE_KEY)) {
            baudRate = (int) map.get(ChannelConstant.BAUD_RATE_KEY);
        }
        int ecc = 2;
        if (map != null && map.containsKey(ChannelConstant.PARITY_CHECK_BIT_KEY)) {
            ecc = (int) map.get(ChannelConstant.PARITY_CHECK_BIT_KEY);
        }
        int dataBit = 8;
        if (map != null && map.containsKey(ChannelConstant.BYTE_BIT_CNT_KEY)) {
            dataBit = (int) map.get(ChannelConstant.BYTE_BIT_CNT_KEY);
        }

        int stopBit = 1;
        if (map != null && map.containsKey(ChannelConstant.STOP_BIT_KEY)) {
            stopBit = (int) map.get(ChannelConstant.STOP_BIT_KEY);
        }

        byte[] frame = new byte[1024];

        frame[2] = (byte)0x03;
        frame[3] = (byte)0x00;

        frame[4] = (byte) (baudRate & 0xFF);
        frame[5] = (byte) ((baudRate >> 8) & 0xFF);
        frame[6] = (byte)((baudRate >> 16) & 0xFF);

        frame[7] = (byte) dataBit;
        frame[8] = (byte) ecc;
        frame[9] = (byte) stopBit;

        int len = 7;
        frame[1] = (byte) ((len << 8));
        frame[0] = (byte) (len & 0xFF);

        byte[] sendFrame = new byte[10];
        System.arraycopy (frame,0, sendFrame,0,10);
        byte[] recv =  sendAndReceiveFrameSync(sendFrame);
        if (recv == null || recv.length <= 0) {
            return 0;
        } else {
            String dataString = DataConvertUtils.convertByteArrayToString(recv, false);
            Log.i(TAG, "setChannelParams, recv frame: " + dataString);
            return 1;
        }

    }

    @Override
    public Map getChannelParams() {
        return null;
    }

    @Override
    public ChannelConstant.Channel getChannelType() {
        return ChannelConstant.Channel.CHANNEL_485;
    }
}
