package com.rk.commonmodule.channel.channelmanager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.rk.commonmodule.channel.Channel485;
import com.rk.commonmodule.channel.ChannelConstant;
import com.rk.commonmodule.channel.ChannelConstant.Channel;
import com.rk.commonmodule.channel.ChannelConstant.ChannelCtrl;
import com.rk.commonmodule.channel.IChannel;
import com.rk.commonmodule.channel.InfraredChannel;
import com.rk.commonmodule.channel.UsbChannel;
import com.rk.commonmodule.jni.JniMethods;
import com.rk.commonmodule.transfer.TransferManager;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.Map;


public class ChannelManager {
    private static final String TAG = ChannelManager.class.getSimpleName();

    private static Context sContext;

    private NonUIHandler mNonUIHandler;
    private HandlerThread mHandlerThread;

    private boolean mIsChannelManagerOpen = false;

    private boolean mIsPowerOn = false;

    private ChannelConstant.Channel mCurrentChannelType = ChannelConstant.Channel.CHANNEL_NONE;
    private IChannel mCurrentChannel;
    private static final InfraredChannel sInfraredChannel = new InfraredChannel();
    private static final Channel485 s485Channel = new Channel485();
    private static final UsbChannel sUsbChannel = new UsbChannel();

    private IChannelOpenAndCloseListener mChannelOpenAndCloseListener;
    private IChannelManagerSendListener mChannelManagerSendListener;
    private IChannelManagerReceiveListener mChannelManagerReceiveListener;

    public enum ChannelManagerStatus {
        IDLE,
        BUSY,
    }

    private ChannelManagerStatus mStatus = ChannelManagerStatus.IDLE;

    private class NonUIHandler extends Handler {
        public static final int OPEN_TTY_MSG = 0;
        public static final int CLOSE_TTY_MSG = 1;
        public static final int SEND_MSG = 2;
        public static final int RECEIVE_MSG = 3;
        public NonUIHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            Log.i(TAG, "handleMessage, what: " + msg.what);
            switch (msg.what) {
                case OPEN_TTY_MSG:
                    int retOpen = JniMethods.tryOpenTty();
                    Log.i(TAG, "handleMessage, OPEN_TTY_MSG, ret: " + retOpen);
                    mIsChannelManagerOpen = true;
                    mIsPowerOn = true;
                    if (mChannelOpenAndCloseListener != null) {
                        mChannelOpenAndCloseListener.onOpenSuccess();
                    }
                    break;
                case CLOSE_TTY_MSG:
                    int retClose = JniMethods.ttyClose();
                    Log.i(TAG, "handleMessage, CLOSE_TTY_MSG, ret: " + retClose);
                    mIsChannelManagerOpen = false;
                    mIsPowerOn = false;
                    mStatus = ChannelManagerStatus.IDLE;
                    if (mChannelOpenAndCloseListener != null) {
                        mChannelOpenAndCloseListener.onCloseSuccess();
                    }
                    break;
                case SEND_MSG:
                    if (mChannelManagerSendListener != null) {
                        mChannelManagerSendListener.onChannelSendBegin();
                    }
                    byte[] frame = (byte[]) msg.obj;
                    Log.i(TAG, "handleMessage, SEND_MSG, send data: " + DataConvertUtils.convertByteArrayToString(frame, false));
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
                            if (mChannelManagerSendListener != null) {
                                mChannelManagerSendListener.onChannelSendFail("Fail");
                            }
                            mStatus = ChannelManagerStatus.IDLE;
                        } else {
                            if (mChannelManagerSendListener != null) {
                                mChannelManagerSendListener.onChannelSendSuccess("Channel send successfully");
                            }
                            if (mChannelManagerReceiveListener != null) {
                                mChannelManagerReceiveListener.onChannelReceiveBegin();
                            }
                            byte[] data = new byte[1024];
                            ArrayList<Byte> frameByteList = new ArrayList<>();
                            int tryTime = 5;
                            for (int i = 0; i < 30; i++) {
                                int length = JniMethods.readMGR(data, data.length);
                                Log.i(TAG, "handleMessage, SEND_MSG, length: " + length + ", i: " + i);
                                if (length <= 0) {
                                    if (tryTime > 0) {
                                        tryTime = tryTime - 1;
                                        Thread.sleep(200);
                                        continue;
                                    } else {
                                        Log.i(TAG, "handleMessage, SEND_MSG, i: " + i);
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
                                String dataString = DataConvertUtils.convertByteArrayToString(recvFrame, false);
                                Log.i(TAG, "handleMessage, SEND_MSG, recev frame: " + dataString);
                                // Infrared open
                                if ("01000500".equals(dataString)) {
                                    TransferManager.getInstance(sContext).setChannel(new InfraredChannel());
                                }
                                if (mChannelManagerReceiveListener != null) {
                                    mChannelManagerReceiveListener.onChannelReceiveSuccess(recvFrame);
                                }
                            } else {
                                if (mChannelManagerReceiveListener != null) {
                                    mChannelManagerReceiveListener.onChannelReceiveFail("Channel receive failed");
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "handleMessage, error: " + e.getMessage());
                    }
                    break;
                case RECEIVE_MSG:
                    break;
                default:
                    super.handleMessage(msg);
            }
            mStatus = ChannelManagerStatus.IDLE;

        }
    }

