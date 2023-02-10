package com.rk.commonmodule.protocol.protocol101;

import com.rk.commonlib.util.LogUtils;

import java.lang.reflect.Array;

public class Protocol101 {

    public final class FixFrame {
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

    public final class B_Fix_Ctrl_Area {
        public int dir = 0;
        public int prm = 0;
        public int fcb_res = 0;
        public int fcv_dfc = 0;
        public int fc = 0;
        public byte data;

        public B_Fix_Ctrl_Area(int dir, int prm, int fcb_res, int fcv_dfc, int fc) {
            this.data = 0x00;
            if (dir == 1) {
                this.data = (byte)(this.data | 0x80);
            }
            if (prm == 1) {
                this.data = (byte)(this.data | 0x40);
            }
            if (fcb_res == 1) {
                this.data = (byte)(this.data | 0x20);
            }
            if (fcv_dfc == 1) {
                this.data = (byte)(this.data | 0x10);
            }
            this.data = (byte) (this.data | (fc & 0x0F));
            this.dir = dir;
            this.prm = prm;
            this.fcb_res = fcb_res;
            this.fcv_dfc = fcv_dfc;

            this.fc = fc;
        }

        public B_Fix_Ctrl_Area(byte data) {
            if ((data & 0x80) != 0x00) {
                this.dir = 1;
            }
            if ((data & 0x40) != 0x00) {
                this.prm = 1;
            }
            if ((data & 0x20) != 0x00) {
                this.fcb_res = 1;
            }
            if ((data & 0x10) != 0x00) {
                this.fcv_dfc = 1;
            }
            this.fc = (data & 0x0F);
            this.data = data;
        }

    }

    public final class VarLenFrame {
        public byte c;
        public byte[] a;
        public byte[] data;
        public IAsdu asdu;
        public VarLenFrame(byte ctrl, byte[] addr, IAsdu asdu) {
            if (addr == null || addr.length != 2 || asdu == null || asdu.getData() != null) {
                return;
            }
            c = ctrl;
            a = addr;
            this.asdu = asdu;
            byte cs = (byte)(c + a[0] + a[1]);
            for (int i = 0; i < asdu.getData().length; i++) {
                cs = (byte) (cs + asdu.getData()[i]);
            }
            byte l = (byte) (3 + asdu.getData().length);
            data = new byte[9 + asdu.getData().length];
            data[0] = 0x68;
            data[1] = l;
            data[2] = l;
            data[3] = 0x68;
            data[4] = c;
            data[5] = a[0];
            data[6] = a[1];
            int pos = 7;
            System.arraycopy(asdu.getData(), 0, data, pos, asdu.getData().length);
            pos = pos + asdu.getData().length;
            data[pos] = cs;
            data[pos + 1] = 0x16;
        }

        public VarLenFrame(byte[] frame, int begin) {
            try {
                if (frame[begin] != 0x68) {
                    return;
                }
                if (frame[begin + 1] != frame[begin + 2]){
                    return;
                }
                if (frame[begin + 3] != 0x68) {
                    return;
                }

                int c_p = begin + 4;
                int a_p = c_p + 1;
                int asdu_p = a_p + 2;
                int asdu_len = frame[begin + 1] - 3;
                int cs_p = asdu_p + asdu_len;
                if (frame[cs_p + 1] != 0x16) {
                    return;
                }

                byte cs = (byte) (frame[c_p] + frame[a_p] + frame[a_p + 1]);
                for (int i = 0; i < asdu_len; i++) {
                    cs = (byte) (cs + frame[asdu_p + i]);
                }
                if (cs != frame[cs_p]) {
                    return ;
                }

                this.c = frame[c_p];
                this.a = new byte[2];
                this.a[0] = frame[a_p];
                this.a[1] = frame[a_p + 1];
                this.asdu = parseAsdu(frame, asdu_p, asdu_len);

                this.data = new byte[9 + asdu_len];
                System.arraycopy(frame, begin, this.data, 0, this.data.length);

            } catch (Exception e) {
                LogUtils.e("parse err:" + e.getMessage());
            }
        }

        private IAsdu parseAsdu(byte[] frame, int begin, int len) {
            return null;
        }
    }
}

