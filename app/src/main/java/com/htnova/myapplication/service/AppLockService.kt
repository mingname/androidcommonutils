package com.htnova.myapplication.service

/**
 * @author xqm
 * @date 2025/11/25 17:09
 * @description AppLockService 类功能说明
 */

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.htnova.myapplication.MainActivity

class AppLockService : AccessibilityService() {

    private val TAG = "AppLockService"
    private val lockedApps = listOf(
        "com.eg.android.AlipayGphone",
        "com.tencent.mobileqq"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            Log.d(TAG, "当前前台应用: $packageName")

            if (lockedApps.contains(packageName)) {
                // 启动锁屏 Activity
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}

}
