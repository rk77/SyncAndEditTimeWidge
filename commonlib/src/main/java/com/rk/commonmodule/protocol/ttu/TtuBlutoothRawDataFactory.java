package com.rk.commonmodule.protocol.ttu;

import android.text.TextUtils;

import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.BL_CONNECT_RESPOND;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.BL_CONNECT_SET;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.FILE_READ_REQUEST;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.FILE_READ_RESPOND;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.FILE_WRITE_REQUEST;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.FILE_WRITE_RESPOND;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.MQTT_REQUEST;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.MQTT_RESPOND;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.SEG_FRAME_CONFIRM;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.SHELL_REQUEST;
import static com.rk.commonmodule.protocol.ttu.TtuBluetoothFrame.FrameType.SHELL_RESPOND;

public class TtuBlutoothRawDataFactory {

    public ITtuBluetoothRawData parseRawData(TtuBluetoothFrame.FrameType type, byte[] data) {
        switch (type) {
            case SEG_FRAME_CONFIRM:
                return new SegConfirmRawData();
            case MQTT_REQUEST:
                MqttRequestRawData mqttRequestRawData = new MqttRequestRawData();
                mqttRequestRawData.parse(data, 0);
                return mqttRequestRawData;
            case MQTT_RESPOND:
                MqttRespondRawData mqttRespondRawData = new MqttRespondRawData();
                mqttRespondRawData.parse(data, 0);
                return mqttRespondRawData;
            case SHELL_REQUEST:
                ShellRequestRawData shellRequestRawData = new ShellRequestRawData();
                shellRequestRawData.parse(data, 0, data.length);
                return shellRequestRawData;
            case SHELL_RESPOND:
                ShellRespondRawData shellRespondRawData = new ShellRespondRawData();
                shellRespondRawData.parse(data, 0, data == null ? 0 : data.length);
                return shellRespondRawData;
            case FILE_WRITE_REQUEST:
                FileWriteRequestRawData fileWriteRequestRawData = new FileWriteRequestRawData();
                fileWriteRequestRawData.parse(data, 0);
                return fileWriteRequestRawData;
            case FILE_WRITE_RESPOND:
                FileWriteRespondRawData fileWriteRespondRawData = new FileWriteRespondRawData();
                return fileWriteRespondRawData;
            case FILE_READ_REQUEST:
                FileReadRequestRawData fileReadRequestRawData = new FileReadRequestRawData();
                fileReadRequestRawData.parse(data, 0);
                return fileReadRequestRawData;
            case FILE_READ_RESPOND:
                FileReadRespondRawData fileReadRespondRawData = new FileReadRespondRawData();
                fileReadRespondRawData.parse(data, 0);
                return fileReadRespondRawData;
            case BL_CONNECT_SET:
                BtConnectSetRawData btConnectSetRawData = new BtConnectSetRawData();
                btConnectSetRawData.parse(data, 0, data.length);
                return btConnectSetRawData;
            case BL_CONNECT_RESPOND:
                BtConnectRespondRawData btConnectRespondRawData = new BtConnectRespondRawData();
                btConnectRespondRawData.parse(data, 0);
                return btConnectRespondRawData;
            default:
                return null;
        }
    }
    public static class SegConfirmRawData implements ITtuBluetoothRawData {

        @Override
        public void parse(byte[] frame, int begin) {
        }

        @Override
        public void parse(byte[] frame, int begin, int length) {

        }

        @Override
        public byte[] getData() {
            return null;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return SEG_FRAME_CONFIRM;
        }
    }

    public static class MqttRequestRawData implements ITtuBluetoothRawData {

        public byte[] topic;
        public byte[] payload;
        public byte[] data;

        public MqttRequestRawData() {

        }

