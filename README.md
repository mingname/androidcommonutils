安卓使用的公共类
MqttManager  mqtt连接，订阅主题，发送接收消息
PermissionManager   权限申请管理
PermissionManager.requestPermissions( this, Arrays.asList(Permission.CAMERA), ()->{ }, (deniedList,doNotAskAgain) ->{ if (doNotAskAgain) { PermissionManager.gotoPermissionSettings(this); } else { // 用户拒绝 } } );
