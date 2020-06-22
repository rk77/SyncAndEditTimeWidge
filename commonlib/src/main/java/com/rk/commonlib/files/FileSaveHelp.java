package com.rk.commonlib.files;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.rk.commonlib.R;

import java.io.File;

public enum FileSaveHelp {
    FILE_SAVE_HELP;
    private static final String TAG = "FileSaveHelp";


    public String saveToTxtFile(Activity baseActivity, String fileContent, String currentFileName, String tag, Context context) {
        String fileName = currentFileName + "." + tag;
        String saveSuccess = "";
        StringBuilder dataFile = new StringBuilder("");
        dataFile.append(fileContent + "\r\n");
        Log.d(TAG, "fileName = " + fileName);
        boolean result = false;
        try {
            result = new AppExternalFileWriter(baseActivity).writeDataToFile(fileName, dataFile.toString(), false, true, false);
            Log.d(TAG, "result = " + result);
            if (result) {
                saveSuccess = Environment.getExternalStorageDirectory().toString() + "/" + context.getString(R.string.rk_file) + "/" + fileName;
            } else {
                saveSuccess = "保存失败";
            }
        } catch (AppExternalFileWriter.ExternalFileWriterException e) {
            e.printStackTrace();
            saveSuccess = "发生异常错误." + e.getMessage();
        }
        if (result) {
            scanFile(baseActivity, fileName, tag, context);
        }
        return saveSuccess;
    }


    /**
     * 压缩文件
     *
     * @param baseActivity
     * @param filePath
     * @return
     */
    public String zipFile(Activity baseActivity, String filePath, Context context) {
        File file = new File(filePath);
        String zipPath = new AppExternalFileWriter(baseActivity).makeFileToZip(file);
        if (!zipPath.equals("")) {
            scanFile(zipPath, context);
        }
        return zipPath;
    }


    /**
     * 解压文件，传入压缩文件路径
     *
     * @param baseActivity
     * @param filePath
     * @return
     * @throws Exception
     */
    public String unZipFile(Activity baseActivity, String filePath, Context context) throws Exception {
        String zipPath = new AppExternalFileWriter(baseActivity).unZipFile(filePath);
        if (!zipPath.equals("")) {
            scanFile(zipPath, context);
        }
        return zipPath;
    }

    /**
     * 扫描文件
     *
     * @param activity
     * @param fileName
     * @param tag
     */
    public void scanFile(Activity activity, String fileName, String tag, Context context) {
        scanFile(Environment.getExternalStorageDirectory().toString() + "/" + context.getString(R.string.rk_file) + "/" + fileName + tag, context);
    }


    /**
     * 扫描文件
     *
     * @param filePath
     */
    public void scanFile(String filePath, Context context) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

}
