package com.rk.commonmodule.protocol.protocol698;

import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;

public class Protocol698Frame {

    public static class PIID {
        public int priority;
        public int serviceNum;
        public byte data;
        public PIID(int priority, int serviceNum) {
            this.priority = priority;
            this.serviceNum = serviceNum;
            if (priority >=0 && priority <= 1 && serviceNum >= 0 && serviceNum <= 63) {
                if (priority == 0) {
                    data = (byte) (0x7F & serviceNum);
                } else {
                    data = (byte) (0x80 | serviceNum);
                }
            }
        }
        public PIID(byte data) {
            this.data = data;
            if ((data & 0x80) == 0x80) {
                this.priority = 1;
            } else {
                this.priority = 0;
            }
            this.serviceNum = data & 0x3F;
        }
    }

    public static class PIID_ACD {
        public int priority;
        public boolean accessACD;
        public int serviceNum;
        public byte data;
        public PIID_ACD(int priority, boolean accessACD, int serviceNum) {
            this.priority = priority;
            this.accessACD = accessACD;
            this.serviceNum = serviceNum;
            if (priority >=0 && priority <= 1 && serviceNum >= 0 && serviceNum <= 63) {
                if (priority == 0) {
                    data = (byte) (0x7F & serviceNum);
                } else {
                    data = (byte) (0x80 | serviceNum);
                }

                if (accessACD) {
                    data = (byte) (data | 0x40);
                } else {
                    data = (byte) (data & 0xBF);
                }
            }
        }
        public PIID_ACD(byte data) {
            this.data = data;
            if ((data & 0x80) == 0x80) {
                this.priority = 1;
            } else {
                this.priority = 0;
            }

            if ((data & 0x40) == 0x00) {
                this.accessACD = false;
            } else {
                this.accessACD = true;
            }
            this.serviceNum = data & 0x3F;
        }
    }

    public static class OAD {
        public byte[] data;
        public byte[] OI = new byte[2];
        public int OI_VALUE;
        public int propertyChara;
        public int propertyNum;
        public int propertyInnerIdx;
        public OAD(byte[] data) {
            this.data = data;
            if (data.length >= 4) {
                OI[0] = data[0];
                OI[1] = data[1];
                OI_VALUE = OI[1] * 16 + OI[0];
                propertyNum = data[2] & 0x1F;
                propertyChara = (data[2] & 0xE0) / 32;
                propertyInnerIdx = data[3];
            }
        }
        public OAD(int OI_VALUE, int propertyNum, int propertyChara, int propertyInnerIdx) {
            this.OI_VALUE = OI_VALUE;
            this.propertyChara = propertyChara;
            this.propertyNum = propertyNum;
            this.propertyInnerIdx = propertyInnerIdx;
            if (OI_VALUE >=0 && OI_VALUE <= 65535 && propertyChara >= 0 && propertyChara <= 7
                    && propertyNum >= 0 && propertyNum <= 31 && propertyInnerIdx >= 0 && propertyInnerIdx <= 255) {
                data = new byte[4];
                data[0] = (byte) (OI_VALUE & 0x00FF);
                data[1] = (byte) ((OI_VALUE & 0xFF00) / 256);
                data[2] = (byte) ((propertyChara * 32) | (propertyNum & 0x1F));
                data[3] = (byte) propertyInnerIdx;
            }

        }

    }

    public class TimeTag {
        public byte[] data;
        public DateTimeS sendTime;
        public TI delayTime;

        public TimeTag(DateTimeS sendTime, TI delayTime) {
            this.sendTime = sendTime;
            this.delayTime = delayTime;
            if (sendTime != null && sendTime.data != null && delayTime != null && delayTime.data != null) {
                data = new byte[sendTime.data.length + delayTime.data.length];
                for (int i = 0; i < sendTime.data.length; i++) {
                    data[i] = sendTime.data[i];
                }
                for (int i = sendTime.data.length; i < data.length; i++) {
                    data[i] = delayTime.data[i - sendTime.data.length];
                }
            }

        }

