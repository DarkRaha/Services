package com.darkraha.services.core.worker

import com.darkraha.services.core.job.JobNotifyProgress
import com.darkraha.services.core.job.JobResult
import com.darkraha.services.core.deferred.Deferred
import java.io.File
import java.io.InputStream

/**
 * Actions that can do worker.
 *
 *@author Rahul Verma
 */
interface WorkerActions<RESULT> : JobNotifyProgress {
    /**
     * Hold result of job.
     */
    val result: JobResult<RESULT>


    /**
     * Set job to pending state, job started in background.
     */
    fun setPending(): Boolean

    /**
     * Set job to the success state, override result data.
     */
    fun setSuccessWithData(value: RESULT?, file: File? = null, url: String? = null, istream: InputStream? = null)

    /**
     * Set job to the success state, save current result data.
     */
    fun setSuccess()


    /**
     * Set job to the error state.
     */
    fun setError(exception: Exception? = null, reason: String = exception?.message ?: Deferred.ERR_REASON_DFAULT)

    /**
     * Reject job for some non-error reason.
     */
    fun setReject(reason: String = Deferred.CANCEL_REASON_SERVICE)

    fun doDispatchCallbacks()

}
