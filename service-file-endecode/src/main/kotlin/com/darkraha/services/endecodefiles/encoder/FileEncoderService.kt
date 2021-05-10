package com.darkraha.services.endecodefiles.encoder

import com.darkraha.services.core.deferred.UserDeferred
import com.darkraha.services.core.job.Task
import com.darkraha.services.core.service.TypedService
import com.darkraha.services.core.worker.Worker
import java.io.File

class FileEncoderService private constructor() : TypedService<Any>(Any::class.java) {

    private val encoders = HashMap<String, Encoder<*>>()

    fun <T> addEncoder(fileExtension: String, clsTarget: Class<*>, encoder: Encoder<T>) {
        encoders[fileExtension + "-" + clsTarget.simpleName] = encoder
    }

    fun <T> encode(file: File, source: T, encoderParams: Any? = null): UserDeferred<File> {
        val key = file.extension + "-" + source!!::class.java.simpleName
        val encoder = encoders[key]!! as Encoder<T>

        return newTypedDeferred(File::class.java).apply {
            job.exeCode = {
                val result = encoder.encode(source, encoderParams, file)
                job.result.tmpResult = result
                job.result.file = result
                result
            }
        }.async()
    }


    class Builder {
        private val srv = FileEncoderService()
        private var cntThreads = 5

        fun threadsCount(cnt: Int): Builder = this.apply { cntThreads = cnt }
        fun defaultWorker(w: Worker): Builder = this.apply { srv.defaultWorker = w }
        fun defaultMainTask(t: Task<Any>?) = this.apply { srv.defaultTask = t }
        fun defaultPreProcessors(p: List<Task<Any>>?) = this.apply { srv.defaultPreProcessors = p }
        fun defaultPostProcessors(p: List<Task<Any>>?) = this.apply { srv.defaultPostProcessors = p }
        fun addEncoder(fileExtension: String, clsTarget: Class<*>, encoder: Encoder<in Any?>): Builder = this.apply {
            srv.encoders[fileExtension + "-" + clsTarget.simpleName] = encoder
        }

        fun build(): FileEncoderService {
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