package com.rk.commonmodule.protocol.protocol101;

import com.rk.commonlib.util.LogUtils;

public class Protocol101 {
    public class FixFrame {
        public byte c;
        public byte[] a;
        public byte[] data;
        public FixFrame(byte ctrl, byte[] addr) {
            if (addr == null || addr.length != 2) {
                return;
            }
            c = ctrl;
            a = addr;
            byte cs = (byte)(c + a[0] + a[1]);
            data = new byte[6];
            data[0] = 0x10;
            data[1] = c;
            data[2] = a[0];
            data[3] = a[1];
            data[4] = cs;
            data[5] = 0x16;
        }

        public FixFrame(byte[] frame, int begin) {
            try {
                if ((frame[begin] == 0x10) && (frame[begin + 5] == 0x16)) {
                    this.c = frame[begin + 1];
                    this.a = new byte[2];
                    this.a[0] = frame[begin + 2];
                    this.a[1] = frame[begin + 3];
                    this.data = new byte[6];
                    System.arraycopy(frame, begin, data, 0, 6);
                }

            } catch (Exception e) {
                LogUtils.e("parse err:" + e.getMessage());
            }
        }
    }

    public class VarLenFrame {

    }
}
