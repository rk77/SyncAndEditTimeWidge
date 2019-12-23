package com.rk.commonmodule.protocol.protocol645.y2007;

import android.util.Log;

import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.HashMap;
import java.util.Map;

public enum  Protocol645FramePaser {
    PROTOCOL_645_FRAME_PASER;
    private static final String TAG = Protocol645FramePaser.class.getSimpleName();

    private int mFirstBeginPos = 0;
    private int mSecondBeginPos = 0;
    private int mCtrlCodePos = 0;
    private int mDataLengthPos = 0;
    private int mCsPos = 0;
    private int mEndPos = 0;
    public synchronized Map parse(byte[] frame) {
        Log.i(TAG, "parse");
        Map map = new HashMap();

        if (verify645Frame(frame)) {
            Log.i(TAG, "parse, frame verify OK");
            map.put(Protocol645Constant.FRAME_OK, true);
            byte ctrlCode = frame[mCtrlCodePos];
            String address = DataConvertUtils.convertByteArrayToString(frame, mFirstBeginPos + 1, mSecondBeginPos - 1, true);
            map.put(Protocol645Constant.ADDRESS, address);
            map.put(Protocol645Constant.CTRL_CODE, frame[mCtrlCodePos]);
            byte[] data = DataConvertUtils.getSubByteArray(frame, mDataLengthPos + 1, mCsPos - 1);
            if (data != null && data.length > 0) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (data[i] - (byte) 0x33);
                }
            }
            map.put(Protocol645Constant.DATA, data);
            switch (ctrlCode) {
                case Protocol645Constant.ControlCode.READ_ADDRESS_RESPOND_OK:
                    map.put(Protocol645Constant.ControlCode.READ_ADDRESS_VALUE_KEY, DataConvertUtils.convertByteArrayToString(data, true));
                case Protocol645Constant.ControlCode.READ_DATA_RESPOND_OK:
                    if (data == null || data.length < 4) {
                        return null;
                    }
                    byte[] dataID = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        dataID[i] = data[4 -1 - i];
                    }
                    String dataIDString = DataConvertUtils.convertByteArrayToString(dataID, false);
                    map.put(Protocol645Constant.DATA_IDENTIFIER, dataIDString);

                    switch (dataIDString) {
                        case Protocol645Constant.DataIdentifier.POSITIVE_ACTIVE_TOTAL_POWER_DI:
                            if (data.length == 8) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 4; i < 8; i++) {
                                    sb.insert(0, DataConvertUtils.convertByteToString(data[i]));
                                    if (i == 4) {
                                        sb.insert(0, ".");
                                    }
                                }
                                map.put(Protocol645Constant.DataIdentifier.POSITIVE_ACTIVE__TOTAL_POWER_KEY, sb.toString());
                            }

                            break;
                        default:
                            break;

                    }

                    break;
                case Protocol645Constant.ControlCode.READ_DATA_RESPOND_ERROR:
                    break;
                case Protocol645Constant.ControlCode.READ_DATA_RESPOND_OK_CONTINUE:
                    break;
                default:
                    break;

            }
        } else {
            map.put(Protocol645Constant.FRAME_OK, false);
        }
        return map;
    }

    private boolean verify645Frame(byte[] frame) {

        // judge the frame length, at least 12
        if (frame == null || frame.length < 12) {
            return false;
        }

        for (mFirstBeginPos = 0; mFirstBeginPos < frame.length; mFirstBeginPos++) {
            if (frame[mFirstBeginPos] == Protocol645Constant.FRAME_BEGIN) {
                break;
            }
        }
        if (mFirstBeginPos == frame.length) {
            return false;
        }

        mSecondBeginPos = mFirstBeginPos + 7;
        Log.i(TAG, "verify645Frame, mSecondBeginPos: " + mSecondBeginPos);
        if (mSecondBeginPos > frame.length - 1) {
            return false;
        }
        if (frame[mSecondBeginPos] != Protocol645Constant.FRAME_BEGIN) {
            return false;
        }

        mCtrlCodePos = mSecondBeginPos + 1;
        Log.i(TAG, "verify645Frame, mCtrlCodePos: " + mCtrlCodePos);
        if (mCtrlCodePos > frame.length - 1) {
            return false;
        }
        mDataLengthPos = mCtrlCodePos + 1;
        Log.i(TAG, "verify645Frame, mDataLengthPos: " + mDataLengthPos);
        if (mDataLengthPos > frame.length - 1) {
            return false;
        }

        int dataLength = frame[mDataLengthPos];

        mCsPos = mDataLengthPos + dataLength + 1;
        Log.i(TAG, "verify645Frame, mCsPos: " + mCsPos);
        if (mCsPos > frame.length - 1) {
            return false;
        }

        mEndPos =  mCsPos + 1;
        Log.i(TAG, "verify645Frame, mEndPos: " + mEndPos);
        if (mEndPos > frame.length - 1) {
            return false;
        }
        Log.i(TAG, "verify645Frame, mEndCode: " + frame[mEndPos]);
        if (frame[mEndPos] != Protocol645Constant.FRAME_END) {
            return false;
        }

        byte csValue = frame[mFirstBeginPos];

        for (int i = mFirstBeginPos + 1; i <= mCsPos - 1; i++) {
            csValue = (byte) (csValue + frame[i]);
        }
        Log.i(TAG, "verify645Frame, calculate csValue: " + csValue + ", cs in frame: " + frame[mCsPos]);
        if (csValue != frame[mCsPos]) {
            return false;
        }

        return true;
    }
}
