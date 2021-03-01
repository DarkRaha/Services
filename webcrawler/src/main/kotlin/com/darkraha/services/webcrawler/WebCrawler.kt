package com.darkraha.services.webcrawler

import com.darkraha.services.core.service.TypedService
import com.darkraha.services.core.worker.WorkerA
import com.darkraha.services.http.HttpClient
import com.darkraha.services.http.HttpServiceOk

open class WebCrawler protected constructor() : TypedService<CrawlerRequest>(CrawlerRequest::class.java) {


    protected var httpClient: HttpClient? = null

    override fun setupDefault() {
        httpClient = httpClient ?: HttpServiceOk.newInstance()
        defaultTask = HtmlCrawlingTask(httpClient!!)
        super.setupDefault()
    }


    fun newCrawlingTask(url: String) = DeferredCrawlerRequestBuilder(
        newDeferred(
            CrawlerRequest::class.java,
            CrawlingResult::class.java
        )
    ).url(url)


    class Builder {
        private val srv = WebCrawler()
        fun httpService(http: HttpClient): Builder = this.apply { srv.httpClient = http }
        fun mainWorker(w: WorkerA): Builder = this.apply { srv.defaultWorker = w }
        fun build(): WebCrawler {
            srv.setupDefault()
            return srv
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): WebCrawler = Builder().build()
    }
}