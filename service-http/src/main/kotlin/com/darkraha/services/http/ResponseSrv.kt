package com.darkraha.services.http

open class ResponseSrv<T> {
    var time: Long = 0
    var result: String? = null
    var code: Int = 0
    var message: String? = null
    var data: T? = null

    val isSuccess: Boolean
        get() = "success" == result

    val isError: Boolean
        get() = "error" == result
}