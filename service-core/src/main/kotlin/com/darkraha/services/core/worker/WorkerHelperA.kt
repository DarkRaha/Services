package com.darkraha.services.core.worker

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.job.JobCallbacks
import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.JobState
import java.util.function.Consumer

/**
 * Helper class for worker that responses for synchronization and worker actions.
 * @author Rahul Verma
 */
abstract class WorkerHelperA<PARAMS, RESULT>(val deferred: Deferred<PARAMS, RESULT>) : WorkerActions<RESULT> {

    abstract fun addCallback(
        jobState: JobState,
        objWeak: Any?,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    )
    abstract fun addSubscribe(cb: JobCallbacks<RESULT>)
    abstract fun removeCallbacks(objWeak: Any?)
    abstract fun await(): JobResponse<RESULT>
    abstract fun notifyFinished()

    fun isPending() = deferred.job.state.get() == JobState.PENDING
}