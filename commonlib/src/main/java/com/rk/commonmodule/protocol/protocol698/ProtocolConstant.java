package com.rk.commonmodule.protocol.protocol698;

import java.util.HashMap;
import java.util.Map;

public class ProtocolConstant {
    public final static String PIID_KEY = "PIID";
    public final static String PIID_ACD_KEY = "PIID_ACD_KEY";
    public final static String OAD_KEY = "OAD";
    public final static String DATA_KEY = "data";
    public final static String OMD_KEY = "OMD";
    public final static String OMD_PARAM_KEY = "omd_param";
    public final static String OAD_ARRAY_KEY = "OAD_ARRAY_KEY";
    public final static String TIME_LABLE_KEY = "TIME_LABLE_KEY";
    public final static String DAR_KEY = "dar";
    public final static String GET_RECORD_KEY = "GET_RECORD_KEY";
    public final static String GET_RESPONSE_NORMAL_KEY = "GET_RESPONSE_NORMAL_KEY";

    public final static String DATA_UNIT_KEY = "DATA_UNIT_KEY";
    public final static String DATA_VERIFY_INFO_KEY = "DATA_VERIFY_INFO_KEY";
    public final static String A_RECORD_ROW_KEY = "A_RECORD_ROW_KEY";
    public final static String A_RECORD_ROW_LIST_KEY = "A_RECORD_ROW_LIST_KEY";
    public final static String RCSD_KEY = "RCSD_KEY";

    public final static String FAC_CODE_KEY = "FAC_CODE_KEY";
    public final static String FAC_SW_VERSION_KEY = "FAC_SW_VERSION_KEY";
    public final static String FAC_SW_VERSION_DATE_KEY = "FAC_SW_VERSION_DATE_KEY";
    public final static String FAC_HW_VERSION_KEY = "FAC_HW_VERSION_KEY";
    public final static String FAC_HW_VERSION_DATE_KEY = "FAC_HW_VERSION_DATE_KEY";
    public final static String FAC_EX_INFO = "FAC_EX_INFO";


    //厂商代码（size(4)）+ 软件版本号（size(4)）+软件版本日期（size(6)）+硬件版本号（size(4)）+硬件版本日期（size(6)）+厂商扩展信息（size(8)



    public final static class LINK_APDU {
        public final static class LINK_REQUEST {
            public final static int CLASS_ID = 1;
        }

        public final static class LINK_RESPONSE {
            public final static int CLASS_ID = 129;
        }
    }

    public final static class CLIENT_APDU {
        public final static class CONNECT_REQUEST {
            public final static int CLASS_ID = 2;
        }

        public final static class RELEASE_REQUEST {
            public final static int CLASS_ID = 3;
        }

        public final static class GET_REQUEST {
            public final static int CLASS_ID = 5;
            public final static class GET_REQUEST_NORMAL {
                public final static int CLASS_ID = 1;
            }

            public final static class GET_REQUEST_NORMAL_LIST {
                public final static int CLASS_ID = 2;
            }

            public final static class GET_REQUEST_RECORD {
                public final static int CLASS_ID = 3;
            }
        }

        public final static class SET_REQUEST {
            public final static int CLASS_ID = 6;
            public final static class SET_REQUEST_NORMAL {
                public final static int CLASS_ID = 1;
            }

            public final static class SET_REQUEST_NORMAL_LIST {
                public final static int CLASS_ID = 2;
            }

            public final static class SET_THEN_GET_REQUEST_NORMAL_LIST {
                public final static int CLASS_ID = 3;
            }
        }

        //操作请求
        public final static class ACTION_REQUEST {
            public final static int CLASS_ID = 7;
            public final static class ACTION_REQUEST_NORMAL {
                public final static int CLASS_ID = 1;
            }
            public final static class ACTION_REQUEST_NORMAL_LIST {
                public final static int CLASS_ID = 2;
            }
        }
    }

    public final static class SERVER_APDU {
        //建立应用连接响应
        public final static class CONNECT_RESPONSE {
            public final static int CLASS_ID = 130;
        }

        //断开应用连接响应
        public final static class RELEASE_RESPONSE {
            public final static int CLASS_ID = 131;
        }

