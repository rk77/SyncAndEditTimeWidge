package com.rk.commonmodule.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.rk.commonmodule.protocol.protocol645.y1997.Protocol645Of97Constant;
import com.rk.commonmodule.protocol.protocol645.y1997.Protocol645Of97FrameMaker;
import com.rk.commonmodule.protocol.protocol645.y1997.Protocol645Of97FramePaser;
import com.rk.commonmodule.protocol.protocol645.y2007.Protocol645Constant;
import com.rk.commonmodule.protocol.protocol645.y2007.Protocol645FrameMaker;
import com.rk.commonmodule.protocol.protocol645.y2007.Protocol645FramePaser;
import com.rk.commonmodule.protocol.protocol698.Protocol698;
import com.rk.commonmodule.protocol.protocol698.Protocol698Frame;
import com.rk.commonmodule.protocol.protocol698.ProtocolConstant;
import com.rk.commonmodule.transfer.TransferManager;

import java.util.HashMap;
import java.util.Map;

public class MeterProtocolDetector {

    private static final String TAG = MeterProtocolDetector.class.getSimpleName();

    public enum METER_PROTOCOL_TYPE {
        PROTOCOL_645_97,
        PROTOCOL_645_07,
        PROTOCOL_698,
        PROTOCOL_NONE,

    }

    public enum BAUD_RATE {
        bps_1200,
        bps_2400,
        bps_4800,
        bps_9600,
    }

    public enum METER_TYPE {
        SINGLE_PHASE,
        THRESS_PHASE,
    }

    public enum PORT_485 {
        PORT_485_1,
        PORT_485_2,
        PORT_485_3,
        PORT_485_4,
    }

    public enum PHASE_INFO {
        PHASE_UNKNOWN,
        PHASE_A,
        PHASE_B,
        PHASE_C,
        PHASE_METER,
    }

    public static class MeterInfo {
        public METER_PROTOCOL_TYPE protocolType;
        public String address;
        public METER_TYPE meterType;
        public BAUD_RATE baudRateOf485;
        public PORT_485 port485ConnectLtu;
        public PHASE_INFO phaseInfo;
        public String lineLossInfo;
        public int baudRateOfMaintain485 = 0;
        public int baudRateOfMaintainLora = 0;

        //format:"A phase impedance|B phase impedance|C phase impedance"
        public String impedance;
        public MeterInfo(METER_PROTOCOL_TYPE type, String address) {
            this.protocolType = type;
            this.address = address;
        }
    }

    public static class ModeOf485 {
        public int mode_485_1 = 0;
        public int mode_485_2 = 0;
        public int mode_485_3 = 0;
        public int mode_485_4 = 0;
        public ModeOf485(int mode_1, int mode_2, int mode_3, int mode_4) {
            this.mode_485_1 = mode_1;
            this.mode_485_2 = mode_2;
            this.mode_485_3 = mode_3;
            this.mode_485_4 = mode_4;
        }
    }

