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