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
import android.bluetooth.BluetoothManager;
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

import java.util.ArrayList;
import java.util.List;

public class BluetoothInstance {
    private static final String TAG = BluetoothInstance.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_LACATION = 2;
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 3;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public final static String ACTION_GATT_CONNECTED = "com.rk.commonlib.bluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.rk.commonlib.bluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.rk.commonlib.bluetooth.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_CHARACTERISTIC_READ = "com.rk.commonlib.bluetooth.ACTION_GATT_CHARACTERISTIC_READ";
    public final static String ACTION_GATT_CHARACTERISTIC_WRITE = "com.rk.commonlib.bluetooth.ACTION_GATT_CHARACTERISTIC_WRITE";
    public final static String ACTION_GATT_DESCRIPTOR_READ = "com.rk.commonlib.bluetooth.ACTION_GATT_DESCRIPTOR_READ";
    public final static String ACTION_GATT_DESCRIPTOR_WRITE = "com.rk.commonlib.bluetooth.ACTION_GATT_DESCRIPTOR_WRITE";

    private final static String SERVICE_COMMUNICATION_UUID = "6e400001-b5a3-f393-e0a9-e50e24dc4179";
    private final static String CHARACTERISTIC_WRITE_UUID = "6e400002-b5a3-f393-e0a9-e50e24dc4179";
    private final static String CHARACTERISTIC_NOTIFY_UUID = "6e400003-b5a3-f393-e0a9-e50e24dc4179";
    private final static String DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private BluetoothGattService mCommunicationService;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private static Context sContext;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    private ILeScanVallback mLeScanListener;

    public interface ILeScanVallback {
        void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (mLeScanListener != null) {
                mLeScanListener.onLeScan(device, rssi, scanRecord);
            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "onConnectionStateChange, Connected to GATT server.");
                broadcastUpdate(ACTION_GATT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "onConnectionStateChange, Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "onCharacteristicChanged");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        private void broadcastUpdate(final String action) {
            Log.i(TAG, "broadcastUpdate, action: " + action);
            final Intent intent = new Intent(action);
            if (sContext != null) {
                sContext.sendBroadcast(intent);
            }

        }

        private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
            final Intent intent = new Intent(action);
            if (sContext != null) {
                sContext.sendBroadcast(intent);
            }
        }

        private void broadcastUpdate(final String action, final BluetoothGattDescriptor descriptor) {
            final Intent intent = new Intent(action);
            if (sContext != null) {
                sContext.sendBroadcast(intent);
            }
        }
    };

    private BluetoothInstance() {
        if (sContext != null) {
            final BluetoothManager bluetoothManager = (BluetoothManager) sContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    private static class InstanceHolder {
        private static final BluetoothInstance INSTANCE = new BluetoothInstance();
    }

    public static BluetoothInstance getInstance(Context context) {
        sContext = context.getApplicationContext();
        return InstanceHolder.INSTANCE;
    }

    public boolean startScan(Activity activity) {
        if (mBluetoothAdapter == null) {
            Toast.makeText(sContext, "Not Support BLE！", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        if (!isLocationOpen(sContext)) {
            Log.i(TAG, "startScan, Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT >= 23) {
                Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivityForResult(enableLocate, REQUEST_ENABLE_LACATION);
                return false;
            }
        } else {
            dynamicRequestPermission(activity);
        }
        mBluetoothAdapter.startDiscovery();
        return true;
    }

    public boolean stopScan(Activity activity) {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
            return true;
        }
        return false;
    }

    public void setLeScanListener(ILeScanVallback listener) {
        mLeScanListener = listener;
    }

    public boolean scanLeDevice(final boolean enable, Activity activity) {
        if (enable) {
            if (mBluetoothAdapter == null) {
                Toast.makeText(sContext, "Not Support BLE！", Toast.LENGTH_LONG).show();
                return false;
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return false;
            }

            if (!isLocationOpen(sContext)) {
                Log.i(TAG, "startScan, Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
                if (Build.VERSION.SDK_INT >= 23) {
                    Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivityForResult(enableLocate, REQUEST_ENABLE_LACATION);
                    return false;
                }
            } else {
                dynamicRequestPermission(activity);
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            return true;
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            return false;
        }
    }

    /**
     * Judge If location function is opened.
     */
    public static boolean isLocationOpen(final Context context) {
        Log.i(TAG, "isLocationOpen");
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void dynamicRequestPermission(Activity activity) {
        Log.i(TAG, "dynamicRequestPermission");
        //Android6.0需要动态申请权限
        if (ContextCompat.checkSelfPermission(sContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //判断是否需要解释
                Toast.makeText(sContext, "需要蓝牙权限....", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "connect, BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "connect, Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "connect, Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(sContext, false, mGattCallback);
        Log.d(TAG, "connect, Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    public void disconnect() {
        Log.i(TAG, "disconnect");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        Log.i(TAG, "close");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "readCharacteristic, BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;
        return mBluetoothGatt.getServices();
    }

    public void discoveryGattServices() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.discoverServices();
    }

    public String getCharacteristicProperty(int prop) {
        String s = null;
        switch (prop) {
            case 1:
                s = "PROPERTY_BROADCAST";
                break;
            case 128:
                s = "PROPERTY_EXTENDED_PROPS";
                break;
            case 32:
                s = "PROPERTY_INDICATE";
                break;
            case 16:
                s = "PROPERTY_NOTIFY";
                break;
            case 2:
                s = "PROPERTY_READ";
                break;
            case 64:
                s = "PROPERTY_SIGNED_WRITE";
                break;
            case 8:
                s = "PROPERTY_WRITE";
                break;
            case 4:
                s = "PROPERTY_WRITE_NO_RESPONSE";
                break;
        }
        return s;
    }

    public void getServicesAndCharacteristics() {
        List<BluetoothGattService> services = getSupportedGattServices();
        for (BluetoothGattService service : services) {
            Log.i(TAG, "getServicesAndCharacteristics, service uuid: " + service.getUuid().toString());
            if (SERVICE_COMMUNICATION_UUID.equals(service.getUuid().toString())) {
                mCommunicationService = service;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                Log.i(TAG, "getServicesAndCharacteristics, characteristic uuid: " + characteristic.getUuid().toString()
                        + ", property : " + getCharacteristicProperty(characteristic.getProperties()));
                if (CHARACTERISTIC_WRITE_UUID.equals(characteristic.getUuid().toString())) {
                    mWriteCharacteristic = characteristic;
                } else if (CHARACTERISTIC_NOTIFY_UUID.equals(characteristic.getUuid().toString())) {
                    mNotifyCharacteristic = characteristic;
                }
                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    Log.i(TAG, "getServicesAndCharacteristics, descriptor uuid: " + descriptor.getUuid().toString());
                }
            }
        }
    }


}
