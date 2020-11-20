package com.rk.commonmodule.protocol.protocol3761;

import android.util.Log;

import com.rk.commonlib.util.LogUtils;

import java.util.ArrayList;

public enum Protocol3761 {
    PROTOCOL_3761;
    private static final String TAG = Protocol3761.class.getSimpleName();
    public enum DIR {
        MASTER_STATION,
        TERMINAL,
    }
    public enum PRM {
        FROM_MASTER,
        FROM_SLAVE,
    }
    public enum FCV {
        AVAILABLE,
        UNAVAILABLE,
    }

    public enum FCB_OR_ACD {
        AVAILABLE,
        UNAVAILABLE,
    }
    public enum FUNCTION_CODE {
        VALUE_0,
        VALUE_1,
        VALUE_2,
        VALUE_3,
        VALUE_4,
        VALUE_5,
        VALUE_6,
        VALUE_7,
        VALUE_8,
        VALUE_9,
        VALUE_10,
        VALUE_11,
        VALUE_12,
        VALUE_13,
        VALUE_14,
        VALUE_15,
    }

    public byte makeCtrlArea(DIR dir, PRM prm, FCB_OR_ACD fcb_or_acd, FCV fcv, FUNCTION_CODE function_code) {
        byte ctrlArea = 0x00;
        switch (dir) {
            case TERMINAL:
                ctrlArea = (byte) (ctrlArea | 0x80);
                break;
            case MASTER_STATION:
                ctrlArea = (byte) (ctrlArea & 0x7F);
                break;
        }
        switch (prm) {
            case FROM_MASTER:
                ctrlArea = (byte) (ctrlArea | 0x40);
                break;
            case FROM_SLAVE:
                ctrlArea = (byte) (ctrlArea & 0xBF);
                break;
        }
        switch (fcb_or_acd) {
            case AVAILABLE:
                ctrlArea = (byte) (ctrlArea | 0x20);
                break;
            case UNAVAILABLE:
                ctrlArea = (byte) (ctrlArea & 0xDF);
                break;
        }
        switch (fcv) {
            case AVAILABLE:
                ctrlArea = (byte) (ctrlArea | 0x10);
                break;
            case UNAVAILABLE:
                ctrlArea = (byte) (ctrlArea & 0xEF);
                break;
        }
        byte funCode = (byte) (function_code.ordinal() & 0x0F);
        ctrlArea = (byte)(ctrlArea | funCode);
        return ctrlArea;
    }

    public byte[] makeAddressArea(String A1, int A2, int A3) {
        Log.i(TAG, "makeAddressArea, A1: " + A1 + ", A2: " + A2 + ", A3: " + A3);
        if (A1 == null || A1.length() != 4 || !isNumberString(A1)
                || A2 < 1 || A2 > 65535
                || A3 < 0 || A3 > 127) {
            return null;

        }
        byte[] address = new byte[5];
        address[0] = (byte) Integer.parseInt(A1.substring(2, 4), 16);
        address[1] = (byte) Integer.parseInt(A1.substring(0, 2), 16);

        address[2] = (byte) (A2 & 0xFF);
        address[3] = (byte) ((A2>>8) & 0xFF);

        address[4] = (byte) (A3 & 0xFF);

        return address;
    }

