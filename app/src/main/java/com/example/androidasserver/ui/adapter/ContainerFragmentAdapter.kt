package com.example.androidasserver.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.properties.Delegates


/**
 * 加载Fragment页面的适配器
 */
class ContainerFragmentAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle, list: MutableList<Fragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private var mItemList: MutableList<Fragment> by Delegates.notNull() // fragment列表


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