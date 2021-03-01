package com.darkraha.services.core.worker

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.job.*
import java.io.File
import java.io.InputStream
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import kotlin.concurrent.withLock

class WorkerHelper<PARAMS, RESULT>(deferred: Deferred<PARAMS, RESULT>) : WorkerHelperA<PARAMS, RESULT>(deferred) {

    val jobLock = ReentrantLock()
    val condFinished = jobLock.newCondition()
    // val future

    override val result: JobResult<RESULT>
        get() = deferred.job.result

    override fun pending(): Boolean = jobLock.withLock { deferred.job.setPending() }

    override fun dispatchCallbacks() = jobLock.withLock {
        val state = deferred.job.state.get()
        deferred.job.doDispatchCallbacks(state)

        if (state.isFinished()) {
            deferred.job.doDispatchCallbacks(JobState.FINISHED)
        }
    }

    override fun success() {
        jobLock.lock()
        deferred.job.setSuccess()
        jobLock.unlock()
    }

    override fun error(exception: Exception?, reason: String) {
        jobLock.lock()
        deferred.job.setError(exception, reason)
        jobLock.unlock()
    }

    override fun reject(reason: String) {
        jobLock.lock()
        deferred.job.setRejected(reason)
        jobLock.unlock()
    }

    override fun successWithData(value: RESULT?, file: File?, url: String?, istream: InputStream?) {
        jobLock.lock()
        deferred.job.setSuccess(value, file, url, istream)
        jobLock.unlock()
    }

    override fun notifyProgress(dataProgress: ProgressData) {
        jobLock.withLock {
            deferred.job.apply {
                result.progressData = dataProgress
                doDispatchCallbacks(JobState.PROGRESS)
            }
        }
    }

    override fun addCallback(
        jobState: JobState,
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ) {
        jobLock.withLock {
            deferred.job.doAddCallback(jobState, objWeak, onMainThread, cb)
        }
    }

    override fun addSubscribe(cb: JobCallbacks<RESULT>) {
        jobLock.withLock {
            deferred.job.doAddCallbacks(cb)
        }
    }

    override fun removeCallbacks(objWeak: Any?) {
        objWeak?.also {
            jobLock.lock()
            deferred.job.doRemoveCallbacks(it)
            jobLock.unlock()
        }
    }

    override fun await(): JobResponse<RESULT> {
        jobLock.withLock {
            if (!deferred.job.state.get().isFinished()) {
                condFinished.await()
            }
        }
        return deferred.job
    }

    override fun notifyFinished() {
        jobLock.withLock {
            condFinished.signalAll()
        }
    }
}