package com.darkraha.services.endecodefiles.encoder

import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.Task
import com.darkraha.services.core.worker.WorkerActions

class FileEncoderTask: Task<FileEncoderParams>() {
    override fun onTask(params: FileEncoderParams?, workerActions: WorkerActions<*>, jobResponse: JobResponse<*>) {
        params!!.also { p->
           // p.encoder.
        }
    }
}