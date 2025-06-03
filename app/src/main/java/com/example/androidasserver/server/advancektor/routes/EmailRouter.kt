package com.example.androidasserver.server.advancektor.routes

import com.example.androidasserver.helper.EmailService
import com.example.androidasserver.server.advancektor.models.EmailRequest
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.emailRoutes(emailService: EmailService) {
    route("/email") {
        // 发送验证码
        post("/sendCode") {
            val request = call.receive<EmailRequest>()
            val result = emailService.sendVerificationCode(
                request.email,
                "0"
            )
            call.respond(result)
        }
    }
}