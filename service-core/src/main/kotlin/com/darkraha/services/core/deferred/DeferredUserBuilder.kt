package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.JobCallbacks
import com.darkraha.services.core.job.JobResponse
import java.util.function.Consumer

/**
 * Deferred builder for users, allows to add callbacks and execute job.
 * @author Rahul Verma
 */
interface DeferredUserBuilder<RESULT> : UserDeferred<RESULT> {
    override fun onBeforeStart(
        objWeak: Any? ,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserBuilder<RESULT>

    override fun onSuccess(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserBuilder<RESULT>

    override fun onCancel(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserBuilder<RESULT>

    override fun onError(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserBuilder<RESULT>

    override fun onFinish(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserBuilder<RESULT>

    override fun onProgress(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredUserBuilder<RESULT>

    override fun subscribe(cb: JobCallbacks<RESULT>): DeferredUserBuilder<RESULT>

    fun sync(): UserDeferred<RESULT>
    fun async(): UserDeferred<RESULT>
}