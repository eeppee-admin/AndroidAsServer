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

    fun createNotificationChannel(
        context: Context,
        channelName: String = "DEFAULT",
        desc: String = "DEFAULT"
    ) {
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

    fun justShowOneDefault(
        context: Context,
        title: String,
        content: String,
        whichNum: Int = 1
    ) {
        createNotificationChannel(context)
        showNotification(
            context = context,
            notificationId = whichNum,
            title = title,
            content = content,
            smallIcon = R.drawable.ic_launcher_background
        )
    }

    fun justCancelOneDefault(context: Context, whichNum: Int = 1) {
        cancelNotification(context, whichNum)
    }

    @SuppressLint("MissingPermission")
    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String,
        smallIcon: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(context).areNotificationsEnabled()
        ) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(createPendingIntent(context))
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun cancelNotification(context: Context, whichNum: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(whichNum)
        }
    }

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("FROM_NOTIFICATION", true)
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @Deprecated(message = "I dont know")
    @SuppressLint("MissingPermission")
    fun showNotificationWithCallback(
        context: Context,
        notificationId: Int,
        title: String,
        content: String,
        smallIcon: Int
    ) {
        createNotificationChannel(context, "NotificationChannel", "This is a notification channel")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("FROM_NOTIFICATION", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}