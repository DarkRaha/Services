package com.darkraha.services.http

import com.darkraha.services.core.deferred.DeferredUserCallbacks
import java.io.File

interface HttpService {
    fun download(url: String, params: List<Pair<String, Any>>? = null): DeferredUserCallbacks<String>
    fun downloadFile(url: String, file: File? = null): DeferredUserCallbacks<File>
    fun downloadByteArray(url: String): DeferredUserCallbacks<ByteArray>
    fun postForm(url: String, params: List<Pair<String, Any>>?): DeferredUserCallbacks<String>
    fun uploadFile(
        url: String, file: File, fileParamName: String,
        mimetype: String,
        params: List<Pair<String, Any>>? = null
    ): DeferredUserCallbacks<String>

    fun <POST, RESPONSE> restApi(
        url: String,
        cls: Class<RESPONSE>,
        obj: POST?,
        method: String = "GET"
    ): DeferredUserCallbacks<RESPONSE>

    fun <RESPONSE> restApiGet(url: String, cls: Class<RESPONSE>):
            DeferredUserCallbacks<RESPONSE> {
        return restApi(url, cls, null, "GET")
    }

    fun <POST, RESPONSE> restApiPost(url: String, cls: Class<RESPONSE>, obj: POST? = null):
            DeferredUserCallbacks<RESPONSE> {
        return restApi(url, cls, obj, "POST")
    }

    fun <POST, RESPONSE> restApiPut(url: String, cls: Class<RESPONSE>, obj: POST? = null):
            DeferredUserCallbacks<RESPONSE> {
        return restApi(url, cls, obj, "PUT")
    }

    fun <POST, RESPONSE> restApiDel(url: String, cls: Class<RESPONSE>, obj: POST? = null):
            DeferredUserCallbacks<RESPONSE> {
        return restApi(url, cls, obj, "DEL")
    }

    fun <POST, RESPONSE> restApiUpdate(url: String, cls: Class<RESPONSE>, obj: POST? = null):
            DeferredUserCallbacks<RESPONSE> {
        return restApi(url, cls, obj, "UPDATE")
    }

    fun httpRequest(url: String): HttpRequestBuilder

}