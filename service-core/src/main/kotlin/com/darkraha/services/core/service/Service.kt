package com.darkraha.services.core.service

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.utils.Common
import com.darkraha.services.core.worker.Worker
import com.darkraha.services.core.worker.WorkerA
import java.text.SimpleDateFormat
import java.util.concurrent.*

/**
 * Base class to perform job. Child classes provides methods to create Deferred object which will be associated with job.
 * By default Worker class will be used to execute job in background. It uses thread pool.
 *
 */
open class Service {
    protected var defaultWorker: WorkerA? = null


    open fun checkSetup(): Boolean {
        return defaultWorker != null
    }

    protected open fun setupDefault() {
        defaultWorker = defaultWorker ?: Worker(newExecutorService(5))
    }

    open fun <PARAM, RESULT> newDeferred(clsParam: Class<PARAM>, clsResult: Class<RESULT>?) =
        Deferred<PARAM, RESULT>(clsResult).apply {
            if (defaultWorker == null) {
                setupDefault()
            }
            worker = defaultWorker!!
        }

    companion object {
        val w3cDateTimeFormat: SimpleDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX") }
        val w3cDateFormat: SimpleDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd") }

        val sharedThreadWorker: WorkerA by lazy { Worker(newExecutorService(10)) }

        fun newExecutorService(maxCore: Int, q: BlockingQueue<Runnable> = LinkedBlockingQueue()): ExecutorService {
            return ThreadPoolExecutor(0, maxCore, 2L, TimeUnit.MINUTES, q)
        }

        fun newThreadWorker(cntThreads: Int = 5): Worker {
            return Worker(newExecutorService(cntThreads))
        }
    }


}