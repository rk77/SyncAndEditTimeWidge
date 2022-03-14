package com.rk.commonmodule.protocol.protocol698;

import android.text.TextUtils;
import android.util.Log;

import com.rk.commonmodule.protocol.protocol698.Protocol698Frame.A_ResultNormal;
import com.rk.commonmodule.protocol.protocol698.Protocol698Frame.CSD;
import com.rk.commonmodule.protocol.protocol698.Protocol698Frame.DateTimeS;
import com.rk.commonmodule.protocol.protocol698.Protocol698Frame.OAD;
import com.rk.commonmodule.protocol.protocol698.Protocol698Frame.RCSD;
import com.rk.commonmodule.protocol.protocol698.Protocol698Frame.TimeUnit;
import com.rk.commonmodule.utils.BitUtils;
import com.rk.commonmodule.utils.DataConvertUtils;

import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;

public class Protocol698Frame {

    private static final String TAG = Protocol698Frame.class.getSimpleName();

    public interface IData {
        Data toData();
    }
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

        public OAD (String oadString) {
            //byte[] oad_data = DataConvertUtils.convertHexStringToByteArray(oadString, oadString.length(), false);
            this(DataConvertUtils.convertHexStringToByteArray(oadString, oadString.length(), false));
        }

        public OAD(byte[] frame, int begin) {
            this.data = DataConvertUtils.getSubByteArray(frame, begin, begin + 3);
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

        @Override
        public String toString() {
            return DataConvertUtils.convertByteArrayToString(this.data, false).toUpperCase();
        }

    }

    public static class OMD {
        public byte[] OI;
        public byte methodId;
        public byte operationMode;
        public byte[] data;

        public OMD(byte[] data) {
            if (data != null && data.length >= 4) {
                this.data = data;
                this.OI = new byte[2];
                for (int i = 0; i < 2; i++) {
                    this.OI[i] = data[i];
                }
                this.methodId = data[2];
                this.operationMode = data[3];
            }
        }

        public OMD(byte[] IO, byte methodId, byte operationMode) {
            if (IO != null && IO.length == 2) {
                this.OI = IO;
                this.methodId = methodId;
                this.operationMode = operationMode;

                this.data = new byte[4];
                this.data[0] = IO[0];
                this.data[1] = IO[1];
                this.data[2] = methodId;
                this.data[3] = operationMode;
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
                this.length = (data[1] & 0x3F) * 256 + (data[0] & 0xFF);

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

        public byte[] tsa;

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

                tsa = new byte[this.data.length + 1];
                tsa[0] = (byte) this.data.length;
                for (int i = 0; i < this.data.length; i++) {
                    tsa[i + 1] = this.data[i];
                }

            }

        }

