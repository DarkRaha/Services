package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.*
import com.darkraha.services.core.worker.WorkerA
import com.darkraha.services.core.worker.WorkerActions
import java.io.File
import java.io.InputStream
import java.util.concurrent.locks.ReentrantLock
import java.util.function.BiFunction
import java.util.function.Consumer
import kotlin.concurrent.withLock

/**
 * Default implementation of deferred object.
 *
 * @author Rahul Verma
 */
open class Deferred<PARAMS, RESULT>(clsResult: Class<RESULT>? = null) :
    DeferredServiceBuilder<PARAMS, RESULT>, WorkerActions<RESULT> {
    val job: Job<PARAMS, RESULT> = Job(clsResult)
    lateinit var worker: WorkerA

    val jobLock = ReentrantLock()
    val condFinished = jobLock.newCondition()
    var chainNext: ChainBlock<RESULT, *, *>? = null


    override fun id(v: Long): DeferredServiceBuilder<PARAMS, RESULT> = apply { job.info.id = v }
    override fun idObject(v: Any?): DeferredServiceBuilder<PARAMS, RESULT> = apply { job.info.idObject = v }
    override fun cmd(v: String?): DeferredServiceBuilder<PARAMS, RESULT> = apply { job.info.cmd = v }
    override fun setResultMimetype(mimetype: String?): DeferredServiceBuilder<PARAMS, RESULT> =
        apply { job.result.mimetype = mimetype }

    override fun setResultFile(file: File?): DeferredServiceBuilder<PARAMS, RESULT> = apply { job.result.file = file }


    override fun setMainTask(t: Task<PARAMS>?): DeferredServiceBuilder<PARAMS, RESULT> = apply {
        job.tasks.main = t
    }

    override fun setPreProcessors(p: List<Task<PARAMS>>?): DeferredServiceBuilder<PARAMS, RESULT> = apply {
        job.tasks.preProcessors = p
    }

    override fun setPostProcessors(p: List<Task<PARAMS>>?): DeferredServiceBuilder<PARAMS, RESULT> = apply {
        job.tasks.postProcessors = p
    }

    override fun onBeforeStart(objWeak: Any?, onMainThread: Boolean, cb: Consumer<JobResponse<RESULT>>):
            DeferredServiceBuilder<PARAMS, RESULT> = this.apply {
        jobLock.withLock {
            job.doAddCallback(JobState.PENDING, objWeak, onMainThread, cb)
        }
    }

    override fun onCancel(objWeak: Any?, onMainThread: Boolean, cb: Consumer<JobResponse<RESULT>>)
            : DeferredServiceBuilder<PARAMS, RESULT> = this.apply {
        jobLock.withLock {
            job.doAddCallback(JobState.CANCELED, objWeak, onMainThread, cb)
        }
    }

    override fun onSuccess(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> =
        this.apply {
            jobLock.withLock {
                job.doAddCallback(JobState.SUCCESS, objWeak, onMainThread, cb)
            }
        }

    override fun onError(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> =
        this.apply {
            jobLock.withLock {
                job.doAddCallback(JobState.ERROR, objWeak, onMainThread, cb)
            }
        }

    override fun onFinish(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> =
        this.apply {
            jobLock.withLock {
                job.doAddCallback(JobState.FINISHED, objWeak, onMainThread, cb)
            }
        }

    override fun onProgress(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> =
        this.apply {
            jobLock.withLock {
                job.doAddCallback(JobState.PROGRESS, objWeak, onMainThread, cb)
            }
        }

    override fun <NEXTPARAM, NEXTRESULT> append(
        next: Deferred<NEXTPARAM, NEXTRESULT>,
    ): ChainBlock.ChainNextBuilder<RESULT, NEXTPARAM, NEXTRESULT> {
        return ChainBlock.ChainNextBuilder(this, next)
    }

    override fun removeCallbacks(objWeak: Any?) {
        objWeak?.also { weak ->
            jobLock.withLock {
                job.doRemoveCallbacks(weak)
            }
        }
    }

    override fun subscribe(cb: JobCallbacks<RESULT>)
            : DeferredServiceBuilder<PARAMS, RESULT> = this.apply {
        jobLock.withLock {
            job.doAddCallbacks(cb)
        }
    }

    override fun sync(): UserDeferred<RESULT> = apply {
        worker.sync(this)
    }

    override fun async(): UserDeferred<RESULT> = apply {
        worker.async(this)
    }

    override fun await(): JobResponse<RESULT> {
        jobLock.withLock {
            if (!job.state.get().isFinished()) {
                condFinished.await()
            }
        }
        return job
    }

    override fun getResponse(): JobResponse<RESULT> = job


    //===================================================================

    /**
     * Dispatch callbacks for current state (thread non-safe)
     */
    fun dispatchCallbacks() {
        val state = job.state.get()
        job.doDispatchCallbacks(state)

        if (state.isFinished()) {
            job.doDispatchCallbacks(JobState.FINISHED)
        }
    }

    /**
     * Perform specified tasks if state is PENDING.
     */
    fun performTasks(
        tasks: List<Task<PARAMS>>?
    ) {
        tasks?.takeIf { isPending() }?.forEach {
            if (isPending()) {
                try {
                    it.onTask(job.params, this, job)
                } catch (e: Exception) {
                    setError(e)
                    e.printStackTrace()
                }
            } else return
        }
    }

    open fun notifyFinished() {
        jobLock.withLock {
            condFinished.signalAll()
        }
    }

    fun isPending() = job.state.get() == JobState.PENDING

    //===================================================================
    override val result: JobResult<RESULT>
        get() = job.result

    override fun doDispatchCallbacks() {
        jobLock.withLock {
            dispatchCallbacks()
        }
    }

    override fun setError(exception: Exception?, reason: String) {
        jobLock.withLock {
            job.setError(exception, reason)
        }
    }

    override fun setSuccess() {
        jobLock.withLock {
            job.setSuccess()
        }
    }

    override fun setSuccessWithData(value: RESULT?, file: File?, url: String?, istream: InputStream?) {
        jobLock.withLock {
            job.setSuccess(value, file, url, istream)
        }
    }

    override fun setPending(): Boolean = jobLock.withLock { job.setPending() }

    override fun setReject(reason: String) {
        jobLock.withLock { job.setRejected(reason) }
    }

    override fun notifyProgress(dataProgress: ProgressData) {
        jobLock.withLock {
            job.also {
                it.progress = dataProgress
                it.doDispatchCallbacks(JobState.PROGRESS)
            }
        }
    }

    companion object {
        @JvmStatic
        val ERR_REASON_DFAULT = "Unknown error."

        @JvmStatic
        val CANCEL_REASON_SERVICE = "Canceled by service."

        @JvmStatic
        val CANCEL_REASON_USER = "Canceled by user."
    }
}

