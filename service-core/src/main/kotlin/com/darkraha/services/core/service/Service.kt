package com.darkraha.services.core.service

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.utils.Common
import com.darkraha.services.core.worker.Worker
import com.darkraha.services.core.worker.WorkerA

open class Service {
    protected var defaultWorker: WorkerA? = null


    open fun checkSetup(): Boolean {
        return defaultWorker != null
    }

    protected open fun setupDefault() {
        defaultWorker = defaultWorker ?: Worker(Common.newExecutorService(5))
    }

    open fun <PARAM, RESULT> newDeferred(clsParam: Class<PARAM>, clsResult: Class<RESULT>) =
        Deferred<PARAM, RESULT>(clsResult).apply {
            if (defaultWorker==null) {
                setupDefault()
            }
            worker = defaultWorker!!
            workerHelper = defaultWorker!!.newHelper(this)
        }

}