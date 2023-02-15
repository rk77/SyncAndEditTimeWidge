package com.rk.commonmodule.protocol.ttu;

import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;

public class TtuBluetoothFrame {

    public enum FrameType {
        SEG_FRAME_CONFIRM(0),
        MQTT_REQUEST(1),
        MQTT_RESPOND(2),
        SHELL_REQUEST(3),
        SHELL_RESPOND(4),
        FILE_WRITE_REQUEST(5),
        FILE_WRITE_RESPOND(6),
        FILE_READ_REQUEST(7),
        FILE_READ_RESPOND(8),
        BL_CONNECT_SET(9),
        BL_CONNECT_RESPOND(10);

        private final int value;

        private FrameType(int value) {
            this.value = value;
        }

        public static FrameType valueOf(int value) {
            switch (value) {
                case 0:
                    return FrameType.SEG_FRAME_CONFIRM;
                case 1:
                    return FrameType.MQTT_REQUEST;
                case 2:
                    return FrameType.MQTT_RESPOND;
                case 3:
                    return FrameType.SHELL_REQUEST;
                case 4:
                    return FrameType.SHELL_RESPOND;
                case 5:
                    return FrameType.FILE_WRITE_REQUEST;
                case 6:
                    return FrameType.FILE_WRITE_RESPOND;
                case 7:
                    return FrameType.FILE_READ_REQUEST;
                case 8:
                    return FrameType.FILE_READ_RESPOND;
                case 9:
                    return FrameType.BL_CONNECT_SET;
                case 10:
                    return FrameType.BL_CONNECT_RESPOND;
                default:
                    return null;
            }
        }
    }
    public FrameType mFrameType;
    public int mTotalFrameCount;
    public int mSegFrameNum;
    public ITtuBluetoothRawData mRawData;

    public int mLength;

    public byte[] data;

    public TtuBluetoothFrame(FrameType type, int total, int segNum, ITtuBluetoothRawData rawData) {
        this.mFrameType = type;
        this.mTotalFrameCount = total;
        this.mSegFrameNum = segNum;
        this.mRawData = rawData;

        mLength = 8 + ((rawData != null && rawData.getData() != null) ? rawData.getData().length : 0);

        int pos = 0;
        this.data = new byte[mLength];
        this.data[0] = 0x68;
        this.data[1] = (byte) (mLength & 0xFF);
        this.data[2] = (byte) ((mLength >> 8) & 0xFF);
        this.data[3] = (byte) (type.value);
        this.data[4] = (byte) (total);
        this.data[5] = (byte) (segNum);
        pos = pos + 6;
        if (rawData != null && rawData.getData() != null) {
            System.arraycopy(rawData.getData(), 0, this.data, pos, rawData.getData().length);
            pos = pos + rawData.getData().length;
        }
        byte cs = 0;
        for (int i = 0; i < pos; i++) {
            cs = (byte) (cs + this.data[i]);
        }
        this.data[pos] = cs;
        pos = pos + 1;
        this.data[pos] = (byte) 0x16;
    }

    public TtuBluetoothFrame(byte[] frame, int begin) {

        if (!verifyFrame(frame)) {
            LogUtils.i("verify err");
            return;
        }

        mFrameType = FrameType.valueOf(frame[mFrameTypePos]);
        mTotalFrameCount = frame[mTotalPos];
        mSegFrameNum = frame[mSegNumPos];

        LogUtils.i("type: " + mFrameType + ", total: " + mTotalFrameCount + ", seg: " + mSegFrameNum);

        TtuBlutoothRawDataFactory factory = new TtuBlutoothRawDataFactory();
        byte[] rawData = DataConvertUtils.getSubByteArray(frame, mRawDataPos, mCsPos - 1);
        mRawData = factory.parseRawData(mFrameType, rawData);

        this.data = DataConvertUtils.getSubByteArray(frame, mHeadPos, mEndPos);



    }

    private int mHeadPos, mLengthPos, mFrameTypePos, mTotalPos, mSegNumPos, mRawDataPos, mCsPos, mEndPos;

    private void initPos() {
        mHeadPos = -1;
        mLengthPos = -1;
        mFrameTypePos = -1;
        mTotalPos = -1;
        mSegNumPos = -1;
        mRawDataPos = -1;
        mCsPos = -1;
        mEndPos = -1;
    }

    private boolean verifyFrame(byte[] frame) {
        initPos();
        if (frame == null || frame.length < 8) {
            LogUtils.i("frame is null or too short");
            return false;
        }

        for (int i = 0; i < frame.length; i++) {
            if (frame[i] == (byte) 0x68) {
                mHeadPos = i;
                break;
            }
        }
        if (mHeadPos < 0) {
            LogUtils.i("not find frame head.");
            return false;
        }

        if (mHeadPos + 5 > frame.length - 1) {
            LogUtils.i("frame length error");
            return false;
        }

        mLengthPos = mHeadPos + 1;
        mLength = (frame[mLengthPos] & 0xFF) + (frame[mLengthPos + 1] & 0xFF) * 256;

        int end = mHeadPos + mLength - 1;
        if (end > frame.length - 1 || frame[end] != 0x16) {
            LogUtils.i("frame total length error");
            return false;
        }

        mEndPos = end;
        mCsPos = mEndPos - 1;

        byte cs = 0;
        for (int i = mHeadPos; i <= mCsPos - 1; i++) {
            cs = (byte) (frame[i] + cs);
        }

        if (cs != frame[mCsPos]) {
            LogUtils.i("cs verify false");
            return false;
        }

        mFrameTypePos = mLengthPos + 2;

        mTotalPos = mFrameTypePos + 1;
        mSegNumPos = mTotalPos + 1;

        mRawDataPos = mSegNumPos + 1;

        return true;
    }
}
