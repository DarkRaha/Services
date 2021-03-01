package com.darkraha.services.core.job

import java.io.File
import java.io.InputStream

/**
 * @author Rahul Verma
 */
interface JobResponse<RESULT> {
    fun getFile(): File?
    fun getUrl(): String?
    fun getStream(): InputStream?
    fun getResult(): RESULT?
    fun getMimetype(): String?
    fun getReason(): String?
    fun getError(): Exception?
    fun getState(): JobState
    fun getId(): Long
    fun getIdObject(): Any?
    fun getCmd(): String?
    fun getTimeStart(): Long
    fun getTimeEnd(): Long
    fun getPlugin(name:String): Plugin<RESULT>?
    fun getProgressData(): ProgressData?
}
