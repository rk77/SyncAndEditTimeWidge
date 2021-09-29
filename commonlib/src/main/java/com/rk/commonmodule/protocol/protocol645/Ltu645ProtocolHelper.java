package com.rk.commonmodule.protocol.protocol645;

import android.text.TextUtils;

import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.utils.DataConvertUtils;
import com.rk.commonmodule.utils.MeterProtocolDetector;
import com.rk.commonmodule.utils.MeterProtocolDetector.PHASE_INFO;
import com.rk.commonmodule.utils.MeterProtocolDetector.METER_PROTOCOL_TYPE;
import com.rk.commonmodule.utils.MeterProtocolDetector.MeterInfo;
import com.rk.commonmodule.utils.MeterProtocolDetector.ModeOf485;
import com.rk.commonmodule.utils.MeterProtocolDetector.FuncSwitch;

import java.util.ArrayList;

public class Ltu645ProtocolHelper {
    public static byte[] makeGetVersionInfoFrame(String address) {
        byte ctrlCode = 0x1E;
        String data = "06";
        return Protocol645FrameBaseMaker.getInstance().makeFrame(address, ctrlCode, data);
    }

    public static String parseDetail(byte[] frame) {
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        switch (protocol645Frame.mCtrlCode) {
            case (byte) 0x9E:
                //LogUtils.i("info: " + DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData));
                if (6 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 3, 6, false)).append("|");
                }
                if (13 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 10, 13, false)).append("|");
                }
                if (19 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 14, 19, false)).append("|");
                }
                if (26 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 23, 26, false)).append("|");
                }
                if (32 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 27, 32, false)).append("|");
                }
                if (44 <= protocol645Frame.mData.length - 1) {
                    sb.append(DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 36, 44, false));
                }
                break;
        }
        return sb.toString();
    }

    public static byte[] makeGetAddrFrame() {
        String addr = "AAAAAAAAAAAA";
        byte ctrlCode = 0x13;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, null);
    }
    public static String  parseGetAddrFrame(byte[] frame) {
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame == null) {
            return null;
        }
        return DataConvertUtils.convertByteArrayToString(protocol645Frame.mAddressArea, true);
    }

    public static byte[] makeSetAddrFrame(String oldAddr, String address) {
        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(oldAddr)) {
            return null;
        }
        byte ctrlCode = 0x1E;
        String data = "82" + address;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(oldAddr, ctrlCode, data);
    }

    public static String parseSetAddrFrame(byte[] frame) {
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame == null) {
            return "false";
        }

        if (protocol645Frame.mCtrlCode == (byte) 0x9E) {
            return "true";
        } else {
            return "false|" + DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, false);
        }
    }

    public static byte[] makeResetFrame(String addr, String dataLable) {
        if (TextUtils.isEmpty(addr) || TextUtils.isEmpty(dataLable)) {
            return null;
        }
        byte ctrlCode = 0x1E;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static byte[] makeReadCurrentFrame(String addr) {
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        byte ctrlCode = 0x1E;
        String dataLable = "86";
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static byte[] makeReadTmprtrSensorFrame(String addr) {
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        byte ctrlCode = 0x11;
        String dataLable = "FE002602";
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static String parseTmprtrSensorFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);

        if (protocol645Frame.mCtrlCode == (byte) 0x91) {
            LogUtils.i("data: " + DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, false));
            if (protocol645Frame.mData != null && protocol645Frame.mData.length == 10) {
                StringBuilder sb = new StringBuilder();
                if (protocol645Frame.mData[4] != (byte) 0xFF && protocol645Frame.mData[5] != (byte)0xFF) {
                    if ((protocol645Frame.mData[5] & 0x80) == 0x80) {
                        sb.append("-");
                    }
                    sb.append(DataConvertUtils.convertByteToString((byte) (protocol645Frame.mData[5] & 0x7F)))
                            .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[4]).charAt(0))
                            .append(".")
                            .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[4]).charAt(1))
                            .append("\u2103").append("|");
                } else {
                    sb.append("无效").append("|");
                }

                if (protocol645Frame.mData[6] != (byte) 0xFF && protocol645Frame.mData[7] != (byte)0xFF) {
                    if ((protocol645Frame.mData[7] & 0x80) == 0x80) {
                        sb.append("-");
                    }
                    sb.append(DataConvertUtils.convertByteToString((byte) (protocol645Frame.mData[7] & 0x7F)))
                            .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[6]).charAt(0))
                            .append(".")
                            .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[6]).charAt(1))
                            .append("\u2103").append("|");
                } else {
                    sb.append("无效").append("|");
                }

                if (protocol645Frame.mData[8] != (byte) 0xFF && protocol645Frame.mData[9] != (byte)0xFF) {
                    if ((protocol645Frame.mData[9] & 0x80) == 0x80) {
                        sb.append("-");
                    }
                    sb.append(DataConvertUtils.convertByteToString((byte) (protocol645Frame.mData[9] & 0x7F)))
                            .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[8]).charAt(0))
                            .append(".")
                            .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[8]).charAt(1))
                            .append("\u2103");
                } else {
                    sb.append("无效");
                }
                return sb.toString();
            }
        }
        return null;
    }

    public static String parseReadCurrentFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);

        if (protocol645Frame.mCtrlCode == (byte) 0x9E) {
            LogUtils.i("data: " + DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, false));
            if (protocol645Frame.mData != null && protocol645Frame.mData.length == 9) {
                StringBuilder sb = new StringBuilder();
                int mainVoltage = ((protocol645Frame.mData[2] << 8) & 0x0000FF00) | (protocol645Frame.mData[1] & 0x000000FF);
                sb.append((double) mainVoltage / 1000).append("V").append("|");
                int capacitanceVoltage = ((protocol645Frame.mData[4] << 8) & 0x0000FF00) | (protocol645Frame.mData[3] & 0x000000FF);
                sb.append((double) capacitanceVoltage / 1000).append("V").append("|");
                int batteryVoltage = ((protocol645Frame.mData[6] << 8) & 0x0000FF00) | (protocol645Frame.mData[5] & 0x000000FF);
                sb.append((double) batteryVoltage / 1000).append("V").append("|");
                int systemTemperature = ((protocol645Frame.mData[8] << 8) & 0x0000FF00) | (protocol645Frame.mData[7] & 0x000000FF);
                sb.append(systemTemperature).append("\u2103");
                return sb.toString();
            }
        }
        return null;
    }

    public static byte[] makeReadTelesignalisationFrame(String addr, boolean hasTimeTag) {
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        byte ctrlCode = 0x1E;
        String dataLable = "A8";
        String data = "01";
        if (hasTimeTag) {
            data = "1E";
        }
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable + data);
    }

    public static String parseReadTelesignalisationFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }

        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);

        if (protocol645Frame.mCtrlCode == (byte) 0x9E) {
            LogUtils.i("data: " + DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, false));
            if (protocol645Frame.mData != null && protocol645Frame.mData.length >= 4) {
                StringBuilder sb = new StringBuilder();
                sb.append(protocol645Frame.mData[2]).append("|");
                if (protocol645Frame.mData[3] == (byte) 0x00) {
                    sb.append("分");
                } else {
                    sb.append("合");
                }
                if (protocol645Frame.mData[1] == (byte) 0x1E && protocol645Frame.mData.length == 11) {
                    sb.append("|");
                    sb.append(parse_CP56_Time(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 4, 10)));
                }

                return sb.toString();
            }
        }
        return null;

    }

    public static String parseDisplaceUnitTelesignalisationFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }

        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);

        if (protocol645Frame.mCtrlCode == (byte) 0x9E) {
            LogUtils.i("data: " + DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, false));
            if (protocol645Frame.mData != null && protocol645Frame.mData.length >= 4) {
                StringBuilder sb = new StringBuilder();
                sb.append(protocol645Frame.mData[2]).append("|");
                if (protocol645Frame.mData[3] == (byte) 0x00) {
                    sb.append("分");
                } else {
                    if (protocol645Frame.mData[2]  > 1) {
                        for (int i = 0; i < protocol645Frame.mData[2]; i++) {
                            if (((protocol645Frame.mData[3] >> i) & 0x01) > 0) {
                                sb.append("第" + (i + 1) + "路: 合");
                            } else {
                                sb.append("第" + (i + 1) + "路: 开");
                            }
                            if (i < (protocol645Frame.mData[2] - 1)) {
                                sb.append("；");
                            }
                        }
                    } else {
                        sb.append("合");
                    }
                }
                if (protocol645Frame.mData[1] == (byte) 0x1E && protocol645Frame.mData.length == 11) {
                    sb.append("|");
                    sb.append(parse_CP56_Time(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 4, 10)));
                }

                return sb.toString();
            }
        }
        return null;

    }

    private static String parse_CP56_Time(byte[] data) {
        if (data == null || data.length != 7) {
            return null;
        }
        if ("ffffffffffffff".equals(DataConvertUtils.convertByteArrayToString(data, false))
                || "FFFFFFFFFFFFFF".equals(DataConvertUtils.convertByteArrayToString(data, false))) {
            return "无效";
        }
        //String tst = "ac df 27 0d 06 01 15".replaceAll(" ", "");
        //data = DataConvertUtils.convertHexStringToByteArray(tst, tst.length(), false);
        StringBuilder sb = new StringBuilder();
        LogUtils.i("data: " + DataConvertUtils.convertByteArrayToString(data, false));
        sb.append((data[6] & 0x7F) + 2000).append("-")
                .append(String.format("%02d", data[5] & 0x0F)).append("-")
                .append(String.format("%02d", data[4] & 0x1F)).append(" ")
                .append(String.format("%02d", data[3] & 0x1F)).append(":")
                .append(String.format("%02d", data[2] & 0x3F)).append(":");
        int second = (((data[1] << 8) & 0x0000FF00) | (data[0] & 0x000000FF)) / 1000;
        int million_second = (((data[1] << 8) & 0x0000FF00) | (data[0] & 0x000000FF)) - second * 1000;
        sb.append(String.format("%02d", second)).append(".").append(million_second);
        return sb.toString();

    }

    public static byte[] makeReadClockTimeFrame(String addr) {
        if (TextUtils.isEmpty(addr)) {
            return null;
        }

        byte ctrlCode = 0x11;
        String dataLable = DataConvertUtils.reverse("040001e0", 2);
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static String parseReadClockTimeFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mData != null && protocol645Frame.mData.length == 11) {
            return parse_CP56_Time(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 4, 10));
        }
        return null;
    }

    public static byte[] makeBroadcastResetTimeFrame(String date, String time) {
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            return null;
        }
        String address = "999999999999";
        byte ctrlCode = 0x08;
        String data = DataConvertUtils.reverse(time, 2) + DataConvertUtils.reverse(date, 2);
        return Protocol645FrameBaseMaker.getInstance().makeFrame(address, ctrlCode, data);
    }
    //date: yyDDMM; time:HHMMSS
    public static byte[] makeResetTimeFrame(String date, String time) {
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            return null;
        }
        String address = "999999999999";
        byte ctrlCode = 0x08;
        byte[] data = make_CP56_Time(date, time);
        return Protocol645FrameBaseMaker.getInstance().makeFrame(address, ctrlCode, DataConvertUtils.convertByteArrayToString(data, false));
    }

    //date: yyMMDD; time:HHmmSS
    private static byte[] make_CP56_Time(String date, String time) {
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || date.length() != 6 || time.length() != 6) {
            return null;
        }
        byte[] cp56_time = new byte[7];
        int mill_second = Integer.parseInt(time.substring(4)) * 1000;
        cp56_time[1] = (byte) ((mill_second >> 8) & 0x000000FF);
        cp56_time[0] = (byte) (mill_second & 0x000000FF);
        int minute = Integer.parseInt(time.substring(2, 4));
        cp56_time[2] = (byte) (minute & 0x3F);
        int hour = Integer.parseInt(time.substring(0, 2));
        cp56_time[3] = (byte) (hour & 0x1F);
        int day = Integer.parseInt(date.substring(4));
        cp56_time[4] = (byte) (day & 0x1F);
        int month = Integer.parseInt(date.substring(2, 4));
        cp56_time[5] = (byte) (month & 0x0F);
        int year = Integer.parseInt(date.substring(0, 2));
        cp56_time[6] = (byte) (year & 0x7F);
        return cp56_time;
    }

    public static byte[] makeGetArchieveFrame(String addr) {
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        byte ctrlCode = 0x11;
        String dataLable = "03000008";
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static ArrayList<MeterInfo> parseGetArchieveFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame.mCtrlCode == (byte)0x91 && protocol645Frame.mData != null && protocol645Frame.mData.length >= 13) {
            int cnt = protocol645Frame.mData[4];
            ArrayList<MeterInfo> meters = new ArrayList<>(cnt);
            LogUtils.i("cnt: " + cnt);
            for (int i = 0; i < cnt; i++) {
                String addr = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 6 + 7 * i, 6 + 7 * i + 5, true);
                METER_PROTOCOL_TYPE protocol_type = METER_PROTOCOL_TYPE.PROTOCOL_NONE;
                if (protocol645Frame.mData[6 + 7 * i + 5 + 1] == (byte) 0) {
                    protocol_type = METER_PROTOCOL_TYPE.PROTOCOL_698;
                } else if (protocol645Frame.mData[6 + 7 * i + 5 + 1] == (byte) 1) {
                    protocol_type = METER_PROTOCOL_TYPE.PROTOCOL_645_97;
                } else if (protocol645Frame.mData[6 + 7 * i + 5 + 1] == (byte) 2) {
                    protocol_type = METER_PROTOCOL_TYPE.PROTOCOL_645_07;
                } else if (protocol645Frame.mData[6 + 7 * i + 5 + 1] == (byte) 3) {
                    protocol_type = METER_PROTOCOL_TYPE.PROTOCOL_698;
                }
                meters.add(new MeterInfo(protocol_type, addr));
            }
            return meters;

        }
        return null;
    }

    public static byte[] makeAddOneArchieveFrame(String addr, MeterInfo meterInfo) {
        if (meterInfo == null || TextUtils.isEmpty(addr)) {
            return null;
        }
        byte ctrlCode = 0x1E;
        String dataLable = "AB";

        byte[] data = new byte[9];
        data[0] = 0x01;
        byte[] addrData = DataConvertUtils.convertHexStringToByteArray(meterInfo.address, addr.length(), true);
        if (addrData == null || addrData.length != 6) {
            return null;
        }
        for (int i = 0; i < 6; i++) {
            data[i + 1] = addrData[i];
        }
        data[7] = 0x00;
        switch (meterInfo.baudRateOf485) {
            case bps_1200:
                data[7] = (byte) (data[7] | 0x10);
                break;
            case bps_2400:
                data[7] = (byte) (data[7] | 0x20);
                break;
            case bps_4800:
                data[7] = (byte) (data[7] | 0x30);
                break;
            case bps_9600:
                data[7] = (byte) (data[7] | 0x40);
                break;
        }
        switch (meterInfo.protocolType) {
            case PROTOCOL_645_97:
                data[7] = (byte) (data[7] | 0x01);
                break;
            case PROTOCOL_645_07:
                data[7] = (byte) (data[7] | 0x02);
                break;
            case PROTOCOL_698:
                data[7] = (byte) (data[7] | 0x03);
                break;
        }

        data[8] = 0x00;
        switch (meterInfo.meterType) {
            case SINGLE_PHASE:
                data[8] = (byte) (data[8] | 0x00);
                break;
            case THRESS_PHASE:
                data[8] = (byte) (data[8] | 0x10);
                break;
        }
        switch (meterInfo.port485ConnectLtu) {
            case PORT_485_1:
                data[8] = (byte) (data[8] | 0x01);
                break;
            case PORT_485_2:
                data[8] = (byte) (data[8] | 0x02);
                break;
            case PORT_485_3:
                data[8] = (byte) (data[8] | 0x03);
                break;
            case PORT_485_4:
                data[8] = (byte) (data[8] | 0x04);
                break;
        }

        String dataString = DataConvertUtils.convertByteArrayToString(data, false);

        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable + dataString);
    }

    public static boolean parseAddOneArchieveFrame(byte[] frame) {
        if (frame == null) {
            return false;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mCtrlCode == (byte) 0x9E) {
            return true;
        } else {
            return false;
        }
    }

    public static byte[] makeReadPhaseResultFrame(String addr) {
        LogUtils.i("addr: " + addr);
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        String dataLable = "11000809";
        byte ctrlCode = 0x11;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static ArrayList<MeterInfo> parseReadPhaseResultFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }

        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mCtrlCode == (byte) 0x91) {
            int cnt = protocol645Frame.mData[4];
            ArrayList<MeterInfo> meters = new ArrayList<>(cnt);
            LogUtils.i("cnt: " + cnt);
            for (int i = 0; i < cnt; i++) {
                String addr = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 5 + 7 * i, 5 + 7 * i + 5, true);
                METER_PROTOCOL_TYPE protocol_type = METER_PROTOCOL_TYPE.PROTOCOL_NONE;
                PHASE_INFO phase_info = PHASE_INFO.PHASE_UNKNOWN;
                if (protocol645Frame.mData[5 + 7 * i + 5 + 1] == (byte) 0) {
                    phase_info = PHASE_INFO.PHASE_UNKNOWN;
                } else if (protocol645Frame.mData[5 + 7 * i + 5 + 1] == (byte) 1) {
                    phase_info = PHASE_INFO.PHASE_A;
                } else if (protocol645Frame.mData[5 + 7 * i + 5 + 1] == (byte) 2) {
                    phase_info = PHASE_INFO.PHASE_B;
                } else if (protocol645Frame.mData[5 + 7 * i + 5 + 1] == (byte) 3) {
                    phase_info = PHASE_INFO.PHASE_C;
                } else if (protocol645Frame.mData[5 + 7 * i + 5 + 1] == (byte) 0xFF) {
                    phase_info = PHASE_INFO.PHASE_METER;
                }
                MeterInfo meterInfo = new MeterInfo(protocol_type, addr);
                meterInfo.phaseInfo = phase_info;
                meters.add(meterInfo);
            }
            return meters;
        } else {
            return null;
        }

    }

    public static byte[] makeReadImpedanceResultFrame(String addr) {
        LogUtils.i("addr: " + addr);
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        String dataLable = "02000809";
        byte ctrlCode = 0x11;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static String parseReadImpedanceResultFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mCtrlCode == (byte) 0x91
                && protocol645Frame.mData != null && protocol645Frame.mData.length == 13) {
            String a_impedance = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 4, 6, true);
            String b_impedance = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 7, 9, true);
            String c_impedance = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 10, 12, true);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Integer.parseInt(a_impedance)).append(" \u006D\u03A9").append("|")
                    .append(Integer.parseInt(b_impedance)).append(" \u006D\u03A9").append("|")
                    .append(Integer.parseInt(c_impedance)).append(" \u006D\u03A9");
            return stringBuilder.toString();

        } else {
            return null;
        }
    }

    public static byte[] makeReadLineLoseResultFrame(String addr) {
        LogUtils.i("addr: " + addr);
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        String dataLable = "14000809";
        byte ctrlCode = 0x11;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static String parseReadLineLossResultFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mCtrlCode == (byte) 0x91
                && protocol645Frame.mData != null && protocol645Frame.mData.length == 24) {
            String timeLable = DataConvertUtils.convertByteToString(protocol645Frame.mData[4]);
            String apply_power = parsePower(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 5, 8));
            String consumed_power = parsePower(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 9, 12));
            String reverse_power = parsePower(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 13, 16));
            String distributed_net_power = parsePower(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 17, 20));
            String line_loss_rate = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 21, 22, false);

            String exception_flag = "正常";
            if ((protocol645Frame.mData[23] & 0xFF) == 0x00) {
                exception_flag = "正常";
            } else if ((protocol645Frame.mData[23] & 0xFF) == 0x01) {
                exception_flag = "总表电量异常";
            } else if ((protocol645Frame.mData[23] & 0xFF) == 0x02) {
                exception_flag = "户表电量异常";
            } else {
                exception_flag = "未知";
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Integer.parseInt(timeLable)).append("日").append("|")
                    .append(apply_power).append("kWh").append("|")
                    .append(consumed_power).append("kWh").append("|")
                    .append(reverse_power).append("kWh").append("|")
                    .append(distributed_net_power).append("kWh").append("|")
                    .append(line_loss_rate).append("%").append("|")
                    .append(exception_flag);
            return stringBuilder.toString();

        } else if (protocol645Frame != null && protocol645Frame.mCtrlCode == (byte) 0x91
                && protocol645Frame.mData != null && protocol645Frame.mData.length == 26) {
            String timeLable = DataConvertUtils.convertByteToString(protocol645Frame.mData[4]);
            String apply_power = parsePower(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 5, 8));
            String consumed_power = parsePower(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 9, 12));
            String reverse_power = parsePower(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 13, 16));
            String distributed_net_power = parsePower(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 17, 20));
            String line_loss_rate = parsePower(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 21, 24));

            String exception_flag = "正常";
            if ((protocol645Frame.mData[25] & 0xFF) == 0x00) {
                exception_flag = "正常";
            } else if ((protocol645Frame.mData[25] & 0xFF) == 0x01) {
                exception_flag = "总表电量异常";
            } else if ((protocol645Frame.mData[25] & 0xFF) == 0x02) {
                exception_flag = "户表电量异常";
            } else {
                exception_flag = "未知";
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Integer.parseInt(timeLable)).append("日").append("|")
                    .append(apply_power).append("kWh").append("|")
                    .append(consumed_power).append("kWh").append("|")
                    .append(reverse_power).append("kWh").append("|")
                    .append(distributed_net_power).append("kWh").append("|")
                    .append(line_loss_rate).append("%").append("|")
                    .append(exception_flag);
            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    private static String parsePower(byte[] frame) {
        if (frame == null || frame.length < 4) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.insert(0, DataConvertUtils.convertByteToString(frame[0]))
                .insert(0, ".")
                .insert(0, DataConvertUtils.convertByteToString(frame[1]))
                .insert(0, DataConvertUtils.convertByteToString(frame[2]))
                .insert(0, DataConvertUtils.convertByteToString(frame[3]));
        return sb.toString();
    }

    public static byte[] makeReadLTUBaseInfoFrame(String addr) {
        LogUtils.i("addr: " + addr);
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        String dataLable = "01";
        byte ctrlCode = 0x1E;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static String parseReadLTUBaseInfoFrame(byte[] frame) {
        if (frame == null || frame.length < 4) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mData != null && protocol645Frame.mData.length == 0x23) {
            LogUtils.i("data: " + DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, false));
            StringBuilder sb = new StringBuilder();
            String address = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 1, 6, true);
            String sofware_version = DataConvertUtils.getByteArray2AsciiString(protocol645Frame.mData, 7, 12, false);
            String sys_clock = parseTime(DataConvertUtils.getSubByteArray(protocol645Frame.mData, 13, 18));
            String real_time_search_meter_switch = ((protocol645Frame.mData[19] & 0xFF)) > 0 ? "开" : "关";
            int time = (protocol645Frame.mData[21] & 0xFF) * 256 + (protocol645Frame.mData[20] & 0xFF);
            String interval_time_per_round = ((protocol645Frame.mData[19] & 0xFF)) > 0 ? (time + "分") : "N/N";
            String interval_time_per_frame = ((protocol645Frame.mData[19] & 0xFF)) > 0 ? ((protocol645Frame.mData[22] & 0xFF) + "秒") : "N/N";
            String sys_reset_time = ((protocol645Frame.mData[24] & 0xFF) * 256 + (protocol645Frame.mData[23] & 0xFF)) + "";
            String delay_time_of_485 = (protocol645Frame.mData[25] & 0xFF) + "秒";
            String timing_switch = (protocol645Frame.mData[26] & 0xFF) > 0 ? "允许" : "不允许";
            String tele_signal_switch = (protocol645Frame.mData[27] & 0xFF) > 0 ? "合" : "分";
            String search_speed_ctrl = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 28, 29, true);
            String baud_rate_485 = ((protocol645Frame.mData[31] & 0xFF) * 256 + (protocol645Frame.mData[30] & 0xFF)) + "";
            String baud_rate_lora = ((protocol645Frame.mData[33] & 0xFF) * 256 + (protocol645Frame.mData[32] & 0xFF)) + "";

            sb.append(address).append("|").append(sofware_version).append("|").append(sys_clock).append("|")
                    .append(real_time_search_meter_switch).append("|").append(interval_time_per_round).append("|")
                    .append(interval_time_per_frame).append("|").append(sys_reset_time).append("|")
                    .append(delay_time_of_485).append("|").append(timing_switch).append("|")
                    .append(tele_signal_switch).append("|").append(search_speed_ctrl).append("|")
                    .append(baud_rate_485).append("|").append(baud_rate_lora);
            return sb.toString();

        }
        return null;
    }

    //秒分时日月年, 6 bytes.
    private static String parseTime(byte[] data) {
        if (data == null || data.length != 6) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.insert(0, DataConvertUtils.convertByteToString(data[0])).insert(0, ":")
                .insert(0, DataConvertUtils.convertByteToString(data[1])).insert(0, ":")
                .insert(0, DataConvertUtils.convertByteToString(data[2]))
                .insert(0, " ")
                .insert(0, DataConvertUtils.convertByteToString(data[3])).insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[4])).insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[5]));
        return sb.toString();
    }

    public static byte[] makeSetLtuSearchMeterBaudRateParamsFrame(MeterInfo meterInfo) {
        if (meterInfo == null || meterInfo.baudRateOfMaintainLora == 0 || meterInfo.baudRateOfMaintain485 == 0 || meterInfo.address == null) {
            return null;
        }
        String dataLable = "93";
        byte[] data_frame = new byte[6];
        data_frame[0] = (byte) 0xFF;
        data_frame[1] = (byte) 0xFF;
        data_frame[2] = (byte) (meterInfo.baudRateOfMaintain485 & 0xFF);
        data_frame[3] = (byte) ((meterInfo.baudRateOfMaintain485 >> 8) & 0xFF);
        data_frame[4] = (byte) (meterInfo.baudRateOfMaintainLora & 0xFF);
        data_frame[5] = (byte) ((meterInfo.baudRateOfMaintainLora >> 8) & 0xFF);
        String data = dataLable + DataConvertUtils.convertByteArrayToString(data_frame, false);
        byte ctrlCode = 0x1E;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(meterInfo.address, ctrlCode, data);
    }

    public static byte[] makeReadSearchModeOf485Frame(String addr) {
        LogUtils.i("addr: " + addr);
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        String dataLable = "A2";
        byte ctrlCode = 0x1E;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static ModeOf485 parseReadSearchModeOf485Frame(byte[] frame) {
        if (frame == null) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mData != null && protocol645Frame.mData.length == 0x09 && protocol645Frame.mCtrlCode == (byte) 0x9E) {
            ModeOf485 modeOf485 = new ModeOf485(0, 0, 0, 0);
            modeOf485.mode_485_1 = protocol645Frame.mData[2] & 0xFF;
            modeOf485.mode_485_2 = protocol645Frame.mData[4] & 0xFF;
            modeOf485.mode_485_3 = protocol645Frame.mData[6] & 0xFF;
            modeOf485.mode_485_4 = protocol645Frame.mData[8] & 0xFF;
            return modeOf485;

        }
        return null;
    }

    public static byte[] makeSetSearchModeOf485Frame(MeterInfo meterInfo) {
        if (meterInfo == null || TextUtils.isEmpty(meterInfo.address) || meterInfo.modeOf485 == null) {
            return null;
        }
        String dataLable = "A2";
        String data = "01" + DataConvertUtils.convertByteToString((byte) (meterInfo.modeOf485.mode_485_1))
                + "02" + DataConvertUtils.convertByteToString((byte) (meterInfo.modeOf485.mode_485_2))
                + "03" + DataConvertUtils.convertByteToString((byte) (meterInfo.modeOf485.mode_485_3))
                + "04" + DataConvertUtils.convertByteToString((byte) (meterInfo.modeOf485.mode_485_4));
        byte ctrlCode = 0x1E;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(meterInfo.address, ctrlCode, dataLable + data);
    }

    public static byte[] makeReadFuncSwitchFrame(String addr) {
        LogUtils.i("addr: " + addr);
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        String dataLable = "9B";
        byte ctrlCode = 0x1E;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable);
    }

    public static FuncSwitch parseReadFuncSwitchFrame(byte[] frame) {
        if (frame == null) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mData != null
                && protocol645Frame.mData.length == 0x07 && protocol645Frame.mCtrlCode == (byte) 0x9E) {
            FuncSwitch funcSwitch = new FuncSwitch(0, 0, 0, 0, 0, 0);
            funcSwitch.switch_1 = protocol645Frame.mData[1] & 0xFF;
            funcSwitch.switch_2 = protocol645Frame.mData[2] & 0xFF;
            funcSwitch.switch_3 = protocol645Frame.mData[3] & 0xFF;
            funcSwitch.switch_4 = protocol645Frame.mData[4] & 0xFF;
            funcSwitch.switch_5 = protocol645Frame.mData[5] & 0xFF;
            funcSwitch.switch_6 = protocol645Frame.mData[6] & 0xFF;
            return funcSwitch;

        }
        return null;
    }

    public static byte[] makeSetFuncSwitchFrame(MeterInfo meterInfo) {
        if (meterInfo == null || TextUtils.isEmpty(meterInfo.address) || meterInfo.funcSwitch == null) {
            return null;
        }
        String dataLable = "9B";
        String data = DataConvertUtils.convertByteToString((byte) (meterInfo.funcSwitch.switch_1))
                + DataConvertUtils.convertByteToString((byte) (meterInfo.funcSwitch.switch_2))
                + DataConvertUtils.convertByteToString((byte) (meterInfo.funcSwitch.switch_3))
                + DataConvertUtils.convertByteToString((byte) (meterInfo.funcSwitch.switch_4))
                + DataConvertUtils.convertByteToString((byte) (meterInfo.funcSwitch.switch_5))
                + DataConvertUtils.convertByteToString((byte) (meterInfo.funcSwitch.switch_6));
        byte ctrlCode = 0x1E;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(meterInfo.address, ctrlCode, dataLable + data);
    }

    public static byte[] makeDisplaceCheckUnitRemoteCtrlCmdFrame(String addr, String origData) {
        if (TextUtils.isEmpty(addr) || TextUtils.isEmpty(origData)) {
            return null;
        }
        String dataLable = "B2";
        byte ctrlCode = 0x1E;
        return Protocol645FrameBaseMaker.getInstance().makeFrame(addr, ctrlCode, dataLable + origData);
    }

    public static boolean parseDisplaceCheckUnitRemoteCtrlCmdFrame(byte[] frame) {
        if (frame == null) {
            return false;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mCtrlCode == (byte) 0x9E) {
            return true;
        }
        return false;
    }

    /****************************** Standard 645 Protocol ********************************/

    public static  byte[] makeReadVoltageFrame_07(MeterInfo meterInfo)
    {
        if (meterInfo == null || TextUtils.isEmpty(meterInfo.address)
                || meterInfo.protocolType == null || meterInfo.protocolType == METER_PROTOCOL_TYPE.PROTOCOL_NONE) {
            return null;
        }
        byte ctrlCode = 0x11;
        String dataLable = "00FF0102";
        return Protocol645FrameBaseMaker.getInstance().makeFrame(meterInfo.address, ctrlCode, dataLable);
    }

    public static  String[] parseReadVoltageFrame_07(byte[] frame)
    {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mData != null
                && protocol645Frame.mData.length == 0x0A && protocol645Frame.mCtrlCode == (byte) 0x91) {
            String[] voltage = new String[3];
            for (int i = 0; i < 3; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(DataConvertUtils.convertByteToString(protocol645Frame.mData[5 + i * 2]))
                        .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[4 + i * 2]).charAt(0))
                        .append(".")
                        .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[4 + i * 2]).charAt(1))
                        .append("V");
                voltage[i] = sb.toString();
            }
            return voltage;

        }
        return null;
    }

    public static  byte[] makeReadVoltageFrame_97(MeterInfo meterInfo, PHASE_INFO phase_info)
    {
        if (meterInfo == null || TextUtils.isEmpty(meterInfo.address)
                || meterInfo.protocolType == null || meterInfo.protocolType == METER_PROTOCOL_TYPE.PROTOCOL_NONE) {
            return null;
        }
        byte ctrlCode = 0x01;
        String dataLable = "";
        switch (phase_info) {
            case PHASE_A:
                dataLable = "11B6";
                break;
            case PHASE_B:
                dataLable = "12B6";
                break;
            case PHASE_C:
                dataLable = "13B6";
                break;
            default:
                dataLable = "11B6";
                break;
        }
        return Protocol645FrameBaseMaker.getInstance().makeFrame(meterInfo.address, ctrlCode, dataLable);
    }

    public static String parseReadVoltageFrame_97(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        if (protocol645Frame != null && protocol645Frame.mData != null
                && protocol645Frame.mData.length == 0x06 && protocol645Frame.mCtrlCode == (byte) 0x81) {

            StringBuilder sb = new StringBuilder();
            sb.append(DataConvertUtils.convertByteToString(protocol645Frame.mData[3]))
                    .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[2]).charAt(0))
                    .append(".")
                    .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[2]).charAt(1))
                    .append("V");
            return sb.toString();

        }
        return null;
    }

    public static  byte[] makeReadCurrentFrame_07(MeterInfo meterInfo)
    {
        if (meterInfo == null || TextUtils.isEmpty(meterInfo.address)
                || meterInfo.protocolType == null || meterInfo.protocolType == METER_PROTOCOL_TYPE.PROTOCOL_NONE) {
            return null;
        }
        byte ctrlCode = 0x11;
        String dataLable = "00FF0202";
        return Protocol645FrameBaseMaker.getInstance().makeFrame(meterInfo.address, ctrlCode, dataLable);
    }

    public static  String[] parseReadCurrentFrame_07(byte[] frame)
    {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Protocol645Frame protocol645Frame = Protocol645FrameBaseParser.getInstance().parse(frame);
        LogUtils.i("data: " + DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, false));
        if (protocol645Frame != null && protocol645Frame.mData != null
                && protocol645Frame.mData.length == 13 && protocol645Frame.mCtrlCode == (byte) 0x91) {
            String[] current = new String[3];
            for (int i = 0; i < 3; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(DataConvertUtils.convertByteToString(protocol645Frame.mData[6 + i * 3]))
                        .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[5 + i * 3]).charAt(0))
                        .append(".")
                        .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[5 + i * 3]).charAt(1))
                        .append(DataConvertUtils.convertByteToString(protocol645Frame.mData[4 + i * 3]))
                        .append("A");
                current[i] = sb.toString();
            }
            return current;

        }
        return null;
    }

}
