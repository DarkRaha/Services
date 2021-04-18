package com.darkraha.services.core.job

import com.darkraha.services.core.worker.executors.MainThread
import com.darkraha.services.core.worker.executors.WorkerExecutor
import java.lang.ref.WeakReference
import java.sql.Date.valueOf
import java.util.*
import java.util.function.Consumer

interface JobCallback<T> {
    val owner: WeakReference<out Any>?
    fun dispatchCallback(t: JobResponse<T>, targetState: JobState)
}

open class JobCallbackState<T>(
    val state: JobState, pOwner: Any?,
    val wExe: WorkerExecutor<*>? = MainThread,
    val consumer: Consumer<JobResponse<T>>
) : JobCallback<T> {
    constructor(
        state: JobState, pOwner: Any?,
        uiCallback: Boolean,
        consumer: Consumer<JobResponse<T>>
    ) : this(state, pOwner, if (uiCallback) MainThread else null, consumer)

    private var _owner: WeakReference<out Any>? = when {
        pOwner == null -> null
        pOwner is WeakReference<*> -> pOwner
        else -> WeakReference(pOwner)
    }

    override val owner: WeakReference<out Any>?
        get() = _owner

    var done = false
        protected set

    fun isOwnerAllowed(): Boolean = if (owner == null) true else owner?.get() != null




    override fun dispatchCallback(t: JobResponse<T>, targetState: JobState) {

        if (state == targetState && isOwnerAllowed()) {
            if (wExe != null) {
                wExe.execute {
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

open class JobCallbacks<T>(pOwner: Any?, val wExe: WorkerExecutor<*>? = MainThread) : JobCallback<T> {
    constructor(pOwner: Any?, uiCallback: Boolean) : this(pOwner, if (uiCallback) MainThread else null)

    private var _owner: WeakReference<out Any>? = when {
        pOwner == null -> null
        pOwner is WeakReference<*> -> pOwner
        else -> WeakReference(pOwner)
    }

    override val owner: WeakReference<out Any>?
        get() = _owner

    var done = false
        protected set

    fun isOwnerAllowed(): Boolean = if (owner == null) true else owner?.get() != null

    override fun dispatchCallback(t: JobResponse<T>, targetState: JobState) {
        if (isOwnerAllowed()) {
            if (wExe != null) {
                wExe.execute {
                    if (isOwnerAllowed()) {
                        onCallback(t, targetState)
                        done = true
                    }
                }
            } else {
                onCallback(t, targetState)
                done = true
            }
        }
    }

    fun onCallback(t: JobResponse<T>, targetState: JobState) {
        when (targetState) {
            JobState.PREPARE -> {
            }
            JobState.PENDING -> onPending(t)
            JobState.ERROR -> onError(t)
            JobState.SUCCESS -> onSuccess(t)
            JobState.CANCELED -> onCancel(t)
            JobState.FINISHED -> onFinish(t)
            JobState.PROGRESS -> onProgress(t)
        }
    }

    open fun onPending(t: JobResponse<T>) {

    }

    open fun onError(t: JobResponse<T>) {

    }

    open fun onSuccess(t: JobResponse<T>) {

    }

    open fun onCancel(t: JobResponse<T>) {

    }

    open fun onFinish(t: JobResponse<T>) {

    }

    open fun onProgress(t: JobResponse<T>) {

    }
}

open class Plugin<T> : JobCallbacks<T>(null, false) {
    lateinit var name: String
        protected set

    open val result: Any? = null

}