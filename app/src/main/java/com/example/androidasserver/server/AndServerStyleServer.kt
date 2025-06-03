package com.example.androidasserver.server

import android.content.Context
import com.safframework.log.L
import com.safframework.utils.localIPAddress
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 *
 * https://www.baeldung-cn.com/kotlin/val-cannot-be-reassigned-error
 */
fun startAndServer(context: Context, andServer: Server) {
//    andServer = AndServer.webServer(context)
//        .port(port)
//        .timeout(10, TimeUnit.SECONDS)
//        .listener(object : Server.ServerListener {
//            override fun onStarted() {
//                val addr = localIPAddress
//                L.d("TAG", addr)
//            }
//
//            override fun onStopped() {
//            }
//
//            override fun onException(e: Exception?) {
//                e?.printStackTrace()
//            }
//        })
//        .build()

    andServer.startup()
}

fun shutdownAndServer(andServer: Server) {
    andServer.shutdown()
}