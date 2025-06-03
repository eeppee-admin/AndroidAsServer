package com.example.androidasserver.cpns

import android.app.Service
import android.content.Intent
import android.os.IBinder
import cn.netdiscovery.command.CommandExecutor
import com.example.androidasserver.server.shutdownAndServer
import com.example.androidasserver.server.startAndServer
import com.safframework.log.L
import com.safframework.utils.localIPAddress
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * AndServer风格Service, 要主动去运行服务
 * todo: 需要广播来处理service启动停止的情况
 */
class AndServerService : Service() {
    private var andServer: Server by Delegates.notNull()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        andServer = AndServer.webServer(this)
            .port(6969)
            .timeout(10, TimeUnit.SECONDS)
            .listener(object : Server.ServerListener {
                override fun onStarted() {
                    val addr = localIPAddress
//                    L.d("TAG","Ping 百度之前")
//                    val result = CommandExecutor.execute("ping baidu.com")
//                    L.d("TAG",result.toString())
                    L.d("TAG", "AndServer启动在$addr:6969")
                }

                override fun onStopped() {
                }

                override fun onException(e: Exception?) {
                    e?.printStackTrace()
                }
            })
            .build()

        startAndServer(this, andServer)
        return super.onStartCommand(intent, flags, startId)
    }



    override fun onDestroy() {
        shutdownAndServer(andServer)
        super.onDestroy()
    }
}