package com.rk.commonmodule.transfer;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.rk.commonmodule.channel.ChannelConstant;
import com.rk.commonmodule.channel.IChannel;

import java.util.Map;

public class TransferManager {
    private static final String TAG = TransferManager.class.getSimpleName();

    private static Context sContext;

    private IChannel mChannel;

    public enum STATUS {
        IDLE, BYZY
    }

    private STATUS mStatus = STATUS.IDLE;

    private HandlerThread mHandlerTread;
    private NonUIHandler mNonUIHandler;

    private IChannelOpenAndCloseListener mChannelOpenAndCloseListener;
    private IChannelDataTransferListener mChannelDataTransferListener;
    private IChannelParasGetListener mChannelParasGetListener;

    public interface IChannelOpenAndCloseListener {
        void openFail();
        void openSuccess();
        void closeFail();
        void closeSuccess();
    }

    public interface IChannelDataTransferListener {
        void onSendBegin(String msg);
        void onSendFail(String msg);
        void onSendSuccess(String msg);
        void onReceiveBegin(String msg);
        void onReceiveFail(byte[] data);
        void onReceiveSuccess(byte[] data);
        void onReceiveMultipleSuccess(byte[][] datas);
    }

    public interface IChannelParasGetListener {
        void onParamGet(Map map);
    }