    private ChannelManager() {
        mHandlerThread = new HandlerThread("ChannelManagerThread");
        mHandlerThread.start();
        mNonUIHandler = new NonUIHandler(mHandlerThread.getLooper());
    }

    private static class SingletonInstanceHolder  {
        private static final ChannelManager INSTANCE = new ChannelManager();
    }

    public static ChannelManager getInstance(Context context) {
        sContext = context.getApplicationContext();
        return SingletonInstanceHolder.INSTANCE;
    }

    public interface IChannelOpenAndCloseListener {
        void onOpenFail();
        void onOpenSuccess();
        void onCloseFail();
        void onCloseSuccess();
    }

    public interface IChannelManagerSendListener {
        void onChannelSendBegin();
        void onChannelSendFail(String error);
        void onChannelSendSuccess(String succss);
    }

    public interface IChannelManagerReceiveListener {
        void onChannelReceiveBegin();
        void onChannelReceiveFail(String error);
        void onChannelReceiveSuccess(byte[] data);
    }

    public void setChannelOpenAndCloseListener(IChannelOpenAndCloseListener listener) {
        mChannelOpenAndCloseListener = listener;
    }

    public void removeChannelOpenAndCloseListener() {
        mChannelOpenAndCloseListener = null;
    }

    public void setChannelManagerSendListener(IChannelManagerSendListener listener) {
        mChannelManagerSendListener = listener;
    }

    public void removeChannelManagerSendListener() {
        mChannelManagerSendListener = null;
    }

    public void setChannelManagerReceiveListener(IChannelManagerReceiveListener listener) {
        mChannelManagerReceiveListener = listener;
    }

    public void removeChannelManagerReceiveListener() {
        mChannelManagerReceiveListener = null;
    }

    public void openTty() {
        Log.i(TAG, "openTty");
        if (mNonUIHandler != null) {
            mNonUIHandler.removeMessages(NonUIHandler.OPEN_TTY_MSG);
            mNonUIHandler.sendMessage(mNonUIHandler.obtainMessage(NonUIHandler.OPEN_TTY_MSG));
        }
    }

    public void closeTty() {
        Log.i(TAG, "closeTty");
        if (mNonUIHandler != null) {
            mNonUIHandler.removeMessages(NonUIHandler.CLOSE_TTY_MSG);
            mNonUIHandler.sendMessage(mNonUIHandler.obtainMessage(NonUIHandler.CLOSE_TTY_MSG));
        }
    }

    public void openChannel(ChannelConstant.Channel channel) {
        if (!mIsChannelManagerOpen) {
            if (mChannelManagerSendListener != null) {
                mChannelManagerSendListener.onChannelSendFail("Channel is not open");
            }
        }

        mCurrentChannelType = channel;
        switch (channel) {
            case CHANNEL_NONE:
                break;
            case CHANNEL_INFRARED:
                openIR();
                break;
            case CHANNEL_485:
                break;
            case CHANNEL_PLC:
                break;
            case CHANNEL_LORA:
                break;
            case CHANNEL_NET:
                break;
            default:
                break;
        }
    }

