package com.rk.commonmodule.protocol.protocol3761;

import android.util.Log;

public enum Protocol3761 {
    PROTOCOL_3761;
    private static final String TAG = Protocol3761.class.getSimpleName();
    public enum DIR {
        MASTER_STATION,
        TERMINAL,
    }
    public enum PRM {
        FROM_MASTER,
        FROM_SLAVE,
    }
    public enum FCV {
        AVAILABLE,
        UNAVAILABLE,
    }

    public enum FCB_OR_ACD {
        AVAILABLE,
        UNAVAILABLE,
    }
    public enum FUNCTION_CODE {
        VALUE_0,
        VALUE_1,
        VALUE_2,
        VALUE_3,
        VALUE_4,
        VALUE_5,
        VALUE_6,
        VALUE_7,
        VALUE_8,
        VALUE_9,
        VALUE_10,
        VALUE_11,
        VALUE_12,
        VALUE_13,
        VALUE_14,
        VALUE_15,
    }

    public byte makeCtrlArea(DIR dir, PRM prm, FCB_OR_ACD fcb_or_acd, FCV fcv, FUNCTION_CODE function_code) {
        byte ctrlArea = 0x00;
        switch (dir) {
            case TERMINAL:
                ctrlArea = (byte) (ctrlArea | 0x80);
                break;
            case MASTER_STATION:
                ctrlArea = (byte) (ctrlArea & 0x7F);
                break;
        }
        switch (prm) {
            case FROM_MASTER:
                ctrlArea = (byte) (ctrlArea | 0x40);
                break;
            case FROM_SLAVE:
                ctrlArea = (byte) (ctrlArea & 0xBF);
                break;
        }
        switch (fcb_or_acd) {
            case AVAILABLE:
                ctrlArea = (byte) (ctrlArea | 0x20);
                break;
            case UNAVAILABLE:
                ctrlArea = (byte) (ctrlArea & 0xDF);
                break;
        }
        switch (fcv) {
            case AVAILABLE:
                ctrlArea = (byte) (ctrlArea | 0x10);
                break;
            case UNAVAILABLE:
                ctrlArea = (byte) (ctrlArea & 0xEF);
                break;
        }
        byte funCode = (byte) (function_code.ordinal() & 0x0F);
        ctrlArea = (byte)(ctrlArea | funCode);
        return ctrlArea;
    }

    public byte[] makeAddressArea(String A1, int A2, int A3) {
        Log.i(TAG, "makeAddressArea, A1: " + A1 + ", A2: " + A2 + ", A3: " + A3);
        if (A1 == null || A1.length() != 4 || !isNumberString(A1)
                || A2 < 1 || A2 > 65535
                || A3 < 0 || A3 > 127) {
            return null;

        }
        byte[] address = new byte[5];
        address[0] = (byte) Integer.parseInt(A1.substring(2, 4), 16);
        address[1] = (byte) Integer.parseInt(A1.substring(0, 2), 16);

        address[2] = (byte) (A2 & 0xFF);
        address[3] = (byte) ((A2>>8) & 0xFF);

        address[4] = (byte) (A3 & 0xFF);

        return address;
    }

    public boolean isNumberString(String s) {
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


}
