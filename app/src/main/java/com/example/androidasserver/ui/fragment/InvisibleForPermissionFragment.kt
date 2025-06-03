package com.example.androidasserver.ui.fragment

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

/**
 * 为了申请权限的Fragment
 */
@Suppress("DEPRECATION")
class InvisibleForPermissionFragment : Fragment() {
    private var callback: ((Boolean, List<String>) -> Unit)? = null
    val REQUEST_CODE = 1

    /**
     * 现在就申请
     */
    fun requestNow(cb: (Boolean, List<String>) -> Unit, vararg permissions: String) {
        callback = cb
        requestPermissions(permissions, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            val deniedList = ArrayList<String>()
            for ((index, result) in grantResults.withIndex()) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(permissions[index])
                }
            }
            val allGranted = deniedList.isEmpty()
            callback?.let {
                it(allGranted, deniedList)
            }
        }
    }
}