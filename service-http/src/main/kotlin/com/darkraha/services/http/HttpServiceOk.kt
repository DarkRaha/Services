package com.darkraha.services.http

import com.darkraha.services.core.deferred.UserDeferred
import com.darkraha.services.core.job.Task
import com.darkraha.services.core.service.TypedService
import com.darkraha.services.core.utils.json.JsonConverter
import com.darkraha.services.core.utils.json.JsonConverterA
import com.darkraha.services.core.worker.Worker
import okhttp3.*
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Wrapper for OkHttpClient.
 *
 * @author Rahul Verma
 */
open class HttpServiceOk protected constructor() : TypedService<Request>(Request::class.java), HttpClient {

    var httpClient: OkHttpClient? = null
        protected set
    var jsonConverter: JsonConverterA? = null
        protected set

    var userAgent = ""

    /**
     * common headers
     */
    val headers = mutableMapOf<String, String>()

    override fun checkSetup(): Boolean {
        return defaultWorker != null && httpClient != null && jsonConverter != null && defaultTask != null
    }

    override fun setupDefault() {
        httpClient = httpClient ?: newClient()
        jsonConverter = jsonConverter ?: JsonConverter()
        defaultTask = defaultTask ?: HttpClientOkTask(httpClient!!, jsonConverter!!)

        super.setupDefault()
    }


    fun newClient(): OkHttpClient = OkHttpClient.Builder()
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

    //---------------------------------------------------------
    override fun <RESULT> httpRequest(
        url: String,
        clsResult: Class<RESULT>
    )
            : DeferredHttpRequestBuilder<RESULT> {
        return DeferredHttpRequestBuilder(newDeferred(Request::class.java, clsResult)).url(url).apply {
            if (userAgent.isNotEmpty()) {
                addHeader("User-Agent", userAgent)
            }

            headers.forEach {
                addHeader(it.key, it.value)
            }
        }
    }


    override fun download(url: String, params: List<Pair<String, Any>>?): UserDeferred<String> {
        return httpRequest(url, String::class.java)
            .addStringParams(params)
            .build()
            .async()
    }

    override fun downloadFile(url: String, file: File?): UserDeferred<File> {
        val lFile: File = file ?: run {
            File(File(URI(url).getPath()).name)
        }

        return httpRequest(url, File::class.java).build().setResultFile(lFile).async()
    }

    override fun downloadByteArray(url: String): UserDeferred<ByteArray> {
        return httpRequest(url, ByteArray::class.java).build().async()
    }

    override fun postForm(url: String, params: List<Pair<String, Any>>?): UserDeferred<String> {
        return httpRequest(url, String::class.java).addStringParams(params).method("POST").build().async()
    }

    override fun uploadFile(
        url: String,
        file: File,
        fileParamName: String,
        mimetype: String,
        params: List<Pair<String, Any>>?
    ): UserDeferred<String> {
        return httpRequest(url, String::class.java).addFileParam(fileParamName, file, mimetype)
            .addStringParams(params)
            .build().async()
    }

    override fun <POST, RESPONSE> restApi(
        url: String,
        cls: Class<RESPONSE>,
        obj: POST?,
        method: String
    ): UserDeferred<RESPONSE> {
        val builder = httpRequest(url, cls)
            .method(method)

        if (obj != null) {
            builder.setBody(jsonConverter!!.toJson(obj), JSON)
        }

        return builder.build().async()
    }

    class Builder {
        private val httpSrv = HttpServiceOk()
        fun httpClient(c: OkHttpClient): Builder = this.apply { httpSrv.httpClient = c }
        fun defaultWorker(w: Worker): Builder = this.apply { httpSrv.defaultWorker = w }
        fun defaultMainTask(t: Task<Request>?) = this.apply { httpSrv.defaultTask = t }
        fun defaultPreProcessors(p: List<Task<Request>>?) = this.apply { httpSrv.defaultPreProcessors = p }
        fun defaultPostProcessors(p: List<Task<Request>>?) = this.apply { httpSrv.defaultPostProcessors = p }
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