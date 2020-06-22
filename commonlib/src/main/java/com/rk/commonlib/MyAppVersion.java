package com.rk.commonlib;

import com.rk.commonlib.download.AppVersion;

public class MyAppVersion extends AppVersion {
    public static final String SIZE = "size";
    private String size;
    public static final int URLFAILED = 105; //检查更新失败，
    public static final int NETFAILED = 107; //检查更新失败，
    public static final int UNKNOWNERROR = 106; //未知错误
    public static final int ENTERLOGIN = 108; //发现新版本
    public static final int PARSEERROR = 109; //解析错误
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
