package com.rk.commonmodule.protocol.protocol698;

import android.content.Intent;

import java.nio.charset.Charset;

public class TopscommUtils {
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String byteArrayToHexString(byte[] b){
        StringBuilder resultSb = new StringBuilder();
        for (int i = 0; i < b.length;i++){
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    //去掉字符串中的空格
    public static String removeSpaces(String inputMessage){
        String message = inputMessage.trim();
        message = message.replaceAll(" ","");
        return message;
    }

    //16进制字符串，按字节发送先后顺序反转
    public static String getReverseHexString(String hexString){
        String reverseString = "";
        for(int i=hexString.length()/2;i>0;i--){
            reverseString = reverseString + hexString.substring(2*(i-1),2*i);
        }
        return reverseString;
    }

    //整型转两字节16进制数的字符串
    public static String getTwoBytesHexStr(int i){
        String hexBytes = "";
        if(i<=15){
            hexBytes = "000" + Integer.toHexString(i).toUpperCase();
        }else if(i<=255){
            hexBytes = "00" + Integer.toHexString(i).toUpperCase();
        }else if(i<=4095){
            hexBytes = "0" + Integer.toHexString(i).toUpperCase();
        }else if(i<=65535){
            hexBytes = Integer.toHexString(i).toUpperCase();
        }else {
            hexBytes = "FFFF";
        }
        return hexBytes;
    }

    //整型转1字节16进制数的字符串
    public static String getOneByteHexStr(int i){
        String hexByte = "";
        if(i<=15){
            hexByte = "0" + Integer.toHexString(i);
        }else if(i<=255){
            hexByte = Integer.toHexString(i);
        }else {
            hexByte = "FF";
        }
        return hexByte;
    }

    //获取字节数组中的子数组(含头和尾，数组索引从0开始)
    public static byte[] getSubByteArray(byte[] bytes,int start,int end){
        if(end >= start){
            int subByteArrayLen = end - start + 1;
            byte[] subByteArray = new byte[subByteArrayLen];
            for(int i=0;i<subByteArrayLen;i++){
                subByteArray[i] = bytes[start+i];
            }
            return subByteArray;
        }else {
            return new byte[1];
        }

    }

    //获取字节数组通过ASCII码表对应的字符串
    public static String getByteArray2AsciiString(byte[] data){
        String asciiString = "";
        if(!(data.length == 0)){
            return new String(data, Charset.forName("UTF-8"));
        }else {
            return asciiString;
        }
    }

    //获取字符串通过ASCII码表对应的字节数组
    public static byte[] getAsciiString2ByteArray(String str){
        if(str.equals("")){
            byte[] byteArray = {0};
            return byteArray;
        }else {
            try {
                return str.getBytes("UTF-8");
            }catch (Exception e){

            }
        }
        byte[] byteArray = {0};
        return byteArray;
    }
}


