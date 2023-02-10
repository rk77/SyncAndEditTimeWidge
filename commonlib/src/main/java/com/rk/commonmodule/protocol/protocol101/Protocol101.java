package com.rk.commonmodule.protocol.protocol101;

import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.protocol.protocol3761.Protocol3761Frame;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Protocol101 {

    public static final class FixFrame {
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

        public FixFrame(byte[] frame) {


            try {
                int begin = 0;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] == 0x10) {
                        begin = i;
                        break;
                    }
                }
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

    public static final class B_Fix_Ctrl_Area {
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

    public static final class VarLenFrame {
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

        public VarLenFrame(byte[] frame) {
            try {
                int begin = 0;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] == 0x68) {
                        begin = i;
                        break;
                    }
                }
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
            return new ASDU(frame, begin);
        }
    }

    public static final class DataUnitLable {
        public byte TI;
        public byte VSQ;
        public byte[] COT;
        public int cot;
        public byte[] CO_ADDR;
        public int commonAddr;
        public byte[] data;
        public DataUnitLable(int ti, byte vsq, int cot, int coAddr) {
            this.TI = (byte) ti;
            this.VSQ = vsq;
            this.COT = new byte[2];
            this.COT[0] = (byte)(cot & 0x00FF);
            this.COT[1] = (byte)((cot >> 8) & 0x00FF);
            this.cot = cot;
            this.CO_ADDR = new byte[2];
            this.CO_ADDR[0] = (byte)(coAddr & 0xFF);
            this.CO_ADDR[1] = (byte)((coAddr >> 8) & 0xFF);
            this.commonAddr = coAddr;

            this.data = new byte[6];
            this.data[0] = TI;
            this.data[1] = VSQ;
            this.data[2] = COT[0];
            this.data[3] = COT[1];
            this.data[4] = CO_ADDR[0];
            this.data[5] = CO_ADDR[1];

        }

        public DataUnitLable(byte[] frame, int begin) {
            try {
                TI = frame[begin];
                VSQ = frame[begin + 1];
                COT = new byte[2];
                COT[0] = frame[begin + 2];
                COT[1] = frame[begin + 3];
                cot = COT[1] * 256 + COT[0];
                CO_ADDR = new byte[2];
                CO_ADDR[0] = frame[begin + 4];
                CO_ADDR[1] = frame[begin + 5];
                commonAddr = CO_ADDR[1] * 256 + CO_ADDR[0];
                data = new byte[6];
                System.arraycopy(frame, begin, data, 0, 6);
            } catch (Exception e) {
                LogUtils.e("parse err");
            }
        }
    }

    public static final class InfoObj{
        public byte[] data;
        public InfoObj() {

        }
        public InfoObj(byte[] frame, int begin) {

        }
    }

    public static final class InfoObjList {
        public byte[] data;
        public ArrayList<InfoObj> infoObjs = new ArrayList<>();

        public InfoObjList() {

        }

        public InfoObjList(byte[] frame, int begin) {

        }

    }

    public static final class ASDU implements IAsdu {

        public DataUnitLable dataUnitLable;
        public InfoObjList infoObjList;
        public byte[] data;

        public ASDU(DataUnitLable lable, InfoObjList objList) {
            this.dataUnitLable = lable;
            this.infoObjList = objList;
            int len = 0;
            int pos = 0;
            if (lable != null && lable.data != null) {
                len = len + lable.data.length;
            }
            if (objList != null && objList.data != null) {
                len = len + objList.data.length;
            }

            this.data = new byte[len];
            if (lable != null && lable.data != null) {
                System.arraycopy(lable.data, 0, this.data, pos, lable.data.length);
                pos = pos + lable.data.length;
            }
            if (objList != null && objList.data != null) {
                len = len + objList.data.length;
                System.arraycopy(objList.data, 0, this.data, pos, objList.data.length);
            }

        }

        public ASDU(byte[] frame, int begin) {
            try {
                this.dataUnitLable = new DataUnitLable(frame, begin);
                this.infoObjList = new InfoObjList(frame, begin + 6);
            } catch (Exception e) {

            }

        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public void parseData(byte[] data, int begin) {

        }
    }
}

