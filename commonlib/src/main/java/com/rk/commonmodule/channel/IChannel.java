package com.rk.commonmodule.channel;

import java.util.Map;

public interface IChannel {
    boolean channelOpen(int flag);
    boolean channelClose(int flag);
    int channelSend(byte[] data, int length);
    byte[] channelReceive();
    int setChannelParams(Map map);
    Map getChannelParams();
    ChannelConstant.Channel getChannelType();
}
