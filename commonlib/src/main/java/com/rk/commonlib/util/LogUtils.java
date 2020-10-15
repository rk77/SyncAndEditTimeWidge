package com.rk.commonlib.util;

import android.text.TextUtils;
import android.util.Log;

public class LogUtils {
    public static void i(String info) {
        String fileName = new Exception().getStackTrace()[1].getFileName(); //获取调用者的文件名
        String className = fileName.split("\\.")[0];
        String method_name = new Exception().getStackTrace()[1].getMethodName(); //获取调用者的方法名
        if (TextUtils.isEmpty(info)) {
            Log.i(className, method_name);
        } else {
            Log.i(className, method_name + ", " + info);
        }
    }

    public static void d() {

    }

    public static void e() {

    }
}
