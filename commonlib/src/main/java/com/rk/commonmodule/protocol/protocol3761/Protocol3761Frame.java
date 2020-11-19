package com.rk.commonmodule.protocol.protocol3761;

import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;

public class Protocol3761Frame {
    public static class LengthArea {
        public int D0, D1;
        public int length;
        public byte[] data;

        public LengthArea(int D0, int D1, int length) {
            this.D0 = D0 & 0x01;
            this.D1 = D1 & 0x01;
            if (length > 16383) {
                this.length = 16383;
            } else {
                this.length = length;
            }
            data = new byte[2];
            data[0] = 0x00;
            data[1] = 0x00;

            data[1] = (byte) ((this.length >> 6) & 0xFF | data[1]);
            data[0] = (byte) ((this.length << 2) & 0xFC | data[0]);
            if (this.D1 == 0) {
                data[0] = (byte) (data[0] & 0xFD);
            } else {
                data[0] = (byte) (data[0] | 0x02);
            }

            if (this.D0 == 0) {
                data[0] = (byte) (data[0] & 0xFE);
            } else {
                data[0] = (byte) (data[0] & 0x01);
            }
        }

        public LengthArea(byte[] frame, int begin) {
            if (frame != null && begin + 1 <= frame.length - 1 && begin > 0) {
                data = new byte[2];
                data[0] = frame[begin];
                data[1] = frame[begin + 1];
                if ((data[0] & 0x01) == 0) {
                    this.D0 = 0;
                } else {
                    this.D0 = 1;
                }
                if ((data[0] & 0x02) == 0) {
                    this.D1 = 0;
                } else {
                    this.D1 = 1;
                }
                length = 0;
                length = ((data[0] >> 2) | length) & 0x3F | ((data[1] << 6) & 0x3FC0);
            }
        }
    }

    public static class AddrArea {
        public String A1;
        public int A2 = 0;
        public int A3 = 0;
        public byte[] data;
        public AddrArea(String A1, int A2, int A3) {
            this.A1 = A1;
            this.A2 = A2;
            this.A3 = A3;

            if (A1 == null || A1.length() != 4 || !DataConvertUtils.isNumberString(A1)
                    || A2 < 1 || A2 > 65535
                    || A3 < 0 || A3 > 255) {
                return;

            }
            data = new byte[5];
            data[0] = (byte) Integer.parseInt(A1.substring(2, 4), 16);
            data[1] = (byte) Integer.parseInt(A1.substring(0, 2), 16);

            data[2] = (byte) (A2 & 0xFF);
            data[3] = (byte) ((A2>>8) & 0xFF);

            data[4] = (byte) (A3 & 0xFF);

        }

        public AddrArea(byte[] frame, int begin) {

            if (frame != null && begin > 0 && begin + 4 <= frame.length - 1) {
                A1 = DataConvertUtils.convertByteArrayToString(frame, begin, begin + 1, true);
                A2 = (frame[begin + 3] * 256 + frame[begin + 2]) & 0xFFFF;
                A3 = frame[begin + 4] & 0xFF;
                data = new byte[5];
                System.arraycopy(frame, begin, data, 0, 5);
            }
        }
    }

    public static class LinkUserData {
        public byte AFN;
        public byte SEQ;
        public byte[] DATA_UNIT;
        public byte[] AUX;
        public byte[] data;

        public LinkUserData(byte afn, byte seq, byte[] data_unit, byte[] aux) {

        }

        public LinkUserData(byte[] frame, int begin) {
            if (frame != null && begin > 0 && begin + 1 <= frame.length - 1) {
                AFN = frame[begin];
                SEQ = frame[begin + 1];
            }

        }
    }

    public static class DA {
        public byte DA1;
        public byte DA2;
        public byte[] data;
        public int[] pn;

        public DA(byte da1, byte da2) {
            init(da1, da2);
        }

        public DA(byte[] frame, int begin) {
            if (frame != null && begin > 0 && begin + 1 <= frame.length - 1) {
                init(frame[begin], frame[begin + 1]);
            }
        }

