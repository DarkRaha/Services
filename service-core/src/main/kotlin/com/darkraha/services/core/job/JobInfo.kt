package com.darkraha.services.core.job

class JobInfo {
    var id: Long = 0
    var cmd: String? = null
    var idObject: Any? = null
    var timeStart: Long = 0
    var timeEnd: Long = 0
}

interface JobInfoBuilder{
    fun id(v: Long): JobInfoBuilder
    fun cmd(v: String?): JobInfoBuilder
    fun idObject(v: Any?): JobInfoBuilder
}

