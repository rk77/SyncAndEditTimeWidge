package com.rk.commonmodule.channel;

public class ChannelConstant {
    public enum Channel {
        CHANNEL_NONE,
        CHANNEL_INFRARED,
        CHANNEL_485,
        CHANNEL_PLC,
        CHANNEL_LORA,
        CHANNEL_NET,
        //below is not channel,but regarded as virtual channel.
        CHANNEL_SECURITY_UNIT,
        CHANNEL_ESAM,
    }

    public static class  ChannelCtrl {
        public static final byte READ_RELEASE_CROL = 0x00;//读版本
        public static final byte UPGRADE_CROL = 0x01;//升级命令
        public static final byte SUB_CARD_RESET_CROL = 0x02;//子卡软件复位（F+和PIC芯片同时复位）
        public static final byte PARAS_SET_CROL = 0x03;//参数配置
        public static final byte PARAS__QUERY_CROL = 0x04;//参数/状态查询
        public static final byte CHANNEL_SET_CROL = 0x05;//通道设置
        public static final byte LED_LIGHT_CROL = 0x06;//LED亮灭控制
    }
}
