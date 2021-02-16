package com.darkraha.services.http


import com.darkraha.services.core.job.JobNotifyProgress
import com.darkraha.services.core.job.MutableProgressData

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*


open class ProgressResponseBody(
    private val responseBody: ResponseBody,

    ) :
    ResponseBody() {
    private var bufferedSource: BufferedSource? = null

    var jobNotifyProgress: JobNotifyProgress? = null

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource =
        bufferedSource ?: source(responseBody.source()).buffer().apply { bufferedSource = this }


    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            val pd = MutableProgressData().apply {
                mAction = "download"
                mTotal = responseBody.contentLength()
            }

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.

                jobNotifyProgress?.apply {
                    pd.mCurrent += if (bytesRead != -1L) bytesRead else 0
                    notifyProgress(pd)
                }
                return bytesRead
            }
        }
    }
}