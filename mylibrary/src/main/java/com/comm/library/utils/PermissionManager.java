package com.comm.library.utils;

/**
 * @author xqm
 * @date 2025/9/18 15:40
 * @description PermissionManager 类功能说明
 *PermissionManager.requestPermissions(
 *             this,
 *             listOf(Permission.CAMERA, Permission.RECORD_AUDIO),
 *             {
 *                 // 权限已全部授权
 *                 Toast.makeText(this, "权限通过", Toast.LENGTH_SHORT).show()
 *             },
 *             { deniedList, doNotAskAgain ->
 *                 // 权限被拒绝
 *                 if (doNotAskAgain) {
 *                     PermissionManager.gotoPermissionSettings(this)
 *                 } else {
 *                     Toast.makeText(this, "权限拒绝: $deniedList", Toast.LENGTH_SHORT).show()
 *                 }
 *             }
 *         )
 *
 */

import android.app.Activity;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.util.List;

public class PermissionManager {

    /**
     * 请求权限
     */
    public static void requestPermissions(Activity activity, List<String> permissions, Runnable onGranted, PermissionDeniedCallback onDenied) {
        XXPermissions.with(activity)
                .permission(permissions)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> granted, boolean all) {
                        if (all && onGranted != null) {
                            onGranted.run();
                        }
                    }

                    @Override
                    public void onDenied(List<String> denied, boolean doNotAskAgain) {
                        if (onDenied != null) {
                            onDenied.onDenied(denied, doNotAskAgain);
                        }
                    }
                });
    }

    /**
     * 检查权限
     */
    public static boolean hasPermission(Activity activity, List<String> permissions) {
        return XXPermissions.isGranted(activity, permissions);
    }

    /**
     * 打开应用权限设置
     */
    public static void gotoPermissionSettings(Activity activity) {
        XXPermissions.startPermissionActivity(activity);
    }

    /**
     * 自定义接口：拒绝回调
     */
    public interface PermissionDeniedCallback {
        void onDenied(List<String> deniedList, boolean doNotAskAgain);
    }
}

