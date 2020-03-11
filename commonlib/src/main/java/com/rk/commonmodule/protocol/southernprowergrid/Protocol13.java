package com.rk.commonmodule.protocol.southernprowergrid;

import android.util.Log;

import com.rk.commonmodule.protocol.southernprowergrid.Protocol13Frame.LinkUserData;
import com.rk.commonmodule.protocol.southernprowergrid.Protocol13Frame.LinkUserData.InfoBody;
import com.rk.commonmodule.protocol.southernprowergrid.Protocol13Frame.LinkUserData.InfoBody.DataTimeArea;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;

public enum Protocol13 {
    PROTOCOL_13;
    private static final String TAG = Protocol13.class.getSimpleName();
    private static final int FAKE_INFO_BODY_CNT = 100;
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

    public Protocol13Frame.AddressArea makeAddressArea(String A1, int A2, int A3) {
        Log.i(TAG, "makeAddressArea, A1: " + A1 + ", A2: " + A2 + ", A3: " + A3);
        if (A1 == null || A1.length() != 4 || !isNumberString(A1)
                || A2 < 1 || A2 > 16777216
                || A3 < 0 || A3 > 255) {
            return null;

        }
        Protocol13Frame.AddressArea addressArea = new Protocol13Frame.AddressArea();
        addressArea.mLocation[0] = (byte) Integer.parseInt(A1.substring(4, 6), 16);
        addressArea.mLocation[1] = (byte) Integer.parseInt(A1.substring(2, 4), 16);
        addressArea.mLocation[2] = (byte) Integer.parseInt(A1.substring(0, 2), 16);

        addressArea.mTerminalAddr[3] = (byte) (A2 & 0xFF);
        addressArea.mTerminalAddr[4] = (byte) ((A2>>8) & 0xFF);
        addressArea.mTerminalAddr[5] = (byte) ((A2>>16) & 0xFF);

        addressArea.mMasterStationAddr = (byte) (A3 & 0xFF);

        return addressArea;
    }

    public DataTimeArea makeDataTimeArea(String time1, String time2, int density) {
        if (time1 == null || time1.length() != 12 || !isNumberString(time1)
                || time2 == null || time2.length() != 12 || !isNumberString(time2)
                || density < 0) {
            return null;
        }
        DataTimeArea dataTimeArea = new DataTimeArea();
        for (int i = 0; i < 6; i++) {
            dataTimeArea.mDataTime1[i] = (byte) Integer.parseInt(time1.substring(12 - i * 2 - 2, 12 - i * 2), 16);
        }
        for (int i = 0; i < 6; i++) {
            dataTimeArea.mDataTime2[i] = (byte) Integer.parseInt(time2.substring(12 - i * 2 - 2, 12 - i * 2), 16);
        }

        dataTimeArea.mDataTimeDensity = (byte) density;
        return dataTimeArea;
    }

