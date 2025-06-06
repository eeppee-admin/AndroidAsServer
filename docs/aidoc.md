当点击通知栏时程序新创建的问题，通常与 Android 的任务栈管理和 Activity 的启动模式有关。以下是几种可能的解决方案：

### 1. **检查通知的 PendingIntent 配置**

确保通知的 `PendingIntent` 设置正确。如果通知启动了新的任务（Task），可能会导致应用的新实例被创建。你可以通过设置 `PendingIntent` 的标志位来避免这种情况。

```kotlin
val intent = Intent(this, MainActivity::class.java).apply {
    // 标志位：如果任务栈中已存在该 Activity，则将其带到前台，而不是创建新的实例
    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
}

val pendingIntent = PendingIntent.getActivity(
    this,
    0,
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
```

### 2. **调整 Activity 的启动模式**

在 `AndroidManifest.xml` 中，可以为 `MainActivity`（或相关的 Activity）设置启动模式。根据需求选择合适的启动模式：

| 启动模式                          | 描述                                                                                   |
|-----------------------------------|---------------------------------------------------------------------------------------|
| `singleTop`                       | 如果目标 Activity 已经位于当前任务栈的顶部，则不会创建新的实例，而是直接调用 `onNewIntent()` 方法。 |
| `singleTask`                      | 如果目标 Activity 已经存在于任务栈中，则将其带到前台，并调用 `onNewIntent()` 方法，而不是创建新的实例。 |
| `singleInstance`                  | 类似于 `singleTask`，但 Activity 会被放置在一个单独的任务栈中，且该任务栈中只能有一个 Activity 实例。 |

在 `AndroidManifest.xml` 中设置：

```xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTop"> <!-- 或 singleTask -->
</activity>
```

### 3. **检查任务栈的行为**

如果应用允许多个任务实例（例如从通知启动时创建新的任务），可以通过以下方式避免：

#### （1）通知设置中的任务栈配置

在创建通知时，可以通过 `TaskStackBuilder` 确保通知启动的 Activity 被添加到现有的任务栈中，而不是创建新的任务。

```kotlin
val resultIntent = Intent(this, MainActivity::class.java)

// 使用 TaskStackBuilder 确保 Activity 被添加到现有的任务栈
val stackBuilder = TaskStackBuilder.create(this).apply {
    addNextIntentWithParentStack(resultIntent)
}

val pendingIntent = stackBuilder.getPendingIntent(
    0,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
```

#### （2）检查是否启用了多个任务实例

在 `AndroidManifest.xml` 中，确保 `MainActivity` 没有设置 `allowTaskReparenting` 属性为 `true`，否则可能会导致任务在不同的任务栈之间移动。

```xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTop"
    android:allowTaskReparenting="false"> <!-- 禁用任务重置 -->
</activity>
```

### 4. **检查通知相关的配置**

确保通知的 `setAutoCancel(true)` 没有导致意外行为。如果你不需要通知在点击后自动取消，可以设置为 `false`，但这与创建新实例无关。

```kotlin
val notification = NotificationCompat.Builder(this, CHANNEL_ID)
    .setContentIntent(pendingIntent)
    .setAutoCancel(false) // 禁用自动取消
    .build()
```

### 5. **确保通知的 Intent 是唯一的**

如果通知的 `Intent` 参数（如 `Intent` 的 Extra 数据）不同，系统可能会认为这是一个新的 `Intent`，从而创建新的实例。确保通知的 `Intent` 在内容上是一致的。

```kotlin
val intent = Intent(this, MainActivity::class.java).apply {
    putExtra("notification", true) // 确保相同的 Extra 数据
}
```

### 6. **调试与日志检查**

