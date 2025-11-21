package com.comm.library.utils

/**
 * @author xqm
 * @date 2025/11/7 17:21
 * @description DataBus 发布订阅消息
 *  DataBus.post(data)//生产消息
 *  private val disposable = CompositeDisposable()
 *  disposable.add(
 *             DataBus.toObservable()
 *                 .subscribe { bytes ->
 *                     val time = java.text.SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
 *                     val msg = try {
 *                         bytes.joinToString(separator = " ") { "%02X".format(it) } // 转成十六进制查看
 * //            String(data, Charsets.UTF_8)
 *                     } catch (e: Exception) {
 *                         bytes.joinToString(separator = " ") { "%02X".format(it) } // 转成十六进制查看
 *                     }
 *                     // 追加显示，换行
 *                     val fullMsg = "$time: $msg\n"
 *
 *                     runOnUiThread {
 *                         binding.tvDeviceLog.append(fullMsg)
 *
 *                         // 可选：自动滚动到底部
 *                         val scrollView = binding.tvDeviceLog.parent as? android.widget.ScrollView
 *                         scrollView?.post { scrollView.fullScroll(android.view.View.FOCUS_DOWN) }
 *                     }
 *                 }
 *         )
 *          disposable.clear() // 取消订阅
 *
 */
import io.reactivex.subjects.PublishSubject
import io.reactivex.Observable

object DataBus {
    private val dataBus = PublishSubject.create<ByteArray>()

    // 发布（生产者调用）
    fun post(bytes: ByteArray) {
        dataBus.onNext(bytes)
    }

    // 订阅（消费者调用）
    fun toObservable(): Observable<ByteArray> = dataBus
}
