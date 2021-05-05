package com.darkraha.services.core.service

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.deferred.UserDeferred
import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.Task
import com.darkraha.services.core.worker.WorkerActions
import java.util.function.Supplier

open class ExecutionService : TypedService<Any>(Any::class.java, object : Task<Any>() {
    override fun onTask(params: Any?, workerActions: WorkerActions<*>, jobResponse: JobResponse<*>) {

        when {
            params is () -> Any? -> workerActions.result.tmpResult = params.invoke()

            params is Runnable -> {
                params.run()
                workerActions.result.tmpResult = Unit
            }
            params is Supplier<*> -> {
                workerActions.result.tmpResult = params.get()
            }
        }
    }
}) {

    /**
     * Prepare task for execute code block without parameters.
     */
    fun <T> prepareExe(block: () -> T): Deferred<Any, T> {
        val a: Deferred<Any, T> = newTypedDeferred(block, null)
        a.apply {
            job.params = block
        }
        return a
    }

    /**
     * Prepare task for execute code block with single parameter.
     */
    fun <T> prepareExeP(block: (Any?) -> T?): Deferred<Any, T> {
        val a: Deferred<Any, T> = newTypedDeferred(block, null)
        a.apply {
            job.exeCode = block
        }
        return a
    }

    fun prepareExe(runnable: Runnable): Deferred<Any, Unit> {
        val a: Deferred<Any, Unit> = newTypedDeferred(runnable, null)
        a.apply {
            job.params = runnable
        }
        return a
    }

    fun exe(runnable: Runnable): UserDeferred<Unit> {
        val a: Deferred<Any, Unit> = newTypedDeferred(runnable, null)
        a.apply {
            job.params = runnable
        }
        return a.async()
    }

    fun <T> exe(block: () -> T): UserDeferred<T> {
        val a: Deferred<Any, T> = newTypedDeferred(block, null)
        a.apply {
            job.params = block
        }
        return a.async()
    }
}