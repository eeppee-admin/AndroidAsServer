package com.example.androidasserver.ui.activity

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.example.androidasserver.R
import com.example.androidasserver.data.local.RouterItem
import com.example.androidasserver.databinding.ActivityMainBinding
import com.example.androidasserver.helper.DynamicPermissionHelper

/**
 * app主界面
 */
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        initPermissions()
        initView()
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        val datas = mutableListOf(
            RouterItem("/login", "登录"),
            RouterItem("/register", "注册"),
            RouterItem("详细doc:", "https://gitee.com/eeppee_admin")
        )
        val routerRecyclerView: RecyclerView = findViewById(R.id.router_recycler_view)
        routerRecyclerView.linear().setup {
            addType<RouterItem>(R.layout.item_router)
            onBind {
                findView<TextView>(R.id.router_url_tv).text = getModel<RouterItem>().url
                findView<TextView>(R.id.router_desc_tv).text = getModel<RouterItem>().desc
            }
        }.models = datas
    }

    private fun initPermissions() {
        DynamicPermissionHelper.request(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

}