在 `MainActivity` 中添加日志，检查是否每次点击通知都会调用 `onCreate()` 或 `onNewIntent()` 方法。

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d("MainActivity", "onCreate called")
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    Log.d("MainActivity", "onNewIntent called")
}
```

如果每次点击通知都调用 `onCreate()`，说明新的实例被创建；如果调用 `onNewIntent()`，则说明实例被重用。




添加 FROM_NOTIFICATION 标志：在创建 PendingIntent 时，向 Intent 中添加一个布尔类型的 Extra 数据，表示这是从通知启动的。
处理通知点击事件：在 MainActivity 的 onCreate 和 onNewIntent 方法中，检查是否包含 FROM_NOTIFICATION 标志。如果包含，弹出提示窗口。
确保 MainActivity 的启动模式为 singleTop：在 AndroidManifest.xml 中设置 MainActivity 的 launchMode 为 singleTop，以便在 Activity 已经位于任务栈顶部时，不会创建新的实例，而是调用 onNewIntent 方法。
通过这种方式，就能实现在点击通知栏时，切回原程序并弹出提示窗口的功能。


要实现底部导航栏在特定节日显示不同图标的功能，通常需要结合日期判断、资源管理和动态更新UI的技术。以下是几种常见的实现方案：


### **方案一：本地资源切换（推荐）**
**原理**：在应用内预先存储多套图标资源，根据当前日期判断节日，动态切换图标。

#### **实现步骤**
1. **准备图标资源**：
    - 在`res/drawable`目录下按节日分类存放图标（如`ic_home_normal.png`、`ic_home_christmas.png`）。

2. **日期判断工具类**：
   ```kotlin
   object FestivalUtils {
       fun getFestivalIconResId(originalResId: Int): Int {
           val calendar = Calendar.getInstance()
           val month = calendar.get(Calendar.MONTH) + 1
           val day = calendar.get(Calendar.DAY_OF_MONTH)
           
           // 根据日期返回对应节日图标
           return when {
               // 圣诞节（12月25日）
               month == 12 && day == 25 -> {
                   getChristmasIconResId(originalResId)
               }
               // 春节（示例：假设为1月1日，实际需根据农历计算）
               month == 1 && day == 1 -> {
                   getSpringFestivalIconResId(originalResId)
               }
               // 其他节日...
               else -> originalResId // 默认返回原始图标
           }
       }
       
       private fun getChristmasIconResId(originalResId: Int): Int {
           // 根据原始资源ID映射到圣诞节图标
           return when (originalResId) {
               R.drawable.ic_home_normal -> R.drawable.ic_home_christmas
               R.drawable.ic_profile_normal -> R.drawable.ic_profile_christmas
               else -> originalResId
           }
       }
       
       // 其他节日图标映射方法...
   }
   ```

3. **动态更新导航栏图标**：
   ```kotlin
   // 在Activity或Fragment中初始化导航栏
   private fun initBottomNavigation() {
       val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
       
       // 获取菜单项并更新图标
       for (i in 0 until bottomNavigationView.menu.size()) {
           val menuItem = bottomNavigationView.menu.getItem(i)
           val originalIconResId = getOriginalIconResId(menuItem.itemId)
           val festivalIconResId = FestivalUtils.getFestivalIconResId(originalIconResId)
           menuItem.icon = ContextCompat.getDrawable(this, festivalIconResId)
       }
   }
   
   // 根据菜单项ID获取原始图标资源ID（需自定义映射）
   private fun getOriginalIconResId(menuItemId: Int): Int {
       return when (menuItemId) {
           R.id.menu_home -> R.drawable.ic_home_normal
           R.id.menu_profile -> R.drawable.ic_profile_normal
           else -> 0
       }
   }
   ```


### **方案二：服务器动态下发图标**
**原理**：通过服务器配置节日图标，应用启动时拉取配置并更新。

#### **实现步骤**
1. **服务器配置**：
    - 后端维护节日图标配置表，包含节日日期、图标URL等信息。

2. **客户端拉取配置**：
   ```kotlin
   // 从服务器获取节日配置
   suspend fun fetchFestivalConfig(): FestivalConfig {
       return apiService.getFestivalConfig() // Retrofit 请求
   }
   
   // 应用配置并更新图标
   private fun applyFestivalConfig(config: FestivalConfig) {
       if (config.isFestivalActive()) {
           // 下载并缓存图标
           downloadAndCacheIcons(config.icons)
           
           // 更新导航栏
           updateBottomNavigationIcons(config.icons)
       }
   }
   ```

3. **图标下载与缓存**：
   ```kotlin
   private suspend fun downloadAndCacheIcons(iconUrls: List<String>) {
       withContext(Dispatchers.IO) {
           iconUrls.forEach { url ->
               val file = File(cacheDir, "icon_${url.hashCode()}")
               if (!file.exists()) {
                   // 使用OkHttp下载图标
                   val response = okHttpClient.newCall(Request.Builder().url(url).build()).execute()
                   if (response.isSuccessful) {
                       response.body?.byteStream()?.use { input ->
                           file.outputStream().use { output ->
                               input.copyTo(output)
                           }
                       }
                   }
               }
           }
       }
   }
   ```


### **方案三：SVG动态渲染**
**原理**：使用SVG矢量图，通过修改填充色或路径实现图标变化。

#### **实现步骤**
1. **准备SVG资源**：
    - 在`res/drawable`目录下放置可变色的SVG图标（如`ic_home.svg`）。

2. **动态修改SVG属性**：
   ```kotlin
   // 获取SVGDrawable并修改属性
   val svgDrawable = VectorDrawableCompat.create(resources, R.drawable.ic_home, theme)
   svgDrawable?.setColorFilter(
       ContextCompat.getColor(this, R.color.christmas_red),
       PorterDuff.Mode.SRC_IN
   )
   
   // 应用到导航栏
   bottomNavigationView.menu.getItem(0).icon = svgDrawable
   ```


### **方案四：Lottie动画图标**
**原理**：使用Lottie动画库加载JSON格式的动画图标，根据节日切换不同动画。

#### **实现步骤**
1. **添加依赖**：
   ```groovy
   implementation 'com.airbnb.android:lottie:6.1.0'
   ```

2. **准备Lottie动画文件**：
    - 在`assets`目录下放置不同节日的动画文件（如`home_normal.json`、`home_christmas.json`）。

3. **替换导航栏图标为LottieView**：
   ```xml
   <!-- 在布局文件中使用LottieView替代普通图标 -->
   <com.airbnb.lottie.LottieAnimationView
       android:id="@+id/nav_icon_home"
       android:layout_width="24dp"
       android:layout_height="24dp"
       app:lottie_fileName="home_normal.json"
       app:lottie_autoPlay="false"
       app:lottie_loop="false" />
   ```

4. **根据节日切换动画**：
   ```kotlin
   val lottieView = findViewById<LottieAnimationView>(R.id.nav_icon_home)
   val animationFile = if (isChristmas()) "home_christmas.json" else "home_normal.json"
   lottieView.setAnimation(animationFile)
   lottieView.playAnimation()
   ```


### **注意事项**
1. **性能优化**：
    - 避免在主线程进行图标下载或复杂渲染
    - 使用内存缓存或磁盘缓存存储图标

2. **兼容性**：
    - 确保所有图标资源在不同屏幕密度下正常显示
    - 测试不同 Android 版本的兼容性

3. **用户体验**：
    - 添加图标切换动画，提升视觉体验
    - 提供手动切换回默认图标的选项

通过以上方案，你可以实现底部导航栏根据节日自动切换图标的功能，为用户带来更丰富的视觉体验。

为了实现Ktor服务器的模块化设计，可以将不同功能的路由拆分成独立的文件。以下是实现方案：


### **1. 项目结构**
```
com.example.server
├── KtorServer.kt        // 服务器主类
├── routes/              // 路由模块
│   ├── LoginRouter.kt   // 登录路由
│   ├── RegisterRouter.kt// 注册路由
│   └── EmailRouter.kt   // 邮箱路由
├── models/              // 数据模型
│   ├── User.kt          // 用户模型
│   └── RequestResponse.kt // 请求/响应模型
├── services/            // 业务逻辑
│   ├── EmailService.kt  // 邮箱服务
│   └── AuthService.kt   // 认证服务
└── utils/               // 工具类
    ├── PasswordUtils.kt // 密码工具
    └── CodeManager.kt   // 验证码管理
