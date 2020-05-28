package com.rk.commonmodule.protocol.protocol698;

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
            this.serviceNum = data & 0x1F;
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
            data[0] = (byte) (year & 0xFF);
            data[1] = (byte) ((year / 256) & 0xFF);
            data[2] = (byte) (month & 0xFF);
            data[3] = (byte) (day & 0xFF);
            data[4] = (byte) (hour & 0xFF);
            data[5] = (byte) (minute & 0xFF);
            data[6] = (byte) (second & 0xFF);

        }

        public DateTimeS(byte[] data) {
            this.data = data;
            if (data != null && data.length>= 7) {
                this.year = data[1] * 256 + data[0];
                this.month = data[2];
                this.day = data[3];
                this.hour = data[4];
                this.minute = data[5];
                this.second = data[6];
            }
        }

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
}
