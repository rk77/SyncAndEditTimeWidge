package com.rk.commonmodule.protocol.protocol645;

import android.text.TextUtils;
import android.util.Log;

import com.rk.commonmodule.protocol.protocol645.y2007.Protocol645Constant;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Protocol645FrameBaseMaker {
    private static final String TAG = Protocol645FrameBaseMaker.class.getSimpleName();

    private Protocol645FrameBaseMaker() {
    }

    private static class InstanceHolder {
        private static final Protocol645FrameBaseMaker INSTANCE = new Protocol645FrameBaseMaker();
    }

    public static Protocol645FrameBaseMaker getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public byte[] makeFrame(String address, byte ctrlCode, String data) {

        if (TextUtils.isEmpty(address)) {
            return null;
        }

        ArrayList<Byte> frameArray = new ArrayList<>();

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

        if (address.length() % 2 != 0) {
            return null;
        }

        frameArray.add(Protocol645Constant.FRAME_BEGIN);
        frameArray.addAll(DataConvertUtils.convertHexStringToByteArrayList(address, address.length(), true));
        frameArray.add(Protocol645Constant.FRAME_BEGIN);
        frameArray.add(ctrlCode);
        switch (ctrlCode) {
            case (byte) 0x11: //07规约，读数据
                if (data == null || data.length() < 4 || data.length() % 2 != 0) {
                    return null;
                }

                // two chars stand for one byte.
                byte dataLength = (byte) (data.length() / 2);
                frameArray.add(dataLength);
                byte[] dataByteArray = DataConvertUtils.convertHexStringToByteArray(data, data.length(), false);
                if (dataByteArray == null || dataByteArray.length <= 0) {
                    return null;
                }
                for (int i = 0; i < dataLength; i++) {
                    frameArray.add((byte) (dataByteArray[i] + 0x33));
                }
                if (frameArray == null || frameArray.size() <= 0) {
                    return null;
                }
                byte cs1 = calculateCs(frameArray);
                frameArray.add(cs1);
                frameArray.add(Protocol645Constant.FRAME_END);
                break;
            default:
                byte length = 0x00;
                if (TextUtils.isEmpty(data)) {
                    frameArray.add(length);
                } else if (data.length() %2 != 0) {
                    return null;
                } else {
                    length = (byte)(data.length() / 2);
                    frameArray.add(length);
                    byte[] dataArray = DataConvertUtils.convertHexStringToByteArray(data, data.length(), false);
                    if (dataArray == null || dataArray.length <= 0) {
                        return null;
                    }
                    if (length != (byte) (dataArray.length)) {
                        return null;
                    }
                    for (int i = 0; i < dataArray.length; i++) {
                        frameArray.add((byte) (dataArray[i] + 0x33));
                    }
                    if (frameArray == null || frameArray.size() <= 0) {
                        return null;
                    }
                }
                byte cs = calculateCs(frameArray);
                frameArray.add(cs);
                frameArray.add(Protocol645Constant.FRAME_END);
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
