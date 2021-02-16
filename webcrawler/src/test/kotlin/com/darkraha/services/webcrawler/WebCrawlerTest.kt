package com.darkraha.services.webcrawler

import com.darkraha.services.webcrawler.webtools.SitemapGen
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.Charset

internal class WebCrawlerTest {

    @Test
    fun crawling() {

        val crawler = WebCrawler.newInstance()
        crawler.newCrawlingTask("https://socode4.com/app/en")
           // .maxLinks(40)
            .build()
            .plugin(
                SitemapGen.Builder()
                 //   .changeFreq(SitemapGen.ChangeFreq.weekly)
                    .build()
            )
            .onProgress { pd, result ->
                println(pd.action)
            }.sync().onFinish {
                val sitemapGen = it.getPlugins()!![SitemapGen.NAME] as SitemapGen
                println("total pages ${sitemapGen.countPages}")
                File("socode-sitemap.xml").writeText(sitemapGen.result, Charset.defaultCharset())

            }

        assertTrue(true)
    }

}