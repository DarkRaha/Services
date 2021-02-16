package com.darkraha.services

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.job.JobState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DeferredTest {


    @Test
    fun addCallbacksPrepare(){


        val set = mutableSetOf<String>()

        val def = Deferred<Any, Any>()
        def.onBeforeStart {
            set.add("onBeforeStart")
        }.onCancel {
            set.add("onCancel")
        }.onError {
            set.add("onError")
        }.onSuccess {
            set.add("onSuccess")
        }.onProgress {
            set.add("onProgress")
        }.onFinish {
            set.add("onFinish")
        }

        def.dispatchCallbacks()
        assertTrue(set.isEmpty())

        def.job.state.set(JobState.PENDING)
        def.dispatchCallbacks()
        assertTrue(set.size==1)

        set.clear()
        def.job.state.set(JobState.SUCCESS)
        def.dispatchCallbacks()
        assertTrue(set.size==2)
        assertTrue("onSuccess" in  set)
        assertTrue("onFinish" in  set)

        set.clear()
        def.job.state.set(JobState.ERROR)
        def.dispatchCallbacks()
        assertTrue(set.size==2)
        assertTrue("onError" in  set)
        assertTrue("onFinish" in  set)

        set.clear()
        def.job.state.set(JobState.CANCELED)
        def.dispatchCallbacks()
        assertTrue(set.size==2)
        assertTrue("onCancel" in  set)
        assertTrue("onFinish" in  set)
    }



    @Test
    fun addCallbacksPending(){


        val set = mutableSetOf<String>()

        val def = Deferred<Any, Any>()
        def.job.state.set(JobState.PENDING)
        var tst = false


        def.onBeforeStart {
            tst = true
            set.add("onBeforeStart")
        }
        assertTrue(tst)
        assertTrue(set.size==1)

        def.onCancel {
            set.add("onCancel")
        }.onError {
            set.add("onError")
        }.onSuccess {
            set.add("onSuccess")
        }.onProgress {
            set.add("onProgress")
        }.onFinish {
            set.add("onFinish")
        }

        set.clear()
        def.dispatchCallbacks()
        assertTrue(set.size==0)

        set.clear()
        def.job.state.set(JobState.SUCCESS)
        def.dispatchCallbacks()
        assertTrue(set.size==2)
        assertTrue("onSuccess" in  set)
        assertTrue("onFinish" in  set)

        set.clear()
        def.job.state.set(JobState.ERROR)
        def.dispatchCallbacks()
        assertTrue(set.size==2)
        assertTrue("onError" in  set)
        assertTrue("onFinish" in  set)

        set.clear()
        def.job.state.set(JobState.CANCELED)
        def.dispatchCallbacks()
        assertTrue(set.size==2)
        assertTrue("onCancel" in  set)
        assertTrue("onFinish" in  set)
    }



    @Test
    fun addCallbacksSuccess(){


        val set = mutableSetOf<String>()

        val def = Deferred<Any, Any>()
        def.job.state.set(JobState.SUCCESS)

        def.onBeforeStart {
            println("before start")
            set.add("onBeforeStart")
        }

        assertTrue(set.size==0)
        set.clear()

        def.onCancel {
            set.add("onCancel")
        }
        assertTrue(set.size==0)
        set.clear()

        def.onError {
            set.add("onError")
        }
        assertTrue(set.size==0)
        set.clear()

        def.onSuccess {
            set.add("onSuccess")
        }
        assertTrue(set.size==1)
        set.clear()

        def.onFinish {
            set.add("onFinish")
        }

        assertTrue(set.size==1)
        set.clear()

        set.clear()
        def.dispatchCallbacks()
        assertTrue(set.size==0)
    }





    @Test
    fun addCallbacksCanceled(){


        val set = mutableSetOf<String>()

        val def = Deferred<Any, Any>()
        def.job.state.set(JobState.CANCELED)

        def.onBeforeStart {
            set.add("onBeforeStart")
        }

        assertTrue(set.size==0)
        set.clear()

        def.onCancel {
            set.add("onCancel")
        }
        assertTrue(set.size==1)
        set.clear()

        def.onError {
            set.add("onError")
        }
        assertTrue(set.size==0)
        set.clear()

        def.onSuccess {
            set.add("onSuccess")
        }
        assertTrue(set.size==0)
        set.clear()

        def.onFinish {
            set.add("onFinish")
        }
        assertTrue(set.size==1)
        set.clear()

        set.clear()
        def.dispatchCallbacks()
        assertTrue(set.size==0)
    }


    @Test
    fun addCallbacksError(){


        val set = mutableSetOf<String>()

        val def = Deferred<Any, Any>()
        def.job.state.set(JobState.ERROR)

        def.onBeforeStart {
            set.add("onBeforeStart")
        }

        assertTrue(set.size==0)
        set.clear()

        def.onCancel {
            set.add("onCancel")
        }
        assertTrue(set.size==0)
        set.clear()

        def.onError {
            set.add("onError")
        }
        assertTrue(set.size==1)
        set.clear()

        def.onSuccess {
            set.add("onSuccess")
        }
        assertTrue(set.size==0)
        set.clear()

        def.onFinish {
            set.add("onFinish")
        }
        assertTrue(set.size==1)
        set.clear()

        set.clear()
        def.dispatchCallbacks()
        assertTrue(set.size==0)
    }



}


