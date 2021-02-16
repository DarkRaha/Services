package com.darkraha.services.http


import java.io.File
import java.io.InputStream
import java.net.URL

interface HttpRequestBuilder {

    fun url(url: String): HttpRequestBuilder
    fun url(url: URL): HttpRequestBuilder
    fun method(method: String = "GET"): HttpRequestBuilder
    fun addHeader(name: String, value: String): HttpRequestBuilder
    fun addStringParam(name: String, value: Any?): HttpRequestBuilder
    fun addFileParam(name: String, file: File, mimetype: String): HttpRequestBuilder
    fun setBody(body: String, mimetype: String): HttpRequestBuilder
    fun setBody(body: File, mimetype: String): HttpRequestBuilder
    fun setBody(body: InputStream, mimetype: String): HttpRequestBuilder
    fun addCookie(cookie: Any): HttpRequestBuilder

    fun <RESULT> build(cls: Class<RESULT>): Any
    fun <RESULT> sync(cls: Class<RESULT>): Any
    fun <RESULT> async(cls: Class<RESULT>): Any

    fun addStringParams(params: List<Pair<String, Any?>>?): HttpRequestBuilder = apply {
        params?.forEach { addStringParam(it.first, it.second) }
    }

    fun addStringParams(params: Map<String, Any?>?): HttpRequestBuilder = apply {
        params?.toList()?.forEach { addStringParam(it.first, it.second) }
    }

    fun addCookie(name: String, value: String) = apply {
        addHeader("Cookie", "${name}=${value}")
    }
}