    private class NonUIHandler extends Handler {
        public static final int OPEN_CHANNEL_MSG = 0;
        public static final int CLOSE_CHANNEL_MSG = 1;
        public static final int SET_CHANNEL_PARAMS_MSG = 2;
        public static final int GET_CHANNEL_PARAMS_MSG = 3;
        public static final int CHANNEL_SEND_MSG = 4;
        public static final int CHANNEL_RECEICE_MSG = 5;
        public static final int CHANNEL_SEND_MULTIPLE_MSG = 6;
        public NonUIHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage, msg: " + msg.what);
            switch (msg.what) {
                case OPEN_CHANNEL_MSG:
                    if (mChannel != null) {
                        mChannel.channelOpen(0);
                        // TODO: need call listener different interface according to return value of openChannel()
                        if (mChannelOpenAndCloseListener != null) {
                            mChannelOpenAndCloseListener.openSuccess();
                        }
                    } else {
                        if (mChannelOpenAndCloseListener != null) {
                            mChannelOpenAndCloseListener.openFail();
                        }
                    }
                    break;
                case CLOSE_CHANNEL_MSG:
                    if (mChannel != null) {
                        mChannel.channelOpen(0);
                        // TODO: need call listener different interface according to return value of closeChannel()
                        if (mChannelOpenAndCloseListener != null) {
                            mChannelOpenAndCloseListener.closeSuccess();
                        }
                    } else {
                        if (mChannelOpenAndCloseListener != null) {
                            mChannelOpenAndCloseListener.closeFail();
                        }
                    }
                    break;
                case SET_CHANNEL_PARAMS_MSG:
                    break;
                case GET_CHANNEL_PARAMS_MSG:
                    break;
                case CHANNEL_SEND_MSG:
                    if (mChannelDataTransferListener != null) {
                        mChannelDataTransferListener.onSendBegin("Begin to send by channel");
                    }
                    byte[] data = (byte[]) msg.obj;
                    if (mChannel != null && data != null && data.length > 0) {
                        int ret = mChannel.channelSend(data, data.length);
                        if (ret >= 0) {
                            if (mChannelDataTransferListener != null) {
                                mChannelDataTransferListener.onSendSuccess("Send successfully by channel");
                                mChannelDataTransferListener.onReceiveBegin("Begin to receive by channel");
                            }

                            byte[] recvByteArray = mChannel.channelReceive();
                            if (recvByteArray != null && recvByteArray.length > 0) {
                                if (mChannelDataTransferListener != null) {
                                    mChannelDataTransferListener.onReceiveSuccess(recvByteArray);
                                }
                            } else {
                                if (mChannelDataTransferListener != null) {
                                    mChannelDataTransferListener.onReceiveFail(data);
                                }
                            }
                        } else {
                            if (mChannelDataTransferListener != null) {
                                mChannelDataTransferListener.onSendFail("Send fail by channel");
                            }
                        }

                    } else {
                        if (mChannelDataTransferListener != null) {
                            mChannelDataTransferListener.onSendFail("Send fail by channel");
                        }
                    }
                    break;
                case CHANNEL_RECEICE_MSG:
                    break;
                case CHANNEL_SEND_MULTIPLE_MSG:
                    if (mChannelDataTransferListener != null) {
                        mChannelDataTransferListener.onSendBegin("Begin to send by channel");
                    }
                    byte[][] datas = (byte[][]) msg.obj;
                    byte[][] recvDatas = new byte[datas.length][];
                    if (mChannel != null && datas != null && datas.length > 0) {
                        for (int i = 0; i < datas.length; i++) {
                            if (datas[i] != null && datas[i].length > 0) {
                                int ret = mChannel.channelSend(datas[i], datas[i].length);
                                if (ret >= 0) {
                                    if (mChannelDataTransferListener != null) {
                                        mChannelDataTransferListener.onSendSuccess("Send successfully by channel," + i + " time");
                                        mChannelDataTransferListener.onReceiveBegin("Begin to receive by channel," + i + " time");
                                    }

                                    byte[] recvByteArray = mChannel.channelReceive();
                                    if (recvByteArray != null && recvByteArray.length > 0) {
                                        recvDatas[i] = recvByteArray;
                                        break;
                                    } else {
                                        recvDatas[i] = null;
                                        if (mChannelDataTransferListener != null) {
                                            mChannelDataTransferListener.onReceiveFail(datas[i]);
                                        }
                                    }
                                } else {
                                    recvDatas[i] = null;
                                    if (mChannelDataTransferListener != null) {
                                        mChannelDataTransferListener.onSendFail("Send fail by channel");
                                    }
                                }
                            }
                        }
                        mChannelDataTransferListener.onReceiveMultipleSuccess(recvDatas);

                    } else {
                        if (mChannelDataTransferListener != null) {
                            mChannelDataTransferListener.onSendFail("Send fail by channel");
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
            mStatus = STATUS.IDLE;
        }
    }

    private TransferManager() {
        mHandlerTread = new HandlerThread("Transfer Thread");
        mHandlerTread.start();
        mNonUIHandler = new NonUIHandler(mHandlerTread.getLooper());
    }

    private static class InstanceHolder {
        private static final TransferManager INSTANCE = new TransferManager();
    }

    public static TransferManager getInstance(Context context) {
        sContext = context.getApplicationContext();
        return InstanceHolder.INSTANCE;
    }

    public boolean setChannel(IChannel channel) {
        if (mStatus == STATUS.IDLE) {
            if (mChannel != null) {
                if (mChannel.getChannelType() != channel.getChannelType()) {
                    mChannel = channel;
                }
            } else {
                mChannel = channel;
            }
            return true;
        } else {
            return false;

        }
    }

    public ChannelConstant.Channel getChannelType() {
        if (mChannel != null) {
            return mChannel.getChannelType();
        } else {
            return ChannelConstant.Channel.CHANNEL_NONE;
        }
    }

    public void openChannel() {

    }

    public void closeChannel() {

    }

    public void send(byte[] data, int length) {
        if (mStatus == STATUS.IDLE) {
            mStatus = STATUS.BYZY;

        } else {
            Log.i(TAG, "send, not send because of channel buzy");
        }
    }

    public void sendMultiple(byte[][] datas) {
        if (mStatus == STATUS.IDLE) {
            mStatus = STATUS.BYZY;
            mNonUIHandler.removeMessages(NonUIHandler.CHANNEL_SEND_MULTIPLE_MSG);
            mNonUIHandler.sendMessage(mNonUIHandler.obtainMessage(NonUIHandler.CHANNEL_SEND_MULTIPLE_MSG, datas));
        } else {
            Log.i(TAG, "send, not send multiple because of channel buzy");
        }
    }

    private Object mObjSync = new Object();

    public byte[] sendAndReceiveSync(byte[] data) {
        if (isMainThread()) {
            throw new SecurityException();
        }
        byte[] recvDatas = null;
        if (mStatus == STATUS.IDLE) {
            mStatus = STATUS.BYZY;
            if (mChannel != null && data != null && data.length > 0) {
                int ret = mChannel.channelSend(data, data.length);
                if (ret >= 0) {
                    recvDatas = mChannel.channelReceive();
                }
            }
            mStatus = STATUS.IDLE;
        } else {
            Log.i(TAG, "sendAndReceiveSync, not send, because of channel buzy");
        }
        return recvDatas;
    }

    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public byte[] receive() {
        if (isMainThread()) {
            throw new SecurityException();
        }
        byte[] recvDatas = null;
        if (mStatus == STATUS.IDLE) {
            mStatus = STATUS.BYZY;
            recvDatas = mChannel.channelReceive();
            mStatus = STATUS.IDLE;
        } else {
            Log.i(TAG, "receive, not send, because of channel buzy");
        }
        return recvDatas;
    }

    public void setChannelParams() {
        mStatus = STATUS.BYZY;
    }

    public void getChannelParams() {
        mStatus = STATUS.BYZY;
    }

    public void setChannelOpenAndCloseListener(IChannelOpenAndCloseListener listener) {
        if (mStatus == STATUS.IDLE) {
            this.mChannelOpenAndCloseListener = listener;
        }
    }

    public void setChannelDataTransferListener(IChannelDataTransferListener listener) {
        if (mStatus == STATUS.IDLE) {
            this.mChannelDataTransferListener = listener;
        }
    }

    public void setChannelParasGetListener(IChannelParasGetListener listener) {
        if (mStatus == STATUS.IDLE) {
            this.mChannelParasGetListener = listener;
        }
    }

    public STATUS getCurrentStatus() {
        return mStatus;
    }
}
