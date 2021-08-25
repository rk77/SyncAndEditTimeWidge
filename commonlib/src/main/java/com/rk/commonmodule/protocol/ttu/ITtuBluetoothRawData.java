package com.rk.commonmodule.protocol.ttu;

public interface ITtuBluetoothRawData {
    void parse(byte[] frame, int begin);
    void parse(byte[] frame, int begin, int length);
    byte[] getData();
    TtuBluetoothFrame.FrameType getFrameType();
}
