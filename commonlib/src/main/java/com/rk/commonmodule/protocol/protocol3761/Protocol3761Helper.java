package com.rk.commonmodule.protocol.protocol3761;

import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.utils.DataConvertUtils;

public class Protocol3761Helper {
    public static byte[] makeGetTerminalVersionInfoFrame() {
        Protocol3761Frame.AddrArea addrArea = new Protocol3761Frame.AddrArea("FFFF",0xFFFF, 0x14);
        Protocol3761Frame.DA da = new Protocol3761Frame.DA(0);
        Protocol3761Frame.DT dt = new Protocol3761Frame.DT(1);
        Protocol3761Frame.DataUnitID dataUnitID = new Protocol3761Frame.DataUnitID(da, dt);
        byte[] userLinkData = Protocol3761.PROTOCOL_3761.makeLinkUserData((byte) 0x09, (byte) 0x60, dataUnitID, null);
        if (userLinkData == null || userLinkData.length <= 0) {
            LogUtils.i("userLinkData is null");
            return null;
        }
        byte[] userDataArea = Protocol3761.PROTOCOL_3761.makeUserDataArea((byte) 0x4A, addrArea, userLinkData);
        if (userDataArea == null || userDataArea.length <= 0) {
            LogUtils.i("userDataArea is null");
            return null;
        }
        byte[] frame = Protocol3761.PROTOCOL_3761.makeFrame(userDataArea);
        return frame;
    }

    public static String parseVersionFrame(byte[] frame) {
        boolean isOk = Protocol3761.PROTOCOL_3761.verifyFrame(frame);
        LogUtils.i("isOk: " + isOk);
        if (!isOk) {
            return null;
        }

        byte[] data_unit = DataConvertUtils.getSubByteArray(frame, Protocol3761.PROTOCOL_3761.mSEQPos + 1, Protocol3761.PROTOCOL_3761.mCsPos - 1);
        if (data_unit == null || data_unit.length < 20) {
            return null;
        }
        byte[] version_data = DataConvertUtils.getSubByteArray(data_unit, 16, 19);

        return DataConvertUtils.getByteArray2AsciiString(version_data);
    }

    public static byte[] makeGetRemoteCommuModuleVersionInfoFrame() {
        Protocol3761Frame.AddrArea addrArea = new Protocol3761Frame.AddrArea("FFFF",0xFFFF, 0x14);
        Protocol3761Frame.DA da = new Protocol3761Frame.DA(0);
        Protocol3761Frame.DT dt = new Protocol3761Frame.DT(9);
        Protocol3761Frame.DataUnitID dataUnitID = new Protocol3761Frame.DataUnitID(da, dt);
        byte[] userLinkData = Protocol3761.PROTOCOL_3761.makeLinkUserData((byte) 0x09, (byte) 0x60, dataUnitID, null);
        if (userLinkData == null || userLinkData.length <= 0) {
            LogUtils.i("userLinkData is null");
            return null;
        }
        byte[] userDataArea = Protocol3761.PROTOCOL_3761.makeUserDataArea((byte) 0x4A, addrArea, userLinkData);
        if (userDataArea == null || userDataArea.length <= 0) {
            LogUtils.i("userDataArea is null");
            return null;
        }
        byte[] frame = Protocol3761.PROTOCOL_3761.makeFrame(userDataArea);
        return frame;
    }

    public static String[] parseRemoteModuleVersionInfoFrame(byte[] frame) {
        boolean isOk = Protocol3761.PROTOCOL_3761.verifyFrame(frame);
        LogUtils.i("isOk: " + isOk);
        if (!isOk) {
            return null;
        }

        byte[] data_unit = DataConvertUtils.getSubByteArray(frame, Protocol3761.PROTOCOL_3761.mSEQPos + 1 + 4, Protocol3761.PROTOCOL_3761.mCsPos - 1);
        if (data_unit == null || data_unit.length < 46) {
            return null;
        }
        String[] values = new String[7];
        values[0] = DataConvertUtils.getByteArray2AsciiString(
                DataConvertUtils.getSubByteArray(data_unit, 0, 3));
        values[1] = DataConvertUtils.getByteArray2AsciiString(
                DataConvertUtils.getSubByteArray(data_unit, 4, 11));
        values[2] = DataConvertUtils.getByteArray2AsciiString(
                DataConvertUtils.getSubByteArray(data_unit, 12, 15));
        values[3] = DataConvertUtils.getByteArray2AsciiString(
                DataConvertUtils.getSubByteArray(data_unit, 16, 18));
        values[4] = DataConvertUtils.getByteArray2AsciiString(
                DataConvertUtils.getSubByteArray(data_unit, 19, 22));
        values[5] = DataConvertUtils.getByteArray2AsciiString(
                DataConvertUtils.getSubByteArray(data_unit, 23, 25));
        values[6] = DataConvertUtils.getByteArray2AsciiString(
                DataConvertUtils.getSubByteArray(data_unit, 26, 45));

        return values;
    }

    public static byte[] makeGetLocalCommuModuleVersionInfoFrame() {
        Protocol3761Frame.AddrArea addrArea = new Protocol3761Frame.AddrArea("FFFF",0xFFFF, 0x14);
        Protocol3761Frame.DA da = new Protocol3761Frame.DA(0);
        Protocol3761Frame.DT dt = new Protocol3761Frame.DT(10);
        Protocol3761Frame.DataUnitID dataUnitID = new Protocol3761Frame.DataUnitID(da, dt);
        byte[] userLinkData = Protocol3761.PROTOCOL_3761.makeLinkUserData((byte) 0x09, (byte) 0x60, dataUnitID, null);
        if (userLinkData == null || userLinkData.length <= 0) {
            LogUtils.i("userLinkData is null");
            return null;
        }
        byte[] userDataArea = Protocol3761.PROTOCOL_3761.makeUserDataArea((byte) 0x4A, addrArea, userLinkData);
        if (userDataArea == null || userDataArea.length <= 0) {
            LogUtils.i("userDataArea is null");
            return null;
        }
        byte[] frame = Protocol3761.PROTOCOL_3761.makeFrame(userDataArea);
        return frame;
    }

    public static String[] parseLocalModuleVersionInfoFrame(byte[] frame) {
        boolean isOk = Protocol3761.PROTOCOL_3761.verifyFrame(frame);
        LogUtils.i("isOk: " + isOk);
        if (!isOk) {
            return null;
        }

        byte[] data_unit = DataConvertUtils.getSubByteArray(frame, Protocol3761.PROTOCOL_3761.mSEQPos + 1 + 4, Protocol3761.PROTOCOL_3761.mCsPos - 1);
        if (data_unit == null || data_unit.length < 15) {
            return null;
        }
        String[] values = new String[5];
        values[0] = DataConvertUtils.convertByteArrayToString(data_unit, 0, 5, false);
        values[1] = DataConvertUtils.getByteArray2AsciiString(
                DataConvertUtils.getSubByteArray(data_unit, 6, 7));
        values[2] = DataConvertUtils.getByteArray2AsciiString(
                DataConvertUtils.getSubByteArray(data_unit, 8, 9));
        values[3] = DataConvertUtils.convertByteArrayToString(data_unit, 12, 12, false) + "-"
                + DataConvertUtils.convertByteArrayToString(data_unit, 111, 11, false) + "-"
                + DataConvertUtils.convertByteArrayToString(data_unit, 10, 10, false);
        values[4] = DataConvertUtils.convertByteArrayToString(data_unit, 13, 14, false);

        return values;
    }
}
//