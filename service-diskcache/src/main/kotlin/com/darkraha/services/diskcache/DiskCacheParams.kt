package com.darkraha.services.diskcache

import java.io.File
import java.util.concurrent.TimeUnit

class DiskCacheParams {
    var oldTime = TimeUnit.DAYS.toMillis(10)
    var key: String? = null
    var keys: List<String>? = null
    var source: File? = null
    lateinit var destination: File
}