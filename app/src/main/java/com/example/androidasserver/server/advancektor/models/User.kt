package com.example.androidasserver.server.advancektor.models


data class User(
    val id: String = kotlin.random.Random.Default.nextBytes(16).toString(),
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis()
)