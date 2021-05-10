package com.darkraha.services.core.worker

import com.darkraha.services.core.deferred.Deferred

abstract class WorkerA {
    abstract fun <PARAMS, RESULT> sync(deferred: Deferred<PARAMS, RESULT>)
    abstract fun <PARAMS, RESULT> async(deferred: Deferred<PARAMS, RESULT>)

}