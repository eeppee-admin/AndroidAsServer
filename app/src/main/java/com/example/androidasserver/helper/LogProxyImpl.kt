package com.example.androidasserver.helper

import com.safframework.log.L
import com.safframework.server.core.log.LogProxy

/**
 * 实现LogProxy代理的类，其实，你可以切入任何的第三放log来实现LogProxy
 */
object LogProxyImpl : LogProxy {
    override fun e(tag: String, msg: String) {
        L.e(tag, msg)
    }

    override fun w(tag: String, msg: String) {
        L.w(tag, msg)
    }

    override fun i(tag: String, msg: String) {
        L.i(tag, msg)
    }

    override fun d(tag: String, msg: String) {
        L.d(tag, msg)
    }
}