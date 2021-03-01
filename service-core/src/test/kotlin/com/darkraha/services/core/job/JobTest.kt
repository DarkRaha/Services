package com.darkraha.services.core.job

import com.darkraha.services.core.IsCallbackCalled
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference

internal class JobTest {

    val states = listOf(
        JobState.PENDING, JobState.SUCCESS,
        JobState.ERROR, JobState.CANCELED,
        JobState.FINISHED
    )
    lateinit var job: Job<Unit,String>
    lateinit var isCalled: IsCallbackCalled



    @BeforeEach
    fun onBeforeEach() {
        job = Job(String::class.java)
        isCalled = IsCallbackCalled()
    }

    fun addCallbacks(owner: Any?) {
        job.apply{
            doAddCallback(JobState.PENDING, owner, false) { isCalled.onPending = true }
            doAddCallback(JobState.SUCCESS, owner, false) { isCalled.onSuccess = true }
            doAddCallback(JobState.ERROR, owner, false) { isCalled.onError = true }
            doAddCallback(JobState.CANCELED, owner, false) { isCalled.onCancel = true }
            doAddCallback(JobState.FINISHED, owner, false) { isCalled.onFinish = true }
        }
    }


    @Test
    fun testPrepareAddCallbacks() {
        addCallbacks(null)
        assertTrue(job.callbacks?.size == 5)
        assertTrue(isCalled.isAllFalse())
    }

    @Test
    fun testPendingAddCallbacks() {
        job.state.set(JobState.PENDING)
        addCallbacks(null)
        assertTrue(isCalled.onPending)
        assertTrue(job.callbacks?.size == 4)
        isCalled.onPending = false
        assertTrue(isCalled.isAllFalse())
    }


    @Test
    fun testSuccessAddCallbacks() {
        job.state.set(JobState.SUCCESS)
        addCallbacks(null)
        assertTrue(isCalled.onSuccess)
        assertTrue(isCalled.onFinish)
        assertTrue(job.callbacks == null)
        isCalled.onSuccess = false
        isCalled.onFinish = false
        assertTrue(isCalled.isAllFalse())
    }

    @Test
    fun testErrorAddCallbacks() {
        job.state.set(JobState.ERROR)
        addCallbacks(null)

        assertTrue(isCalled.onError)
        assertTrue(isCalled.onFinish)
        isCalled.onError = false
        isCalled.onFinish = false
        assertTrue(isCalled.isAllFalse())
        assertTrue(job.callbacks == null)
    }

    @Test
    fun testCancelAddCallbacks() {
        job.state.set(JobState.CANCELED)
        addCallbacks(null)
        assertTrue(isCalled.onCancel)
        assertTrue(isCalled.onFinish)
        assertTrue(job.callbacks  == null)
        isCalled.onCancel = false
        isCalled.onFinish = false
        assertTrue(isCalled.isAllFalse())
    }

    @Test
    fun testRemoveCallbacks() {
        val owner = "owner"
        addCallbacks(null)
        addCallbacks(owner)
       job.apply {
           assertTrue(callbacks?.size == 10)
           doRemoveCallbacks(owner)
           assertTrue(callbacks?.size == 5)
           assertTrue(callbacks!![0].owner == null)
           assertTrue(callbacks!![1].owner == null)
           assertTrue(callbacks!![2].owner == null)
           assertTrue(callbacks!![3].owner == null)
           assertTrue(callbacks!![4].owner == null)
       }
    }

    @Test
    fun testDoDispatchWeak() {
        val owner = WeakReference<Any>(null)
        addCallbacks(owner)
        states.forEach {
            job.state.set(it)
            job.doDispatchCallbacks(it)
            println(isCalled)
            assertTrue(isCalled.isAllFalse())
        }

    }

    @Test
    fun testDoDispatch() {
        addCallbacks(null)
        states.forEach {
            job.state.set(it)
            job.doDispatchCallbacks(it)
            assertTrue(isCalled.isState(it))
            isCalled.resetState(it)
            assertTrue(isCalled.isAllFalse())
        }
    }

    @Test
    fun testProgress() {
        job.doAddCallback(JobState.PROGRESS, null, false)
        { isCalled.onProgress = true }

        assertTrue(job.callbacks?.size==1)

        states.forEach{
            job.state.set(it)
            job.doDispatchCallbacks(JobState.PROGRESS)
            assertTrue(if(it==JobState.PENDING)
                isCalled.isState(JobState.PROGRESS) else isCalled.isAllFalse())
            isCalled.resetState(JobState.PROGRESS)
            assertTrue(isCalled.isAllFalse())
        }
    }



    //------------------------------------------------------------------------------
    @Test
    fun testPrepareToPending() {
        assertTrue(job.getState() == JobState.PREPARE)
        job.setPending()
        assertTrue(job.getState() == JobState.PENDING)
        assertTrue(job.info.timeStart > 0L)
        assertTrue(job.info.timeEnd == 0L)
    }

    @Test
    fun testPendingToPending() {
        job.setPending()
        val time = job.info.timeStart
        job.setPending()
        assertTrue(job.getState() == JobState.PENDING)
        assertTrue(job.info.timeStart == time)
    }

    @Test
    fun testPrepareToSuccess() {
        job.setSuccess()
        assertTrue(job.getState() == JobState.SUCCESS)
        assertTrue(job.info.timeEnd > 0L)
    }

    @Test
    fun testPendingToSuccess() {
        job.state.set(JobState.PENDING)
        job.setSuccess()
        assertTrue(job.getState() == JobState.SUCCESS)
        assertTrue(job.info.timeEnd > 0L)
    }

