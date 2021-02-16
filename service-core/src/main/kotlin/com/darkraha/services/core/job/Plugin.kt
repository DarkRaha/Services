package com.darkraha.services.core.job

import com.darkraha.services.core.deferred.DeferredUserBuilder

/**
 * @author Rahul Verma
 */
interface Plugin<T> {
    val name: String
    val result: Any?
    fun attachTo(taskBuilder: DeferredUserBuilder<T>): DeferredUserBuilder<T>
}