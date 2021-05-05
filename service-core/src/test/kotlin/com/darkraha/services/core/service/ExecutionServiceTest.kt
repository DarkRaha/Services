package com.darkraha.services.core.service

import com.darkraha.services.core.IsCallbackCalled
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ExecutionServiceTest {


    @Test
    fun chainingWithResult() {
        val srv = ExecutionService()
        var executed1 = false
        var executed2 = false
        var onsuccess = false
        var p: Any? = null

        srv.exe {
            Thread.sleep(2000)
            executed1 = true
            2
        }.append(srv.prepareExeP {
            Thread.sleep(2000)
            executed2 = true
            p = it
        }).setIniNextParam { response, params, builder ->
            return@setIniNextParam response.getResult() as Int * 2
        }.build().onSuccess {
            onsuccess = true
        }.await()

        assertTrue(executed1)
        assertTrue(executed2)
        assertTrue(onsuccess)
        assertTrue(p is Int && p == 4)

    }

    @Test
    fun chainingEmpty() {
        val srv = ExecutionService()
        var executed1 = false
        var executed2 = false
        var onsuccess = false

        srv.exe {
            Thread.sleep(2000)
            executed1 = true
        }.append(srv.prepareExe {
            Thread.sleep(2000)
            executed2 = true
        }).build().onSuccess {
            onsuccess = true
        }.await()

        assertTrue(executed1)
        assertTrue(executed2)
        assertTrue(onsuccess)
    }

    @Test
    fun exeUnit() {
        val isCallbackCalled = IsCallbackCalled()
        var executed = false

        ExecutionService().exe {
            Thread.sleep(2000)
            executed = true
        }.onSuccess {
            isCallbackCalled.onSuccess = true
        }.onFinish {
            isCallbackCalled.onFinish = true
        }.await()

        assertTrue(executed)
        assertTrue(isCallbackCalled.onFinish)
        assertTrue(isCallbackCalled.onSuccess)
    }

    @Test
    fun exeDouble() {
        val isCallbackCalled = IsCallbackCalled()
        var executed = false

        var d = 0.0

        val acton = ExecutionService().exe {
            Thread.sleep(2000)
            executed = true
            return@exe 2.0
        }

        acton.onSuccess {
            isCallbackCalled.onSuccess = true
            d = it.getResult()!!
        }.onFinish {
            isCallbackCalled.onFinish = true
        }.await()


        assertTrue(executed)
        assertTrue(isCallbackCalled.onFinish)
        assertTrue(isCallbackCalled.onSuccess)
        assertTrue(d == 2.0)
    }

    @Test
    fun exeWithRunnable() {
        val isCallbackCalled = IsCallbackCalled()
        var executed = false
        val runnable = Runnable {
            Thread.sleep(2000)
            executed = true
        }

        ExecutionService().exe(runnable).onSuccess {
            isCallbackCalled.onSuccess = true
        }.onFinish {
            isCallbackCalled.onFinish = true
        }.await()

        assertTrue(executed)
        assertTrue(isCallbackCalled.onFinish)
        assertTrue(isCallbackCalled.onSuccess)
    }

}