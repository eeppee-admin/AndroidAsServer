package com.example.androidasserver.helper


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.androidasserver.R
import com.example.androidasserver.ui.activity.MainActivity

/**
 * 以下context都是Activity Context
 * Example Usage: 我认为你已经授权 POST_NOTIFICATION
 * 1. 第一步先创建channel
 *                     LocalNotificationHelper.createNotificationChannel(
 *                         this@MainActivity,
 *                         "Android As Server",
 *                         "View Your Android As A Server That can Deploy Service"
 *                     )
 * 2. 第二步发送通知
 *                    LocalNotificationHelper.showNotification(
 *                         this@MainActivity,
 *                         1,
 *                         "haha",
 *                         "haha",
 *                         R.drawable.ic_launcher_background
 *                     )
 *
 */
object LocalNotificationHelper {
    private const val CHANNEL_ID = "channel1"

    fun createNotificationChannel(context: Context, channelName: String, desc: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = channelName
            val descriptionText = desc
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun justShowOneDefault(context: Context, title: String, content: String, whichNum: Int = 1) {
        createNotificationChannel(context, "DEFAULT", "DEFAULT")
        showNotification(context, whichNum, title, content, R.drawable.ic_launcher_background)
    }

    fun justCancelOneDefault(context: Context, whichNum: Int = 1) {
        cancelNotification(context, whichNum)
    }

    /**
     * 挂一条本地通知，和showNotification效果一样
     */
    @SuppressLint("MissingPermission")
    fun sendNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String,
        smallIcon: Int
    ) {
        // 检查通知权限（仅适用于 Android 13 及以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(context).areNotificationsEnabled()
        ) {
            // 如果没有权限，可以引导用户去设置页面开启
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
            return
        }

        // 创建通知构建器
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(content)
//            .Priorityset(NotificationCompat.PRIORITY_DEFAULT)
            // 设置点击通知后的意图
            .setContentIntent(createPendingIntent(context))
            .setAutoCancel(true) // 点击后自动取消通知

        // 显示通知,todo:这里自己分配权限
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    /**
     * 挂一条本地通知
     */
    @SuppressLint("MissingPermission")
    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String,
        smallIcon: Int
    ) {
        // 检查通知权限（仅适用于 Android 13 及以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(context).areNotificationsEnabled()
        ) {
            // 如果没有权限，可以引导用户去设置页面开启
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
            return
        }

        // 创建通知构建器
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(content)
//            .Priorityset(NotificationCompat.PRIORITY_DEFAULT)
            // 设置点击通知后的意图
            .setContentIntent(createPendingIntent(context))
            .setAutoCancel(true) // 点击后自动取消通知

        // 显示通知,todo:这里自己分配权限
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }


    /**
     * 取消本地通知
     * whichNum是notificationId
     */
    fun cancelNotification(context: Context, whichNum: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(whichNum)
        }
    }

    /**
     *
     */
    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}