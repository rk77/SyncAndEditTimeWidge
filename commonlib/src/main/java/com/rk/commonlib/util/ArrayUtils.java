package com.rk.commonlib.util;

import org.apache.poi.ss.formula.functions.T;

public class ArrayUtils {

    public static int getIdxOfItem (T[] array, T s) {
        int idx = -1;
        if (array == null || s == null) {
            return idx;
        }
        for (int i = 0; i < array.length; i++) {
            if (s.toString().equals(array[i].toString())) {
                idx = i;
                break;
            }
            if (array[i] == s) {
                idx = i;
                break;
            }
            if (array[i].equals(s)) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    public static <T> int getIdxOfPreItem (T[] array, T s) {
        int idx = -1;
        if (array == null || s == null) {
            return idx;
        }
        for (int i = 0; i < array.length; i++) {
            //LogUtils.i("item:" + array[i].toString());
            if ((array[i].toString()).startsWith(s.toString())) {
                idx = i;
                break;
            }
        }
        return idx;
    }
}
