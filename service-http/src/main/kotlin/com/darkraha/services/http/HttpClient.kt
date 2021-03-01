package com.darkraha.services.http

import com.darkraha.services.core.deferred.UserDeferred
import java.io.File

interface HttpClient {
    fun download(url: String, params: List<Pair<String, Any>>? = null): UserDeferred<String>
    fun downloadFile(url: String, file: File? = null): UserDeferred<File>
    fun downloadByteArray(url: String): UserDeferred<ByteArray>
    fun postForm(url: String, params: List<Pair<String, Any>>?): UserDeferred<String>
    fun uploadFile(
        url: String, file: File, fileParamName: String,
        mimetype: String,
        params: List<Pair<String, Any>>? = null
    ): UserDeferred<String>

    fun <POST, RESPONSE> restApi(
        url: String,
        cls: Class<RESPONSE>,
        obj: POST?,
        method: String = "GET"
    ): UserDeferred<RESPONSE>

    fun <RESPONSE> restApiGet(url: String, cls: Class<RESPONSE>):
            UserDeferred<RESPONSE> {
        return restApi(url, cls, null, "GET")
    }

    fun <POST, RESPONSE> restApiPost(url: String, cls: Class<RESPONSE>, obj: POST? = null):
            UserDeferred<RESPONSE> {
        return restApi(url, cls, obj, "POST")
    }

    fun <POST, RESPONSE> restApiPut(url: String, cls: Class<RESPONSE>, obj: POST? = null):
            UserDeferred<RESPONSE> {
        return restApi(url, cls, obj, "PUT")
    }

    fun <POST, RESPONSE> restApiDel(url: String, cls: Class<RESPONSE>, obj: POST? = null):
            UserDeferred<RESPONSE> {
        return restApi(url, cls, obj, "DEL")
    }

    fun <POST, RESPONSE> restApiUpdate(url: String, cls: Class<RESPONSE>, obj: POST? = null):
            UserDeferred<RESPONSE> {
        return restApi(url, cls, obj, "UPDATE")
    }

    fun <RESULT> httpRequest(url: String, clsResult: Class<RESULT>): HttpRequestBuilder<RESULT>

}