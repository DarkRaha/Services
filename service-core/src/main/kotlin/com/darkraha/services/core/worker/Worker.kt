/**
 * Interfaces and classes to perform job.
 *
 * @author Rahul Verma
 */
package com.darkraha.services.core.worker

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.utils.GlobalLock
import com.darkraha.services.core.worker.executors.TPoolWorkerExecutor
import com.darkraha.services.core.worker.executors.WorkerExecutor
import java.util.concurrent.ExecutorService

/**
 *
 */
class Worker(var exe: WorkerExecutor<*>) : WorkerA() {
    constructor(e: ExecutorService) : this(TPoolWorkerExecutor(e))

    override fun <PARAMS, RESULT> sync(deferred: Deferred<PARAMS, RESULT>) {
        workflow(deferred)
    }

    override fun <PARAMS, RESULT> async(deferred: Deferred<PARAMS, RESULT>) {
        exe.execute {
            workflow(deferred)
        }
    }

    private fun <PARAMS, RESULT> workflow(deferred: Deferred<PARAMS, RESULT>) {

        deferred.apply {

            if (setPending()) {

                doDispatchCallbacks()
                GlobalLock.lock(job.tasks.syncObject)
                performTasks(job.tasks.preProcessors)

                if (isPending()) {
                    try {
                        job.exeCode?.also {
                            job.result.tmpResult = it.invoke(job.params)
                        }
                        job.tasks.main?.onTask(job.params, this, job)
                    } catch (e: Exception) {
                        setError(e)
                        e.printStackTrace()
                    }
                }
                performTasks(job.tasks.postProcessors)
                GlobalLock.unlock(job.tasks.syncObject)
            }
            if (isPending()) {
                setSuccess()
            }

            doDispatchCallbacks()
            notifyFinished()
            chainNext?.startNext()
        }
    }
}
