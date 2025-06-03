package com.example.androidasserver.server.advancektor.services


import com.example.androidasserver.helper.EmailService
import com.example.androidasserver.server.advancektor.models.RegisterRequest
import com.example.androidasserver.server.advancektor.models.Response
import com.example.androidasserver.server.advancektor.models.User
import kotlinx.coroutines.*

class AuthService(
    private val emailService: EmailService,
//    private val passwordUtils: PasswordUtils,
//    private val userRepository: UserRepository // 假设存在用户仓库
) {
    // 注册用户
//    suspend fun register(request: RegisterRequest): Response {
//        // 验证输入
//        if (request.username.isNullOrBlank() ||
//            request.password.isNullOrBlank() ||
//            request.email.isNullOrBlank() ||
//            request.code.isNullOrBlank()) {
//            return Response(false, "所有字段均为必填项")
//        }
//
//        // 验证邮箱格式
//        if (!isValidEmail(request.email)) {
//            return Response(false, "邮箱格式不正确")
//        }
//
//        // 验证验证码
//        if (!VerificationCodeManager.verifyCode(request.email, request.code)) {
//            return Response(false, "验证码无效或已过期")
//        }
//
//        // 检查用户是否已存在
//        if (userRepository.existsByUsername(request.username)) {
//            return Response(false, "用户名已存在")
//        }
//
//        // 加密密码
//        val (hash, salt) = passwordUtils.hashPassword(request.password)
//
//        // 创建用户
//        val user = User(
//            username = request.username,
//            email = request.email,
//            passwordHash = hash,
//            salt = salt
//        )
//
//        // 保存用户
//        userRepository.save(user)
//
//        return Response(true, "注册成功")
//    }

    // 登录验证
//    suspend fun login(request: LoginRequest): LoginResponse {
//        // 验证输入
//        if (request.username.isNullOrBlank() || request.password.isNullOrBlank()) {
//            return LoginResponse(false, "用户名和密码不能为空")
//        }
//
//        // 查找用户
//        val user = userRepository.findByUsername(request.username)
//            ?: return LoginResponse(false, "用户不存在")
//
//        // 验证密码
//        if (!passwordUtils.verifyPassword(request.password, user.passwordHash, user.salt)) {
//            return LoginResponse(false, "密码错误")
//        }
//
//        // 生成令牌
//        val token = generateJwtToken(user.username)
//
//        return LoginResponse(true, "登录成功", token)
//    }

    // 邮箱格式验证
//    private fun isValidEmail(email: String): Boolean {
//        val pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
//        return email.matches(pattern.toRegex())
//    }

    // 生成 JWT 令牌
    private fun generateJwtToken(username: String): String {
        // 实际项目中应使用 JWT 库
        return "Bearer $username:${System.currentTimeMillis()}"
    }
}