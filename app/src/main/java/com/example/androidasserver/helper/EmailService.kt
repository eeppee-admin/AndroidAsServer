package com.example.androidasserver.helper

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.*
import javax.inject.Singleton

@Singleton
class EmailService(
    private val emailConfig: EmailConfig
) {
    // 生成随机验证码
    fun generateVerificationCode(length: Int = 6): String {
        val charset = "0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    // 发送验证码邮件
    suspend fun sendVerificationCode(email: String, code: String): Boolean =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            return@withContext try {
                // 配置 SMTP 服务器
                val properties = Properties().apply {
                    put("mail.smtp.host", emailConfig.host)
                    put("mail.smtp.port", emailConfig.port)
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.ssl.trust", emailConfig.host)
                }

                // 创建会话
                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(emailConfig.username, emailConfig.password)
                    }
                })

                // 创建邮件
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(emailConfig.username))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                    subject = "【AndroidAsServer】验证码"
                    setText("您的验证码是：$code\n有效期5分钟，请不要泄露给他人。")
                }

                // 发送邮件
                jakarta.mail.Transport.send(message)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
}

// 邮箱配置类
data class EmailConfig(
    val host: String = "smtp.qq.com",
    val port: String = "587",
    val username: String = "1690544550@qq.com",  // QQ邮箱地址
    val password: String   // QQ邮箱SMTP授权码
)