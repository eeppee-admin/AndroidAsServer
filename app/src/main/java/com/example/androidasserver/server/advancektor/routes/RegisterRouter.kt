package com.example.androidasserver.server.advancektor.routes

import com.example.androidasserver.server.advancektor.models.RegisterRequest
import com.example.androidasserver.server.advancektor.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.registerRoutes(
    /**authService: AuthService*/
) {
    route("/auth") {
        // 注册接口
        post("/register") {
            val request = call.receive<RegisterRequest>()
//            val result = authService.register(request)
            call.respond("ok")
        }
    }
}