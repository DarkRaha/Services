package com.darkraha.services.core.worker.executors

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TPoolWorkerExecutorTest {

    @Test
    fun testExecute() {
        val exe = TPoolWorkerExecutor(5)

        val f1 = exe.execute {
        }
        assertTrue(f1 != null)
        f1!!.get()

        println("name=${exe.prefix}")
       var tName1: String?=null
        var tName2: String?=null

        val f2  =exe.execute {
            tName1 = Thread.currentThread().name

            exe.execute {
                tName2 = Thread.currentThread().name
            }
        }

        assertTrue(f2!=null)
        f2!!.get()

        println("name1=${tName1} name2=${tName2}")
        assertTrue(tName1==tName2)
    }

}