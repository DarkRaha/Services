package com.darkraha.services.http

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.deferred.DeferredFactory
import com.darkraha.services.core.deferred.DeferredServiceBuilder
import com.darkraha.services.core.deferred.DeferredUserCallbacks
import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.service.Service
import com.darkraha.services.core.utils.json.JsonConverter
import com.darkraha.services.core.utils.json.JsonConverterA
import com.darkraha.services.core.worker.Worker
import com.darkraha.services.core.worker.WorkerActions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

open class HttpServiceOk protected constructor() : Service<Request>(), HttpService<Request> {

    protected var httpClient: OkHttpClient? = null
    protected var jsonConverter: JsonConverterA? = null

    override fun setupDefault() {
        httpClient = httpClient ?: newClient()
        mainWorker = mainWorker ?: Worker<Request>().onMainTask(this::mainTask)
        jsonConverter = jsonConverter ?: JsonConverter()
        super.setupDefault()
    }

    open fun newClient(): OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
            val originalResponse: Response = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body!!))
                .build()
        })
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()


    protected fun mainTask(params: Request?, actions: WorkerActions, jobResponse: JobResponse<*>) {

        httpClient!!.newCall(params!!).execute().use { httpResponse ->

            actions.result.rawResult = httpResponse

            if (!httpResponse.isSuccessful) throw IOException("Unexpected code $httpResponse")

            httpResponse.body.takeIf { httpResponse.body is ProgressResponseBody }?.apply {
                this as ProgressResponseBody
                jobNotifyProgress = actions
            }

            actions.apply {
                result.mimetype = httpResponse.body?.contentType()?.toString()

                when {
                    result.clsTarget == String::class.java -> result.tmpResult = httpResponse.body?.string()
                    result.clsTarget == ByteArray::class.java -> result.tmpResult = httpResponse.body?.bytes()
                    result.clsTarget == File::class.java -> try {
                        result.file?.apply {
                            result.tmpResult = this
                            delete()
                            parentFile?.mkdirs()
                            outputStream().use { fileOut ->
                                httpResponse.body?.use {
                                    it.byteStream().copyTo(fileOut)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        result.file?.delete()
                        e.printStackTrace()
                        error(e)
                    }
                    result.clsTarget == Unit::class.java -> result.tmpResult = Unit
                    else -> {
                        result.tmpResult = jsonConverter!!.fromJson(httpResponse.body!!.string(), result.clsTarget!!)
                    }
                }
            }
        }
    }


    fun get(): DeferredServiceBuilder<String> {
        return httpRequest("https://publicobject.com/helloworld.txt")
            .build(String::class.java)
    }

    //---------------------------------------------------------
    override fun httpRequest(url: String): DeferredHttpRequestBuilder {
        return DeferredHttpRequestBuilder(deferredFactory!!).url(url)
    }

    override fun download(url: String, params: List<Pair<String, Any>>?): DeferredUserCallbacks<String> {
        return httpRequest(url)
            .addStringParams(params)
            .build(String::class.java)
            .async()
    }

    override fun downloadFile(url: String, file: File?): DeferredUserCallbacks<File> {
        val lFile: File = file ?: run {
            File(File(URI(url).getPath()).name)
        }

        return httpRequest(url).build(File::class.java).setResultFile(lFile).async()
    }

    override fun downloadByteArray(url: String): DeferredUserCallbacks<ByteArray> {
        return httpRequest(url).build(ByteArray::class.java).async()
    }

    override fun postForm(url: String, params: List<Pair<String, Any>>?): DeferredUserCallbacks<String> {
        return httpRequest(url).addStringParams(params).method("POST").build(String::class.java).async()
    }

    override fun uploadFile(
        url: String,
        file: File,
        fileParamName: String,
        mimetype: String,
        params: List<Pair<String, Any>>?
    ): DeferredUserCallbacks<String> {
        return httpRequest(url).addFileParam(fileParamName, file, mimetype)
            .addStringParams(params)
            .build(String::class.java).async()
    }

    override  fun <POST, RESPONSE> restApi(
        url: String,
        cls: Class<RESPONSE>,
        obj: POST?,
        method: String
    ): DeferredUserCallbacks<RESPONSE>{
        val builder = httpRequest(url)
            .method(method)

        if (obj != null) {
            builder.setBody(jsonConverter!!.toJson(obj), JSON)
        }

        return builder.build(cls).async()
    }

    class Builder {
        private val httpSrv = HttpServiceOk()
        fun httpClient(c: OkHttpClient): Builder = this.apply { httpSrv.httpClient = c }
        fun mainWorker(w: Worker<Request>): Builder = this.apply { httpSrv.mainWorker = w }
        fun executor(e: ExecutorService): Builder = this.apply { httpSrv.executorService = e }
        fun deferredFactory(df: DeferredFactory<Request>): Builder = this.apply { httpSrv.deferredFactory = df }
        fun jsonConverter(jc: JsonConverterA): Builder = this.apply { httpSrv.jsonConverter = jc }
        fun build(): HttpServiceOk {
            httpSrv.setupDefault()
            return httpSrv
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = Builder().build()

        @JvmStatic
        val JSON = "application/json; charset=utf-8"
    }
}