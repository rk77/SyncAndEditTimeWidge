package com.rk.commonmodule.jni;

import java.io.File;
import java.io.IOException;

/**
 * Created by gaofengx
 */

public class JniMethods {
    /**
     * @author gaofengx
     * @createon 2019-11-11 6:06
     * @Describe load lib
     */
    static {
        System.loadLibrary ("native-lib");
    }

    public static void initPort(File device) throws SecurityException, IOException {
		/* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
				/* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("su");
                String cmd = "chmod 777 " + device.getAbsolutePath() + "\n"
                    + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                    || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }
    }


    public static native int tryOpenTty ();
    public static native int ttyClose ();

    public static native int writeMGR(byte[] bytesArr,int len);
    public static native int readMGR(byte[] bytesArr,int len);

    public static native int read485(byte[] bytesArr,int len);
    public static native int write485(byte[] bytesArr,int len);

    public static native int readIR(byte[] bytesArr,int len);
    public static native int writeIR(byte[] bytesArr,int len);



    //网口操作部分
    public static native int openEth ();
    public static native int closeEth ();
    public static native int readEth(byte[] bytesArr,int len);
    public static native int writeEth(byte[] bytesArr,int len);
    //配置网口参数
    //设置本地IP地址
    public static native int setLocalIp(int ip);
    //设置本地端口或者远程端口
    public static native int setNetPort(int port);
    //协议类型
    public static native int setProtocolType(int protocol_type);
    //设置本机工作模式
    public static native int setNetMode(int mode);
    //设远程IP地址
    public static native int setRemoteIp(int remote_ip);
    //读取本地IP地址
    public static native int getLocalIp();
    //读取子网掩码
    public static native int getSubMask();
    //读取网关地址
    public static native int getGateway();
    //获取协议类型
    public static native int getProtocolType();
    //获取本机工作模式
    public static native int getNetMode();
    //获取端口
    public static native int getNetPort();
    //获取远端IP地址
    public static native int getRemoteIp();


    //载波操作
    //载波口  打开
    public static native int ttyUSBOpen (int baud);
    //载波口  关闭
    public static native int ttyUSBClose ();
    //载波口  发送
    public static native int ttyUSBWrite(byte[] bytesArr,int len);
    //载波口  读取
    public static native int ttyUSBRead(byte[] bytesArr,int len);

    //Lora
    public static native int loraOpen();
    public static native int loraClose();
    public static native int loraWrite(byte[] bytesArr,int len);
    public static native int loraRead(byte[] bytesArr,int len);
    public static native int loraSetFrequency(int freq);
    public static native int loraSetPower(int value);
    public static native int loraSetBandWidth(int value);
    public static native int loraSetSpreadingFactor(int value);
    public static native int loraSetErrorCoding(int value);
    public static native int loraSetPacketCrcOn(int value);
    public static native int loraSetPreambleLength(int value);
    public static native int loraSetImplicitHeaderOn(int value);
    public static native int loraSetPayloadLength(int value);
    public static native int loraSetPaRamp(int value);
    public static native int loraSetLowDatarateOptimize(int value);
    public static native int loraSetSymbTimeOut(int value);
    public static native int loraGetBandWidth();
    public static native int loraGetSpreadingFactor();
    public static native int loraGetFrequency();

    // Security Unit
    public static native int initSecurityUnit();
    public static native int deinitSecurityUnit();
    public static native int writeSecurityUnit(byte[] bytesArr,int len);
    public static native int readSecurityUnit(byte[] bytesArr,int len);



}
