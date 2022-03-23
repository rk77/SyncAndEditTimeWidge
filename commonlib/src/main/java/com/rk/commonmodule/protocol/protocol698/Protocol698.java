package com.rk.commonmodule.protocol.protocol698;

import android.util.Log;

import com.rk.commonmodule.protocol.protocol645.Protocol645Frame;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.DAR_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.DATA_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.DATA_UNIT_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.DATA_VERIFY_INFO_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.GET_RECORD_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.OAD_ARRAY_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.OAD_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.OMD_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.OMD_PARAM_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.PIID_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.RCSD_KEY;

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
        byte[] bytes = null;
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
                        bytes = new byte[byteArray.size()];
                        for (int i = 0; i < bytes.length; i++) {
                            bytes[i] = byteArray.get(i);
                        }
                        return bytes;
                    case ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NORMAL_LIST.CLASS_ID:
                        byteArray.add((byte) ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NORMAL_LIST.CLASS_ID);
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

                        if (map != null && map.containsKey(OAD_ARRAY_KEY) && map.get(OAD_ARRAY_KEY) != null) {
                            ArrayList<Protocol698Frame.OAD> oadArrayList = (ArrayList<Protocol698Frame.OAD>) map.get(OAD_ARRAY_KEY);
                            if (oadArrayList.size() > 0) {
                                byteArray.add((byte)(oadArrayList.size()));
                            } else {
                                return null;
                            }
                            for (int i = 0; i < oadArrayList.size(); i++) {
                                Protocol698Frame.OAD oad = oadArrayList.get(i);
                                if (oad != null && oad.data != null) {
                                    for (int j = 0; j < oad.data.length; j++) {
                                        byteArray.add(oad.data[j]);
                                    }
                                } else {
                                    return null;
                                }
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
                        bytes = new byte[byteArray.size()];
                        for (int i = 0; i < bytes.length; i++) {
                            bytes[i] = byteArray.get(i);
                        }
                        return bytes;
                    case ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_RECORD.CLASS_ID:
                        byteArray.add((byte) ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_RECORD.CLASS_ID);
                        //PIID
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

                        //GetRecord
                        if (map != null && map.containsKey(GET_RECORD_KEY) && map.get(GET_RECORD_KEY) != null) {
                            Protocol698Frame.GetRecord getRecord = (Protocol698Frame.GetRecord) map.get(GET_RECORD_KEY);
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

                        //TimeTag
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


                        bytes = new byte[byteArray.size()];
                        for (int i = 0; i < bytes.length; i++) {
                            bytes[i] = byteArray.get(i);
                        }
                        return bytes;
                    case ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NEXT.CLASS_ID:
                        byteArray.add((byte) ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NEXT.CLASS_ID);
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

                        if (map != null && map.containsKey("end_frame_num") && map.get("end_frame_num") != null) {
                            int frameEndNum = (int) map.get("end_frame_num");
                            byteArray.add((byte)((frameEndNum >> 8) & 0xFF));
                            byteArray.add((byte)((frameEndNum) & 0xFF));
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
                        bytes = new byte[byteArray.size()];
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
                byteArray.add((byte) ProtocolConstant.CLIENT_APDU.SET_REQUEST.CLASS_ID);
                switch (second_id) {
                    case ProtocolConstant.CLIENT_APDU.SET_REQUEST.SET_REQUEST_NORMAL.CLASS_ID:
                        byteArray.add((byte) ProtocolConstant.CLIENT_APDU.SET_REQUEST.SET_REQUEST_NORMAL.CLASS_ID);
                        //PIID
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
                        //OAD
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
                        //Data
                        if (map != null && map.containsKey(DATA_KEY) && map.get(DATA_KEY) != null) {
                            Protocol698Frame.Data data = (Protocol698Frame.Data) map.get(DATA_KEY);
                            if (data != null && data.data != null) {
                                for (int i = 0; i < data.data.length; i++) {
                                    byteArray.add(data.data[i]);
                                }
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                        //TimeTag
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
                        bytes = new byte[byteArray.size()];
                        for (int i = 0; i < bytes.length; i++) {
                            bytes[i] = byteArray.get(i);
                        }
                        return bytes;
                }
                break;
            case ProtocolConstant.CLIENT_APDU.ACTION_REQUEST.CLASS_ID:
                byteArray.add((byte) ProtocolConstant.CLIENT_APDU.ACTION_REQUEST.CLASS_ID);
                switch (second_id) {
                    case ProtocolConstant.CLIENT_APDU.ACTION_REQUEST.ACTION_REQUEST_NORMAL.CLASS_ID:
                        byteArray.add((byte) ProtocolConstant.CLIENT_APDU.ACTION_REQUEST.ACTION_REQUEST_NORMAL.CLASS_ID);

                        //PIID
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
                        //OMD
                        if (map != null && map.containsKey(OMD_KEY) && map.get(OMD_KEY) != null) {
                            Protocol698Frame.OMD omd = (Protocol698Frame.OMD) map.get(OMD_KEY);
                            if (omd != null && omd.data != null) {
                                for (int i = 0; i < omd.data.length; i++) {
                                    byteArray.add(omd.data[i]);
                                }
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                        //Data
                        if (map != null && map.containsKey(OMD_PARAM_KEY) && map.get(OMD_PARAM_KEY) != null) {
                            Protocol698Frame.Data data = (Protocol698Frame.Data) map.get(OMD_PARAM_KEY);
                            if (data != null && data.data != null) {
                                for (int i = 0; i < data.data.length; i++) {
                                    byteArray.add(data.data[i]);
                                }
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                        //TimeTag
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
                        bytes = new byte[byteArray.size()];
                        for (int i = 0; i < bytes.length; i++) {
                            bytes[i] = byteArray.get(i);
                        }
                        return bytes;
                }
                break;
            case ProtocolConstant.SECURITY_APDU.SECURITY_REQUEST.CLASS_ID:
                byteArray.add((byte) ProtocolConstant.SECURITY_APDU.SECURITY_REQUEST.CLASS_ID);
                //TODO:
                if (map != null && map.containsKey(DATA_UNIT_KEY) && map.get(DATA_UNIT_KEY) != null
                        && map.containsKey(DATA_VERIFY_INFO_KEY) && map.get(DATA_VERIFY_INFO_KEY) != null) {
                    Protocol698Frame.DataUnit dataUnit = (Protocol698Frame.DataUnit) map.get(DATA_UNIT_KEY);
                    if (dataUnit != null && dataUnit.data != null && dataUnit.data.length > 0) {
                        for (int i = 0; i < dataUnit.data.length; i++) {
                            byteArray.add(dataUnit.data[i]);
                        }
                    } else {
                        Log.i(TAG, "SECURITY_REQUEST 1");
                        return null;
                    }

                    Protocol698Frame.DataVerifyInfo dataVerifyInfo = (Protocol698Frame.DataVerifyInfo) map.get(DATA_VERIFY_INFO_KEY);
                    if (dataVerifyInfo != null && dataVerifyInfo.data != null && dataVerifyInfo.data.length > 0) {
                        for (int i = 0; i < dataVerifyInfo.data.length; i++) {
                            byteArray.add(dataVerifyInfo.data[i]);
                        }
                    } else {
                        Log.i(TAG, "SECURITY_REQUEST 2");
                        return null;
                    }

                } else {
                    Log.i(TAG, "SECURITY_REQUEST 3");
                    return null;
                }

                bytes = new byte[byteArray.size()];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = byteArray.get(i);
                }
                return bytes;

            case ProtocolConstant.CLIENT_APDU.PROXY_REQUEST.CLASS_ID:
                byteArray.add((byte) ProtocolConstant.CLIENT_APDU.PROXY_REQUEST.CLASS_ID);
                switch (second_id) {
                    case ProtocolConstant.CLIENT_APDU.PROXY_REQUEST.PROXY_TRANS_COMMAND_REQUEST.CLASS_ID:
                        byteArray.add((byte) ProtocolConstant.CLIENT_APDU.PROXY_REQUEST.PROXY_TRANS_COMMAND_REQUEST.CLASS_ID);

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
                        //OAD
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
                        //COMDCB
                        if (map != null && map.containsKey("comdcb_key") && map.get("comdcb_key") != null) {
                            Protocol698Frame.COMDCB comdcb = (Protocol698Frame.COMDCB) map.get("comdcb_key");
                            if (comdcb != null && comdcb.data != null) {
                                for (int i = 0; i < comdcb.data.length; i++) {
                                    byteArray.add(comdcb.data[i]);
                                }
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }

                        //timeout
                        if (map != null && map.containsKey("wait_timeout_key") && map.get("wait_timeout_key") != null) {
                            int waitTimeout = (int) map.get("wait_timeout_key");
                            byte h = (byte) ((waitTimeout >> 8) & 0xFF);
                            byte l = (byte)(waitTimeout & 0xFF);
                            byteArray.add(h);
                            byteArray.add(l);
                        } else {
                            return null;
                        }
                        //timeout
                        if (map != null && map.containsKey("wait_byte_timeout_key") && map.get("wait_byte_timeout_key") != null) {
                            int waitTimeout = (int) map.get("wait_byte_timeout_key");
                            byte h = (byte) ((waitTimeout >> 8) & 0xFF);
                            byte l = (byte)(waitTimeout & 0xFF);
                            byteArray.add(h);
                            byteArray.add(l);
                        } else {
                            return null;
                        }

                        //cmd
                        if (map != null && map.containsKey("trans_cmd") && map.get("trans_cmd") != null) {
                            String cmd = (String) map.get("trans_cmd");
                            byte[] cmd_bytes = DataConvertUtils.convertHexStringToByteArray(cmd, cmd.length(), false);
                            if (cmd_bytes == null) {
                                return null;
                            }
                            for (int i = 0; i < cmd_bytes.length; i++) {
                                byteArray.add(cmd_bytes[i]);
                            }
                        } else {
                            return null;
                        }

                        //TimeTag
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

                        break;
                }

                bytes = new byte[byteArray.size()];
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
                        map.put(ProtocolConstant.GET_RESPONSE_NORMAL_KEY, new Protocol698Frame.GetResponseNormal(apduFrame, 2));
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
                                map.put(DAR_KEY, apduFrame[8]);
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
                                                    length = length * 256 + (apduFrame[10 + i] & 0xFF);
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
                                                    Protocol698Frame.Data data = new Protocol698Frame.Data(apduFrame, 8);
                                                    map.put(DATA_KEY, data);
                                                    Log.i(TAG, "default parse oad：" + oadString);
                                                    break;

                                            }
                                        } else {
                                            int lengthSize = apduFrame[9] & 0x7F;
                                            if (9 + lengthSize <= apduFrame.length - 1) {
                                                int length = 0;
                                                for (int i = 0; i < lengthSize; i++) {
                                                    length = length * 256 + (apduFrame[10 + i] & 0xFF);
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
                                    case 2: //structure
                                        Protocol698Frame.Data data = new Protocol698Frame.Data(apduFrame, 8);
                                        map.put("data", data);
                                        return map;
                                    default:
                                        Protocol698Frame.Data data1 = new Protocol698Frame.Data(apduFrame, 8);
                                        map.put("data", data1);
                                        return map;

                                }
                            }

                        } else {
                            Log.i(TAG, "TST 877");
                            return null;
                        }
                        break;
                    case ProtocolConstant.SERVER_APDU.GET_RESPONSE.GET_RESPONSE_NORMAL_LIST.CLASS_ID:
                        if (apduFrame.length < 3) {
                            return null;
                        }
                        map.put(ProtocolConstant.PIID_ACD_KEY, new Protocol698Frame.PIID_ACD(apduFrame[2]));
                        if (3 > apduFrame.length - 1) {
                            return null;
                        }

                        int length = apduFrame[3];
                        Log.i(TAG, "parseApdu, GET_RESPONSE_NORMAL_LIST, length: " + length);
                        ArrayList<Protocol698Frame.A_ResultNormal> a_resultNormalList = new ArrayList<>();
                        int i1 = 0;
                        int beginPos1 = 4;
                        for (i1 = 0; i1 < length; i1++) {
                            Protocol698Frame.A_ResultNormal a_resultNormal = new Protocol698Frame.A_ResultNormal(apduFrame, beginPos1);
                            if (a_resultNormal.data == null) {
                                break;
                            }
                            a_resultNormalList.add(a_resultNormal);
                            beginPos1 = beginPos1 + a_resultNormal.data.length;
                        }

                        if (i1 < length) {
                            a_resultNormalList.clear();
                            Log.i(TAG, "parse error 1");
                            return null;
                        }
                        map.put("value", a_resultNormalList);
                        return map;
                    case ProtocolConstant.SERVER_APDU.GET_RESPONSE.GET_RESPONSE_RECORD.CLASS_ID:
                        if (apduFrame.length < 3) {
                            return null;
                        }
                        map.put(ProtocolConstant.PIID_ACD_KEY, new Protocol698Frame.PIID_ACD(apduFrame[2]));
                        if (6 <= apduFrame.length - 1) {
                            Protocol698Frame.OAD oad = new Protocol698Frame.OAD(DataConvertUtils.getSubByteArray(apduFrame, 3, 6));
                            map.put(OAD_KEY, oad);

                            Protocol698Frame.RCSD rcsd = new Protocol698Frame.RCSD(apduFrame, 7);
                            map.put(RCSD_KEY, rcsd);
                            if (rcsd.data == null) {
                                return null;
                            }
                            int columeSize = rcsd.csdArrayList.size();
                            if (6 + rcsd.data.length + 1 > apduFrame.length - 1) {
                                return null;
                            }

                            Log.i(TAG, "parsApdu， GET_RESPONSE_RECORD， choice：" + apduFrame[6 + rcsd.data.length + 1] + ", colume size: " + columeSize);

                            if (apduFrame[6 + rcsd.data.length + 1] == 0) {
                                if (6 + rcsd.data.length + 1 + 1 > apduFrame.length - 1) {
                                    return null;
                                }
                                map.put(DAR_KEY, apduFrame[6 + rcsd.data.length + 1 + 1]);
                                return map;

                            } else if (apduFrame[6 + rcsd.data.length + 1] == 1) {
                                if (6 + rcsd.data.length + 1 + 1 <= apduFrame.length - 1) {
                                    int size = apduFrame[6 + rcsd.data.length + 1 + 1];
                                    Log.i(TAG, "parsApdu， GET_RESPONSE_RECORD， a_recordRow list size：" + size);
                                    ArrayList<Protocol698Frame.A_RecordRow> a_recordRowArrayList = new ArrayList<>();
                                    int i = 0;
                                    int beginPos = 6 + rcsd.data.length + 1 + 1 + 1;
                                    Log.i(TAG, "parseApdu, GET_RESPONSE_RECORD, beginPos: " + beginPos);
                                    for (i = 0; i < size; i++) {
                                        Protocol698Frame.A_RecordRow a_recordRow = new Protocol698Frame.A_RecordRow(apduFrame, beginPos, columeSize);
                                        if (a_recordRow.data == null) {
                                            Log.i(TAG, "parsApdu， GET_RESPONSE_RECORD, a_recordRow.data is null, i: " + i);
                                            break;
                                        }
                                        a_recordRowArrayList.add(a_recordRow);
                                        beginPos = beginPos + a_recordRow.data.length;
                                    }
                                    if (i < size) {
                                        a_recordRowArrayList.clear();
                                    }
                                    if (a_recordRowArrayList.size() != 0) {
                                        Log.i(TAG, "parsApdu， GET_RESPONSE_RECORD， a_recordRow：" + a_recordRowArrayList.size());
                                    } else {
                                        Log.i(TAG, "parsApdu， GET_RESPONSE_RECORD， a_recordRow list is null");
                                    }
                                    map.put(ProtocolConstant.A_RECORD_ROW_LIST_KEY, a_recordRowArrayList);
                                    return map;
                                }

                            } else {
                                return null;
                            }

                        } else {
                            return null;
                        }
                        break;
                    case ProtocolConstant.SERVER_APDU.GET_RESPONSE.GET_RESPONSE_NEXT.CLASS_ID:
                        Log.i(TAG, "GET_RESPONSE_NEXT");
                        if (apduFrame.length < 7) {
                            Log.i(TAG, "GET_RESPONSE_NEXT， length null");
                            return null;
                        }
                        int pos = 2;
                        map.put(ProtocolConstant.PIID_ACD_KEY, new Protocol698Frame.PIID_ACD(apduFrame[pos]));
                        pos++;
                        boolean isEndFrame = false;
                        if (apduFrame[pos] == 0x01) {
                            isEndFrame = true;
                        }
                        map.put("end_frame_flag", isEndFrame);
                        pos++;
                        int frameNum = apduFrame[pos] * 256 + apduFrame[pos+1];
                        map.put("frame_num", frameNum);
                        pos = pos + 2;
                        byte choice = apduFrame[pos];
                        map.put("choice", choice);
                        pos++;
                        if (choice == 0x00) {
                            if (pos < apduFrame.length) {
                                map.put(DAR_KEY, apduFrame[pos]);
                            } else {
                                Log.i(TAG, "GET_RESPONSE_NEXT, pos index out");
                            }
                        } else if (choice == 0x01) {
                            int cnt = apduFrame[pos];
                            pos++;
                            ArrayList<Protocol698Frame.A_ResultNormal> resultNormals = new ArrayList<>();
                            for (int i = 0; i < cnt; i++) {
                                Protocol698Frame.A_ResultNormal resultNormal = new Protocol698Frame.A_ResultNormal(apduFrame, pos);
                                if (resultNormal.data != null && resultNormal.data.length > 0) {
                                    resultNormals.add(resultNormal);
                                    pos = pos + resultNormal.data.length;
                                } else {
                                    break;
                                }
                            }
                            map.put("a_result_normal_list", resultNormals);
                            return map;
                        } else if (choice == 0x02) {
                            Log.i(TAG, "parseApud, A-ResultRecord no implement");
                        }
                        break;
                    default:
                        Log.i(TAG, "parseApud, no action 1: " + (((int) apduFrame[1]) & 0xFF));

                        break;
                }
                break;
            case ProtocolConstant.SECURITY_APDU.SECURITY_RESPONSE.CLASS_ID:
                if (1 > apduFrame.length - 1) {
                    return null;
                }
                if (apduFrame[1] == 0) {
                    byte[] value = parse_OctetString(apduFrame, 2);
                    map.put("value", value);
                    map.put("security", Protocol698Frame.DataUnit_Type.CLEAR_TEXT);
                } else if (apduFrame[1] == 1) {
                    byte[] value = parse_OctetString(apduFrame, 2);
                    map.put("value", value);
                    map.put("security", Protocol698Frame.DataUnit_Type.CIPHER_TEXT);
                } else if (apduFrame[1] == 2) {
                    if (2 <= apduFrame.length - 1) {
                        map.put("dar", apduFrame[2]);
                    }
                }
                return map;
            case ProtocolConstant.SERVER_APDU.SET_RESPONSE.CLASS_ID:
                if (1 > apduFrame.length - 1) {
                    return null;
                }
                switch (((int) apduFrame[1]) & 0xFF) {
                    case ProtocolConstant.SERVER_APDU.SET_RESPONSE.SET_RESPONSE_NORMAL.CLASS_ID:
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
                        map.put(DAR_KEY, apduFrame[7]);
                        return map;
                    default:
                        break;
                }
                break;
            case ProtocolConstant.SERVER_APDU.ACTION_RESPONSE.CLASS_ID:
                if (1 > apduFrame.length - 1) {
                    return null;
                }
                switch (((int) apduFrame[1]) & 0xFF) {
                    case ProtocolConstant.SERVER_APDU.ACTION_RESPONSE.ACTION_RESPONSE_NORMAL.CLASS_ID:
                        if (apduFrame.length < 3) {
                            return null;
                        }
                        map.put(ProtocolConstant.PIID_ACD_KEY, new Protocol698Frame.PIID_ACD(apduFrame[2]));
                        if (apduFrame.length < 7) {
                            return null;
                        }
                        map.put(OMD_KEY, new Protocol698Frame.OMD(DataConvertUtils.getSubByteArray(apduFrame, 3, 6)));
                        if (apduFrame.length < 8) {
                            return null;
                        }
                        map.put(DAR_KEY, apduFrame[7]);
                        if (apduFrame.length > 8) {
                            Protocol698Frame.Data data = new Protocol698Frame.Data(apduFrame, 8);
                            map.put(DATA_KEY, data);
                        }
                        return map;
                    default:
                        break;
                }
                break;
            case ProtocolConstant.SERVER_APDU.PROXY_RESPONSE.CLASS_ID:
                try {
                    switch (((int) apduFrame[1]) & 0xFF) {
                        case ProtocolConstant.SERVER_APDU.PROXY_RESPONSE.PROXY_TRANS_COMMAND_RESPONSE.CLASS_ID:
                            map.put(ProtocolConstant.PIID_ACD_KEY, new Protocol698Frame.PIID_ACD(apduFrame[2]));

                            map.put(OAD_KEY, new Protocol698Frame.OAD(DataConvertUtils.getSubByteArray(apduFrame, 3, 6)));
                            byte choice = apduFrame[7];
                            if (choice == 0) {
                                map.put(DAR_KEY, apduFrame[8]);
                            } else if (choice == 1) {
                                Protocol698Frame.OctString octString = new Protocol698Frame.OctString(apduFrame, 8);

                                map.put("ret_cmd", octString.octString);

                            }
                            return map;
                        default:
                            break;
                    }

                } catch (Exception err) {
                    Log.e(TAG, "PROXY_RESPONSE, parse err:" + err.getMessage());
                    return null;
                }
                break;
            default:
                Log.i(TAG, "parseApud, no action: " + (((int) apduFrame[0]) & 0xFF));
                break;

        }

        return null;
    }

    private byte[] parse_OctetString(byte[] frame, int begin) {
        if (frame == null || frame.length <= 0 || begin > frame.length - 1) {
            return null;
        }
        if ((frame[begin] & 0x80) == 0x00) {
            if ((begin + frame[begin]) <= (frame.length - 1)) {
                return DataConvertUtils.getSubByteArray(frame, begin + 1, begin + frame[begin]);
            } else {
                return null;
            }
        } else {
            int lengthSize = frame[begin] & 0x7F;
            if (begin + lengthSize <= frame.length - 1) {
                int length = 0;
                for (int i = 0; i < lengthSize; i++) {
                    length = length * 256 + (frame[begin + 1 + i] & 0xFF);
                }
                if (begin + lengthSize + length <= frame.length - 1) {
                    return DataConvertUtils.getSubByteArray(frame, begin + lengthSize + 1, begin + lengthSize + length);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

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

    public static String parseData(Protocol698Frame.Data data) {
        String value = null;
        if (data != null && data.data != null) {
            Log.i(TAG, "parseData, data type: " + data.type);
            switch (data.type) {
                case DOUBLE_LONG_TYPE:
                    Log.i(TAG, "parseData, data: " + (int)data.obj);
                    value = String.valueOf((int)data.obj);
                    break;
                case ARRAY_TYPE:
                    ArrayList<Protocol698Frame.Data> dataArrayList = (ArrayList<Protocol698Frame.Data>) data.obj;
                    if (dataArrayList != null && dataArrayList.size() > 0) {
                        Log.i(TAG, "parseData, array size: " + dataArrayList.size());
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < dataArrayList.size(); i++) {
                            sb.append(parseData(dataArrayList.get(i)));
                            if (i < dataArrayList.size() - 1) {
                                sb.append("|");
                            }
                        }
                        value = sb.toString();
                    }
                    break;
                case LONG_UNSIGNED_TYPE:
                    Log.i(TAG, "parseData, data: " + (int)data.obj);
                    value = String.valueOf((int)data.obj);
                    break;
            }

        }
        Log.i(TAG, "parseData, value: " + value);
        return value;
    }

}
