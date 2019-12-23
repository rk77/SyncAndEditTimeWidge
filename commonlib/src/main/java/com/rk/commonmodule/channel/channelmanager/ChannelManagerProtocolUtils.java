package com.rk.commonmodule.channel.channelmanager;

import android.util.Log;

import com.rk.commonmodule.channel.ChannelConstant.Channel;
import com.rk.commonmodule.channel.ChannelConstant.ChannelCtrl;

public class ChannelManagerProtocolUtils {
    private static final String TAG = ChannelManagerProtocolUtils.class.getSimpleName();

    public static byte[] makeFrame(Channel channel, byte ctrl) {
        Log.i(TAG, "makeFrame");

        byte[] frame = null;
        switch (ctrl) {
            case ChannelCtrl.READ_RELEASE_CROL:
                break;
            case ChannelCtrl.CHANNEL_SET_CROL:
                frame = getChannelSetFrame(channel);
                break;
            case ChannelCtrl.LED_LIGHT_CROL:
                break;
            case ChannelCtrl.PARAS__QUERY_CROL:
                break;
            case ChannelCtrl.PARAS_SET_CROL:
                break;
            case ChannelCtrl.SUB_CARD_RESET_CROL:
                break;
            case ChannelCtrl.UPGRADE_CROL:
                break;
            default:
                Log.i(TAG, "makeFrame, no this ctrl code: " + ctrl);
                break;
        }

        return frame;

    }

    private static byte[] getChannelSetFrame(Channel channel) {
        byte[] frame = null;
        switch (channel) {
            case CHANNEL_INFRARED:
                frame = new byte[]{0x01, 0x00, 0x05, 0x01};
                break;
            case CHANNEL_485:
                frame = new byte[]{0x01, 0x00, 0x05, 0x00};
                break;
            case CHANNEL_PLC:
                break;
            case CHANNEL_NET:
                break;
            default:
                break;
        }
        return frame;
    }
}
