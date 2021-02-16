package com.darkraha.services.webcrawler

import com.darkraha.services.core.deferred.DeferredFactory
import com.darkraha.services.core.service.Service
import com.darkraha.services.core.worker.Worker
import com.darkraha.services.http.HttpClient
import com.darkraha.services.http.HttpClientOk
import java.util.concurrent.ExecutorService

open class WebCrawler protected constructor() : Service<CrawlerRequest>() {


    protected var httpClient: HttpClient? = null

    override fun setupDefault() {
        httpClient = httpClient ?: HttpClientOk.newInstance()
        mainWorker = mainWorker ?: Worker<CrawlerRequest>().onMainTask(HtmlCrawlingTask(httpClient!!))
        super.setupDefault()
    }


    fun newCrawlingTask(url: String) = DeferredCrawlerRequestBuilder(deferredFactory!!).url(url)


    class Builder {
        private val srv = WebCrawler()
        fun httpService(http: HttpClient) : Builder = this.apply { srv.httpClient = http }
        fun mainWorker(w: Worker<CrawlerRequest>): Builder = this.apply { srv.mainWorker = w }
        fun executor(e: ExecutorService): Builder = this.apply { srv.executorService = e }
        fun deferredFactory(df: DeferredFactory<CrawlerRequest>): Builder = this.apply { srv.deferredFactory = df }
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