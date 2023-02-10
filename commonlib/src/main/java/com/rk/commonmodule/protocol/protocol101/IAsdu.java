package com.rk.commonmodule.protocol.protocol101;

public interface IAsdu {
    byte[] getData();
    void parseData(byte[] data, int begin);
}