```


### **2. 路由模块实现**

#### **LoginRouter.kt**
```kotlin
package com.example.server.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.server.models.*
import com.example.server.services.*

fun Route.loginRoutes(authService: AuthService) {
    route("/auth") {
        // 登录接口
        post("/login") {
            val request = call.receive<LoginRequest>()
            val result = authService.login(request)
            call.respond(result)
        }
    }
}
```

#### **RegisterRouter.kt**
```kotlin
package com.example.server.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.server.models.*
import com.example.server.services.*

fun Route.registerRoutes(authService: AuthService) {
    route("/auth") {
        // 注册接口
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val result = authService.register(request)
            call.respond(result)
        }
    }
}
```

#### **EmailRouter.kt**
```kotlin
package com.example.server.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.server.models.*
import com.example.server.services.*

fun Route.emailRoutes(emailService: EmailService) {
    route("/email") {
        // 发送验证码
        post("/sendCode") {
            val request = call.receive<EmailRequest>()
            val result = emailService.sendVerificationCode(request.email)
            call.respond(result)
        }
    }
}
```


### **3. 服务层实现**

#### **AuthService.kt**
```kotlin
package com.example.server.services

import com.example.server.models.*
import com.example.server.utils.*
import kotlinx.coroutines.*

class AuthService(
    private val emailService: EmailService,
    private val passwordUtils: PasswordUtils,
    private val userRepository: UserRepository // 假设存在用户仓库
) {
    // 注册用户
    suspend fun register(request: RegisterRequest): Response {
        // 验证输入
        if (request.username.isNullOrBlank() || 
            request.password.isNullOrBlank() ||
            request.email.isNullOrBlank() ||
            request.code.isNullOrBlank()) {
            return Response(false, "所有字段均为必填项")
        }
        
        // 验证邮箱格式
        if (!isValidEmail(request.email)) {
            return Response(false, "邮箱格式不正确")
        }
        
        // 验证验证码
        if (!VerificationCodeManager.verifyCode(request.email, request.code)) {
            return Response(false, "验证码无效或已过期")
        }
        
        // 检查用户是否已存在
        if (userRepository.existsByUsername(request.username)) {
            return Response(false, "用户名已存在")
        }
        
        // 加密密码
        val (hash, salt) = passwordUtils.hashPassword(request.password)
        
        // 创建用户
        val user = User(
            username = request.username,
            email = request.email,
            passwordHash = hash,
            salt = salt
        )
        
        // 保存用户
        userRepository.save(user)
        
        return Response(true, "注册成功")
    }
    
    // 登录验证
    suspend fun login(request: LoginRequest): LoginResponse {
        // 验证输入
        if (request.username.isNullOrBlank() || request.password.isNullOrBlank()) {
            return LoginResponse(false, "用户名和密码不能为空")
        }
        
        // 查找用户
        val user = userRepository.findByUsername(request.username) 
            ?: return LoginResponse(false, "用户不存在")
        
        // 验证密码
        if (!passwordUtils.verifyPassword(request.password, user.passwordHash, user.salt)) {
            return LoginResponse(false, "密码错误")
        }
        
        // 生成令牌
        val token = generateJwtToken(user.username)
        
        return LoginResponse(true, "登录成功", token)
    }
    
    // 邮箱格式验证
    private fun isValidEmail(email: String): Boolean {
        val pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
        return email.matches(pattern.toRegex())
    }
    
    // 生成 JWT 令牌
    private fun generateJwtToken(username: String): String {
        // 实际项目中应使用 JWT 库
        return "Bearer $username:${System.currentTimeMillis()}"
    }
}
```


### **4. 主服务器类**

#### **KtorServer.kt**
```kotlin
package com.example.server

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import com.example.server.routes.*
import com.example.server.services.*
import com.example.server.utils.*
import kotlinx.coroutines.*

