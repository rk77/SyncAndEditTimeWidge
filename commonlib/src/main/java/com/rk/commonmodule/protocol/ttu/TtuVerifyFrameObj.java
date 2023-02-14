package com.rk.commonmodule.protocol.ttu;

import com.rk.commonlib.bluetooth.IFrameVerify;
import com.rk.commonlib.util.LogUtils;

public class TtuVerifyFrameObj implements IFrameVerify {
    @Override
    public boolean verify(byte[] frame, int begin) {

        int mHeadPos = -1;
        int mLengthPos = -1;
        int mFrameTypePos = -1;
        int mTotalPos = -1;
        int mSegNumPos = -1;
        int mRawDataPos = -1;
        int mCsPos = -1;
        int mEndPos = -1;
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
        int mLength = (frame[mLengthPos] & 0xFF) + (frame[mLengthPos + 1] & 0xFF) * 256;

        int end = mHeadPos + mLength - 1;
        LogUtils.i("end: " + end + ", len: " + mLength);
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
