package com.rk.syncclock;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveDataToFileUtils {

    private static boolean isExternalStorageAvailable() {
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }


    private static double getAvailableSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        return sdAvailSize;
    }

    public static void writeDataToFile(File file, String data, boolean append) {
        byte[] stringBuffer = data.getBytes();
        writeDataToFile(file, stringBuffer, append);
    }

    public static void writeDataToFile(File file, byte[] data, boolean append) {
        if (isExternalStorageAvailable()) {
            if (file.isDirectory()) {
                return;
            } else {
                if (file != null && data != null) {
                    double dataSize = data.length;
                    double remainingSize = getAvailableSpace();
                    if (dataSize >= remainingSize) {
                        return;
                    } else {
                        try {
                            FileOutputStream out = null;
                            out = new FileOutputStream(file, append);
                            out.write(data);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


}
