package com.darkraha.services.webcrawler


import com.darkraha.services.webcrawler.webtools.SitemapGen
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.jsoup.nodes.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.Charset


internal class WebCrawlerTest {

    val URL_PAGE = "/page"
    val URL_SUBPAGE = "/page/10"
    val page = """
        <p>Hello</p>
        <a href="https://google.com">google</a>
        <a href="/page/1">1</a>
        <a href="/page/2">2</a>
        <a href="/page/3">3</a>
        <a href="/page/4">4</a>
        <a href="/page/5">5</a>
        <a href="/page/6">6</a>
        <a href="/page/7">7</a>
        <a href="/page/8">8</a>
        <a href="/page/9">9</a>
        <a href="/page/10">10</a>
        <a href="/page/10/1">10.1</a>
        <a href="/page/10/2">10.2</a>
        <a href="/page/10/3">10.3</a>
    """.trimIndent()

    val subpage = """
      <a href='/page'>home</a>
      <a href="/page/8">8</a>
      <a href="/page/9">9</a>
      <a href="/page/10">10</a>
      <a href="/page/10/1">10.1</a>
      <a href="/page/10/2">10.2</a>
      <a href="/page/10/3">10.3</a>
      <a href="https://google.com">google</a>
    """.trimIndent()

    val sitemap = """<?xml version="1.0" encoding="UTF-8"?>

<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
<!-- Sitemap generated by the online tool socode4.com -->
    <url>
        <loc>http://localhost:8080/page</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/1</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/2</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/3</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/4</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/5</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/6</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/7</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/8</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/9</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/10</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/10/1</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/10/2</loc>
    </url>
    <url>
        <loc>http://localhost:8080/page/10/3</loc>
    </url>
</urlset>"""


    lateinit var mockWebServer: MockWebServer


    @BeforeEach
    fun beforeEach() {
        mockWebServer = MockWebServer()

        val dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                when {
                    request.path == URL_PAGE -> return MockResponse().setResponseCode(200).setBody(page)
                    request.path?.startsWith("/page") ?: false -> return MockResponse()
                        .setResponseCode(200).setBody(subpage)
                    else -> return MockResponse().setResponseCode(404)
                }

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
    fun testCrawleRootSyncLimit() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_PAGE).toString())
            .maxLinks(5)
            .build().onProgress {
                count++
            }.sync()
        assertTrue(count == 5)
    }

    @Test
    fun testCrawleRootASyncLimit() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_PAGE).toString())
            .maxLinks(5)
            .build().onProgress {
                count++
            }.async().await()
        assertTrue(count == 5)
    }



    @Test
    fun testCrawleExcludeSyncLimit() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_PAGE).toString())
            .linkExcludedCss(arrayOf("[href$=2]"))
            .build().onProgress {
                count++
            }.sync()
        assertTrue(count == 12)
    }

    @Test
    fun testCrawleExcludeASyncLimit() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_PAGE).toString())
            .linkExcludedCss(arrayOf("[href$=2]"))
            .build().onProgress {
                count++
            }.async().await()
        assertTrue(count == 12)
    }


    @Test
    fun testCrawleIncludeSyncLimit() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_PAGE).toString())
            .linkFollowedCss(arrayOf("[href$=2]"))
            .build().onProgress {
                count++
            }.sync()
        assertTrue(count == 3)
    }

    @Test
    fun testCrawleIncludeASyncLimit() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_PAGE).toString())
            .linkFollowedCss(arrayOf("[href$=2]"))
            .build().onProgress {
                count++
            }.async().await()
        assertTrue(count == 3)
    }


    @Test
    fun testCrawleRootSync() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_PAGE).toString())
            .build().onProgress {
                count++
            }.sync()
        // 13 subpages + start page, goggle must be excluded
        assertTrue(count == 14)
    }

    @Test
    fun testCrawleRootASync() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_PAGE).toString())
            .build().onProgress {
                count++
            }.async().await()
        // 13 subpages + start page, goggle must be excluded
        assertTrue(count == 14)
    }

    @Test
    fun testCrawleSubPageSync() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_SUBPAGE).toString())
            .build().onProgress {
                count++
            }.sync()
        println(count)
        assertTrue(count == 4)
    }

    @Test
    fun testCrawleSubPageASync() {
        var count = 0
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_SUBPAGE).toString())
            .build().onProgress {
                count++
            }.async().await()
        assertTrue(count == 4)
    }

    @Test
    fun testCrawlingWithPlugin() {
        var sm: String? = null
        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask(mockWebServer.url(URL_PAGE).toString())
            .build()
            .subscribe(
                SitemapGen.Builder()
                    .build()
            )
            .onProgress {
                println(it.getProgressData()!!.action)
            }.sync().onFinish {
                val sitemapGen = it.getPlugin(SitemapGen.NAME) as SitemapGen
                sm = sitemapGen.result.toString()
            }

        assertTrue(sm == sitemap)
    }
}