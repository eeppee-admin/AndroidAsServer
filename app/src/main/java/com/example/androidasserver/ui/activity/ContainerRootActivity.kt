package com.example.androidasserver.ui.activity

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.androidasserver.R
import com.example.androidasserver.databinding.ActivityContainerBinding
import com.example.androidasserver.ext.viewBinding
import com.example.androidasserver.server.KtorServer
import com.example.androidasserver.server.KtorServerV2
import com.example.androidasserver.ui.adapter.ContainerFragmentAdapter
import com.example.androidasserver.ui.fragment.HomeFragment
import com.example.androidasserver.ui.fragment.SettingFragment
import com.jpeng.jptabbar.OnTabSelectListener
import com.jpeng.jptabbar.animate.AnimationType
import com.jpeng.jptabbar.anno.NorIcons
import com.jpeng.jptabbar.anno.SeleIcons
import com.jpeng.jptabbar.anno.Titles
import com.safframework.log.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 容器Activity
 */
class ContainerRootActivity : AppCompatActivity(), OnTabSelectListener {
    @Titles
    private var mTitles: Array<out String> = arrayOf("Home", "Setting")

    @NorIcons
    private val mNormalIcons = intArrayOf(
        R.drawable.home_normal,
        R.drawable.setting_normal,
    )

    @SeleIcons
    private val mSelectedIcons = intArrayOf(
        R.drawable.home_selected,
        R.drawable.setting_selected
    )

    private val binding: ActivityContainerBinding by viewBinding(
        ActivityContainerBinding::inflate
    )

    private val mFragments: MutableList<Fragment> = mutableListOf(
        HomeFragment(),
        SettingFragment()
    )


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupTabBar()
        setupViewPager2()

        //启动KtorServer
        lifecycleScope.launch(Dispatchers.IO) {
            KtorServer.start(port = 8888)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ContainerRootActivity,
                    "服务器已启动",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 启动服务器（在后台协程中）
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                // 替换为你的 MongoDB 连接 URI
//                val mongoUri =
//                    "mongodb://xxxvideoslover:xxxvideoslover@cluster0.5jzofcs.mongodb.net/go-im?retryWrites=true&w=majority&appName=Cluster0"
//
//                val damn = KtorServerV2.start(
//                    port = 9999,
//                    mongoUri = mongoUri,
//                    dbName = "go-im",
//                    collectionName = "users"
//                )
//
//                withContext(Dispatchers.Main) {
//                    L.d("TAG", damn.toString())
//                    Toast.makeText(
//                        this@ContainerRootActivity,
//                        "服务器已启动",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@ContainerRootActivity,
//                        "服务器启动失败: ${e.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }

    }

    private fun setupTabBar() {
        with(binding.jpTabBar) {
            setAnimation(AnimationType.SCALE2)
            setGradientEnable(false)
            // 禁用页面切换动画
            setPageAnimateEnable(false)
            // 必须设置去监听
            setTabListener(this@ContainerRootActivity)
        }
    }

    private fun setupViewPager2() {
        val containerFragmentAdapter = ContainerFragmentAdapter(
            supportFragmentManager,
            lifecycle,
            mFragments
        )
        with(binding.viewPager) {
            adapter = containerFragmentAdapter
            isUserInputEnabled = false
            offscreenPageLimit = mFragments.size - 1
        }
    }

    override fun onTabSelect(index: Int) {
        when (index) {
            0 ->
                binding.viewPager.setCurrentItem(0, false)

            1 ->
                binding.viewPager.setCurrentItem(1, false)
        }
    }

    override fun onInterruptSelect(index: Int): Boolean {
        //        if(index==2){
        //            //如果这里有需要阻止Tab被选中的话,可以return true
        //            return true;
        //        }

        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        KtorServerV2.stop()
    }
}