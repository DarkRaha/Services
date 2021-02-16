package com.darkraha.services.webcrawler

import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

fun URI.parentPath(): String? {
    return if (path == "/") {
        null
    } else path.lastIndexOf(
        '/',
        if (path.endsWith("/")) path.lastIndex - 1
        else path.lastIndex
    ).let {
        if (it == -1) null
        else path.substring(0, it + 1)
    }
}

fun StringBuilder.appendCnt(ch: Char, count: Int): StringBuilder {
    for (i in 1..count) append(ch)
    return this
}