        public TimeTag(byte[] data) {
            this.data = data;
            if (data != null && data.length >= 10) {
                byte[] sendTimeData = new byte[7];
                byte[] delayTimeData = new byte[3];
                for (int i = 0; i < sendTimeData.length; i++) {
                    sendTimeData[i] = data[i];
                }
                for (int i = 0; i < delayTimeData.length; i++) {
                    delayTimeData[i] = data[i + sendTimeData.length];
                }
                this.sendTime = new DateTimeS(sendTimeData);
                this.delayTime = new TI(delayTimeData);
            }

        }
    }

    public enum FRAME_UNIT {
        BYTE_UNIT,KBYTE_UNIT,
    }

    public static class Length_Area {
        public FRAME_UNIT frame_unit;
        public int length;
        public byte[] data;

        public Length_Area(FRAME_UNIT frame_unit, int length) {
            this.frame_unit = frame_unit;
            this.length = length;
            data = new byte[2];

            data[0] = (byte) (length & 0xFF);
            data[1] = (byte) ((length / 256) & 0x3F);
            switch (frame_unit) {
                case BYTE_UNIT:
                    data[1] = (byte) (data[1] & 0xBF);
                    break;
                case KBYTE_UNIT:
                    data[1] = (byte) (data[1] | 0x40);
                    break;
            }

        }

        public Length_Area(byte[] data) {
            this.data = data;
            if (data != null && data.length == 2) {
                this.length = (data[1] & 0x3F) * 256 + data[0];

                if ((data[1] & 0x40) == 0x00) {
                    this.frame_unit = FRAME_UNIT.BYTE_UNIT;
                } else {
                    this.frame_unit = FRAME_UNIT.KBYTE_UNIT;
                }

            }

        }
    }

    public enum ADDRESS_TYPE {
        SINGLE, GROUP, WILDCARD, BROADCAST,
    }

    public static class SERV_ADDR {
        public ADDRESS_TYPE address_type;
        public boolean hasExLogicAddress;
        public int logicAddress;
        public int addressLength;
        public byte[] address;

        public byte[] data;

        public SERV_ADDR(ADDRESS_TYPE address_type, boolean hasExLogicAddress, int logicAddress, int addressLength, byte[] address) {
            this.address_type = address_type;
            this.hasExLogicAddress = hasExLogicAddress;
            this.logicAddress = logicAddress;
            this.addressLength = addressLength;
            this.address = address;

            if (addressLength >= 1 && addressLength <= 16 && address != null && address.length == addressLength) {
                data = new byte[addressLength + 1];
                data[0] = (byte) (data[0] & 0x00);
                data[0] = (byte) (data[0] | ((addressLength - 1) & 0x0F));
                if (hasExLogicAddress) {
                    data[0] = (byte) (data[0] | 0x20);
                } else {
                    if (logicAddress == 0) {
                        data[0] = (byte) (data[0] | 0x00);
                    } else if (logicAddress == 1) {
                        data[0] = (byte) (data[0] | 0x10);
                    } else {
                        data[0] = (byte) (data[0] | 0x00);
                    }

                }
                switch (address_type) {
                    case SINGLE:
                        data[0] = (byte) (data[0] | 0x00);
                        break;
                    case GROUP:
                        data[0] = (byte) (data[0] | 0x80);
                        break;
                    case WILDCARD:
                        data[0] = (byte) (data[0] | 0x40);
                        break;
                    case BROADCAST:
                        data[0] = (byte) (data[0] | 0xC0);
                        break;
                }

                for (int i = 0; i < addressLength; i++) {
                    data[i + 1] = address[i];
                }

            }

        }

        public SERV_ADDR(byte[] data) {
            this.data = data;

            if (data != null && data.length >= 2) {
                int length = data[0] & 0x0F;
                if (length + 1 == data.length - 1) {
                    this.addressLength = length + 1;
                    if ((data[0] & 0x20) == 0x00) {
                        this.hasExLogicAddress = false;
                        if ((data[0] & 0x10) == 0x00) {
                            this.logicAddress = 0;
                        } else {
                            this.logicAddress = 1;
                        }

                    } else {
                        this.hasExLogicAddress = true;
                    }

                    switch (data[0] & 0xC0) {
                        case 0x00:
                            address_type = ADDRESS_TYPE.SINGLE;
                            break;
                        case 0x40:
                            address_type = ADDRESS_TYPE.WILDCARD;
                            break;
                        case 0x80:
                            address_type = ADDRESS_TYPE.GROUP;
                            break;
                        case 0xC0:
                            address_type = ADDRESS_TYPE.BROADCAST;
                            break;
                    }
                }

            }

        }
    }

