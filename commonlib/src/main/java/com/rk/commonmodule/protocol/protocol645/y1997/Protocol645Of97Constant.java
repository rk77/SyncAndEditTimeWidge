package com.rk.commonmodule.protocol.protocol645.y1997;

public class Protocol645Of97Constant {
    public static final String ADDRESS = "address"; // String
    public static final String CTRL_CODE = "control_code"; // byte
    public static final String DATA_LENGTH = "data_length"; // byte
    public static final String DATA_IDENTIFIER = "data_identifier"; // String
    public static final String DATA = "data"; // String

    public static final String FRAME_OK = "frame_ok";

    public static final byte FRAME_BEGIN = (byte) 0x68;
    public static final byte FRAME_END = (byte) 0x16;

    public static final int ADDRESS_LENGTH = 12;

    public static class ControlCode {
        public static final String READ_ADDRESS_VALUE_KEY = "address_value";
        // read meter data
        public static final byte READ_DATA_REQUEST = (byte) 0x01;
        public static final byte READ_DATA_RESPOND_OK = (byte) 0x81;
        public static final byte READ_DATA_RESPOND_OK_CONTINUE = (byte) 0xA1;
        public static final byte READ_DATA_RESPOND_ERROR = (byte) 0xC1;
    }

    public static class DataIdentifier {
        public static final String POSITIVE_ACTIVE_TOTAL_POWER_DI= "9010";
        public static final String POSITIVE_ACTIVE__TOTAL_POWER_KEY = "positive_active_totol_power"; // String
    }
}
