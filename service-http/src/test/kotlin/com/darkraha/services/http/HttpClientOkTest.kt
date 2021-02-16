package com.darkraha.services.http

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File


internal class HttpServiceOkTest {


    @Test
    fun downloadFile() {
        var file: File? = null

        HttpServiceOk.newInstance().downloadFile("https://publicobject.com/helloworld.txt")
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
        HttpServiceOk.newInstance().downloadFile("https://publicobject.com/helloworld.txt")
            .onProgress { it, jr ->
                got = true
                println("read ${it.current} total ${it.total}")
            }.onSuccess {
                it.getResult()!!.delete()
            }.await()

        assertTrue(got)
    }


    @Test
    fun donwloadString() {
        var got = false
        HttpServiceOk.newInstance().download("https://publicobject.com/helloworld.txt")
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
        HttpServiceOk.newInstance().httpRequest("https://publicobject.com/helloworld.txt").build(String::class.java)
            .onSuccess {
                got = true
                println("donwloadString: ${it.getResult()}")
            }.sync()
        assertTrue(got)
    }

    @Test
    fun tmpTest() {
        val srv = HttpServiceOk.newInstance()

        var str: String? = null


        var result: String? = null
        srv.get().onProgress { pd, jobResponse ->
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