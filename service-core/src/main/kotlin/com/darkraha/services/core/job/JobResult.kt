package com.darkraha.services.core.job

import java.io.File
import java.io.InputStream

/**
 * Holder for job result.
 *
 * @author Rahul Verma
 */
class JobResult<RESULT> {
    /**
     * Main result of job
     */
    var result: RESULT? = null
    var tmpResult: Any? = null
    var file: File? = null
    var url: String? = null
    var istream: InputStream? = null
    var clsTarget: Class<RESULT>? = null
    var mimetype: String? = null
    var rawResult: Any? = null

    fun applyResult() {
        result = tmpResult as RESULT?
    }

}
