package com.darkraha.services.core.worker

import com.darkraha.services.core.IsCallbackCalled
import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.job.Job
import com.darkraha.services.core.job.JobState
import com.darkraha.services.core.service.Service
import com.darkraha.services.core.utils.Common
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

internal class WorkerHelperTest {

    val states = listOf(
        JobState.PENDING, JobState.SUCCESS,
        JobState.ERROR, JobState.CANCELED,
        JobState.FINISHED
    )

    val statesFinishes = listOf(
        JobState.SUCCESS, JobState.ERROR, JobState.CANCELED,

        )

    lateinit var isCallbackCalled: IsCallbackCalled
    lateinit var deferred: Deferred<Unit, String>

    @BeforeEach
    fun onBeforeEach() {
        isCallbackCalled = IsCallbackCalled()
        deferred = Deferred(String::class.java)
        val worker = Worker(Service.newExecutorService(5))
        deferred.worker = worker
        deferred.workerHelper = worker.newHelper(deferred)
    }

    fun addCallbacks() {
        deferred
            .onBeforeStart {
                isCallbackCalled.onPending = true
            }
            .onError {
                isCallbackCalled.onError = true
            }
            .onCancel {
                isCallbackCalled.onCancel = true
            }
            .onProgress {
                isCallbackCalled.onProgress = true
            }
            .onSuccess {
                isCallbackCalled.onSuccess = true
            }.onFinish {
                isCallbackCalled.onFinish = true
            }

    }


    @Test
    fun dispatchCallbacks() {
        deferred = Deferred(String::class.java)
        val worker = Worker(Service.newExecutorService(5))
        deferred.worker = worker
        deferred.workerHelper = worker.newHelper(deferred)
        addCallbacks()
        assertTrue(deferred.job.callbacks?.size == 6)

        statesFinishes.forEach {
            deferred.job.state.set(it)
            deferred.workerHelper.dispatchCallbacks()
            assertTrue(isCallbackCalled.onFinish == true)
            assertTrue(isCallbackCalled.isState(it))
            isCallbackCalled.resetState(it)
            isCallbackCalled.onFinish = false
            assertTrue(isCallbackCalled.isAllFalse())
        }
    }
}