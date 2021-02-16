/**
 * Interfaces to build deferred object by services and users.
 *
 * @author Rahul Verma
 */

package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.Plugin
import com.darkraha.services.core.job.ProgressData
import java.io.File
import java.util.function.BiConsumer
import java.util.function.Consumer
/**
 * Deferred builder for services, allows to add callbacks and execute job.
 * @author Rahul Verma
 */
interface DeferredServiceBuilder<RESULT> : DeferredUserBuilder<RESULT> {
    override fun onBeforeStart(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT>

    override fun onSuccess(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT>

    override fun onCancel(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT>

    override fun onError(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT>

    override fun onFinish(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT>

    override fun onProgress(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: BiConsumer<ProgressData, JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT>

    override fun plugin(p: Plugin<RESULT>): DeferredServiceBuilder<RESULT>

    fun setId(id: Long): DeferredServiceBuilder<RESULT>
    fun setCmd(cmd: String?): DeferredServiceBuilder<RESULT>
    fun setIdObject(idObj: Any?): DeferredServiceBuilder<RESULT>

    /**
     * Can be used on request stage to specify the  expected mimetype.
     */
    fun setResultMimetype(mimetype: String?): DeferredServiceBuilder<RESULT>
    /**
     * Can be used on request stage to specify the  expected mimetype.
     */
    fun setResultFile(file: File?): DeferredServiceBuilder<RESULT>
}


