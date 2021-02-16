/**
 * @author Rahul Verma
 */

package com.darkraha.services.core.job

import java.util.concurrent.atomic.AtomicReference


open class Job<PARAM, RESULT> {
    val state = AtomicReference(JobState.PREPARE)
    val result = JobResult<RESULT>()
    var params: PARAM? = null

    var plugins: MutableMap<String, Plugin<RESULT>>? = null

    var id: Long = 0
    var cmd: String? = null
    var idObject: Any? = null
    var reason: String? = null
    var error: Exception? = null
    var timeStart: Long = 0
    var timeEnd: Long = 0

    var serviceParam: Any? = null
    var rawParams: Any? = null
    var rawResponse: Any? = null
}