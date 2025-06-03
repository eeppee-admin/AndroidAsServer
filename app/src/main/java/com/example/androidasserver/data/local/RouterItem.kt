package com.example.androidasserver.data.local

import com.example.androidasserver.helper.endpoints

/**
 * 整个app提供的路由信息
 */
data class RouterItem(
    val url: String,
    val desc: String,
) {
    companion object {
        // 通过DSL处理recycler view里面的数据
        val endPoints = endpoints {
            post("/login", "登录by ktor")
            post("/register", "注册by ktor")
            get("/hello", "返回hello world")
            get("/sayHi/{name}", "返回Hi {name}")
            post("/uploadLog", "上传的内容打印日志")
            get("/downloadFile", "下载指定文件")
            get("/test", "获取test.html文件，assets")
            fileUpload("/uploadFile")
            moreInfo("https://github.com/eeppee-admin", "更多信息请查看")
        }
    }
}