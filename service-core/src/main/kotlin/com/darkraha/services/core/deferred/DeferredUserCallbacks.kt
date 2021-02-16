package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.ProgressData
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * Interface for adding callbacks by user.
 *
 * @author Rahul Verma
 */
interface DeferredUserCallbacks<RESULT> : JobResponse<RESULT> {
    fun onBeforeStart(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserCallbacks<RESULT>

    fun onSuccess(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserCallbacks<RESULT>

    fun onCancel(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserCallbacks<RESULT>

    fun onError(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserCallbacks<RESULT>

    fun onFinish(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserCallbacks<RESULT>

    fun onProgress(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserCallbacks<RESULT>

    fun removeCallbacks(objWeak: Any)
}


