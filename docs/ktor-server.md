好的！如果不使用 Netty，可以改用 Ktor 的 `CIO`（Coroutine-based I/O）引擎，它更轻量且适合 Android 环境。以下是修改后的代码，同时补充了 `UserRequest` 类的实现：


### **1. 修改依赖**
移除 Netty 依赖，添加 CIO 引擎：
```groovy
dependencies {
    implementation "io.ktor:ktor-server-core:2.3.3"
    implementation "io.ktor:ktor-server-cio:2.3.3"  // 使用 CIO 引擎替代 Netty
    implementation "io.ktor:ktor-server-content-negotiation:2.3.3"
    implementation "io.ktor:ktor-serialization-gson:2.3.3"
}
```


### **2. 重写 Ktor 服务器（使用 CIO 引擎）**
```kotlin
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object KtorServer {
    private var server: ApplicationEngine? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    // 用户数据存储（示例）
    private val users = mutableMapOf<String, User>()

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
    private fun hashPassword(password: String): Pair<String, String> {
        val salt = generateSalt()
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash) to Base64.getEncoder().encodeToString(salt)
    }

    // 验证密码
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
    private fun generateJwtToken(username: String): String {
        // 实际项目中应使用 JWT 库生成签名令牌
        return "Bearer ${Base64.getEncoder().encodeToString("$username:${System.currentTimeMillis()}".toByteArray())}"
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
```


### **3. 在 Android 中启动服务器**
保持与之前相同的启动方式：
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 启动服务器（在后台协程中）
        lifecycleScope.launch(Dispatchers.IO) {
            KtorServer.start()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "服务器已启动", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        KtorServer.stop()
    }
}
```


### **关键改进**
1. **使用 CIO 引擎**：替代 Netty，更轻量适合 Android。
2. **密码安全**：
    - 使用 PBKDF2 哈希算法（比 BCrypt 更适合移动设备）。
    - 生成随机盐值并存储。
3. **用户数据结构**：
    - 完整的 `User` 类定义。
    - 添加 ID、创建时间等字段。
4. **异步处理**：
    - 使用 Kotlin 协程避免阻塞主线程。
    - 后台作用域管理服务器生命周期。


### **注意事项**
1. **生产环境**：
    - 添加 HTTPS 支持（使用 `sslPort` 和证书配置）。
    - 使用真正的 JWT 库（如 `io.jsonwebtoken:jjwt`）。
2. **数据存储**：
    - 示例使用内存 Map，实际项目需连接 MongoDB：
      ```kotlin
      // MongoDB 连接示例
      val client = MongoClient.create("mongodb://localhost:27017")
      val database = client.getDatabase("yourdb")
      val usersCollection = database.getCollection("users")
      ```
3. **权限**：确保 AndroidManifest.xml 中添加网络权限。

这个版本移除了 Netty 依赖，使用更轻量的 CIO 引擎，并完善了用户认证逻辑，适合在 Android 设备上运行。

## 
```bash
PS C:\Users\fuckerfucker\AndroidStudioProjects\AndroidAsServer> http POST 192.168.16.156:8888/register username=damn password=123
{
    "message": "用户名已存在",
    "success": false
}

PS C:\Users\fuckerfucker\AndroidStudioProjects\AndroidAsServer> http POST 192.168.16.156:8888/login username=damn password=123   
{
    "message": "登录成功",
    "success": true,
    "token": "Bearer ZGFtbjoxNzQ4OTIzMTA5Mjg2"
}

```