        public MqttRequestRawData(byte[] topic, byte[] payload) {
            this.topic = topic;
            this.payload = payload;
            int size = 1 + (topic != null ? topic.length : 0) + 2 + (payload != null ? payload.length : 0);
            this.data = new byte[size];
            byte[] topicLength = new byte[1];
            byte[] payloadLength = new byte[2];
            topicLength[0] = (byte) (topic != null ? topic.length : 0);
            payloadLength[0] = (byte) ((payload != null ? payload.length : 0) & 0xFF);
            payloadLength[1] = (byte) (((payload != null ? payload.length : 0) >> 8) & 0xFF);
            int pos = 0;
            System.arraycopy(topicLength, 0, this.data, pos, topicLength.length);
            pos = pos + topicLength.length;
            if (topic != null) {
                System.arraycopy(topic, 0, this.data, pos, topic.length);
                pos = pos + topic.length;
            }
            System.arraycopy(payloadLength, 0, this.data, pos, payloadLength.length);
            pos = pos + payloadLength.length;
            if (payload != null) {
                System.arraycopy(payload, 0, this.data, pos, payload.length);
                pos = pos + payload.length;
            }

        }
        @Override
        public void parse(byte[] frame, int begin) {
            try {
                int pos = begin;
                int topicLen = frame[begin];
                pos = pos + 1;
                this.topic = new byte[topicLen];
                System.arraycopy(frame, pos, this.topic, 0, topicLen);
                pos = pos + topicLen;
                int payloadLen = frame[pos] + frame[pos + 1] * 256;
                pos = pos + 2;
                this.payload = new byte[payloadLen];
                System.arraycopy(frame, pos, this.payload, 0, payloadLen);
            } catch (Exception e) {
                LogUtils.e("error: " + e.getMessage());
            }
        }

        @Override
        public void parse(byte[] frame, int begin, int length) {

        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return MQTT_REQUEST;
        }
    }

    public static class MqttRespondRawData implements ITtuBluetoothRawData {

        public byte[] topic;
        public byte[] payload;
        public byte[] data;

        public MqttRespondRawData() {

        }

        public MqttRespondRawData(byte[] topic, byte[] payload) {
            this.topic = topic;
            this.payload = payload;
            int size = 1 + (topic != null ? topic.length : 0) + 2 + (payload != null ? payload.length : 0);
            this.data = new byte[size];
            byte[] topicLength = new byte[1];
            byte[] payloadLength = new byte[2];
            topicLength[0] = (byte) (topic != null ? topic.length : 0);
            payloadLength[0] = (byte) ((topic != null ? topic.length : 0) & 0xFF);
            payloadLength[1] = (byte) (((topic != null ? topic.length : 0) >> 8) & 0xFF);
            int pos = 0;
            System.arraycopy(topicLength, 0, this.data, pos, topicLength.length);
            pos = pos + topicLength.length;
            if (topic != null) {
                System.arraycopy(topic, 0, this.data, pos, topic.length);
                pos = pos + topic.length;
            }
            System.arraycopy(payloadLength, 0, this.data, pos, payloadLength.length);
            pos = pos + payloadLength.length;
            if (payload != null) {
                System.arraycopy(payload, 0, this.data, pos, payload.length);
                pos = pos + payload.length;
            }

        }
        @Override
        public void parse(byte[] frame, int begin) {
            try {
                int pos = begin;
                int topicLen = frame[begin];
                pos = pos + 1;
                this.topic = new byte[topicLen];
                System.arraycopy(frame, pos, this.topic, 0, topicLen);
                LogUtils.i("mqtt respond, topic:" + DataConvertUtils.getByteArray2AsciiString(this.topic));
                pos = pos + topicLen;
                int payloadLen = (frame[pos] & 0xFF) + (frame[pos + 1] & 0xFF) * 256;
                LogUtils.i("mqtt respond, payload len: " + payloadLen);
                pos = pos + 2;
                this.payload = new byte[payloadLen];
                System.arraycopy(frame, pos, this.payload, 0, payloadLen);
                LogUtils.i("mqtt respond, payload:" + DataConvertUtils.getByteArray2AsciiString(this.payload));
                pos = pos + payloadLen;
                this.data = new byte[1 + 2 + topicLen + payloadLen];
                System.arraycopy(frame, begin, this.data, 0, this.data.length);
            } catch (Exception e) {
                LogUtils.e("error: " + e.getMessage());
            }
        }

        @Override
        public void parse(byte[] frame, int begin, int length) {

        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return MQTT_RESPOND;
        }
    }

    public static class ShellRequestRawData implements ITtuBluetoothRawData {

        public String shellCmd;
        public byte[] data;

        public ShellRequestRawData() {

        }

        public ShellRequestRawData(String shellCmd) {
            if (TextUtils.isEmpty(shellCmd)) {
                return;
            }
            this.shellCmd = shellCmd;
            this.data = DataConvertUtils.assicString2ByteArray(shellCmd);
        }

        @Override
        public void parse(byte[] frame, int begin) {

        }

