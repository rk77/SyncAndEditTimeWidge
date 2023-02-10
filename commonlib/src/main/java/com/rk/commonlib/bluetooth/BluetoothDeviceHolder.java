package com.rk.commonlib.bluetooth;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceHolder {
    public BluetoothDevice bluetoothDevice;
    public int rssi;
    public BluetoothDeviceHolder(BluetoothDevice device, int rssi) {
        this.bluetoothDevice = device;
        this.rssi = rssi;
    }
}