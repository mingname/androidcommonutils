package com.htnova.myapplication.utils;

/**
 * @author xqm
 * @date 2025/11/11 10:55
 * @description BluetoothHelper 类功能说明
 */
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.htnova.myapplication.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothHelper {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothSocket bluetoothSocket;
    private BluetoothGatt bluetoothGatt;
    private Handler handler;
    private List<BluetoothDevice> classicDevices = new ArrayList<>();
    private List<BluetoothDevice> bleDevices = new ArrayList<>();

    // BLE 相关变量
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;

    // 回调接口
    public interface ScanCallback {
        void onClassicDeviceFound(BluetoothDevice device);
        void onBleDeviceFound(BluetoothDevice device);
        void onScanFailed(String error);
    }

    public interface ConnectionCallback {
        void onConnected();
        void onConnectionFailed(String error);
    }

    public interface DataCallback {
        void onDataReceived(byte[] data);
        void onDataSent(boolean success);
    }

    public BluetoothHelper(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            this.bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }

    // 检查权限
    public boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // 请求权限
    public void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions((MainActivity) context,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions((MainActivity) context,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    // 开始扫描经典蓝牙设备
    public void startClassicScan(ScanCallback callback) {
        if (!checkPermissions()) {
            callback.onScanFailed("Permissions not granted");
            return;
        }

        classicDevices.clear();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    classicDevices.add(device);
                    callback.onClassicDeviceFound(device);
                }
            }
        }, filter);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothAdapter.startDiscovery();
    }

    // 开始扫描 BLE 设备
    @SuppressLint("MissingPermission")
    public void startBleScan(ScanCallback callback) {
        if (!checkPermissions()) {
            callback.onScanFailed("Permissions not granted");
            return;
        }

        bleDevices.clear();
        bleScanner.startScan(new android.bluetooth.le.ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                bleDevices.add(device);
                callback.onBleDeviceFound(device);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                callback.onScanFailed("BLE scan failed with error code: " + errorCode);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }
        });

    }

    // 停止扫描
    @SuppressLint("MissingPermission")
    public void stopScan() {
        bluetoothAdapter.cancelDiscovery();
        if (bleScanner != null) {
            bleScanner.stopScan(new android.bluetooth.le.ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            });
        }
    }

    // 连接经典蓝牙设备
    public void connectClassicDevice(BluetoothDevice device, ConnectionCallback callback) {
        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                handler.post(callback::onConnected);
            } catch (IOException e) {
                handler.post(() -> callback.onConnectionFailed(e.getMessage()));
            }
        }).start();
    }

    // 连接 BLE 设备
    @SuppressLint("MissingPermission")
    public void connectBleDevice(BluetoothDevice device, BluetoothGattCallback gattCallback) {
        bluetoothGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BluetoothHelper", "BLE Device Disconnected");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if(status == BluetoothGatt.GATT_SUCCESS){
                    BluetoothGattService service = gatt.getService(UUID.fromString("0000a002-0000-1000-8000-00805f9b34fb"));
                    if(service != null){
                        writeCharacteristic = service.getCharacteristic(UUID.fromString("0000c303-0000-1000-8000-00805f9b34fb"));
                        notifyCharacteristic = service.getCharacteristic(UUID.fromString("0000c305-0000-1000-8000-00805f9b34fb"));
                        // 启用通知
                        enableBleNotifications(notifyCharacteristic, new BluetoothHelper.DataCallback() {
                            @Override
                            public void onDataReceived(byte[] data) {
                                Log.d("BluetoothHelper", "BLE Data Received: " + new String(data));
                            }

                            @Override
                            public void onDataSent(boolean success) {
                                // 无需处理
                            }
                        });
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
                super.onCharacteristicChanged(gatt, characteristic, value);
                byte[] data = characteristic.getValue();
                Log.d("BluetoothHelper", "BLE Notification Data: " + new String(data));
            }
        });
    }

    // 发送数据（经典蓝牙）
    public void sendClassicData(byte[] data, DataCallback callback) {
        new Thread(() -> {
            try {
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                outputStream.write(data);
                handler.post(() -> callback.onDataSent(true));
            } catch (IOException e) {
                handler.post(() -> callback.onDataSent(false));
            }
        }).start();
    }

    // 接收数据（经典蓝牙）
    public void receiveClassicData(DataCallback callback) {
        new Thread(() -> {
            try {
                InputStream inputStream = bluetoothSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;
                while ((bytes = inputStream.read(buffer)) != -1) {
                    byte[] receivedData = new byte[bytes];
                    System.arraycopy(buffer, 0, receivedData, 0, bytes);
                    handler.post(() -> callback.onDataReceived(receivedData));
                }
            } catch (IOException e) {
                handler.post(() -> callback.onDataReceived(null));
            }
        }).start();
    }

    // 发送数据（BLE）
    @SuppressLint("MissingPermission")
    public void sendBleData(byte[] data, DataCallback callback) {
        if (writeCharacteristic != null && bluetoothGatt != null) {
            writeCharacteristic.setValue(data);
            bluetoothGatt.writeCharacteristic(writeCharacteristic);
            handler.post(() -> callback.onDataSent(true));
        } else {
            handler.post(() -> callback.onDataSent(false));
        }
    }

    // 启用通知以接收数据（BLE）
    @SuppressLint("MissingPermission")
    public void enableBleNotifications(BluetoothGattCharacteristic characteristic, DataCallback callback) {
        if (bluetoothGatt != null) {
            bluetoothGatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }

    // 断开连接
    public void disconnect() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            if (bluetoothGatt != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
