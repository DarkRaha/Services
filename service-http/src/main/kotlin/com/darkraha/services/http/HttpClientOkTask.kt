package com.darkraha.services.http

import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.Task
import com.darkraha.services.core.utils.FileUtils

import com.darkraha.services.core.utils.json.JsonConverterA
import com.darkraha.services.core.worker.WorkerActions
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

class HttpClientOkTask(
    private val httpClient: OkHttpClient,
    private val jsonConverter: JsonConverterA
) : Task<Request>() {


    override fun onTask(
        params: Request?,
        workerActions: WorkerActions<*>,
        jobResponse: JobResponse<*>
    ) {
        httpClient.newCall(params!!).execute().use { httpResponse ->
            workerActions.result.rawResult = httpResponse

            if (!httpResponse.isSuccessful) throw IOException("Unexpected code $httpResponse")

            httpResponse.body.takeIf { httpResponse.body is ProgressResponseBody }?.apply {
                this as ProgressResponseBody
                jobNotifyProgress = workerActions
            }

            workerActions.apply {
                result.mimetype = httpResponse.body?.contentType()?.toString()

                when (result.clsTarget) {
                    String::class.java -> result.tmpResult = httpResponse.body?.string()
                    ByteArray::class.java -> result.tmpResult = httpResponse.body?.bytes()
                    File::class.java -> try {
                        val file = when {
                            result.file?.isDirectory ?: false -> {
                                FileUtils.genFile(
                                    params.url.toString(),
                                    result.mimetype,
                                    result.file
                                )
                            }
                            result.file?.isFile ?: false
                                    || 0 < result.file?.extension?.length ?: 0
                            -> result.file!!
                            else -> {
                                FileUtils.genFile(
                                    params.url.toString(), result.mimetype, null
                                )
                            }
                        }

                        file.apply {
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
                        setError(e)
                    }
                    Unit::class.java -> result.tmpResult = Unit
                    else -> {
                        result.tmpResult = jsonConverter.fromJson(httpResponse.body!!.string(), result.clsTarget!!)
                    }
                }
            }
        }
    }

}