package com.rk.commonmodule.protocol.protocol101;

import com.rk.commonlib.bluetooth.IFrameVerify;
import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.utils.DataConvertUtils;;

public class P101FrameVerifyObj implements IFrameVerify {
    //68 0c 0c 68 c3 01 00 46 01 04 00 01 00 00 00 00 10 16
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
                break;
            }
        }
        if (!has68) {
            LogUtils.i("err1");
            return false;
        }
        try {
            if (frame[begin] != 0x68) {
                LogUtils.i("err2");
                return false;
            }
            LogUtils.i("len 1:" + frame[begin + 1]);
            LogUtils.i("len 2:" + frame[begin + 2]);
            if (frame[begin + 1] != frame[begin + 2]){
                LogUtils.i("err3");
                return false;
            }
            if (frame[begin + 3] != 0x68) {
                LogUtils.i("err4");
                return false;
            }

            int c_p = begin + 4;
            int a_p = c_p + 1;
            int asdu_p = a_p + 2;
            int asdu_len = frame[begin + 1] - 3;
            int cs_p = asdu_p + asdu_len;
            if (frame[cs_p + 1] != 0x16) {
                LogUtils.i("err5");
                return false;
            }

            byte cs = (byte) (frame[c_p] + frame[a_p] + frame[a_p + 1]);
            for (int i = 0; i < asdu_len; i++) {
                cs = (byte) (cs + frame[asdu_p + i]);
            }
            if (cs != frame[cs_p]) {
                LogUtils.i("err6");
                return false;
            }

            return true;

        } catch (Exception e) {
            LogUtils.i("err7： " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }
}
