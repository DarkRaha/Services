package com.darkraha.services.http


import com.darkraha.services.core.deferred.DeferredFactory
import com.darkraha.services.core.deferred.DeferredServiceBuilder
import com.darkraha.services.core.deferred.DeferredUserCallbacks
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.InputStream
import java.net.URL


/**
 * @author Rahul Verma
 */
class DeferredHttpRequestBuilder(val deferredFactory: DeferredFactory<Request>) : HttpRequestBuilder {
    private val builder = Request.Builder()
    private var mMethod: String? = null
    private var mStrBody: String? = null
    private var mFileBody: File? = null
    private var mFileParam: File? = null
    private var mFileNameParam: String? = null
    private var mMimetype: String? = null
    private var mUrl: String? = null
    private var params: ArrayList<Pair<String, String>>? = null
    private var mProgressRequestBody: ProgressRequestBody? = null

    override fun url(url: String): DeferredHttpRequestBuilder = apply {
        mUrl = url
        builder.url(url)
    }

    override fun url(url: URL): DeferredHttpRequestBuilder = apply {
        builder.url(url)
    }

    override fun method(method: String): DeferredHttpRequestBuilder = apply {
        this.mMethod = method
    }

    override fun addHeader(name: String, value: String): DeferredHttpRequestBuilder = apply {
        builder.addHeader(name, value)
    }

    override fun addStringParam(name: String, value: Any?): DeferredHttpRequestBuilder = apply {
        value?.also {
            (params ?: ArrayList()).apply {
                params = this
                add(Pair(name, it.toString()))
            }
        }
    }

    override fun addFileParam(name: String, file: File, mimetype: String): DeferredHttpRequestBuilder = apply {
        if (mFileBody != null || mStrBody != null) {
            throw IllegalStateException("Can not create multipart body, another body already assigned by body() method")
        }

        if (mFileParam != null) {
            throw IllegalStateException("Only one file supported")
        }

        mFileParam = file
        mFileNameParam = name
        this.mMimetype = mimetype
    }

    override fun setBody(body: File, mimetype: String): DeferredHttpRequestBuilder = apply {
        if (mFileBody != null || mStrBody != null || mFileParam != null) {
            throw IllegalStateException("Another body already assigned by body() or addFileParam() method")
        }
        mFileBody = body
        this.mMimetype = mimetype
    }

    override fun setBody(body: InputStream, mimetype: String): DeferredHttpRequestBuilder = apply {
        TODO("Not yet implemented")
    }

    override fun setBody(body: String, mimetype: String): DeferredHttpRequestBuilder = apply {
        if (mFileBody != null || mStrBody != null || mFileParam != null) {
            throw IllegalStateException("Another body already assigned by body() or addFileParam() method")
        }
        mStrBody = body
        this.mMimetype = mimetype
    }

    override fun addCookie(cookie: Any): DeferredHttpRequestBuilder = apply {
        TODO("Not yet implemented")
    }


    override fun addStringParams(params: List<Pair<String, Any?>>?): DeferredHttpRequestBuilder = apply {
        params?.forEach { addStringParam(it.first, it.second) }
    }

    override fun addStringParams(params: Map<String, Any?>?): DeferredHttpRequestBuilder = apply {
        params?.toList()?.forEach { addStringParam(it.first, it.second) }
    }

    override fun addCookie(name: String, value: String): DeferredHttpRequestBuilder = apply {
        addHeader("Cookie", "${name}=${value}")
    }

    override fun <RESULT> build(cls: Class<RESULT>): DeferredServiceBuilder<RESULT> {
        return deferredFactory.newDeferred(cls).apply {
            job.params = buildRequest()
            mProgressRequestBody?.jobNotifyProgress = this
        }
    }

    override fun <RESULT> async(cls: Class<RESULT>): DeferredUserCallbacks<RESULT> {
        return build(cls).async()
    }

    override fun <RESULT> sync(cls: Class<RESULT>): DeferredUserCallbacks<RESULT> {
        return build(cls).sync()
    }

    //------------------------------------------------------------------------------
    private fun buildPostRequestBody(): RequestBody = when {
        // post file as multipart
        mFileParam != null -> {
            MultipartBody.Builder().apply {
                params?.forEach {
                    addFormDataPart(it.first, it.second)
                }
            }.build()
        }

        // post file
        mFileBody != null -> ProgressRequestBody(mFileBody!!, mMimetype!!).apply {
            mProgressRequestBody = this
        }// mFileBody!!.asRequestBody(mMimetype?.toMediaType())

        // post string like JSON
        mStrBody != null -> mStrBody!!.toRequestBody(mMimetype?.toMediaType())

        // html form URL encoded
        else -> {
            FormBody.Builder().apply {
                params?.forEach {
                    add(it.first, it.second)
                }
            }.build()
        }
    }


    private fun buildRequest(): Request {
        if (mStrBody != null || mFileBody != null || mFileParam != null) {
            if (mMethod == "GET") {
                throw IllegalStateException("POST like method is expected")
            }

            if (mMethod == null) {
                mMethod = "POST"
            }

            builder.method(mMethod!!, buildPostRequestBody())
            return builder.build()
        }

        if (mMethod == null) {
            mMethod = "GET"
        }

        params?.apply {
            mUrl!!.toHttpUrl().newBuilder().apply {
                forEach {
                    addQueryParameter(it.first, it.second)
                }
                builder.url(build())
            }
        }
        return builder.build()
    }

}


