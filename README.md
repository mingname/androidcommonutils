安卓使用的公共类
```
MqttManager  mqtt连接，订阅主题，发送接收消息
PermissionManager   权限申请管理
 PermissionManager.requestPermissions(
            this,
            listOf(Permission.CAMERA, Permission.RECORD_AUDIO),
            {
                // 权限已全部授权
                Toast.makeText(this, "权限通过", Toast.LENGTH_SHORT).show()
            },
            { deniedList, doNotAskAgain ->
                // 权限被拒绝
                if (doNotAskAgain) {

                    PermissionManager.gotoPermissionSettings(this)
                } else {
                    Toast.makeText(this, "权限拒绝: $deniedList", Toast.LENGTH_SHORT).show()
                }
            }
        )
```