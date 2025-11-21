package com.comm.library.utils

/**
 * @author xqm
 * @date 2025/10/27 9:56
 * @description AppBluetoothCallback 低功耗蓝牙,全局数据监测管理
 *
 */
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlin.collections.joinToString
import kotlin.collections.none
import kotlin.collections.toList
import kotlin.text.format
import kotlin.text.isNullOrEmpty

object AppBluetoothBleCallback : BluetoothBleUtil.Callback {

    private val deviceList = mutableListOf<BluetoothDevice>()
    // 全局 LiveData，页面可以观察
    val deviceListLiveData = MutableLiveData<List<BluetoothDevice>>()
    override fun onScanStarted() {
        Log.d("gasdetection", "扫描开始")
    }

    override fun onDeviceFound(device: BluetoothDevice) {
        val name = device.name
        // 过滤掉 name 为空的设备
        if (name.isNullOrEmpty()) return

        if (deviceList.none { it.address == device.address }) {
            deviceList.add(device)
            // 通知所有订阅页面
            deviceListLiveData.postValue(deviceList.toList())
        }
    }

    override fun onScanFinished() {
        Log.d("gasdetection", "扫描结束")
    }

    override fun onConnected(device: BluetoothDevice) {
        Log.d("gasdetection", "连接成功: ${device.name}")
    }

    override fun onDisconnected(device: BluetoothDevice) {
        Log.d("gasdetection", "已断开: ${device.name}")
    }

    override fun onMessageReceived(device: BluetoothDevice, data: ByteArray) {
        val msg = try {
            data.joinToString(separator = " ") { "%02X".format(it) } // 转成十六进制查看
//            String(data, Charsets.UTF_8)
        } catch (e: Exception) {
            data.joinToString(separator = " ") { "%02X".format(it) } // 转成十六进制查看
        }
        Log.d("gasdetection", "收到消息: $msg")

        //分发消息
//        DataBus.post(data)//生产消息
    }

    override fun onError(message: String) {
        Log.e("gasdetection", message)
    }

}
