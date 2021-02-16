package com.darkraha.services.webcrawler

import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.MutableProgressData
import com.darkraha.services.core.worker.Task
import com.darkraha.services.core.worker.WorkerActions
import com.darkraha.services.http.HttpClient
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.net.URI
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author Rahul Verma
 */
open class HtmlCrawlingTask(protected var httpClient: HttpClient) : Task<CrawlerRequest> {
    override fun onTask(params: CrawlerRequest?, workerActions: WorkerActions, jobResponse: JobResponse<*>) {

        val state = CrawlerState().also {
            it.request = params!!
            it.actions = workerActions
            it.jobResponse = jobResponse
            workerActions.result.tmpResult = it.result
        }


        state.mPendingUrl.add(HandlingUri().apply {
            uri = URI.create(state.request.url)
            url = state.request.url
        })

        while ((state.mPendingUrl.size > 0 || state.countPending.get() > 0)
        ) {
            handlePending(state)
            state.lock.withLock {
                println("onTask count pending: ${state.countPending.get()}")
                if (state.countPending.get() > 0) {
                    state.condUrlHandled.await(1, TimeUnit.MINUTES)
                }
                println("onTask after waiting ${state.countPending.get()}")
            }
        }
        println("onTask result ${state.result.mHandledUrls.size}")
    }

    protected open fun handlePending(state: CrawlerState) {
        state.apply {
            var handlingUri = mPendingUrl.poll()

            while (handlingUri != null) {


                val url = handlingUri.url!!
                var isNotHandled = false
                var isMaxcount: Boolean

                lock.withLock {
                    isMaxcount = result.mHandledUrls.size >= request.linkMaxCount

                    if (!isMaxcount && !result.isHandled(url)) {
                        result.mHandledUrls[url] = HandledUrl(handlingUri, true)
                        state.countPending.incrementAndGet()
                        isNotHandled = true
                    }
                }


                if (isNotHandled) {
                    val localHandlingUri = handlingUri
                    httpClient.download(localHandlingUri.url!!).onSuccess {

                        localHandlingUri.doc = Jsoup.parse(it.getResult(), localHandlingUri.url)
                        handlingUri(localHandlingUri, state)

                        lock.withLock {
                            state.countPending.decrementAndGet()
                            state.condUrlHandled.signalAll()
                            println("handlePending on success ${localHandlingUri.url} ")
                        }
                    }.onError {
                        result.mHandledUrls[url]!!.exception = it.getError()
                        lock.withLock {
                            state.countPending.decrementAndGet()
                            state.condUrlHandled.signalAll()
                        }
                    }
                }
                handlingUri = state.mPendingUrl.poll()
                println("handlingUri next url ${handlingUri?.url} size = ${state.mPendingUrl.size}")
            }
        }
    }

    protected open fun handlingUri(
        handlingUri: HandlingUri,
        // doc: Document,
        state: CrawlerState
    ) {
        val actions = state.actions
        val result = state.result
        val url = handlingUri.url!!

        try {
            extractFollowLinks(handlingUri, state)

            val pd = MutableProgressData().apply {
                mCurrentData = handlingUri
                mAction = url
            }

            result.mHandledUrls[url]!!.apply { actions.notifyProgress(pd) }
        } catch (e: Exception) {
            e.printStackTrace()
            (actions.result.tmpResult as MutableCrawlingResult).apply {
                mHandledUrls[url]!!.exception = e
            }
        }
    }

    protected open fun extractFollowLinks(
        uriFrom: HandlingUri,
        state: CrawlerState
    ) {
        val doc = uriFrom.doc
        val request = state.request

        if (request.linkFollowedCss == null) {
            followLinks(uriFrom, doc.select("a[href]"), state)
        } else {
            request.linkFollowedCss?.forEach {
                followLinks(uriFrom, doc.select(it), state)
            }
        }
    }

    protected open fun followLinks(
        uriFrom: HandlingUri,
        elements: Elements,
        state: CrawlerState
    ) {
        val request = state.request
        var selected = elements

        selected = selected.not("[download]")

        request.linkExcludedCss?.apply {
            forEach { selected = selected.not(it) }
        }

        for (element in selected) {
            val href: String = element.absUrl("href")
            followLink(href, uriFrom, state)
        }
    }

    protected open fun followLink(
        urlFollow: String,
        uriFrom: HandlingUri,
        state: CrawlerState
    ) {
        val request = state.request
        val result = state.result
        val tmpUrl = URL(urlFollow)

        if (tmpUrl.protocol != request.uri.scheme || urlFollow.startsWith('#')) {
            return
        }

        val uriFollow = tmpUrl.let {
            URI(
                it.protocol, it.userInfo, it.host, it.port,
                if (request.ignoreLastSlash && it.path.endsWith('/')) {
                    it.path.substringBeforeLast('/')
                } else it.path,
                it.query,
                if (request.ignoreRef) null else it.ref
            )
        }

        val uriFollowStr = uriFollow.toASCIIString()

        state.lock.withLock {
            if (result.isHandled(uriFollowStr)) {
                result.mHandledUrls[uriFollowStr]!!.also {
                    it.minLvl = Math.min(it.minLvl, uriFrom.lvl + 1)
                    if (request.collectFromPages) {
                        it.addFromPage(uriFrom.url!!)
                    }
                }
            } else {
                if (result.mHandledUrls.size >= request.linkMaxCount) {
                    return@withLock
                }

                state.mPendingUrl.takeIf {
                    uriFrom.lvl + 1 < request.lvlMax && when (request.allowedByDefault) {
                        AllowedByDefault.ALL -> true // include external url
                        AllowedByDefault.HOST -> uriFollow.host == request.uri.host// url with same host as start url
                        AllowedByDefault.PARENT -> uriFollow.host == request.uri.host && uriFollow.path.startsWith(
                            request.parentPath!!
                        )
                        AllowedByDefault.CHILDS -> uriFollow.host == request.uri.host && uriFollow.path.startsWith(
                            request.uri.path
                        )
                    }
                }?.apply {
                    add(HandlingUri().apply {
                        uri = uriFollow
                        url = uriFollowStr
                        lvl = uriFrom.lvl + 1
                        fromPage = uriFrom.uri
                    })
                } ?: println("can not add url ${urlFollow} converted to ${uriFollowStr}")
            }
        }
    }
}

class CrawlerState {
    val countPending = AtomicInteger()
    val lock = ReentrantLock()
    val condUrlHandled = lock.newCondition()
    val mPendingUrl: LinkedBlockingQueue<HandlingUri> = LinkedBlockingQueue<HandlingUri>()
    val result = MutableCrawlingResult()
    lateinit var request: CrawlerRequest
    lateinit var actions: WorkerActions
    lateinit var jobResponse: JobResponse<*>
}