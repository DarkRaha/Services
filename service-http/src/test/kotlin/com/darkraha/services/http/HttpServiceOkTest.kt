package com.darkraha.services.http

import com.darkraha.services.core.job.ProgressData
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import okhttp3.mockwebserver.MockResponse

import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.io.TempDir


internal class HttpServiceOkTest {
    class ResponseSrvString : ResponseSrv<String>()

    val VAL_HELLO = "Hello World!"
    val URL_STRING = "/hello_world"
    val URL_STRING2 = "/hello_world2"
    val URL_API = "/api/action"

    lateinit var mockWebServer: MockWebServer
    lateinit var httpClient: HttpServiceOk
    lateinit var jsonResponse: String


    @BeforeEach
    fun beforeEach() {
        mockWebServer = MockWebServer()
        httpClient = HttpServiceOk.newInstance()
        jsonResponse = httpClient.jsonConverter!!.toJson(ResponseSrvString().apply {
            data = VAL_HELLO
        })
        val dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {

                when (request.path) {
                    URL_STRING -> return MockResponse()
                        .setResponseCode(200)
                        .addHeader("content-type: text/html; charset=utf-8")
                        .setBody(VAL_HELLO)
                    URL_STRING2 -> return MockResponse()
                        .setResponseCode(200)
                        .addHeader("content-type: text/plain; charset=utf-8")
                        .setBody(VAL_HELLO)
                    URL_API -> return MockResponse().setResponseCode(200)
                        .setBody(jsonResponse)
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                    "/v1/profile/info" -> return MockResponse().setResponseCode(200)
                        .setBody("{\\\"info\\\":{\\\"name\":\"Lucas Albuquerque\",\"age\":\"21\",\"gender\":\"male\"}}")
                }
                return MockResponse().setResponseCode(404)
            }
        }
        mockWebServer.dispatcher = dispatcher
        mockWebServer.start(8080)

    }

    @AfterEach
    fun afterEach() {
        mockWebServer.shutdown()
    }


    @Test
    fun testNewInstanceDefault() {
        assertTrue(httpClient.checkSetup())
    }


    @Test
    fun downloadFile() {
        var result: File? = null
        val url = mockWebServer.url(URL_STRING).toString()

        httpClient.downloadFile(url)
            .onSuccess {
                result = it.getResult()
            }.await().apply {
                assertTrue(getState().isSuccess())
            }

        result!!.apply {
            assertTrue(exists())
            assertTrue(length() > 0)
            println(name)
            assertTrue("hello_world.html" == name)
            assertTrue(delete())
        }
    }

    @Test
    fun downloadFileTo() {
        val result = File("hello.txt")
        val url = mockWebServer.url(URL_STRING).toString()

        httpClient.downloadFile(url, result)
            .onSuccess {

            }.await().apply {
                assertTrue(getState().isSuccess())
            }

        result.apply {
            assertTrue(exists())
            assertTrue(length() == VAL_HELLO.length.toLong())
            assertTrue("hello.txt" == name)
            assertTrue(delete())
        }
    }

    @Test
    fun downloadFileToDir(@TempDir tempDir: File) {
        val result = File(tempDir, "hello_world.html")
        val url = mockWebServer.url(URL_STRING).toString()

        httpClient.downloadFile(url, tempDir)
            .await().apply {
                assertTrue(getState().isSuccess())
            }

        result.apply {
            assertTrue(exists())
            assertTrue(length() == VAL_HELLO.length.toLong())
        }
    }

    @Test
    fun downloadFileProgress() {
        var result: File? = null
        val url = mockWebServer.url(URL_STRING).toString()
        var progressData: ProgressData? = null
        httpClient.downloadFile(url)
            .onProgress {
                progressData = it.getProgressData()
            }.onSuccess {
                result = it.getResult()
            }.await()

        assertTrue(result != null)
        assertTrue(result?.length() == VAL_HELLO.length.toLong())
        assertTrue(progressData != null) //?
        result!!.delete()
    }


    @Test
    fun donwloadString() {

        var result: String? = null
        val url = mockWebServer.url(URL_STRING).toString()
        httpClient.download(url)
            .onSuccess {
                result = it.getResult()
            }.onError {
                println("donwloadString: error")
            }.await()
        assertTrue(result == VAL_HELLO)
    }

    @Test
    fun donwloadStringSync() {
        var result: String? = null

        httpClient.httpRequest(mockWebServer.url(URL_STRING).toString(), String::class.java)
            .build()
            .onSuccess {
                result = it.getResult()
            }.sync()

        assertTrue(result == VAL_HELLO)
    }


    @Test
    fun testRestApiGet() {
        val url = mockWebServer.url(URL_API).toString()
        var response: ResponseSrvString? = null

        httpClient.restApiGet(url, ResponseSrvString::class.java).onSuccess {
            println("result ${it.getResult()}")
            response = it.getResult()
        }.await()

        assertTrue(response != null)
        assertTrue(response?.data == VAL_HELLO)
    }


}