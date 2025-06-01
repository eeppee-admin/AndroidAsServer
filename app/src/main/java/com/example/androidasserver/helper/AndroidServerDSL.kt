package com.example.androidasserver.helper

import com.example.androidasserver.data.local.RouterItem

/**
 * DSL
 */
class EndPoint {
    private val routerItems = mutableListOf<RouterItem>()

    /**
     * get
     */
    fun get(url: String, desc: String = "") {
        routerItems.add(RouterItem("GET\t\n\t$url", desc))
    }

    /**
     * post
     */
    fun post(url: String, desc: String = "") {
        routerItems.add(RouterItem("POST\t\n\t$url", desc))
    }

    /**
     * 文件上传
     */
    fun fileUpload(url: String, desc: String = "文件上传") {
        routerItems.add(RouterItem("POST\t\n\t$url", desc))
    }

    fun getRouterItems(): List<RouterItem> {
        return routerItems
    }

    /**
     * 更多信息
     */
    fun moreInfo(url: String, desc: String) {
        // 倒转
        routerItems.add(RouterItem(desc, url))
    }
}

fun endpoints(block: EndPoint.() -> Unit): List<RouterItem> {
    val endPoint = EndPoint()
    endPoint.block()
    return endPoint.getRouterItems()
}

/**
 * 测试DSL效果
 */
//fun main() {
//    val routerItems = endpoints {
//        get("/login", "登录")
//        post("/register", "注册")
//    }
//
//    for (item in routerItems) {
//        println("URL: ${item.url}, Description: ${item.desc}")
//    }
//}

