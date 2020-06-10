package com.rk.commonmodule.protocol.protocol698;

import android.util.Log;

import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.GET_RECORD_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.OAD_KEY;

public enum Protocol698 {
    PROTOCOL_698;
    private static final String TAG = Protocol698.class.getSimpleName();

    public byte[] makeFrame(Protocol698Frame.CtrlArea ctrlArea, Protocol698Frame.AddressArea addressArea, byte[] apdu) {
        ArrayList<Byte> byteArray = new ArrayList<>();
        if (ctrlArea != null && addressArea != null && addressArea.data != null && addressArea.data.length > 0 && apdu != null) {
            byteArray.add((byte)0x68);
            int length = 2 + 1 + addressArea.data.length + 2 + apdu.length + 2;
            Protocol698Frame.Length_Area length_area = new Protocol698Frame.Length_Area(Protocol698Frame.FRAME_UNIT.BYTE_UNIT, length);
            Log.i(TAG, "makeFrame, length_area: " + DataConvertUtils.convertByteArrayToString(length_area.data, false));
            if (length_area.data != null && length_area.data.length == 2) {
                for (int i = 0; i < length_area.data.length; i++) {
                    byteArray.add(length_area.data[i]);
                }

            } else {
                return null;
            }
            byteArray.add(ctrlArea.data);
            for (int i = 0; i < addressArea.data.length; i++) {
                byteArray.add(addressArea.data[i]);
            }

            byte[] headData = new byte[byteArray.size() - 1];
            for (int i = 0; i < headData.length; i++) {
                headData[i] = byteArray.get(i + 1);
            }
            byte[] hCs = new byte[2];
            boolean calcHCSRight = calculateCs(ProtocolConstant.INIT_FCS, headData, hCs);
            Log.i(TAG, "makeFrame, calcHCSRight: " + calcHCSRight + ", hCs: " + hCs);
            if (calcHCSRight) {
                byteArray.add(hCs[0]);
                byteArray.add(hCs[1]);

                for (int i = 0; i < apdu.length; i++) {
                    byteArray.add(apdu[i]);
                }

                byte[] frameData = new byte[byteArray.size() - 1];
                for (int i = 0; i < frameData.length; i++) {
                    frameData[i] = byteArray.get(i + 1);
                }

                byte[] fCs = new byte[2];
                boolean calcFCSRight = calculateCs(ProtocolConstant.INIT_FCS, frameData, fCs);
                if (calcFCSRight) {
                    byteArray.add(fCs[0]);
                    byteArray.add(fCs[1]);
                    byteArray.add((byte)0x16);

                    byte[] bytes = new byte[byteArray.size()];

                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = byteArray.get(i);
                    }
                    return bytes;

                } else {
                    return null;
                }


            } else {
                return null;
            }


        }
        return null;

    }


    public byte[] makeAPDU(int first_id, int second_id, Map map) {
        ArrayList<Byte> byteArray = new ArrayList<>();

        switch (first_id) {
            case ProtocolConstant.CLIENT_APDU.GET_REQUEST.CLASS_ID:
                byteArray.add((byte) ProtocolConstant.CLIENT_APDU.GET_REQUEST.CLASS_ID);
                switch (second_id) {
                    case ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NORMAL.CLASS_ID:
                        byteArray.add((byte) ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NORMAL.CLASS_ID);
                        if (map != null && map.containsKey(ProtocolConstant.PIID_KEY)) {
                            Protocol698Frame.PIID piid = (Protocol698Frame.PIID) map.get(ProtocolConstant.PIID_KEY);
                            if (piid != null) {
                                byteArray.add(piid.data);
                            } else {
                                byteArray.add((byte)0x00); // set default priority and service number
                            }

                        } else {
                            byteArray.add((byte)0x00); // set default priority and service number
                        }
                        if (map != null && map.containsKey(OAD_KEY) && map.get(OAD_KEY) != null) {
                            Protocol698Frame.OAD oad = (Protocol698Frame.OAD) map.get(OAD_KEY);
                            if (oad != null && oad.data != null) {
                                for (int i = 0; i < oad.data.length; i++) {
                                    byteArray.add(oad.data[i]);
                                }
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                        if (map != null && map.containsKey(ProtocolConstant.TIME_LABLE_KEY)) {
                            Protocol698Frame.TimeTag timeTag = (Protocol698Frame.TimeTag) map.get(ProtocolConstant.TIME_LABLE_KEY);
                            if (timeTag != null && timeTag.data != null) {
                                for (int i = 0; i < timeTag.data.length; i++) {
                                    byteArray.add(timeTag.data[i]);
                                }
                            } else {
                                byteArray.add((byte) 0x00);
                            }

                        } else {
                            byteArray.add((byte)0x00);
                        }
                        byte[] bytes = new byte[byteArray.size()];
                        for (int i = 0; i < bytes.length; i++) {
                            bytes[i] = byteArray.get(i);
                        }
                        return bytes;
                }
                break;
            case ProtocolConstant.CLIENT_APDU.CONNECT_REQUEST.CLASS_ID:
                break;
            case ProtocolConstant.CLIENT_APDU.RELEASE_REQUEST.CLASS_ID:
                break;
            case ProtocolConstant.CLIENT_APDU.SET_REQUEST.CLASS_ID:
                break;
            case ProtocolConstant.SECURITY_APDU.SECURITY_REQUEST.CLASS_ID:
                byteArray.add((byte) ProtocolConstant.SECURITY_APDU.SECURITY_REQUEST.CLASS_ID);
                if (map != null && map.containsKey(ProtocolConstant.PIID_KEY)) {
                    Protocol698Frame.PIID piid = (Protocol698Frame.PIID) map.get(ProtocolConstant.PIID_KEY);
                    if (piid != null) {
                        byteArray.add(piid.data);
                    } else {
                        byteArray.add((byte)0x00); // set default priority and service number
                    }

                } else {
                    byteArray.add((byte)0x00); // set default priority and service number
                }

                if (map != null && map.containsKey(GET_RECORD_KEY) && map.get(GET_RECORD_KEY) != null) {
                    Protocol698Frame.GetRecord getRecord = (Protocol698Frame.GetRecord) map.get(OAD_KEY);
                    if (getRecord != null && getRecord.data != null) {
                        for (int i = 0; i < getRecord.data.length; i++) {
                            byteArray.add(getRecord.data[i]);
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
                if (map != null && map.containsKey(ProtocolConstant.TIME_LABLE_KEY)) {
                    Protocol698Frame.TimeTag timeTag = (Protocol698Frame.TimeTag) map.get(ProtocolConstant.TIME_LABLE_KEY);
                    if (timeTag != null && timeTag.data != null) {
                        for (int i = 0; i < timeTag.data.length; i++) {
                            byteArray.add(timeTag.data[i]);
                        }
                    } else {
                        byteArray.add((byte) 0x00);
                    }

                } else {
                    byteArray.add((byte)0x00);
                }
                byte[] bytes = new byte[byteArray.size()];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = byteArray.get(i);
                }
                return bytes;
        }
        return null;
    }


    public byte makeCtrlArea(Protocol698Frame.DIR_PRM dir_prm, boolean isSplitedFrame, boolean isScramble, int funCode) {
        byte data = (byte) 0xFF;
        switch (dir_prm) {
            case SERVER_REPORT:
                data = (byte) (data & 0x80);
                break;
            case SERVER_RESPONSE:
                data = (byte) (data & 0xC0);
                break;
            case CLIENT_REQUEST:
                data = (byte) (data & 0x40);
                break;
            case CLIENT_RESPONSE:
                data = (byte) (data & 0x00);
                break;
        }
        if (isSplitedFrame) {
            data = (byte) (data | 0x20);
        } else {
            data = (byte) (data & 0xDF);
        }

        if (isScramble) {
            data = (byte) (data | 0x08);
        } else {
            data = (byte) (data & 0xF7);
        }
        data = (byte) (data | (0x07 & funCode));
        return data;
    }

    /**
     * if cs is null, use this function to calculate the Cs value.
     * if cs is non-null, use this function to verify the Cs value.
     * @param initCs
     * @param data
     * @param cs
     * @return
     */

    public boolean calculateCs(int initCs, byte[] data, byte[] cs) {
        Log.i(TAG, "calculateCs, data: " + DataConvertUtils.convertByteArrayToString(data, false));
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                initCs = (((initCs & 0xFFFF) >> 8) ^ ProtocolConstant.FCS_TAB[((initCs & 0xFFFF) ^ data[i]) & 0xFF]) & 0xFFFF;
            }
            initCs = initCs ^ 0xFFFF;
            if (cs != null) {
                if (cs.length == 2) {
                    cs[0] = (byte) (initCs & 0xFF);
                    cs[1] = (byte) ((initCs >> 8) & 0xFF);
                    Log.i(TAG, "calculateCs, cs value: " + initCs + ", cs: " + DataConvertUtils.convertByteArrayToString(cs, false));
                    return true;
                } else {
                    return false;
                }

            } else {
                return false;
            }

        }
        return false;
    }

    public boolean verifyCs(int initCs, byte[] data, byte[] cs) {
        Log.i(TAG, "verifyCs, data: " + DataConvertUtils.convertByteArrayToString(data, false)
                + ", verified cs: " + DataConvertUtils.convertByteArrayToString(cs, false));
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                initCs = (((initCs & 0xFFFF) >> 8) ^ ProtocolConstant.FCS_TAB[((initCs & 0xFFFF) ^ data[i]) & 0xFF]) & 0xFFFF;
            }
            initCs = initCs ^ 0xFFFF;
            if (cs != null) {
                if (cs.length == 2) {
                    byte cs0 = (byte) (initCs & 0xFF);
                    byte cs1 = (byte) ((initCs >> 8) & 0xFF);
                    Log.i(TAG, "verifyCs, cs0: " + DataConvertUtils.convertByteToString(cs0)
                            + ", cs1: " + DataConvertUtils.convertByteToString(cs1));
                    if (cs0 == cs[0] && cs1 == cs[1]) {
                        return true;
                    }
                } else {
                    return false;
                }

            } else {
                return false;
            }

        }
        return false;
    }

    public int mBeginPos = 0;
    public int mLengthPos0 = 0;
    public int mLengthPos1 = 0;
    public int mCtrlAreaPos = 0;
    public int mAddressAreaBeginPos = 0;
    public int mAddressAreaEndPos = 0;
    public int mHCsPos0 = 0;
    public int mHCsPos1 = 0;
    public int mApduBegin = 0;
    public int mApduEnd = 0;
    public int mFCsPos0 = 0;
    public int mFCsPos1 = 0;
    public int mEndPos = 0;
    public boolean verify698Frame(byte[] frame) {
        if (frame == null || frame.length < 12) {
            return false;
        }

        for (mBeginPos = 0; mBeginPos < frame.length; mBeginPos++) {
            if (frame[mBeginPos] == 0x68) {
                break;
            }
        }
        if (mBeginPos == frame.length) {
            return false;
        }

        if (mBeginPos + 3 >= (frame.length - 1)) {
            return false;
        }

        mLengthPos0 = mBeginPos + 1;
        mLengthPos1 = mLengthPos0 + 1;

        byte[] lengthData = new byte[2];
        lengthData[0] = frame[mLengthPos0];
        lengthData[1] = frame[mLengthPos1];
        int frameLength = 0;

        Protocol698Frame.Length_Area length_area = new Protocol698Frame.Length_Area(lengthData);

        switch (length_area.frame_unit) {
            case BYTE_UNIT:
                frameLength = length_area.length;
                break;
            case KBYTE_UNIT:
                frameLength = length_area.length * 1024;
                break;
        }
        Log.i(TAG, "verify698Frame, frame length: " + frameLength);
        if (frameLength == 0) {
            return false;
        }

        mCtrlAreaPos = mLengthPos1 + 1;

        if (mCtrlAreaPos + 1 >= (frame.length - 1)) {
            return false;
        }

        mAddressAreaBeginPos = mCtrlAreaPos + 1;

        int addrLength = (frame[mAddressAreaBeginPos] & 0x0F) + 1;

        Log.i(TAG, "verify698Frame, serve address length: " + addrLength + ", address begin position: " + mAddressAreaBeginPos);

        if ((mAddressAreaBeginPos + addrLength + 1) >= (frame.length - 1)) {
            return false;
        }
        mAddressAreaEndPos = mAddressAreaBeginPos + addrLength + 1;

        if ((mAddressAreaEndPos + 1 + 1) >= (frame.length - 1)) {
            return false;
        }

        mHCsPos0 = mAddressAreaEndPos + 1;
        mHCsPos1 = mAddressAreaEndPos + 2;

        byte[] hCs = new byte[2];
        hCs[0] = frame[mHCsPos0];
        hCs[1] = frame[mHCsPos1];

        boolean verifyHCs = verifyCs(ProtocolConstant.INIT_FCS, DataConvertUtils.getSubByteArray(frame, mLengthPos0, mAddressAreaEndPos), hCs);
        if (!verifyHCs) {
            return false;
        }

        if (mHCsPos1 + 1 >= frame.length - 1) {
            return false;
        }

        mApduBegin = mHCsPos1 + 1;

        mApduEnd = mBeginPos + frameLength - 1 - 1;

        if (mApduEnd < mApduBegin) {
            return false;
        }

        if (mApduEnd + 1 + 1 >= frame.length - 1) {
            return false;
        }
        mFCsPos0 = mApduEnd + 1;
        mFCsPos1 = mApduEnd + 2;

        byte[] fCs = new byte[2];
        fCs[0] = frame[mFCsPos0];
        fCs[1] = frame[mFCsPos1];

        boolean verifyFCs = verifyCs(ProtocolConstant.INIT_FCS, DataConvertUtils.getSubByteArray(frame, mLengthPos0, mApduEnd), fCs);
        if (!verifyFCs) {
            return false;
        }

        if (mFCsPos1 + 1 > frame.length - 1) {
            return false;
        }

        mEndPos = mFCsPos1 + 1;

        if (frame[mEndPos] != 0x16) {
            return false;
        }
        Log.i(TAG, "verify698Frame, verfiy OK.");
        return true;

    }

    public Map parseApud(byte[] apduFrame) {
        if (apduFrame == null || apduFrame.length <= 0) {
            return null;
        }
        Log.i(TAG, "parseApud, apud: " + DataConvertUtils.convertByteArrayToString(apduFrame, false));

        Map map = new HashMap();

        switch (((int) apduFrame[0]) & 0xFF) {
            case ProtocolConstant.SERVER_APDU.GET_RESPONSE.CLASS_ID:
                if (apduFrame.length < 2) {
                    return null;
                }
                switch (((int) apduFrame[1]) & 0xFF) {
                    case ProtocolConstant.SERVER_APDU.GET_RESPONSE.GET_RESPONSE_NORMAL.CLASS_ID:
                        if (apduFrame.length < 3) {
                            return null;
                        }
                        map.put(ProtocolConstant.PIID_ACD_KEY, new Protocol698Frame.PIID_ACD(apduFrame[2]));
                        if (apduFrame.length < 7) {
                            return null;
                        }
                        map.put(OAD_KEY, new Protocol698Frame.OAD(DataConvertUtils.getSubByteArray(apduFrame, 3, 6)));
                        if (apduFrame.length < 8) {
                            return null;
                        }
                        if (apduFrame[7] == 0) {
                            if (apduFrame.length < 9) {
                                return null;
                            } else {
                                map.put(ProtocolConstant.DAR_KEY, apduFrame[8]);
                                return map;
                            }

                        } else if (apduFrame[7] == 1) {
                            if (apduFrame.length < 9) {
                                return null;
                            } else {
                                Log.i(TAG, "paseApdu, value type: " + (int) apduFrame[8]);
                                switch ((int) apduFrame[8]) {
                                    case 9:
                                        if (apduFrame.length < 10) {
                                            return null;
                                        }

                                        if ((apduFrame[9] & 0x80) == 0x00) {
                                            if ((9 + apduFrame[9]) <= (apduFrame.length - 1)) {
                                                map.put("value", DataConvertUtils.convertByteArrayToString(
                                                        DataConvertUtils.getSubByteArray(apduFrame, 10, 10 + apduFrame[9] - 1), false));
                                                return map;
                                            } else {
                                                return null;
                                            }
                                        } else {
                                            int lengthSize = apduFrame[9] & 0x7F;
                                            if (9 + lengthSize <= apduFrame.length - 1) {
                                                int length = 0;
                                                for (int i = 0; i < lengthSize; i++) {
                                                    length = length * 256 + apduFrame[10 + i];
                                                }
                                                if (9 + lengthSize + length <= apduFrame.length - 1) {
                                                    map.put("value", DataConvertUtils.convertByteArrayToString(
                                                            DataConvertUtils.getSubByteArray(apduFrame, 9 + lengthSize + 1, 9 + lengthSize + length),
                                                            false
                                                    ));
                                                    return map;
                                                } else {
                                                    return null;
                                                }
                                            } else {
                                                return null;
                                            }
                                        }
                                    case 28:
                                        if (apduFrame.length < 18) {
                                            return null;
                                        }
                                        Protocol698Frame.DateTimeS dateTimeS = new Protocol698Frame.DateTimeS(DataConvertUtils.getSubByteArray(apduFrame, 9, 15));
                                        map.put("value", dateTimeS);
                                        return map;
                                    case 1: //array
                                        Protocol698Frame.OAD oad = (Protocol698Frame.OAD) map.get(ProtocolConstant.OAD_KEY);
                                        String oadString = DataConvertUtils.convertByteArrayToString(oad.data, false);
                                        Log.i(TAG, "parseApdu, oad: " + oad);
                                        if (apduFrame.length < 10) {
                                            return map;
                                        }

                                        if ((apduFrame[9] & 0x80) == 0x00) {
                                            int arrayLen = apduFrame[9];
                                            switch (oadString) {
                                                case "00100200": //正向有功总电能组
                                                    double[] array = new double[arrayLen];
                                                    if ((9 + arrayLen * 5) > (apduFrame.length - 1)) {
                                                        return map;
                                                    }
                                                    for (int i = 0; i < arrayLen; i++) {
                                                        array[i] = parse_DoubleLongUnsigned(DataConvertUtils.getSubByteArray(apduFrame, 9 + i * 5 + 1 + 1, 9 + i * 5 + 5), -2);
                                                    }
                                                    map.put("array", array);
                                                    break;
                                                default:
                                                    Log.i(TAG, "default parse oad：" + oadString);
                                                    break;

                                            }
                                        } else {
                                            int lengthSize = apduFrame[9] & 0x7F;
                                            if (9 + lengthSize <= apduFrame.length - 1) {
                                                int length = 0;
                                                for (int i = 0; i < lengthSize; i++) {
                                                    length = length * 256 + apduFrame[10 + i];
                                                }
                                                if (9 + lengthSize + length <= apduFrame.length - 1) {
                                                    //TODO:
                                                } else {
                                                    return null;
                                                }
                                            } else {
                                                return null;
                                            }
                                        }

                                        return map;
                                    case 6: //double-long-unsigned
                                        Protocol698Frame.OAD oad1 = (Protocol698Frame.OAD) map.get(ProtocolConstant.OAD_KEY);
                                        String oadString1 = DataConvertUtils.convertByteArrayToString(oad1.data, false);
                                        Log.i(TAG, "parseApdu, oad1: " + oadString1);
                                        if (8 + 4 > apduFrame.length - 1) {
                                            return map;
                                        }
                                        switch (oadString1.substring(0, 4)) {
                                            case "0010": //正向有功电能
                                                double power = parse_DoubleLongUnsigned(DataConvertUtils.getSubByteArray(apduFrame, 9, 12), -2);
                                                map.put("value", power);
                                                return map;
                                                //break;
                                        }
                                        break;
                                }
                            }

                        } else {
                            Log.i(TAG, "TST 877");
                            return null;
                        }
                        break;
                    default:
                        Log.i(TAG, "parseApud, no action 1: " + (((int) apduFrame[1]) & 0xFF));

                        break;
                }
                break;
            default:
                Log.i(TAG, "parseApud, no action: " + (((int) apduFrame[0]) & 0xFF));
                break;

        }

        return null;
    }

    private double parse_DoubleLongUnsigned(byte[] data, int divisor) {
        if (data == null || data.length != 4) {
            return 0.0;
        }

        long value = 0 & 0x00000000;

        value = value | (data[3] & 0xFF);
        value = value | ((data[2] << 8) & 0xFF00);
        value = value | ((data[1] << 16) & 0xFF0000);
        value = value | ((data[0] << 24) & 0xFF000000);
        value = value & 0xFFFFFFFF;
        Log.i(TAG, "parse_DoubleLongUnsigned, value: " + value);
        return value * Math.pow(10, divisor * 1);
    }

}
