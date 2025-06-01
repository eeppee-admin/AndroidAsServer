package com.example.androidasserver.server

import android.content.Context
import com.safframework.server.core.AndroidServer
import com.safframework.server.core.http.Request
import com.safframework.server.core.http.Response
import com.safframework.server.core.http.filter.HttpFilter
import com.safframework.server.core.log.LogManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * AndroidServer 库风格的Server
 * context是Activity Context
 */
fun startHttpServer(context: Context, androidServer: AndroidServer) {
    androidServer
        .get("/hello") { _, response: Response ->
            response.setBodyText("hello world")
        }
        .get("/sayHi/{name}") { request, response: Response ->
            val name = request.param("name")
            response.setBodyText("hi $name!")
        }
        .post("/uploadLog") { request, response: Response ->
            val requestBody = request.content()
            response.setBodyText(requestBody)
        }
        //http GET 192.168.16.156:3333/downloadFile/deploy.cmd
        .get("/downloadFile/{filename}") { request, response: Response ->
            val fileName = request.param("filename")
            // todo: 文件下载
//            File("/sdcard/$fileName").takeIf { it.exists() }?.let {
//                response.sendFile(it.readBytes(), fileName, "application/octet-stream")
//            } ?: response.setBodyText("no file found")
            // 测试Android13通过
            File(context.getExternalFilesDir("MyFiles"), fileName).takeIf { it.exists() }?.let {
                response.sendFile(it.readBytes(), fileName!!, "application/octet-stream")
            } ?: response.setBodyText("no file found")

        }
        .get("/test") { _, response: Response ->
            response.html(context, "test")
        }
        // http -f POST 192.168.16.156:3333/uploadFile file@"C:/Users/fuckerfucker/AndroidStudioProjects/AndroidAsServer/deploy.cmd"
        .fileUpload("/uploadFile") { request, response: Response -> // curl -v -F "file=@/Users/tony/1.png" 10.184.18.14:8080/uploadFile
            val uploadFile = request.file("file")
            val fileName = uploadFile.fileName
//            val f = File("/sdcard/$fileName") // 旧方法
            // 以下Android13测试通过，小米10pro
            // 找到Android/data/com.example.androidasserver/files/MyFiles/deploy.cmd
            val ff = File(context.getExternalFilesDir("MyFiles"), fileName)
            try {
                val fos = FileOutputStream(ff)
                fos.write(uploadFile.content)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val byteArray = uploadFile.content
//            f.writeBytes(byteArray),todo: 好像上面已经写入了?
            ff.writeBytes(byteArray)
            response.setBodyText("upload success")
        }
        .filter("/sayHi/*", object : HttpFilter {
            override fun before(request: Request): Boolean {
                LogManager.d("HttpService", "before....")
                return true
            }

            override fun after(request: Request, response: Response) {
                LogManager.d("HttpService", "after....")
            }

        })
        .start()
}