package com.rk.commonmodule.protocol.protocol698;

import java.util.ArrayList;
import java.util.Map;

public enum Protocol698 {
    PROTOCOL_698;
    private static final String TAG = Protocol698.class.getSimpleName();

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
                data = (byte) (data & 0xBF);
                break;
            case SERVER_RESPONSE:
                data = (byte) (data & 0xFF);
                break;
            case CLIENT_REQUEST:
                data = (byte) (data & 0x7F);
                break;
            case CLIENT_RESPONSE:
                data = (byte) (data & 0x3F);
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
        data = (byte) (data & (0x07 & funCode));
        return data;
    }

}