    public static MeterInfo getMeterProtocol(Context context) {

        byte[] frame = make698Frame();
        byte[] recvFrame = TransferManager.getInstance(context).sendAndReceiveSync(frame);
        Log.i(TAG, "getMeterProtocol, recv frame: " + DataConvertUtils.convertByteArrayToString(recvFrame, false));

        if (recvFrame != null && recvFrame.length > 0) {

            boolean isOK = Protocol698.PROTOCOL_698.verify698Frame(recvFrame);
            Log.i(TAG, "getMeterProtocol, apdu begin: " + Protocol698.PROTOCOL_698.mApduBegin + ", end: " + Protocol698.PROTOCOL_698.mApduEnd);
            if (isOK) {
                final Map value = Protocol698.PROTOCOL_698.parseApud(DataConvertUtils.getSubByteArray(recvFrame,
                        Protocol698.PROTOCOL_698.mApduBegin, Protocol698.PROTOCOL_698.mApduEnd));

                Log.i(TAG, "getMeterProtocol, value: " + value);
                return new MeterInfo(METER_PROTOCOL_TYPE.PROTOCOL_698, (String)value.get("value"));
            }
        } else {

            //String address = getMeterAddress();

            byte[] recvDatas = null;
            Map recvMap = null;
            Map map = new HashMap();
            map.put(Protocol645Constant.ADDRESS, "AAAAAAAAAAAA");
            map.put(Protocol645Constant.CTRL_CODE, Protocol645Constant.ControlCode.READ_ADDRESS_REQUEST);
            map.put(Protocol645Constant.DATA_LENGTH, 0);
            byte[] frame645 = Protocol645FrameMaker.PROTOCOL_645_FRAME_MAKER.makeFrame(map);
            Log.i(TAG, "getMeterProtocol, frame645: " + DataConvertUtils.convertByteArrayToString(frame, false));
            //TransferManager.getInstance(sParentActivity).setChannel(new InfraredChannel());
            recvDatas = TransferManager.getInstance(context).sendAndReceiveSync(frame645);
            Log.i(TAG, "getMeterProtocol, recvDatas645: " + DataConvertUtils.convertByteArrayToString(recvDatas, false));
            if (recvDatas != null) {

                recvMap = Protocol645FramePaser.PROTOCOL_645_FRAME_PASER.parse(recvDatas);
                if (recvMap != null && recvMap.containsKey(Protocol645Constant.ADDRESS)) {
                    final String address1 = (String) recvMap.get(Protocol645Constant.ADDRESS);
                    if (address1 != null) {
                        return new MeterInfo(METER_PROTOCOL_TYPE.PROTOCOL_645_07, address1);
                    }
                }
            }

            map.clear();

            map.put(Protocol645Of97Constant.ADDRESS, "999999999999");
            map.put(Protocol645Of97Constant.CTRL_CODE, Protocol645Of97Constant.ControlCode.READ_DATA_REQUEST);
            map.put(Protocol645Of97Constant.DATA_LENGTH, 4);
            map.put(Protocol645Of97Constant.DATA_IDENTIFIER, Protocol645Of97Constant.DataIdentifier.POSITIVE_ACTIVE_TOTAL_POWER_DI);
            byte[] frame97 = Protocol645Of97FrameMaker.PROTOCOL_645_OF_97_FRAME_MAKER.makeFrame(map);
            Log.i(TAG, "getMeterProtocol, frame97: " + DataConvertUtils.convertByteArrayToString(frame97, false));

            recvDatas = TransferManager.getInstance(context).sendAndReceiveSync(frame97);
            Log.i(TAG, "getMeterProtocol, recvDatas97: " + DataConvertUtils.convertByteArrayToString(recvDatas, false));

            if (recvDatas != null) {
                recvMap = Protocol645Of97FramePaser.PROTOCOL_645_OF_97_FRAME_PASER.parse(recvDatas);
                if (recvMap != null && recvMap.containsKey(Protocol645Of97Constant.ADDRESS)) {
                    final String address = (String) recvMap.get(Protocol645Of97Constant.ADDRESS);
                    if (address != null) {
                        return new MeterInfo(METER_PROTOCOL_TYPE.PROTOCOL_645_97, address);
                    }
                }
            }
        }
        return new MeterInfo(METER_PROTOCOL_TYPE.PROTOCOL_NONE, null);
    }

    private static byte[] make698Frame() {
        Protocol698Frame.CtrlArea ctrlArea = new Protocol698Frame.CtrlArea(Protocol698Frame.DIR_PRM.CLIENT_REQUEST, false, false, 3);
        Protocol698Frame.SERV_ADDR serv_addr = new Protocol698Frame.SERV_ADDR(Protocol698Frame.ADDRESS_TYPE.WILDCARD, false,
                0, 6, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
        Protocol698Frame.AddressArea addressArea = new Protocol698Frame.AddressArea(serv_addr, (byte) 0x10);

        Protocol698Frame.OAD oad = new Protocol698Frame.OAD(new byte[] {(byte) 0x40, (byte) 0x01, (byte) 0x02, (byte) 0x00});
        Protocol698Frame.PIID piid = new Protocol698Frame.PIID(0, 1);
        Map map = new HashMap();
        map.put(ProtocolConstant.OAD_KEY, oad);
        map.put(ProtocolConstant.PIID_KEY, piid);

        byte[] apdu = Protocol698.PROTOCOL_698.makeAPDU(ProtocolConstant.CLIENT_APDU.GET_REQUEST.CLASS_ID, ProtocolConstant.CLIENT_APDU.GET_REQUEST.GET_REQUEST_NORMAL.CLASS_ID, map);
        Log.i(TAG, "make698Frame, ctrlArea: " + DataConvertUtils.convertByteToString(ctrlArea.data));
        Log.i(TAG, "make698Frame, addrArea: " + DataConvertUtils.convertByteArrayToString(addressArea.data, false));
        Log.i(TAG, "make698Frame, apdu: " + DataConvertUtils.convertByteArrayToString(apdu, false));

        byte[] frame = Protocol698.PROTOCOL_698.makeFrame(ctrlArea, addressArea, apdu);

        Log.i(TAG, "make698Frame, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        return frame;
    }



}
