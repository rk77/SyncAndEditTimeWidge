package com.rk.commonmodule.protocol.protocol698;

public class ProtocolConstant {
    public final static String PIID_KEY = "PIID";
    public final static String OAD_KEY = "OAD";
    public final static String TIME_LABLE_KEY = "TIME_LABLE_KEY";

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
        }

        public final static class SET_REQUEST {
            public final static int CLASS_ID = 6;
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
        }

        //设置响应
        public final static class SET_RESPONSE {
            public final static int CLASS_ID = 134;
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
}
