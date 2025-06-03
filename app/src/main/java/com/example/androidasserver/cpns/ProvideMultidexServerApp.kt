package com.example.androidasserver.cpns

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.IOSStyle
import kotlin.properties.Delegates

/**
 * 提供MultiDex 的 App类
 */
class ProvideMultidexServerApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        CONTEXT = applicationContext
        MultiDex.install(this)
        DialogX.init(this)
        // IOS 风格
        DialogX.globalStyle = IOSStyle.style()
    }

    companion object {
        var CONTEXT: Context by Delegates.notNull()
    }
}