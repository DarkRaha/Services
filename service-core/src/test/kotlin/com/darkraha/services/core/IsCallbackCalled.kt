package com.darkraha.services.core

import com.darkraha.services.core.job.JobState

class IsCallbackCalled {
    var onPending = false
    var onSuccess = false
    var onCancel = false
    var onError = false
    var onFinish = false
    var onProgress = false

    fun isAllFalse() = !(onPending || onSuccess || onError || onCancel || onFinish || onProgress)
    fun isState(s: JobState): Boolean {
        return when (s) {
            JobState.PENDING -> onPending
            JobState.SUCCESS -> onSuccess
            JobState.ERROR -> onError
            JobState.CANCELED -> onCancel
            JobState.FINISHED -> onFinish
            JobState.PROGRESS -> onProgress
            JobState.PREPARE -> false
        }

    }

    override fun toString(): String {
        return "onPending=$onPending , onSuccess=$onSuccess , onError=$onError , onCanceled=$onCancel , "+
                "onFinish=$onFinish , onProgress=$onProgress "
    }
    fun resetState(s: JobState) {
        when (s) {
            JobState.PENDING -> onPending = false
            JobState.SUCCESS -> onSuccess = false
            JobState.ERROR -> onError = false
            JobState.CANCELED -> onCancel = false
            JobState.FINISHED -> onFinish = false
            JobState.PROGRESS -> onProgress = false
            JobState.PREPARE -> {
            }
        }
    }


    fun reset() {
        onPending = false
        onSuccess = false
        onCancel = false
        onError = false
        onFinish = false
    }
}
