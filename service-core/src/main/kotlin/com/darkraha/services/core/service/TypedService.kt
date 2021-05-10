package com.darkraha.services.core.service

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.job.Task


open class TypedService<PARAM>(protected val defaultClsParam: Class<PARAM>, mainTask: Task<PARAM>? = null) :
    Service() {

    protected var defaultTask: Task<PARAM>? = mainTask
    protected var defaultPreProcessors: List<Task<PARAM>>? = null
    protected var defaultPostProcessors: List<Task<PARAM>>? = null

    override fun <PARAM, RESULT> newDeferred(
        clsParam: Class<PARAM>,
        clsResult: Class<RESULT>?
    ): Deferred<PARAM, RESULT> {
        return super.newDeferred(clsParam, clsResult).apply {
            if (clsParam == defaultClsParam) {
                defaultTask?.apply { job.tasks.main = this as Task<PARAM> }
                defaultPreProcessors?.apply { job.tasks.preProcessors = this as List<Task<PARAM>> }
                defaultPostProcessors?.apply { job.tasks.postProcessors = this as List<Task<PARAM>> }
            }
        }
    }

    fun <RESULT> newTypedDeferred(param: PARAM, clsResult: Class<RESULT>?): Deferred<PARAM, RESULT> {
        return newDeferred(defaultClsParam, clsResult).apply {
            job.params = param
            defaultTask?.apply { job.tasks.main = this as Task<PARAM> }
            defaultPreProcessors?.apply { job.tasks.preProcessors = this as List<Task<PARAM>> }
            defaultPostProcessors?.apply { job.tasks.postProcessors = this as List<Task<PARAM>> }
        }
    }

    fun <RESULT> newTypedDeferred(clsResult: Class<RESULT>?): Deferred<PARAM, RESULT> {
        return newDeferred(defaultClsParam, clsResult).apply {
            defaultTask?.apply { job.tasks.main = this as Task<PARAM> }
            defaultPreProcessors?.apply { job.tasks.preProcessors = this as List<Task<PARAM>> }
            defaultPostProcessors?.apply { job.tasks.postProcessors = this as List<Task<PARAM>> }
        }
    }
}