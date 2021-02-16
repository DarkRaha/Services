package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.*
import com.darkraha.services.core.worker.Worker
import com.darkraha.services.core.worker.WorkerActions
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.locks.ReentrantLock
import java.util.function.BiConsumer
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.withLock

/**
 * Default implementation of deferred object.
 *
 * @author Rahul Verma
 */
open class Deferred<PARAMS, RESULT> :
    DeferredServiceBuilder<RESULT>, WorkerActions {


    val job: Job<PARAMS, RESULT> = Job()
    val lock = ReentrantLock()
    protected val condFinished = lock.newCondition()
    var executor: ExecutorService? = null
    protected var future: Future<*>? = null
    var worker: Worker<PARAMS>? = null
    var globalSyncObject: Any? = null

    protected var userCallbacks: LinkedList<UserCallback<JobResponse<RESULT>>>? = null
    protected var cbProgress: MutableList<BiConsumer<ProgressData, JobResponse<RESULT>>>? = null

    fun toJobResponse() = this as JobResponse<RESULT>
    fun toDeferredCallbacks() = this as DeferredUserCallbacks<RESULT>


    protected open fun doDispatchCallbacks(jobState: JobState) {
        userCallbacks?.apply {
            forEach {
                try {
                    it.dispatchCallback(this@Deferred, jobState)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    protected open fun dispatchProgressCallbacks(pdi: ProgressData) {
        cbProgress?.apply {
            forEach {
                dispatchProgressCallback(pdi, it)
            }
        }
    }

    protected open fun dispatchProgressCallback(
        pdi: ProgressData,
        cb: BiConsumer<ProgressData, JobResponse<RESULT>>
    ) {
        try {
            cb.accept(pdi, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Assign error info, thread non-safe
     */
    protected open fun doError(exception: Exception?, reason: String) {
        job.state.get()
            .takeIf { it == JobState.PREPARE || it == JobState.PENDING }
            ?.apply {
                job.let {
                    it.reason = reason
                    it.error = exception
                    it.timeEnd = System.currentTimeMillis()
                }
                job.state.set(JobState.ERROR)
            }
    }

    //-------------------------------------------------------

    override val result: JobResult<RESULT>
        get() = job.result

    override val syncObject: Any?
        get() = globalSyncObject


    override fun success() {
        lock.withLock { }
        lock.lock()
        try {
            job.state.get()
                .takeIf { it == JobState.PREPARE || it == JobState.PENDING }
                ?.apply {
                    job.timeEnd = System.currentTimeMillis()
                    job.result.clsTarget?.apply { job.result.applyResult() }
                    job.state.set(JobState.SUCCESS)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            doError(e, e.message ?: "Fail on do success")
        } finally {
            lock.unlock()
        }
    }

    override fun success(tmpResult: Any?, file: File?, url: String?, istream: InputStream?) {
        lock.lock()
        try {
            job.state.get()
                .takeIf { it == JobState.PREPARE || it == JobState.PENDING }
                ?.apply {

                    job.result.let {
                        it.tmpResult = tmpResult
                        it.file = file
                        it.url = url
                        it.istream = istream
                        it.clsTarget?.apply { it.applyResult() }
                    }

                    job.timeEnd = System.currentTimeMillis()
                    job.state.set(JobState.SUCCESS)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            doError(e, e.message ?: "Fail on do success")
        } finally {
            lock.unlock()
        }
    }

    override fun error(exception: Exception?, reason: String) {
        lock.lock()
        doError(exception, reason)
        lock.unlock()
    }

    override fun reject(reason: String) {
        lock.lock()
        job.state.get()
            .takeIf { it == JobState.PREPARE || it == JobState.PENDING }
            ?.apply {
                job.state.set(JobState.CANCELED)
                job.reason = reason
                job.timeEnd = System.currentTimeMillis()
            }
        lock.unlock()
    }

    override fun notifyProgress(dataProgress: ProgressData) = synchronized(this) {
        dispatchProgressCallbacks(dataProgress)
    }

    override fun pending() = run {
        lock.lock()
        job.state.get()
            .takeIf { it == JobState.PREPARE }
            ?.run {
                job.state.set(JobState.PENDING)
                job.timeStart = System.currentTimeMillis()
                true
            } ?: false
    }.apply { lock.unlock() }

    override fun dispatchCallbacks() {
        lock.lock()
        val state = job.state.get()

        doDispatchCallbacks(state)

        if (state.isFinished()) {
            doDispatchCallbacks(JobState.FINISHED)
        }
        lock.unlock()
    }

    override fun notifyFinished() {
        lock.withLock { condFinished.signalAll() }
    }

    //-------------------------------------------------------
    override fun getResult(): RESULT? = job.result.result
    override fun getMimetype(): String? = job.result.mimetype
    override fun getFile(): File? = job.result.file
    override fun getStream(): InputStream? = job.result.istream
    override fun getUrl(): String? = job.result.url
    override fun getState(): JobState = job.state.get()
    override fun getReason(): String? = job.reason
    override fun getError(): Exception? = job.error
    override fun getTimeEnd(): Long = job.timeEnd
    override fun getTimeStart(): Long = job.timeStart
    override fun getCmd(): String? = job.cmd
    override fun getId(): Long = job.id
    override fun getIdObject(): Any? = job.idObject
    override fun getPlugins(): Map<String, Plugin<RESULT>>? = job.plugins

    override fun await(): JobResponse<RESULT> = apply {
        lock.withLock {
            takeIf { !job.state.get().isFinished() }?.apply {
                condFinished.await()
            }
        }
    }


    //--------------------------------------------

    protected open fun addHandler(
        jobState: JobState,
        objWeak: Any?,
        onMainThread: Boolean = false,
        cb: Consumer<JobResponse<RESULT>>
    ) = apply {
        val cbInner = if (onMainThread) UserCallbackMainThread(jobState, objWeak, cb)
        else UserCallback(jobState, objWeak, cb)
        lock.lock()
        val currentState = job.state.get()
        if (currentState.isIgnoreAdd(jobState)) {
            cbInner.dispatchCallback(
                this, if (jobState == JobState.FINISHED && currentState.isFinished())
                    jobState
                else currentState
            )
        } else {
            userCallbacks = userCallbacks ?: LinkedList()
            userCallbacks!!.add(cbInner)
        }
        lock.unlock()
    }

    protected open fun addProgressHandler(
        objWeak: Any?,
        onMainThread: Boolean = false,
        cb: BiConsumer<ProgressData, JobResponse<RESULT>>
    ) = apply {
        if (!getState().isFinished()) {
            val cbInner = when {
                objWeak != null && onMainThread -> WeakBiConsumerOnMainThread(objWeak, cb)
                objWeak != null -> WeakBiConsumer(objWeak, cb)
                onMainThread -> BiConsumerOnMainThread(cb)
                else -> cb
            }

            lock.lock()
            cbProgress = (cbProgress ?: ArrayList()).apply { add(cbInner) }
            lock.unlock()
        }
    }


    override fun setId(id: Long): DeferredServiceBuilder<RESULT> = apply { job.id = id }
    override fun setIdObject(idObj: Any?): DeferredServiceBuilder<RESULT> = apply { job.idObject = idObj }
    override fun setCmd(cmd: String?): DeferredServiceBuilder<RESULT> = apply { job.cmd = cmd }
    override fun setResultMimetype(mimetype: String?): DeferredServiceBuilder<RESULT> =
        apply { job.result.mimetype = mimetype }

    override fun setResultFile(file: File?): DeferredServiceBuilder<RESULT> = apply { job.result.file = file }

    override fun onBeforeStart(objWeak: Any?, onMainThread: Boolean, cb: Consumer<JobResponse<RESULT>>):
            DeferredServiceBuilder<RESULT> =
        addHandler(JobState.PENDING, objWeak, onMainThread, cb)


    override fun onCancel(objWeak: Any?, onMainThread: Boolean, cb: Consumer<JobResponse<RESULT>>):
            DeferredServiceBuilder<RESULT> =
        addHandler(JobState.CANCELED, objWeak, onMainThread, cb)


    override fun onSuccess(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT> =
        addHandler(JobState.SUCCESS, objWeak, onMainThread, cb)


    override fun onError(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT> =
        addHandler(JobState.ERROR, objWeak, onMainThread, cb)


    override fun onFinish(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT> =
        addHandler(JobState.FINISHED, objWeak, onMainThread, cb)


    override fun onProgress(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: BiConsumer<ProgressData, JobResponse<RESULT>>
    ): DeferredServiceBuilder<RESULT> =
        addProgressHandler(objWeak, onMainThread, cb)


    override fun plugin(p: Plugin<RESULT>): DeferredServiceBuilder<RESULT> = this.apply {
        if (job.plugins == null) {
            job.plugins = HashMap()
        }

        job.plugins?.apply {
            if (this[p.name] != null) {
                throw IllegalStateException("Plugin '${p.name}' already attached.")
            }
            this[p.name] = p
            p.attachTo(this@Deferred)
        }
    }

    override fun sync(): DeferredUserCallbacks<RESULT> = apply {
        worker?.getJobWorkflow(this)?.invoke(this)
    }

    override fun async(): DeferredUserCallbacks<RESULT> = apply {
        worker!!.getJobWorkflow(this).let { workflow ->
            executor!!.apply {
                future = submit { workflow(this@Deferred) }
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