    public InfoBody makeInfoBody(String DA, String DI, byte[] DIContent, DataTimeArea dataTimeArea) {
        if (DA == null || DA.length() != 4 || !isNumberString(DA)
                || DI == null || DI.length() != 8 || !isNumberString(DI)) {
            return null;
        }
        InfoBody infoBody = new InfoBody();
        for (int i = 0; i < 2; i++) {
            infoBody.mDA[i] = (byte) Integer.parseInt(DA.substring(4 - i * 2 - 2, 4 - i * 2), 16);
        }
        for (int i = 0; i < 4; i++) {
            infoBody.mDI[i] = (byte) Integer.parseInt(DA.substring(8 - i * 2 - 2, 8 - i * 2), 16);
        }
        if (DIContent != null) {
            infoBody.mDIContent = DIContent;
        }
        if (dataTimeArea != null) {
            infoBody.mDataTimeArea = dataTimeArea;
        }
        return infoBody;
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

    public Protocol13Frame parse(byte[] frame) {
        if (!verifyFrame(frame)) {
            return null;
        }
        print();
        Protocol13Frame protocol13Frame = new Protocol13Frame();
        protocol13Frame.mL0[0] = frame[mLengthPos];
        protocol13Frame.mL0[1] = frame[mLengthPos + 1];
        protocol13Frame.mL1[0] = frame[mLengthPos + 2];
        protocol13Frame.mL1[1] = frame[mLengthPos + 3];
        protocol13Frame.mCtrlArea = frame[mCtrlAreaPos];
        Protocol13Frame.AddressArea addressArea = new Protocol13Frame.AddressArea();
        addressArea.mLocation[0] = frame[mAddressAreaPos];
        addressArea.mLocation[1] = frame[mAddressAreaPos + 1];
        addressArea.mLocation[2] = frame[mAddressAreaPos + 2];
        addressArea.mTerminalAddr[0] = frame[mAddressAreaPos + 3];
        addressArea.mTerminalAddr[1] = frame[mAddressAreaPos + 4];
        addressArea.mTerminalAddr[2] = frame[mAddressAreaPos + 5];
        addressArea.mMasterStationAddr = frame[mAddressAreaPos + 6];
        protocol13Frame.mAddressArea = addressArea;

        LinkUserData linkUserData = new LinkUserData();
        linkUserData.mAFN = frame[mLinkUserDataPos];
        Log.i(TAG, "parse, mAFN: " + linkUserData.mAFN);
        linkUserData.mSEQ = frame[mLinkUserDataPos + 1];
        Log.i(TAG, "parse, mSEQ: " + linkUserData.mSEQ);
        linkUserData.mInfoBodyArray = null;
        linkUserData.mTp = null;
        linkUserData.mPW = null;
        protocol13Frame.mLinkUserData = linkUserData;
        protocol13Frame.mCs = frame[mCsPos];
///////////////////////////////////////////////////////////////////
        int infoBodyBeginPos = mLinkUserDataPos + 1 + 1;
        if (infoBodyBeginPos >= mCsPos) {
            Log.i(TAG, "parse, TST 1");
            return protocol13Frame;
        }

        ArrayList<InfoBody> infoBodyArrayList = new ArrayList<>();

        int infoBodyEndPos = -1;
        if (hasTp(linkUserData.mSEQ)) {
            if (mCsPos - 5 < infoBodyBeginPos) {
                Log.i(TAG, "parse, TST 2");
                return protocol13Frame;
            }
            LinkUserData.Tp tp = new LinkUserData.Tp();
            tp.mAllowTransDelayTime = frame[mCsPos - 1];
            tp.mStartFrameSendTimeLable[3] = frame[mCsPos - 2];
            tp.mStartFrameSendTimeLable[2] = frame[mCsPos - 3];
            tp.mStartFrameSendTimeLable[1] = frame[mCsPos - 4];
            tp.mStartFrameSendTimeLable[0] = frame[mCsPos - 5];
            linkUserData.mTp = tp;
            if (hasPW()) {
                if (mCsPos - 5 - 16 < infoBodyBeginPos) {
                    Log.i(TAG, "parse, TST 2");
                    return protocol13Frame;
                }
                byte[] pw = new byte[16];
                for (int i = 0; i < 16; i++) {
                    pw[15 - i] = frame[mCsPos - 5 - 1 - i];
                }
                linkUserData.mPW = pw;
                infoBodyEndPos = mCsPos - 5 - 16 - 1;
            } else {
                infoBodyEndPos = mCsPos - 5 - 1;
            }
        } else {
            if (hasPW()) {
                if (mCsPos - 16 < infoBodyBeginPos) {
                    Log.i(TAG, "parse, TST 3");
                    return protocol13Frame;
                }
                byte[] pw = new byte[16];
                for (int i = 0; i < 16; i++) {
                    pw[15 - i] = frame[mCsPos - 1 - i];
                }
                linkUserData.mPW = pw;
                infoBodyEndPos = mCsPos - 16 - 1;
            } else {
                infoBodyEndPos = mCsPos - 1;
            }
        }



        int nextInfoBodyBeginPos = infoBodyBeginPos;

        Log.i(TAG, "parse, nextInfoBodyBeginPos: " + nextInfoBodyBeginPos);
        Log.i(TAG, "parse, infoBodyEndPos: " + infoBodyEndPos);
        if (infoBodyBeginPos <= infoBodyEndPos) {
            for (int i = 0; i <= FAKE_INFO_BODY_CNT; i++) {
                if (nextInfoBodyBeginPos + 5 > infoBodyEndPos) {
                    break;
                }
                InfoBody infoBody = new InfoBody();
                infoBody.mDA[0] = frame[nextInfoBodyBeginPos];
                infoBody.mDA[1] = frame[nextInfoBodyBeginPos + 1];
                infoBody.mDI[0] = frame[nextInfoBodyBeginPos + 2];
                infoBody.mDI[1] = frame[nextInfoBodyBeginPos + 3];
                infoBody.mDI[2] = frame[nextInfoBodyBeginPos + 4];
                infoBody.mDI[3] = frame[nextInfoBodyBeginPos + 5];
                Log.i(TAG, "infoBody[" + i + "], DA: " + DataConvertUtils.convertByteArrayToString(infoBody.mDA, true));
                Log.i(TAG, "infoBody[" + i + "], DI: " + DataConvertUtils.convertByteArrayToString(infoBody.mDI, true));
                infoBody.mDataTimeArea = null;
                infoBody.mDIContent = null;
                infoBodyArrayList.add(infoBody);
                if (hasData()) {
                    int length = getDataLength(linkUserData.mAFN, frame[mCtrlAreaPos], infoBody.mDA, infoBody.mDI, nextInfoBodyBeginPos + 6, infoBodyEndPos);
                    Log.i(TAG, "parse, data length: " + length);
                    if (length < 0) {
                        break;
                    }
                    if (length + 5 + nextInfoBodyBeginPos > infoBodyEndPos) {
                        break;
                    }
                    byte[] data= new byte[length];
                    for (int j = 0; j < length; j++) {
                        data[j] = frame[nextInfoBodyBeginPos + 5 + 1 + j];
                    }
                    infoBody.mDIContent = data;

                    if (hasDataTimeDomain()) {
                        if (nextInfoBodyBeginPos + 5 + length + 13 > infoBodyEndPos) {
                            break;
                        }

                        DataTimeArea dataTimeArea = new DataTimeArea();
                        for (int j = 0; j < 6; j++) {
                            dataTimeArea.mDataTime1[j] = frame[nextInfoBodyBeginPos + 6 + length + j];
                        }
                        for (int j = 0; j < 6; j++) {
                            dataTimeArea.mDataTime2[j] = frame[nextInfoBodyBeginPos + 12 + length + j];
                        }
                        dataTimeArea.mDataTimeDensity = frame[nextInfoBodyBeginPos + 18 + length];
                        infoBody.mDataTimeArea = dataTimeArea;
                        nextInfoBodyBeginPos = nextInfoBodyBeginPos + 18 + length + 1;

                    } else {
                        nextInfoBodyBeginPos = nextInfoBodyBeginPos + 5 + length + 1;
                    }

                } else {
                    if (hasDataTimeDomain()) {
                        if (nextInfoBodyBeginPos + 18 > infoBodyEndPos) {
                            break;
                        }

                        DataTimeArea dataTimeArea = new DataTimeArea();
                        for (int j = 0; j < 6; j++) {
                            dataTimeArea.mDataTime1[j] = frame[nextInfoBodyBeginPos + 6 + j];
                        }
                        for (int j = 0; j < 6; j++) {
                            dataTimeArea.mDataTime2[j] = frame[nextInfoBodyBeginPos + 12 + j];
                        }
                        dataTimeArea.mDataTimeDensity = frame[nextInfoBodyBeginPos + 18];
                        infoBody.mDataTimeArea = dataTimeArea;
                        nextInfoBodyBeginPos = nextInfoBodyBeginPos + 18 + 1;

                    } else {
                        nextInfoBodyBeginPos = nextInfoBodyBeginPos + 5 + 1;
                    }
                }

            }
        }
        if (infoBodyArrayList != null && infoBodyArrayList.size() > 0) {
            InfoBody[] infoBodies = new InfoBody[infoBodyArrayList.size()];
            for (int i = 0; i < infoBodyArrayList.size(); i++) {
                infoBodies[i] = infoBodyArrayList.get(i);

            }
            linkUserData.mInfoBodyArray = infoBodies;
        }
        //InfoBody infoBody = new InfoBody();
        Log.i(TAG, "parse， linkUserData: " + linkUserData.mInfoBodyArray[0].mDIContent.length);
        Log.i(TAG, "parse, TST 4");

        return protocol13Frame;
    }

    private boolean hasData() {
        return true;
    }

    private boolean hasDataTimeDomain() {
        return false;
    }
    private int getDataLength(byte afn, byte ctrlArea, byte[] da, byte[] di, int begin, int end) {
        Log.i(TAG, "getDataLength: afn: " + Integer.toHexString(afn & 0xFF) + ", ctrl area: " + Integer.toHexString(ctrlArea & 0xFF));
        Log.i(TAG, "getDataLength: DA: " + DataConvertUtils.convertByteArrayToString(da, true));
        Log.i(TAG, "getDataLength: DI: " + DataConvertUtils.convertByteArrayToString(di, true));
        if (((byte)(ctrlArea & 0x80)) == (byte) 0x80) {
            if (afn == (byte) 0x21) {
                Log.i(TAG, "DI: " + DataConvertUtils.convertByteArrayToString(di, false).toUpperCase());
                if (DataConvertUtils.convertByteArrayToString(di, false).toUpperCase().equals("122000E0")) {
                    return (end - begin + 1);
                }
            }

        } else {

        }
        return 0;
    }
    private boolean hasPW() {
        return false;
    }

    private boolean hasTp(byte seq) {
        if (((byte)(seq & 0x80)) == (byte) 0x80) {
            return true;
        }
        return false;
    }

    private int mBegin1Pos = -1;
    private int mLengthPos = -1;
    private int mBegin2Pos = -1;
    private int mCtrlAreaPos = -1;
    private int mAddressAreaPos = -1;
    private int mLinkUserDataPos = -1;
    private int mCsPos = -1;
    private int mEndPos = -1;

    private boolean verifyFrame(byte[] frame) {
        init();
        if (frame == null || frame.length < 16 /* at least 16 bytes*/) {
            Log.i(TAG, "verifyFrame, frame is null or size is so little");
            return false;
        }
        for (int i = 0; i < frame.length; i++) {
            if (frame[i] == 0x68) {
                mBegin1Pos = i;
                break;
            }
        }
        if (mBegin1Pos < 0) {
            Log.i(TAG, "verifyFrame, not find start char");
            return false;
        }

        if (mBegin1Pos + 2 + 2 + 1 < frame.length) {
            if (frame[mBegin1Pos + 2 + 2 + 1] == 0x68) {
                mBegin2Pos = mBegin1Pos + 5;
            } else {
                Log.i(TAG, "verifyFrame, no second start char");
                return false;
            }
        } else {
            Log.i(TAG, "verifyFrame, verify error 1");
            return false;
        }
        int L1 = ((frame[mBegin2Pos - 1] << 8) & 0xFF00) | frame[mBegin2Pos - 2];
        int length = L1;
        if (length < 10 /* at least 10 bytes, include C(size = 1)/A(size = 7)/AFN(size = 1)/SEQ(size = 1) */) {
            Log.i(TAG, "verifyFrame, verify error 2");
            return false;
        }

        if ((mBegin2Pos + length + 1 + 1) < frame.length) {
            if (frame[mBegin2Pos + length + 2] == 0x16) {
                mEndPos = mBegin2Pos + length + 2;
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

        for (int i = mBegin2Pos + 1; i <= (mBegin2Pos + length); i++) {
            value = (byte) (value + frame[i]);
        }
        if (value == cs) {
            mLengthPos = mBegin1Pos + 1;
            mCtrlAreaPos = mBegin2Pos + 1;
            mAddressAreaPos = mCtrlAreaPos + 1;
            mLinkUserDataPos = mAddressAreaPos + 7;
            mCsPos = mEndPos - 1;
        } else {
            Log.i(TAG, "verifyFrame, verify error 5");
            return false;
        }
        Log.i(TAG, "verifyFrame, verify successfully");
        return true;
    }

    private void init() {
        mBegin1Pos = -1;
        mLengthPos = -1;
        mBegin2Pos = -1;
        mCtrlAreaPos = -1;
        mAddressAreaPos = -1;
        mLinkUserDataPos = -1;
        mCsPos = -1;
        mEndPos = -1;
    }

    private void print() {
        Log.i(TAG, "mBegin1Pos: " + mBegin1Pos);
        Log.i(TAG, "mLengthPos: " + mLengthPos);
        Log.i(TAG, "mBegin2Pos: " + mBegin2Pos);
        Log.i(TAG, "mCtrlAreaPos: " + mCtrlAreaPos);
        Log.i(TAG, "mAddressAreaPos: " + mAddressAreaPos);
        Log.i(TAG, "mLinkUserDataPos: " + mLinkUserDataPos);
        Log.i(TAG, "mCsPos： " + mCsPos);
        Log.i(TAG, "mEndPos: " + mEndPos);
    }


}