        @Override
        public void parse(byte[] frame, int begin, int length) {
            try {
                this.shellCmd = DataConvertUtils.getByteArray2AsciiString(frame, begin, begin + length - 1, false);
                this.data = DataConvertUtils.getSubByteArray(frame, begin, begin + length - 1);
            } catch (Exception e) {
                LogUtils.e("error: " + e.getMessage());
            }
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return SHELL_REQUEST;
        }
    }

    public static class ShellRespondRawData implements ITtuBluetoothRawData {

        public String shellCmd;
        public byte[] data;

        public ShellRespondRawData() {

        }

        public ShellRespondRawData(String shellCmd) {
            if (TextUtils.isEmpty(shellCmd)) {
                return;
            }
            this.shellCmd = shellCmd;
            this.data = DataConvertUtils.assicString2ByteArray(shellCmd);
        }

        @Override
        public void parse(byte[] frame, int begin) {

        }

        @Override
        public void parse(byte[] frame, int begin, int length) {
            try {
                this.shellCmd = DataConvertUtils.getByteArray2AsciiString(frame, begin, begin + length - 1, false);
                this.data = DataConvertUtils.getSubByteArray(frame, begin, begin + length - 1);
            } catch (Exception e) {
                LogUtils.e("error: " + e.getMessage());
            }
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return SHELL_RESPOND;
        }
    }

    public static class FileWriteRequestRawData implements ITtuBluetoothRawData {

        public String filePath;
        public byte[] content;
        public byte[] data;

        public FileWriteRequestRawData() {

        }

        public FileWriteRequestRawData(String filePath, byte[] content) {
            if (TextUtils.isEmpty(filePath)) {
                return;
            }
            this.filePath = filePath;
            this.content = content;
            int pos = 0;
            byte[] filePathData = DataConvertUtils.assicString2ByteArray(filePath);
            byte[] dirLenData = new byte[1];
            dirLenData[0] = (byte) (filePathData != null ? filePathData.length : 0);

            int contentLen = (content != null ? content.length : 0);
            byte[] contentLenData = new byte[2];
            contentLenData[0] = (byte) (contentLen & 0xFF);
            contentLenData[1] = (byte) ((contentLen >> 8) & 0xFF);

            int size = 1 + dirLenData[0] + 2 + contentLen;
            this.data = new byte[size];

            System.arraycopy(dirLenData, 0, this.data, pos, dirLenData.length);
            pos = pos + dirLenData.length;
            if (filePathData != null) {
                System.arraycopy(filePathData, 0, this.data, pos, filePathData.length);
                pos = pos + filePathData.length;
            }
            System.arraycopy(contentLenData, 0, this.data, pos, contentLenData.length);
            pos = pos + contentLenData.length;
            if (content != null) {
                System.arraycopy(content, 0, this.data, pos, content.length);
                pos = pos + content.length;
            }
        }

        @Override
        public void parse(byte[] frame, int begin) {
            try {
                int pos = begin;
                int dirLen = frame[begin];
                pos = pos + 1;
                this.filePath = DataConvertUtils.getByteArray2AsciiString(frame, pos, pos + dirLen - 1, false);
                pos = pos + dirLen;
                int contentLen = frame[pos] + frame[pos + 1];
                pos = pos + 2;
                this.content = DataConvertUtils.getSubByteArray(frame, pos, pos + contentLen - 1);

            } catch (Exception e) {
                LogUtils.e("error: " + e.getMessage());
            }
        }

        @Override
        public void parse(byte[] frame, int begin, int length) {
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return FILE_WRITE_REQUEST;
        }
    }

    public static class FileWriteRespondRawData implements ITtuBluetoothRawData {

        public FileWriteRespondRawData() {

        }

        @Override
        public void parse(byte[] frame, int begin) {

        }

        @Override
        public void parse(byte[] frame, int begin, int length) {
        }

        @Override
        public byte[] getData() {
            return null;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return FILE_WRITE_RESPOND;
        }
    }

    public static class FileReadRequestRawData implements ITtuBluetoothRawData {

        public String filePath;
        public byte[] data;

        public FileReadRequestRawData() {

        }

        public FileReadRequestRawData(String filePath) {
            if (TextUtils.isEmpty(filePath)) {
                return;
            }
            this.filePath = filePath;
            int pos = 0;
            byte[] filePathData = DataConvertUtils.assicString2ByteArray(filePath);
            byte[] dirLenData = new byte[1];
            dirLenData[0] = (byte) (filePathData != null ? filePathData.length : 0);

            int size = 1 + dirLenData[0];
            this.data = new byte[size];

            System.arraycopy(dirLenData, 0, this.data, pos, dirLenData.length);
            pos = pos + dirLenData.length;
            if (filePathData != null) {
                System.arraycopy(filePathData, 0, this.data, pos, filePathData.length);
                pos = pos + filePathData.length;
            }
        }

