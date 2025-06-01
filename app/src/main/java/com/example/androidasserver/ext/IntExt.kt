package com.example.androidasserver.ext



/**
 * 检查int值是否在常见的端口
 */
fun Int.checkIsCommonServicePort(): Boolean {
    val commonPorts = listOf(80, 3306, 443, 22, 21, 25, 110, 143, 465, 993, 995)
    return this in commonPorts && this in 1..65535
}