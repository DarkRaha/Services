package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.*
import com.darkraha.services.core.job.JobCallback
import com.darkraha.services.core.worker.WorkerA
import com.darkraha.services.core.worker.WorkerHelperA
import java.io.File
import java.util.*
import java.util.function.Consumer
import kotlin.collections.HashMap

/**
 * Default implementation of deferred object.
 *
 * @author Rahul Verma
 */
class Deferred<PARAMS, RESULT>(clsResult: Class<RESULT>) :
    DeferredServiceBuilder<PARAMS, RESULT> {
    val job: Job<PARAMS, RESULT> = Job(clsResult)
    lateinit var worker: WorkerA
    lateinit var workerHelper: WorkerHelperA<PARAMS, RESULT>


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
        workerHelper.addCallback(JobState.PENDING, objWeak, onMainThread, cb)
    }

    override fun onCancel(objWeak: Any?, onMainThread: Boolean, cb: Consumer<JobResponse<RESULT>>)
            : DeferredServiceBuilder<PARAMS, RESULT> = this.apply {
        workerHelper.addCallback(JobState.CANCELED, objWeak, onMainThread, cb)
    }

    override fun onSuccess(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> =
        this.apply {
            workerHelper.addCallback(JobState.SUCCESS, objWeak, onMainThread, cb)
        }

    override fun onError(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> =
        this.apply {
            workerHelper.addCallback(JobState.ERROR, objWeak, onMainThread, cb)
        }

    override fun onFinish(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> =
        this.apply {
            workerHelper.addCallback(JobState.FINISHED, objWeak, onMainThread, cb)
        }

    override fun onProgress(
        objWeak: Any?,
        onMainThread: Boolean,
        cb: Consumer<JobResponse<RESULT>>
    ): DeferredServiceBuilder<PARAMS, RESULT> =
        this.apply {
            workerHelper.addCallback(JobState.PROGRESS, objWeak, onMainThread, cb)
        }

    override fun removeCallbacks(objWeak: Any?) {
        workerHelper.removeCallbacks(objWeak)
    }

    override fun subscribe(cb: JobCallbacks<RESULT>): DeferredServiceBuilder<PARAMS, RESULT> = this.apply {
        workerHelper.addSubscribe(cb)
    }

    override fun sync(): UserDeferred<RESULT> = apply {
        worker.sync(this)
    }

    override fun async(): UserDeferred<RESULT> = apply {
        worker.async(this)
    }

    override fun await(): JobResponse<RESULT> = workerHelper.await()
    override fun getResponse(): JobResponse<RESULT> = job

    companion object {
        @JvmStatic
        val ERR_REASON_DFAULT = "Unknown error."

        @JvmStatic
        val CANCEL_REASON_SERVICE = "Canceled by service."

        @JvmStatic
        val CANCEL_REASON_USER = "Canceled by user."
    }
}

