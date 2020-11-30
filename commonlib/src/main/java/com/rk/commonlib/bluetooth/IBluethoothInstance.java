package com.rk.commonlib.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

public interface IBluethoothInstance {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_LACATION = 2;
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 3;

    public static final String CONNECT_ACTION = "com.rk.commonlib.bluetooth.IBluethoothInstance.CONNECT";
    public static final String DISCONNECT_ACTION = "com.rk.commonlib.bluetooth.IBluethoothInstance.DISCONNECT";
    boolean startScan(Activity activity, boolean isBle, BluetoothAdapter.LeScanCallback leScanCallback);
    boolean stopScan(Activity activity, boolean isBle);

    boolean connect(String address);
    void disconnect();
    void close();

    byte[] sendAndReceiveSync(byte[] frame);
    boolean isDeviceConnected(String deviceAddr);
}
