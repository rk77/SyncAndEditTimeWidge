package com.rk.commonmodule.protocol.protocol101;

import com.rk.commonlib.bluetooth.IFrameVerify;

public class P101FixFrameVerifyObj implements IFrameVerify {
    @Override
    public boolean verify(byte[] frame, int begin) {
        if (frame == null || frame.length <= 0) {
            return false;
        }
        boolean has10 = false;
        for (int i = 0; i < frame.length; i++) {
            if (frame[i] == 0x10) {
                has10 = true;
                begin = i;
            }
        }
        if (!has10) {
            return false;
        }
        try {
            if (frame.length - 1 - begin + 1 < 6) {
                return false;
            }
            int c_p = begin + 1;
            int a_p = c_p + 1;
            int cs_p = a_p + 2;
            int end_p = cs_p + 1;
            if (frame[end_p] != 0x16) {
                return false;
            }
            byte cs = (byte) (frame[c_p] + frame[a_p] + frame[a_p + 1]);
            if (cs != frame[cs_p]) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