        public SERV_ADDR(byte[] data) {
            this.data = data;

            if (data != null && data.length >= 2) {
                int length = data[0] & 0x0F;
                if (length + 1 + 1 == data.length) {
                    this.addressLength = length + 1;
                    if ((data[0] & 0x20) == 0x00) {
                        this.hasExLogicAddress = false;
                        if ((data[0] & 0x10) == 0x00) {
                            this.logicAddress = 0;
                        } else {
                            this.logicAddress = 1;
                        }
                        this.address = new byte[this.addressLength];
                        for (int i = 0; i < address.length; i++) {
                            this.address[i] = data[1 + i];
                        }

                    } else {
                        this.hasExLogicAddress = true;
                        this.logicAddress = data[1];
                        this.address = new byte[this.addressLength - 1];
                        for (int i = 0; i < address.length; i++) {
                            this.address[i] = data[2 + i];
                        }
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

        public SERV_ADDR(byte[] data, int begin) {
            //this.data = data;
            Log.i("AX", "SERV, frame: " + DataConvertUtils.convertByteArrayToString(data, false) + ", begin: " + begin);
            if (data != null && data.length >= 2) {
                int length = (data[begin] & 0x0F) + 1;
                if (begin + length <= data.length - 1) {
                    this.addressLength = length; //地址长度
                    Log.i("AX", "SERV, address length: " + length);
                    this.data = DataConvertUtils.getSubByteArray(data, begin, begin + this.addressLength);
                    this.tsa = new byte[1 + this.data.length];
                    for (int i = 0; i < this.tsa.length; i++) {
                        if (i == 0) {
                            this.tsa[i] = (byte) this.data.length;
                            continue;
                        }
                        this.tsa[i] = this.data[i - 1];
                    }
                    if ((data[begin] & 0x20) == 0x00) {
                        this.hasExLogicAddress = false;
                        if ((data[begin] & 0x10) == 0x00) {
                            this.logicAddress = 0;
                        } else {
                            this.logicAddress = 1;
                        }
                        this.address = new byte[this.addressLength];
                        for (int i = 0; i < address.length; i++) {
                            this.address[i] = data[begin + 1 + i];
                        }
                        Log.i("AX", "SERV, address: " + DataConvertUtils.convertByteArrayToString(this.address, false));

                    } else {
                        this.hasExLogicAddress = true;
                        this.address = new byte[this.addressLength];
                        for (int i = 0; i < address.length; i++) {
                            this.address[i] = data[begin + 1 + i];
                        }
                    }

                    switch (data[begin] & 0xC0) {
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

    public static class DateTimeS implements IData {
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

        public DateTimeS(java.util.Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            this.year = cal.get(Calendar.YEAR);
            this.month = cal.get(Calendar.MONTH) + 1;
            this.day = cal.get(Calendar.DAY_OF_MONTH);
            this.hour = cal.get(Calendar.HOUR_OF_DAY);
            this.minute = cal.get(Calendar.MINUTE);
            this.second = cal.get(Calendar.SECOND);
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

        @Override
        public Data toData() {
            return new Data(Data_Type.DATE_TIME_S_TYPE, this);
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
        public final static TimeUnit DAY = new TimeUnit(3, "日");
        public final static TimeUnit MONTH = new TimeUnit(4, "月");
        public final static TimeUnit YEAR = new TimeUnit(5, "年");

        public static TimeUnit getTimeUnit(int value) {
            switch (value) {
                case 0:
                    return SECOND;
                case 1:
                    return MINUTE;
                case 2:
                    return HOUR;
                case 3:
                    return DAY;
                case 4:
                    return MONTH;
                case 5:
                    return YEAR;
                default:
                    return null;
            }
        }

    }

    public static class TI implements IData {
        public byte[] data;
        public TimeUnit unit;
        public int delayTime;

        public TI(TimeUnit unit, int delayTime) {
            this.unit = unit;
            this.delayTime = delayTime;
            data = new byte[3];
            data[0] = (byte) unit.value;
            data[1] = (byte) ((delayTime >> 8) & 0xFF);
            data[2] = (byte) ((delayTime) & 0xFF);
        }

        public TI(byte[] frame, int begin) {
            this(DataConvertUtils.getSubByteArray(frame, begin, begin + 2));
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
                        this.unit = TimeUnit.DAY;
                        break;
                    case 4:
                        this.unit = TimeUnit.MONTH;
                        break;
                    case 5:
                        this.unit = TimeUnit.YEAR;
                        break;

                }
                this.delayTime = data[1] * 256 + (data[2] & 0xFF);

            }
        }

        @Override
        public Data toData() {
            return new Data(Data_Type.TI_TYPE, this);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(delayTime).append(unit.valueName);
            return sb.toString();
        }
    }

    public static class RSD {
        public int type;
        public Object obj;
        public byte[] data;

        public RSD(int type, Object obj) {
            this.type = type;
            this.obj = obj;
            switch (type) {
                case 0:
                    data = new byte[1];
                    data[0] = (byte) (type & 0xFF);
                    break;
                case 1:
                    break;
                case 2:
                    if (obj != null && obj instanceof Selector2) {
                        this.data = new byte[1 + ((Selector2) obj).data.length];
                        this.data[0] = (byte) (type & 0xFF);
                        System.arraycopy(((Selector2) obj).data, 0, this.data, 1, ((Selector2) obj).data.length);
                    }
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
                    if (obj != null && obj instanceof Selector10) {
                        Selector10 src = ((Selector10) obj);
                        this.data = new byte[1 + src.data.length];
                        this.data[0] = (byte)(10 & 0xFF);
                        System.arraycopy(src.data, 0, this.data, 1, src.data.length);
                    }
                    break;
            }

        }

        public RSD(byte[] data, int begin) {
            if (data != null && data.length > 0) {
                switch (data[begin] & 0xFF) {
                    case 0:
                        this.type = 0;
                        break;
                    case 1:
                        break;
                    case 2:
                        this.type = 2;
                        Selector2 selector2 = new Selector2(data, begin + 1);
                        if (selector2.data != null && selector2.data.length > 0) {
                            this.obj = selector2;

                        }
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
                        if (begin + 1 + 1 <= data.length - 1) {
                            this.obj = new Selector9(data, begin + 1);
                            this.data = new byte[2];
                            this.data[0] = 9;
                            this.data[1] = data[begin + 1];
                        }
                        break;
                    case 10:
                        this.type = 10;
                        Selector10 selector10 = new Selector10(data, begin + 1);
                        if (selector10.data == null || selector10.data.length <= 0) {
                            return;
                        }
                        this.data = new byte[1 + selector10.data.length];
                        System.arraycopy(data, begin, this.data, 0, this.data.length);
                        break;
                }
            }
        }
    }

    public static class Selector2 {
        public OAD oad;
        public Data begin;
        public Data end;
        public Data interval;
        public byte[] data;
        public Selector2(OAD oad, Data begin, Data end, Data interval) {
            this.oad = oad;
            this.begin = begin;
            this.end = end;
            this.interval = interval;

            if (oad  != null && oad.data != null && begin != null && begin.data != null
                    && end != null && end.data != null && interval != null && interval.data != null) {
                this.data = new byte[oad.data.length + begin.data.length + end.data.length + interval.data.length];
                System.arraycopy(oad.data, 0, this.data, 0, oad.data.length);
                System.arraycopy(begin.data, 0, this.data, oad.data.length, begin.data.length);
                System.arraycopy(end.data, 0, this.data, oad.data.length + begin.data.length, end.data.length);
                System.arraycopy(interval.data, 0, this.data, oad.data.length + begin.data.length + end.data.length, interval.data.length);
            }

        }

        public Selector2(byte[] data, int begin) {
            if (data == null || data.length <= 0) {
                return;
            }
            if (begin + 3 > data.length - 1) {
                return;
            }
            this.oad = new OAD(DataConvertUtils.getSubByteArray(data, begin, begin + 3));

            Data beginData = new Data(data, begin + 4);
            if (beginData.data == null || beginData.data.length <= 0) {
                return;
            }
            this.begin = beginData;

            Data endData = new Data(data, begin + 4 + beginData.data.length);
            if (endData.data == null || endData.data.length <= 0) {
                return;
            }
            this.end = endData;

            Data intervalData = new Data(data, begin + 4 + beginData.data.length + end.data.length);
            if (intervalData.data == null || intervalData.data.length <= 0) {
                return;
            }
            this.interval = intervalData;

            this.data = new byte[4 + beginData.data.length + endData.data.length + intervalData.data.length];
            this.data = DataConvertUtils.getSubByteArray(data, begin, begin + this.data.length - 1);
        }
    }

    public static class Selector9 {
        public int last;
        public byte data;
        public Selector9(int last) {
            this.last = last;
            data = (byte) (last & 0xFF);

        }

        public Selector9(byte[] data, int begin) {
            this.data = data[begin];
            this.last = data[begin] & 0xFF;
        }
    }

    public static class Selector10 {
        public int last;
        public MS ms;
        public byte[] data;
        public Selector10(int last, MS ms) {
            this.ms = ms;
            this.last = last;
            if (ms.data != null) {
                this.data = new byte[1 + ms.data.length];
                this.data[0] = (byte) last;
                System.arraycopy(ms.data, 0, this.data, 1, ms.data.length);
            }

        }

        public Selector10(byte[] data, int begin) {
            if (data == null || data.length <= 0 || begin > data.length - 1) {
                return;
            }
            this.last = data[begin] & 0xFF;
            MS ms = new MS(data, begin + 1);
            if (ms.data == null || ms.data.length <= 0) {
                return;
            }
            this.ms = ms;
            this.data = new byte[1 + ms.data.length];
            System.arraycopy(data, begin, this.data, 0, this.data.length);
        }
    }

    public static class NewRCSD {
        public byte[] data;
        public ArrayList<CSD> csds;
        public NewRCSD(ArrayList<CSD> csdList) {
            if (csdList == null || csdList.size() <= 0) {
                return;
            }
            this.csds = csdList;
            int size = 1;
            for (int i = 0; i < csdList.size(); i++) {
                CSD csd = csdList.get(i);
                if (csd != null && csd.data != null && csd.data.length > 0) {
                    size = size + csd.data.length;
                }
            }
            this.data = new byte[size];
            int pos = 0;
            this.data[pos] = (byte) (csdList.size() & 0xFF);
            pos = pos + 1;
            for (int i = 0; i < csdList.size(); i++) {
                CSD csd = csdList.get(i);
                if (csd != null && csd.data != null && csd.data.length > 0) {
                    System.arraycopy(csd.data, 0, this.data, pos, csd.data.length);
                    pos = pos + csd.data.length;
                }
            }
        }

        public NewRCSD(byte[] frame, int begin) {
            try {
                int pos = begin;
                int length = frame[pos];
                pos = pos + 1;
                if (length >= 128) {
                    int lengthAreaLength = length & 0x7F;
                    int lengthAreaBegin = pos;
                    int lengthAreaEnd = pos + lengthAreaLength - 1;
                    int size = 0;
                    for (int i = 0; i <= lengthAreaEnd - lengthAreaBegin; i++) {
                         size = size + (int)(frame[pos] * Math.pow(256, i));
                         pos = pos + 1;
                    }
                    for (int i = 0; i < size; i++) {
                        CSD csd = new CSD(frame, pos);
                        if (csd.data != null && csd.data.length > 0) {
                            this.csds.add(csd);
                            pos = pos + csd.data.length;
                        } else {
                            break;
                        }
                    }
                    this.data = DataConvertUtils.getSubByteArray(frame, begin, pos - 1);

                } else {
                    this.csds = new ArrayList<CSD>();
                    for (int i = 0; i < length; i++) {
                        CSD csd = new CSD(frame, pos);
                        if (csd.data != null && csd.data.length > 0) {
                            this.csds.add(csd);
                            pos = pos + csd.data.length;
                        } else {
                            break;
                        }
                    }
                    this.data = DataConvertUtils.getSubByteArray(frame, begin, pos - 1);
                }

            } catch (Exception err) {

            }
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
                            bytes.add(oadItem.data[j]);
                        }
                    }
                }
            }

            this.data = new byte[bytes.size()];
            for (int i = 0; i < data.length; i++) {
                this.data[i] = bytes.get(i);
            }
        }

        public ROAD(byte[] data, int begin) {
            //this.data = data;

            if (data != null && begin + 3 <= data.length - 1) {
                this.oad = new OAD(DataConvertUtils.getSubByteArray(data, begin, begin + 3));
                int road_length = data[begin + 4];

                Log.i(TAG, "ROAD, road_length: " + road_length);

                if (road_length > 0) {
                    for (int i = 1; i <= road_length; i++) {
                        this.oadArrayList.add(new OAD(DataConvertUtils.getSubByteArray(data, i * 4 + 1 + begin, i * 4 + 4 + begin)));
                    }
                }
                this.data = new byte[4 + 1 + 4 * road_length];
                this.data = DataConvertUtils.getSubByteArray(data, begin, begin + this.data.length - 1);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(oad.toString()).append(":[");
            for (int i = 0; i < oadArrayList.size(); i++) {
                sb.append(oadArrayList.get(i).toString());
                if (i < oadArrayList.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
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
                    if (object != null && object instanceof OAD) {
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

        public CSD(byte[] data, int begin) {
            if (data != null && data.length > 0) {
                //this.data = data;
                this.type = data[begin];
                switch (this.type) {
                    case 0:
                        if (data.length >= 5) {
                            this.object = new OAD(DataConvertUtils.getSubByteArray(data, begin + 1, begin + 4));
                            if (((OAD) this.object).data == null) {
                                return;
                            }
                            this.data = new byte[1 + ((OAD) this.object).data.length];
                            this.data = DataConvertUtils.getSubByteArray(data, begin, begin + this.data.length - 1);
                        }
                        break;
                    case 1:
                        if (data.length >= 5) {
                            //this.object = new ROAD(DataConvertUtils.getSubByteArray(data, 1, data.length - 1));
                            ROAD road = new ROAD(data, begin + 1);
                            if (road.data == null || road.data.length <= 0) {
                                return;
                            }
                            this.object = road;
                            this.data = new byte[1 + road.data.length];
                            this.data = DataConvertUtils.getSubByteArray(data, begin, begin + this.data.length -1);
                        }
                        break;
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (type == 0 && object instanceof OAD) {
                sb.append(((OAD)object).toString());
            } else if (type == 0 && object instanceof ROAD) {
                sb.append(((ROAD)object).toString());
            }
            return sb.toString();
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
            } else {
                this.data = new byte[1];
                this.data[0] = 0;
            }

        }

        public RCSD(byte[] frame, int begin) {
            if (frame != null && frame.length > 0 && begin >= 0 && begin <= frame.length - 1) {
                if (frame[begin] == 0) {
                    this.data = new byte[1];
                    this.data[0] = 0;
                    this.length = 0;
                } else {
                    int cnt = frame[begin];
                    int pos = begin + 1;
                    for (int i = 0; i < cnt; i++) {
                        //int csdLength = calcCsdLength(frame, pos);
                        CSD csd = new CSD(frame, pos);
                        if (csd.data != null && csd.data.length > 0) {
                            this.csdArrayList.add(csd);
                            pos = csd.data.length + pos;
                        } else {
                            break;
                        }
                    }
                    if (csdArrayList.size() == cnt) {
                        this.data = DataConvertUtils.getSubByteArray(frame, begin, pos - 1);
                        this.length = cnt;
                    } else {
                        this.data = null;
                        this.csdArrayList.clear();
                        this.length = 0;
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

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < csdArrayList.size(); i++) {
                sb.append(csdArrayList.get(i));
                if (i < csdArrayList.size() - 1) {
                    sb.append(",");
                }
            }
            return sb.toString();
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

        public GetRecord(byte[] frame, int begin) {
            try {
                int pos = begin;
                this.oad = new OAD(frame, begin);
                pos = pos + this.oad.data.length;
                this.rsd = new RSD(frame, pos);
                pos = pos + this.rsd.data.length;
                this.rcsd = new RCSD(frame, pos);
                pos = pos + this.rcsd.data.length;
                this.data = DataConvertUtils.getSubByteArray(frame, begin, pos - 1);

            } catch (Exception err) {

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
                    if (obj != null && obj instanceof ArrayList) {
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

                        } else {
                            this.data = new byte[2];
                            this.data[0] = (byte) 1;
                            this.data[1] = (byte) 0;
                        }
                    } else {
                        this.data = new byte[2];
                        this.data[0] = (byte) 1;
                        this.data[1] = (byte) 0;
                    }
                    break;
                case DOUBLE_LONG_TYPE:
                    if (obj instanceof Integer) {
                        this.data = new byte[1 + 4];
                        this.data[0] = 5;
                        int value = (int) obj;
                        this.data[1] = (byte) ((value >> 24) & 0xFF);
                        this.data[2] = (byte) ((value >> 16) & 0xFF);
                        this.data[3] = (byte) ((value >> 8) & 0xFF);
                        this.data[4] = (byte) (value & 0xFF);
                    }
                    break;
                case LONG_UNSIGNED_TYPE:
                    if (obj instanceof Integer) {
                        this.data = new byte[1 + 2];
                        this.data[0] = 18;
                        int value = (int) obj;
                        this.data[1] = (byte) ((value >> 8) & 0xFF);
                        this.data[2] = (byte) (value & 0xFF);
                    }
                    break;
                case VISIBLE_STRING_TYPE:
                    if (obj != null && obj instanceof String) {
                        String s = (String) obj;
                        this.data = new byte[2 + s.length()];
                        this.data[0] = (byte) 10;
                        this.data[1] = (byte) (s.length());
                        for (int i = 0; i < s.length(); i++) {
                            this.data[i + 2] =(byte)(s.charAt(i));
                        }
                    }
                    if (obj == null) {
                        this.data = new byte[6];
                        this.data[0] = (byte) 10;
                        this.data[1] = (byte) 4;
                        for (int i = 0; i < 4; i++) {
                            this.data[2 + i] = (byte)0;
                        }
                    }
                    if (((String)obj).equals("")) {
                        this.data = new byte[2];
                        this.data[0] = (byte) 10;
                        this.data[1] = (byte) 0;
                    }
                    break;
                case STRUCTURE_TYPE:
                    if (obj != null && obj instanceof ArrayList) {
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
                            this.data[0] = 2;
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
                case ENUM_TYPE:
                    if (obj instanceof Byte) {
                        this.data = new byte[2];
                        this.data[0] = (byte) 22;
                        this.data[1] = (byte) obj;
                    }
                    break;
                case BIT_STRING_TYPE:
                    if (obj instanceof String) {
                        String bitString = (String) obj;
                        int length = bitString.length();
                        if (length <= 127) {
                            int size = (length + 7) / 8;
                            this.data = new byte[1 + 1 + size];
                            this.data[0] = (byte) 4;
                            this.data[1] = (byte) length;

                            for (int i = 0; i < size; i++) {
                                this.data[2 + i] = (byte) 0xFF;
                                for (int j = i * 8; j < i * 8 + 8; j++) {
                                    char bit = '0';
                                    if (j <= length - 1) {
                                        bit = bitString.charAt(j);
                                    }
                                    switch (bit) {
                                        case '0':
                                            this.data[2 + i] = BitUtils.setBitValue(this.data[2 + i], 7 - (j % 8), (byte) 0);
                                            break;
                                        case '1':
                                            this.data[2 + i] = BitUtils.setBitValue(this.data[2 + i], 7 - (j % 8), (byte) 1);
                                            break;
                                        default:
                                            this.data[2 + i] = BitUtils.setBitValue(this.data[2 + i], 7 - (j % 8), (byte) 0);
                                    }
                                }
                            }

                        } else {
                            //TODO:
                        }

                    }
                    break;
                case OCTET_STRING_TYPE:
                    if (obj != null && obj instanceof byte[]) {
                        try {
                            byte[] d = (byte[]) obj;
                            int size = d.length;
                            if (size <= 127) {
                                this.data = new byte[1 + 1 + size];
                                this.data[0] = (byte) 9;
                                this.data[1] = (byte) size;
                                for (int i = 0; i < size; i++) {
                                    this.data[2 + i] = d[i];
                                }
                            } else {
                                //TODO:
                                int length_area_length = 1;
                                int s = size;
                                while (s / 256 >= 1) {
                                    s = s / 256;
                                    length_area_length++;
                                }
                                this.data = new byte[1 + 1 + length_area_length + size];
                                this.data[0] = (byte) 9;
                                this.data[1] = (byte) ((length_area_length | 0x80) & 0xFF);
                                int j = length_area_length;
                                for (int i = 2; i <= 2 + length_area_length - 1; i++) {
                                    this.data[i] = (byte) ((size >> ((j - 1) * 8)) & 0xFF);
                                    j--;
                                }
                                System.arraycopy(d, 0, this.data, 2 + length_area_length, size);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "OCTET_STRING_TYPE, error: " + e.getMessage());
                        }
                    } else if (obj == null) {
                        this.data = new byte[2];
                        this.data[0] = (byte) 9;
                        this.data[1] = (byte) 0;
                    }
                    break;
                case UNSIGNED_TYPE:
                    if (obj instanceof Byte) {
                        this.data = new byte[2];
                        this.data[0] = (byte) 17;
                        this.data[1] = (byte) obj;
                    }
                    break;
                case TSA_TYPE:
                    if (obj != null && obj instanceof byte[]) {
                        byte[] lengh_and_addr = (byte[]) obj;
                        if (lengh_and_addr.length <= 0) {
                            return;
                        }
                        this.data = new byte[1 + lengh_and_addr.length];
                        for (int i = 0; i < this.data.length; i++) {
                            if (i == 0) {
                                this.data[i] = (byte) 85;
                                continue;
                            }
                            this.data[i] = lengh_and_addr[i - 1];
                        }
                    }
                    break;
                case OAD_TYPE:
                    if (obj != null && obj instanceof OAD) {
                        OAD oad = (OAD) obj;
                        if (oad.data == null || oad.data.length <= 0) {
                            return;
                        }
                        this.data = new byte[1 + oad.data.length];
                        for (int i = 0; i < this.data.length; i++) {
                            if (i == 0) {
                                this.data[i] = (byte) 81;
                                continue;
                            }
                            this.data[i] = oad.data[i - 1];
                        }
                    }
                    break;
                case COMDCB_TYPE:
                    if (obj != null && obj instanceof COMDCB) {
                        COMDCB comdcb = (COMDCB) obj;
                        if (comdcb.data == null || comdcb.data.length <= 0) {
                            return;
                        }
                        this.data = new byte[1 + comdcb.data.length];
                        this.data[0] = (byte) 95;
                        System.arraycopy(comdcb.data, 0, this.data, 1, comdcb.data.length);
                    }
                    break;
                case DATE_TYPE:
                    if (obj != null && obj instanceof Date) {
                        Date date = (Date) obj;
                        if (date.data == null || date.data.length <= 0) {
                            return;
                        }
                        this.data = new byte[1 + date.data.length];
                        this.data[0] = (byte) 26;
                        System.arraycopy(date.data, 0, this.data, 1, date.data.length);
                    }
                    break;
                case LONG_TYPE:
                    if (obj instanceof Short) {
                        short value = (short) obj;
                        this.data = new byte[1+ 2];
                        this.data[0] = (byte) 16;
                        this.data[1] = (byte) ((value>>8) | 0xFF);
                        this.data[2] = (byte) (value & 0xFF);
                    }
                case TI_TYPE:
                    if (obj instanceof TI) {
                        TI ti = (TI) obj;
                        this.data = new byte[1 + ti.data.length];
                        this.data[0] = 84;
                        System.arraycopy(ti.data, 0, this.data, 1, ti.data.length);
                    }
            }
        }

        public Data(byte[] frame, int begin) {
            if (frame != null && begin <= frame.length - 1) {
                switch (frame[begin] & 0xFF) {
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
                                        bytes.add(data.data[j]);
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
                            } else {
                                this.data = new byte[bytes.size()];
                                for (int k = 0; k < bytes.size(); k++) {
                                    this.data[k] = bytes.get(k);
                                }
                            }
                        }
                        break;
                    case 2:
                        this.type = Data_Type.STRUCTURE_TYPE;
                        ArrayList<Byte> bytes2 = new ArrayList<>();
                        bytes2.add((byte)2);
                        if (begin + 1 <= frame.length - 1) {
                            int size = frame[begin + 1];
                            Log.i(TAG, "Data, STRUCTURE_TYPE, size: " + size);
                            bytes2.add((byte)size);
                            if (size > 0) {
                                ArrayList<Data> dataArrayList = new ArrayList<>(size);
                                int pos = begin + 2;
                                int i = 0;
                                for (i = 0; i < size; i++) {
                                    Data new_data = new Data(frame, pos);
                                    if (new_data.data == null) {
                                        Log.i(TAG, "Data, STRUCTURE_TYPE, break, i: " + i);
                                        break;
                                    }
                                    Log.i(TAG, "Data, STRUCTURE_TYPE, data type:" + new_data.type
                                            + ", data: " + DataConvertUtils.convertByteArrayToString(new_data.data, false));
                                    for (int j = 0; j < new_data.data.length; j++) {
                                        bytes2.add(new_data.data[j]);
                                    }
                                    dataArrayList.add(new_data);
                                    pos = pos + new_data.data.length;
                                }
                                if (i == size) {
                                    this.obj = dataArrayList;
                                    this.data = new byte[bytes2.size()];
                                    for (int k = 0; k < bytes2.size(); k++) {
                                        this.data[k] = bytes2.get(k);
                                    }
                                } else {
                                    this.data = null;
                                    this.obj = null;
                                }
                            }
                        }
                        break;
                    case 4:
                        this.type = Data_Type.BIT_STRING_TYPE;
                        if (begin + 1 <= frame.length - 1) {
                            if ((frame[begin + 1] & 0x80) == 0) {
                                int length = frame[begin + 1] & 0xFF;
                                int size = (length + 7) / 8;
                                this.data = new byte[1 + 1 + size];
                                if (frame.length- begin >= 1 + 1 + size) {
                                    System.arraycopy(frame, begin, this.data, 0, this.data.length);
                                    StringBuilder bitString = new StringBuilder();
                                    for (int i = 0; i < size; i++) {
                                        for (int j = 0; j < 8; j++) {
                                            bitString.append(BitUtils.getBitValue(this.data[2 + i], 7 - j));
                                        }
                                    }
                                    this.obj = bitString.toString();
                                }
                            } else {
                                //TODO:
                            }

                        }
                        break;
                    case 5:
                        this.type = Data_Type.DOUBLE_LONG_TYPE;
                        this.data = new byte[5];
                        this.data[0] = 5;
                        int value1 = 0;
                        if (begin + 4 <= frame.length - 1) {
                            value1 = (value1 | ((frame[begin + 1] & 0xFF) << 24)) & 0xFF000000;
                            this.data[1] = frame[begin + 1];
                            value1 = (value1 | ((frame[begin + 2] & 0xFF) << 16)) & 0xFFFF0000;
                            this.data[2] = frame[begin + 2];
                            value1 = (value1 | ((frame[begin + 3] & 0xFF) << 8)) & 0xFFFFFF00;
                            this.data[3] = frame[begin + 3];
                            value1 = (value1 | (frame[begin + 4] & 0xFF)) & 0xFFFFFFFF;
                            this.data[4] = frame[begin + 4];
                        } else {
                            this.data = null;
                        }
                        this.obj = value1;
                        break;
                    case 6:
                        this.type = Data_Type.DOUBLE_LONG_UNSIGNED_TYPE;
                        this.data = new byte[5];
                        this.data[0] = 6;
                        int value = 0;
                        if (begin + 4 <= frame.length - 1) {
                            value = (value | ((frame[begin + 1] & 0xFF) << 24)) & 0xFF000000;
                            this.data[1] = frame[begin + 1];
                            value = (value | ((frame[begin + 2] & 0xFF) << 16)) & 0xFFFF0000;
                            this.data[2] = frame[begin + 2];
                            value = (value | ((frame[begin + 3] & 0xFF) << 8)) & 0xFFFFFF00;
                            this.data[3] = frame[begin + 3];
                            value = (value | (frame[begin + 4] & 0xFF)) & 0xFFFFFFFF;
                            this.data[4] = frame[begin + 4];
                        } else {
                            this.data = null;
                        }
                        this.obj = value;
                        break;
                    case 9:
                        this.type = Data_Type.OCTET_STRING_TYPE;
                        if (begin + 1 <= frame.length - 1) {
                            if ((frame[begin + 1] & 0x80) == 0x00) {
                                int size = frame[begin + 1];
                                if (begin + 1 + size <= frame.length - 1) {
                                    this.data = new byte[1 + 1 + size];
                                    this.data[0] = (byte) 9;
                                    this.data[1] = (byte) size;
                                    byte[] o = new byte[size];
                                    for (int i = 0; i < size; i++) {
                                        data[2 + i] = frame[begin + 1 + 1 + i];
                                        o[i] = frame[begin + 1 + 1 + i];
                                    }
                                    this.obj = o;
                                }
                            } else {
                                //TODO:
                                int length_area_length = frame[9] & 0x7F;
                                if (begin + 1 + length_area_length <= frame.length - 1) {
                                    int lenght = frame[begin + 1 + 1];
                                    for (int i = begin + 1 + 1 + 1; i <= begin + 1 + length_area_length; i++) {
                                        lenght = lenght * 256 + frame[i];
                                    }
                                    this.data = new byte[1 + 1 + length_area_length + lenght];
                                    byte[] o = new byte[lenght];
                                    if (frame.length - 1 - begin + 1 >= this.data.length) {
                                        System.arraycopy(frame, begin, this.data, 0, this.data.length);
                                        System.arraycopy(frame, begin + 1 + length_area_length + 1, o, 0, o.length);
                                        this.obj = o;
                                    }
                                }
                            }
                        }
                        break;
                    case 10:
                        this.type = Data_Type.VISIBLE_STRING_TYPE;
                        if (begin + 1 <= frame.length - 1) {
                            int size = frame[begin + 1];
                            this.data = new byte[1 + 1 + size];
                            this.data[0] = (byte) 10;
                            this.data[1] = (byte) size;
                            for (int i = 0; i < size; i++) {
                                if (begin + 2 + i <= frame.length - 1) {
                                    this.data[2 + i] = frame[begin + 2 + i];
                                } else {
                                    this.data = null;
                                    break;
                                }
                            }
                            if (this.data != null) {
                                String s = DataConvertUtils.getByteArray2AsciiString(
                                        DataConvertUtils.getSubByteArray(this.data, 2, this.data.length - 1));
                                this.obj = s;
                            }
                        }
                        break;
                    case 16:
                        this.type = Data_Type.LONG_TYPE;
                        if (begin + 2 <= frame.length - 1) {
                            this.data = new byte[3];
                            this.data[0] = (byte) 16;
                            this.data[1] = frame[begin + 1];
                            this.data[2] = frame[begin + 2];
                            short o = 0;
                            o = (short) ((frame[begin + 1] << 8) & 0xFF00);
                            o = (short) (frame[begin + 2] & 0x00FF | o);
                            this.obj = o;
                        }
                        break;
                    case 17:
                        this.type = Data_Type.UNSIGNED_TYPE;
                        if (begin + 1 <= frame.length - 1) {
                            this.data = new byte[2];
                            this.data[0] = (byte) 17;
                            this.data[1] = frame[begin + 1];
                            byte o = frame[begin + 1];
                            this.obj = o;
                        }
                        break;
                    case 18:
                        this.type = Data_Type.LONG_UNSIGNED_TYPE;
                        this.data = new byte[3];
                        this.data[0] = (byte) 18;
                        int long_unsigned_value = 0;
                        if (begin + 2 <= frame.length - 1) {
                            long_unsigned_value = (long_unsigned_value | ((frame[begin + 1] & 0xFF) << 8)) & 0xFF00;
                            this.data[1] = frame[begin + 1];
                            long_unsigned_value = (long_unsigned_value | (frame[begin + 2] & 0xFF)) & 0xFFFF;
                            this.data[2] = frame[begin + 2];
                        } else {
                            this.data = null;
                        }
                        this.obj = long_unsigned_value;
                        break;
                    case 22:
                        this.type = Data_Type.ENUM_TYPE;
                        if (begin + 1 <= frame.length - 1) {
                            this.data = new byte[2];
                            this.data[0] = (byte) 22;
                            this.data[1] = frame[begin + 1];
                            this.obj = frame[begin + 1];
                        }
                        break;
                    case 26:
                        this.type = Data_Type.DATE_TYPE;
                        if (begin + 1 <= frame.length - 1) {
                            Date date = new Date(frame, begin + 1);
                            if (date.data != null && date.data.length > 0) {
                                this.obj = date;
                                this.data = new byte[1 + date.data.length];
                                this.data[0] = (byte) 26;
                                System.arraycopy(date.data, 0, this.data, 1, date.data.length);
                            }
                        }
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
                        } else {
                            this.data = null;
                        }
                        break;
                    case 81:
                        this.type = Data_Type.OAD_TYPE;
                        OAD oad = new OAD(DataConvertUtils.getSubByteArray(frame, begin + 1, begin + 1 + 3));
                        if (oad.data != null && oad.data.length == 4) {
                            this.obj = oad;
                            this.data = new byte[1 + oad.data.length];
                            for (int i = 0; i < this.data.length; i++) {
                                if (i == 0) {
                                    this.data[i] = (byte) 81;
                                    continue;
                                }
                                this.data[i] = oad.data[i - 1];
                            }
                        }
                        break;
                    case 84:
                        this.type = Data_Type.TI_TYPE;
                        int pos = begin + 1;
                        TI ti = new TI(frame, pos);
                        pos = pos + ti.data.length;
                        this.obj = ti;
                        this.data = DataConvertUtils.getSubByteArray(frame, begin, pos - 1);
                        break;
                    case 85:
                        this.type = Data_Type.TSA_TYPE;
                        if (begin + 1 <= frame.length - 1) {
                            int length = frame[begin + 1];
                            this.data = new byte[1 + 1 + length];
                            this.data[0] = (byte) 85;
                            this.data[1] = (byte) length;

                            for (int i = 0; i < length; i++) {
                                this.data[i + 2] = frame[begin + 1 + 1 + i];
                            }

                            this.obj = new byte[1 + length];
                            for (int i = 0; i < ((byte[]) this.obj).length; i++) {
                                ((byte[])this.obj)[i] = frame[begin + 1 + i];
                            }
                        }
                        /*
                        SERV_ADDR addr = new SERV_ADDR(frame, begin + 1 + 1);
                        if (addr.data != null && addr.data.length > 0 && addr.address != null && addr.address.length > 0) {
                            this.obj = addr;
                            this.data = new byte[1 + addr.data.length + 1];
                            for (int i = 0; i < this.data.length; i++) {
                                if (i == 0) {
                                    this.data[i] = (byte) 85;
                                    continue;
                                }

                                if (i == 1) {
                                    this.data[i] = (byte) this.data.length;
                                    continue;
                                }
                                this.data[i] = addr.data[i - 2];
                            }
                        }
                        */
                        break;
                    case (byte)95:
                        this.type = Data_Type.COMDCB_TYPE;
                        if (begin + 1 <= frame.length - 1) {
                            COMDCB comdcb = new COMDCB(frame, begin + 1);
                            if (comdcb.data != null) {
                                this.data = new byte[1 + 5];
                                this.data[0] = (byte) 95;
                                System.arraycopy(frame, begin + 1, this.data, 1, 5);
                            }
                        }
                        break;
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            switch (type) {
                case NULL_TYPE:
                    sb.append("NULL");
                    break;
                case DOUBLE_LONG_UNSIGNED_TYPE:
                    sb.append(obj.toString());
                    break;
                case DATE_TIME_S_TYPE:
                    if (obj instanceof DateTimeS) {
                        sb.append(((DateTimeS)obj).toString());
                    }
                    break;
                case ARRAY_TYPE:
                    if (obj != null && obj instanceof ArrayList) {
                        sb.append("{");
                        ArrayList<Data> array = (ArrayList<Data>) obj;
                        if (array != null) {
                            for (int i = 0; i < array.size(); i++) {
                                sb.append(array.get(i).toString());
                                if (i < array.size() - 1) {
                                    sb.append(",");
                                }
                            }
                        }
                        sb.append("}");
                    } else {
                        sb.append("{").append("}");
                    }
                    break;
                case DOUBLE_LONG_TYPE:
                    if (obj instanceof Integer) {
                        sb.append((obj).toString());
                    }
                    break;
                case LONG_UNSIGNED_TYPE:
                    if (obj instanceof Integer) {
                        sb.append((obj).toString());
                    }
                    break;
                case VISIBLE_STRING_TYPE:
                    if (obj != null && obj instanceof String) {
                        sb.append(obj);
                    }
                    break;
                case STRUCTURE_TYPE:
                    if (obj != null && obj instanceof ArrayList) {
                        sb.append("{");
                        ArrayList<Data> array = (ArrayList<Data>) obj;
                        if (array != null) {
                            for (int i = 0; i < array.size(); i++) {
                                sb.append(array.get(i).toString());
                                if (i < array.size() - 1) {
                                    sb.append(",");
                                }
                            }
                        }
                        sb.append("}");
                    } else {
                        sb.append("{").append("}");
                    }
                    break;
                case ENUM_TYPE:
                    if (obj instanceof Byte) {
                        sb.append(obj.toString());
                    }
                    break;
                case BIT_STRING_TYPE:
                    if (obj instanceof String) {
                        sb.append(obj);
                    }
                    break;
                case OCTET_STRING_TYPE:
                    if (obj != null && obj instanceof byte[]) {
                        byte[] d = (byte[])obj;
                        sb.append(DataConvertUtils.convertByteArrayToString(d, false));

                    }
                    break;
                case UNSIGNED_TYPE:
                    if (obj instanceof Byte) {
                        sb.append(obj.toString());
                    }
                    break;
                case TSA_TYPE:
                    if (obj != null && obj instanceof byte[]) {
                        byte[] d = (byte[])obj;
                        sb.append(DataConvertUtils.convertByteArrayToString(d, 1, d.length - 1, true));
                    }
                    break;
                case OAD_TYPE:
                    if (obj != null && obj instanceof OAD) {
                        sb.append(((OAD) obj).toString());
                    }
                    break;
                case COMDCB_TYPE:
                    if (obj != null && obj instanceof COMDCB) {
                        COMDCB comdcb = (COMDCB) obj;
                        sb.append(comdcb.toString());
                    }
                    break;
                case DATE_TYPE:
                    if (obj != null && obj instanceof Date) {
                        Date date = (Date) obj;
                        sb.append(date.toString());
                    }
                    break;
                case LONG_TYPE:
                    if (obj instanceof Short) {
                        sb.append(obj.toString());
                    }
                case TI_TYPE:
                    if (obj instanceof TI) {
                        TI ti = (TI) obj;
                        sb.append(ti.toString());
                    }
            }
            return sb.toString();
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

        public A_RecordRow(byte[] frame, int begin, int columeSize) {
            if (frame != null && begin <= frame.length - 1) {
                ArrayList<Byte> bytes = new ArrayList<>();
                int size = columeSize;
                Log.i(TAG, "A_RecordRow, colume size: " + size);
                if (size > 0) {
                    this.dataArrayList = new ArrayList<>(size);
                    int i = 0;
                    int pos = begin;
                    for (i = 0; i < size; i++) {
                        Data data = new Data(frame, pos);
                        if (data.data == null) {
                            Log.i(TAG, "A_RecordRow, data null, i: " + i);
                            break;
                        }
                        for (int j = 0; j < data.data.length; j++) {
                            bytes.add(data.data[j]);
                        }
                        Log.i(TAG, "A_RecordRow, data[" + i + "]: " + DataConvertUtils.convertByteArrayToString(data.data, false));
                        this.dataArrayList.add(data);
                        pos = pos + data.data.length;
                    }
                    if (i != size) {
                        Log.i(TAG, "A_RecordRow, clear data, i: " + i);
                        this.dataArrayList.clear();
                        this.data = null;
                    } else {
                        this.data = new byte[bytes.size()];
                        for (int j = 0; j < this.data.length; j++) {
                            this.data[j] = bytes.get(j);
                        }
                    }
                }
            }
        }
    }

    public static class SequenceOfA_RecordRow {
        public int length = 0;
        public ArrayList<A_RecordRow> a_recordRows;
        public byte[] data;

        public SequenceOfA_RecordRow(ArrayList<A_RecordRow> a_recordRows) {
            if (a_recordRows != null && a_recordRows.size() > 0) {
                this.length = a_recordRows.size();
                this.a_recordRows = a_recordRows;

                ArrayList<Byte> byteArrayList = new ArrayList<>();
                int i = 0;
                for (i = 0; i < a_recordRows.size(); i++) {
                    A_RecordRow item = a_recordRows.get(i);
                    if (item == null || item.data == null || item.data.length <= 0) {
                        break;
                    }
                    for (int j = 0; j < item.data.length; j++) {
                        byteArrayList.add(item.data[j]);
                    }
                }
                if (i == a_recordRows.size()) {
                    data = new byte[byteArrayList.size()];
                    for (int k = 0; k < data.length; k++) {
                        data[k] = byteArrayList.get(k);
                    }

                } else {
                    this.length = 0;
                    this.a_recordRows = null;
                    data = null;
                }

            }
        }

        public SequenceOfA_RecordRow(byte[] frame, int begin, int columeSize) {
            if (frame == null || begin > frame.length - 1) {
                return;
            }
            int size = frame[begin];
            this.a_recordRows = new ArrayList<>();
            int pos = begin + 1;
            int i = 0;
            for (i = 0; i < size; i++) {
                A_RecordRow a_recordRow = new A_RecordRow(frame, pos, columeSize);
                if (a_recordRow.data == null || a_recordRow.data.length <= 0) {
                    break;
                }
                this.a_recordRows.add(a_recordRow);
                pos = pos + a_recordRow.data.length;
            }

            if (i == size) {
                this.length = size;
                this.data = DataConvertUtils.getSubByteArray(frame, begin + 1, pos - 1);
            } else {
                this.length = 0;
                this.a_recordRows = null;
                this.data = null;
            }

        }
    }

    public enum Get_Result_Type {
        DAR_TYPE, DATA_TYPE,
    }
    public static class Get_Result {
        public Get_Result_Type type;
        public Object object;
        public byte[] data;

        public Get_Result(Get_Result_Type type, Object object) {
            this.type = type;
            this.object = object;
            switch (type) {
                case DAR_TYPE:
                    data = new byte[2];
                    data[0] = 0;
                    data[1] = (byte) ((int) object);
                    break;
                case DATA_TYPE:
                    if (object instanceof Data) {
                        Data data1 = (Data) object;
                        if (data1 != null && data1.data != null) {
                            data = new byte[1 + data1.data.length];
                            data[0] = (byte) 1;
                            for (int i = 1; i < data.length; i++) {
                                data[1] = data1.data[i - 1];
                            }
                        }
                    }
                    break;
            }
        }

        public Get_Result(byte[] frame, int begin) {
            if (frame != null && begin <= frame.length - 1) {
                switch (frame[begin] & 0xFF) {
                    case 0:
                        this.type = Get_Result_Type.DAR_TYPE;
                        if (begin + 1 <= frame.length - 1) {
                            this.object = (int) frame[begin + 1];
                            data = new byte[2];
                            data[0] = frame[begin];
                            data[1] = frame[begin + 1];
                        }
                        break;
                    case 1:
                        this.type = Get_Result_Type.DATA_TYPE;
                        Data dat1 = new Data(frame, begin + 1);
                        if (dat1 != null && dat1.data != null) {
                            this.object = dat1;
                            data = new byte[1 + dat1.data.length];
                            data[0] = frame[begin];
                            for (int i = 1; i < data.length; i++) {
                                data[i] = dat1.data[i - 1];
                            }
                        }
                        break;
                }
            }
        }

    }

    public static class A_ResultNormal {
        public OAD oad;
        public Get_Result getResult;
        public byte[] data;

        public A_ResultNormal(OAD oad, Get_Result get_result) {
            this.oad = oad;
            this.getResult = get_result;

            if (oad != null && oad.data != null && get_result != null && get_result.data != null) {
                this.data = new byte[oad.data.length + get_result.data.length];
                System.arraycopy(oad.data, 0, this.data, 0, oad.data.length);
                System.arraycopy(get_result.data, 0, this.data, oad.data.length, get_result.data.length);
            }
        }

        public A_ResultNormal(byte[] frame, int begin) {
            if (frame != null && begin + 3 <= frame.length - 1) {
                this.oad = new OAD(DataConvertUtils.getSubByteArray(frame, begin, begin + 3));

                this.getResult = new Get_Result(frame, begin + 4);
                if (this.getResult.data == null) {
                    this.getResult = null;
                    return;
                }

                if (oad != null && oad.data != null && getResult != null && getResult.data != null) {
                    this.data = new byte[oad.data.length + getResult.data.length];
                    System.arraycopy(oad.data, 0, this.data, 0, oad.data.length);
                    System.arraycopy(getResult.data, 0, this.data, oad.data.length, getResult.data.length);
                }
            }
        }
    }

    public static class GetResponseNormal {
        public PIID_ACD piid_acd;
        public A_ResultNormal a_resultNormal;
        public byte[] data;

        public GetResponseNormal(byte[] frame, int begin) {
            if (frame == null || frame.length <= 0) {
                return;
            }
            if (begin <= frame.length - 1) {
                piid_acd = new PIID_ACD(frame[begin]);
            }
            if (piid_acd != null && begin + 1 <= frame.length - 1) {
                a_resultNormal = new A_ResultNormal(frame, begin + 1);
            }
            if (a_resultNormal != null && a_resultNormal.data != null) {
                data = new byte[1 + a_resultNormal.data.length];
                data[0] = piid_acd.data;
                System.arraycopy(a_resultNormal.data, 0, data, 1, a_resultNormal.data.length);
            }
        }
    }

    public static class Regin {
        public int unit;
        public Data beginData;
        public Data endData;
        public byte[] data;

        public Regin(int unit, Data beginData, Data endData) {
            if (unit >= 0 && unit <= 3 && beginData != null && beginData.data != null && endData != null && endData.data != null) {
                this.unit = unit;
                this.beginData = beginData;
                this.endData = endData;
                data = new byte[1 + beginData.data.length + endData.data.length];
                data[0] = (byte) unit;
                System.arraycopy(beginData.data, 0, data, 1, beginData.data.length);
                System.arraycopy(endData.data, 0, data, 1 + beginData.data.length, endData.data.length);
            }
        }

        public Regin(byte[] frame, int begin) {
            if (frame == null || frame.length <= 0) {
                return;
            }

            this.unit = frame[begin] & 0xFF;

            Data beginData = new Data(frame, begin + 1);
            if (beginData.data == null || beginData.data.length <= 0) {
                return;
            }
            Data endData = new Data(frame, begin + 1 + beginData.data.length);
            if (endData.data == null || endData.data.length <= 0) {
                return;
            }
            this.beginData = beginData;
            this.endData = endData;
            this.data = new byte[1 + beginData.data.length + endData.data.length];
            System.arraycopy(frame, begin, this.data, 0, this.data.length);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            switch (unit) {
                case 0:
                    sb.append("[").append(beginData.toString()).append(", ").append(endData.toString()).append(")");
                    break;
                case 1:
                    sb.append("(").append(beginData.toString()).append(", ").append(endData.toString()).append("]");
                    break;
                case 2:
                    sb.append("[").append(beginData.toString()).append(", ").append(endData.toString()).append("]");
                    break;
                case 3:
                    sb.append("(").append(beginData.toString()).append(", ").append(endData.toString()).append(")");
                    break;
            }
            return sb.toString();

        }
    }

    public static class MS {
        public int type;
        public ArrayList<Object> objectArrayList;
        public byte[] data;

        public MS(int type, ArrayList list) {
            if (type >= 0 && type <= 7) {
                this.type = type;
                this.objectArrayList = list;

                switch (type) {
                    case 0:
                        this.data = new byte[1];
                        this.data[0] = (byte) 0;
                        break;
                    case 1:
                        this.data = new byte[1];
                        this.data[0] = (byte) 1;
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        if (list != null && list.size() > 0) {
                            ArrayList<Byte> bytes = new ArrayList<>();
                            bytes.add((byte) 4);

                            int cnt = 0;
                            for (int i = 0; i < list.size(); i++) {
                                Object item = list.get(i);
                                if (item instanceof Integer) {
                                    int value = (int) item;
                                    Log.i(TAG, "MS, value: " + value);
                                    bytes.add((byte)((value >> 8) & 0xFF));
                                    Log.i(TAG, "MS, byte 0: " + (byte)((value >> 8) & 0xFF));
                                    bytes.add((byte)(value & 0xFF));
                                    Log.i(TAG, "MS, byte 1: " + (byte)(value & 0xFF));
                                    cnt = cnt + 1;
                                }
                            }

                            bytes.add(1, (byte)cnt);

                            this.data = new byte[bytes.size()];
                            for (int i = 0; i < this.data.length; i++) {
                                this.data[i] = bytes.get(i);
                            }

                        }
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                }
            }

        }

        public MS(byte[] frame, int begin) {
            if (frame == null || frame.length <= 0) {
                return;
            }

            switch (frame[begin] & 0xFF) {
                case 0:
                    this.type = 0;
                    this.data = new byte[1];
                    this.data[0] = (byte) 0;
                    break;
                case 1:
                    this.type = 1;
                    this.data = new byte[1];
                    this.data[0] = (byte) 1;
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    ArrayList<Byte> bytes = new ArrayList<>();
                    this.type = 4;
                    bytes.add((byte) 4);
                    ArrayList<Object> integers = new ArrayList<>();
                    if (begin + 1 > frame.length - 1) {
                        return;
                    }
                    int cnt = frame[begin + 1] & 0xFF;
                    bytes.add((byte) cnt);
                    if (begin + 1 + cnt * 2 > frame.length - 1) {
                        return;
                    }
                    for (int i = 0; i < cnt; i++) {
                        byte b_0 = frame[begin + 1 + 2 * i + 1];
                        byte b_1 = frame[begin + 1 + 2 * i + 2];
                        bytes.add(b_0);
                        bytes.add(b_1);
                        int value = 0x0000 | ((b_0 << 8) & 0xFF00);
                        value = value | (b_1 & 0x00FF);
                        integers.add(value);
                    }
                    this.objectArrayList = integers;

                    this.data = new byte[bytes.size()];
                    for (int i = 0; i < this.data.length; i++) {
                        this.data[i] = bytes.get(i);
                    }
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    break;
            }

        }
    }

    public static class Check_Info {
        public byte check_type;
        public byte[] check_value;
        public byte[] data;
        public Data checkInfoData;
        public Check_Info(byte check_type, byte[] check_value){
            this.check_type = check_type;
            this.check_value = check_value;

            Data checkTypeData = new Data(Data_Type.ENUM_TYPE, check_type);
            Data checkValueData = new Data(Data_Type.OCTET_STRING_TYPE, check_value);

            ArrayList<Data> dataArrayList = new ArrayList<>();
            dataArrayList.add(checkTypeData);
            dataArrayList.add(checkValueData);

            this.checkInfoData = new Data(Data_Type.STRUCTURE_TYPE, dataArrayList);

            this.data = checkInfoData.data;

        }

        public Check_Info(byte[] frame, int begin) {
            this.checkInfoData = new Data(frame, begin);
            ArrayList<Data> dataArrayList = (ArrayList<Data>) checkInfoData.obj;
            if (dataArrayList != null && dataArrayList.size() == 2) {
                Data checkTypeData = dataArrayList.get(0);
                Data checkValueData = dataArrayList.get(1);

                if (checkTypeData != null && checkTypeData.type == Data_Type.ENUM_TYPE) {
                    this.check_type = (byte) checkTypeData.obj;
                }

                if (checkValueData != null && checkValueData.type == Data_Type.OCTET_STRING_TYPE) {
                    this.check_value = (byte[]) checkValueData.obj;
                }
                this.data = checkInfoData.data;
            }
        }
    }

    public static class File_Info {
        public Data srcFile;
        public Data desFile;
        public Data fileSize;
        public Data fileAttr;
        public Data fileVer;
        public Data fileType;
        public byte[] data;
        public Data fileInfoData;

        public File_Info(String src, String des, int size, String attr, String version, byte type){
            this.srcFile = new Data(Data_Type.VISIBLE_STRING_TYPE, src);
            this.desFile = new Data(Data_Type.VISIBLE_STRING_TYPE, des);
            this.fileSize = new Data(Data_Type.DOUBLE_LONG_UNSIGNED_TYPE, size);
            this.fileAttr = new Data(Data_Type.BIT_STRING_TYPE, attr);
            this.fileVer = new Data(Data_Type.VISIBLE_STRING_TYPE, version);
            this.fileType = new Data(Data_Type.ENUM_TYPE, type);

            ArrayList<Data> dataArrayList = new ArrayList<>();
            dataArrayList.add(srcFile);
            dataArrayList.add(desFile);
            dataArrayList.add(fileSize);
            dataArrayList.add(fileAttr);
            dataArrayList.add(fileVer);
            //dataArrayList.add(fileType);
            Data file_info = new Data(Data_Type.STRUCTURE_TYPE, dataArrayList);
            this.data = file_info.data;
            this.fileInfoData = file_info;
        }

        public File_Info(byte[] frame, int begin) {
            Data file_info = new Data(frame, begin);
            if (file_info.type == Data_Type.STRUCTURE_TYPE) {
                ArrayList<Data> dataArrayList = (ArrayList<Data>) file_info.obj;
                if (dataArrayList != null && dataArrayList.size() == 6) {
                    this.srcFile = dataArrayList.get(0);
                    this.desFile = dataArrayList.get(1);
                    this.fileSize = dataArrayList.get(2);
                    this.fileAttr = dataArrayList.get(3);
                    this.fileVer = dataArrayList.get(4);
                    this.fileType = dataArrayList.get(5);
                    this.data = file_info.data;
                    this.fileInfoData = file_info;
                }
            }

        }

    }

    public enum BaudRate {
        bps_300, bps_600, bps_1200, bps_2400, bps_4800, bps_7200,
        bps_9600, bps_19200, bps_38400, bps_57600, bps_115200,
        self_adaption,
    }
    public enum ChkBit {
        no_check, odd_check, even_check,
    }
    public enum DataBit {
        bit_5, bit_6, bit_7, bit_8,
    }
    public enum StopBit {
        bit_1, bit_2,
    }
    public enum SteamCtrl {
        no, hardware, software,
    }
    public static class COMDCB {
        public BaudRate baudRate;
        public ChkBit chkBit;
        public DataBit dataBit;
        public StopBit stopBit;
        public SteamCtrl steamCtrl;
        public byte[] data;
        public COMDCB(BaudRate baudRate, ChkBit chkBit, DataBit dataBit, StopBit stopBit, SteamCtrl steamCtrl) {
            this.baudRate = baudRate;
            this.chkBit = chkBit;
            this.dataBit = dataBit;
            this.stopBit = stopBit;
            this.steamCtrl = steamCtrl;

            this.data = new byte[5];
            if (this.baudRate == BaudRate.self_adaption) {
                this.data[0] = (byte) 255;
            } else {
                this.data[0] = (byte) (this.baudRate.ordinal());
            }
            this.data[1] = (byte) (this.chkBit.ordinal());
            this.data[2] = (byte) (this.dataBit.ordinal() + 5);
            this.data[3] = (byte) (this.stopBit.ordinal() + 1);
            this.data[4] = (byte) (this.steamCtrl.ordinal());
        }

        public COMDCB(byte[] frame, int begin) {
            if (frame == null || frame.length <= 0 || begin < 0
                    || begin > frame.length - 1 || begin + 4 > frame.length - 1) {
                return;
            }
            if (frame[begin] == (byte)255) {
                this.baudRate = BaudRate.self_adaption;
            } else {
                this.baudRate = BaudRate.values()[frame[begin]];
            }
            this.chkBit = ChkBit.values()[frame[begin + 1]];
            this.dataBit = DataBit.values()[frame[begin + 2] - 5];
            this.stopBit = StopBit.values()[frame[begin + 3] - 1];
            this.steamCtrl = SteamCtrl.values()[frame[begin + 4]];
            this.data = new byte[5];
            System.arraycopy(frame, begin, this.data, 0, 5);
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.baudRate).append("|").append(this.chkBit).append("|").append(this.dataBit)
                    .append("|").append(this.stopBit).append("|").append(this.steamCtrl);
            return sb.toString();
        }
    }

    public static class Date {
        public int year;
        public int month;
        public int dayOfMonth;
        public int dayOfWeek;
        public byte[] data;
        public Date(int year, int month, int dayOfMonth, int dayOfWeek) {
            this.year = year;
            this.month = month;
            this.dayOfMonth = dayOfMonth;
            this.dayOfWeek = dayOfWeek;
            this.data = new byte[5];
            this.data[0] = (byte) ((year >> 8) & 0xFF);
            this.data[1] = (byte) (year & 0xFF);
            this.data[2] = (byte) (month & 0xFF);
            this.data[3] = (byte) (dayOfMonth & 0xFF);
            this.data[4] = (byte) (dayOfWeek & 0xFF);
        }

        public Date(byte[] frame, int begin) {
            if (frame == null || frame.length <= 0 || begin < 0
                    || begin > frame.length - 1 || begin + 4 > frame.length - 1) {
                return;
            }
            this.year = frame[begin] * 256 + frame[begin + 1];
            this.month = frame[begin + 2] & 0xFF;
            this.dayOfMonth = frame[begin + 3] & 0xFF;
            this.dayOfWeek = frame[begin + 4] & 0xFF;
            this.data = new byte[5];
            System.arraycopy(frame, begin, this.data, 0, 5);
        }

        @Override
        public String toString() {
            return this.year + "-" + this.month + "-" + this.dayOfMonth + " " + this.dayOfWeek;
        }
    }

    public static class TSA {
        public String addr;
        public byte[] data;
        public TSA(String addr) {
            byte[] addr_data = DataConvertUtils.convertHexStringToByteArray(addr, 12, true);
            if (addr_data != null) {

                this.addr = addr;
                this.data = new byte[8];
                this.data[0] = 0x07;
                this.data[1] = 0x05;
                System.arraycopy(addr_data, 0, this.data, 2, 6);

            }
        }

        public TSA(byte[] frame, int begin) {
            try {
                this.data = new byte[8];
                System.arraycopy(frame, 0, this.data, 0, 8);
                this.addr = DataConvertUtils.convertByteArrayToString(frame, begin + 2, begin + 2 + 5, true);

            } catch (Exception e) {
                this.data = null;
                this.addr = null;
            }

        }

        @Override
        public String toString() {
            return addr;
        }
    }
}
