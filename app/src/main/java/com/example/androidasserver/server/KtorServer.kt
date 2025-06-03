package com.example.androidasserver.server

import android.os.Build
import androidx.annotation.RequiresApi
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object KtorServer {
    private var server: ApplicationEngine? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // 用户数据存储，内存值，不是数据库
    private val users = mutableMapOf<String, User>()

    @RequiresApi(Build.VERSION_CODES.O)
    fun start(port: Int = 8080) {
        server = embeddedServer(CIO, port = port) {
            install(ContentNegotiation) {
                gson()
            }

            routing {
                post("/register") {
                    val request = call.receive<UserRequest>()
                    val response = handleRegistration(request)
                    call.respond(response)
                }

                post("/login") {
                    val request = call.receive<UserRequest>()
                    val response = handleLogin(request)
                    call.respond(response)
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 5000)
        job.cancel()
    }

    // 处理注册逻辑
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleRegistration(request: UserRequest): RegistrationResponse {
        // 验证输入
        if (request.username.isNullOrBlank() || request.password.isNullOrBlank()) {
            return RegistrationResponse(false, "用户名和密码不能为空")
        }

        // 检查用户是否已存在
        if (users.containsKey(request.username)) {
            return RegistrationResponse(false, "用户名已存在")
        }

        // 加密密码
        val (hash, salt) = hashPassword(request.password)

        // 保存用户（实际项目中应存入 MongoDB）
        val user = User(
            id = UUID.randomUUID().toString(),
            username = request.username,
            passwordHash = hash,
            salt = salt,
            createdAt = System.currentTimeMillis()
        )
        users[request.username] = user

        return RegistrationResponse(true, "注册成功")
    }

    // 处理登录逻辑
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleLogin(request: UserRequest): LoginResponse {
        // 验证输入
        if (request.username.isNullOrBlank() || request.password.isNullOrBlank()) {
            return LoginResponse(false, null, "用户名和密码不能为空")
        }

        // 查找用户
        val user = users[request.username] ?: return LoginResponse(false, null, "用户不存在")

        // 验证密码
        val isPasswordValid = verifyPassword(request.password, user.passwordHash, user.salt)
        if (!isPasswordValid) {
            return LoginResponse(false, null, "密码错误")
        }

        // 生成令牌
        val token = generateJwtToken(user.username)
        return LoginResponse(true, token, "登录成功")
    }

    // 密码哈希（使用 PBKDF2）
    @RequiresApi(Build.VERSION_CODES.O)
    private fun hashPassword(password: String): Pair<String, String> {
        val salt = generateSalt()
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash) to Base64.getEncoder().encodeToString(salt)
    }

    // 验证密码
    @RequiresApi(Build.VERSION_CODES.O)
    private fun verifyPassword(password: String, storedHash: String, storedSalt: String): Boolean {
        val salt = Base64.getDecoder().decode(storedSalt)
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        return storedHash == Base64.getEncoder().encodeToString(hash)
    }

    // 生成盐值
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    // 生成 JWT 令牌（简化示例）
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateJwtToken(username: String): String {
        // 实际项目中应使用 JWT 库生成签名令牌
        return "Bearer ${
            Base64.getEncoder()
                .encodeToString("$username:${System.currentTimeMillis()}".toByteArray())
        }"
    }
}

// 用户请求类（补充实现）
data class UserRequest(
    val username: String? = null,
    val password: String? = null
)

// 注册响应类
data class RegistrationResponse(
    val success: Boolean,
    val message: String
)

// 登录响应类
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null
)

// 用户数据类
data class User(
    val id: String,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long
)