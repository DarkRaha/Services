/**
 * @author Rahul Verma
 */

package com.darkraha.services.core.job

import java.io.File
import java.io.InputStream
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer


class Job<PARAM, RESULT>(clsTarget: Class<RESULT>) : JobResponse<RESULT> {
    val info = JobInfo()
    var params: PARAM? = null
    val result = JobResult(clsTarget)
    val tasks = JobTasks<PARAM>()
    val state = AtomicReference(JobState.PREPARE)
    var callbacks: MutableList<JobCallback<RESULT>>? = null

    var progress: ProgressData? = null


    fun doAddCallbacks(
        cb: JobCallbacks<RESULT>
    ) {
        callbacks = callbacks ?: ArrayList()
        callbacks!!.add(cb)
        val state = state.get()
        cb.dispatchCallback(this, state)
        if (state.isFinished()) {
            cb.dispatchCallback(this, JobState.FINISHED)
        }
    }


    fun doAddCallback(
        jobState: JobState,
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ) {
        try {
            val currentState = state.get()
            if (currentState.isIgnoreAdd(jobState)) {
                if (currentState.isAllowExecute(jobState)) {
                    JobCallbackState(jobState, objWeak, onMainThread, cb)
                        .dispatchCallback(this, jobState)
                }
            } else {
                callbacks = callbacks ?: ArrayList()
                callbacks!!.add(JobCallbackState(jobState, objWeak, onMainThread, cb))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun doRemoveCallbacks(objWeak: Any) {
        callbacks?.apply {
            val iterator = iterator()
            while (iterator.hasNext()) {
                val cb = iterator.next()
                if (objWeak == cb.owner || cb.owner?.get() == objWeak) {
                    iterator.remove()
                }
            }
        }
    }

    fun doDispatchCallbacks(jobState: JobState) {
        callbacks?.apply {
            if (state.get().isAllowExecute(jobState))
                forEach {
                    try {
                        it.dispatchCallback(this@Job, jobState)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }

    fun setPending(): Boolean =
        if (state.get() == JobState.PREPARE) {
            state.set(JobState.PENDING)
            info.timeStart = System.currentTimeMillis()
            true
        } else
            false


    fun setSuccess() {
        try {

            if (state.get() <= JobState.PENDING) {
                info.timeEnd = System.currentTimeMillis()
                result.expectResult()
                state.set(JobState.SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setError(e, e.message ?: "Fail on do success")
        }
    }

    fun setSuccess(value: RESULT?, file: File?, url: String?, istream: InputStream?) {
        try {
            if (state.get() <= JobState.PENDING) {
                result.result = value
                result.file = file
                result.url = url
                result.istream = istream
                result.expectResult()
                info.timeEnd = System.currentTimeMillis()
                state.set(JobState.SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setError(e, e.message ?: "Fail on do success")
        }
    }

    fun setError(e: Exception?, reason: String?) {
        if (state.get() <= JobState.PENDING) {
            result.reason = reason
            result.error = e
            info.timeEnd = System.currentTimeMillis()
            state.set(JobState.ERROR)
        }
    }

    fun setRejected(reason: String?) {
        if (state.get() <= JobState.PENDING) {
            state.set(JobState.CANCELED)
            result.reason = reason
            info.timeEnd = System.currentTimeMillis()
        }
    }

    //--------------------------------------------------------------------------
    override fun getFile(): File? = result.file
    override fun getUrl(): String? = result.url
    override fun getStream(): InputStream? = result.istream
    override fun getResult(): RESULT? = result.result
    override fun getMimetype(): String? = result.mimetype
    override fun getReason(): String? = result.reason
    override fun getError(): Exception? = result.error
    override fun getState(): JobState = state.get()
    override fun getId(): Long = info.id
    override fun getIdObject(): Any? = info.idObject
    override fun getCmd(): String? = info.cmd
    override fun getTimeStart(): Long = info.timeStart
    override fun getTimeEnd(): Long = info.timeEnd
    override fun getProgressData(): ProgressData? = progress
    override fun getPlugin(name: String): Plugin<RESULT>? = callbacks?.find {
        it is Plugin && it.name == name
    } as Plugin<RESULT>?


}