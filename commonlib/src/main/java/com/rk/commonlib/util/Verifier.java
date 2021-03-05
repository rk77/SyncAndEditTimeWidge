package com.rk.commonlib.util;

import java.util.List;
import java.util.Map;

public class Verifier {

    public static boolean isNullOrEmptyObject(Object obj) {
        return obj == null || obj.toString().trim().isEmpty();
    }

    public static boolean isNullOrEmptyArray(Object[] array) {
        if (array == null) {
            return true;
        }

        if (array.length == 0) {
            return true;
        }

        boolean eachItemIsNull = true;
        for (Object item : array) {
            if (item != null) {
                eachItemIsNull = false;
            }
        }
        return eachItemIsNull;
    }

    public static boolean isNullOrEmptyList(List<?> list) {
        if (list == null) {
            return true;
        }

        if (list.isEmpty()) {
            return true;
        }

        boolean eachItemIsNull = true;
        for (Object item : list) {
            if (item != null) {
                eachItemIsNull = false;
            }
        }
        return eachItemIsNull;
    }

    public static boolean isNullOrEmptyMap(Map<?, ?> map) {
        if (map == null) {
            return true;
        }

        if (map.isEmpty()) {
            return true;
        }

        return false;
    }
}