object KtorServer {
    private var server: ApplicationEngine? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    // 服务实例
    private val emailService = EmailService()
    private val passwordUtils = PasswordUtils()
    private val userRepository = InMemoryUserRepository() // 内存存储实现
    private val authService = AuthService(emailService, passwordUtils, userRepository)

    fun start(port: Int = 8080) {
        server = embeddedServer(CIO, port = port) {
            // 配置序列化
            install(ContentNegotiation) {
                gson()
            }
            
            // 注册路由
            routing {
                loginRoutes(authService)
                registerRoutes(authService)
                emailRoutes(emailService)
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 5000)
        job.cancel()
    }
}
```


### **5. 数据模型**

#### **models/User.kt**
```kotlin
package com.example.server.models

data class User(
    val id: String = kotlin.random.Random.Default.nextBytes(16).toString(),
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

#### **models/RequestResponse.kt**
```kotlin
package com.example.server.models

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
```


### **6. 优点**
1. **单一职责原则**：每个文件专注于特定功能
2. **可测试性**：服务层可独立测试
3. **可维护性**：代码结构清晰，易于扩展
4. **可复用性**：服务和工具类可在多个模块复用


### **7. 注意事项**
1. **依赖注入**：使用构造函数注入依赖，便于测试和替换实现
2. **异步处理**：耗时操作（如数据库访问）应在协程中执行
3. **错误处理**：添加全局异常处理中间件
4. **配置管理**：敏感信息（如邮箱密码）应从配置文件加载

这种模块化设计使代码更易于管理和扩展，适合中大型项目。