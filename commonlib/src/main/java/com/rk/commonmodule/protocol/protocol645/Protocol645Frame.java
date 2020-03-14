package com.rk.commonmodule.protocol.protocol645;

import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.Arrays;

public class Protocol645Frame {
    public byte[] mAddressArea;
    public byte mCtrlCode;
    public byte mDataLength;
    public byte[] mData;
    public byte mCs;

    @Override
    public String toString() {
        return "Protocol645Frame{" +
                "mAddressArea=" + DataConvertUtils.convertByteArrayToString(mAddressArea, true) +
                ", mCtrlCode=" + DataConvertUtils.convertByteToString(mCtrlCode) +
                ", mDataLength=" + DataConvertUtils.convertByteToString(mDataLength) +
                ", mData=" + DataConvertUtils.convertByteArrayToString(mData, false) +
                ", mCs=" + DataConvertUtils.convertByteToString(mCs) +
                '}';
    }
}
