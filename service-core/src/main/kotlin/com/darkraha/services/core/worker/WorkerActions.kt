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
    fun pending(): Boolean

    /**
     * Set job to the success state, override result data.
     */
    fun successWithData(value: RESULT?, file: File? = null, url: String? = null, istream: InputStream? = null)

    /**
     * Set job to the success state, save current result data.
     */
    fun success()


    /**
     * Set job to the error state.
     */
    fun error(exception: Exception? = null, reason: String = exception?.message ?: Deferred.ERR_REASON_DFAULT)

    /**
     * Reject job for some non-error reason.
     */
    fun reject(reason: String = Deferred.CANCEL_REASON_SERVICE)

    fun dispatchCallbacks()

}
