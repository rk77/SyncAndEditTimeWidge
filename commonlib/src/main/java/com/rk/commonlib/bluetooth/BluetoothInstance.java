package com.rk.commonlib.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
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

public class BluetoothInstance {
    private static final String TAG = BluetoothInstance.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_LACATION = 2;
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 3;

    private static Context sContext;
    private BluetoothAdapter mBluetoothAdapter;

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

}
