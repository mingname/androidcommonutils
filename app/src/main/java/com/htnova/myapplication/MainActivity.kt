package com.htnova.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.widget.Toast
import com.comm.library.utils.PermissionManager
import com.hjq.permissions.Permission

class MainActivity : AppCompatActivity() {
    private var autoClickService: AutoClickService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnClick).setOnClickListener {
            packageManager.getLaunchIntentForPackage("com.tencent.mm")?.let {
                startActivity(it)
            }
            autoClickService?.click(500f, 1000f) // 点击坐标 (500,1000)
            Handler().postDelayed({
                autoClickService?.click(500f, 1000f)
            }, 2000)
        }

        findViewById<Button>(R.id.btnSwipe).setOnClickListener {
            autoClickService?.swipe(300f, 1200f, 800f, 1200f) // 左右滑动
        }
        findViewById<LinearLayout>(R.id.lineall).setOnClickListener {
            Log.d("xqm","点击了")
        }

        // 请求相机和录音权限
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

    }





}