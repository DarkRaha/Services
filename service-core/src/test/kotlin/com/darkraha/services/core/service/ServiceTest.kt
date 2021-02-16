package com.darkraha.services.core.service

import com.darkraha.services.core.worker.Worker
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ServiceTest {


    @Test
    fun syncTask() {
        var got = false
        val srv = Service.Builder<Any>()
            .mainWorker(Worker<Any>().onMainTask { p, action, response ->
                got = true
            })
            .build()


        srv.newDeferred(Any::class.java).sync()
        assertTrue(got)
    }

    @Test
    fun asyncTask() {
        var got = false

        val srv = Service.Builder<Any>()
            .mainWorker(Worker<Any>().onMainTask { p, action, response ->
                got = true

                Thread.sleep(5000)
                println("main task")
            })
            .build()

        srv.newDeferred(Any::class.java).async().await()
        assertTrue(got)
    }


}