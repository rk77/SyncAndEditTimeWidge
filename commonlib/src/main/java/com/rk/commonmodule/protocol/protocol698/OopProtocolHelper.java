package com.rk.commonmodule.protocol.protocol698;

import android.renderscript.Element;
import android.util.Log;

import com.rk.commonmodule.utils.DataConvertUtils;
import com.rk.commonmodule.utils.MeterProtocolDetector;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.DAR_KEY;
import static com.rk.commonmodule.protocol.protocol698.ProtocolConstant.DATA_KEY;

public class OopProtocolHelper {

    private static final String TAG = OopProtocolHelper.class.getSimpleName();
    private static final String mDeviceAddress = "AAAAAAAAAAAA";
    public static String currentDeviceAddress = "";

    private static String mSendApdu = "";

    private static int[] fcstab = {
            0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
            0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
            0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
            0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
            0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
            0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
            0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
            0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
            0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
            0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
            0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
            0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
            0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
            0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
            0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
            0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
            0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
            0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
            0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
            0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
            0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
            0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
            0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
            0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
            0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
            0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
            0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
            0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
            0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
            0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
            0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
            0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
    };

    public static String makeOopReportFrame(String apduFrame){
        mSendApdu = apduFrame;

        int devid_len = 0; //地址长度
        String buffer = "";   //暂存报文链路层头部
        devid_len = mDeviceAddress.length()/2 ;
        if(devid_len > 10){
            devid_len = 10;
        }
        buffer = buffer + "68$1$243"; //到控制域
        buffer = buffer + "4" + (devid_len  - 1); //添加地址长度
        buffer = buffer + TopscommUtils.getReverseHexString(mDeviceAddress);//添加地址
        buffer = buffer + "10$3$4";  //添加客户机地址CA、HCS(两字节)
        buffer = buffer + apduFrame; //添加应用层apdu
        buffer = buffer + "00";   //时间标签
        buffer = buffer + "$5$6"; //添加FCS校验位
        buffer = buffer + "16";
        int frameLen = buffer.length()/2;
        String len$1$2 = TopscommUtils.getTwoBytesHexStr(frameLen-2);
        buffer = buffer.replace("$1$2",TopscommUtils.getReverseHexString(len$1$2));

        int indexOf_$3$4 = buffer.indexOf("$3");
        byte[] subFrame1 = TopscommUtils.hexStringToBytes(buffer.substring(2,indexOf_$3$4));
        String verify_HCS = getVerifyHexString(subFrame1);
        buffer = buffer.replace("$3$4",TopscommUtils.getReverseHexString(verify_HCS));

        int indexOf_$5$6 = buffer.indexOf("$5");
        byte[] subFrame2 = TopscommUtils.hexStringToBytes(buffer.substring(2,indexOf_$5$6));
        String verify_FCS = getVerifyHexString(subFrame2);
        buffer = buffer.replace("$5$6",TopscommUtils.getReverseHexString(verify_FCS));
        return buffer;
    }

    //计算校验位
    private static String getVerifyHexString(byte[] subFrame){
        String verify = "";
        int trialfcs = pppfcs16(65535,subFrame);
        trialfcs = trialfcs ^ 0xFFFF ;
        verify = TopscommUtils.getTwoBytesHexStr(trialfcs);
       return verify;
    }

    private static int pppfcs16(int fcs,byte[] subFrame){
        int len = subFrame.length;
        int index = 0;
        while (len>0){
            len--;
            fcs = (fcs >> 8) ^ fcstab[(fcs ^ subFrame[index++]) & 0xFF ];
        }
        return fcs;
    }

