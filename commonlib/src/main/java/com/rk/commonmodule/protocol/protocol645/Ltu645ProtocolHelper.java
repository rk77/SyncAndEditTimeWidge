package com.rk.commonmodule.protocol.protocol645;

import android.text.TextUtils;

import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.utils.DataConvertUtils;

public class Ltu645ProtocolHelper {
    public static byte[] makeGetVersionInfoFrame(String address) {
        byte ctrlCode = 0x1E;
        String data = "06";
        return Protocol645FrameBaseMaker.getInstance().makeFrame(address, ctrlCode, data);
    }

    public static String parseDetail(byte[] frame) {
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        switch (protocol645Frame.mCtrlCode) {
            case (byte) 0x9E:
                //LogUtils.i("info: " + DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData));
                if (6 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 3, 6, false)).append("|");
                }
                if (13 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 10, 13, false)).append("|");
                }
                if (19 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 14, 19, false)).append("|");
                }
                if (26 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 23, 26, false)).append("|");
                }
                if (32 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 27, 32, false)).append("|");
                }
                if (44 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 36, 44, false));
                }
                break;
        }
        return sb.toString();
    }

    public static byte[] makeGetAddrFrame() {
        String addr = "AAAAAAAAAAAA";
        byte ctrlCode = 0x13;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, null);
    }
    public static String  parseGetAddrFrame(byte[] frame) {
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame == null) {
            return null;
        }
        return DataConvertUtils.convertByteArrayToString(protocol645Frame.mAddressArea, true);
    }

    public static byte[] makeSetAddrFrame(String oldAddr, String address) {
        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(oldAddr)) {
            return null;
        }
        byte ctrlCode = 0x1E;
        String data = "82" + address;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(oldAddr, ctrlCode, data);
    }

    public static String parseSetAddrFrame(byte[] frame) {
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame == null) {
            return "false";
        }

        if (protocol645Frame.mCtrlCode == 0x9E) {
            return "true";
        } else {
            return "false|" + DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, false);
        }
    }

    public static byte[] makeResetFrame(String addr, String dataLable) {
        if (TextUtils.isEmpty(addr) || TextUtils.isEmpty(dataLable)) {
            return null;
        }
        byte ctrlCode = 0x1E;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }
}