    public static class AddressArea {
        public SERV_ADDR serv_addr;
        public byte client_addr;
        public byte[] data;

        public AddressArea(SERV_ADDR serv_addr, byte client_addr) {
            this.serv_addr = serv_addr;
            this.client_addr = client_addr;

            if (serv_addr != null && serv_addr.data.length > 0) {
                data = new byte[serv_addr.data.length + 1];
                for (int i = 0; i < serv_addr.data.length; i++) {
                    data[i] = serv_addr.data[i];
                }
                data[serv_addr.data.length] = client_addr;
            }

        }

        public AddressArea(byte[] data) {
            this.data = data;
            if (data != null && data.length >= 3) {
                byte[] serAddrData = new byte[data.length - 1];
                for (int i = 0; i < serAddrData.length; i++) {
                    serAddrData[i] = data[i];
                }
                this.serv_addr = new SERV_ADDR(serAddrData);
                this.client_addr = data[data.length - 1];
            }
        }
    }



    public enum DIR_PRM {
        CLIENT_RESPONSE, CLIENT_REQUEST, SERVER_REPORT, SERVER_RESPONSE,
    }

    public static class CtrlArea {
        public DIR_PRM dir_prm;
        public boolean isSplitFrame;
        public boolean isScramble;
        public int functionCode;
        public byte data;

        public CtrlArea(DIR_PRM dir_prm, boolean isSplitFrame, boolean isScramble, int functionCode) {
            this.dir_prm = dir_prm;
            this.isSplitFrame = isSplitFrame;
            this.isScramble = isScramble;
            this.functionCode = functionCode;
            this.data = Protocol698.PROTOCOL_698.makeCtrlArea(dir_prm, isSplitFrame, isScramble, functionCode);
        }

        public CtrlArea(byte data) {
            this.data = data;
            switch (data & 0xC0) {
                case 0x00:
                    this.dir_prm = DIR_PRM.CLIENT_RESPONSE;
                    break;
                case 0x40:
                    this.dir_prm = DIR_PRM.CLIENT_REQUEST;
                    break;
                case 0x80:
                    this.dir_prm = DIR_PRM.SERVER_REPORT;
                    break;
                case 0xC0:
                    this.dir_prm = DIR_PRM.SERVER_RESPONSE;
                    break;
            }

            if ((data & 0x20) == 0x00) {
                this.isSplitFrame = false;
            } else {
                this.isSplitFrame = true;
            }

            if ((data & 0x08) == 0x00) {
                this.isScramble = false;
            } else {
                this.isScramble = true;
            }
            this.functionCode = data & 0x07;
        }

    }

    public static class DateTimeS {
        public int year;
        public int month;
        public int day;
        public int hour;
        public int minute;
        public int second;
        public byte[] data;

        public DateTimeS(int year, int month, int day, int hour, int minute, int second) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            data = new byte[7];
            data[1] = (byte) (year & 0xFF);
            data[0] = (byte) ((year >> 8) & 0xFF);
            data[2] = (byte) (month & 0xFF);
            data[3] = (byte) (day & 0xFF);
            data[4] = (byte) (hour & 0xFF);
            data[5] = (byte) (minute & 0xFF);
            data[6] = (byte) (second & 0xFF);

        }

        public DateTimeS(byte[] data) {
            this.data = data;
            if (data != null && data.length>= 7) {
                this.year = ((data[0] << 8) & 0xFF00) | (data[1] & 0xFF);
                this.month = data[2];
                this.day = data[3];
                this.hour = data[4];
                this.minute = data[5];
                this.second = data[6];
            }
        }

