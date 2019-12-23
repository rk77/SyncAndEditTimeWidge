package com.rk.commonmodule.protocol.protocol645.y2007;

import android.text.TextUtils;

import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.Map;

public enum Protocol645FrameMaker {
    PROTOCOL_645_FRAME_MAKER;
    private static final String TAG = Protocol645FrameMaker.class.getSimpleName();
    public byte[] makeFrame(Map map) {
        if (map == null || map.isEmpty() || !map.containsKey(Protocol645Constant.ADDRESS)
                || !map.containsKey(Protocol645Constant.CTRL_CODE)) {
            return null;
        }
        if (map.get(Protocol645Constant.ADDRESS) == null || map.get(Protocol645Constant.CTRL_CODE) == null) {
            return null;
        }
        ArrayList<Byte> frameArray = new ArrayList<>();
        String address = (String) map.get(Protocol645Constant.ADDRESS);
        if (!TextUtils.isEmpty(address)) {
            if (address.length() <= Protocol645Constant.ADDRESS_LENGTH) {
                // supplement "0" in the begin of address string, if address length < 12;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Protocol645Constant.ADDRESS_LENGTH - address.length(); i++) {
                    sb.append("0");
                }
                address = sb.append(address).toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
        frameArray.add(Protocol645Constant.FRAME_BEGIN);
        ArrayList<Byte> addressArray = DataConvertUtils.convertHexStringToByteArrayList(address, address.length(), true);
        if (addressArray == null || addressArray.size() <= 0) {
            return null;
        }
        frameArray.addAll(DataConvertUtils.convertHexStringToByteArrayList(address, address.length(), true));
        frameArray.add(Protocol645Constant.FRAME_BEGIN);
        byte ctrlCode = (byte) map.get(Protocol645Constant.CTRL_CODE);
        frameArray.add(ctrlCode);
        switch (ctrlCode) {
            case Protocol645Constant.ControlCode.READ_ADDRESS_REQUEST:
                frameArray.add((byte)0x0);
                byte cs = calculateCs(frameArray);
                frameArray.add(cs);
                frameArray.add(Protocol645Constant.FRAME_END);
                break;
            case Protocol645Constant.ControlCode.READ_DATA_REQUEST:
                if (!map.containsKey(Protocol645Constant.DATA_IDENTIFIER)) {
                    return null;
                }
                String dataIDString = (String) map.get(Protocol645Constant.DATA_IDENTIFIER);
                if (dataIDString == null || dataIDString.length() <= 0) {
                    return null;
                }
                // two chars stand for one byte.
                byte dataLength = (byte) (dataIDString.length() / 2);
                frameArray.add(dataLength);
                // add data identifier to array list.
                byte[] dataID = DataConvertUtils.convertHexStringToByteArray(dataIDString, dataIDString.length(), true);
                if (dataID == null || dataID.length <= 0) {
                    return null;
                }
                for (int i = 0; i < dataLength; i++) {
                    // revert and plus 0x33
                    frameArray.add((byte) (dataID[i] + 0x33));
                }
                byte cs1 = calculateCs(frameArray);
                frameArray.add(cs1);
                frameArray.add(Protocol645Constant.FRAME_END);
                break;
            default:
                break;
        }
        byte[] frame = new byte[frameArray.size()];
        for (int i = 0; i < frame.length; i++) {
            frame[i] = frameArray.get(i);
        }
        return frame;
    }

    private byte calculateCs(ArrayList<Byte> arrayList) {
        int cs = 0;
        if (arrayList == null || arrayList.size() <=0) {
            return (byte) cs;
        }
        for (int i = 0; i< arrayList.size(); i++) {
            cs = cs + arrayList.get(i);
        }

        return (byte) cs;
    }
}
