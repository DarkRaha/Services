package com.darkraha.services.http

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File


internal class HttpClientOkTest {


    @Test
    fun downloadFile() {
        var file: File? = null

        HttpClientOk.newInstance().downloadFile("https://publicobject.com/helloworld.txt")
            .onSuccess {
                file = it.getResult()
            }.await()

        file!!.apply {
            assertTrue(exists())
            assertTrue(length() > 0)
            assertTrue("helloworld.txt" == name)
            assertTrue(delete())
        }
    }

    @Test
    fun downloadFileProgress() {

        var got = false
        HttpClientOk.newInstance().downloadFile("https://publicobject.com/helloworld.txt")
            .onProgress { it
                got = true
                val progressData = it.getProgressData()
                println("read ${progressData?.current} total ${progressData?.total}")
            }.onSuccess {
                it.getResult()!!.delete()
            }.await()

        assertTrue(got)
    }


    @Test
    fun donwloadString() {
        var got = false
        HttpClientOk.newInstance().download("https://publicobject.com/helloworld.txt")
            .onSuccess {
                got = true
                println("donwloadString: ${it.getResult()}")
                assertTrue(got)
            }.onError {
                println("donwloadString: error")
            }.await()

        assertTrue(got)
    }

    @Test
    fun donwloadStringSync() {
        var got = false
        HttpClientOk.newInstance().httpRequest("https://publicobject.com/helloworld.txt").build(String::class.java)
            .onSuccess {
                got = true
                println("donwloadString: ${it.getResult()}")
            }.sync()
        assertTrue(got)
    }

    @Test
    fun tmpTest() {
        val srv = HttpClientOk.newInstance()

        var str: String? = null


        var result: String? = null
        srv.get().onProgress {
            val pd = it.getProgressData()!!
            println("onProgress: ${pd.current} action: ${pd.action}")
        }.onSuccess {
            println("task success 1")
            result = it.getResult()
        }.sync().onFinish {
            println("task finished ${it.getResult()}")
        }.onSuccess {
            println("task success 2")

        }.onError {
            println("ERROR ")
        }


        assertTrue(result != null)
    }

}