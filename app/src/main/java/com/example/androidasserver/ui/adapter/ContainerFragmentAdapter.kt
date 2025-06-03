package com.example.androidasserver.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.properties.Delegates


/**
 * 加载Fragment页面的适配器
 * ref link: https://gitee.com/zhanshengshu/qloop_android/blob/cleanup/app/src/main/java/com/smiot/qloop/adapter/MainFragmentAdapter.java
 */
class ContainerFragmentAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle, list: MutableList<Fragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    // fragment列表
    private var mItemList: MutableList<Fragment> by Delegates.notNull()

    init {
        mItemList = list
    }

    override fun createFragment(position: Int): Fragment {
        return mItemList.get(position)
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }
}