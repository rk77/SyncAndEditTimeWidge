package com.rk.commonmodule.protocol.protocol3761;

import com.rk.commonlib.util.LogUtils;

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
}
