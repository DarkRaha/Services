package com.darkraha.services.http


import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.deferred.DeferredServiceBuilder
import com.darkraha.services.core.deferred.UserDeferred
import java.io.File
import java.io.InputStream
import java.net.URL

interface HttpRequestBuilder<RESULT> {

    fun url(url: String): HttpRequestBuilder<RESULT>
    fun url(url: URL): HttpRequestBuilder<RESULT>
    fun method(method: String = "GET"): HttpRequestBuilder<RESULT>
    fun addHeader(name: String, value: String): HttpRequestBuilder<RESULT>
    fun addStringParam(name: String, value: Any?): HttpRequestBuilder<RESULT>
    fun addFileParam(name: String, file: File, mimetype: String): HttpRequestBuilder<RESULT>
    fun setBody(body: String, mimetype: String): HttpRequestBuilder<RESULT>
    fun setBody(body: File, mimetype: String): HttpRequestBuilder<RESULT>
    fun setBody(body: InputStream, mimetype: String): HttpRequestBuilder<RESULT>
    fun addCookie(cookie: Any): HttpRequestBuilder<RESULT>

    fun build(): DeferredServiceBuilder<*, RESULT>
    fun sync(): UserDeferred<RESULT>
    fun async(): UserDeferred<RESULT>

    fun addStringParams(params: List<Pair<String, Any?>>?): HttpRequestBuilder<RESULT> = apply {
        params?.forEach { addStringParam(it.first, it.second) }
    }

    fun addStringParams(params: Map<String, Any?>?): HttpRequestBuilder<RESULT> = apply {
        params?.toList()?.forEach { addStringParam(it.first, it.second) }
    }

    fun addCookie(name: String, value: String) = apply {
        addHeader("Cookie", "${name}=${value}")
    }
}



