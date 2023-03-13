package com.rk.commonmodule.utils;

import android.text.TextUtils;
import android.util.Log;

import com.rk.commonlib.util.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class DataConvertUtils {
    private static final String TAG = DataConvertUtils.class.getSimpleName();

    public static String OAD = "";

    public static final String alignFrontZeroString(String a, int size) {
        if (size <= 0) {
            return "";
        }
        if (!TextUtils.isEmpty(a)) {
            if (a.length() >= size) {
                return a.substring(a.length() - size);
            } else {
                StringBuilder sb = new StringBuilder(a);
                for (int i = 0; i < size - a.length(); i++) {
                    sb.insert(0, "0");
                }
                return sb.toString();
            }

        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                sb.append("0");
            }
            return sb.toString();
        }
    }

    public static final float byte4ToFloat(byte[] data, int begin) {
        if (data == null || begin < 0 || data.length - 1 - begin + 1 < 4) {
            return -1f;
        }

        float f = (float) ((data[begin] & 0xFF)
                | ((data[begin + 1] << 8) & 0xFF00)
                | ((data[begin + 2] << 16) & 0xFF0000)
                | ((data[begin + 3] << 24) & 0xFF000000));
        return f;
    }

    public static final String byte4ToFloat_Str(byte[] data, int begin) {
        if (data == null || begin < 0 || data.length - 1 - begin + 1 < 4) {
            return "##";
        }

        try {
            float flt = ByteBuffer.wrap(DataConvertUtils.getSubByteArray(data, begin, begin + 3)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return String.valueOf(flt);
        } catch (Exception e) {
            return "##";
        }
    }

    public static byte[] floatToByte(float v) {
        byte[] data = new byte[4];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(( Float.floatToIntBits(v) >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public static byte[] doubleToByte(double v) {
        byte[] data = new byte[8];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(( Double.doubleToLongBits(v) >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public static final String byte4ToDouble_Str(byte[] data, int begin) {
        if (data == null || begin < 0 || data.length - 1 - begin + 1 < 8) {
            return "##";
        }

        try {
            double flt = ByteBuffer.wrap(DataConvertUtils.getSubByteArray(data, begin, begin + 7)).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            return String.valueOf(flt);
        } catch (Exception e) {
            return "##";
        }
    }

    public static final boolean ByteToBool(byte data) {
        if (data > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static final byte[] BoolToByte(boolean v) {
        if (v) {
            return new byte[]{0x01};
        } else {
            return new byte[]{0x00};
        }
    }

    public static final int ByteToTiny(byte data) {
        return data;
    }

    public static final byte[] TinyToByte(byte v) {
        byte[] data = new byte[1];
        data[0] = v;
        return data;
    }

    public static final int ByteToUTiny(byte data) {
        return (data & 0xFF);
    }

    public static final byte[] UTinyToByte(int v) {
        byte[] data = new byte[1];
        data[0] = (byte) (v & 0xFF);
        return data;
    }

    public static final short ByteToShort(byte[] data) {
        short i = (short) (((data[1] & 0xff) << 8) | (data[0] & 0xff));
        return i;
    }

    public static final byte[] ShortToByte(short v) {
        byte[] data = new byte[2];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(((v) >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public static final int ByteToUShort(byte[] data) {
        int i = (int) ((((data[1] & 0xff) << 8) | (data[0] & 0xff)) & 0xFFFF);
        return i;
    }

    public static final byte[] UShortToByte(int v) {
        byte[] data = new byte[2];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(((v) >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public static final int ByteToInt(byte[] data) {
        int i = (int) ((((data[3] & 0xff) << 24) | ((data[2] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[0] & 0xff)));
        return i;
    }

    public static final byte[] IntToByte(int v) {
        byte[] data = new byte[4];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(((v) >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public static final long ByteToUInt(byte[] data) {
        long i = (long) ((((data[3] & 0xff) << 24) | ((data[2] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[0] & 0xff)) & 0xFFFFFFFF);
        return i;
    }

    public static final byte[] UIntToByte(long v) {
        byte[] data = new byte[4];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(((v) >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public static final long ByteToLong(byte[] data) {
        long i = (long) ((
                ((data[7] & 0xff) << 56)
                | ((data[6] & 0xff) << 48)
                | ((data[5] & 0xff) << 40)
                | ((data[4] & 0xff) << 32)
                | ((data[3] & 0xff) << 24)
                | ((data[2] & 0xff) << 16)
                | ((data[1] & 0xff) << 8)
                | (data[0] & 0xff)));
        return i;
    }

    public static final byte[] LongToByte(long v) {
        byte[] data = new byte[8];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(((v) >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public static final long ByteToUlong(byte[] data) {
        long i = (long) ((
                ((data[7] & 0xff) << 56)
                        | ((data[6] & 0xff) << 48)
                        | ((data[5] & 0xff) << 40)
                        | ((data[4] & 0xff) << 32)
                        | ((data[3] & 0xff) << 24)
                        | ((data[2] & 0xff) << 16)
                        | ((data[1] & 0xff) << 8)
                        | (data[0] & 0xff)));
        return i;
    }

    public static final byte[] UlongToByte(long v) {
        byte[] data = new byte[8];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(((v) >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public static final String ByteToOctStr(byte[] data) {
        return convertByteArrayToString(data, false);
    }

    public static final byte[] OctStrToByte(String octS) {
        return convertHexStringToByteArray(octS, octS.length(), false);
    }

    public static final String ByteToStr(byte[] data) {
        return getByteArray2AsciiString(data, 0, data.length - 1, false);
    }

    public static final byte[] StrToByte(String str) {
        return assicString2ByteArray_end_S(str);
    }

    public static final String convertByteToString(byte data) {
        String item = Integer.toHexString(data & 0xFF);
        if (item == null || item.trim().equals("")) {
            Log.i(TAG, "convertByteToString, data error");
            return null;
        }

        if (item.length() < 2) {
            item = "0" + item;
        }
        return item;
    }

    public static final ArrayList<Byte> convertHexStringToByteArrayList(String hexString, int length, boolean revertTo) {
        Log.i(TAG, "convertHexStringToByteArrayList, hex string: " + hexString + ", length: " + length);
        ArrayList<Byte> byteArray = new ArrayList<>();
        if (hexString == null || hexString.trim().equals("")) {
            Log.i(TAG, "convertHexStringToByteArrayList, no hex string data");
            return null;
        }
        if (length <= 0 || length % 2 != 0) {
            Log.i(TAG, "convertHexStringToByteArrayList, this length value is not suitable");
            return null;
        }
        if (hexString.length() > length) {
            Log.i(TAG, "convertHexStringToByteArrayList, hex string length is too longer");
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(hexString);
        for (int i = 0; i < length - hexString.length(); i++) {
            sb.insert(0, "0");
        }
        String rawHexData = sb.toString();
        Log.i(TAG, "convertHexStringToByteArrayList, hex data: " + rawHexData);

        for (int j = 0; j < length / 2; j++) {
            String item = rawHexData.substring(j * 2, j * 2 + 2);
            char tens = item.charAt(0);
            char ones = item.charAt(1);
            if (isNumber(tens) && isNumber(ones)) {
                if (revertTo) {
                    byteArray.add(0, (byte) Integer.parseInt(item, 16));
                } else {
                    byteArray.add((byte) Integer.parseInt(item, 16));
                }
            } else {
                return null;
            }
        }
        return byteArray;
    }

    private static boolean isNumber(char a) {
        return ((a >= '0' && a <= '9') || (a >= 'a' && a <= 'z') || (a >= 'A' && a <= 'Z'));
    }

    public static final byte[] convertHexStringToByteArray(String hexString, int length, boolean revertTo) {
        Log.i(TAG, "convertHexStringToByteArray, hex string: " + hexString + ", length: " + length);
        ArrayList<Byte> byteArray = convertHexStringToByteArrayList(hexString, length, revertTo);
        if (byteArray == null || byteArray.size() <= 0) {
            return null;
        }
        byte[] data = new byte[byteArray.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = byteArray.get(i);
        }
        return data;
    }

    public static final byte[] getSubByteArray(byte[] data, int begin, int end) {
        if (data == null || data.length <= 0) {
            return null;
        }
        if (begin > end || begin > data.length - 1 || end > data.length - 1) {
            return null;
        }

        byte[] subData = new byte[end - begin + 1];
        for (int i = begin; i <= end; i++) {
            subData[i - begin] = data[i];
        }
        return subData;

    }

    /**
     * @param      begin   the beginning index, inclusive.
     * @param      end     the ending index, inclusive.
     */
    public static final String convertByteArrayToString(byte[] data, int begin, int end, boolean revertTo) {
        if (data == null || data.length <= 0) {
            return null;
        }
        if (begin > end || begin > data.length - 1 || end > data.length - 1) {
            return null;
        }
        byte[] subData = new byte[end - begin + 1];
        for (int i = begin; i <= end; i++) {
            subData[i - begin] = data[i];
        }
        return convertByteArrayToString(subData, revertTo);
    }

    public static final String convertByteArrayToString(byte[] data, boolean revertTo) {
        Log.i(TAG, "convertByteArrayToString");
        if (data == null || data.length <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String item = Integer.toHexString(data[i] & 0xFF);
            if (item == null || item.trim().equals("")) {
                Log.i(TAG, "convertByteArrayToString, data error");
                return null;
            }

            if (item.length() < 2) {
                item = "0" + item;
            }
            if (revertTo) {
                sb.insert(0, item);
            } else {
                sb.append(item);
            }
        }
        return sb.toString();
    }

    public static String reverse(String s, int bits) {
        if (s == null || s.length() <= bits || bits < 1) {
            return s;
        }
        int postBit = s.length() % bits;
        for (int i = 0; i < postBit; i++) {
            s = "0" + s;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length() / bits; i++) {
            sb.insert(0, s.substring(i * bits, (i + 1) * bits));
        }
        return sb.toString();
    }

    public static String InterDivToString(int a, int b) {
        DecimalFormat df=new DecimalFormat("0.00");
        return (df.format((float)a/b));
    }

    public static String InterDivToString(int a, int b, int reserveBit) {
        StringBuilder sb = new StringBuilder();
        sb.append("0").append(".");
        for (int i = 0; i < reserveBit; i++) {
            sb.append("0");
        }
        DecimalFormat df=new DecimalFormat(sb.toString());
        return df.format(a/(double)b);
    }

    public static String StringReserveBit(String a, int reserveBit) {
        StringBuilder sb = new StringBuilder();
        sb.append("0.");
        int b = 1;
        for (int i = 0; i < reserveBit; i++) {
            sb.append("0");
            b = b * 10;
        }
        DecimalFormat df=new DecimalFormat(sb.toString());
        return df.format(Float.parseFloat(a) / b);
    }

    //String or Double conver to String（0.00）
    public static String format2(Object value) {
        Double a = 0.0;
        if (value instanceof String) {
            a = Double.valueOf((String)value);
        } else if (value instanceof Double) {
            a = (Double) value;
        } else {
            return "";
        }
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
        try {
            return df.format(a);
        } catch (IllegalArgumentException e) {
            return "";
        }

    }

    public static String getByteArray2AsciiString(byte[] data, int begin, int end, boolean revertTo){
        if (data == null || data.length <= 0) {
            return "";
        }
        if (begin > end || begin > data.length - 1 || end > data.length - 1) {
            return "";
        }
        byte[] subData = new byte[end - begin + 1];
        if (revertTo) {
            for (int i = 0; i <= subData.length - 1; i++) {
                subData[i] = data[end - i];
            }
        } else {
            for (int i = 0; i <= subData.length - 1; i++) {
                subData[i] = data[begin + i];
            }
        }
        return getByteArray2AsciiString(subData);
    }

    public static String getByteArray2AsciiString(byte[] data){
        String asciiString = "";
        if (data != null && data.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                if (data[i] == (byte) 0) {
                    sb.append("");
                } else {
                    char c = (char) data[i];
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            return asciiString;
        }
    }

    public static byte[] assicString2ByteArray(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        int size = s.length();
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (s.charAt(i));
        }
        return data;
    }

    public static byte[] assicString2ByteArray_end_S(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        int size = s.length();
        byte[] data = new byte[size + 1];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (s.charAt(i));
        }
        data[size] = '\0';
        return data;
    }

    public static boolean isNumberString(String s) {
        if (s != null) {
            int length = s.length();
            for (int i = 0; i < length; i++) {
                if (!((s.charAt(i) >= '0' && s.charAt(i) <= '9')
                        || (s.charAt(i) >= 'a' && s.charAt(i) <= 'f')
                        || (s.charAt(i) >= 'A' && s.charAt(i) <= 'F'))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static String IntToBCDString(int a) {
        String s =String.valueOf(a);
        if (s.length() % 2 != 0)
        {
            s = "0" + s;
        }
        return s;
    }

    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    public static byte setBitVal(byte data, int pos, int value) {
        if (value <= 0) {
            byte mask = (byte) (~(0x01 << pos));
            return (byte)(mask & data);
        } else {
            byte mask = (byte) (0x01 << pos);
            return (byte)(mask | data);
        }
    }

}
