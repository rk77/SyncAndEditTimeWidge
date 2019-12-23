package com.rk.commonmodule.channel;

import java.util.Map;

public interface IChannel {
    int channelOpen(int flag);
    int channelClose(int flag);
    int channelSend(byte[] data, int length);
    byte[] channelReceive();
    int setChannelParams(Map map);
    Map getChannelParams();
    ChannelConstant.Channel getChannelType();
}
