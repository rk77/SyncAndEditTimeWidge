package com.rk.commonmodule.protocol.protocol645.y1997;

import android.text.TextUtils;

import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.Map;

public enum Protocol645Of97FrameMaker {
    PROTOCOL_645_OF_97_FRAME_MAKER;
    private static final String TAG = Protocol645Of97FrameMaker.class.getSimpleName();
    public byte[] makeFrame(Map map) {
        if (map == null || map.isEmpty() || !map.containsKey(Protocol645Of97Constant.ADDRESS)
                || !map.containsKey(Protocol645Of97Constant.CTRL_CODE)) {
            return null;
        }
        if (map.get(Protocol645Of97Constant.ADDRESS) == null || map.get(Protocol645Of97Constant.CTRL_CODE) == null) {
            return null;
        }
        ArrayList<Byte> frameArray = new ArrayList<>();
        String address = (String) map.get(Protocol645Of97Constant.ADDRESS);
        if (!TextUtils.isEmpty(address)) {
            if (address.length() <= Protocol645Of97Constant.ADDRESS_LENGTH) {
                // supplement "AA" in the begin of address string, if address length < 12;
                if (address.length() % 2 != 0) {
                    address = "0" + address;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Protocol645Of97Constant.ADDRESS_LENGTH - address.length(); i++) {
                    sb.append("A");
                }
                address = sb.append(address).toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
        frameArray.add(Protocol645Of97Constant.FRAME_BEGIN);
        ArrayList<Byte> addressArray = DataConvertUtils.convertHexStringToByteArrayList(address, address.length(), true);
        if (addressArray == null || addressArray.size() <= 0) {
            return null;
        }
        frameArray.addAll(DataConvertUtils.convertHexStringToByteArrayList(address, address.length(), true));
        frameArray.add(Protocol645Of97Constant.FRAME_BEGIN);
        byte ctrlCode = (byte) map.get(Protocol645Of97Constant.CTRL_CODE);
        frameArray.add(ctrlCode);
        switch (ctrlCode) {
            case Protocol645Of97Constant.ControlCode.READ_DATA_REQUEST:
                if (!map.containsKey(Protocol645Of97Constant.DATA_IDENTIFIER)) {
                    return null;
                }
                String dataIDString = (String) map.get(Protocol645Of97Constant.DATA_IDENTIFIER);
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
                byte cs = calculateCs(frameArray);
                frameArray.add(cs);
                frameArray.add(Protocol645Of97Constant.FRAME_END);
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
