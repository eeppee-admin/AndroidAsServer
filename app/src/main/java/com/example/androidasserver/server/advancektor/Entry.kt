package com.example.androidasserver.server.advancektor

import com.example.androidasserver.helper.EmailConfig
import com.example.androidasserver.helper.EmailService
import com.example.androidasserver.server.advancektor.routes.emailRoutes
import com.example.androidasserver.server.advancektor.routes.loginRoutes
import com.example.androidasserver.server.advancektor.routes.registerRoutes
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.server.routing.routing
import kotlinx.coroutines.*

/**
 * Server 入口
 */
object Entry {
    private var server: ApplicationEngine? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // 服务实例
    private val emailService = EmailService(
        EmailConfig(
            password = "damn"
        )
    )
//    private val passwordUtils = PasswordUtils()
//    private val userRepository = InMemoryUserRepository() // 内存存储实现
//    private val authService = AuthService(emailService, passwordUtils, userRepository)

    fun start(port: Int = 8080) {
        server = embeddedServer(CIO, port = port) {
            // 配置序列化
            install(ContentNegotiation) {
                gson()
            }

            // 注册路由
            routing {
//                loginRoutes(authService)
                loginRoutes()
                registerRoutes()
                emailRoutes(emailService)
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 5000)
        job.cancel()
    }
}