    public ChannelManagerStatus getCurrentStatus() {
        return mStatus;
    }

    private void openIR() {
        Log.i(TAG, "openIR");
        byte[] frame = ChannelManagerProtocolUtils.makeFrame(Channel.CHANNEL_INFRARED, ChannelCtrl.CHANNEL_SET_CROL);
        if (frame == null) {
            Log.i(TAG, "openIR, no frame, not send");
        }
        sendFrame(frame);
    }

    private void open485() {
        Log.i(TAG, "open485");
    }

    private void openPLC() {
        Log.i(TAG, "openPLC");
    }

    private void sendFrame(byte[] frame) {
        Log.i(TAG, "sendFrame");
        if (mStatus == ChannelManagerStatus.BUSY) {
            if (mChannelManagerSendListener != null) {
                mChannelManagerSendListener.onChannelSendFail("Channel manager is busy, not send");
            }
            return;
        }
        mStatus = ChannelManagerStatus.BUSY;
        if (mNonUIHandler != null) {
            mNonUIHandler.removeMessages(NonUIHandler.SEND_MSG);
            mNonUIHandler.sendMessage(mNonUIHandler.obtainMessage(NonUIHandler.SEND_MSG, frame));
        }
    }

    public boolean openChannelSync(ChannelConstant.Channel channel) {
        if (!mIsChannelManagerOpen) {
            Log.i(TAG, "openChannelSync, channel manager is not opened");
            return false;
        }

        boolean opened = false;

        mCurrentChannelType = channel;
        switch (channel) {
            case CHANNEL_NONE:
                break;
            case CHANNEL_INFRARED:
                opened = openIRSync();
                break;
            case CHANNEL_485:
                break;
            case CHANNEL_PLC:
                break;
            case CHANNEL_LORA:
                break;
            case CHANNEL_NET:
                break;
            default:
                break;
        }
        return opened;
    }

    private boolean openIRSync() {
        Log.i(TAG, "openIRSync");
        byte[] frame = ChannelManagerProtocolUtils.makeFrame(Channel.CHANNEL_INFRARED, ChannelCtrl.CHANNEL_SET_CROL);
        if (frame == null) {
            Log.i(TAG, "openIRSync, no frame, not send");
            return false;
        }
        byte[] recv =  sendAndReceiveFrameSync(frame);
        if (recv == null || recv.length <= 0) {
            return false;
        } else {
            String dataString = DataConvertUtils.convertByteArrayToString(recv, false);
            Log.i(TAG, "openIRSync, recv frame: " + dataString);
            // Infrared open
            if ("01000500".equals(dataString)) {
                TransferManager.getInstance(sContext).setChannel(new InfraredChannel());
            }
            return true;
        }
    }

    private boolean open485Sync() {
        Log.i(TAG, "open485Sync");
        byte[] frame = ChannelManagerProtocolUtils.makeFrame(Channel.CHANNEL_INFRARED, ChannelCtrl.CHANNEL_SET_CROL);
        if (frame == null) {
            Log.i(TAG, "open485Sync, no frame, not send");
            return false;
        }
        byte[] recv =  sendAndReceiveFrameSync(frame);
        if (recv == null || recv.length <= 0) {
            return false;
        } else {
            String dataString = DataConvertUtils.convertByteArrayToString(recv, false);
            Log.i(TAG, "open485Sync, recv frame: " + dataString);
            TransferManager.getInstance(sContext).setChannel(new Channel485());
            return true;
        }
    }

    private boolean openPLCSync() {
        Log.i(TAG, "openPLCSync");
        int result = JniMethods.ttyUSBOpen(9600);
        if (result > 0) {
            TransferManager.getInstance(sContext).setChannel(new UsbChannel());
            return true;
        } else {
            return false;
        }
    }

