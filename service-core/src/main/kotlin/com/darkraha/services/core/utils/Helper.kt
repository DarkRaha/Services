package com.darkraha.services.core.utils

import com.darkraha.services.core.deferred.DeferredAbstractFactory
import java.text.SimpleDateFormat
import java.util.concurrent.*

object Helper {
    val w3cDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
    val w3cDateFormat = SimpleDateFormat("yyyy-MM-dd")

    var sharedDeferredAbstractFactory = DeferredAbstractFactory()

    fun newExecutorService(maxCore: Int, q: BlockingQueue<Runnable> = LinkedBlockingQueue()): ExecutorService {
        return ThreadPoolExecutor(
            0, maxCore, 2L, TimeUnit.MINUTES,
            q
        )
    }
}