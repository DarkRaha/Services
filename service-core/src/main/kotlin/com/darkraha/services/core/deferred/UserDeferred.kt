package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.JobCallbacks
import com.darkraha.services.core.job.JobResponse
import java.util.function.Consumer

/**
 * Interface for adding callbacks by user.
 *
 * @author Rahul Verma
 */
interface UserDeferred<RESULT> {
    fun onBeforeStart(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): UserDeferred<RESULT>

    fun onSuccess(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): UserDeferred<RESULT>

    fun onCancel(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): UserDeferred<RESULT>

    fun onError(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): UserDeferred<RESULT>

    fun onFinish(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): UserDeferred<RESULT>

    fun onProgress(
        objWeak: Any? = null,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ): UserDeferred<RESULT>

    fun subscribe(cb: JobCallbacks<RESULT>): UserDeferred<RESULT>

    fun removeCallbacks(objWeak: Any?)

    fun getResponse(): JobResponse<RESULT>

    fun await(): JobResponse<RESULT>
}


