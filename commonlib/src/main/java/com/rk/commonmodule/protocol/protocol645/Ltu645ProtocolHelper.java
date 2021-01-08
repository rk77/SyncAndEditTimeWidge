package com.rk.commonmodule.protocol.protocol645;

import android.text.TextUtils;

import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.utils.DataConvertUtils;

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

    private static String parse_CP56_Time(byte[] data) {
        if (data == null || data.length != 7) {
            return null;
        }
        //String tst = "ac df 27 0d 06 01 15".replaceAll(" ", "");
        //data = DataConvertUtils.convertHexStringToByteArray(tst, tst.length(), false);
        StringBuilder sb = new StringBuilder();
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
}
