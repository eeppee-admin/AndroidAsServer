package com.example.androidasserver.server.advancektor.models

data class EmailRequest(val email: String)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val code: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class Response(
    val success: Boolean,
    val message: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null
)