        //断开应用连接通知
        public final static class RELEASE_NOTIFICATION {
            public final static int CLASS_ID = 132;
        }

        //读取响应
        public final static class GET_RESPONSE {
            public final static int CLASS_ID = 133;
            public final static class GET_RESPONSE_NORMAL {
                public final static int CLASS_ID = 1;
            }

            public final static class GET_RESPONSE_NORMAL_LIST {
                public final static int CLASS_ID = 2;
            }

            public final static class GET_RESPONSE_RECORD {
                public final static int CLASS_ID = 3;
            }
        }

        //设置响应
        public final static class SET_RESPONSE {
            public final static int CLASS_ID = 134;
            public final static class SET_RESPONSE_NORMAL {
                public final static int CLASS_ID = 1;
            }

            public final static class SET_RESPONSE_NORMAL_LIST {
                public final static int CLASS_ID = 2;
            }

            public final static class SET_THEN_GET_RESPONSE_NORMAL_LIST {
                public final static int CLASS_ID = 3;
            }
        }

        //操作响应
        public final static class ACTION_RESPONSE {
            public final static int CLASS_ID = 135;
            //操作一个对象方法的响应
            public final static class ACTION_RESPONSE_NORMAL {
                public final static int CLASS_ID = 1;
            }
        }
    }

    public final static class SECURITY_APDU {
        //安全请求
        public final static class SECURITY_REQUEST {
            public final static int CLASS_ID = 16;
        }

        //安全响应
        public final static class SECURITY_RESPONSE {
            public final static int CLASS_ID = 144;
        }
    }

    public final static int INIT_FCS = 0xFFFF;

    public final static int[] FCS_TAB = {
        0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
        0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
        0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
        0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
        0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
        0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
        0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
        0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
        0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
        0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
        0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
        0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
        0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
        0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
        0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
        0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
        0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
        0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
        0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
        0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
        0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
        0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
        0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
        0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
        0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
        0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
        0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
        0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
        0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
        0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
        0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
        0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
    };

    public static final Map<String, String> TERMINAL_EVENT_MAP = new HashMap<String, String>() {{
        put("终端初始化", "31000200");
        put("终端版本变更", "31010200");
        put("状态量变位", "31040200");
        put("电能表时钟超差", "31050200");
        put("终端停/上电", "31060200");
        put("直流模拟量越上限", "31070200");
        put("直流模拟量越下限", "31080200");
        put("消息认证错误", "31090200");
        put("设备故障记录", "310A0200");
        put("电能表示度下降", "310B0200");
        put("电能量超差", "310C0200");
        put("电能表飞走", "310D0200");
        put("电能表停走", "310E0200");
        put("抄表失败", "310F0200");
        put("月通信流里超限", "31100200");
        put("发现未知电能表", "31110200");
        put("跨台区电能表事件", "31120200");
        put("终端对时事件", "31140200");
        put("遥控跳闸记录", "31150200");
        put("有功总电能量差动越限事件记录", "31160200");
        put("输出回路开关接入状态变位记录", "31170200");
        put("终端编程记录", "31180200");
        put("终端电流回路异常事件", "31190200");
        put("电能表在网状态切换事件", "311A0200");
        put("终端对电表校时记录", "311B0200");
        put("电能表数据变更监控记录", "311C0200");
        put("通信模块变更事件", "30300200");
    }};

    public static final Map<String, String> EVENT_RECORD_MAP = new HashMap<String, String>() {{
        put("事件记录序号", "20220200");
        put("事件发生时间", "201E0200");
        put("事件结束时间", "20200200");
        put("事件发生源", "20240200");
        put("事件上报状态", "33000200");
    }};

    public static final Map<String, String> CHANNEL_OAD_MAP = new HashMap<String, String>() {{
        put("45000200", "公网通信模块1");
        put("45100200", "以太网通信模块1");

        put("50040200", "日冻结");
        put("00100200", "正向有功电能");
        put("00200200", "反向有功电能");

        put("202A0200", "目标服务器地址");
        put("40010200", "通信地址");
        put("60400200", "采集启动时标");
        put("60410200", "采集成功时标");
        put("60420200", "采集存储时标");

    }};

