package com.darkraha.services.core.service

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.deferred.DeferredAbstractFactory
import com.darkraha.services.core.deferred.DeferredFactory
import com.darkraha.services.core.utils.Helper
import com.darkraha.services.core.worker.Worker
import java.util.concurrent.*

open class Service<PARAM> {
    protected var deferredFactory: DeferredFactory<PARAM>? = null
    protected var mainWorker: Worker<PARAM>? = null
    protected var executorService: ExecutorService? = null
        set(value) {
            field?.apply { shutdown() }
            field = value ?: Helper.newExecutorService(5)
        }

    protected open fun setupDefault() {
        deferredFactory = deferredFactory ?: Helper.sharedDeferredAbstractFactory.newDeferredFactory(this)
        executorService = executorService ?: Helper.newExecutorService(5)
    }

    open fun <RESULT> newDeferred(cls: Class<RESULT>?): Deferred<PARAM, RESULT> =
        deferredFactory!!.newDeferred(cls).also {
            setupDeferred(cls, it)
        }

    open fun <RESULT> setupDeferred(cls: Class<RESULT>?, d: Deferred<PARAM, RESULT>) {
        d.job.result.clsTarget = cls
        d.worker = mainWorker
        d.executor = executorService
    }

     class Builder<PARAM> {
         private val srv = Service<PARAM>()
         fun mainWorker(w: Worker<PARAM>): Builder<PARAM> = this.apply { srv.mainWorker = w }
         fun executor(e: ExecutorService): Builder<PARAM> = this.apply { srv.executorService = e }
         fun deferredFactory(df: DeferredFactory<PARAM>): Builder<PARAM> = this.apply { srv.deferredFactory = df }
         fun build(): Service<PARAM> {
            srv.setupDefault()
            return srv
        }
    }



}