package com.rk.commonmodule.protocol.southernprowergrid;

import android.util.Log;

public class Protocol13Frame {
    private static final String TAG = Protocol13Frame.class.getSimpleName();
    public final byte mBigin1 = 0x68;
    public byte[] mL0 = new byte[2];
    public byte[] mL1 = new byte[2];
    public final byte mBegin2 = 0x68;
    public byte mCtrlArea;
    public AddressArea mAddressArea;
    public LinkUserData mLinkUserData;
    public byte mCs;
    public final byte mEnd = 0x16;

    public int getLength() {
        int length = 0;
        length = length + 7 + 2;
        if (mAddressArea != null) {
            length = length + mAddressArea.getLength();
        }
        if (mLinkUserData != null) {
            length = length + mLinkUserData.getLength();
        }
        Log.i(TAG, "Frame, length: " + length);
        return length;
    }

    public static class AddressArea {
        public byte[] mLocation = new byte[3];
        public byte[] mTerminalAddr = new byte[3];
        public byte mMasterStationAddr;
        public int getLength() {
            Log.i(TAG, "AddressArea, length: 7");
            return 7;
        }

    }

    public static class LinkUserData {
        public byte mAFN;
        public byte mSEQ;
        public InfoBody[] mInfoBodyArray;
        public byte[] mPW;
        public Tp mTp;

        public int getLength() {
            int length = 0;
            length = length + 2;
            if (mInfoBodyArray != null && mInfoBodyArray.length > 0) {
                for (int i = 0; i < mInfoBodyArray.length; i++) {
                    length = length + mInfoBodyArray[i].getLength();
                }
            }
            if (mPW != null) {
                length = length + 16;
            }
            if (mTp != null) {
                length = length + mTp.getLength();
            }
            Log.i(TAG, "LinkUserData, lenght: " + length);
            return length;
        }

        public static class InfoBody {
            public byte[] mDA = new byte[2];
            public byte[] mDI = new byte[4];
            public byte[] mDIContent;
            public DataTimeArea mDataTimeArea;

            public static class DataTimeArea {
                public byte[] mDataTime1 = new byte[6];
                public byte[] mDataTime2 = new byte[6];
                public byte mDataTimeDensity;
                public int getLength() {
                    return 13;
                }
            }

            public int getLength() {
                int length = 0;
                length = length + 6;
                if (mDIContent != null) {
                    length = length + mDIContent.length;
                }
                if (mDataTimeArea != null) {
                    length = length + mDataTimeArea.getLength();
                }
                Log.i(TAG, "InfoBody length: " + length);
                return length;
            }
        }

        public static class Tp {
            public byte[] mStartFrameSendTimeLable = new byte[4];
            public byte mAllowTransDelayTime;
            public int getLength() {
                return 5;
            }
        }

    }

}
