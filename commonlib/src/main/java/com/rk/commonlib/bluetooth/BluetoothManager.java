package com.rk.commonlib.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rk.commonmodule.protocol.protocol698.Protocol698Frame;
import com.rk.commonmodule.protocol.protocol698.ProtocolConstant;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothManager implements IBluethoothInstance{
    private static final String TAG = BluetoothManager.class.getSimpleName();

    public final static String ACTION_GATT_CONNECTED = "com.rk.commonlib.bluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.rk.commonlib.bluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.rk.commonlib.bluetooth.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_CHARACTERISTIC_READ = "com.rk.commonlib.bluetooth.ACTION_GATT_CHARACTERISTIC_READ";
    public final static String ACTION_GATT_CHARACTERISTIC_WRITE = "com.rk.commonlib.bluetooth.ACTION_GATT_CHARACTERISTIC_WRITE";
    public final static String ACTION_GATT_DESCRIPTOR_READ = "com.rk.commonlib.bluetooth.ACTION_GATT_DESCRIPTOR_READ";
    public final static String ACTION_GATT_DESCRIPTOR_WRITE = "com.rk.commonlib.bluetooth.ACTION_GATT_DESCRIPTOR_WRITE";
    public final static String ACTION_GATT_MTU_WRITE = "com.rk.commonlib.bluetooth.ACTION_GATT_MTU_WRITE";

    private static Context sContext;
    private IBluethoothInstance mCurrentBluetoothInstance;

    private BluetoothManager() {
        if (sContext != null) {

        }
    }

    private static class InstanceHolder {
        private static final BluetoothManager INSTANCE = new BluetoothManager();
    }

    public static BluetoothManager getInstance(Context context) {
        sContext = context.getApplicationContext();
        return InstanceHolder.INSTANCE;
    }

    public void setCurrentBluetoothType(int type) {
        if (type == 0) {
            mCurrentBluetoothInstance = EnergyControlBluetoothInstance.getInstance(sContext);
        } else if (type == 1) {
            mCurrentBluetoothInstance = TerminalFourBluetoothInstance.getInstance(sContext);
        } else if (type == 2) {
            mCurrentBluetoothInstance = BluetoothToSerialPortInstance.getInstance(sContext);
        } else if (type == 3) {
            mCurrentBluetoothInstance = LTUBluetoothInstance.getInstance(sContext);
        }
    }

    public boolean startScan(Activity activity, boolean isBle, BluetoothAdapter.LeScanCallback leScanCallback) {
        if (mCurrentBluetoothInstance == null) {
            return false;
        }
        return mCurrentBluetoothInstance.startScan(activity, isBle, leScanCallback);

    }

    public boolean stopScan(Activity activity, boolean isBle) {
        if (mCurrentBluetoothInstance == null) {
            return false;
        }
        return mCurrentBluetoothInstance.stopScan(activity, isBle);
    }

    public boolean connect(final String address) {
        if (mCurrentBluetoothInstance == null) {
            return false;
        }
        return mCurrentBluetoothInstance.connect(address);
    }

    public synchronized byte[] sendAndReceiveSync(byte[] frame) {
        if (mCurrentBluetoothInstance == null) {
            return null;
        }
        return mCurrentBluetoothInstance.sendAndReceiveSync(frame);
    }


    public void disconnect() {
        if (mCurrentBluetoothInstance == null) {
            return;
        }
        mCurrentBluetoothInstance.disconnect();
    }

    public void close() {
        if (mCurrentBluetoothInstance == null) {
            return;
        }
        mCurrentBluetoothInstance.close();
    }

    public boolean isDeviceConnected(String deviceAddr) {
        if (mCurrentBluetoothInstance == null) {
            return false;
        }
        return mCurrentBluetoothInstance.isDeviceConnected(deviceAddr);
    }

}
