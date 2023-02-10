package com.rk.commonmodule.protocol.protocol101;

import com.rk.commonlib.bluetooth.IFrameVerify;;

public class P101FrameVerifyObj implements IFrameVerify {
    @Override
    public boolean verify(byte[] frame, int begin) {
        if (frame == null || frame.length <= 0) {
            return false;
        }
        boolean has68 = false;
        for (int i = 0; i < frame.length; i++) {
            if (frame[i] == 0x68) {
                has68 = true;
                begin = i;
            }
        }
        if (!has68) {
            return false;
        }
        try {
            if (frame[begin] != 0x68) {
                return false;
            }
            if (frame[begin + 1] != frame[begin + 2]){
                return false;
            }
            if (frame[begin + 3] != 0x68) {
                return false;
            }

            int c_p = begin + 4;
            int a_p = c_p + 1;
            int asdu_p = a_p + 2;
            int asdu_len = frame[begin + 1] - 3;
            int cs_p = asdu_p + asdu_len;
            if (frame[cs_p + 1] != 0x16) {
                return false;
            }

            byte cs = (byte) (frame[c_p] + frame[a_p] + frame[a_p + 1]);
            for (int i = 0; i < asdu_len; i++) {
                cs = (byte) (cs + frame[asdu_p + i]);
            }
            if (cs != frame[cs_p]) {
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }

    }
}
