package com.example.androidasserver.server

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.androidasserver.helper.EmailConfig
import com.example.androidasserver.helper.EmailService
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


import java.util.concurrent.ConcurrentHashMap

object VerificationCodeManager {
    // 存储：邮箱 -> [验证码, 过期时间]
    private val codeMap = ConcurrentHashMap<String, Pair<String, Long>>()
    private const val EXPIRATION_TIME = 5 * 60 * 1000  // 5分钟有效期

    // 保存验证码
    fun saveCode(email: String, code: String) {
        codeMap[email] = Pair(code, System.currentTimeMillis() + EXPIRATION_TIME)
    }

    // 验证验证码
    fun verifyCode(email: String, inputCode: String): Boolean {
        val entry = codeMap[email] ?: return false

        // 检查是否过期
        if (System.currentTimeMillis() > entry.second) {
            codeMap.remove(email)
            return false
        }

        // 验证验证码
        val isValid = entry.first == inputCode
        if (isValid) codeMap.remove(email)  // 验证通过后移除
        return isValid
    }

    // 检查邮箱是否已发送验证码（防刷）
    fun canSend(email: String): Boolean {
        val entry = codeMap[email] ?: return true
        return System.currentTimeMillis() > entry.second - 50000 // 剩余50秒内不能重发
    }
}

/**
 * 数据存储在内存的ktor 风格
 * 登录注册
 */
object KtorServer {
    private var server: ApplicationEngine? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // 邮箱服务实例
    private val emailService = EmailService(
        EmailConfig(
            username = "1690544550@qq.com",     // 替换为你的QQ邮箱
            password = "znxogkkkexpbeeaj",    // 替换为你的SMTP授权码
            host = "smtp.qq.com",
            port = "587"
        )
    )

    // 用户数据存储，内存值，不是数据库
    private val users = mutableMapOf<String, User>()

    @RequiresApi(Build.VERSION_CODES.O)
    fun start(port: Int = 8080) {
        server = embeddedServer(CIO, port = port) {
            install(ContentNegotiation) {
                gson()
            }
            routing {
                // 发送验证码接口
                post("/sendCode") {
                    val request = call.receive<EmailRequest>()

                    // 邮箱格式验证
                    if (!isValidEmail(request.email)) {
                        call.respond(
                            Response(
                                false,
                                "邮箱格式不正确"
                            )
                        )
                        return@post
                    }

                    // 防刷检查
                    if (!VerificationCodeManager.canSend(request.email)) {
                        call.respond(
                            Response(
                                false,
                                "发送频率过高，请稍后再试"
                            )
                        )
                        return@post
                    }

                    // 生成并发送验证码
                    val code = emailService.generateVerificationCode()
                    val isSent = emailService.sendVerificationCode(request.email, code)

                    if (isSent) {
                        VerificationCodeManager.saveCode(request.email, code)
                        call.respond(Response(true, "验证码已发送"))
                    } else {
                        call.respond(
                            Response(
                                false,
                                "验证码发送失败"
                            )
                        )
                    }
                }
                // 注册接口（带验证码验证）
                post("/register") {
                    val request = call.receive<RegisterRequest>()
                    // 验证输入
                    if (request.username.isNullOrBlank() ||
                        request.password.isNullOrBlank() ||
                        request.email.isNullOrBlank() ||
                        request.code.isNullOrBlank()
                    ) {
                        call.respond(Response(false, "所有字段均为必填项"))
                        return@post
                    }

                    // 验证邮箱格式
                    if (!isValidEmail(request.email)) {
                        call.respond(Response(false, "邮箱格式不正确"))
                        return@post
                    }
                    // 验证验证码
                    if (!VerificationCodeManager.verifyCode(request.email, request.code)) {
                        call.respond(Response(false, "验证码无效或已过期"))
                        return@post
                    }

                    // 检查用户是否已存在
                    if (users.containsKey(request.username)) {
                        call.respond(Response(false, "用户名已存在"))
                        return@post
                    }
                    // 加密密码
                    val (hash, salt) = hashPassword(request.password)

                    // 保存用户
                    val user = User(
                        id = UUID.randomUUID().toString(),
                        username = request.username,
                        email = request.email,
                        passwordHash = hash,
                        salt = salt,
                        createdAt = System.currentTimeMillis()
                    )
                    users[request.username] = user

                    call.respond(Response(true, "注册成功"))
                }


                // 登录接口
                post("/login") {
                    val request = call.receive<LoginRequest>()

                    // 验证输入
                    if (request.username.isNullOrBlank() || request.password.isNullOrBlank()) {
                        call.respond(Response(false, "用户名和密码不能为空"))
                        return@post
                    }

                    // 查找用户
                    val user = users[request.username] ?: return@post call.respond(
                        Response(false, "用户不存在")
                    )

                    // 验证密码
                    if (!verifyPassword(request.password, user.passwordHash, user.salt)) {
                        call.respond(Response(false, "密码错误"))
                        return@post
                    }
                    // 生成令牌
                    val token = generateJwtToken(user.username)
                    call.respond(LoginResponse(true, "登录成功", token))
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 5000)
        job.cancel()
    }


    // -----------------------------------------------------------------------

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
            createdAt = System.currentTimeMillis(),
            email = ""
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
    val message: String,
    val email: String = "",
    val code: String = ""
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val code: String
)

data class LoginRequest(val username: String, val password: String)

// 登录响应类
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null
)

// 用户数据类 新增用户字段
data class User(
    val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long
)

data class EmailRequest(val email: String)
data class Response(
    val success: Boolean,
    val message: String
)

// 邮箱格式验证
private fun isValidEmail(email: String): Boolean {
    val pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    return email.matches(pattern.toRegex())
}