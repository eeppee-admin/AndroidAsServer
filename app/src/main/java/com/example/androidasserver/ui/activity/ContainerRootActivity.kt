package com.example.androidasserver.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.androidasserver.R
import com.example.androidasserver.databinding.ActivityContainerBinding
import com.example.androidasserver.ext.viewBinding
import com.example.androidasserver.ui.adapter.ContainerFragmentAdapter
import com.example.androidasserver.ui.fragment.HomeFragment
import com.example.androidasserver.ui.fragment.SettingFragment
import com.jpeng.jptabbar.OnTabSelectListener
import com.jpeng.jptabbar.animate.AnimationType
import com.jpeng.jptabbar.anno.NorIcons
import com.jpeng.jptabbar.anno.SeleIcons
import com.jpeng.jptabbar.anno.Titles

/**
 * 容器Activity
 */
class ContainerRootActivity : AppCompatActivity(), OnTabSelectListener {
    @Titles
    private var mTitles: Array<out String> = arrayOf("Home", "Setting")

    @NorIcons
    private val mNormalIcons = intArrayOf(
        R.drawable.home_nor,
        R.drawable.home_nor,
    )

    @SeleIcons
    private val mSelectedIcons = intArrayOf(
        R.drawable.home,
        R.drawable.home
    )

    private val binding: ActivityContainerBinding by viewBinding(
        ActivityContainerBinding::inflate
    )

    private val mFragments: MutableList<Fragment> = mutableListOf(
        HomeFragment(),
        SettingFragment()
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupTabBar()
        setupViewPager2()
    }

    private fun setupTabBar() {
        with(binding.jpTabBar) {
//            setTitles(*mTitles)
            // viewpager2 不兼容
//            setContainer(binding.viewPager)
//            setNormalIcons(*mNormalIcons)
//            setSelectedIcons(*mSelectedIcons)
//            mTabbar.setTabTypeFace("fonts/Jaden.ttf");
//            setTabListener(this@ContainerRootActivity)
            setAnimation(AnimationType.SCALE2)
            setGradientEnable(true)
            // 禁用页面切换动画
            setPageAnimateEnable(false)
//            setSelectTab(0)
//            generate()
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

            else -> {}
        }
    }

    override fun onInterruptSelect(index: Int): Boolean {
        //        if(index==2){
        //            //如果这里有需要阻止Tab被选中的话,可以return true
        //            return true;
        //        }

        return false
    }
}