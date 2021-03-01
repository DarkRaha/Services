/**
 * Interfaces and classes to perform job.
 *
 * @author Rahul Verma
 */
package com.darkraha.services.core.worker

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.job.Task
import com.darkraha.services.core.utils.GlobalLock
import com.darkraha.services.core.worker.executors.TPoolWorkerExecutor
import com.darkraha.services.core.worker.executors.WorkerExecutor
import java.util.concurrent.ExecutorService


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
            if (workerHelper.pending()) {
                workerHelper.dispatchCallbacks()
                GlobalLock.lock(job.tasks.syncObject)
                performTasks(this, job.tasks.preProcessors)
                if (workerHelper.isPending()) {
                    try {
                        job.tasks.main?.onTask(job.params, workerHelper, job)
                    } catch (e: Exception) {
                        workerHelper.error(e)
                        e.printStackTrace()
                    }
                }
                performTasks(this, job.tasks.postProcessors)
                GlobalLock.unlock(job.tasks.syncObject)
            }
            if (workerHelper.isPending()) {
                workerHelper.success()
            }

            workerHelper.dispatchCallbacks()
            workerHelper.notifyFinished()
        }
    }


    override fun <PARAMS, RESULT> newHelper(deferred: Deferred<PARAMS, RESULT>): WorkerHelperA<PARAMS, RESULT> {
        return WorkerHelper(deferred)
    }


    private fun <PARAMS, RESULT> performTasks(
        deferred: Deferred<PARAMS, RESULT>,
        tasks: List<Task<PARAMS>>?
    ) {
        deferred.apply {
            if (workerHelper.isPending()) {
                tasks?.forEach {
                    if (workerHelper.isPending()) {
                        try {
                            it.onTask(job.params, workerHelper, job)
                        } catch (e: Exception) {
                            workerHelper.error(e)
                            e.printStackTrace()
                        }
                    } else return
                }
            }
        }
    }
}
