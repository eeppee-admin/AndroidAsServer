package com.example.androidasserver.server

import android.content.Context
import com.safframework.server.core.AndroidServer
import com.safframework.server.core.http.Request
import com.safframework.server.core.http.Response
import com.safframework.server.core.http.filter.HttpFilter
import com.safframework.server.core.log.LogManager
import java.io.File

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
        .get("/downloadFile") { request, response: Response ->
            val fileName = "xxx.txt"
            File("/sdcard/$fileName").takeIf { it.exists() }?.let {
                response.sendFile(it.readBytes(), fileName, "application/octet-stream")
            } ?: response.setBodyText("no file found")
        }
        .get("/test") { _, response: Response ->
            response.html(context, "test")
        }
        .fileUpload("/uploadFile") { request, response: Response -> // curl -v -F "file=@/Users/tony/1.png" 10.184.18.14:8080/uploadFile

            val uploadFile = request.file("file")
            val fileName = uploadFile.fileName
            val f = File("/sdcard/$fileName")
            val byteArray = uploadFile.content
            f.writeBytes(byteArray)

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