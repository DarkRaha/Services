package com.darkraha.services.core.job


import com.darkraha.services.core.worker.WorkerActions

/**
 * @author Rahul Verma
 */

abstract class Task<PARAMS>{
    abstract fun  onTask(
        params: PARAMS?, workerActions: WorkerActions<*>,
        jobResponse: JobResponse<*>
    )
}

class JobTasks<PARAMS> {
    var main: Task<PARAMS>?=null
    var preProcessors: List<Task<PARAMS>>? = null
    var postProcessors: List<Task<PARAMS>>? = null
    /**
     * Used to synchronize main task
     */
    var syncObject: Any? = null
}