    //解析函数
    /**
     *
     * @param frameString 待解析的上行报文
     * @return 返回解析结果（String集合）
     * 集合第0个位置存放帧长度、HCS、FCS校验结果，第1个位置存放设备地址，
     * 第2个位置存放下行报文合法性判断结果或APDU对比结果（对比正确的话就存入APDU服务类型），第3个位置存放OAD，后面的位置存
     * 放根据各自的功能决定
     */
    public static List<String> parseFrame(String frameString){
        List<String> parseResult = new ArrayList<>();
        try {
            byte[] frame = TopscommUtils.hexStringToBytes(frameString);
            LogUtil.d("实际不含起始、结束字符的帧长度",(frame.length-2)+"");
            byte frameLenByte_H = frame[2];  //长度域高字节
            byte[] frameLenBytes = TopscommUtils.getSubByteArray(frame,1,2);
            String frameLenHexString = TopscommUtils.getReverseHexString(TopscommUtils.byteArrayToHexString(frameLenBytes));
            int frameLen;
            if((frameLenByte_H & 0x40) == 0x40){
                //千字节
                frameLen = 1000*(Integer.parseInt(frameLenHexString,16) & 0x3FFF);
            }else{
                //字节
                frameLen = Integer.parseInt(frameLenHexString,16) & 0x3FFF;
            }
            LogUtil.d("长度域计算的帧长度",frameLen+"");
            //计算对比帧长度
            boolean frameLenPass = (frame.length == (frameLen + 2));
            int addressLen = (frame[4] & 0x0F) + 1; //地址长度
            byte[] addrssReserve = TopscommUtils.getSubByteArray(frame,5,5+addressLen-1);
            String address = TopscommUtils.getReverseHexString(TopscommUtils.byteArrayToHexString(addrssReserve));//获取设备地址
            currentDeviceAddress = address;
            //计算并对比HCS
            String hcs_HexString = getVerifyHexString(TopscommUtils.getSubByteArray(frame,1,5+addressLen));
            hcs_HexString = TopscommUtils.getReverseHexString(hcs_HexString);//字节高低位顺序交换
            LogUtil.d("hcs_HexString",hcs_HexString);
            boolean hcsPass = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(frame,6+addressLen,7+addressLen)).equals(hcs_HexString);
            LogUtil.d("HCS",TopscommUtils.byteArrayToHexString(TopscommUtils.getSubByteArray(frame,6+addressLen,7+addressLen)));
            String fcs_HexString = getVerifyHexString(TopscommUtils.getSubByteArray(frame,1,frame.length-4));
            fcs_HexString = TopscommUtils.getReverseHexString(fcs_HexString);//字节高低位顺序交换
            LogUtil.d("fcs_HexString",fcs_HexString);
            boolean fcsPass = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(frame,frame.length-3,frame.length-2)).equals(fcs_HexString);
            LogUtil.d("FCS",TopscommUtils.byteArrayToHexString(TopscommUtils.getSubByteArray(frame,frame.length-3,frame.length-2)));
            if(!(hcsPass && fcsPass && frameLenPass)){
                //HCS和FCS校验不通过
                LogUtil.d("帧长度、HCS、FCS校验","不通过");
                parseResult.add("VERIFY_ERROR");
                return parseResult;
            }
            parseResult.add("VERIFY_PASS");
            parseResult.add(address);//设备地址
            //判断下行是否为非法报文
            if(TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(frame,8+addressLen,8+addressLen)).equals("EE")){
                LogUtil.e("下行报文检验","非法！");
                parseResult.add("下行报文非法");//位置2
                parseResult.add("无OAD");
                return parseResult;
            }
            /*-------------------------------APDU部分------------------------------------*/
            byte[] APDU = TopscommUtils.getSubByteArray(frame,8+addressLen,frame.length-4);
            //对比APDU
            if((APDU[1]&0xFF)==0x01){//请求一个对象属性
                byte[] sendApdu = TopscommUtils.hexStringToBytes(mSendApdu.substring(0,14));//发送下行报文时的APDU
                sendApdu[0] = (byte) (sendApdu[0] | 0x80); //例如：下行05 对应上行85
                String sendApduString = TopscommUtils.byteArrayToHexString(sendApdu);
                LogUtil.d("sendApdu",sendApduString);
                byte[] receiveApdu = TopscommUtils.getSubByteArray(APDU,0,6);  //其中OAD固定为4字节
                String receiveApduString = TopscommUtils.byteArrayToHexString(receiveApdu);
                LogUtil.d("receiveApdu",receiveApduString);
                if(!sendApduString.equals(receiveApduString)){
                    //对比APDU不通过
                    LogUtil.d("APDU比对","不通过");
                    parseResult.add("APDU_ERROR");
                    return parseResult;
                }
                parseResult.add(TopscommUtils.byteArrayToHexString(
                        TopscommUtils.getSubByteArray(APDU,0,0)));//APDU对比通过，第二个位置存放APDU服务类型
                //获取OAD
                String oad = TopscommUtils.byteArrayToHexString(
                        TopscommUtils.getSubByteArray(APDU,3,6));
                parseResult.add(oad);  //第三个位置存放OAD
                LogUtil.d("OAD",oad);
                String oi = TopscommUtils.byteArrayToHexString(
                        TopscommUtils.getSubByteArray(APDU,3,4));
                LogUtil.d("OI",oi);
                String objectAttribute = TopscommUtils.byteArrayToHexString(
                        TopscommUtils.getSubByteArray(APDU,5,5));
                LogUtil.d("对象属性",objectAttribute);
            }else {
                //请求多个对象属性

            }
            //根据APDU服务类型区分
            switch ((APDU[0]&0xFF)){
                case 0x85:
                    //读取
                    parseResult = parseApdu_Read(parseResult,APDU);
                    break;
                case 0x86:
                    //设置
                    parseResult = parseApdu_Set(parseResult,APDU);
                    break;
                case 0x87:
                    //操作
                    parseResult = parseApdu_Action(parseResult,APDU);
                    break;
                default:
                    break;
            }
        }catch (Exception e){
           e.printStackTrace();
        }
        return parseResult;
    }

    //APDU服务类型为操作
    private static List<String> parseApdu_Action(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        if((APDU[1]&0xFF)==0x01){  //请求操作一个对象方法
            switch (parseResult.get(3)){
                case "FFFD0A00":
                    parseResult = parseFrame_Action_FFFD0A00(parseResult,APDU);
                    break;
                default:
                    break;
            }

        }else {

        }
        return parseResult;
    }

    private static List<String> parseFrame_Action_FFFD0A00(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        //操作执行结果DAR
        int dar = APDU[7] & 0xFF;
        if(dar == 0){
            //操作成功
            parseResult.add("操作成功！");//位置4
        }else{
            //操作失败
            parseResult.add("操作失败！（DAR："+dar+"）");//位置4
        }
        return parseResult;
    }


    //APDU服务类型为读取
    private static List<String> parseApdu_Read(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        if((APDU[1]&0xFF)==0x01){//请求读取一个对象属性
            switch (parseResult.get(3)){
                case "43000300":
                    parseResult = parseFrame_Read_43000300(parseResult,APDU);
                    break;
                case "40000200":
                    parseResult = parseFrame_Read_40000200(parseResult,APDU);
                    break;
                case "40010200":
                    parseResult = parseFrame_Read_40010200(parseResult,APDU);
                    break;
                case "45000200":
                    parseResult = parseFrame_Read_45000200(parseResult,APDU);
                    break;
                case "45000300":
                    parseResult = parseFrame_Read_45000300(parseResult,APDU);
                    break;
                case "45100200":
                    parseResult = parseFrame_Read_45100200(parseResult,APDU);
                    break;
                case "45100300":
                    parseResult = parseFrame_Read_45100300(parseResult,APDU);
                    break;
                case "45100400":
                    parseResult = parseFrame_Read_45100400(parseResult,APDU);
                    break;
                default:
                    break;
            }
        }else {

        }
        return parseResult;
    }

    private static List<String> parseFrame_Read_45000300(List<String> resultList,byte[] APDU){
        List<String> parseResult = parseMasterStationCommunicationParam(resultList,APDU);
        return parseResult;
    }

    private static List<String> parseFrame_Read_45100300(List<String> resultList,byte[] APDU){
        List<String> parseResult = parseMasterStationCommunicationParam(resultList,APDU);
        return parseResult;
    }

    //供主站通信参数读取功能使用，即读取45000300、45100300
    private static List<String> parseMasterStationCommunicationParam(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        //Get-Result：0表示错误信息，1表示数据
        int getResult = APDU[7] & 0xFF;
        if(getResult == 0){
            parseResult.add("DAR");//错误信息，位置4
            parseResult.add(""+APDU[8]);//错误码，位置5
            return parseResult;
        }else if (getResult == 1){
            parseResult.add("Data");//数据信息，位置4
            String dataType = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,8,8));
            LogUtil.d("数据类型",dataType);
            int dataTypeNum = APDU[9] & 0xFF;
            LogUtil.d("数据类型"+dataType+"下的个数",dataTypeNum+"");
            int index = 10; //数据内容在APDU中的索引
            if(dataTypeNum == 0){
                parseResult.add("无数据");//位置5，无数据
                return parseResult;
            }else if(dataTypeNum == 1){
                index = addIp_Port(index,APDU,parseResult);
                parseResult.add("无IP");//位置7，IP2
                parseResult.add("无端口");//位置8，端口2
                parseResult.add("无IP");//位置9，IP3
                parseResult.add("无端口");//位置10，端口3
                parseResult.add("无IP");//位置11，IP4
                parseResult.add("无端口");//位置12，端口4
            }else if(dataTypeNum == 2){
                index = addIp_Port(index,APDU,parseResult);
                index = addIp_Port(index,APDU,parseResult);
                parseResult.add("无IP");//位置9，IP3
                parseResult.add("无端口");//位置10，端口3
                parseResult.add("无IP");//位置11，IP4
                parseResult.add("无端口");//位置12，端口4
            }else if(dataTypeNum == 3){
                index = addIp_Port(index,APDU,parseResult);
                index = addIp_Port(index,APDU,parseResult);
                index = addIp_Port(index,APDU,parseResult);
                parseResult.add("无IP");//位置11，IP4
                parseResult.add("无端口");//位置12，端口4
            }else {
                index = addIp_Port(index,APDU,parseResult);
                index = addIp_Port(index,APDU,parseResult);
                index = addIp_Port(index,APDU,parseResult);
                index = addIp_Port(index,APDU,parseResult);
            }
            parseResult.add(dataTypeNum+"");//位置13，Ip和端口个数
            LogUtil.d("IP1",parseResult.get(5));
            LogUtil.d("端口1",parseResult.get(6));
            LogUtil.d("IP2",parseResult.get(7));
            LogUtil.d("端口2",parseResult.get(8));
            LogUtil.d("IP3",parseResult.get(9));
            LogUtil.d("端口3",parseResult.get(10));
            LogUtil.d("IP4",parseResult.get(11));
            LogUtil.d("端口4",parseResult.get(12));
            return parseResult;
        }else {
            parseResult.add("未知信息");//位置4
            parseResult.add("未知信息");//位置5
            return parseResult;
        }
    }

    //供主站通信参数，添加IP和端口使用
    private static int addIp_Port(int index,byte[] APDU,List<String > parseResult){
        if((APDU[index+2]&0xFF)==0){
            parseResult.add("无IP");//IP
            if((APDU[index+3]&0xFF)==0){
                parseResult.add("无端口");//端口
                index += 4;
            }else if((APDU[index+3]&0xFF)==0x12){
                int port = (((APDU[index+4]&0xFF)<<8) + (APDU[index+5]&0xFF))&0xFFFF;
                parseResult.add(port+"");//端口
                index += 6;
            }
        }else if((APDU[index+2]&0xFF)==0x09){
            if((APDU[index+3]&0xFF)==0x00){
                parseResult.add("无IP");//IP
                if((APDU[index+4]&0xFF)==0){
                    parseResult.add("无端口");//端口
                    index += 5;
                }else if((APDU[index+4]&0xFF)==0x12){
                    int port = (((APDU[index+5]&0xFF)<<8) + (APDU[index+6]&0xFF))&0xFFFF;
                    parseResult.add(port+"");//端口
                    index += 7;
                }
            }else if((APDU[index+3]&0xFF)==0x04){
                int IP_1 = APDU[index+4] & 0xFF;
                int IP_2 = APDU[index+5] & 0xFF;
                int IP_3 = APDU[index+6] & 0xFF;
                int IP_4 = APDU[index+7] & 0xFF;
                parseResult.add(IP_1+"."+IP_2+"."+IP_3+"."+IP_4);//IP
                if((APDU[index+8]&0xFF)==0){
                    parseResult.add("无端口");//端口
                    index += 9;
                }else if((APDU[index+8]&0xFF)==0x12){
                    int port = (((APDU[index+9]&0xFF)<<8) + (APDU[index+10]&0xFF))&0xFFFF;
                    parseResult.add(port+"");//端口
                    index += 11;
                }
            }
        }
        //LogUtil.d("List_Size",parseResult.size()+"");
        return index;
    }


    private static List<String> parseFrame_Read_45100400(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        //Get-Result：0表示错误信息，1表示数据
        int getResult = APDU[7] & 0xFF;
        if(getResult == 0){
            parseResult.add("DAR");//错误信息，位置4
            parseResult.add(""+APDU[8]);//错误码，位置5
            return parseResult;
        }else if(getResult == 1){
            parseResult.add("Data");//数据信息，位置4
            String dataType = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,8,8));
            LogUtil.d("数据类型",dataType);
            int dataTypeNum = APDU[9] & 0xFF;
            LogUtil.d("数据类型"+dataType+"下的个数",dataTypeNum+"");
            if(dataTypeNum == 0){
                parseResult.add("无数据");//位置5，无数据
                return parseResult;
            }
            int index = 10;//数据内容在APDU中的索引，IP配置方式
            if(dataTypeNum>=1){
                if((APDU[index]&0xFF) != 0){
                    switch (APDU[index+1] & 0xFF){
                        case 0:
                            //DHCP
                            parseResult.add("DHCP");//位置5
                            break;
                        case 1:
                            //静态
                            parseResult.add("静态");//位置5
                            break;
                        case 2:
                            //PPPoE
                            parseResult.add("PPPoE");//位置5
                            break;
                        default:
                            break;
                    }
                    index += 2;//IP地址
                }else {
                    parseResult.add("无IP配置方式");//位置5
                    index += 1;
                }
                LogUtil.d("IP配置方式",parseResult.get(5));
            }else {
                parseResult.add("无IP配置方式");//位置5
                return parseResult;
            }
            if(dataTypeNum>=2){
                if((APDU[index]&0xFF) != 0){
                    int ipAddressLen = APDU[index+1] & 0xFF;
                    if(ipAddressLen == 4){
                        int IP_1 = APDU[index+2] & 0xFF;
                        int IP_2 = APDU[index+3] & 0xFF;
                        int IP_3 = APDU[index+4] & 0xFF;
                        int IP_4 = APDU[index+5] & 0xFF;
                        parseResult.add(IP_1+"."+IP_2+"."+IP_3+"."+IP_4);//位置6，IP地址
                        index += 6;//子网掩码
                    }else {
                        parseResult.add(" . . . ");//位置6，IP地址
                        index += (2+ipAddressLen);
                    }
                }else {
                    parseResult.add(" . . . ");//位置6，IP地址
                    index += 1;
                }
                LogUtil.d("IP地址",parseResult.get(6));
            }else {
                parseResult.add("无IP地址");
                return parseResult;
            }
            if(dataTypeNum>=3){
                if((APDU[index]&0xFF) != 0){
                    int subnetMaskLen = APDU[index+1] & 0xFF;
                    if(subnetMaskLen == 4){
                        int IP_1 = APDU[index+2] & 0xFF;
                        int IP_2 = APDU[index+3] & 0xFF;
                        int IP_3 = APDU[index+4] & 0xFF;
                        int IP_4 = APDU[index+5] & 0xFF;
                        parseResult.add(IP_1+"."+IP_2+"."+IP_3+"."+IP_4);//位置7，子网掩码
                        index += 6;//子网掩码
                    }else {
                        parseResult.add(" . . . ");//位置7，子网掩码
                        index += (2+subnetMaskLen);
                    }
                }else {
                    parseResult.add(" . . . ");//位置7，子网掩码
                    index += 1;
                }
                LogUtil.d("子网掩码",parseResult.get(7));
            }else {
                parseResult.add("无子网掩码");//位置7
                return parseResult;
            }
            if(dataTypeNum>=4){
                if((APDU[index]&0xFF) != 0){
                    int gatewayLen = APDU[index+1] & 0xFF;
                    if(gatewayLen == 4){
                        int IP_1 = APDU[index+2] & 0xFF;
                        int IP_2 = APDU[index+3] & 0xFF;
                        int IP_3 = APDU[index+4] & 0xFF;
                        int IP_4 = APDU[index+5] & 0xFF;
                        parseResult.add(IP_1+"."+IP_2+"."+IP_3+"."+IP_4);//位置8，网关地址
                        index += 6;//子网掩码
                    }else {
                        parseResult.add(" . . . ");//位置8，网关地址
                        index += (2+gatewayLen);
                    }
                }else {
                    parseResult.add(" . . . ");//位置8，网关地址
                    index += 1;
                }
                LogUtil.d("网关地址",parseResult.get(8));
            }else {
                parseResult.add("无网关地址");//位置8
                return parseResult;
            }
            if(dataTypeNum>=5){
                if((APDU[index]&0xFF) != 0){
                    int userNameLen = APDU[index+1] & 0xFF;
                    if(userNameLen != 0){
                        byte[] userName = TopscommUtils.getSubByteArray(APDU,index+2,index+1+userNameLen);
                        String userNameString = TopscommUtils.getByteArray2AsciiString(userName);
                        parseResult.add(userNameString);//位置9，PPPoE用户名
                    }else {
                        parseResult.add("");
                    }
                    index += (2+userNameLen);//PPPoE密码
                }else {
                    parseResult.add("");
                    index += 1;
                }
                LogUtil.d("PPPoE用户名",parseResult.get(9));
            }else {
                parseResult.add("无PPPoE用户名");
                return parseResult;
            }
            if(dataTypeNum == 6){
                if((APDU[index]&0xFF) != 0){
                    int keyLen = APDU[index+1] & 0xFF;
                    if(keyLen != 0){
                        byte[] key = TopscommUtils.getSubByteArray(APDU,index+2,index+1+keyLen);
                        String keyString = TopscommUtils.getByteArray2AsciiString(key);
                        parseResult.add(keyString);//位置10，PPPoE密码
                    }else {
                        parseResult.add("");
                    }
                }else {
                    parseResult.add("");
                }
                LogUtil.d("PPPoE密码",parseResult.get(10));
                return parseResult;
            }else{
                parseResult.add("无PPPoE密码");
                return parseResult;
            }

        }else {
            parseResult.add("未知信息");//位置4
            parseResult.add("未知信息");//位置5
            return parseResult;
        }
    }

    private static List<String> parseFrame_Read_45100200(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        //Get-Result: 0表示错误信息，1表示数据
        int getResult = APDU[7] & 0xFF;
        if(getResult == 0){
            parseResult.add("DAR");//错误信息，位置4
            parseResult.add(""+APDU[8]);//错误码，位置5
            return parseResult;
        }else if(getResult == 1){
            parseResult.add("Data");//数据信息，位置4
            String dataType = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,8,8));
            LogUtil.d("数据类型",dataType);
            int dataTypeNum = APDU[9] & 0xFF;
            LogUtil.d("数据类型"+dataType+"下的个数",dataTypeNum+"");
            if(dataTypeNum == 0){
                parseResult.add("无数据");//位置5，无数据
                return parseResult;
            }
            int index = 10;//数据内容在APDU中的索引，工作模式
            if(dataTypeNum>=1){
                if((APDU[index]&0xFF) != 0){
                    switch (APDU[index+1] & 0xFF){
                        case 0:
                            //混合模式
                            parseResult.add("混合模式");//位置5
                            break;
                        case 1:
                            //客户机模式
                            parseResult.add("客户机模式");//位置5
                            break;
                        case 2:
                            //服务器模式
                            parseResult.add("服务器模式");//位置5
                            break;
                        default:
                            parseResult.add("未知！");
                            break;
                    }
                    index += 2;//连接方式
                }else {
                    parseResult.add("无工作模式");//位置5
                    index += 1;
                }
                LogUtil.d("工作模式",parseResult.get(5));
            }else {
                parseResult.add("无工作模式");//位置5
                return parseResult;
            }
            if(dataTypeNum>=2){
                if((APDU[index]&0xFF)!=0){
                    switch (APDU[index+1] & 0xFF){
                        case 0:
                            //TCP
                            parseResult.add("TCP");//位置6
                            break;
                        case 1:
                            //UDP
                            parseResult.add("UDP");//位置6
                            break;
                        default:
                            parseResult.add("未知！");//位置6
                            break;
                    }
                    index += 2;//连接应用方式
                }else {
                    parseResult.add("无连接方式");
                    index += 1;
                }
                LogUtil.d("连接方式",parseResult.get(6));
            }else {
                parseResult.add("连接方式无");//位置6
                return parseResult;
            }
            if(dataTypeNum>=3){
                if((APDU[index]&0xFF)!=0){
                    switch (APDU[index+1] & 0xFF){
                        case 0:
                            //主备模式
                            parseResult.add("主备模式");//位置7
                            break;
                        case 1:
                            //多连接模式
                            parseResult.add("多连接模式");//位置7
                            break;
                        default:
                            parseResult.add("未知！");//位置7
                            break;
                    }
                    index += 2;//侦听端口列表
                }else {
                    parseResult.add("无连接应用方式");
                    index+=1;
                }
                LogUtil.d("连接应用方式",parseResult.get(7));
            }else {
                parseResult.add("连接应用方式无");//位置7
                return parseResult;
            }
            if(dataTypeNum>=4){
                if((APDU[index]&0xFF)!=0){
                    int listeningPortNum = APDU[index+1] & 0xFF;//侦听端口个数
                    switch (listeningPortNum){
                        case 0:
                            parseResult.add("0");//位置8,侦听端口个数
                            //端口号：位置9-12
                            parseResult.add("");
                            parseResult.add("");
                            parseResult.add("");
                            parseResult.add("");
                            index += 2;//代理服务器地址
                            break;
                        case 1:
                            parseResult.add("1");//位置8,侦听端口个数
                            //端口号：位置9-12
                            int port1_1 = (((APDU[index+3]&0xFF)<<8) + (APDU[index+4]&0xFF))&0xFFFF;
                            parseResult.add(port1_1+"");
                            parseResult.add("");
                            parseResult.add("");
                            parseResult.add("");
                            index += 5;//代理服务器地址
                            break;
                        case 2:
                            parseResult.add("2");//位置8,侦听端口个数
                            //端口号：位置9-12
                            int port2_1 = (((APDU[index+3]&0xFF)<<8) + (APDU[index+4]&0xFF))&0xFFFF;
                            int port2_2 = (((APDU[index+6]&0xFF)<<8) + (APDU[index+7]&0xFF))&0xFFFF;
                            parseResult.add(port2_1+"");
                            parseResult.add(port2_2+"");
                            parseResult.add("");
                            parseResult.add("");
                            index += 8;//代理服务器地址
                            break;
                        case 3:
                            parseResult.add("3");//位置8,侦听端口个数
                            //端口号：位置9-12
                            int port3_1 = (((APDU[index+3]&0xFF)<<8) + (APDU[index+4]&0xFF))&0xFFFF;
                            int port3_2 = (((APDU[index+6]&0xFF)<<8) + (APDU[index+7]&0xFF))&0xFFFF;
                            int port3_3 = (((APDU[index+9]&0xFF)<<8) + (APDU[index+10]&0xFF))&0xFFFF;
                            parseResult.add(port3_1+"");
                            parseResult.add(port3_2+"");
                            parseResult.add(port3_3+"");
                            parseResult.add("");
                            index += 11;//代理服务器地址
                            break;
                        default:
                            parseResult.add("4");//位置8,侦听端口个数
                            //端口号：位置9-12
                            int port4_1 = (((APDU[index+3]&0xFF)<<8) + (APDU[index+4]&0xFF))&0xFFFF;
                            int port4_2 = (((APDU[index+6]&0xFF)<<8) + (APDU[index+7]&0xFF))&0xFFFF;
                            int port4_3 = (((APDU[index+9]&0xFF)<<8) + (APDU[index+10]&0xFF))&0xFFFF;
                            int port4_4 = (((APDU[index+12]&0xFF)<<8) + (APDU[index+13]&0xFF))&0xFFFF;
                            parseResult.add(port4_1+"");
                            parseResult.add(port4_2+"");
                            parseResult.add(port4_3+"");
                            parseResult.add(port4_4+"");
                            index += 3*listeningPortNum + 2;//代理服务器地址
                            break;
                    }
                }else {
                    parseResult.add("0");//位置8,侦听端口个数
                    //端口号：位置9-12
                    parseResult.add("");
                    parseResult.add("");
                    parseResult.add("");
                    parseResult.add("");
                    index += 1;
                }
                LogUtil.d("侦听端口个数",parseResult.get(8));
                LogUtil.d("侦听端口1",parseResult.get(9));
                LogUtil.d("侦听端口2",parseResult.get(10));
                LogUtil.d("侦听端口3",parseResult.get(11));
                LogUtil.d("侦听端口4",parseResult.get(12));
            }else {
                parseResult.add("0");//位置8,侦听端口个数
                //端口号：位置9-12
                parseResult.add("");
                parseResult.add("");
                parseResult.add("");
                parseResult.add("");
                return parseResult;
            }
            if(dataTypeNum>=5){
                if((APDU[index]&0xFF)!=0){
                    int proxyServerAddressLen = APDU[index+1] & 0xFF;
                    if(proxyServerAddressLen == 4){
                        int IP_1 = APDU[index+2] & 0xFF;
                        int IP_2 = APDU[index+3] & 0xFF;
                        int IP_3 = APDU[index+4] & 0xFF;
                        int IP_4 = APDU[index+5] & 0xFF;
                        parseResult.add(IP_1+"."+IP_2+"."+IP_3+"."+IP_4);//位置13，代理服务器地址
                        index += 6;//代理端口
                    }else {
                        parseResult.add(" . . . ");//位置13，代理服务器地址异常
                        index += (2+proxyServerAddressLen);
                    }
                }else {
                    parseResult.add(" . . . ");//位置13，代理服务器地址异常
                    index += 1;
                }
                LogUtil.d("代理服务器地址",parseResult.get(13));
            }else {
                parseResult.add("无代理服务器地址");
                return parseResult;
            }
            if(dataTypeNum>=6){
                if((APDU[index]&0xFF)!=0){
                    int proxyPort = (((APDU[index+1]&0xFF)<<8) + (APDU[index+2]&0xFF))&0xFFFF;
                    parseResult.add(proxyPort+"");//位置14，代理端口
                    index += 3;//超时时间及重发次数
                }else {
                    parseResult.add("");
                    index += 1;//超时时间及重发次数
                }
                LogUtil.d("代理端口",parseResult.get(14));
            }else {
                parseResult.add("无代理端口");
                return parseResult;
            }
            if(dataTypeNum>=7){
                if((APDU[index]&0xFF)!=0){
                    int retransmissions = APDU[index+1] & 0x03;
                    int timeout = ((APDU[index+1]&0xFF)>>>2) & 0x3F;
                    parseResult.add(retransmissions+"");//位置15，重发次数
                    parseResult.add(timeout+"");//位置16，超时时间
                    LogUtil.d("重发次数",parseResult.get(15));
                    LogUtil.d("超时时间",parseResult.get(16));
                    index += 2;//心跳周期
                }else {
                    parseResult.add("无重发次数");
                    parseResult.add("无超时时间");
                    index += 1;
                }
            }else {
                parseResult.add("无重发次数");
                parseResult.add("无超时时间");
                return parseResult;
            }
            if(dataTypeNum == 8){
                if((APDU[index]&0xFF)!=0){
                    int heartbeatCycle = (((APDU[index+1]&0xFF)<<8)+(APDU[index+2]&0xFF))&0xFFFF;
                    parseResult.add(heartbeatCycle+"");//位置17，心跳周期
                }else {
                    parseResult.add("无心跳周期");
                }
                LogUtil.d("心跳周期",parseResult.get(17));
                return parseResult;
            }else {
                parseResult.add("无心跳周期");
                return parseResult;
            }
        }else {
            parseResult.add("未知信息");//位置4
            parseResult.add("未知信息");//位置5
            return parseResult;
        }

    }

    private static List<String> parseFrame_Read_45000200(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        //Get-Result: 0表示错误信息；1表示数据
        int getResult = APDU[7] & 0xFF;
        if(getResult == 0){
            parseResult.add("DAR");//错误信息，位置4
            parseResult.add(""+APDU[8]);//错误码，位置5
            return parseResult;
        }else if(getResult == 1){
            parseResult.add("Data");//数据信息，位置4
            String dataType = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,8,8));
            LogUtil.d("数据类型",dataType);
            int dataTypeNum = APDU[9] & 0xFF;
            LogUtil.d("数据类型"+dataType+"下的个数",dataTypeNum+"");
            if(dataTypeNum==0){
                parseResult.add("无数据");//位置5，无数据
                return parseResult;
            }
            int index = 10;//数据内容在APDU中的索引,工作模式
            if(dataTypeNum>=1){
                if((APDU[index]&0xFF)!=0){
                    switch (APDU[index+1] & 0xFF){
                        case 0:
                            //混合模式
                            parseResult.add("混合模式");//位置5
                            break;
                        case 1:
                            //客户机模式
                            parseResult.add("客户机模式");//位置5
                            break;
                        case 2:
                            //服务器模式
                            parseResult.add("服务器模式");//位置5
                            break;
                        default:
                            parseResult.add("未知！");
                            break;
                    }
                    index += 2;//在线方式
                }else {
                    parseResult.add("无工作模式");
                    index += 1;
                }
                LogUtil.d("工作模式",parseResult.get(5));
            }else {
                parseResult.add("无工作模式");//位置5
                return parseResult;
            }
            if(dataTypeNum>=2){
                if((APDU[index]&0xFF)!=0){
                    switch (APDU[index+1] & 0xFF){
                        case 0:
                            //永久在线
                            parseResult.add("永久在线");//位置6
                            break;
                        case 1:
                            //被动激活
                            parseResult.add("被动激活");//位置6
                            break;
                        default:
                            parseResult.add("未知！");//位置6
                            break;
                    }
                    index += 2;//连接方式
                }else {
                    parseResult.add("无在线方式");//位置6
                    index += 1;
                }
                LogUtil.d("在线方式",parseResult.get(6));
            }else {
                parseResult.add("无在线方式");//位置6
                return parseResult;
            }
            if(dataTypeNum>=3){
                if((APDU[index]&0xFF)!=0){
                    switch (APDU[index+1] & 0xFF){
                        case 0:
                            //TCP
                            parseResult.add("TCP");//位置7
                            break;
                        case 1:
                            //UDP
                            parseResult.add("UDP");//位置7
                            break;
                        default:
                            parseResult.add("未知！");//位置7
                            break;
                    }
                    index += 2;//连接应用方式
                }else {
                    parseResult.add("无连接方式");//位置7
                    index += 1;
                }
                LogUtil.d("连接方式",parseResult.get(7));
            }else {
                parseResult.add("无连接方式");//位置7
                return parseResult;
            }
            if(dataTypeNum>=4){
                if((APDU[index]&0xFF)!=0){
                    switch (APDU[index+1] & 0xFF){
                        case 0:
                            //主备模式
                            parseResult.add("主备模式");//位置8
                            break;
                        case 1:
                            //多连接模式
                            parseResult.add("多连接模式");//位置8
                            break;
                        default:
                            parseResult.add("未知！");//位置8
                            break;
                    }
                    index += 2;//侦听端口列表
                }else {
                    parseResult.add("无连接应用方式");//位置8
                    index += 1;
                }
                LogUtil.d("连接应用方式",parseResult.get(8));
            }else {
                parseResult.add("无连接应用方式");//位置8
                return parseResult;
            }
            if(dataTypeNum>=5){
                if((APDU[index]&0xFF)!=0){
                    int listeningPortNum = APDU[index+1] & 0xFF;//侦听端口个数
                    switch (listeningPortNum){
                        case 0:
                            parseResult.add("0");//位置9,侦听端口个数
                            //端口号：位置10-13
                            parseResult.add("");
                            parseResult.add("");
                            parseResult.add("");
                            parseResult.add("");
                            index += 2;//APN
                            break;
                        case 1:
                            parseResult.add("1");//位置9,侦听端口个数
                            //端口号：位置10-13
                            int port1_1 = (((APDU[index+3]&0xFF)<<8) + (APDU[index+4]&0xFF))&0xFFFF;
                            parseResult.add(port1_1+"");
                            parseResult.add("");
                            parseResult.add("");
                            parseResult.add("");
                            index += 5;//APN
                            break;
                        case 2:
                            parseResult.add("2");//位置9,侦听端口个数
                            //端口号：位置10-13
                            int port2_1 = (((APDU[index+3]&0xFF)<<8) + (APDU[index+4]&0xFF))&0xFFFF;
                            int port2_2 = (((APDU[index+6]&0xFF)<<8) + (APDU[index+7]&0xFF))&0xFFFF;
                            parseResult.add(port2_1+"");
                            parseResult.add(port2_2+"");
                            parseResult.add("");
                            parseResult.add("");
                            index += 8;//APN
                            break;
                        case 3:
                            parseResult.add("3");//位置9,侦听端口个数
                            //端口号：位置10-13
                            int port3_1 = (((APDU[index+3]&0xFF)<<8) + (APDU[index+4]&0xFF))&0xFFFF;
                            int port3_2 = (((APDU[index+6]&0xFF)<<8) + (APDU[index+7]&0xFF))&0xFFFF;
                            int port3_3 = (((APDU[index+9]&0xFF)<<8) + (APDU[index+10]&0xFF))&0xFFFF;
                            parseResult.add(port3_1+"");
                            parseResult.add(port3_2+"");
                            parseResult.add(port3_3+"");
                            parseResult.add("");
                            index += 11;//APN
                            break;
                        default:
                            parseResult.add("4");//位置9,侦听端口个数
                            //端口号：位置10-13
                            int port4_1 = (((APDU[index+3]&0xFF)<<8) + (APDU[index+4]&0xFF))&0xFFFF;
                            int port4_2 = (((APDU[index+6]&0xFF)<<8) + (APDU[index+7]&0xFF))&0xFFFF;
                            int port4_3 = (((APDU[index+9]&0xFF)<<8) + (APDU[index+10]&0xFF))&0xFFFF;
                            int port4_4 = (((APDU[index+12]&0xFF)<<8) + (APDU[index+13]&0xFF))&0xFFFF;
                            parseResult.add(port4_1+"");
                            parseResult.add(port4_2+"");
                            parseResult.add(port4_3+"");
                            parseResult.add(port4_4+"");
                            index += 3*listeningPortNum + 2;//APN
                            break;
                    }
                }else {
                    parseResult.add("0");//位置9,侦听端口个数
                    //端口号：位置10-13
                    parseResult.add("");
                    parseResult.add("");
                    parseResult.add("");
                    parseResult.add("");
                    index += 1;
                }
                LogUtil.d("侦听端口个数",parseResult.get(9));
                LogUtil.d("侦听端口1",parseResult.get(10));
                LogUtil.d("侦听端口2",parseResult.get(11));
                LogUtil.d("侦听端口3",parseResult.get(12));
                LogUtil.d("侦听端口4",parseResult.get(13));
            }else {
                parseResult.add("0");//位置9,侦听端口个数
                //端口号：位置10-13
                parseResult.add("");
                parseResult.add("");
                parseResult.add("");
                parseResult.add("");
                return parseResult;
            }
            if(dataTypeNum>=6){
                if((APDU[index]&0xFF)!=0){
                    int apnLen = APDU[index+1] & 0xFF;
                    if(apnLen != 0){
                        byte[] APN = TopscommUtils.getSubByteArray(APDU,index+2,index+1+apnLen);
                        String apnString = TopscommUtils.getByteArray2AsciiString(APN);
                        parseResult.add(apnString);//位置14，APN
                    }else {
                        parseResult.add("");//位置14，APN
                    }
                    index += (2+apnLen);//用户名
                }else {
                    parseResult.add("");//位置14，APN
                    index += 1;
                }
                LogUtil.d("APN",parseResult.get(14));
            }else {
                parseResult.add("无APN");
                return parseResult;
            }
            if(dataTypeNum>=7){
                if((APDU[index]&0xFF)!=0){
                    int userNameLen = APDU[index+1] & 0xFF;
                    if(userNameLen != 0){
                        byte[] userName = TopscommUtils.getSubByteArray(APDU,index+2,index+1+userNameLen);
                        String userNameString = TopscommUtils.getByteArray2AsciiString(userName);
                        parseResult.add(userNameString);//位置15，用户名
                    }else {
                        parseResult.add("");
                    }
                    index += (2+userNameLen);//密码
                }else {
                    parseResult.add("");
                    index += 1;
                }
                LogUtil.d("用户名",parseResult.get(15));
            }else {
                parseResult.add("无用户名");
                return parseResult;
            }
            if(dataTypeNum>=8){
                if((APDU[index]&0xFF)!=0){
                    int keyLen = APDU[index+1] & 0xFF;
                    if(keyLen != 0){
                        byte[] key = TopscommUtils.getSubByteArray(APDU,index+2,index+1+keyLen);
                        String keyString = TopscommUtils.getByteArray2AsciiString(key);
                        parseResult.add(keyString);//位置16，密码
                    }else {
                        parseResult.add("");
                    }
                    index += (2+keyLen);//代理服务器地址
                }else {
                    parseResult.add("");
                    index += 1;
                }
                LogUtil.d("密码",parseResult.get(16));
            }else {
                parseResult.add("无密码");
                return parseResult;
            }
            if(dataTypeNum>=9){
                if((APDU[index]&0xFF)!=0){
                    int proxyServerAddressLen = APDU[index+1] & 0xFF;
                    if(proxyServerAddressLen == 4){
                        int IP_1 = APDU[index+2] & 0xFF;
                        int IP_2 = APDU[index+3] & 0xFF;
                        int IP_3 = APDU[index+4] & 0xFF;
                        int IP_4 = APDU[index+5] & 0xFF;
                        parseResult.add(IP_1+"."+IP_2+"."+IP_3+"."+IP_4);//位置17，代理服务器地址
                        index += 6;//代理端口
                    }else {
                        parseResult.add(" . . . ");//位置17，代理服务器地址异常
                        index += (2+proxyServerAddressLen);
                    }
                }else {
                    parseResult.add(" . . . ");//位置17，代理服务器地址异常
                    index += 1;
                }
                LogUtil.d("代理服务器地址",parseResult.get(17));
            }else {
                parseResult.add("无代理服务器地址");
                return parseResult;
            }
            if(dataTypeNum>=10){
                if((APDU[index]&0xFF)!=0){
                    int proxyPort = (((APDU[index+1]&0xFF)<<8) + (APDU[index+2]&0xFF))&0xFFFF;
                    parseResult.add(proxyPort+"");//位置18，代理端口
                    index += 3;//超时时间及重发次数
                }else {
                    parseResult.add("");
                    index += 1;//超时时间及重发次数
                }
                LogUtil.d("代理端口",parseResult.get(18));
            }else {
                parseResult.add("无代理端口");
                return parseResult;
            }
            if(dataTypeNum>=11){
                if((APDU[index]&0xFF)!=0){
                    int retransmissions = APDU[index+1] & 0x03;
                    int timeout = ((APDU[index+1]&0xFF)>>>2) & 0x3F;
                    parseResult.add(retransmissions+"");//位置19，重发次数
                    parseResult.add(timeout+"");//位置20，超时时间
                    index += 2;//心跳周期
                }else {
                    parseResult.add("");
                    parseResult.add("");
                    index += 1;
                }
                LogUtil.d("重发次数",parseResult.get(19));
                LogUtil.d("超时时间",parseResult.get(20));
            }else {
                parseResult.add("无重发次数");
                parseResult.add("无超时时间");
                return parseResult;
            }
            if(dataTypeNum == 12){
                if((APDU[index]&0xFF)!=0){
                    int heartbeatCycle = (((APDU[index+1]&0xFF)<<8)+(APDU[index+2]&0xFF))&0xFFFF;
                    parseResult.add(heartbeatCycle+"");//位置21，心跳周期
                }else {
                    parseResult.add("");
                }
                LogUtil.d("心跳周期",parseResult.get(21));
                return parseResult;
            }else {
                parseResult.add("无心跳周期");
                return parseResult;
            }
        }else {
            parseResult.add("未知信息");//位置4
            parseResult.add("未知信息");//位置5
            return parseResult;
        }

    }

    private static List<String> parseFrame_Read_40010200(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        //Get-Result: 0表示错误信息，1表示数据
        int getResult = APDU[7] & 0xFF;
        if(getResult == 0){
            parseResult.add("DAR");//错误信息，位置4
            parseResult.add(""+APDU[8]);//错误码，位置5
            return parseResult;
        }else if(getResult == 1){
            parseResult.add("Data");//数据信息，位置4
            String dataType = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,8,8));
            LogUtil.d("数据类型",dataType);
            int deviceAddressLen = APDU[9] & 0xFF;//设备地址字节长度
            String deviceAddress = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,10,9+deviceAddressLen));
            currentDeviceAddress = deviceAddress;
            LogUtil.d("设备地址1",parseResult.get(1));
            LogUtil.d("设备地址2",deviceAddress);
            parseResult.add(deviceAddress);//设备地址，位置5
        }else {
            parseResult.add("未知信息！");//位置4
            parseResult.add("未知信息！");//位置5
            return parseResult;
        }
        return parseResult;
    }

    private static List<String> parseFrame_Read_40000200(List<String> resultList,byte[] APDU){
       List<String> parseResult = resultList;
       //Get-Result: 0表示错误信息，1表示数据
        int getResult = APDU[7] & 0xFF;
        if(getResult == 0){
            parseResult.add("DAR");//错误信息，位置4
            parseResult.add(""+APDU[8]);//错误码，位置5
            return parseResult;
        }else if(getResult == 1){
            parseResult.add("Data");//数据信息，位置4
            String dataType = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,8,8));
            LogUtil.d("数据类型",dataType);
            String yearHexString = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,9,10));
            int year = Integer.parseInt(yearHexString,16);
            String monthHexString = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,11,11));
            int month = Integer.parseInt(monthHexString,16);
            String dayHexString = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,12,12));
            int day = Integer.parseInt(dayHexString,16);
            String hourHexString = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,13,13));
            int hour = Integer.parseInt(hourHexString,16);
            String minuteHexString = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,14,14));
            int minute = Integer.parseInt(minuteHexString,16);
            String secondHexString = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,15,15));
            int second = Integer.parseInt(secondHexString,16);
            String  deviceClock = year+"-"+month+"-"+day+"   "+hour+":"+minute+":"+second;
            LogUtil.d("设备时钟",deviceClock);
            parseResult.add(deviceClock);//设备时钟，位置5
            return parseResult;
        }else {
            parseResult.add("未知信息！");//位置4
            parseResult.add("未知信息！");//位置5
            return parseResult;
        }
    }

    /**
     *
     * @param resultList
     * @param APDU
     * @return parseResult
     * 第4个位置存放获取结果（即获取的是错误信息还是数据信息）
     * 第5个位置：若4存放的是错误信息，则存放错误码；若4存放的是数据信息，则存放具体数据
     */
    private static List<String> parseFrame_Read_43000300(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        //Get-Result: 0表示错误信息，1表示数据
        int getResult = APDU[7] & 0xFF;
        if(getResult == 0){
            parseResult.add("DAR");//错误信息，位置4
            parseResult.add(""+APDU[8]);//错误码，位置5
            return parseResult;
        }else if (getResult == 1){
            parseResult.add("Data");//数据信息，位置4
            String dataType = TopscommUtils.byteArrayToHexString(
                    TopscommUtils.getSubByteArray(APDU,8,8));
            LogUtil.d("数据类型",dataType);
            int dataNum = TopscommUtils.getSubByteArray(APDU,9,9)[0] & 0xFF;  //该数据类型下的数据个数
            LogUtil.d("数据类型"+dataType+"下的数据个数",dataNum+"");
            int data1_Len = APDU[11] & 0xFF;//厂商代码长度
            int index = 12;//该数据项的起始索引
            parseResult.add(TopscommUtils.getByteArray2AsciiString(
                    TopscommUtils.getSubByteArray(APDU,index,index+data1_Len-1)));//厂商代码，位置5
            LogUtil.d("厂商代码",parseResult.get(5));
            int data2_Len = APDU[index+data1_Len+1] & 0xFF;//软件版本号长度
            index = index + data1_Len + 2;//该数据项的起始索引
            parseResult.add(TopscommUtils.getByteArray2AsciiString(
                    TopscommUtils.getSubByteArray(APDU,index,index+data2_Len-1)));//软件版本号，位置6
            LogUtil.d("软件版本号",parseResult.get(6));
            int data3_Len = APDU[index+data2_Len+1] & 0xFF;//软件版本日期长度
            index = index + data2_Len + 2;
            parseResult.add(TopscommUtils.getByteArray2AsciiString(
                    TopscommUtils.getSubByteArray(APDU,index,index+data3_Len-1)));//软件版本日期，位置7
            LogUtil.d("软件版本日期",parseResult.get(7));
            int data4_Len = APDU[index+data3_Len+1] & 0xFF;//硬件版本号长度
            index = index + data3_Len + 2;
            parseResult.add(TopscommUtils.getByteArray2AsciiString(
                    TopscommUtils.getSubByteArray(APDU,index,index+data4_Len-1)));//硬件版本号，位置8
            LogUtil.d("硬件版本号",parseResult.get(8));
            int data5_len = APDU[index+data4_Len+1] & 0xFF;//硬件版本日期长度
            index = index + data4_Len + 2;
            parseResult.add(TopscommUtils.getByteArray2AsciiString(
                    TopscommUtils.getSubByteArray(APDU,index,index+data5_len-1)));//硬件版本日期，位置9
            LogUtil.d("硬件版本日期",parseResult.get(9));
            int data6_Len = APDU[index+data5_len+1] & 0xFF;//厂商扩展信息长度
            index = index + data5_len + 2;
            parseResult.add(TopscommUtils.getByteArray2AsciiString(
                    TopscommUtils.getSubByteArray(APDU,index,index+data6_Len-1)));//厂商扩展信息，位置10
            LogUtil.d("厂商扩展信息",parseResult.get(10));
            return parseResult;
        }else {
            return parseResult;
        }
    }

    //APDU服务类型为设置
    private static List<String> parseApdu_Set(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        if((APDU[1]&0xFF)==0x01){//请求设置一个对象属性
            parseResult = parseFrame_Set_OneObjectAttribute(parseResult,APDU);
        }else {

        }
        return parseResult;
    }

    private static List<String> parseFrame_Set_OneObjectAttribute(List<String> resultList,byte[] APDU){
        List<String> parseResult = resultList;
        //设置执行结果DAR
        int dar = APDU[7] & 0xFF;
        if(dar == 0){
            //设置成功
            parseResult.add("设置成功！");//位置4
        }else{
            //设置失败
            parseResult.add("设置失败！（DAR："+dar+"）");//位置4
        }
        return parseResult;
    }

    public static byte[] makeFactoryVersionInfoFrame() {

        Protocol698Frame.CtrlArea ctrlArea = new Protocol698Frame.CtrlArea(Protocol698Frame.DIR_PRM.CLIENT_REQUEST, false, false, 3);
        Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(Protocol698Frame.ADDRESS_TYPE.WILDCARD, false,
                0, 6, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
        Protocol698Frame.AddressArea addressArea = new Protocol698Frame.AddressArea(serv_addr, (byte) 0x10);

        Protocol698Frame.OAD oad = new Protocol698Frame.OAD(new byte[] {(byte) 0x43, (byte) 0x00, (byte) 0x03, (byte) 0x00});
        Protocol698Frame.PIID piid = new Protocol698Frame.PIID(0, 1);
        Map map = new HashMap();
        map.put(ProtocolConstant.OAD_KEY, oad);
        map.put(ProtocolConstant.PIID_KEY, piid);

        byte[] apdu = Protocol698.PROTOCOL_698.makeAPDU(ProtocolConstant.CLIENT_APDU.GET_REQUEST.CLASS_ID, ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NORMAL.CLASS_ID, map);
        Log.i(TAG, "makeFactoryVersionInfoFrame, ctrlArea: " + DataConvertUtils.convertByteToString(ctrlArea.data));
        Log.i(TAG, "makeFactoryVersionInfoFrame, addrArea: " + DataConvertUtils.convertByteArrayToString(addressArea.data, false));
        Log.i(TAG, "makeFactoryVersionInfoFrame, apdu: " + DataConvertUtils.convertByteArrayToString(apdu, false));

        byte[] frame = Protocol698.PROTOCOL_698.makeFrame(ctrlArea, addressArea, apdu);

        Log.i(TAG, "makeFactoryVersionInfoFrame, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        return frame;
    }

    /**
     *
     * @param frame, received oop frame
     * @return String[], String[0]: server address, String[1]: software version, String[2]: software date,
     *                   String[3]: hardware version, String[4]: hardware date.
     */
    public static String[] parseFractoryVersionFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        boolean isOK = Protocol698.PROTOCOL_698.verify698Frame(frame);
        Log.i(TAG, "parseFractoryVersionFrame, is OK: " + isOK + ", apdu begin: " + Protocol698.PROTOCOL_698.mApduBegin);
        if (isOK) {
            Map map = Protocol698.PROTOCOL_698.parseApud(DataConvertUtils.getSubByteArray(frame,
                    Protocol698.PROTOCOL_698.mApduBegin, Protocol698.PROTOCOL_698.mApduEnd));
            String[] strings = new String[5];
            Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(
                    DataConvertUtils.getSubByteArray(frame, Protocol698.PROTOCOL_698.mAddressAreaBeginPos, Protocol698.PROTOCOL_698.mAddressAreaEndPos - 1));
            strings[0] = DataConvertUtils.convertByteArrayToString(serv_addr.address, false);
            if (map != null && map.containsKey("data")) {
                Protocol698Frame.Data data = (Protocol698Frame.Data) map.get("data");
                ArrayList<Protocol698Frame.Data> dataArrayList = (ArrayList<Protocol698Frame.Data>) data.obj;
                if (dataArrayList != null && dataArrayList.size() > 0) {
                    for (int i = 0; i < dataArrayList.size(); i++) {
                        Protocol698Frame.Data item = dataArrayList.get(i);
                        Log.i(TAG, "parseFractoryVersionFrame, item: " + item.obj);
                        if (i >= 1 && i <= 4) {
                            strings[i] = (String) item.obj;
                        }
                    }


                }
            }
            return strings;

        }
        return null;
    }

    public static byte[] makeResetFrame() {
        Protocol698Frame.CtrlArea ctrlArea = new Protocol698Frame.CtrlArea(Protocol698Frame.DIR_PRM.CLIENT_REQUEST, false, false, 3);
        Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(Protocol698Frame.ADDRESS_TYPE.WILDCARD, false,
                0, 6, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
        Protocol698Frame.AddressArea addressArea = new Protocol698Frame.AddressArea(serv_addr, (byte) 0x10);

        byte[] IO = new byte[]{0x43, 0x00};
        Protocol698Frame.OMD omd = new Protocol698Frame.OMD(IO, (byte) 1, (byte) 0);
        Protocol698Frame.PIID piid = new Protocol698Frame.PIID(0, 1);
        Map map = new HashMap();
        map.put(ProtocolConstant.OMD_KEY, omd);
        map.put(ProtocolConstant.PIID_KEY, piid);
        Protocol698Frame.Data data = new Protocol698Frame.Data(Protocol698Frame.Data_Type.NULL_TYPE, null);
        map.put(ProtocolConstant.OMD_PARAM_KEY, data);

        byte[] apdu = Protocol698.PROTOCOL_698.makeAPDU(ProtocolConstant.CLIENT_APDU.ACTION_REQUEST.CLASS_ID, ProtocolConstant.CLIENT_APDU.ACTION_REQUEST.ACTION_REQUEST_NORMAL.CLASS_ID, map);
        Log.i(TAG, "makeResetFrame, ctrlArea: " + DataConvertUtils.convertByteToString(ctrlArea.data));
        Log.i(TAG, "makeResetFrame, addrArea: " + DataConvertUtils.convertByteArrayToString(addressArea.data, false));
        Log.i(TAG, "makeResetFrame, apdu: " + DataConvertUtils.convertByteArrayToString(apdu, false));

        byte[] frame = Protocol698.PROTOCOL_698.makeFrame(ctrlArea, addressArea, apdu);

        Log.i(TAG, "makeResetFrame, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        return frame;
    }

    public static boolean parseResetFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return false;
        }
        boolean isOK = Protocol698.PROTOCOL_698.verify698Frame(frame);
        Log.i(TAG, "parseResetFrame, is OK: " + isOK + ", apdu begin: " + Protocol698.PROTOCOL_698.mApduBegin);
        if (isOK) {
            Map map = Protocol698.PROTOCOL_698.parseApud(DataConvertUtils.getSubByteArray(frame,
                    Protocol698.PROTOCOL_698.mApduBegin, Protocol698.PROTOCOL_698.mApduEnd));
            if (map != null && map.containsKey(DAR_KEY)) {
                byte dar = (byte) map.get(DAR_KEY);
                if (dar == (byte) 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }


        }
        return false;
    }

    public static boolean parseSetResponseNormalFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return false;
        }
        boolean isOK = Protocol698.PROTOCOL_698.verify698Frame(frame);
        Log.i(TAG, "parseSetResponseNormalFrame, is OK: " + isOK + ", apdu begin: " + Protocol698.PROTOCOL_698.mApduBegin);
        if (isOK) {
            Map map = Protocol698.PROTOCOL_698.parseApud(DataConvertUtils.getSubByteArray(frame,
                    Protocol698.PROTOCOL_698.mApduBegin, Protocol698.PROTOCOL_698.mApduEnd));
            if (map != null && map.containsKey(DAR_KEY)) {
                byte dar = (byte) map.get(DAR_KEY);
                if (dar == (byte) 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }


        }
        return false;
    }

    public static byte[] makeWirelessNetParamFrame() {

        Protocol698Frame.CtrlArea ctrlArea = new Protocol698Frame.CtrlArea(Protocol698Frame.DIR_PRM.CLIENT_REQUEST, false, false, 3);
        Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(Protocol698Frame.ADDRESS_TYPE.WILDCARD, false,
                0, 6, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
        Protocol698Frame.AddressArea addressArea = new Protocol698Frame.AddressArea(serv_addr, (byte) 0x10);

        Protocol698Frame.OAD oad = new Protocol698Frame.OAD(new byte[] {(byte) 0x45, (byte) 0x00, (byte) 0x02, (byte) 0x00});
        Protocol698Frame.PIID piid = new Protocol698Frame.PIID(0, 1);
        Map map = new HashMap();
        map.put(ProtocolConstant.OAD_KEY, oad);
        map.put(ProtocolConstant.PIID_KEY, piid);

        byte[] apdu = Protocol698.PROTOCOL_698.makeAPDU(ProtocolConstant.CLIENT_APDU.GET_REQUEST.CLASS_ID, ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NORMAL.CLASS_ID, map);
        Log.i(TAG, "makeWirelessNetParamFrame, ctrlArea: " + DataConvertUtils.convertByteToString(ctrlArea.data));
        Log.i(TAG, "makeWirelessNetParamFrame, addrArea: " + DataConvertUtils.convertByteArrayToString(addressArea.data, false));
        Log.i(TAG, "makeWirelessNetParamFrame, apdu: " + DataConvertUtils.convertByteArrayToString(apdu, false));

        byte[] frame = Protocol698.PROTOCOL_698.makeFrame(ctrlArea, addressArea, apdu);

        Log.i(TAG, "makeWirelessNetParamFrame, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        return frame;
    }

    public static byte[] makeSetWirelessNetParamFrame(Protocol698Frame.Data data) {

        Protocol698Frame.CtrlArea ctrlArea = new Protocol698Frame.CtrlArea(Protocol698Frame.DIR_PRM.CLIENT_REQUEST, false, false, 3);
        Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(Protocol698Frame.ADDRESS_TYPE.WILDCARD, false,
                0, 6, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
        Protocol698Frame.AddressArea addressArea = new Protocol698Frame.AddressArea(serv_addr, (byte) 0x10);

        Protocol698Frame.OAD oad = new Protocol698Frame.OAD(new byte[] {(byte) 0x45, (byte) 0x00, (byte) 0x02, (byte) 0x00});
        Protocol698Frame.PIID piid = new Protocol698Frame.PIID(0, 1);
        Map map = new HashMap();
        map.put(ProtocolConstant.OAD_KEY, oad);
        map.put(ProtocolConstant.PIID_KEY, piid);
        map.put(ProtocolConstant.DATA_KEY, data);

        byte[] apdu = Protocol698.PROTOCOL_698.makeAPDU(ProtocolConstant.CLIENT_APDU.SET_REQUEST.CLASS_ID, ProtocolConstant.CLIENT_APDU.SET_REQUEST.SET_REQUEST_NORMAL.CLASS_ID, map);
        Log.i(TAG, "makeSetWirelessNetParamFrame, ctrlArea: " + DataConvertUtils.convertByteToString(ctrlArea.data));
        Log.i(TAG, "makeSetWirelessNetParamFrame, addrArea: " + DataConvertUtils.convertByteArrayToString(addressArea.data, false));
        Log.i(TAG, "makeSetWirelessNetParamFrame, apdu: " + DataConvertUtils.convertByteArrayToString(apdu, false));

        byte[] frame = Protocol698.PROTOCOL_698.makeFrame(ctrlArea, addressArea, apdu);

        Log.i(TAG, "makeSetWirelessNetParamFrame, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        return frame;
    }

    public static byte[] makeMasterNetParamForWirelessFrame() {

        Protocol698Frame.CtrlArea ctrlArea = new Protocol698Frame.CtrlArea(Protocol698Frame.DIR_PRM.CLIENT_REQUEST, false, false, 3);
        Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(Protocol698Frame.ADDRESS_TYPE.WILDCARD, false,
                0, 6, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
        Protocol698Frame.AddressArea addressArea = new Protocol698Frame.AddressArea(serv_addr, (byte) 0x10);

        Protocol698Frame.OAD oad = new Protocol698Frame.OAD(new byte[] {(byte) 0x45, (byte) 0x00, (byte) 0x03, (byte) 0x00});
        Protocol698Frame.PIID piid = new Protocol698Frame.PIID(0, 1);
        Map map = new HashMap();
        map.put(ProtocolConstant.OAD_KEY, oad);
        map.put(ProtocolConstant.PIID_KEY, piid);

        byte[] apdu = Protocol698.PROTOCOL_698.makeAPDU(ProtocolConstant.CLIENT_APDU.GET_REQUEST.CLASS_ID, ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NORMAL.CLASS_ID, map);
        Log.i(TAG, "makeMasterNetParamForWirelessFrame, ctrlArea: " + DataConvertUtils.convertByteToString(ctrlArea.data));
        Log.i(TAG, "makeMasterNetParamForWirelessFrame, addrArea: " + DataConvertUtils.convertByteArrayToString(addressArea.data, false));
        Log.i(TAG, "makeMasterNetParamForWirelessFrame, apdu: " + DataConvertUtils.convertByteArrayToString(apdu, false));

        byte[] frame = Protocol698.PROTOCOL_698.makeFrame(ctrlArea, addressArea, apdu);

        Log.i(TAG, "makeMasterNetParamForWirelessFrame, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        return frame;
    }

    public static byte[] makeSetMasterNetParamForWirelessFrame(Protocol698Frame.Data data) {

        Protocol698Frame.CtrlArea ctrlArea = new Protocol698Frame.CtrlArea(Protocol698Frame.DIR_PRM.CLIENT_REQUEST, false, false, 3);
        Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(Protocol698Frame.ADDRESS_TYPE.WILDCARD, false,
                0, 6, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
        Protocol698Frame.AddressArea addressArea = new Protocol698Frame.AddressArea(serv_addr, (byte) 0x10);

        Protocol698Frame.OAD oad = new Protocol698Frame.OAD(new byte[] {(byte) 0x45, (byte) 0x00, (byte) 0x03, (byte) 0x00});
        Protocol698Frame.PIID piid = new Protocol698Frame.PIID(0, 1);
        Map map = new HashMap();
        map.put(ProtocolConstant.OAD_KEY, oad);
        map.put(ProtocolConstant.PIID_KEY, piid);
        map.put(ProtocolConstant.DATA_KEY, data);

        byte[] apdu = Protocol698.PROTOCOL_698.makeAPDU(ProtocolConstant.CLIENT_APDU.SET_REQUEST.CLASS_ID, ProtocolConstant.CLIENT_APDU.SET_REQUEST.SET_REQUEST_NORMAL.CLASS_ID, map);
        Log.i(TAG, "makeSetMasterNetParamForWirelessFrame, ctrlArea: " + DataConvertUtils.convertByteToString(ctrlArea.data));
        Log.i(TAG, "makeSetMasterNetParamForWirelessFrame, addrArea: " + DataConvertUtils.convertByteArrayToString(addressArea.data, false));
        Log.i(TAG, "makeSetMasterNetParamForWirelessFrame, apdu: " + DataConvertUtils.convertByteArrayToString(apdu, false));

        byte[] frame = Protocol698.PROTOCOL_698.makeFrame(ctrlArea, addressArea, apdu);

        Log.i(TAG, "makeSetMasterNetParamForWirelessFrame, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        return frame;
    }

    public final static String WORK_MODE_KEY = "work_mode";
    public final static String ONLINE_WAY_KEY = "online_way";
    public final static String LINK_WAY_KEY = "link_way";
    public final static String LINK_APP_WAY_KEY = "link_app_way";
    public final static String LISTEN_PORT_LIST_KEY = "listen_port_list";
    public final static String APN_KEY = "apn";
    public final static String USER_KEY = "user";
    public final static String PASSWORD_KEY = "password";
    public final static String PROXY_SERV_ADDRESS_KEY = "proxy_server_address";
    public final static String PROXY_PORT_KEY = "proxy_port";
    public final static String REPEAT_TIME_KEY = "repeat_send_time";
    public final static String TIMEOUT_KEY = "time_out";
    public final static String HEARTBEAT_CYCLE_KEY = "heartbeat_cycle";

    public static Map parseWirelessNetParamFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        Map resultMap = new HashMap();
        boolean isOK = Protocol698.PROTOCOL_698.verify698Frame(frame);
        Log.i(TAG, "parseWirelessNetParamFrame, is OK: " + isOK + ", apdu begin: " + Protocol698.PROTOCOL_698.mApduBegin);
        if (isOK) {
            Map map = Protocol698.PROTOCOL_698.parseApud(DataConvertUtils.getSubByteArray(frame,
                    Protocol698.PROTOCOL_698.mApduBegin, Protocol698.PROTOCOL_698.mApduEnd));
            if (map != null && map.containsKey("data")) {
                Protocol698Frame.Data data = (Protocol698Frame.Data) map.get("data");
                if (data != null && data.data != null && data.type == Protocol698Frame.Data_Type.STRUCTURE_TYPE) {
                    ArrayList<Protocol698Frame.Data> dataArrayList = (ArrayList< Protocol698Frame.Data>) data.obj;
                    if (dataArrayList != null && dataArrayList.size() == 12) {
                        for (int i = 0; i < 12; i++) {
                            Protocol698Frame.Data item = dataArrayList.get(i);
                            if (i == 0) {
                                resultMap.put(WORK_MODE_KEY, ((Byte)(item.obj)).intValue());
                            } else if (i == 1) {
                                resultMap.put(ONLINE_WAY_KEY, ((Byte)(item.obj)).intValue());
                            } else if (i == 2) {
                                resultMap.put(LINK_WAY_KEY, ((Byte)(item.obj)).intValue());
                            } else if (i == 3) {
                                resultMap.put(LINK_APP_WAY_KEY, ((Byte)(item.obj)).intValue());
                            } else if (i == 4) {
                                StringBuilder sb = new StringBuilder();
                                if (item.obj != null && item.obj instanceof ArrayList) {
                                    ArrayList<Protocol698Frame.Data> datas = (ArrayList<Protocol698Frame.Data>) item.obj;
                                    if (datas != null) {
                                        for (int j = 0; j < datas.size(); j++) {
                                            sb.append((int)(datas.get(j).obj) + "");
                                            if (j < datas.size() - 1) {
                                                sb.append(",");
                                            }
                                        }
                                    }
                                }
                                resultMap.put(LISTEN_PORT_LIST_KEY, sb.toString());
                            } else if (i == 5) {
                                resultMap.put(APN_KEY, item.obj);
                            } else if (i == 6) {
                                resultMap.put(USER_KEY, item.obj);
                            } else if (i == 7) {
                                resultMap.put(PASSWORD_KEY, item.obj);
                            } else if (i == 8) {
                                try {
                                    resultMap.put(PROXY_SERV_ADDRESS_KEY, DataConvertUtils.convertByteArrayToString((byte[]) item.obj, false));
                                } catch (Exception e) {
                                    Log.e(TAG, "parseWirelessNetParamFrame, err: " + e.getMessage());
                                }

                            } else if (i == 9) {
                                resultMap.put(PROXY_PORT_KEY, String.valueOf((int)item.obj));
                            } else if (i == 10) {
                                byte b = (byte)item.obj;
                                int repeat = b & 0x03;
                                resultMap.put(REPEAT_TIME_KEY, String.valueOf(repeat));
                                int timeout = (b >> 2) & 0x7F;
                                resultMap.put(TIMEOUT_KEY, String.valueOf(timeout));
                            } else if (i == 11) {
                                resultMap.put(HEARTBEAT_CYCLE_KEY, String.valueOf((int)item.obj));
                            }
                        }


                    }

                }
                return resultMap;
            } else {
                return null;
            }


        }
        return null;

    }

    public static byte[] makeRequestNormalFrame(byte[] oad) {

        if (oad == null || oad.length <= 0) {
            return null;
        }
        Protocol698Frame.CtrlArea ctrlArea = new Protocol698Frame.CtrlArea(Protocol698Frame.DIR_PRM.CLIENT_REQUEST, false, false, 3);
        Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(Protocol698Frame.ADDRESS_TYPE.WILDCARD, false,
                0, 6, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
        Protocol698Frame.AddressArea addressArea = new Protocol698Frame.AddressArea(serv_addr, (byte) 0x10);

        Protocol698Frame.OAD oadObj = new Protocol698Frame.OAD(oad);
        Protocol698Frame.PIID piid = new Protocol698Frame.PIID(0, 1);
        Map map = new HashMap();
        map.put(ProtocolConstant.OAD_KEY, oadObj);
        map.put(ProtocolConstant.PIID_KEY, piid);

        byte[] apdu = Protocol698.PROTOCOL_698.makeAPDU(ProtocolConstant.CLIENT_APDU.GET_REQUEST.CLASS_ID, ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NORMAL.CLASS_ID, map);
        Log.i(TAG, "makeRequestNormalFrame, ctrlArea: " + DataConvertUtils.convertByteToString(ctrlArea.data));
        Log.i(TAG, "makeRequestNormalFrame, addrArea: " + DataConvertUtils.convertByteArrayToString(addressArea.data, false));
        Log.i(TAG, "makeRequestNormalFrame, apdu: " + DataConvertUtils.convertByteArrayToString(apdu, false));
        byte[] frame = Protocol698.PROTOCOL_698.makeFrame(ctrlArea, addressArea, apdu);
        Log.i(TAG, "makeRequestNormalFrame, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        return frame;
    }

    public static ArrayList<String> parseMasterNetParamForWirelessFrame(byte[] frame) {
        if (frame == null || frame.length <= 0) {
            return null;
        }
        ArrayList<String> resultMap = new ArrayList<>();
        boolean isOK = Protocol698.PROTOCOL_698.verify698Frame(frame);
        Log.i(TAG, "parseMasterNetParamForWirelessFrame, is OK: " + isOK + ", apdu begin: " + Protocol698.PROTOCOL_698.mApduBegin);
        if (isOK) {
            Map map = Protocol698.PROTOCOL_698.parseApud(DataConvertUtils.getSubByteArray(frame,
                    Protocol698.PROTOCOL_698.mApduBegin, Protocol698.PROTOCOL_698.mApduEnd));
            if (map != null && map.containsKey(DATA_KEY)) {
                Protocol698Frame.Data data = (Protocol698Frame.Data) map.get(DATA_KEY);
                if (data != null && data.data != null && data.type == Protocol698Frame.Data_Type.ARRAY_TYPE) {
                    ArrayList<Protocol698Frame.Data> dataArrayList = (ArrayList< Protocol698Frame.Data>) data.obj;
                    if (dataArrayList == null || dataArrayList.size() <= 0) {
                        return null;
                    }
                    Log.i(TAG, "parseMasterNetParamForWirelessFrame, size:" + dataArrayList.size() );

                    for (int i = 0; i < dataArrayList.size(); i++) {
                        Protocol698Frame.Data item = dataArrayList.get(i);
                        if (item == null || item.data == null || item.type != Protocol698Frame.Data_Type.STRUCTURE_TYPE || item.obj == null) {
                            return null;
                        }

                        ArrayList<Protocol698Frame.Data> ip_and_port = (ArrayList<Protocol698Frame.Data>) item.obj;

                        if (ip_and_port == null || ip_and_port.size() < 2) {
                            resultMap.add("");
                            continue;
                        }

                        StringBuilder sb = new StringBuilder();
                        Protocol698Frame.Data ip = ip_and_port.get(0);
                        if (ip == null || ip.obj == null || ((byte[]) (ip.obj)).length != 4) {
                            sb.append("0.0.0.0");
                        } else {
                            byte[] ip_items = (byte[]) ip.obj;
                            Log.i(TAG, "parseMasterNetParamForWirelessFrame, ip_items: " + DataConvertUtils.convertByteArrayToString(ip_items, false));
                            sb.append(String.valueOf(ip_items[0] & 0xFF)).append(".").append(String.valueOf(ip_items[1] & 0xFF)).append(".")
                                    .append(String.valueOf(ip_items[2] & 0xFF)).append(".").append(String.valueOf(ip_items[3] & 0xFF));
                        }
                        sb.append(":");
                        Protocol698Frame.Data port = ip_and_port.get(1);
                        if (port == null || port.obj == null) {
                            sb.append("0");
                        } else {
                            Log.i(TAG, "parseMasterNetParamForWirelessFrame, port: " + port.obj);
                            sb.append(String.valueOf((int) port.obj));
                        }
                        resultMap.add(sb.toString());
                    }
                }
                return resultMap;
            } else {
                Log.i(TAG, "parseMasterNetParamForWirelessFrame, return null");
                return null;
            }


        }
        return null;

    }

    public final static String CONFIG_SERIAL_NUM_KEY = "CONFIG_SERIAL_NUM";
    public final static String COMMU_ADDRESS_KEY = "COMMU_ADDRESS";
    public final static String USER_TYPE_KEY = "USER_TYPE";
    public final static String COMMU_PASSWORD_KEY = "COMMU_PASSWORD";
    public final static String RATE_CNT_KEY = "RATE_CNT";
    public final static String RATE_VOLTAGE_KEY = "RATE_VOLTAGE";
    public final static String RATE_CURRENT_KEY = "RATE_CURRENT";
    public final static String COLLECTOR_ADDRESS_KEY = "COLLECTOR_ADDRESS";
    public final static String ASSET_NUM_KEY = "ASSET_NUM";

    public final static String PORT_TYPE_KEY = "PORT_TYPE";
    public final static String BAUD_RATE_KEY = "BAUD_RATE";
    public final static String PROTOCOL_TYPE_KEY = "PROTOCOL_TYPE";
    public final static String LINE_CONNECT_TYPE_KEY = "LINE_CONNECT_TYPE";

    public static byte[] makeGetArchiveFrame(int begin, int end, int interval) {
        if (end < 0 || begin < 0 || end < begin || interval < 0) {
            return null;
        }
        Protocol698Frame.CtrlArea ctrlArea = new Protocol698Frame.CtrlArea(Protocol698Frame.DIR_PRM.CLIENT_REQUEST, false, false, 3);
        Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(Protocol698Frame.ADDRESS_TYPE.WILDCARD, false,
                0, 6, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
        Protocol698Frame.AddressArea addressArea = new Protocol698Frame.AddressArea(serv_addr, (byte) 0x10);

        Protocol698Frame.PIID piid = new Protocol698Frame.PIID(0, 1);

        Protocol698Frame.OAD oad = new Protocol698Frame.OAD(new byte[]{(byte) 0x60, (byte) 0x00, (byte) 0x02, (byte) 0x00});

        Protocol698Frame.OAD selector_oad = new Protocol698Frame.OAD(new byte[]{(byte) 0x60, (byte) 0x01, (byte) 0x02, (byte) 0x00});
        Protocol698Frame.Data beginData = new Protocol698Frame.Data(Protocol698Frame.Data_Type.LONG_UNSIGNED_TYPE, begin);
        Protocol698Frame.Data endData = new Protocol698Frame.Data(Protocol698Frame.Data_Type.LONG_UNSIGNED_TYPE, end);
        Protocol698Frame.Data intervalData = new Protocol698Frame.Data(Protocol698Frame.Data_Type.LONG_UNSIGNED_TYPE, interval);
        Protocol698Frame.Selector2 selector2 = new Protocol698Frame.Selector2(selector_oad, beginData, endData,intervalData);

        Protocol698Frame.RSD rsd = new Protocol698Frame.RSD(2, selector2);

        Protocol698Frame.RCSD rcsd = new Protocol698Frame.RCSD(null);

        Protocol698Frame.GetRecord getRecord = new Protocol698Frame.GetRecord(oad, rsd, rcsd);
        Map map = new HashMap();
        map.put(ProtocolConstant.GET_RECORD_KEY, getRecord);
        map.put(ProtocolConstant.PIID_KEY, piid);

        byte[] apdu = Protocol698.PROTOCOL_698.makeAPDU(ProtocolConstant.CLIENT_APDU.GET_REQUEST.CLASS_ID, ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_RECORD.CLASS_ID, map);
        Log.i(TAG, "makeGetArchiveFrame, ctrlArea: " + DataConvertUtils.convertByteToString(ctrlArea.data));
        Log.i(TAG, "makeGetArchiveFrame, addrArea: " + DataConvertUtils.convertByteArrayToString(addressArea.data, false));
        Log.i(TAG, "makeGetArchiveFrame, apdu: " + DataConvertUtils.convertByteArrayToString(apdu, false));
        byte[] frame = Protocol698.PROTOCOL_698.makeFrame(ctrlArea, addressArea, apdu);
        Log.i(TAG, "makeGetArchiveFrame, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        return frame;
    }


}
