package com.htnova.gasdetection.serialport

import com.htnova.myapplication.utils.ByteUtils


/**
 * @author xqm
 * @date 2025/10/31 10:24
 * @description SerialCodecUtil 类功能说明
 */
object SerialCodecUtil {
    private val transSendCodingList = ArrayList<Byte>()
    private lateinit var transSendCodingBytes: ByteArray

    //转码函数
    @Synchronized
    fun transSendCoding(bytes: ByteArray): ByteArray {
        //"转码前长度：${bytes.size} ：${bytes.toHexString()}".logE(logFlag)
        bytes.let {
            var i = 1
            if (it[0] == ByteUtils.FRAME_START) {
                transSendCodingList.clear()
                transSendCodingList.add(it[0])
            }
            while (i < it.size) {
                //校验开头
                //开始转码
                when {
                    it[i] == ByteUtils.FRAME_START -> {
                        transSendCodingList.add(ByteUtils.FRAME_FF)
                        transSendCodingList.add(ByteUtils.FRAME_00)
                    }

                    it[i] == ByteUtils.FRAME_FF -> {
                        transSendCodingList.add(ByteUtils.FRAME_FF)
                        transSendCodingList.add(ByteUtils.FRAME_FF)
                    }

                    else -> transSendCodingList.add(it[i])
                }
                i++
            }
        }

        transSendCodingList.let {
            if (it.size > 0) {
                transSendCodingBytes = ByteArray(it.size)
                for (k in transSendCodingBytes.indices) {
                    transSendCodingBytes[k] = it[k]
                }
            }
        }
        return transSendCodingBytes
    }
}