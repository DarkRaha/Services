/**
 * Interfaces to build deferred object by services and users.
 *
 * @author Rahul Verma
 */

package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.*
import java.io.File
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * Deferred builder for services, allows to add callbacks and execute job.
 * @author Rahul Verma
 */
interface DeferredServiceBuilder<PARAMS, RESULT> : DeferredUserBuilder<RESULT>, JobInfoBuilder {
    override fun onBeforeStart(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT>

    override fun onSuccess(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT>

    override fun onCancel(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT>

    override fun onError(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT>

    override fun onFinish(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT>

    override fun onProgress(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT>

    override fun subscribe(cb: JobCallbacks<RESULT>): DeferredServiceBuilder<PARAMS, RESULT>

    override fun id(v: Long): DeferredServiceBuilder<PARAMS, RESULT>
    override fun cmd(v: String?): DeferredServiceBuilder<PARAMS, RESULT>
    override fun idObject(v: Any?): DeferredServiceBuilder<PARAMS, RESULT>

    fun setMainTask(t: Task<PARAMS>?): DeferredServiceBuilder<PARAMS, RESULT>
    fun setPreProcessors(p: List<Task<PARAMS>>?): DeferredServiceBuilder<PARAMS, RESULT>
    fun setPostProcessors(p: List<Task<PARAMS>>?): DeferredServiceBuilder<PARAMS, RESULT>

    /**
     * Can be used on request stage to specify the  expected mimetype.
     */
    fun setResultMimetype(mimetype: String?): DeferredServiceBuilder<PARAMS, RESULT>

    /**
     * Can be used on request stage to specify the  expected mimetype.
     */
    fun setResultFile(file: File?): DeferredServiceBuilder<PARAMS, RESULT>

    //------------------------------------------------------
    // ui shorthands
    override fun uiSuccess(
        objWeak: Any?,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> = onSuccess(objWeak, true, cb)

    override fun uiError(
        objWeak: Any?,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> = onError(objWeak, true, cb)

    override fun uiProgress(
        objWeak: Any?,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> = onProgress(objWeak, true, cb)

    override fun uiBeforeStart(
        objWeak: Any?,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> = onBeforeStart(objWeak, true, cb)
}


