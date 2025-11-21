package com.htnova.myapplication.utils

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.util.concurrent.LinkedBlockingQueue
import kotlin.jvm.javaClass


/**
 * @author xqm
 * @date 2025/9/24 9:37
 * @description CommonUtils 公共顶层函数
 */
var delayTime=300L
val job= Job()
val scope = CoroutineScope(job)
var isRecOK=false

var recSerialPortData = LinkedBlockingQueue<ByteArray>()
var sendSerialPortData = LinkedBlockingQueue<ByteArray>()

lateinit var connectedDevice: BluetoothDevice

const val DEVICE_DSN: Long = 2008040032L//设备序列号
// DSN 大端字节数组，多个命令可直接复用
val DEVICE_DSN_BYTES: ByteArray = byteArrayOf(
    ((DEVICE_DSN shr 24) and 0xFF).toByte(),
    ((DEVICE_DSN shr 16) and 0xFF).toByte(),
    ((DEVICE_DSN shr 8) and 0xFF).toByte(),
    (DEVICE_DSN and 0xFF).toByte()
)
/**
 * 发送指令给消息池
 */
@Synchronized
fun sendSerialPortMsg(byteArray: ByteArray){
    sendSerialPortData.offer(byteArray)
}

/**
 * 判断蓝牙是否已经连接
 */
fun isDeviceConnected(device: BluetoothDevice): Boolean {
    return try {
        val method = device.javaClass.getMethod("isConnected")
        method.invoke(device) as Boolean
    } catch (e: Exception) {
        false
    }
}