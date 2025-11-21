package com.htnova.myapplication.utils

import kotlin.collections.map
import kotlin.collections.toByteArray
import kotlin.text.removePrefix
import kotlin.text.toInt


object ByteUtils {
    const val FRAME_START: Byte = 0x55
    const val FRAME_END: Byte = 0x23.toByte()
    const val FRAME_FF: Byte = 0xFF.toByte()
    const val FRAME_00: Byte = 0x00

    /**
     * 将单个 Int 转为 Byte
     * 超过 127 的自动转换为有符号 Byte
     */
    fun toByte(value: Int): Byte {
        require(value in 0..255) { "Value must be in 0..255" }
        return value.toByte()
    }

    /**
     * 将 Hex String 转为 Byte
     * "0x80" -> -128
     */
    fun hexToByte(hex: String): Byte {
        val intValue = hex.removePrefix("0x").toInt(16)
        return toByte(intValue)
    }

    /**
     * 批量转换 IntArray 为 ByteArray
     */
    fun toByteArray(values: IntArray): ByteArray {
        return values.map { toByte(it) }.toByteArray()
    }

    /**
     * 批量转换 Hex 字符串数组为 ByteArray
     * ["0x58", "0x80"] -> [0x58, 0x80.toByte()]
     */
    fun hexArrayToByteArray(hexArray: Array<String>): ByteArray {
        return hexArray.map { hexToByte(it) }.toByteArray()
    }
}