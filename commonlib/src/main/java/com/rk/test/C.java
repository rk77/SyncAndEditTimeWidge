package com.rk.test;

import com.rk.commonlib.util.LogUtils;

public class C extends B {
    public C() {
        LogUtils.i("C Constructor");
        intInA();
    }
}