    @Test
    fun testSuccessToSuccess() {
        job.state.set(JobState.SUCCESS)
        job.setSuccess()
        assertTrue(job.getState() == JobState.SUCCESS)
        assertTrue(job.info.timeEnd == 0L)
    }

    @Test
    fun testErrorToSuccess() {
        job.state.set(JobState.ERROR)
        job.setSuccess()
        assertTrue(job.getState() == JobState.ERROR)
        assertTrue(job.info.timeEnd == 0L)
    }

    @Test
    fun testCanceledToSuccess() {
        job.state.set(JobState.CANCELED)
        job.setSuccess()
        assertTrue(job.getState() == JobState.CANCELED)
        assertTrue(job.info.timeEnd == 0L)
    }

    @Test
    fun testFinishedToSuccess() {
        job.state.set(JobState.FINISHED)
        job.setSuccess()
        assertTrue(job.getState() == JobState.FINISHED)
        assertTrue(job.info.timeEnd == 0L)
    }

    @Test
    fun testPrepareToError() {
        val reason = "reason"
        val error = IllegalArgumentException("error")
        assertTrue(job.getError() == null)
        assertTrue(job.getReason() == null)
        job.setError(error, reason)
        assertTrue(job.getState() == JobState.ERROR)
        assertTrue(job.info.timeEnd > 0L)
        assertTrue(job.getReason() == reason)
        assertTrue(job.getError() == error)
        assertTrue(job.getError()!!.message == "error")
    }

    @Test
    fun testPendingToError() {
        val reason = "reason"
        val error = IllegalArgumentException("error")
        assertTrue(job.getError() == null)
        assertTrue(job.getReason() == null)
        job.state.set(JobState.PENDING)
        job.setError(error, reason)
        assertTrue(job.getState() == JobState.ERROR)
        assertTrue(job.info.timeEnd > 0L)
        assertTrue(job.getReason() == reason)
        assertTrue(job.getError() == error)
        assertTrue(job.getError()!!.message == "error")
    }

    @Test
    fun testSuccessToError() {
        val reason = "reason"
        val error = IllegalArgumentException("error")
        assertTrue(job.getError() == null)
        assertTrue(job.getReason() == null)
        job.state.set(JobState.SUCCESS)
        job.setError(error, reason)
        assertTrue(job.getState() == JobState.SUCCESS)
        assertTrue(job.info.timeEnd == 0L)
        assertTrue(job.getReason() == null)
        assertTrue(job.getError() == null)

    }

    @Test
    fun testCanceledToError() {
        val reason = "reason"
        val error = IllegalArgumentException("error")
        assertTrue(job.getError() == null)
        assertTrue(job.getReason() == null)
        job.state.set(JobState.CANCELED)
        job.setError(error, reason)
        println("state ${job.getState()}")
        assertTrue(job.getState() == JobState.CANCELED)
        assertTrue(job.info.timeEnd == 0L)
        assertTrue(job.getReason() == null)
        assertTrue(job.getError() == null)
    }


    @Test
    fun testErrorToError() {
        val reason = "reason"
        val error = IllegalArgumentException("error")
        assertTrue(job.getError() == null)
        assertTrue(job.getReason() == null)
        job.state.set(JobState.ERROR)
        job.setError(error, reason)
        assertTrue(job.getState() == JobState.ERROR)
        assertTrue(job.info.timeEnd == 0L)
        assertTrue(job.getReason() == null)
        assertTrue(job.getError() == null)
    }

    @Test
    fun testPrepareToSuccessData() {
        val file = File("")
        val url = "url"
        val value = "result"
        job.setSuccess(value, file, url, null)
        assertTrue(job.getState() == JobState.SUCCESS)
        assertTrue(job.getFile() == file)
        assertTrue(job.getUrl() == url)
        assertTrue(job.getResult() == value)
    }

    @Test
    fun testPendingToSuccessData() {
        val file = File("")
        val url = "url"
        val value = "result"
        job.state.set(JobState.PENDING)
        job.setSuccess(value, file, url, null)
        assertTrue(job.getState() == JobState.SUCCESS)
        assertTrue(job.getFile() == file)
        assertTrue(job.getUrl() == url)
        assertTrue(job.getResult() == value)
    }

    @Test
    fun testSuccessToSuccessData() {
        val file = File("")
        val url = "url"
        val value = "result"
        job.state.set(JobState.SUCCESS)
        job.setSuccess(value, file, url, null)
        assertTrue(job.getState() == JobState.SUCCESS)
        assertTrue(job.getFile() == null)
        assertTrue(job.getUrl() == null)
        assertTrue(job.getResult() == null)
    }

    @Test
    fun testErrorToSuccessData() {
        val file = File("")
        val url = "url"
        val value = "result"
        job.state.set(JobState.ERROR)
        job.setSuccess(value, file, url, null)
        assertTrue(job.getState() == JobState.ERROR)
        assertTrue(job.getFile() == null)
        assertTrue(job.getUrl() == null)
        assertTrue(job.getResult() == null)
    }

    @Test
    fun testCanceledToSuccessData() {
        val file = File("")
        val url = "url"
        val value = "result"
        job.state.set(JobState.CANCELED)
        job.setSuccess(value, file, url, null)
        assertTrue(job.getState() == JobState.CANCELED)
        assertTrue(job.getFile() == null)
        assertTrue(job.getUrl() == null)
        assertTrue(job.getResult() == null)
    }
}