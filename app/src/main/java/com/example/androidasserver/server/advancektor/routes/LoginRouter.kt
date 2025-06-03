package com.example.androidasserver.server.advancektor.routes


import com.example.androidasserver.server.advancektor.models.LoginRequest
import com.example.androidasserver.server.advancektor.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.loginRoutes(
    /**authService: AuthService*/
) {
    route("/auth") {
        // 登录接口
        post("/login") {
            val request = call.receive<LoginRequest>()
//            val result = authService.login(request)
            call.respond("ok")
        }
    }
}

