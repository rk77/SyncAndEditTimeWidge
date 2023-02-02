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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rk.commonlib.util.LogUtils;
import com.rk.commonmodule.protocol.protocol698.Protocol698Frame;
import com.rk.commonmodule.protocol.protocol698.ProtocolConstant;
import com.rk.commonmodule.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EnergyControlBluetoothInstance implements IBluethoothInstance{
    private static final String TAG = EnergyControlBluetoothInstance.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public final static String ACTION_GATT_CONNECTED = "com.rk.commonlib.bluetooth.EnergyControlBluetoothInstance.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.rk.commonlib.bluetooth.EnergyControlBluetoothInstance.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.rk.commonlib.bluetooth.EnergyControlBluetoothInstance.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_CHARACTERISTIC_READ = "com.rk.commonlib.bluetooth.EnergyControlBluetoothInstance.ACTION_GATT_CHARACTERISTIC_READ";
    public final static String ACTION_GATT_CHARACTERISTIC_WRITE = "com.rk.commonlib.bluetooth.EnergyControlBluetoothInstance.ACTION_GATT_CHARACTERISTIC_WRITE";
    public final static String ACTION_GATT_DESCRIPTOR_READ = "com.rk.commonlib.bluetooth.EnergyControlBluetoothInstance.ACTION_GATT_DESCRIPTOR_READ";
    public final static String ACTION_GATT_DESCRIPTOR_WRITE = "com.rk.commonlib.bluetooth.EnergyControlBluetoothInstance.ACTION_GATT_DESCRIPTOR_WRITE";
    public final static String ACTION_GATT_MTU_WRITE = "com.rk.commonlib.bluetooth.EnergyControlBluetoothInstance.ACTION_GATT_MTU_WRITE";

    private final static String SERVICE_COMMUNICATION_UUID = "6e400001-b5a3-f393-e0a9-e50e24dc4179";
    private final static String CHARACTERISTIC_WRITE_UUID = "6e400002-b5a3-f393-e0a9-e50e24dc4179";
    private final static String CHARACTERISTIC_NOTIFY_UUID = "6e400003-b5a3-f393-e0a9-e50e24dc4179";
    private final static String DESCRIPTOR_CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private BluetoothGattService mCommunicationService;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private int MTU = 247/*247*/;
    private static final long WAIT_TIMEOUT = 3000;
    private static final long RECV_WAIT_TIMEOUT = 20000;
    private Object mWriteSync = new Object();
    private Object mReadSync = new Object();
    private ArrayList<Byte> mReceivedFrame = new ArrayList<>();

    private static Context sContext;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    private boolean mIsConnected = false;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "onConnectionStateChange, Connected to GATT server.");
                mIsConnected = true;
                broadcastUpdate(ACTION_GATT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "onConnectionStateChange, Disconnected from GATT server.");
                mIsConnected = false;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                //getServicesAndCharacteristics();

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
            byte[] recvFrame = characteristic.getValue();
            Log.i(TAG, "onCharacteristicChanged, recvFrame: " + DataConvertUtils.convertByteArrayToString(recvFrame, false));
            if (mReceivedFrame != null && recvFrame != null && recvFrame.length > 0) {
                for (int i = 0; i < recvFrame.length; i++) {
                    mReceivedFrame.add(recvFrame[i]);
                }
            }

            if (isReceivedDone(mReceivedFrame)) {
                synchronized (mReadSync) {
                    mReadSync.notify();
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    Log.i(TAG, "onCharacteristicWrite, UUID: " + characteristic.getUuid().toString()
                            + ", write data: " + DataConvertUtils.convertByteArrayToString(characteristic.getValue(), false));
                } catch (Exception e) {
                    Log.e(TAG, "onCharacteristicWrite, error: " + e.getMessage());
                }
                synchronized (mWriteSync) {
                    mWriteSync.notify();
                }

            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorWrite");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_DESCRIPTOR_WRITE, descriptor);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.i(TAG, "onMtuChanged, mtu: " + mtu + ", set successfully: " + (status == BluetoothGatt.GATT_SUCCESS));
            MTU = mtu;
            if (mCommunicationService != null) {
                broadcastUpdate(ACTION_GATT_MTU_WRITE);
            }
        }
    };

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
        intent.putExtra("descriptor", descriptor.getValue());
        if (sContext != null) {
            sContext.sendBroadcast(intent);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.i("action: " + action);
            if (ACTION_GATT_CONNECTED.equals(action)) {
                discoveryGattServices();
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                broadcastUpdate(IBluethoothInstance.DISCONNECT_ACTION);
            } else if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                getServicesAndCharacteristics();
                enableCharacteristicNotify();
            } else if (ACTION_GATT_CHARACTERISTIC_READ.equals(action)) {

            } else if (ACTION_GATT_CHARACTERISTIC_WRITE.equals(action)) {

            } else if (ACTION_GATT_DESCRIPTOR_READ.equals(action)) {

            } else if (ACTION_GATT_DESCRIPTOR_WRITE.equals(action)) {
                setMtu();
            } else if (ACTION_GATT_MTU_WRITE.equals(action)) {
                broadcastUpdate(IBluethoothInstance.CONNECT_ACTION);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_GATT_CHARACTERISTIC_READ);
        intentFilter.addAction(ACTION_GATT_CHARACTERISTIC_WRITE);
        intentFilter.addAction(ACTION_GATT_DESCRIPTOR_READ);
        intentFilter.addAction(ACTION_GATT_DESCRIPTOR_WRITE);
        intentFilter.addAction(ACTION_GATT_MTU_WRITE);
        return intentFilter;
    }

    private EnergyControlBluetoothInstance() {
        if (sContext != null) {
            final BluetoothManager bluetoothManager = (BluetoothManager) sContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            sContext.registerReceiver(mBroadcastReceiver, makeGattUpdateIntentFilter());
        }
    }

    private static class InstanceHolder {
        private static final EnergyControlBluetoothInstance INSTANCE = new EnergyControlBluetoothInstance();
    }

    public static EnergyControlBluetoothInstance getInstance(Context context) {
        sContext = context.getApplicationContext();
        return InstanceHolder.INSTANCE;
    }

    private boolean startScan(Activity activity) {
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
            if (!dynamicRequestPermission(activity)) {
                return false;
            }
        }
        return mBluetoothAdapter.startDiscovery();
    }

    private boolean stopScan(Activity activity) {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.cancelDiscovery();
        }
        return false;
    }

    private boolean scanLeDevice(final boolean enable, Activity activity, BluetoothAdapter.LeScanCallback leScanCallback) {
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
                if (!dynamicRequestPermission(activity)) {
                    return false;
                }
            }
            return mBluetoothAdapter.startLeScan(leScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(null);
            return true;
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

    public static boolean dynamicRequestPermission(Activity activity) {
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
            return false;
        }
        return true;
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
            //Log.i(TAG, "getServicesAndCharacteristics, service uuid: " + service.getUuid().toString());
            if (SERVICE_COMMUNICATION_UUID.equals(service.getUuid().toString())) {
                mCommunicationService = service;
                //Log.i("AX", "mCommunicationService: " + mCommunicationService);
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                //Log.i(TAG, "getServicesAndCharacteristics, characteristic uuid: " + characteristic.getUuid().toString()
                //        + ", property : " + getCharacteristicProperty(characteristic.getProperties()));
                if (CHARACTERISTIC_WRITE_UUID.equals(characteristic.getUuid().toString())) {
                    mWriteCharacteristic = characteristic;
                } else if (CHARACTERISTIC_NOTIFY_UUID.equals(characteristic.getUuid().toString())) {
                    mNotifyCharacteristic = characteristic;
                }
            }
        }
    }

    public void enableCharacteristicNotify() {
        Log.i(TAG, "enableCharacteristicNotify");
        if (mBluetoothGatt != null && mNotifyCharacteristic != null) {
            boolean succ = mBluetoothGatt.setCharacteristicNotification(mNotifyCharacteristic, true);
            BluetoothGattDescriptor descriptor = mNotifyCharacteristic.getDescriptor(
                    UUID.fromString(DESCRIPTOR_CCCD_UUID));
            if (succ) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
            Log.i(TAG, "enableCharacteristicNotify, succ: " + succ);
        }
    }

    public void setMtu() {
        Log.i(TAG, "setMtu");
        //mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        mBluetoothGatt.requestMtu(MTU);
    }

    private boolean isReceivedDone(ArrayList<Byte> frame) {
        if (frame != null && frame.size() > 0) {
            byte[] tmpFrame = new byte[frame.size()];
            for (int i = 0; i < tmpFrame.length; i++) {
                tmpFrame[i] = frame.get(i);
            }
            if (mVerifyObj != null) {
                mVerifyObj.verify(tmpFrame, 0);
            }
            return verify698Frame(tmpFrame);
        }
        return false;
    }

    public boolean verify698Frame(byte[] frame) {
        int beginPos;
        if (frame == null || frame.length < 12) {
            return false;
        }

        for (beginPos = 0; beginPos < frame.length; beginPos++) {
            if (frame[beginPos] == 0x68) {
                break;
            }
        }
        if (beginPos == frame.length) {
            return false;
        }

        if (beginPos + 3 >= (frame.length - 1)) {
            return false;
        }

        int lengthPos0, lengthPos1;
        lengthPos0 = beginPos + 1;
        lengthPos1 = lengthPos0 + 1;

        byte[] lengthData = new byte[2];
        lengthData[0] = frame[lengthPos0];
        lengthData[1] = frame[lengthPos1];
        int frameLength = 0;

        Protocol698Frame.Length_Area length_area = new Protocol698Frame.Length_Area(lengthData);

        switch (length_area.frame_unit) {
            case BYTE_UNIT:
                frameLength = length_area.length;
                break;
            case KBYTE_UNIT:
                frameLength = length_area.length * 1024;
                break;
        }
        Log.i(TAG, "verify698Frame, frame length: " + frameLength);
        if (frameLength == 0) {
            return false;
        }

        int ctrlAreaPos = lengthPos1 + 1;

        if (ctrlAreaPos + 1 >= (frame.length - 1)) {
            return false;
        }

        int addressAreaBeginPos = ctrlAreaPos + 1;

        int addrLength = (frame[addressAreaBeginPos] & 0x0F) + 1;

        Log.i(TAG, "verify698Frame, serve address length: " + addrLength + ", address begin position: " + addressAreaBeginPos);

        if ((addressAreaBeginPos + addrLength + 1) >= (frame.length - 1)) {
            return false;
        }
        int addressAreaEndPos = addressAreaBeginPos + addrLength + 1;

        if ((addressAreaEndPos + 1 + 1) >= (frame.length - 1)) {
            return false;
        }

        int mHCsPos0 = addressAreaEndPos + 1;
        int mHCsPos1 = addressAreaEndPos + 2;

        byte[] hCs = new byte[2];
        hCs[0] = frame[mHCsPos0];
        hCs[1] = frame[mHCsPos1];

        boolean verifyHCs = verifyCs(ProtocolConstant.INIT_FCS, DataConvertUtils.getSubByteArray(frame, lengthPos0, addressAreaEndPos), hCs);
        if (!verifyHCs) {
            return false;
        }

        if (mHCsPos1 + 1 >= frame.length - 1) {
            return false;
        }

        int mApduBegin = mHCsPos1 + 1;

        int mApduEnd = beginPos + frameLength - 1 - 1;

        if (mApduEnd < mApduBegin) {
            return false;
        }

        if (mApduEnd + 1 + 1 >= frame.length - 1) {
            return false;
        }
        int mFCsPos0 = mApduEnd + 1;
        int mFCsPos1 = mApduEnd + 2;

        byte[] fCs = new byte[2];
        fCs[0] = frame[mFCsPos0];
        fCs[1] = frame[mFCsPos1];

        boolean verifyFCs = verifyCs(ProtocolConstant.INIT_FCS, DataConvertUtils.getSubByteArray(frame, lengthPos0, mApduEnd), fCs);
        if (!verifyFCs) {
            return false;
        }

        if (mFCsPos1 + 1 > frame.length - 1) {
            return false;
        }

        int mEndPos = mFCsPos1 + 1;

        if (frame[mEndPos] != 0x16) {
            return false;
        }
        Log.i(TAG, "verify698Frame, verfiy OK.");
        return true;

    }

    public boolean verifyCs(int initCs, byte[] data, byte[] cs) {
        Log.i(TAG, "verifyCs, data: " + DataConvertUtils.convertByteArrayToString(data, false)
                + ", verified cs: " + DataConvertUtils.convertByteArrayToString(cs, false));
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                initCs = (((initCs & 0xFFFF) >> 8) ^ ProtocolConstant.FCS_TAB[((initCs & 0xFFFF) ^ data[i]) & 0xFF]) & 0xFFFF;
            }
            initCs = initCs ^ 0xFFFF;
            if (cs != null) {
                if (cs.length == 2) {
                    byte cs0 = (byte) (initCs & 0xFF);
                    byte cs1 = (byte) ((initCs >> 8) & 0xFF);
                    Log.i(TAG, "verifyCs, cs0: " + DataConvertUtils.convertByteToString(cs0)
                            + ", cs1: " + DataConvertUtils.convertByteToString(cs1));
                    if (cs0 == cs[0] && cs1 == cs[1]) {
                        return true;
                    }
                } else {
                    return false;
                }

            } else {
                return false;
            }

        }
        return false;
    }


    /** Interface implements **/
    public boolean startScan(Activity activity, boolean isBle, BluetoothAdapter.LeScanCallback leScanCallback) {
        if (isBle) {
            return scanLeDevice(true, activity, leScanCallback);
        } else {
            return startScan(activity);
        }
    }

    public boolean stopScan(Activity activity, boolean isBle) {
        if (isBle) {
            return scanLeDevice(false, activity, null);
        } else {
            return stopScan(activity);
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
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = device.connectGatt(sContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {*/
        mBluetoothGatt = device.connectGatt(sContext, false, mGattCallback);
        //}

        Log.d(TAG, "connect, Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mNotifyCharacteristic = null;
        mWriteCharacteristic = null;
        mCommunicationService = null;
        return true;
    }

    public synchronized byte[] sendAndReceiveSync(byte[] frame) {
        Log.i(TAG, "sendAndReceiveSync, frame: " + DataConvertUtils.convertByteArrayToString(frame, false));
        if (frame == null) {
            return null;
        }
        mReceivedFrame.clear();

        int sendTime = (frame.length + (MTU - 3 - 1)) / (MTU - 3);
        for (int i = 0; i < sendTime; i++) {
            int begin = i * (MTU - 3);
            int end = (i + 1) * (MTU - 3) - 1;
            if (end > frame.length - 1) {
                end = frame.length - 1;
            }
            if (mWriteCharacteristic != null && mBluetoothGatt != null) {
                byte[] subFrame = DataConvertUtils.getSubByteArray(frame, begin, end);
                Log.i(TAG, "sendAndReceiveSync, total: " + frame.length + ", begin: " + begin + ", end: " + end
                        + ", sub frame: " + DataConvertUtils.convertByteArrayToString(subFrame, false));

                synchronized (mWriteSync) {
                    try {
                        mWriteCharacteristic.setValue(subFrame);
                        mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
                        Log.i("AX", "send chara: " + mWriteCharacteristic);
                        mWriteSync.wait(WAIT_TIMEOUT);
                    } catch (Exception e) {
                        Log.e(TAG, "sendAndReceiveSync, wait for writing error: " + e.getMessage());
                    }
                }
            }
        }
        Log.i(TAG, "sendAndReceiveSync, send done.");

        // wait for finishing receiving message
        synchronized (mReadSync) {
            try {
                mReadSync.wait(RECV_WAIT_TIMEOUT);
            } catch (Exception e) {
                Log.e(TAG, "sendAndReceiveSync, wait for reading error: " + e.getMessage());
            }
        }
        Log.i(TAG, "sendAndReceiveSync, receive done.");
        byte[] recvFrame = null;
        if (mReceivedFrame != null && mReceivedFrame.size() > 0) {
            recvFrame = new byte[mReceivedFrame.size()];
            for (int i = 0; i < recvFrame.length; i++) {
                recvFrame[i] = mReceivedFrame.get(i);
            }
        }
        return recvFrame;

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
        mNotifyCharacteristic = null;
        mWriteCharacteristic = null;
        mCommunicationService = null;
        mBluetoothGatt = null;
        mIsConnected = false;
        mBluetoothDeviceAddress = null;
    }

    public boolean isDeviceConnected(String deviceAddr) {
        if (deviceAddr != null && deviceAddr.equals(mBluetoothDeviceAddress)) {
            return mIsConnected;
        }
        return false;
    }

    private IFrameVerify mVerifyObj = null;
    @Override
    public void setFrameVerifyInterface(IFrameVerify verifyInterface) {
        mVerifyObj = verifyInterface;
    }


}
