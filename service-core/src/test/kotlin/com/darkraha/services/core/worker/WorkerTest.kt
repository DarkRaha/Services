package com.darkraha.services.core.worker

import com.darkraha.services.core.IsCallbackCalled
import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.MutableProgressData
import com.darkraha.services.core.job.Task
import com.darkraha.services.core.service.Service
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException

internal class WorkerTest {


    lateinit var isCallbackCalled: IsCallbackCalled
    lateinit var deferred: Deferred<Unit, String>
    var isOnTask = false

    @BeforeEach
    fun onBeforeEach() {
        isOnTask = false
        isCallbackCalled = IsCallbackCalled()
        deferred = Deferred(String::class.java)
        deferred.worker = Worker(Service.newExecutorService(5))

        deferred.job.tasks.main = object : Task<Unit>() {
            override fun onTask(params: Unit?, workerActions: WorkerActions<*>, jobResponse: JobResponse<*>) {
                isOnTask = true
            }
        }
    }

    fun addCallbacks() {
        deferred
            .onBeforeStart {
                println("onBeforeStart")
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
               println("onSuccess")
                isCallbackCalled.onSuccess = true
            }.onFinish {
                isCallbackCalled.onFinish = true
            }

    }


    @Test
    fun testWorkflowSuccess() {
        addCallbacks()
        deferred.sync()
        assertTrue(isCallbackCalled.onPending == true)
        assertTrue(isCallbackCalled.onSuccess == true)
        assertTrue(isCallbackCalled.onFinish == true)
        assertTrue(isCallbackCalled.onCancel == false)
        assertTrue(isCallbackCalled.onError == false)
        assertTrue(isCallbackCalled.onProgress == false)
    }


    @Test
    fun testWorkflowSuccessProgess() {
        deferred.job.tasks.main = object : Task<Unit>() {
            override fun onTask(
                params: Unit?,
                workerActions: WorkerActions<*>,
                jobResponse: JobResponse<*>
            ) {
                workerActions.notifyProgress(MutableProgressData())
            }
        }
        addCallbacks()
        deferred.sync()
        assertTrue(isCallbackCalled.onPending == true)
        assertTrue(isCallbackCalled.onSuccess == true)
        assertTrue(isCallbackCalled.onFinish == true)
        assertTrue(isCallbackCalled.onCancel == false)
        assertTrue(isCallbackCalled.onError == false)
        assertTrue(isCallbackCalled.onProgress == true)
    }

    @Test
    fun testWorkflowError() {
        deferred.job.tasks.main = object : Task<Unit>() {
            override fun onTask(
                params: Unit?,
                workerActions: WorkerActions<*>,
                jobResponse: JobResponse<*>
            ) {
                workerActions.setError(IllegalStateException("test error"))
            }
        }
        addCallbacks()
        deferred.sync()
        assertTrue(isCallbackCalled.onPending == true)
        assertTrue(isCallbackCalled.onSuccess == false)
        assertTrue(isCallbackCalled.onFinish == true)
        assertTrue(isCallbackCalled.onCancel == false)
        assertTrue(isCallbackCalled.onError == true)
        assertTrue(isCallbackCalled.onProgress == false)
        var cberror = false
        deferred.onError {
            assertTrue(it.getError()!!::class.java == IllegalStateException::class.java)
            cberror = true
        }
        assertTrue(cberror)
    }


    @Test
    fun testWorkflowCancel() {
        deferred.job.tasks.main = object : Task<Unit>() {
            override fun onTask(
                params: Unit?,
                workerActions: WorkerActions<*>,
                jobResponse: JobResponse<*>
            ) {
                workerActions.setReject("test canceled")
            }
        }
        addCallbacks()
        deferred.sync()
        assertTrue(isCallbackCalled.onPending == true)
        assertTrue(isCallbackCalled.onSuccess == false)
        assertTrue(isCallbackCalled.onFinish == true)
        assertTrue(isCallbackCalled.onCancel == true)
        assertTrue(isCallbackCalled.onError == false)
        assertTrue(isCallbackCalled.onProgress == false)
        var cbcanceled = false
        deferred.onCancel() {
            cbcanceled = true
        }
        assertTrue(cbcanceled)
    }


}