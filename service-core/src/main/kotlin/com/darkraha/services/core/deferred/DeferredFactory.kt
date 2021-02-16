package com.darkraha.services.core.deferred

import com.darkraha.services.core.service.Service


/**
 * Factories for services.
 *
 * @author Rahul Verma
 */


interface DeferredFactory<PARAM> {
    fun <RESULT> newDeferred(cls: Class<RESULT>?): Deferred<PARAM, RESULT>
}

open class DeferredAbstractFactory {

    open fun <PARAM> newDeferredFactory(srv: Service<PARAM>): DeferredFactory<PARAM> {
        return object : DeferredFactory<PARAM> {
            val service = srv
            override fun <RESULT> newDeferred(cls: Class<RESULT>?): Deferred<PARAM, RESULT> {
                return Deferred<PARAM, RESULT>().apply {
                    service.setupDeferred(cls, this)
                }
            }
        }

    }
}

