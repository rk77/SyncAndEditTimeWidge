package com.rk.commonmodule.utils;

import android.util.Log;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class DataConvertUtils {
    private static final String TAG = DataConvertUtils.class.getSimpleName();

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

}
