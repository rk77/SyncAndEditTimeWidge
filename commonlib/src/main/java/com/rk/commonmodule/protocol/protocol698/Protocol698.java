package com.rk.commonmodule.protocol.protocol698;

import android.util.Log;

import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.Map;

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
                        if (map != null && map.containsKey(ProtocolConstant.OAD_KEY) && map.get(ProtocolConstant.OAD_KEY) != null) {
                            Protocol698Frame.OAD oad = (Protocol698Frame.OAD) map.get(ProtocolConstant.OAD_KEY);
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

}
