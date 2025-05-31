package com.example.androidasserver.helper

import androidx.fragment.app.FragmentActivity
import com.example.androidasserver.ui.fragment.InvisibleForPermissionFragment

/**
 * 动态申请权限帮助类
 */
object DynamicPermissionHelper {
    private const val TAG = "DynamicPermissionHelper"

    var defaultCallback: (Boolean, List<String>) -> Unit = { isOK, one ->
        {
            // just ignore all
        }
    }

    /**
     * Usage:
     *  PermissionHelper.request(this, Manifest.permission.CALL_PHONE) { allGranted, deniedList ->
     *             {
     *                 if (allGranted) {
     *                     Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()
     *                 } else {
     *                     Toast.makeText(this, "$deniedList", Toast.LENGTH_SHORT).show()
     *                 }
     *             }
     *         }
     */
    fun request(
        activity: FragmentActivity,
        vararg permissions: String,
        callback: (Boolean, List<String>) -> Unit = defaultCallback
    ) {
        val fragmentManager = activity.supportFragmentManager
        val existedFragment = fragmentManager.findFragmentByTag(TAG)
        val fragment = if (existedFragment != null) {
            existedFragment as InvisibleForPermissionFragment
        } else {
            val invisibleFragment = InvisibleForPermissionFragment()
            fragmentManager.beginTransaction().add(invisibleFragment, TAG).commitNow()
            invisibleFragment
        }
        fragment.requestNow(callback, *permissions)
    }
}