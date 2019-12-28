package com.rk.commonmodule.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    public static ArrayList<File> getSpecificFiles(String rootDir, String pattenName) {
        Log.i(TAG, "getSpecificFiles, rootDir: " + rootDir + ", filter name: " + pattenName);
        ArrayList<File> fileList = new ArrayList<>();
        if (TextUtils.isEmpty(rootDir) || TextUtils.isEmpty(pattenName)) {
            return null;
        }
        try {
            File rootFileDir = new File(rootDir);
            if (!rootFileDir.exists() || !rootFileDir.isDirectory()) {
                return null;
            }
            File[] files = rootFileDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                Log.i(TAG, "getSpecificFiles, file: " + file.getName());
                if (file.getName().contains(pattenName) && file.isFile()) {
                    fileList.add(file);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "getSpecificFiles, error: " + e.getMessage());
        }

        Log.i(TAG, "getSpecificFiles, file list size: "  + fileList.size());
        return fileList;
    }

    public static String getSuffiex(String filepath) {
        if (TextUtils.isEmpty(filepath)) {
            return "";
        }
        int index = filepath.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return filepath.substring(index + 1, filepath.length());
    }
}