        @Override
        public void parse(byte[] frame, int begin) {
            try {
                int pos = begin;
                int dirLen = frame[begin];
                pos = pos + 1;
                this.filePath = DataConvertUtils.getByteArray2AsciiString(frame, pos, pos + dirLen - 1, false);

            } catch (Exception e) {
                LogUtils.e("error: " + e.getMessage());
            }
        }

        @Override
        public void parse(byte[] frame, int begin, int length) {
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return FILE_READ_REQUEST;
        }
    }

    public static class FileReadRespondRawData implements ITtuBluetoothRawData {

        public byte[] content;
        public byte[] data;

        public FileReadRespondRawData() {

        }

        public FileReadRespondRawData(byte[] content) {

            this.content = content;
            int pos = 0;

            int contentLen = (content != null ? content.length : 0);
            byte[] contentLenData = new byte[2];
            contentLenData[0] = (byte) (contentLen & 0xFF);
            contentLenData[1] = (byte) ((contentLen >> 8) & 0xFF);
            int size = 2 + contentLen;
            this.data = new byte[size];

            System.arraycopy(contentLenData, 0, this.data, pos, contentLenData.length);
            pos = pos + contentLenData.length;
            if (content != null) {
                System.arraycopy(content, 0, this.data, pos, content.length);
                pos = pos + content.length;
            }
        }

        @Override
        public void parse(byte[] frame, int begin) {
            try {
                int pos = begin;

                int contentLen = frame[pos] + frame[pos + 1] * 256;
                pos = pos + 2;
                this.content = DataConvertUtils.getSubByteArray(frame, pos, pos + contentLen - 1);

            } catch (Exception e) {
                LogUtils.e("error: " + e.getMessage());
            }
        }

        @Override
        public void parse(byte[] frame, int begin, int length) {
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return FILE_READ_RESPOND;
        }
    }

    public static class BtConnectSetRawData implements ITtuBluetoothRawData {

        public String btName;
        public byte[] data;

        public BtConnectSetRawData() {

        }

        public BtConnectSetRawData(String btName) {
            if (TextUtils.isEmpty(btName)) {
                return;
            }
            this.btName = btName;
            this.data = DataConvertUtils.assicString2ByteArray(btName);
        }

        @Override
        public void parse(byte[] frame, int begin) {

        }

        @Override
        public void parse(byte[] frame, int begin, int length) {
            try {
                this.btName = DataConvertUtils.getByteArray2AsciiString(frame, begin, begin + length - 1, false);
                this.data = DataConvertUtils.getSubByteArray(frame, begin, begin + length - 1);
            } catch (Exception e) {
                LogUtils.e("error: " + e.getMessage());
            }
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return BL_CONNECT_SET;
        }
    }

    public enum BT_CON_RSPND_TYPE {
        SUCC(0),
        SCAN_FAIL(1),
        CON_FAIL(2),
        OTHER(3);

        private final int value;

        private BT_CON_RSPND_TYPE(int value) {
            this.value = value;
        }

        public static BT_CON_RSPND_TYPE valueOf(int value) {
            switch (value) {
                case 0:
                    return SUCC;
                case 1:
                    return SCAN_FAIL;
                case 2:
                    return CON_FAIL;
                case 3:
                    return OTHER;
                default:
                    return null;
            }
        }
    }

    public static class BtConnectRespondRawData implements ITtuBluetoothRawData {

        public BT_CON_RSPND_TYPE respondType;

        public byte[] data;

        public BtConnectRespondRawData() {

        }

        public BtConnectRespondRawData(BT_CON_RSPND_TYPE type) {
            this.respondType = type;
            this.data = new byte[1];
            this.data[0] = (byte) type.value;
        }

        @Override
        public void parse(byte[] frame, int begin) {
            try {
                this.respondType = BT_CON_RSPND_TYPE.valueOf(frame[begin]);
                this.data = new byte[1];
                this.data[0] = frame[begin];
            } catch (Exception e) {

            }

        }

        @Override
        public void parse(byte[] frame, int begin, int length) {
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public TtuBluetoothFrame.FrameType getFrameType() {
            return BL_CONNECT_RESPOND;
        }
    }

}
