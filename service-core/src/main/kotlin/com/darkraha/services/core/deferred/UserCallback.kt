package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.JobState
import com.darkraha.services.core.utils.MainThread
import com.darkraha.services.core.utils.OnMainThread
import java.lang.ref.WeakReference
import java.util.function.Consumer


open class UserCallback<T>(val state: JobState, pOwner: Any?, val uiCallback: Boolean, val consumer: Consumer<T>) {
    var owner: WeakReference<Any>? = if (pOwner == null) null else WeakReference(pOwner)

    var done = false
        protected set

    fun isOwnerAllowed(): Boolean = owner?.get() != null ?: true

    open fun dispatchCallback(t: T, targetState: JobState) {
        if (state == targetState && isOwnerAllowed()) {
            if (uiCallback) {
                MainThread.execute {
                    if (isOwnerAllowed()) {
                        consumer.accept(t)
                        done = true
                    }
                }
            } else {
                consumer.accept(t)
                done = true
            }
        }
    }
}

