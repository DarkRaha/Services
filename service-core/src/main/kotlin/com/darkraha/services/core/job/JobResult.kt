package com.darkraha.services.core.job

import java.io.File
import java.io.InputStream

/**
 * Holder for job result.
 *
 * @author Rahul Verma
 */
class JobResult<RESULT>(val clsTarget: Class<RESULT>) {
    /**
     * Main result of job
     */
    var result: RESULT? = null
    var tmpResult: Any? = null
    var file: File? = null
    var url: String? = null
    var istream: InputStream? = null
    var isExpectedValue = false
    var mimetype: String? = null
    var rawResult: Any? = null

    var progressData: ProgressData? = null

    /**
     * Reason of error or cancellation
     */
    var reason: String? = null
    var error: Exception? = null

    fun expectResult() {
        if (result == null && tmpResult!=null) {
            result = tmpResult as RESULT?
        }
    }
}