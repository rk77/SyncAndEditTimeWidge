package com.rk.commonmodule.protocol.protocol645;

import android.util.Log;

import com.rk.commonmodule.protocol.protocol645.y2007.Protocol645Constant;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.HashMap;
import java.util.Map;

public class Protocol645FrameBaseParser {
    private static final String TAG = Protocol645FrameBaseParser.class.getSimpleName();

    private int mFirstBeginPos = 0;
    private int mSecondBeginPos = 0;
    private int mCtrlCodePos = 0;
    private int mDataLengthPos = 0;
    private int mCsPos = 0;
    private int mEndPos = 0;

    private Protocol645FrameBaseParser() {
    }

    private static class InstanceHolder {
        private static final Protocol645FrameBaseParser INSTANCE = new Protocol645FrameBaseParser();
    }

    public static Protocol645FrameBaseParser getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public Protocol645Frame parse(byte[] frame) {
        Log.i(TAG, "parse");
        Map map = new HashMap();
        Protocol645Frame protocol645Frame = new Protocol645Frame();

        if (verify645Frame(frame)) {
            Log.i(TAG, "parse, frame verify OK");
            byte ctrlCode = frame[mCtrlCodePos];
            protocol645Frame.mCtrlCode = ctrlCode;
            protocol645Frame.mAddressArea = DataConvertUtils.getSubByteArray(frame, mFirstBeginPos + 1, mSecondBeginPos - 1);
            protocol645Frame.mDataLength = frame[mDataLengthPos];
            byte[] data = DataConvertUtils.getSubByteArray(frame, mDataLengthPos + 1, mCsPos - 1);
            if (data != null && data.length > 0) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (data[i] - (byte) 0x33);
                }
            }
            protocol645Frame.mData = data;
            protocol645Frame.mCs = frame[mCsPos];
            return protocol645Frame;
        } else {
            return null;
        }
    }

    public String parseDetail(Protocol645Frame protocol645Frame) {
        if (protocol645Frame == null) {
            return null;
        }

        switch (protocol645Frame.mCtrlCode) {
            case (byte) 0x91: //07规范读数据，从站正常应答
                if (protocol645Frame.mData == null || protocol645Frame.mData.length < 4) {
                    return null;
                }
                String dataLable = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 0, 3, true);
                Log.i(TAG, "parseDetail, dataLable: " + dataLable);
                switch (dataLable.toUpperCase()) {
                    case "00000000": //(当前)组合有功总电能
                    case "00000100": //(当前)组合有功费率 1 电能
                    case "00000200": //(当前)组合有功费率 2 电能
                    case "00000300": //(当前)组合有功费率 3 电能
                    case "00000400": //(当前)组合有功费率 4 电能
                    case "00010000": //(当前)正向有功总电能
                    case "00010100": //(当前)正向有功费率 1 电能
                    case "00010200": //(当前)正向有功费率 2 电能
                    case "00010300": //(当前)正向有功费率 3 电能
                    case "00010400": //(当前)正向有功费率 4 电能
                    case "00010001": //(上 1 结算日)正向有功总电能
                    case "00010101": //(上 1 结算日)正向有功费率 1 电能
                    case "00010201": //(上 1 结算日)正向有功费率 2 电能
                    case "00010301": //(上 1 结算日)正向有功费率 3 电能
                    case "00010401": //(上 1 结算日)正向有功费率 4 电能
                    case "00020000": //(当前)反向有功总电能
                    case "00020100": //(当前)反向有功费率 1 电能
                    case "00020200": //(当前)反向有功费率 2 电能
                    case "00020300": //(当前)反向有功费率 3 电能
                    case "00020400": //(当前)反向有功费率 4 电能
                    case "00020001": //(上 1 结算日)反向有功总电能
                    case "00020101": //(上 1 结算日)反向有功费率 1 电能
                    case "00020201": //(上 1 结算日)反向有功费率 2 电能
                    case "00020301": //(上 1 结算日)反向有功费率 3 电能
                    case "00020401": //(上 1 结算日)反向有功费率 4 电能
                    case "00030000": //(当前)组合无功 1（正向无功） 总电能
                    case "00030100": //(当前)组合无功 1（正向无功） 费率 1 电能
                    case "00030200": //(当前)组合无功 1（正向无功） 费率 2 电能
                    case "00030300": //(当前)组合无功 1（正向无功） 费率 3 电能
                    case "00030400": //(当前)组合无功 1（正向无功） 费率 4 电能
                    case "00030001": //(上 1 结算日)组合无功 1（正向无功） 总电能
                    case "00030101": //(上 1 结算日)组合无功 1（正向无功）费率 1 电能
                    case "00030201": //(上 1 结算日)组合无功 1（正向无功）费率 2 电能
                    case "00030301": //(上 1 结算日)组合无功 1（正向无功）费率 3 电能
                    case "00030401": //(上 1 结算日)组合无功 1（正向无功）费率 4 电能
                    case "00040000": //(当前)组合无功 2（反向无功） 总电能
                    case "00040100": //(当前)组合无功 2（反向无功）费率 1 电能
                    case "00040200": //(当前)组合无功 2（反向无功）费率 2 电能
                    case "00040300": //(当前)组合无功 2（反向无功）费率 3 电能
                    case "00040400": //(当前)组合无功 2（反向无功）费率 4 电能
                    case "00040001": //(上 1 结算日)组合无功 2（反向无功） 总电能
                    case "00040101": //(上 1 结算日)组合无功 2（反向无功） 费率 1 电能
                    case "00040201": //(上 1 结算日)组合无功 2（反向无功） 费率 2 电能
                    case "00040301": //(上 1 结算日)组合无功 2（反向无功） 费率 3 电能
                    case "00040401": //(上 1 结算日)组合无功 2（反向无功） 费率 4 电能
                        if (protocol645Frame.mData.length < 8) {
                            return null;
                        } else {
                            return parseXXXXXX_XX(protocol645Frame.mData, 4);
                        }
                    case "01010000": //(当前)正向有功总最大需量及发生时间
                    case "01010001": //(上 1 结算日)正向有功总最大需量及发生时间
                        if (protocol645Frame.mData.length < 12) {
                            return null;
                        } else {
                            return parse_XX_XXXX_YYMMDDhhmm(protocol645Frame.mData, 4);
                        }
                   // case "05060101": //（上 1 次）日冻结正向有功电能数据：正向有功总电能/正向有功费率 1 电能/.../正向有功费率 63 电能
                    case "05060201": //（上 1 次）日冻结反向有功电能数据：反向有功总电能/反向有功费率 1 电能/.../反向有功费率 63 电能
                    case "05060301": //（上 1 次）日冻结组合无功 1（正向无功）电能数据：组合无功 1 总电能/组合无功 1 费率 1 电能/.../组合无功 1 费率 63 电能
                    case "05060401": //（上 1 次）日冻结组合无功 2（反向无功）电能数据：组合无功 2 总电能/组合无功 2 费率 1 电能/.../组合无功 2 费率 63 电能
                        return parse_N_XXXXXX_XX(protocol645Frame.mData, 4);
                    case "05060901": // (上 1 次）日冻结正向有功最大需量及发生时间数据：
                                     // 正向有功总最大需量及发生时间/正向有功费率 1 最大需量及发生时间/.../正向有功费率 63 最大需量及发生时间
                        return parse_N_XX_XXXX_YYMMDDhhmm(protocol645Frame.mData, 4);
                    case "04000101": //日期及星期
                        return parse_YYMMDDWW(protocol645Frame.mData, 4);
                    case "04000102": //时间
                        return parse_hhmmss(protocol645Frame.mData, 4);
                    case "0400010C":
                        return parse_YYMMDDWWhhmmss(protocol645Frame.mData, 4);
                    default:
                        //return null;

                }
                switch (dataLable.toUpperCase().substring(0, 6)) {
                    case "050600": //日冻结时间
                        return parse_YYMMDDhhmm(protocol645Frame.mData, 4);
                    case "050601": //日冻结正向有功电能数据
                        return parse_N_XXXXXX_XX(protocol645Frame.mData, 4);
                }
            case (byte) 0xD1: //07规约，读数据，从站异常应答
                return null;
            case (byte) 0x81: //97规约，读数据，从站正常应答
                if (protocol645Frame.mData == null || protocol645Frame.mData.length < 2) {
                    return null;
                }
                String dataLable97 = DataConvertUtils.convertByteArrayToString(protocol645Frame.mData, 0, 1, true);
                Log.i(TAG, "parseDetail, dataLable97: " + dataLable97);
                switch (dataLable97.toUpperCase()) {
                    case "9010": //(当前)正向有功总电能(+A)
                    case "9011": //(当前)费率 1 正向有功电能
                    case "9012": //(当前)费率 2 正向有功电能
                    case "9013": //(当前)费率 3 正向有功电能
                    case "9014": //(当前)费率 4 正向有功电能
                    case "9410": //(上月)正向有功总电能(+A)
                    case "9411": //(上月)费率 1 正向有功电能
                    case "9412": //(上月)费率 2 正向有功电能
                    case "9413": //(上月)费率 3 正向有功电能
                    case "9414": //(上月)费率 4 正向有功电能
                    case "9020": //(当前)反向有功总电能(-A)
                    case "9021": //(当前)费率 1 反向有功电能
                    case "9022": //(当前)费率 2 反向有功电能
                    case "9023": //(当前)费率 3 反向有功电能
                    case "9024": //(当前)费率 4 反向有功电能
                    case "9420": //(上月)反向有功总电能(-A)
                    case "9421": //(上月)费率 1 反向有功电能
                    case "9422": //(上月)费率 2 反向有功电能
                    case "9423": //(上月)费率 3 反向有功电能
                    case "9424": //(上月)费率 4 反向有功电能
                    case "9110": //(当前)正向无功总电能(+RL， +RC)
                    case "9111": //(当前)费率 1 正向无功电能
                    case "9112": //(当前)费率 2 正向无功电能
                    case "9113": //(当前)费率 3 正向无功电能
                    case "9114": //(当前)费率 4 正向无功电能
                    case "9510": //(上月)正向无功总电能(+RL， +RC)
                    case "9511": //(上月)费率 1 正向无功电能
                    case "9512": //(上月)费率 2 正向无功电能
                    case "9513": //(上月)费率 3 正向无功电能
                    case "9514": //(上月)费率 4 正向无功电能
                    case "9120": //(当前)反向无功总电能(-RL， -RC)
                    case "9121": //(当前)费率 1 反向无功电能
                    case "9122": //(当前)费率 2 反向无功电能
                    case "9123": //(当前)费率 3 反向无功电能
                    case "9124": //(当前)费率 4 反向无功电能
                    case "9520": //(上月)反向无功总电能(-RL， +RC)
                    case "9521": //(上月)费率 1 反向无功电能
                    case "9522": //(上月)费率 2 反向无功电能
                    case "9523": //(上月)费率 3 反向无功电能
                    case "9524": //(上月)费率 4 反向无功电能
                        if (protocol645Frame.mData.length < 6) {
                            return null;
                        } else {
                            return parseXXXXXX_XX(protocol645Frame.mData, 2);
                        }
                    case "A010": //(当前)正向有功总最大需量
                    case "A410": //(上月)正向有功总最大需量
                        if (protocol645Frame.mData.length < 5) {
                            return null;
                        } else {
                            return parseXX_XXXX(protocol645Frame.mData, 2);
                        }
                    case "C010": //日期周次
                        return parse_YYMMDDWW(protocol645Frame.mData, 2);
                    case "C011": //时间
                        return parse_hhmmss(protocol645Frame.mData, 2);
                    default:
                        return null;
                }
            case (byte) 0xC1: //97规约，读数据，从站异常应答
                break;
            default:
                return null;
        }
        return null;
    }

    private String parseXXXXXX_XX(byte[] data, int dataBegin) {
        if (data == null || data.length <= 0
                || dataBegin >= data.length
                || (dataBegin + 3) >= data.length) {
            return null;

        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            byte byteData = data[dataBegin + i];
            sb.insert(0, DataConvertUtils.convertByteToString(byteData));
            if (i == 0) {
                sb.insert(0, ".");
            }
        }
        return sb.toString();
    }

    private String parseXX_XXXX(byte[] data, int dataBegin) {
        if (data == null || data.length <= 0
                || dataBegin >= data.length
                || (dataBegin + 2) >= data.length) {
            return null;

        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            byte byteData = data[dataBegin + i];
            sb.insert(0, DataConvertUtils.convertByteToString(byteData));
            if (i == 1) {
                sb.insert(0, ".");
            }
        }
        return sb.toString();
    }

    //split by one blank space
    private String parse_N_XXXXXX_XX(byte[] data, int dataBegin) {
        if (data == null || dataBegin > data.length - 1) {
            return null;
        }
        int dataCnt = (data.length - dataBegin) / 4;
        if (dataCnt <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataCnt; i++) {
            sb.append(DataConvertUtils.convertByteToString(data[dataBegin + i * 4 + 3]));
            sb.append(DataConvertUtils.convertByteToString(data[dataBegin + i * 4 + 3 - 1]));
            sb.append(DataConvertUtils.convertByteToString(data[dataBegin + i * 4 + 3 - 2]));
            sb.append(".");
            sb.append(DataConvertUtils.convertByteToString(data[dataBegin + i * 4 + 3 - 3]));
            sb.append(" ");
        }
        return sb.toString();
    }

    //split by one blank space
    private String parse_XX_XXXX_YYMMDDhhmm(byte[] data, int dataBegin) {
        if (data == null || dataBegin + 7 > data.length - 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder dataSb = new StringBuilder();
        dataSb.insert(0, DataConvertUtils.convertByteToString(data[dataBegin]))
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 1]))
                .insert(0, ".")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 2]));

        StringBuilder dateSb = new StringBuilder();
        dateSb.insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 3]))
                .insert(0, ":")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 4]))
                .insert(0, " ")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 5]))
                .insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 6]))
                .insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 7]));
        sb.append(dataSb.toString()).append(" ").append(dateSb.toString());
        return sb.toString();
    }

    //YY-MM-DD WW
    private String parse_YYMMDDWW(byte[] data, int dataBegin) {
        if (data == null || dataBegin > data.length - 1 || (dataBegin + 3) > (data.length - 1)) {
            return null;
        }
        StringBuilder dateSb = new StringBuilder();
        dateSb.insert(0, DataConvertUtils.convertByteToString(data[dataBegin]))
                .insert(0, " ")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 1]))
                .insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 2]))
                .insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 3]));

        return dateSb.toString();
    }

    //hh:mm:ss
    private String parse_hhmmss(byte[] data, int dataBegin) {
        if (data == null || dataBegin > data.length - 1 || (dataBegin + 2) > (data.length - 1)) {
            return null;
        }
        StringBuilder timeSb = new StringBuilder();
        timeSb.insert(0, DataConvertUtils.convertByteToString(data[dataBegin]))
                .insert(0, ":")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 1]))
                .insert(0, ":")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 2]));
        return timeSb.toString();
    }

    private String parse_YYMMDDWWhhmmss(byte[] data, int dataBegin) {
        Log.i(TAG, "parse_YYMMDDWWhhmmss, data: " + DataConvertUtils.convertByteArrayToString(data, false));
        if (data == null || dataBegin > data.length - 1 || (dataBegin + 6) > (data.length - 1)) {
            return null;
        }
        StringBuilder timeSb = new StringBuilder();
        timeSb.insert(0, DataConvertUtils.convertByteToString(data[dataBegin]))
                .insert(0, ":")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 1]))
                .insert(0, ":")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 2]))
                .insert(0, " ")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 3]))
                .insert(0, " ")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 4]))
                .insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 5]))
                .insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 6]));
        return timeSb.toString();
    }

    private String parse_YYMMDDhhmm(byte[] data, int dataBegin) {
        Log.i(TAG, "parse_YYMMDDhhmm, data: " + DataConvertUtils.convertByteArrayToString(data, false));
        if (data == null || dataBegin > data.length - 1 || (dataBegin + 4) > (data.length - 1)) {
            return null;
        }
        StringBuilder timeSb = new StringBuilder();
        timeSb.insert(0, DataConvertUtils.convertByteToString(data[dataBegin]))
                .insert(0, ":")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 1]))
                .insert(0, " ")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 2]))
                .insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 3]))
                .insert(0, "-")
                .insert(0, DataConvertUtils.convertByteToString(data[dataBegin + 4]));
        return timeSb.toString();
    }

    // split by one blank space, sub string is split by "_"
    private String parse_N_XX_XXXX_YYMMDDhhmm(byte[] data, int dataBegin) {
        if (data == null || dataBegin > data.length - 1) {
            return null;
        }
        int dataCnt = (data.length - dataBegin) / 8;
        if (dataCnt <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataCnt; i++) {
            int begin =  dataBegin + 8 * i;
            int end = dataBegin + 8 * i + 8 - 1;
            StringBuilder dataSb = new StringBuilder();
            dataSb.insert(0, DataConvertUtils.convertByteToString(data[begin]))
                    .insert(0, DataConvertUtils.convertByteToString(data[begin + 1]))
                    .insert(0, ".")
                    .insert(0, DataConvertUtils.convertByteToString(data[begin + 2]));

            StringBuilder dateSb = new StringBuilder();
            dateSb.insert(0, DataConvertUtils.convertByteToString(data[begin + 3]))
                    .insert(0, ":")
                    .insert(0, DataConvertUtils.convertByteToString(data[begin + 4]))
                    .insert(0, " ")
                    .insert(0, DataConvertUtils.convertByteToString(data[begin + 5]))
                    .insert(0, "-")
                    .insert(0, DataConvertUtils.convertByteToString(data[begin + 6]))
                    .insert(0, "-")
                    .insert(0, DataConvertUtils.convertByteToString(data[begin + 7]));
            sb.append(dataSb.toString()).append("_").append(dateSb.toString()).append(" ");
        }
        return sb.toString();
    }

    private boolean verify645Frame(byte[] frame) {

        // judge the frame length, at least 12
        if (frame == null || frame.length < 12) {
            return false;
        }

        for (mFirstBeginPos = 0; mFirstBeginPos < frame.length; mFirstBeginPos++) {
            if (frame[mFirstBeginPos] == Protocol645Constant.FRAME_BEGIN) {
                break;
            }
        }
        if (mFirstBeginPos == frame.length) {
            return false;
        }

        mSecondBeginPos = mFirstBeginPos + 7;
        Log.i(TAG, "verify645Frame, mSecondBeginPos: " + mSecondBeginPos);
        if (mSecondBeginPos > frame.length - 1) {
            return false;
        }
        if (frame[mSecondBeginPos] != Protocol645Constant.FRAME_BEGIN) {
            return false;
        }

        mCtrlCodePos = mSecondBeginPos + 1;
        Log.i(TAG, "verify645Frame, mCtrlCodePos: " + mCtrlCodePos);
        if (mCtrlCodePos > frame.length - 1) {
            return false;
        }
        mDataLengthPos = mCtrlCodePos + 1;
        Log.i(TAG, "verify645Frame, mDataLengthPos: " + mDataLengthPos);
        if (mDataLengthPos > frame.length - 1) {
            return false;
        }

        int dataLength = frame[mDataLengthPos];

        mCsPos = mDataLengthPos + dataLength + 1;
        Log.i(TAG, "verify645Frame, mCsPos: " + mCsPos);
        if (mCsPos > frame.length - 1) {
            return false;
        }

        mEndPos =  mCsPos + 1;
        Log.i(TAG, "verify645Frame, mEndPos: " + mEndPos);
        if (mEndPos > frame.length - 1) {
            return false;
        }
        Log.i(TAG, "verify645Frame, mEndCode: " + frame[mEndPos]);
        if (frame[mEndPos] != Protocol645Constant.FRAME_END) {
            return false;
        }

        byte csValue = frame[mFirstBeginPos];

        for (int i = mFirstBeginPos + 1; i <= mCsPos - 1; i++) {
            csValue = (byte) (csValue + frame[i]);
        }
        Log.i(TAG, "verify645Frame, calculate csValue: " + csValue + ", cs in frame: " + frame[mCsPos]);
        if (csValue != frame[mCsPos]) {
            return false;
        }

        return true;
    }
}
