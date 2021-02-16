package com.darkraha.services.http

import com.darkraha.services.core.job.JobNotifyProgress
import com.darkraha.services.core.job.MutableProgressData
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.IOException


class ProgressRequestBody(
    private val mFile: File,
    private val mMimetype: String
) : RequestBody() {
    private val SEGMENT_SIZE = 1024L

    var jobNotifyProgress: JobNotifyProgress? = null


    override fun contentLength(): Long {
        return mFile.length()
    }

    override fun contentType(): MediaType? {
        return mMimetype.toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {

        mFile.source().use {
            var read: Long

            val pd = MutableProgressData().apply {
                mAction = "upload"
                mTotal = mFile.length()
            }

            while (it.read(sink.buffer(), SEGMENT_SIZE).also { read = it } != -1L) {
                pd.mCurrent += read
                sink.flush()
                jobNotifyProgress?.notifyProgress(pd)
            }
        }
    }
}