        public DA(int pn) {
            if (pn >=0 && pn <=2040) {
                this.pn = new int[1];
                this.pn[0] = pn;
                if (pn == 0) {
                    this.DA1 = 0x00;
                    this.DA2 = 0x00;
                } else {
                    this.DA2 = (byte) ((pn + (8 - 1)) / 8);
                    int index = pn - (this.DA2 - 1) * 8 - 1;
                    this.DA1 = (byte) (0x01 <<index);
                }
                this.data = new byte[2];
                this.data[0] = this.DA1;
                this.data[1] = this.DA2;
            }
        }

        private void init(byte da1, byte da2) {
            this.DA1 = da1;
            this.DA2 = da2;
            data = new byte[2];
            data[0] = da1;
            data[1] = da2;
            ArrayList<Integer> indexArray = new ArrayList<>();
            if (da2 > 0) {
                for (int i = 0; i < 8; i++) {
                    if (((da1 >> i) & 0x01) == 1) {
                        indexArray.add(i);
                    }
                }
                if (indexArray.size() > 0) {
                    pn = new int[indexArray.size()];
                    for (int i = 0; i < indexArray.size(); i++) {
                        int index = indexArray.get(i);
                        pn[i] = (da2  - 1) * 8 + (index + 1);
                    }
                }
            } else {
                if (da1 == 0x00) {
                    pn = new int[1];
                    pn[0] = 0;
                }

            }
        }
    }

    public static class DT {
        public byte DT1;
        public byte DT2;
        public byte[] data;
        public int[] fn;

        public DT(byte dt1, byte dt2) {
            init(dt1, dt2);
        }

        public DT(byte[] frame, int begin) {
            if (frame != null && begin > 0 && begin + 1 <= frame.length - 1) {
                init(frame[begin], frame[begin + 1]);
            }
        }

        public DT(int fn) {
            if (fn >=1 && fn <=248) {
                this.fn = new int[1];
                this.fn[0] = fn;
                this.DT2 = (byte) ((fn - 1) / 8);
                int index = fn - this.DT2 * 8 - 1;
                this.DT1 = (byte) (0x01 <<index);
                this.data = new byte[2];
                this.data[0] = this.DT1;
                this.data[1] = this.DT2;
            }
        }

        private void init(byte dt1, byte dt2) {
            this.DT1 = dt1;
            this.DT2 = dt2;
            data = new byte[2];
            data[0] = dt1;
            data[1] = dt2;
            ArrayList<Integer> indexArray = new ArrayList<>();
            if (dt2 >= 0) {
                for (int i = 0; i < 8; i++) {
                    if (((dt1 >> i) & 0x01) == 1) {
                        indexArray.add(i);
                    }
                }
                if (indexArray.size() > 0) {
                    fn = new int[indexArray.size()];
                    for (int i = 0; i < indexArray.size(); i++) {
                        int index = indexArray.get(i);
                        fn[i] = dt2 * 8 + (index + 1);
                    }
                }
            }
        }
    }

    public static class DataUnitID {
        public DA da;
        public DT dt;
        public byte[] data;

        public DataUnitID(DA da, DT dt) {
            if (da.data != null && dt.data != null) {
                this.da = da;
                this.dt = dt;
                this.data = new byte[4];
                System.arraycopy(da.data, 0, this.data, 0 , 2);
                System.arraycopy(dt.data, 0, this.data, 2 , 2);
            }
        }

        public DataUnitID(byte[] frame, int begin) {
            if (frame != null && begin > 0 && begin + 3 < frame.length - 1) {
                this.da = new DA(frame, begin);
                this.dt = new DT(frame, begin + 2);
                this.data = new byte[4];
                System.arraycopy(this.da.data, 0, this.data, 0 , 2);
                System.arraycopy(this.dt.data, 0, this.data, 2 , 2);
            }
        }
    }

    public static class DataUnit {
        public byte[] data;
        public DataUnit() {

        }

        public DataUnit(byte[] frame, int begin, DataUnit dataUnit) {

        }
    }
}
