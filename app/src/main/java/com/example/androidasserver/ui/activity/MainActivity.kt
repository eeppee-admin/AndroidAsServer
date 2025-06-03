package com.example.androidasserver.ui.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.example.androidasserver.R
import com.example.androidasserver.data.local.RouterItem
import com.example.androidasserver.databinding.ActivityMainBinding
import com.example.androidasserver.ext.checkIsInt
import com.example.androidasserver.ext.viewBinding
import com.example.androidasserver.helper.DynamicPermissionHelper
import com.example.androidasserver.helper.LocalNotificationHelper
import com.example.androidasserver.helper.LogProxyImpl
import com.example.androidasserver.server.startHttpServer
import com.king.pay.apppay.AppPay
import com.king.pay.wxpay.WXPay
import com.king.pay.wxpay.WXPayReq
import com.king.pay.wxpay.WXPayResult
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnInputDialogButtonClickListener
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener
import com.safframework.kotlin.coroutines.runInBackground
import com.safframework.log.L
import com.safframework.server.converter.gson.GsonConverter
import com.safframework.server.core.AndroidServer
import com.safframework.utils.localIPAddress
import kotlin.properties.Delegates


/**
 * app主界面
 */
class MainActivity : AppCompatActivity() {
    private var androidServer: AndroidServer? = null
    private var inputPort: Int by Delegates.notNull()

    private var mAppPay: AppPay? = null

    // 使用Activity ViewBinding Ext扩展
    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mAppPay = AppPay(this)
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
        with(binding) {
            // BUG: binding 无法识别到此id?
            routerRecyclerView.linear().setup {
                addType<RouterItem>(R.layout.item_router)
                onBind {
                    findView<TextView>(R.id.router_url_tv).text = getModel<RouterItem>().url
                    findView<TextView>(R.id.router_desc_tv).text = getModel<RouterItem>().desc
                }
                // 点击描述，弹出对话框
                onClick(R.id.router_desc_tv, R.id.router_url_tv) {
                    //Material 可滑动展开 BottomMenu 演示
                    BottomMenu.build()
                        .setTitle("底部弹窗")
                        .setBottomDialogMaxHeight(0.6f)
                        .setMenuList(
                            arrayOf<String>(
                                "复制github链接",
                                "打开github",
                                "给我钱?"
                            )
                        ).setOnMenuItemClickListener(object :
                            OnMenuItemClickListener<BottomMenu?> {
                            override fun onClick(
                                dialog: BottomMenu?,
                                text: CharSequence?,
                                index: Int
                            ): Boolean {
                                PopTip.show(text)
                                when (text) {
                                    "打开github" -> L.d("TAG", "点击打开github")
                                    "给我钱?" -> {
                                        val req = WXPayReq()
                                        req.setAppId("")
                                        mAppPay!!.sendWXPayReq(
                                            req,
                                            object : WXPay.OnPayListener {
                                                override fun onPayResult(result: WXPayResult) {
                                                    // 支付结果
                                                    if (result.isSuccess()) {
                                                        // TODO 支付成功

                                                        // 务必以服务端结果为准
                                                    }
                                                }
                                            })
                                    }
                                }
                                return false
                            }
                        }).show()
                }
            }.models = RouterItem.endPoints

            // BUG: kotlin Lambda无法生效
            openSwitch.setOnCheckedChangeListener(object :
                CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(
                    buttonView: CompoundButton?,
                    isChecked: Boolean
                ) {
                    if (isChecked) {
                        showPortInputDialog()

                    } else {
                        androidServer?.close()
                        openHintTv.text = ""
                        LocalNotificationHelper.cancelNotification(this@MainActivity, 1)
                    }
                }
            })
        }
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
            .setCancelable(false)
            .setOkButton(object : OnInputDialogButtonClickListener<InputDialog> {
                override fun onClick(
                    dialog: InputDialog?,
                    v: View?,
                    inputStr: String?
                ): Boolean {
                    if (inputStr?.checkIsInt() == true) {
                        inputPort = inputStr?.toIntOrNull()!!
                        if (isValidPort(inputPort)) {
                            with(binding) {
                                openSwitch.isChecked = true
                                openHintTv.text = "Server is Opening On Port: $inputPort"
                            }
                            // AndroidServer
                            upServer()
                            // AndServer
//                            startService(Intent(this@MainActivity, AndServerService::class.java))
                            dialog?.dismiss()
                        } else {
                            with(binding) {
                                openSwitch.isChecked = false
                                openHintTv.text = ""
                            }
                            androidServer?.close()
                            // todo: 没效果,6969没关闭
//                            stopService(Intent(this@MainActivity, AndServerService::class.java))
                            dialog?.dismiss()
                        }
                    } else {
                        binding.openSwitch.isChecked = false
                        androidServer?.close()
                        // todo: 6969没关闭
//                        stopService(Intent(this@MainActivity, AndServerService::class.java))
                        dialog?.dismiss()
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
                    binding.openSwitch.isChecked = false
                    dialog?.dismiss()
                    return true
                }
            })
        dia.show()
    }

    private fun upServer() {
        binding.openHintTv.text =
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

            startHttpServer(this@MainActivity, androidServer!!)
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
        androidServer?.close()
        // todo: 6969没关闭
//        stopService(Intent(this@MainActivity, AndServerService::class.java))
        super.onDestroy()
    }
}