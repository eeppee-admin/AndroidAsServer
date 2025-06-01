package com.example.androidasserver.ui.activity

import android.Manifest
import android.app.ComponentCaller
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.example.androidasserver.R
import com.example.androidasserver.cpns.AndServerService
import com.example.androidasserver.data.local.RouterItem
import com.example.androidasserver.ext.checkIsCommonServicePort
import com.example.androidasserver.ext.checkIsInt
import com.example.androidasserver.helper.DynamicPermissionHelper
import com.example.androidasserver.helper.LocalNotificationHelper
import com.example.androidasserver.helper.LogProxyImpl
import com.example.androidasserver.helper.endpoints
import com.example.androidasserver.server.startHttpServer
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.interfaces.OnInputDialogButtonClickListener
import com.safframework.kotlin.coroutines.runInBackground
import com.safframework.log.L
import com.safframework.server.converter.gson.GsonConverter
import com.safframework.server.core.AndroidServer
import com.safframework.utils.NotificationUtil.Companion.CHANNEL_ID
import com.safframework.utils.localIPAddress
import kotlin.properties.Delegates

/**
 * app主界面
 */
class MainActivity : AppCompatActivity() {
    private var androidServer: AndroidServer by Delegates.notNull()
    val routerRecyclerView: RecyclerView by lazy {
        findViewById(R.id.router_recycler_view)
    }

    val openAndroidServerSwitch: Switch by lazy {
        findViewById(R.id.open_switch)
    }

    val openHintTv: AppCompatTextView by lazy {
        findViewById(R.id.open_hint_tv)
    }
    var inputPort: Int by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermissions()
        initView()
    }

    /**
     * 在程序从状态栏切回来的时候，展示一个弹窗
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("FROM_NOTIFICATION", false)) {
            showNotificationDialog()
        }
    }


    /**
     * 在程序从状态栏切换回来的时候，展示一个弹窗
     */
    private fun showNotificationDialog() {
        MessageDialog.show("提示", "你从通知栏回来了", "确定");
//        AlertDialog.Builder(this)
//            .setTitle("提示")
//            .setMessage("你从通知栏回来了！")
//            .setPositiveButton("确定") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .show()
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        L.d("TAG", RouterItem.endPoints.toString())
        // BUG: binding 无法识别到此id?
        routerRecyclerView.linear().setup {
            addType<RouterItem>(R.layout.item_router)
            onBind {
                findView<TextView>(R.id.router_url_tv).text = getModel<RouterItem>().url
                findView<TextView>(R.id.router_desc_tv).text = getModel<RouterItem>().desc
            }
        }.models = RouterItem.endPoints

        // BUG: kotlin Lambda无法生效
        openAndroidServerSwitch.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(
                buttonView: CompoundButton?,
                isChecked: Boolean
            ) {
                if (isChecked) {
                    L.d("TAG", "打开了")
                    showPortInputDialog()

                } else {
                    L.d("TAG", "关闭了")
                    androidServer.close()
                    openHintTv.text = ""
                    LocalNotificationHelper.cancelNotification(this@MainActivity, 1)
                }
            }
        })
    }

    /**
     * 权限: 通知, 读写文件
     */
    private fun initPermissions() {
        DynamicPermissionHelper.request(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }


    /**
     * 展示端口输入对话框
     */
    private fun showPortInputDialog() {
        val dia = InputDialog("请输入端口号", "不能输入常见的端口号,如3306", "确定", "取消")
            .setCancelable(true)
            .setOkButton(object : OnInputDialogButtonClickListener<InputDialog> {
                override fun onClick(
                    dialog: InputDialog?,
                    v: View?,
                    inputStr: String?
                ): Boolean {
                    if (inputStr?.checkIsInt() == true) {
                        inputPort = inputStr?.toIntOrNull()!!
                        if (isValidPort(inputPort)) {
                            openAndroidServerSwitch.isChecked = true
                            openHintTv.text = "Server is Opening On Port: $inputPort"
                            // AndroidServer
                            upServer()
                            // AndServer
                            startService(Intent(this@MainActivity, AndServerService::class.java))
                            dialog?.dismiss()
                        } else {
                            openAndroidServerSwitch.isChecked = false
                            openHintTv.text = ""
                            androidServer.close()
                            // todo: 没效果,6969没关闭
                            stopService(Intent(this@MainActivity, AndServerService::class.java))

                            dialog?.dismiss()
                        }
                    } else {
                        openAndroidServerSwitch.isChecked = false
                        dialog?.dismiss()
                        androidServer.close()
                        // todo: 6969没关闭
                        stopService(Intent(this@MainActivity, AndServerService::class.java))
                    }
                    return true
                }
            })
            .setCancelButton(object : OnInputDialogButtonClickListener<InputDialog> {
                override fun onClick(
                    dialog: InputDialog?,
                    v: View?,
                    inputStr: String?
                ): Boolean {
                    openAndroidServerSwitch.isChecked = false
                    dialog?.dismiss()
                    return true
                }
            })
        dia.show()
    }

    private fun upServer() {
        openHintTv.text =
            "内网IP: $localIPAddress\nAndroidServer库在${inputPort}端口提供服务\n6969端口也有服务"
        runInBackground {
            androidServer = AndroidServer.Builder {
                converter {
                    GsonConverter()
                }
                logProxy {
                    LogProxyImpl
                }
                port {
                    inputPort
                }
            }.build()

            startHttpServer(this@MainActivity, androidServer)
            LocalNotificationHelper.justShowOneDefault(
                this@MainActivity,
                "Server",
                "Start At $inputPort and 6969"
            )
        }
    }

    /**
     *启动本程序不要在常见的port
     */
    private fun isValidPort(port: Int): Boolean {
        val commonPorts = listOf(80, 3306, 443, 22, 21, 25, 110, 143, 465, 993, 995)
        return port !in commonPorts && port in 1..65535
    }

    override fun onDestroy() {
        androidServer.close()
        // todo: 6969没关闭
        stopService(Intent(this@MainActivity, AndServerService::class.java))
        super.onDestroy()
    }
}