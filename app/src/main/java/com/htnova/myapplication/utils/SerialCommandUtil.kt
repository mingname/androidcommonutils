package com.htnova.myapplication.utils

/**
 * @author xqm
 * @date 2025/10/31 10:20
 * @description DEVICE_DSN_BYTES  设备序列号
 */
import com.htnova.gasdetection.serialport.Crc8
import com.htnova.gasdetection.serialport.SerialCodecUtil
import kotlin.collections.plus

object SerialCommandUtil {
    /**
     * 心跳包
     */
    fun buildHeartBeat(): ByteArray {
        //发送心跳包
        var MSG_HEART_BEAT = byteArrayOf(
            0x55.toByte(),
            0x00.toByte(),
            0x0D.toByte()//帧长度
        ) + DEVICE_DSN_BYTES+byteArrayOf(
            0x06.toByte(),
            0x00.toByte(), //消息ID
            0x00.toByte(),
            0x00.toByte()  //消息长度
        )
        val resultByte = MSG_HEART_BEAT
        return SerialCodecUtil.transSendCoding(resultByte + Crc8.cal_crc8_t(resultByte, resultByte.size) + ByteUtils.FRAME_END)
    }

    fun getDeviceInfo(): ByteArray {
        //获取设备信息
        var MSG_HEART_BEAT = byteArrayOf(
            0x55.toByte(),
            0x00.toByte(),
            0x0D.toByte()//帧长度
        ) + DEVICE_DSN_BYTES+byteArrayOf(
            0x06.toByte(),
            0x02.toByte(), //消息ID
            0x00.toByte(),
            0x00.toByte()  //消息长度
        )
        val resultByte = MSG_HEART_BEAT
        return SerialCodecUtil.transSendCoding(resultByte + Crc8.cal_crc8_t(resultByte, resultByte.size) + ByteUtils.FRAME_END)
    }

    /**
     * 通用单个指令的调用指令
     * 0x56 一键开机 fun aKeyOpen(type: Int) = buildKeyCommand(type, 0x56.toByte())
     * 0x58 一键关机 fun aKeyClose(type: Int) = buildKeyCommand(type, 0x58.toByte())
     *
     */
    fun buildKeyCommand(type:Int,msgId: Byte): ByteArray {
        val msgLength = 1 // 数据长度
        val headerLength = 11 // header 11字节（包括DEVICE_DSN_BYTES长度）
        val frameLength: Byte = (headerLength + msgLength + 2).toByte() // + CRC + FE

        var header = byteArrayOf(
            0x55.toByte(),
            0x00.toByte(),
            frameLength//帧长度
        ) + DEVICE_DSN_BYTES+byteArrayOf(
            0x06.toByte(),
            msgId, //消息ID
            0x00.toByte(),
            msgLength.toByte()  //消息长度
        )
        val resultByte = header+type.toByte()
        return SerialCodecUtil.transSendCoding(resultByte + Crc8.cal_crc8_t(resultByte, resultByte.size) + ByteUtils.FRAME_END)
    }

}
