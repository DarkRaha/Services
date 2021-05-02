package com.darkraha.services.core.utils

import java.text.SimpleDateFormat
import java.util.concurrent.*


@Deprecated("see Service companion")
object Common {
    val w3cDateTimeFormat : SimpleDateFormat by lazy{SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")}
    val w3cDateFormat : SimpleDateFormat by lazy{SimpleDateFormat("yyyy-MM-dd")}

    fun newExecutorService(maxCore: Int, q: BlockingQueue<Runnable> = LinkedBlockingQueue()): ExecutorService {
        return ThreadPoolExecutor(
            0, maxCore, 2L, TimeUnit.MINUTES,
            q
        )
    }
}