    public static final Map<String, String> OAD_MAP = new HashMap<String, String>() {{
        put("20210200", "冻结时标");
        put("45100200", "以太网通信模块1");
        put("20230200", "冻结记录序号");
        put("50020200", "分钟冻结");
        put("50040200", "日冻结");
        put("00100200", "正向有功电能");
        put("00100201", "正向有功总电能");
        put("00200200", "反向有功电能");
        put("00200201", "反向有功总电能");
        put("202A0200", "目标服务器地址");
        put("40010200", "通信地址");
        put("60400200", "采集启动时标");
        put("60410200", "采集成功时标");
        put("60420200", "采集存储时标");

        put("20000200", "电压");
        put("20010200", "电流");
        put("20040200", "有功功率");
        put("20050200", "无功功率");
        put("200A0200", "功率因数");

    }};

    public static final Map<String, String[]> OAD_GROUP_MAP = new HashMap<String, String[]>() {{

        put("00100200", new String[]{"总", "费率1", "费率2", "费率3", "费率4"});
        put("00200200", new String[]{"总", "费率1", "费率2", "费率3", "费率4"});

        put("20000200", new String[]{"A相", "B相", "C相"});
        put("20010200", new String[]{"A相", "B相", "C相"});

        put("20040200", new String[]{"总", "A相", "B相", "C相"});
        put("20050200", new String[]{"总", "A相", "B相", "C相"});
        put("200A0200", new String[]{"总", "A相", "B相", "C相"});

    }};

    public static final Map<String, Protocol698Frame.Data_Type> OAD_DATA_TYPE_MAP = new HashMap<String, Protocol698Frame.Data_Type>() {{
        put("20210200", Protocol698Frame.Data_Type.DATE_TIME_S_TYPE);
        put("20230200", Protocol698Frame.Data_Type.DOUBLE_LONG_UNSIGNED_TYPE);
        put("00100200", Protocol698Frame.Data_Type.DOUBLE_LONG_UNSIGNED_TYPE);
        put("00100201", Protocol698Frame.Data_Type.DOUBLE_LONG_UNSIGNED_TYPE);
        put("00200200", Protocol698Frame.Data_Type.DOUBLE_LONG_UNSIGNED_TYPE);
        put("00200201", Protocol698Frame.Data_Type.DOUBLE_LONG_UNSIGNED_TYPE);
        put("202A0200", Protocol698Frame.Data_Type.TSA_TYPE);
        put("40010200", Protocol698Frame.Data_Type.OCTET_STRING_TYPE);
        put("60400200", Protocol698Frame.Data_Type.DATE_TIME_S_TYPE);
        put("60410200", Protocol698Frame.Data_Type.DATE_TIME_S_TYPE);
        put("60420200", Protocol698Frame.Data_Type.DATE_TIME_S_TYPE);

        put("20000200", Protocol698Frame.Data_Type.LONG_UNSIGNED_TYPE);
        put("20010200", Protocol698Frame.Data_Type.DOUBLE_LONG_TYPE);
        put("20040200", Protocol698Frame.Data_Type.DOUBLE_LONG_TYPE);
        put("20050200", Protocol698Frame.Data_Type.DOUBLE_LONG_TYPE);
        put("200A0200", Protocol698Frame.Data_Type.LONG_TYPE);

    }};

    public static final Map<String, String> OAD_DATA_UNIT_MAP = new HashMap<String, String>() {{
        put("00100200", "kWh");
        put("00100201", "kWh");
        put("00200200", "kWh");
        put("00200201", "kWh");

        put("20000200", "V");
        put("20010200", "A");
        put("20040200", "W");
        put("20050200", "var");
    }};

    public static final Map<String, Integer> OAD_DATA_SCALE_MAP = new HashMap<String, Integer>() {{
        put("00100200", -2);
        put("00100201", -2);
        put("00200200", -2);
        put("00200201", -2);

        put("20000200", -1);
        put("20010200", -3);
        put("20040200", -1);
        put("20050200", -1);
        put("200A0200", -3);
    }};
}
