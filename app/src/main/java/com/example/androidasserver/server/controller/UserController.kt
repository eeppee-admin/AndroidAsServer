package com.example.androidasserver.server.controller

import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.RestController

/**
 * todo: AndServer Style Code
 */
@RestController
@RequestMapping(path = ["/user"])
class UserController {
    @PostMapping("/login")
    fun login(
        @RequestParam("account")
        account: String,
        @RequestParam("password")
        password: String
    ): String {
        return "Successfully"
    }
}