        @Override
        public String toString() {
            return (this.year + "-" + postD(this.month) + "-" + postD(this.day) + " " + postD(this.hour) + ":" + postD(this.minute) + ":" + postD(this.second));
        }

    }

    private static String postD(int a) {
        if (a < 10) {
            return ("0" + a);
        }
        return String.valueOf(a);

    }

    public static final class TimeUnit {
        public int value;
        public String valueName;
        private TimeUnit(int value, String valueName) {
            this.value = value;
            this.valueName = valueName;
        }

        public final static TimeUnit SECOND = new TimeUnit(0, "秒");
        public final static TimeUnit MINUTE = new TimeUnit(1, "分");
        public final static TimeUnit HOUR = new TimeUnit(2, "时");
        public final static TimeUnit DAT = new TimeUnit(3, "日");
        public final static TimeUnit MONTH = new TimeUnit(4, "月");
        public final static TimeUnit YEAR = new TimeUnit(5, "年");

    }

    public static class TI {
        public byte[] data;
        public TimeUnit unit;
        public int delayTime;

        public TI(TimeUnit unit, int delayTime) {
            this.unit = unit;
            this.delayTime = delayTime;
            data = new byte[3];
            data[0] = (byte) unit.value;
            data[1] = (byte) (delayTime & 0xFF);
            data[2] = (byte) ((delayTime / 256) & 0xFF);
        }

        public TI(byte[] data) {
            this.data = data;
            if (data != null && data.length >= 3) {
                switch (data[0]) {
                    case 0:
                        this.unit = TimeUnit.SECOND;
                        break;
                    case 1:
                        this.unit = TimeUnit.MINUTE;
                        break;
                    case 2:
                        this.unit = TimeUnit.HOUR;
                        break;
                    case 3:
                        this.unit = TimeUnit.DAT;
                        break;
                    case 4:
                        this.unit = TimeUnit.MONTH;
                        break;
                    case 5:
                        this.unit = TimeUnit.YEAR;
                        break;

                }
                this.delayTime = data[2] * 256 + data[1];

            }
        }

    }

    public static class RSD {
        public int type;
        public Object obj;
        public byte[] data;

        public RSD(int type, Object obj) {
            this.type = type;
            switch (type) {
                case 0:
                    data = new byte[1];
                    data[0] = (byte) (type & 0xFF);
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    break;
                case 9:
                    if (obj != null && obj instanceof Selector9) {
                        Selector9 src = ((Selector9) obj);
                        data = new byte[1 + 1];
                        data[0] = (byte) (type & 0xFF);
                        data[1] = src.data;
                    }
                    break;
                case 10:
                    break;
            }

        }

        public RSD(byte[] data) {
            this.data = data;
            if (data != null && data.length > 0) {
                switch (data[0] & 0xFF) {
                    case 0:
                        this.type = 0;
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                    case 9:
                        this.type = 9;
                        if (data.length >= 2) {
                            this.obj = new Selector9(data[1]);
                        }
                        break;
                    case 10:
                        break;
                }
            }
        }
    }

    public static class Selector9 {
        public int last;
        public byte data;
        public Selector9(int last) {
            this.last = last;
            data = (byte) (last & 0xFF);

        }

        public Selector9(byte data) {
            this.data = data;
            this.last = data & 0xFF;
        }
    }

    public static class ROAD {
        public OAD oad;
        public ArrayList<OAD> oadArrayList = new ArrayList<>();
        public byte[] data;

        public ROAD(OAD oad, ArrayList<OAD> oadArrayList) {
            this.oad = oad;
            this.oadArrayList = oadArrayList;
            ArrayList<Byte> bytes = new ArrayList<>();
            if (oad != null && oad.data != null && oad.data.length > 0) {
                for (int i = 0; i < oad.data.length; i++) {
                    bytes.add(oad.data[i]);
                }
            }

            if (oadArrayList != null && oadArrayList.size() > 0) {
                bytes.add((byte)(oadArrayList.size()));
                for (int i = 0; i < oadArrayList.size(); i++) {
                    OAD oadItem = oadArrayList.get(i);
                    if (oadItem != null && oadItem.data != null && oadItem.data.length > 0) {
                        for (int j = 0; j < oadItem.data.length; j++) {
                            bytes.add(oad.data[i]);
                        }
                    }
                }
            }

            data = new byte[bytes.size()];
            for (int i = 0; i < data.length; i++) {
                data[i] = bytes.get(i);
            }
        }

        public ROAD(byte[] data) {
            this.data = data;

            if (data != null && data.length >= 5) {
                this.oad = new OAD(DataConvertUtils.getSubByteArray(data, 0, 3));
                int road_length = data[4];

                if (road_length > 0) {
                    for (int i = 1; i <= road_length - 1; i++) {
                        this.oadArrayList.add(new OAD(DataConvertUtils.getSubByteArray(data, i * 4 + 1, i * 4 + 4)));
                    }
                }
            }
        }
    }

    public static class CSD {
        public int type;
        public byte[] data;
        public Object object;

        public CSD(int type, Object object) {
            this.type = type;
            this.object = object;
            switch (type) {
                case 0:
                    if (object instanceof OAD) {
                        OAD src = (OAD) object;
                        if (src.data != null && src.data.length > 0) {
                            this.data = new byte[1 + src.data.length];
                            for (int i = 0; i < this.data.length; i++) {
                                if (i == 0) {
                                    this.data[0] = (byte) (type & 0xFF);
                                    continue;
                                }
                                this.data[i] = src.data[i - 1];
                            }
                        }
                    }
                    break;
                case 1:
                    if (object instanceof ROAD) {
                        ROAD src = (ROAD) object;
                        if (src.data != null && src.data.length > 0) {
                            this.data = new byte[1 + src.data.length];
                            for (int i = 0; i < this.data.length; i++) {
                                if (i == 0) {
                                    this.data[0] = (byte) (type & 0xFF);
                                    continue;
                                }
                                this.data[i] = src.data[i - 1];
                            }
                        }
                    }
                    break;
            }
        }

        public CSD(byte[] data) {
            if (data != null && data.length > 0) {
                this.data = data;
                this.type = data[0];
                switch (this.type) {
                    case 0:
                        if (data.length >= 5) {
                            this.object = new OAD(DataConvertUtils.getSubByteArray(data, 1, 4));
                        }
                        break;
                    case 1:
                        if (data.length >= 5) {
                            this.object = new ROAD(DataConvertUtils.getSubByteArray(data, 1, data.length - 1));
                        }
                        break;
                }
            }
        }
    }

    public static class RCSD {
        public int length = 0;
        public ArrayList<CSD> csdArrayList = new ArrayList<>();
        public byte[] data;

        public RCSD(ArrayList<CSD> csds) {
            this.csdArrayList = csds;
            if (csds != null && csds.size() > 0) {
                this.length = csds.size();
                ArrayList<Byte> bytes = new ArrayList<>();
                bytes.add((byte)(this.length));
                for (int i = 0; i < csds.size(); i++) {
                    CSD csdItem = csds.get(i);
                    if (csdItem != null && csdItem.data != null && csdItem.data.length > 0) {
                        for (int j = 0; j < csdItem.data.length; j++) {
                            bytes.add(csdItem.data[j]);
                        }
                    }
                }
                this.data = new byte[bytes.size()];
                for (int i = 0; i < this.data.length; i++) {
                    this.data[i] = bytes.get(i);
                }
            }

        }

        public RCSD(byte[] frame, int begin) {
            if (frame != null && frame.length > 0 && begin >= 0 && begin <= frame.length - 1) {
                if (frame[begin] == 0) {
                    this.data = new byte[1];
                    this.data[0] = 0;
                } else {
                    int cnt = frame[begin];
                    int pos = begin + 1;
                    int i = 0;
                    for (i = 0; i < cnt; i++) {
                        int csdLength = calcCsdLength(frame, pos);
                        if (csdLength > 0) {
                            CSD csd = new CSD(DataConvertUtils.getSubByteArray(frame, pos, pos + csdLength - 1));
                            this.csdArrayList.add(csd);
                            pos = pos + csdLength;
                        } else {
                            break;
                        }
                    }
                    if (i == cnt) {
                        this.data = DataConvertUtils.getSubByteArray(frame, begin, pos - 1);
                    } else {
                        this.data = null;
                        this.csdArrayList.clear();
                    }

                }
            }
        }

        private int calcCsdLength(byte[] frame, int begin) {
            int length_nextPos = -1;
            if (begin <= frame.length - 1) {
                if (frame[begin] == 0) {
                    length_nextPos = 5;
                } else if (frame[begin] == 1) {
                    if (begin + 5 <= frame.length - 1) {
                        int oadCnt = frame[begin + 5];
                        if (begin + 5 + oadCnt * 4 <= frame.length - 1) {
                            length_nextPos = 1 + 4 + 1 + oadCnt * 4;
                        }
                    }
                }
            }
            return length_nextPos;

        }
    }

    public static class GetRecord {
        public OAD oad;
        public RSD rsd;
        public RCSD rcsd;
        public byte[] data;

        public GetRecord(OAD oad, RSD rsd, RCSD rcsd) {
            this.oad = oad;
            this.rsd = rsd;
            this.rcsd = rcsd;
            ArrayList<Byte> bytes = new ArrayList<>();
            if (oad != null && oad.data != null && oad.data.length > 0) {
                for (int i = 0; i < oad.data.length ; i++) {
                    bytes.add(oad.data[i]);
                }
            }

            if (rsd != null && rsd.data != null && rsd.data.length > 0) {
                for (int i = 0; i < rsd.data.length ; i++) {
                    bytes.add(rsd.data[i]);
                }
            }

            if (rcsd != null && rcsd.data != null && rcsd.data.length > 0) {
                for (int i = 0; i < rcsd.data.length ; i++) {
                    bytes.add(rcsd.data[i]);
                }
            }

            this.data = new byte[bytes.size()];
            for (int i = 0; i < this.data.length; i++) {
                this.data[i] = bytes.get(i);
            }
        }

        public GetRecord(byte[] data) {
            if (data != null) {
                this.data = data;
            }
        }
    }

    public enum DataUnit_Type {
        CLEAR_TEXT, CIPHER_TEXT
    }
    public static class DataUnit {
        public DataUnit_Type type;
        public byte[] srcData;
        public byte[] data;

        public DataUnit(DataUnit_Type type, byte[] srcData) {
            this.type = type;
            this.srcData = srcData;
            if (srcData != null && srcData.length > 0) {
                data = new byte[1 + 1 + srcData.length];
                switch (type) {
                    case CLEAR_TEXT:
                        data[0] = 0;
                        break;
                    case CIPHER_TEXT:
                        data[0] = 1;
                        break;
                }
                data[1] = (byte) srcData.length;
                for (int i = 0; i < srcData.length; i++) {
                    data[i + 1 + 1] = srcData[i];
                }

            }
        }

    }

    public enum DataVerifyInfo_Type {
        SID_MAC, RN, RN_MAC, SID
    }
    public static class DataVerifyInfo {
        public DataVerifyInfo_Type type;
        public Object object;
        public byte[] data;

        public DataVerifyInfo(DataVerifyInfo_Type type, Object object) {
            this.type = type;
            object = object;
            switch (type) {
                case SID_MAC:
                    break;
                case RN:
                    if (object instanceof  RN) {
                        RN rn = (RN) object;
                        if (rn != null&& rn.data != null && rn.data.length > 0) {
                            data = new byte[1 + rn.data.length];
                            data[0] = 1;
                            for (int i = 0; i < rn.data.length; i++) {
                                data[i + 1] = rn.data[i];
                            }
                        }
                    }
                    break;
                case RN_MAC:
                    break;
                case SID:
                    break;
            }
        }

    }

    public static class RN {
        public byte[] srcData;
        public byte[] data;
        public RN(byte[] srcData) {
            this.srcData = srcData;
            if (srcData != null && srcData.length > 0) {
                data = new byte[1 + srcData.length];
                data[0] = (byte) srcData.length;
                for (int i = 0; i < srcData.length; i++) {
                    data[i + 1] = srcData[i];
                }
            }
        }
    }

    public enum Data_Type {
        NULL_TYPE,
        ARRAY_TYPE,
        STRUCTURE_TYPE,
        BOOL_TYPE,
        BIT_STRING_TYPE,
        DOUBLE_LONG_TYPE,
        DOUBLE_LONG_UNSIGNED_TYPE,
        OCTET_STRING_TYPE,
        VISIBLE_STRING_TYPE,
        UTF8_STRING_TYPE,
        INTEGER_TYPE,
        LONG_TYPE,
        UNSIGNED_TYPE,
        LONG_UNSIGNED_TYPE,
        LONG64_TYPE,
        LONG64_UNSIGNED_TYPE,
        ENUM_TYPE,
        FLOAT32_TYPE,
        FLOAT64_TYPE,
        DATE_TIME_TYPE,
        DATE_TYPE,
        TIME_TYPE,
        DATE_TIME_S_TYPE,
        OI_TYPE,
        OAD_TYPE,
        ROAD_TYPE,
        OMD_TYPE,
        TI_TYPE,
        TSA_TYPE,
        MAC_TYPE,
        RN_TYPE,
        REGION_TYPE,
        SCALER_UNIT_TYPE,
        RSD_TYPE,
        CSD_TYPE,
        MS_TYPE,
        SID_TYPE,
        SID_MAC_TYPE,
        COMDCB_TYPE,
        RCSD_TYPE,
    }

    public static class Data {
        public Data_Type type;
        public Object obj;
        public byte[] data;

        public Data(Data_Type type, Object obj) {
            this.type = type;
            this.obj = obj;
            switch (type) {
                case NULL_TYPE:
                    this.data = new byte[1];
                    this.data[0] = 0;
                    break;
                case DOUBLE_LONG_UNSIGNED_TYPE:
                    if (obj instanceof Integer) {
                        this.data = new byte[1 + 4];
                        this.data[0] = 6;
                        int value = (int) obj;
                        this.data[1] = (byte) ((value >> 24) & 0xFF);
                        this.data[2] = (byte) ((value >> 16) & 0xFF);
                        this.data[3] = (byte) ((value >> 8) & 0xFF);
                        this.data[4] = (byte) (value & 0xFF);
                    }
                    break;
                case DATE_TIME_S_TYPE:
                    if (obj instanceof DateTimeS) {
                        this.data = new byte[8];
                        this.data[0] = 28;
                        DateTimeS dateTimeS = (DateTimeS) obj;
                        if (dateTimeS.data != null && dateTimeS.data.length == 7) {
                            for (int i = 0; i < dateTimeS.data.length; i++) {
                                this.data[i + 1] = dateTimeS.data[i];
                            }
                        }
                    }
                    break;
                case ARRAY_TYPE:
                    if (obj instanceof ArrayList) {
                        ArrayList<Data> array = (ArrayList<Data>) obj;
                        if (array.size() > 0) {
                            int length = 0;
                            for (int i = 0; i < array.size(); i++) {
                                Data item = array.get(i);
                                if (item.data != null) {
                                    length = length + item.data.length;
                                }
                            }
                            length = length + 1 + 1;
                            this.data = new byte[length];
                            this.data[0] = 1;
                            this.data[1] = (byte) array.size();
                            int pos = 2;

                            for (int i = 0; i < array.size(); i++) {
                                Data item = array.get(i);
                                if (item.data != null) {
                                    for (int j = 0; j < item.data.length; j++) {
                                        this.data[pos++] = item.data[j];
                                    }
                                }
                            }

                        }
                    }
                    break;
            }
        }

        public Data(byte[] frame, int begin) {
            if (frame != null && begin <= frame.length - 1) {
                switch (frame[begin]) {
                    case 0:
                        this.type = Data_Type.NULL_TYPE;
                        this.obj = null;
                        this.data = new byte[1];
                        this.data[0] = 0;
                        break;
                    case 1:
                        this.type = Data_Type.ARRAY_TYPE;
                        ArrayList<Byte> bytes = new ArrayList<>();
                        bytes.add((byte)1);
                        if (begin + 1 <= frame.length - 1) {
                            int size = frame[begin + 1];
                            bytes.add((byte)size);
                            if (size > 0) {
                                ArrayList<Data> dataArrayList = new ArrayList<>(size);
                                int pos = begin + 2;
                                int i = 0;
                                for (i = 0; i < size; i++) {
                                    Data data = new Data(frame, pos);
                                    if (data.data == null) {
                                        break;
                                    }
                                    for (int j = 0; j < data.data.length; j++) {
                                        bytes.add(data.data[0]);
                                    }
                                    dataArrayList.add(data);
                                    pos = pos + data.data.length;
                                }
                                if (i == size) {
                                    this.obj = dataArrayList;
                                    this.data = new byte[bytes.size()];
                                    for (int k = 0; k < bytes.size(); k++) {
                                        this.data[k] = bytes.get(k);
                                    }
                                } else {
                                    this.data = null;
                                    this.obj = null;
                                }
                            }
                        }

                        break;
                    case 6:
                        this.type = Data_Type.DOUBLE_LONG_UNSIGNED_TYPE;
                        this.data = new byte[5];
                        this.data[0] = 6;
                        int value = 0;
                        if (begin + 4 <= frame.length - 1) {
                            value = (value | (frame[begin + 1] << 24)) & 0xFF000000;
                            this.data[1] = frame[begin + 1];
                            value = (value | (frame[begin + 2] << 16)) & 0xFFFF0000;
                            this.data[2] = frame[begin + 2];
                            value = (value | (frame[begin + 3] << 8)) & 0xFFFFFF00;
                            this.data[3] = frame[begin + 3];
                            value = (value | (frame[begin + 4])) & 0xFFFFFFFF;
                            this.data[4] = frame[begin + 4];
                        }
                        this.obj = value;
                        break;
                    case 28:
                        this.type = Data_Type.DATE_TIME_S_TYPE;
                        this.data = new byte[8];
                        this.data[0] = 28;
                        if (begin + 7 <= frame.length - 1) {
                            DateTimeS dateTimeS = new DateTimeS(DataConvertUtils.getSubByteArray(frame, begin + 1, begin + 7));
                            this.obj = dateTimeS;

                            for (int i = 0; i < 7; i++) {
                                this.data[i + 1] = frame[begin + 1 + i];
                            }
                        }
                        break;
                }
            }
        }
    }

    public static class A_RecordRow {
        public ArrayList<Data> dataArrayList;
        public byte[] data;
        public A_RecordRow(ArrayList<Data> dataArrayList) {
            this.dataArrayList = dataArrayList;
            ArrayList<Byte> bytes = new ArrayList<>();
            if (dataArrayList != null && dataArrayList.size() > 0) {
                for (int i = 0; i < dataArrayList.size(); i++) {
                    Data item = dataArrayList.get(i);
                    if (item.data != null) {
                        for (int j = 0; j < item.data.length; j++) {
                            bytes.add(item.data[0]);
                        }
                    }
                }
            }
            if (bytes.size() > 0) {
                this.data = new byte[bytes.size()];
                for (int i = 0; i < bytes.size(); i++) {
                    this.data[i] = bytes.get(i);
                }
            }
        }

        public A_RecordRow(byte[] frame, int begin) {
            if (frame != null && begin <= frame.length - 1) {
                ArrayList<Byte> bytes = new ArrayList<>();
                int size = frame[begin];
                if (size > 0) {
                    bytes.add(frame[begin]);
                    this.dataArrayList = new ArrayList<>(size);
                    int i = 0;
                    int pos = begin + 1;
                    for (i = 0; i < size; i++) {
                        Data data = new Data(frame, pos);
                        if (data.data == null) {
                            break;
                        }
                        for (int j = 0; j < data.data.length; j++) {
                            bytes.add(data.data[j]);
                        }
                        this.dataArrayList.add(data);
                        pos = pos + data.data.length;
                    }
                    if (i != size) {
                        this.dataArrayList.clear();
                        this.data = null;
                    }

                }
            }
        }
    }
}
