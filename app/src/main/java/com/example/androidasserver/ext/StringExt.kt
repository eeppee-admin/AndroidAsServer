package com.example.androidasserver.ext

/**
 * 检查String的是否是Int数字
 */
fun String.checkIsInt(): Boolean {
    try {
        Integer.parseInt(this)
        return true
    } catch (e: NumberFormatException) {
        return false
    }
}