    public byte[] sendAndReceiveFrameSync(byte[] frame) {
        Log.i(TAG, "sendAndReceiveFrameSync");
        if (mStatus == ChannelManagerStatus.BUSY) {
            return null;
        }
        mStatus = ChannelManagerStatus.BUSY;
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
                mStatus = ChannelManagerStatus.IDLE;
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
                    /*
                    String dataString = DataConvertUtils.convertByteArrayToString(recvFrame, false);
                    Log.i(TAG, "sendAndReceiveFrameSync, recev frame: " + dataString);
                    // Infrared open

                    if ("01000500".equals(dataString)) {
                        TransferManager.getInstance(sContext).setChannel(new InfraredChannel());
                    }
                    */
                    mStatus = ChannelManagerStatus.IDLE;
                    return recvFrame;
                } else {
                    mStatus = ChannelManagerStatus.IDLE;
                    return null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "sendAndReceiveFrameSync, error: " + e.getMessage());
        }
        mStatus = ChannelManagerStatus.IDLE;
        return null;
    }
    /************************** new interface ***************************************/
    private boolean powerOn() {
        int retOpen = JniMethods.tryOpenTty();
        if (retOpen >= 0){
            mIsPowerOn = true;
            return true;
        } else {
            return false;
        }
    }

    private boolean powerOff() {
        int retClose = JniMethods.ttyClose();
        if (retClose >= 0) {
            mIsPowerOn = false;
            return true;
        } else {
            return false;
        }
    }

    public boolean channelOnWithParas(Channel channel, Map params) {
        mCurrentChannelType = channel;
        boolean opened = false;
        switch (channel) {
            case CHANNEL_NONE:
                break;
            case CHANNEL_INFRARED:
                mCurrentChannel = sInfraredChannel;
                if (mIsPowerOn){
                    if (mCurrentChannel.setChannelParams(params) == 1) {
                        opened = mCurrentChannel.channelOpen(0);
                    }
                } else {
                    if (powerOn()) {
                        if (mCurrentChannel.setChannelParams(params) == 1) {
                            opened = mCurrentChannel.channelOpen(0);
                        }
                    }
                }
                break;
            case CHANNEL_485:
                mCurrentChannel = s485Channel;
                if (mIsPowerOn){
                    if (mCurrentChannel.setChannelParams(params) == 1) {
                        opened = mCurrentChannel.channelOpen(0);
                    }
                } else {
                    if (powerOn()) {
                        if (mCurrentChannel.setChannelParams(params) == 1) {
                            opened = mCurrentChannel.channelOpen(0);
                        }
                    }
                }
                break;
            case CHANNEL_PLC:
                mCurrentChannel = sUsbChannel;
                opened = mCurrentChannel.channelOpen(0);
                break;
            case CHANNEL_LORA:
                break;
            case CHANNEL_NET:
                break;
            default:
                break;
        }
        if (opened) {
            TransferManager.getInstance(sContext).setChannel(mCurrentChannel);
        }
        return opened;

    }

    public boolean channelOn(Channel channel) {
        mCurrentChannelType = channel;
        boolean opened = false;
        switch (channel) {
            case CHANNEL_NONE:
                break;
            case CHANNEL_INFRARED:
                mCurrentChannel = sInfraredChannel;
                if (mIsPowerOn){
                    opened = mCurrentChannel.channelOpen(0);
                } else {
                    if (powerOn()) {
                        opened = mCurrentChannel.channelOpen(0);
                    }
                }
                break;
            case CHANNEL_485:
                mCurrentChannel = s485Channel;
                if (mIsPowerOn){
                    opened = mCurrentChannel.channelOpen(0);
                } else {
                    if (powerOn()) {
                        opened = mCurrentChannel.channelOpen(0);
                    }
                }
                break;
            case CHANNEL_PLC:
                mCurrentChannel = sUsbChannel;
                opened = mCurrentChannel.channelOpen(0);
                break;
            case CHANNEL_LORA:
                break;
            case CHANNEL_NET:
                break;
            default:
                break;
        }
        if (opened) {
            TransferManager.getInstance(sContext).setChannel(mCurrentChannel);
        }
        return opened;
    }

    public void channelOff() {
        powerOff();
        JniMethods.closeEth();
        JniMethods.loraClose();
        JniMethods.ttyUSBClose();
        //JniMethods.deinitSecurityUnit();
    }


}