    public boolean isNumberString(String s) {
        if (s != null) {
            int length = s.length();
            for (int i = 0; i < length; i++) {
                if (!((s.charAt(i) >= '0' && s.charAt(i) <= '9')
                        || (s.charAt(i) >= 'a' && s.charAt(i) <= 'f')
                        || (s.charAt(i) >= 'A' && s.charAt(i) <= 'F'))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public byte[] makeFrame(byte[] userDataArea) {
        if (userDataArea != null && userDataArea.length >= 8) {

            byte[] frame = new byte[1 + 1 + 1 + 1 + 4 + userDataArea.length];
            frame[0] = 0x68;
            Protocol3761Frame.LengthArea lengthArea = new Protocol3761Frame.LengthArea(0, 1, userDataArea.length);
            System.arraycopy(lengthArea.data, 0, frame, 1, lengthArea.data.length);
            System.arraycopy(lengthArea.data, 0, frame, 1 + lengthArea.data.length, lengthArea.data.length);
            frame[1 + lengthArea.data.length * 2] = 0x68;
            System.arraycopy(userDataArea, 0, frame, 1 + lengthArea.data.length * 2 + 1, userDataArea.length);
            byte cs = calcCs(userDataArea);
            frame[1 + lengthArea.data.length * 2 + 1 + userDataArea.length] = cs;
            frame[1 + lengthArea.data.length * 2 + 1 + userDataArea.length + 1] = 0x16;
            return frame;
        }
        LogUtils.i("frame is null");
        return null;
    }

    public byte[] makeLinkUserData(byte afn, byte seq, Protocol3761Frame.DataUnitID dataUnitID, byte[] aux) {
        if (dataUnitID != null && dataUnitID.data != null) {
            int size = 1 + 1 + dataUnitID.data.length;
            if (aux != null && aux.length > 0) {
                size = size + aux.length;
            }
            byte[] linkUserData = new byte[size];
            linkUserData[0] = afn;
            linkUserData[1] = seq;
            System.arraycopy(dataUnitID.data, 0 , linkUserData, 2, dataUnitID.data.length);
            if (aux != null && aux.length > 0) {
                System.arraycopy(aux, 0 , linkUserData, 1 + 1 + dataUnitID.data.length, aux.length);
            }
            return linkUserData;
        }
        return null;

    }

    public byte[] makeUserDataArea(byte ctrlArea, Protocol3761Frame.AddrArea addrArea, byte[] linkUserData) {
        if (addrArea != null && addrArea.data != null && linkUserData != null) {
            byte[] userDataArea = new byte[1 + addrArea.data.length + linkUserData.length];
            userDataArea[0] = ctrlArea;
            System.arraycopy(addrArea.data, 0, userDataArea, 1, addrArea.data.length);
            System.arraycopy(linkUserData, 0, userDataArea, 1 + addrArea.data.length, linkUserData.length);
            return userDataArea;
        }
        return null;
    }

    private byte calcCs(byte[] userDataAre) {
        int cs = 0;
        if (userDataAre == null || userDataAre.length <=0) {
            return (byte) cs;
        }
        for (int i = 0; i< userDataAre.length; i++) {
            cs = cs + userDataAre[i];
        }

        return (byte) cs;
    }

    public int mFirstBeginPos = 0;
    public int mLengthPos = 0;
    public int mSecondBeginPos = 0;
    public int mCtrlAreaPos = 0;
    public int mAddrAreaPos = 0;
    public int mAFNPos = 0;
    public int mSEQPos = 0;
    public int mLinkUserDataPos = 0;
    public int mCsPos = 0;
    public int mEndPos = 0;

    public boolean verifyFrame(byte[] frame) {
        init();
        if (frame == null || frame.length < 16 /* at least 16 bytes*/) {
            Log.i(TAG, "verifyFrame, frame is null or size is so little");
            return false;
        }
        for (int i = 0; i < frame.length; i++) {
            if (frame[i] == 0x68) {
                mFirstBeginPos = i;
                break;
            }
        }
        if (mFirstBeginPos < 0) {
            Log.i(TAG, "verifyFrame, not find start char");
            return false;
        }

        if (mFirstBeginPos + 2 + 2 + 1 < frame.length) {
            if (frame[mFirstBeginPos + 2 + 2 + 1] == 0x68) {
                mSecondBeginPos = mFirstBeginPos + 5;
            } else {
                Log.i(TAG, "verifyFrame, no second start char");
                return false;
            }
        } else {
            Log.i(TAG, "verifyFrame, verify error 1");
            return false;
        }
        mLengthPos = mFirstBeginPos + 1;
        Protocol3761Frame.LengthArea lengthArea = new Protocol3761Frame.LengthArea(frame, mLengthPos);
        int length = lengthArea.length;
        if (length < 8 /* at least 8 bytes, include CtrlArea(size = 1)/AddrArea(size = 5)/AFN(size = 1)/SEQ(size = 1) */) {
            Log.i(TAG, "verifyFrame, verify error 2");
            return false;
        }

        if ((mSecondBeginPos + length + 1 + 1) < frame.length) {
            if (frame[mSecondBeginPos + length + 2] == 0x16) {
                mEndPos = mSecondBeginPos + length + 2;
            } else {
                Log.i(TAG, "verifyFrame, verify error 3");
                return false;
            }
        } else {
            Log.i(TAG, "verifyFrame, verify error 4");
            return false;
        }
        byte cs = frame[mEndPos - 1];
        byte value = 0x00;

        for (int i = mSecondBeginPos + 1; i <= (mSecondBeginPos + length); i++) {
            value = (byte) (value + frame[i]);
        }
        if (value == cs) {
            mCtrlAreaPos = mSecondBeginPos + 1;
            mAddrAreaPos = mCtrlAreaPos + 1;
            mAFNPos = mAddrAreaPos + 5;
            mSEQPos = mAddrAreaPos + 5 + 1;
            mLinkUserDataPos = mAddrAreaPos + 5;
            mCsPos = mEndPos - 1;
        } else {
            Log.i(TAG, "verifyFrame, verify error 5");
            return false;
        }
        Log.i(TAG, "verifyFrame, verify successfully");
        return true;
    }

    private void init() {
        mFirstBeginPos = -1;
        mLengthPos = -1;
        mSecondBeginPos = -1;
        mCtrlAreaPos = -1;
        mAddrAreaPos = -1;
        mAFNPos = -1;
        mSEQPos = -1;
        mLinkUserDataPos = -1;
        mCsPos = -1;
        mEndPos = -1;
    }


}
