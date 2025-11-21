package com.htnova.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.htnova.myapplication.utils.BluetoothHelper;
import com.htnova.myapplication.utils.SerialCommandUtil;

import java.util.UUID;

public class BluteA extends AppCompatActivity {
    BluetoothHelper bluetoothHelper = new BluetoothHelper(this);
    Button btnconet,btnColse,btn_disscan,btn_sendmsg,btn_getmsg;
    BluetoothDevice bluetoothDevices;

    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blute);
        btnconet = findViewById(R.id.btn_connect);
        btnColse = findViewById(R.id.btn_close);
        btn_disscan = findViewById(R.id.btn_disscan);
        btn_sendmsg = findViewById(R.id.btn_sendmsg);
        btn_getmsg = findViewById(R.id.btn_getmsg);
        btnconet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothHelper.connectBleDevice(bluetoothDevices, new BluetoothGattCallback() {
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
                                bluetoothHelper.enableBleNotifications(notifyCharacteristic, new BluetoothHelper.DataCallback() {
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
        });
        btnColse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothHelper.disconnect();
            }
        });
        btn_disscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothHelper.stopScan();
            }
        });
        btn_sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SerialCommandUtil.INSTANCE.buildHeartBeat();
                bluetoothHelper.sendBleData(SerialCommandUtil.INSTANCE.buildHeartBeat(), new BluetoothHelper.DataCallback() {
                    @Override
                    public void onDataReceived(byte[] data) {
                        // 无需处理
                        String strr ="";
                    }

                    @Override
                    public void onDataSent(boolean success) {
                        Log.d("BluetoothHelper", "BLE Data Sent: " + success);
                    }
                });
            }
        });
        btn_getmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothHelper.sendBleData(SerialCommandUtil.INSTANCE.getDeviceInfo(), new BluetoothHelper.DataCallback() {
                    @Override
                    public void onDataReceived(byte[] data) {
                        String strr ="";
                    }

                    @Override
                    public void onDataSent(boolean success) {
                        String strr ="";
                    }
                });
            }
        });
        bluetoothHelper.startBleScan(new BluetoothHelper.ScanCallback() {
            @Override
            public void onClassicDeviceFound(BluetoothDevice device) {
                Log.d("BluetoothHelper", "Classic Device Found: " + device.getName());
            }

            @Override
            public void onBleDeviceFound(BluetoothDevice device) {
                Log.d("BluetoothHelper", "BLE Device Found: " + device.getName());
                if(device.getName() == null){
                    return;
                }
                if(device.getName().contains("GCPID")){
                    bluetoothDevices = device;
                }
            }

            @Override
            public void onScanFailed(String error) {
                Log.e("BluetoothHelper", "Scan Failed: " + error);
            }
        });
    }
}