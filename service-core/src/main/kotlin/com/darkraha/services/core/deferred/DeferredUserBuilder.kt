package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.Plugin
import com.darkraha.services.core.job.ProgressData
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * Deferred builder for users, allows to add callbacks and execute job.
 * @author Rahul Verma
 */
interface DeferredUserBuilder<RESULT> : DeferredUserCallbacks<RESULT> {
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
        cb: BiConsumer<ProgressData, JobResponse<RESULT>>
    ): DeferredUserBuilder<RESULT>

    fun plugin(p: Plugin<RESULT>): DeferredUserCallbacks<RESULT>
    fun sync(): DeferredUserCallbacks<RESULT>
    fun async(): DeferredUserCallbacks<RESULT>
}