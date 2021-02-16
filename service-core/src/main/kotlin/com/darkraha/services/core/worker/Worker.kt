/**
 * Interfaces and classes to perform job.
 *
 * @author Rahul Verma
 */
package com.darkraha.services.core.worker

import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.JobState
import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.utils.GlobalLock
import java.util.concurrent.atomic.AtomicInteger


/**
 * Step of job.
 */

fun interface Task<PARAMS> {
    fun onTask(params: PARAMS?, workerActions: WorkerActions, jobResponse: JobResponse<*>)
}

/**
 * It is assumed that the worker will be fully prepared before use.
 */
open class Worker<PARAMS> {
    protected val jobsCount = AtomicInteger()
    protected var taskPreProcessors: MutableList<Task<PARAMS>>? = null
    protected var taskPostProcessors: MutableList<Task<PARAMS>>? = null
    protected var mainTask: Task<PARAMS>? = null

    open fun getJobWorkflow(deferred: Deferred<PARAMS, *>): (deferred: Deferred<PARAMS, *>) -> Unit {
        jobsCount.incrementAndGet()
        return this::workflow
    }

    open fun workflow(deferred: Deferred<PARAMS, *>) {
        if (startJob(deferred.job.params, deferred, deferred)) {
            GlobalLock.lock(deferred.globalSyncObject)
            doJob(deferred.job.params, deferred, deferred)
            GlobalLock.unlock(deferred.globalSyncObject)
            finishJob(deferred.job.params, deferred, deferred)
        }
    }

    protected fun performTasks(
        params: PARAMS?,
        workerActions: WorkerActions,
        jobResponse: JobResponse<*>,
        tasks: List<Task<PARAMS>>?
    ) {

        tasks?.forEach {
            if (jobResponse.getState() == JobState.PENDING) {
                performTask(params, workerActions, jobResponse, it)
            } else return
        }
    }

    protected fun performTask(
        params: PARAMS?,
        workerActions: WorkerActions,
        jobResponse: JobResponse<*>,
        task: Task<PARAMS>
    ) {

        if (jobResponse.getState() == JobState.PENDING) {
            try {
                task.onTask(params, workerActions, jobResponse)
            } catch (e: Exception) {
                workerActions.error(e)
                e.printStackTrace()
            }
        }
    }

    protected open fun startJob(
        params: PARAMS?,
        workerActions: WorkerActions,
        jobResponse: JobResponse<*>
    ): Boolean {
        return workerActions.pending().apply {
            workerActions.dispatchCallbacks()
        }
    }

    protected open fun doJob(
        params: PARAMS?,
        workerActions: WorkerActions,
        jobResponse: JobResponse<*>
    ) {
        performTasks(params, workerActions, jobResponse, taskPreProcessors)
        mainTask?.apply { performTask(params, workerActions, jobResponse, this) }
        performTasks(params, workerActions, jobResponse, taskPostProcessors)
    }

    protected open fun finishJob(
        params: PARAMS?,
        workerActions: WorkerActions,
        jobResponse: JobResponse<*>
    ) {
        if (jobResponse.getState() == JobState.PENDING) {
            workerActions.success()
        }
        jobsCount.decrementAndGet()
        workerActions.dispatchCallbacks()
        workerActions.notifyFinished()
    }

    open fun onPreProcess(task: Task<PARAMS>): Worker<PARAMS> {
        (taskPreProcessors ?: ArrayList()).apply {
            taskPreProcessors = this
            add(task)
        }
        return this
    }

    open fun onPostProcess(task: Task<PARAMS>): Worker<PARAMS> {
        (taskPostProcessors ?: ArrayList()).apply {
            taskPostProcessors = this
            add(task)
        }
        return this
    }

    open fun onMainTask(task: Task<PARAMS>): Worker<PARAMS> {
        mainTask = task
        return this
    }
}

