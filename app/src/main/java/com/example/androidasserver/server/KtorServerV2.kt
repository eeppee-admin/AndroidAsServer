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


import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
import org.bson.types.ObjectId

/**
 * TODO: crash bug fix
 */

object MongoDbManager {
    private lateinit var usersCollection: MongoCollection<UserV2>

    // 初始化 MongoDB 连接
    suspend fun init(uri: String, dbName: String, collectionName: String) =
        withContext(Dispatchers.IO) {
            val client = MongoClient.create(uri)
            val database = client.getDatabase(dbName)
            usersCollection = database.getCollection(collectionName)
        }

    // 插入用户
    suspend fun insertUser(user: UserV2) = withContext(Dispatchers.IO) {
        usersCollection.insertOne(user)
    }

    // 根据用户名查找用户（修正版）
    suspend fun findUserByUsername(username: String): UserV2? = withContext(Dispatchers.IO) {
        null
    }
}

// MongoDB 映射的用户类
data class UserV2(
    @BsonId val id: ObjectId = ObjectId(),
    val username: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis()
)

object KtorServerV2 {
    private var server: ApplicationEngine? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // 启动服务器并初始化 MongoDB
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun start(
        port: Int = 8080,
        mongoUri: String = "mongodb://localhost:27017",
        dbName: String = "android_auth",
        collectionName: String = "users"
    ) {
        // 初始化 MongoDB
        MongoDbManager.init(mongoUri, dbName, collectionName)

        server = embeddedServer(CIO, port = port) {
            install(ContentNegotiation) {
                gson()
            }

            routing {
                post("/register") {
                    val request = call.receive<UserRequest>()
                    val response = scope.async { handleRegistration(request) }.await()
                    call.respond(response)
                }

                post("/login") {
                    val request = call.receive<UserRequest>()
                    val response = scope.async { handleLogin(request) }.await()
                    call.respond(response)
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 5000)
        job.cancel()
    }

    // 处理注册逻辑（使用 MongoDB）
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleRegistration(request: UserRequest): RegistrationResponse {
        if (request.username.isNullOrBlank() || request.password.isNullOrBlank()) {
            return RegistrationResponse(false, "用户名和密码不能为空")
        }

        // 检查用户是否已存在
        if (MongoDbManager.findUserByUsername(request.username) != null) {
            return RegistrationResponse(false, "用户名已存在")
        }


        // 加密密码
        val (hash, salt) = hashPassword(request.password)

        // 创建用户并保存到 MongoDB
        val user = UserV2(
            username = request.username,
            passwordHash = hash,
            salt = salt,
            createdAt = 1,
        )
        MongoDbManager.insertUser(user)

        return RegistrationResponse(true, "注册成功")
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

    // 生成盐值
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    // 处理登录逻辑（使用 MongoDB）
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleLogin(request: UserRequest): LoginResponse {
        if (request.username.isNullOrBlank() || request.password.isNullOrBlank()) {
            return LoginResponse(false, null, "用户名和密码不能为空")
        }

        // 查找用户
        val user = MongoDbManager.findUserByUsername(request.username)
        if (user == null) {
            return LoginResponse(false, null, "用户不存在")
        }

        // 验证密码
        val isPasswordValid = verifyPassword(request.password, user.passwordHash, user.salt)
        if (!isPasswordValid) {
            return LoginResponse(false, null, "密码错误")
        }

        // 生成令牌
        val token = generateJwtToken(user.username)
        return LoginResponse(true, token, "登录成功")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun verifyPassword(password: String, storedHash: String, storedSalt: String): Boolean {
        val salt = Base64.getDecoder().decode(storedSalt)
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        return storedHash == Base64.getEncoder().encodeToString(hash)
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