package com.darkraha.services.endecodefiles.decoder

import com.darkraha.services.core.deferred.UserDeferred
import com.darkraha.services.core.job.JobState
import com.darkraha.services.core.job.Task
import com.darkraha.services.core.service.TypedService
import com.darkraha.services.core.utils.LRUMemCache
import com.darkraha.services.core.utils.SoftRef
import com.darkraha.services.core.worker.Worker
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class FileDecoderService private constructor() : TypedService<Any>(Any::class.java) {

    private val cache = Collections.synchronizedMap(LRUMemCache<String, Any>(1024 * 1024 * 10))
    private val decoders = HashMap<String, Decoder<*>>()

    fun <T> addDecoder(fileExtension: String, clsTarget: Class<*>, decoder: Decoder<T>) {
        decoders[fileExtension + "-" + clsTarget.simpleName] = decoder
    }

    fun <R> decode(file: File, clsTarget: Class<R>, decoderParams: Any? = null): UserDeferred<R> {
        val key = file.extension + "-" + clsTarget.simpleName
        val decoder = decoders[key]!! as Decoder<R>

        return newTypedDeferred(clsTarget).apply {
            cache[key]?.get()?.apply {
                job.result.tmpResult = this
                setSuccess()
            }
            if (job.getState() == JobState.PREPARE) {
                job.exeCode = {
                    job.result.tmpResult = cache[key]?.get()
                        ?: decoder.decode(file, decoderParams)
                            ?.apply {
                                cache[key] = SoftRef(this, decoder.calcSize(this))
                            }
                     job.result.tmpResult as R?
                }
            }
        }.async()
    }


    class Builder {
        private val srv = FileDecoderService()
        private var cntThreads = 5

        fun threadsCount(cnt: Int): Builder = this.apply { cntThreads = cnt }
        fun defaultWorker(w: Worker): Builder = this.apply { srv.defaultWorker = w }
        fun defaultMainTask(t: Task<Any>?) = this.apply { srv.defaultTask = t }
        fun defaultPreProcessors(p: List<Task<Any>>?) = this.apply { srv.defaultPreProcessors = p }
        fun defaultPostProcessors(p: List<Task<Any>>?) = this.apply { srv.defaultPostProcessors = p }
        fun addDecoder(fileExtension: String, clsTarget: Class<*>, decoder: Decoder<in Any?>): Builder = this.apply {
            srv.decoders[fileExtension + "-" + clsTarget.simpleName] = decoder
        }

        fun build(): FileDecoderService {
            return srv.apply {
                if (srv.defaultWorker == null) {
                    srv.defaultWorker = newThreadWorker(cntThreads)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = Builder().build()
    }

}