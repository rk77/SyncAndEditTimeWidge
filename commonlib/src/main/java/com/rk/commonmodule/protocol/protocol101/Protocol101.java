package com.rk.commonmodule.protocol.protocol101;

import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.protocol.protocol3761.Protocol3761Frame;
import com.rk.commonmodule.protocol.protocol645.Ltu645ProtocolHelper;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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
        //10 8b 01 00 8c 16
        public FixFrame(byte[] frame) {


            try {
                int begin = 0;
                for (int i = 0; i < frame.length; i++) {
                    if (frame[i] == 0x10) {
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
        public B_Fix_Ctrl_Area ctrlArea;
        public byte[] a;
        public byte[] data;
        public IAsdu asdu;
        public VarLenFrame(byte ctrl, byte[] addr, IAsdu asdu) {
            if (addr == null || addr.length != 2 || asdu == null || asdu.getData() == null) {
                LogUtils.i("make frame err:" + ((asdu == null) ? "asdu null": asdu.getData()));
                return;
            }
            c = ctrl;
            a = addr;
            ctrlArea = new B_Fix_Ctrl_Area(c);
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
                for (int i = 0; i < frame.length; i++) {
                    if (frame[i] == 0x68) {
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
                this.ctrlArea = new B_Fix_Ctrl_Area(this.c);
                this.a = new byte[2];
                this.a[0] = frame[a_p];
                this.a[1] = frame[a_p + 1];
                this.asdu = parseAsdu(frame, asdu_p, asdu_len);

                this.data = new byte[9 + asdu_len];
                System.arraycopy(frame, begin, this.data, 0, this.data.length);

            } catch (Exception e) {
                LogUtils.e("VarLenFrame parse err: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private IAsdu parseAsdu(byte[] frame, int begin, int len) {
            return new ASDU(frame, begin);
        }
    }

    public static final class VSQ_Obj {
        public int num;
        public int sq;
        public byte data;
        public VSQ_Obj(int num, int sq) {
            data = 0;
            this.num = num;
            this.sq = sq;

            data = (byte) ((num) & 0x7F);
            if (sq == 1) {
                data = (byte) (data | 0x80);
            }

        }

        public VSQ_Obj(byte data) {
            this.data = data;
            this.num = (data) & 0x7F;
            this.sq = 0;
            if ((data & 0x80) != 0) {
                this.sq = 1;
            }
        }
    }

    public static final class DataUnitLable {
        public int TI;
        public byte VSQ;
        public VSQ_Obj vsq_obj;
        public byte[] COT;
        public int cot;
        public byte[] CO_ADDR;
        public int commonAddr;
        public byte[] data;
        public DataUnitLable(int ti, byte vsq, int cot, int coAddr) {
            this.TI = ti;
            this.VSQ = vsq;
            this.vsq_obj = new VSQ_Obj(vsq);
            this.COT = new byte[2];
            this.COT[0] = (byte)(cot & 0x00FF);
            this.COT[1] = (byte)((cot >> 8) & 0x00FF);
            this.cot = cot;
            this.CO_ADDR = new byte[2];
            this.CO_ADDR[0] = (byte)(coAddr & 0xFF);
            this.CO_ADDR[1] = (byte)((coAddr >> 8) & 0xFF);
            this.commonAddr = coAddr;

            this.data = new byte[6];
            this.data[0] = (byte) TI;
            this.data[1] = VSQ;
            this.data[2] = COT[0];
            this.data[3] = COT[1];
            this.data[4] = CO_ADDR[0];
            this.data[5] = CO_ADDR[1];

        }

        public DataUnitLable(byte[] frame, int begin) {
            try {
                TI = frame[begin] & 0xFF;
                VSQ = frame[begin + 1];
                this.vsq_obj = new VSQ_Obj(VSQ);
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

    public static class InfoObj{
        public byte[] data;
        public InfoObj() {

        }
        public InfoObj(byte[] frame, int begin) {

        }
    }

    public static final class ZongZhaoInfoObj extends InfoObj {
        public int QOI;
        public int infoObjAddr;
        public ZongZhaoInfoObj(int infoObjAddr, int qoi) {
            this.QOI = qoi;
            this.data = new byte[3];
            this.data[0] = (byte) (infoObjAddr & 0xFF);
            this.data[1] = (byte) ((infoObjAddr >> 8) & 0xFF);
            this.data[2] = (byte) (qoi & 0xFF);
        }

        public ZongZhaoInfoObj(byte[] frame, int begin) {
            try {
                this.infoObjAddr = frame[begin] + frame[begin + 1] * 256;
                this.QOI = frame[begin + 2];
                this.data = new byte[3];
                System.arraycopy(frame, begin, this.data, 0, 3);

            } catch (Exception e) {
                LogUtils.e("zong zhao info obj parsed err: " + e.getMessage());
            }

        }

    }

    public static final class ClockInfoObj extends InfoObj {
        public String clockTime;
        public int infoObjAddr;

        //date: yyMMDD; time:HHmmSS
        public ClockInfoObj (String date, String time) {
            try {
                infoObjAddr = 0;
                byte[] cp56 = Ltu645ProtocolHelper.make_CP56_Time(date, time);
                this.clockTime = Ltu645ProtocolHelper.parse_CP56_Time(cp56);

                this.data = new byte[9];

                this.data[0] = 0;
                this.data[1] = 0;
                System.arraycopy(cp56, 0, data, 2, 7);

            } catch (Exception e) {
                LogUtils.e("make info body err:" + e.getMessage());
            }
        }

        public ClockInfoObj (byte[] frame, int begin) {
            try {
                this.infoObjAddr = frame[begin] + frame[begin + 1] * 256;
                int pos = begin + 2;

                byte[] cp56_d = DataConvertUtils.getSubByteArray(frame, pos, pos + 6);
                this.clockTime = Ltu645ProtocolHelper.parse_CP56_Time(cp56_d);

                this.data = DataConvertUtils.getSubByteArray(frame, begin, begin + 8);


            } catch (Exception e) {
                LogUtils.e("parse clock info body err: " + e.getMessage());
            }

        }


    }

    public static final class YaoCe_YaoXin_SQ_1_InfoObj extends InfoObj {
        public int yaoCeObjAddr;
        public String infoAddr;
        ArrayList<String> values = new ArrayList<>();
        public YaoCe_YaoXin_SQ_1_InfoObj(int ti, int vsq, byte[] frame, int begin) {
            try {
                int pos = begin;
                this.yaoCeObjAddr = (frame[begin] & 0xFF) + (frame[begin + 1] & 0xFF) * 256;
                this.infoAddr = DataConvertUtils.convertByteArrayToString(frame, begin, begin + 1, true);
                pos = pos + 2;

                if (ti == 13 || ti == 9 || ti == 11)  { //YaoCe
                    for (int i = 0; i < vsq; i++) {
                        if (ti == 13) {
                            values.add(DataConvertUtils.byte4ToFloat_Str(frame, pos));
                        } else if (ti == 9) {
                            values.add(DataConvertUtils.byte4ToFloat_Str(frame, pos));
                        } else if (ti == 11) {
                            values.add(DataConvertUtils.byte4ToFloat_Str(frame, pos));
                        }
                        pos = pos + 5;
                    }
                } else if (ti == 1 || ti == 3) { //YaoXin
                    for (int i = 0; i < vsq; i++) {
                        if (ti == 1) {
                            values.add("单点:" + DataConvertUtils.convertByteToString(frame[pos]));
                        } else if (ti == 3) {
                            values.add("双点:" + DataConvertUtils.convertByteToString(frame[pos]));
                        }
                        pos = pos + 1;
                    }
                }

                this.data = DataConvertUtils.getSubByteArray(frame, begin, pos - 1);

            } catch (Exception e) {
                LogUtils.e("yc sq 1, err: " + e.getMessage());
            }
        }

        @Override
        public String toString() {
            if (this.data == null || this.data.length <= 0) {
                return "NULL";
            }
            return "信息点地址：" + this.infoAddr + ", 数据：" + Arrays.toString(values.toArray());
        }
    }

    public static final class InfoObjList {
        public byte[] data;
        public ArrayList<InfoObj> infoObjs;

        public InfoObjList(ArrayList<InfoObj> objs) {
            infoObjs = objs;
            if (objs == null ||objs.size() <= 0) {
                LogUtils.i("objs is null");
                return;
            }

            int size = 0;
            for (int i = 0; i < objs.size(); i++) {
                InfoObj item = objs.get(i);
                if (item == null || item.data == null || item.data.length <= 0) {
                    continue;
                }
                size = size + item.data.length;
            }

            int pos = 0;
            this.data = new byte[size];
            for (int i = 0; i < objs.size(); i++) {
                InfoObj item = objs.get(i);
                if (item == null || item.data == null || item.data.length <= 0) {
                    continue;
                }
                System.arraycopy(item.data, 0, this.data, pos, item.data.length);
                pos = pos + item.data.length;
            }

        }

        public InfoObjList(DataUnitLable lable, byte[] frame, int begin) {
            try {
                LogUtils.i("InfoObjList, lable TI: " + lable.TI + ", cot: " + lable.cot);
                infoObjs = new ArrayList<>();
                if (lable.TI == 100 && lable.cot == 6) {
                    infoObjs.add(new ZongZhaoInfoObj(frame, begin));
                } else if (lable.TI == 100 && lable.cot == 7) {
                    infoObjs.add(new ZongZhaoInfoObj(frame, begin));
                } else if ((lable.cot == 20 || lable.cot == 3) && lable.vsq_obj.sq == 1) {
                    infoObjs.add(new YaoCe_YaoXin_SQ_1_InfoObj(lable.TI, lable.vsq_obj.num, frame, begin));
                } else if (lable.TI == 103 && (lable.cot == 7 || lable.cot == 5)) {
                    infoObjs.add(new ClockInfoObj(frame, begin));
                } else if (lable.TI == 202 && lable.cot == 7) {
                    infoObjs.add(new Read_Param_Obsv_InfoObj(lable.vsq_obj.num, frame, begin));
                }

                int size = 0;
                for (int i = 0; i < infoObjs.size(); i++) {
                    InfoObj item = infoObjs.get(i);
                    if (item == null || item.data == null || item.data.length <= 0) {
                        continue;
                    }
                    size = size + item.data.length;
                }

                int pos = 0;
                this.data = new byte[size];
                for (int i = 0; i < infoObjs.size(); i++) {
                    InfoObj item = infoObjs.get(i);
                    if (item == null || item.data == null || item.data.length <= 0) {
                        continue;
                    }
                    System.arraycopy(item.data, 0, this.data, pos, item.data.length);
                    pos = pos + item.data.length;
                }

            } catch (Exception e){
                LogUtils.e("p err : " + e.getMessage());
            }
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
            else
            {
                LogUtils.i("lable is null");
            }
            if (objList != null && objList.data != null) {
                len = len + objList.data.length;
            }
            else
            {
                LogUtils.i("objList is null");
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
                this.infoObjList = new InfoObjList(dataUnitLable, frame, begin + 6);
                DataUnitLable lable = this.dataUnitLable;
                InfoObjList objList = this.infoObjList;
                int len = 0;
                int pos = 0;
                if (lable != null && lable.data != null) {
                    len = len + lable.data.length;
                }
                else
                {
                    LogUtils.i("lable is null");
                }
                if (objList != null && objList.data != null) {
                    len = len + objList.data.length;
                }
                else
                {
                    LogUtils.i("objList is null");
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
            } catch (Exception e) {
                LogUtils.e("ASDU parse err: " + e.getMessage());
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

    public static final class Read_Param_Ctrl_InfoObj extends InfoObj {
        public int sn;
        ArrayList<String> infoAddrList = new ArrayList<>();
        public Read_Param_Ctrl_InfoObj(int sn, ArrayList<String> list) {
            try {
                this.sn = sn;
                this.infoAddrList = list;
                this.data = new byte[2 + list.size() * 2];
                this.data[0] = (byte) (sn & 0xFF);
                this.data[1] = (byte) ((sn >> 8) & 0xFF);
                int pos = 2;
                for (int i = 0; i < list.size(); i++) {
                    byte[] info_d = DataConvertUtils.convertHexStringToByteArray(list.get(i), list.get(i).length(), true);
                    data[pos] = info_d[0];
                    data[pos + 1] = info_d[1];
                    pos = pos + 2;
                }
            } catch (Exception e) {
                LogUtils.e("yc sq 1, err: " + e.getMessage());
            }
        }

        @Override
        public String toString() {
            if (this.data == null || this.data.length <= 0) {
                return "NULL";
            }
            return "SN：" + this.sn + ", 信息体地址：" + Arrays.toString(infoAddrList.toArray());
        }
    }


    public static final class Read_Param_Obsv_InfoObj extends InfoObj {
        public int sn;
        public byte parmCharaLable;
        ArrayList<Param_Obsv_Val> values = new ArrayList<>();
        public Read_Param_Obsv_InfoObj(int vsq, byte[] frame, int begin) {
            try {
                sn = frame[begin + 1] * 256 + frame[begin];
                parmCharaLable = frame[begin + 2];
                int pos = begin + 3;
                for (int i = 0; i < vsq; i++) {
                    Param_Obsv_Val item = new Param_Obsv_Val(frame, pos);
                    values.add(item);
                    pos = pos + item.data.length;
                }

                this.data = DataConvertUtils.getSubByteArray(frame, begin, pos - 1);


            } catch (Exception e) {
                LogUtils.e("Read_Param_Obsv_InfoObj, err: " + e.getMessage());
            }
        }

        @Override
        public String toString() {
            if (this.data == null || this.data.length <= 0) {
                return "NULL";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < values.size(); i++) {
                sb.append(values.get(i).toString()).append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public static final class Set_Param_Pre_InfoObj extends InfoObj {
        public int sn;
        public byte parmCharaLable;
        ArrayList<Param_Obsv_Val> values = new ArrayList<>();
        public Set_Param_Pre_InfoObj(int sn, byte parmCharaLable, ArrayList<Param_Obsv_Val> param_obsv_vals) {
            try {
                this.sn = sn;
                this.parmCharaLable = parmCharaLable;
                values = param_obsv_vals;

                int len = 0;
                for (int i = 0; i < values.size(); i++) {
                    len = values.get(i).data.length + len;
                }
                this.data = new byte[3 + len];

                this.data[0] = (byte) (sn & 0xFF);
                this.data[1] = (byte) ((sn >> 8) & 0xFF);
                this.data[2] = parmCharaLable;
                int pos = 3;
                for (int i = 0; i < values.size(); i++) {
                    Param_Obsv_Val item = values.get(i);
                    System.arraycopy(item.data, 0, data, pos, item.data.length);
                    pos = pos + item.data.length;
                }

            } catch (Exception e) {
                LogUtils.e("Read_Param_Obsv_InfoObj, err: " + e.getMessage());
            }
        }

        @Override
        public String toString() {
            if (this.data == null || this.data.length <= 0) {
                return "NULL";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < values.size(); i++) {
                sb.append(values.get(i).toString()).append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public static final class Param_Obsv_Val {
        public String infoAddr;
        public byte tag;
        public String tag_s;
        public String value;

        public byte[] data;

        public Param_Obsv_Val(String infoAddr, byte tag, String value) {
            try {
                this.infoAddr = infoAddr;
                this.tag = tag;
                this.value = value;
                int len = getLen(tag, value);

                byte[] v_d = getData(tag, value);

                this.data = new byte[4 + len];

                byte[] add_d = DataConvertUtils.convertHexStringToByteArray(infoAddr, infoAddr.length(), true);
                this.data[0] = add_d[0];
                this.data[1] = add_d[1];
                this.data[2] = tag;
                this.data[3] = (byte) len;
                int pos = 4;
                for (int i = 0; i < v_d.length; i++) {
                    this.data[pos + i] = v_d[i];
                }

            } catch (Exception e) {
                LogUtils.e("Param_Obsv_Val, make err:" + e.getMessage());
            }

        }

        public Param_Obsv_Val(byte[] frame, int begin) {
            this.infoAddr = DataConvertUtils.convertByteArrayToString(frame, begin, begin + 1, true);
            this.tag = frame[begin + 2];
            int pos = begin + 3;
            int len = frame[pos];
            pos = pos + 1;
            int end = pos + len - 1;

            byte[] val_data = DataConvertUtils.getSubByteArray(frame, pos, end);
            value = conV(tag, val_data);
            this.data = DataConvertUtils.getSubByteArray(frame, begin, end);
        }

        @Override
        public String toString() {
            return this.infoAddr + "：" + value;
        }


    }

    private static String conV(byte tag, byte[] data) {
        switch (tag & 0xFF) {
            case 1:
                return DataConvertUtils.ByteToBool(data[0]) + "";
            case 43:
                return DataConvertUtils.ByteToTiny(data[0]) + "";
            case 32:
                return DataConvertUtils.ByteToUTiny(data[0]) + "";
            case 33:
                return DataConvertUtils.ByteToShort(data) + "";
            case 45:
                return DataConvertUtils.ByteToUShort(data) + "";
            case 2:
                return DataConvertUtils.ByteToInt(data) + "";
            case 35:
                return DataConvertUtils.ByteToUInt(data) + "";
            case 36:
                return DataConvertUtils.ByteToLong(data) + "";
            case 37:
                return DataConvertUtils.ByteToUlong(data) + "";
            case 38:
                return DataConvertUtils.byte4ToFloat_Str(data, 0);
            case 39:
                return DataConvertUtils.byte4ToDouble_Str(data, 0);
            case 4:
                return DataConvertUtils.ByteToStr(data);
        }
        return DataConvertUtils.convertByteArrayToString(data, false);
    }

    private static int getLen(byte tag, String value) {
        switch (tag & 0xFF) {
            case 1:
                return 1;
            case 43:
                return 1;
            case 32:
                return 1;
            case 33:
                return 2;
            case 45:
                return 2;
            case 2:
                return 4;
            case 35:
                return 4;
            case 36:
                return 8;
            case 37:
                return 8;
            case 38:
                return 4;
            case 39:
                return 8;
            case 4:
                return value.length() + 1;
        }
        return 4;
    }

    private static byte[] getData(byte tag, String value) {
        switch (tag & 0xFF) {
            case 1:
                return DataConvertUtils.BoolToByte(Boolean.valueOf(value));
            case 43:
                return DataConvertUtils.TinyToByte(Byte.valueOf(value));
            case 32:
                return DataConvertUtils.UTinyToByte(Integer.valueOf(value));
            case 33:
                return DataConvertUtils.ShortToByte(Short.valueOf(value));
            case 45:
                return DataConvertUtils.UShortToByte(Integer.valueOf(value));
            case 2:
                return DataConvertUtils.IntToByte(Integer.valueOf(value));
            case 35:
                return DataConvertUtils.UIntToByte(Long.valueOf(value));
            case 36:
                return DataConvertUtils.LongToByte(Long.valueOf(value));
            case 37:
                return DataConvertUtils.UlongToByte(Long.valueOf(value));
            case 38:
                return DataConvertUtils.floatToByte(Float.valueOf(value));
            case 39:
                return DataConvertUtils.doubleToByte(Double.valueOf(value));
            case 4:
                return DataConvertUtils.StrToByte(value);
        }
        return new byte[]{0, 0, 0, 0};
    }

    public static int getTag(String type) {
        String t = type.toLowerCase();
        switch (t) {
            case "boolean":
                return 1;
            case "tiny":
                return 43;
            case "utiny":
                return 32;
            case "short":
                return 33;
            case "ushort":
                return 45;
            case "int":
                return 2;
            case "uint":
                return 35;
            case "long":
                return 36;
            case "ulong":
                return 37;
            case "float":
                return 38;
            case "double":
                return 39;
            case "octerstring":
                return 4;
            case "string":
                return 4;
            default:
                return 0;
        }
    }



}

