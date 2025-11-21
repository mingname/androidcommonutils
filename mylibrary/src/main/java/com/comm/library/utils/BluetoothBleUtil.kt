package com.comm.library.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.UUID
import kotlin.let
import kotlin.text.toByteArray

/**
 * 低功耗蓝牙工具类
 *  BluetoothBleUtil.init(this,AppBluetoothBleCallback)
 *  BluetoothBleUtil.startScan(80000)
 */
@SuppressLint("MissingPermission")
object BluetoothBleUtil {

    private const val TAG = "gasdetection"

    // ------------------------- BLE参数配置 -------------------------
    private const val DEVICE_NAME = "qqq"
    private val SERVICE_UUID = UUID.fromString("0000a002-0000-1000-8000-00805f9b34fb")
    private val WRITE_UUID = UUID.fromString("0000c303-0000-1000-8000-00805f9b34fb")
    private val NOTIFY_UUID = UUID.fromString("0000c305-0000-1000-8000-00805f9b34fb")

    // ------------------------- 回调接口 -------------------------
    interface Callback {
        fun onScanStarted() {}
        fun onDeviceFound(device: BluetoothDevice) {}
        fun onScanFinished() {}
        fun onConnected(device: BluetoothDevice) {}
        fun onDisconnected(device: BluetoothDevice) {}
        fun onMessageReceived(device: BluetoothDevice, data: ByteArray) {}
        fun onError(msg: String) {}
    }

    // ------------------------- 内部状态 -------------------------
    private var callback: Callback? = null
    private lateinit var appContext: Context
    private var adapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
    private val handler = Handler(Looper.getMainLooper())

    private var isScanning = false
    private var isConnected = false

    val connectedDeviceLiveData = MutableLiveData<BluetoothDevice?>()

    // ------------------------- 初始化 -------------------------
    fun init(context: Context, cb: Callback) {
        appContext = context.applicationContext
        callback = cb
        adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        scanner = adapter?.bluetoothLeScanner
    }

    // ------------------------- 扫描 -------------------------
    fun startScan(timeoutMs: Long = 8000) {
        if (isScanning) return
        if (adapter == null || !adapter!!.isEnabled) {
            callback?.onError("蓝牙未开启")
            return
        }

//        val filters = listOf(ScanFilter.Builder().setDeviceName(DEVICE_NAME).build())
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        isScanning = true
        callback?.onScanStarted()
        scanner?.startScan(null, settings, scanCallback)

        handler.postDelayed({
            stopScan()
        }, timeoutMs)
    }

    fun stopScan() {
        if (!isScanning) return
        isScanning = false
        try { scanner?.stopScan(scanCallback) } catch (_: Exception) {}
        callback?.onScanFinished()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            callback?.onDeviceFound(device)
            if (device.name == DEVICE_NAME) {
                callback?.onDeviceFound(device)
                stopScan()
//                connect(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            callback?.onError("扫描失败: $errorCode")
            isScanning = false
        }
    }

    // ------------------------- 连接 -------------------------
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "正在连接: ${device.name} ${device.address}")
        bluetoothGatt = device.connectGatt(appContext, false, gattCallback)
    }

    fun disconnect() {
        isConnected = false
        try { bluetoothGatt?.disconnect() } catch (_: Exception) {}
        try { bluetoothGatt?.close() } catch (_: Exception) {}
        bluetoothGatt = null
        connectedDeviceLiveData.postValue(null)
    }

    fun disconnectTwo(){
        isConnected = false
        try {
            if(bluetoothGatt != null){
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    // ------------------------- GATT回调 -------------------------
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    isConnected = true
                    connectedDeviceLiveData.postValue(gatt.device)
                    callback?.onConnected(gatt.device)
                    Log.d(TAG, "已连接，开始发现服务")

                    Handler(Looper.getMainLooper()).postDelayed({
                        val desiredMtu = 128

                        val result = gatt.requestMtu(desiredMtu)
                        Log.d(TAG, "开始请求 MTU=$desiredMtu，结果=$result")
                    }, 5000)


                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    isConnected = false
                    connectedDeviceLiveData.postValue(null)
                    callback?.onDisconnected(gatt.device)
                    Log.d(TAG, "已断开: ${gatt.device.name}")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    writeCharacteristic = service.getCharacteristic(WRITE_UUID)
                    notifyCharacteristic = service.getCharacteristic(NOTIFY_UUID)

                    // 启用通知
                    if (notifyCharacteristic != null) {
                        gatt.setCharacteristicNotification(notifyCharacteristic, true)
                        val descriptor = notifyCharacteristic!!.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        )
                        descriptor?.let {
                            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(it)
                        }


                    }
                } else {
                    callback?.onError("未发现指定服务UUID")
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.d(TAG, "有数据上报")
            if (characteristic.uuid == NOTIFY_UUID) {
                val data = characteristic.value
                callback?.onMessageReceived(gatt.device, data)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                callback?.onError("写入失败: $status")
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "MTU 改变成功，新 MTU: $mtu")
            } else {
                Log.w(TAG, "MTU 改变失败，status=$status")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.d(TAG, "Descriptor write status=$status")
        }
    }

    // ------------------------- 写数据 -------------------------
    fun sendBytes(data: ByteArray) {
        if (!isConnected || writeCharacteristic == null || bluetoothGatt == null) {
            callback?.onError("未连接设备")
            return
        }
        writeCharacteristic!!.value = data
        val result = bluetoothGatt!!.writeCharacteristic(writeCharacteristic)
        if (!result) {
            callback?.onError("发送失败: writeCharacteristic返回false")
        }
    }

    fun sendMessage(msg: String) = sendBytes(msg.toByteArray())

    // ------------------------- 工具 -------------------------
    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true
    fun enableBluetooth() = adapter?.enable()
    fun disableBluetooth() = adapter?.disable()

    fun release() {
        stopScan()
        disconnect()
        callback = null
        adapter = null
        scanner = null
    }

    fun getConnectedBleDevices(): List<BluetoothDevice> {
        val manager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.getConnectedDevices(BluetoothProfile.GATT)
    }


}
