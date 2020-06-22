package com.rk.commonlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.rk.commonlib.download.listener.MyDownloadListener;
import com.rk.commonlib.download.util.DownloadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class UpdateChecker {
    private static final String TAG = UpdateChecker.class.getSimpleName();
    private Context mContext;
    //检查版本信息的线程
    private Thread mThread;
    //版本对比地址
    private String mCheckUrl;
    private MyAppVersion mAppVersion;

    private AlertDialog mDialog;

    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    private File apkFile;
    //新版本保存文件名
    public static String apkFileName = "app.apk";
    private boolean showAlert = true;//是否显示新本版alert。默认显示
    //检查更新后，判断当前已是最新版本时的提示消息
    private String checkMessage = "当前已是最新版本";
    private Handler handler;
    private boolean sysDown = true;//启动系统下载器
    private ProgressBar update_progress;
    private TextView tv_title;
    private TextView tv_update_info;
    private Button btn_update;
    private TextView tv_ignore;
    private LinearLayout ll_progress;
    private TextView tv_progress;
    private TextView tv_size;
    public void setCheckUrl(String url) {
        mCheckUrl = url;
    }

    /**
     * @author lixiaojin
     * @createon 2018-08-21 14:44
     * @Describe 初始化操作
     */
    public UpdateChecker(Context context, Handler handlerIn) {
        mContext = context;
        handler = handlerIn;

    }

    public void checkUpdates() {
        Log.i(TAG, "checkUpdates");
        try {
            if (mCheckUrl == null) {
                Message msg = new Message();
                msg.what = MyAppVersion.URLFAILED;
                handler.sendMessage(msg);
                return;
            }
            final int versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            Log.i(TAG, "checkUpdates, apk version code: " + versionCode);
            mThread = new Thread() {
                @Override
                public void run() {
                    //if (isNetworkAvailable(mContext)) {
                    Message msg = new Message();
                    String json = sendPost();
                    if (json != null) {
                        MyAppVersion appVersion = parseJson(json);
                        msg.what = MyAppVersion.CONNECTSUCCESS;
                        msg.obj = appVersion;
                        mAppVersion = (MyAppVersion) msg.obj;
                        Log.i(TAG, "checkUpdates, updating app versionCode: " + mAppVersion.getApkCode());
                        if (mAppVersion.getApkCode() > versionCode) {
                            Message message = new Message();
                            message.what = MyAppVersion.NEW_VERSION;
                            message.obj = checkMessage;
                            handler.sendMessage(message);
                        } else {
                            sendResult();
                        }
                        //handlers.sendMessage(msg);
                    } else {
                        msg.what = MyAppVersion.PARSEERROR;
                        handler.sendMessage(msg);
                    }
                }
            };
            mThread.start();
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = MyAppVersion.NETFAILED;
            handler.sendMessage(msg);
        }
    }
    /**
     * @author lixiaojin
     * @createon 2018-08-21 14:45
     * @Describe 检查更新
     */
    public void checkForUpdates() {
        if(mCheckUrl == null) {
            //throw new Exception("checkUrl can not be null");
            return;
        }
        final Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                if (msg.what == MyAppVersion.CONNECTSUCCESS) {
                    mAppVersion = (MyAppVersion) msg.obj;
                    try{
                        int versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
                        Log.d(TAG,"versionCode = " + versionCode);
                        if (mAppVersion.getApkCode() > versionCode) {
                            showUpdateDialog();
                        }else{
                            sendResult();
                        }
                    }catch (PackageManager.NameNotFoundException ignored){
                    }
                }
            }
        };
        //检查服务端版本信息
        mThread = new Thread() {
            @Override
            public void run() {
                //if (isNetworkAvailable(mContext)) {
                Message msg = new Message();
                String json = sendPost();
                if (json!=null) {
                    //服务端版本信息获取成功后，发送Message
                    MyAppVersion appVersion = parseJson(json);
                    msg.what = MyAppVersion.CONNECTSUCCESS;
                    msg.obj = appVersion;
                    handler.sendMessage(msg);
                }else{
                    Log.e(TAG, "can't get app update json");
                }
            }
        };
        mThread.start();
    }

    protected String sendPost() {
        Log.i(TAG, "sendPost");
        HttpURLConnection uRLConnection = null;
        InputStream is = null;
        BufferedReader buffer = null;
        String result = null;
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int len = 0;
            URL url = new URL(mCheckUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int code = conn.getResponseCode();
            String message = conn.getResponseMessage();
            if(code == 200){ //请求成功
                InputStream inStream = conn.getInputStream();
                while ((len = inStream.read(data)) != -1) {
                    outStream.write(data, 0, len);
                }
                inStream.close();
                result = new String(outStream.toByteArray());//通过out.Stream.toByteArray获取到写的数据
            }else{
                //服务端版本文件请求失败，发送Message
                Message message1 = new Message();
                message1.what = MyAppVersion.CONNECTFAILED;
                message1.obj = "请求错误：code:" + code + "message:"+message;
                handler.sendMessage(message1);
            }
        } catch (Exception e) {
            Log.i(TAG, "sendPost, error: " + e.getMessage());
            Message msg = new Message();
            msg.what = MyAppVersion.NETFAILED;
            handler.sendMessage(msg);
        } finally {
            if(buffer!=null){
                try {
                    buffer.close();
                } catch (IOException e) {
                    Message msg = new Message();
                    msg.what = MyAppVersion.NETFAILED;
                    handler.sendMessage(msg);
                }
            }
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    Message msg = new Message();
                    msg.what = MyAppVersion.NETFAILED;
                    handler.sendMessage(msg);
                }
            }
            if(uRLConnection!=null){
                uRLConnection.disconnect();
            }
        }
        return result;
    }
    public MyAppVersion parseJson(String json) {
        MyAppVersion appVersion = new MyAppVersion();
        try {
            JSONObject obj = new JSONObject(json);
            String updateMessage = obj.optString(MyAppVersion.APK_UPDATE_CONTENT);
            String apkUrl = obj.optString(MyAppVersion.APK_DOWNLOAD_URL);
            String installNow = obj.optString(MyAppVersion.INSTALL_NOW);
            String versionName = obj.optString(MyAppVersion.VERSION_NAME);
            String size = obj.optString(MyAppVersion.SIZE);
            int apkCode = obj.optInt(MyAppVersion.APK_VERSION_CODE);
            appVersion.setApkCode(apkCode);
            appVersion.setApkUrl(apkUrl);
            appVersion.setUpdateMessage(updateMessage);
            appVersion.setInstallNow(installNow);
            appVersion.setVersionName(versionName);
            appVersion.setSize(size);
        } catch (JSONException e) {
            Log.e(TAG, "parse json error", e);
        }
        return appVersion;
    }

    public void showUpdateDialog() {
        Log.i(TAG, "showUpdateDialog");
        String installNow = mAppVersion.getInstallNow();
        String versionName = mAppVersion.getVersionName();
        String size = mAppVersion.getSize();
        if (showAlert){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            mDialog = builder.create ();
            View view = mDialog.getLayoutInflater().inflate(R.layout.update_dialog,null);

            tv_title = view.findViewById (R.id.tv_title);
            tv_update_info = view.findViewById (R.id.tv_update_info);
            btn_update = view.findViewById (R.id.btn_update);
            tv_ignore = view.findViewById (R.id.tv_ignore);
            update_progress = view.findViewById (R.id.update_progress);
            tv_progress = view.findViewById (R.id.tv_progress);
            ll_progress = view.findViewById (R.id.ll_progress);
            tv_size = view.findViewById (R.id.tv_size);
            tv_size.setText("版本大小：" + size);
            tv_title.setText ("是否升级到" + versionName + "版本？");
            String[] s = mAppVersion.getUpdateMessage ().split("n");
            String msg = "";
            for (int i = 0; i < s.length; i++){
                msg = msg + s[i] + "\n";
            }
            tv_update_info.setText(msg);
            btn_update.setOnClickListener (new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downLoadApk();
                }
            });

            mDialog.setView(view);
            mDialog.setCancelable(false);
            mDialog.show();
            mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            if (installNow.equals("0")) {
                tv_ignore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        Message message = new Message();
                        message.what = MyAppVersion.ENTERLOGIN;
                        handler.sendMessage(message);
                    }
                });
                tv_ignore.setVisibility(View.VISIBLE);
            } else {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) btn_update.getLayoutParams();
                lp.topMargin = 200;//DensityUtil.px2dip (mContext,200);
                lp.bottomMargin = 300; //DensityUtil.px2dip (mContext,300);
                btn_update.setLayoutParams(lp);
                tv_ignore.setVisibility (View.GONE);
            }

        }
    }


    private void downLoadApk(){
        DownloadUtils downloadUtils = new DownloadUtils(mAppVersion.getApkUrl() + "/");
        downloadUtils.downloadFile(mAppVersion.getApkUrl(), new MyDownloadListener() {
            @Override
            public void onStart() {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_update.setVisibility (View.GONE);
                        tv_ignore.setVisibility (View.INVISIBLE);
                        ll_progress.setVisibility (View.VISIBLE);
                    }
                });

                /*mProgressDialog.show ();
                mProgressDialog.setMessage ("开始下载");*/
            }
            @Override
            public void onProgress(final int currentLength) {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        update_progress.setProgress (currentLength);
                        tv_progress.setText (currentLength+"%");
                    }
                });

               // mProgressDialog.setProgress (currentLength);
            }
            @Override
            public void onFinish(String localPath) {
                Log.i(TAG, "downLoadApk, onFinish, localPath: " + localPath);
                //mProgressDialog.dismiss ();
               // ll_progress.setVisibility (View.GONE);
                installApk(localPath);
            }
            @Override
            public void onFailure(final String errorInfo) {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ll_progress.setVisibility (View.GONE);
                        tv_ignore.setVisibility (View.VISIBLE);
                        tv_ignore.setText ("下载失败：" + errorInfo);
                    }
                });

            }
        });
    }

    /**
     * 安装APK文件
     */
    private void installApk(String filePath){
        File apkFile = new File(filePath);
        if (!apkFile.exists()){
            Log.i(TAG, "installApk, file not exist");
            return;
        }
        Log.i(TAG,"installApk, filePath = " + filePath);
        try{
            Intent i = new Intent(Intent.ACTION_VIEW);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0 android and more bigger
                uri = FileProvider.getUriForFile(mContext,
                        mContext.getApplicationContext().getPackageName() + ".providerrk", new File(filePath));
            }else{
                uri = Uri.fromFile(new File(filePath));
            }
            Log.i(TAG, "installApp, uri: " + uri);
            i.setDataAndType(uri, "application/vnd.android.package-archive");
            i.addCategory("android.intent.category.DEFAULT");
            mContext.grantUriPermission (mContext.getPackageName (),uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION| Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,"安装失败，请在文件管理器中找到" + MyAppVersion.APK_FILENAME + "进行安装。", Toast.LENGTH_LONG);
        }
    }

    /**
     * 当前已是最新，发布Message
     */
    private void sendResult(){
        Message message = new Message();
        message.what = MyAppVersion.ALREADY_NEW;
        message.obj = checkMessage;
        handler.sendMessage(message);
    }

    public boolean isShowAlert() {
        return showAlert;
    }

    public void setShowAlert(boolean showAlert) {
        this.showAlert = showAlert;
    }

    public String getCheckMessage() {
        return checkMessage;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public boolean isSysDown () {
        return sysDown;
    }

    public void setSysDown (boolean sysDown) {
        this.sysDown = sysDown;
    }
}



