package com.darkraha.services.webcrawler

import com.darkraha.services.core.deferred.Deferred
import com.darkraha.services.core.deferred.DeferredServiceBuilder
import com.darkraha.services.core.deferred.UserDeferred
import java.net.URI

class DeferredCrawlerRequestBuilder(val deferred: Deferred<CrawlerRequest, CrawlingResult>) {
    private val request = MutableCrawlerRequest()

    fun url(url: String): DeferredCrawlerRequestBuilder = this.apply { request.mUrl = url; request.mUri = null }
    fun uri(uri: URI): DeferredCrawlerRequestBuilder = this.apply { request.mUri = uri; request.mUrl = null }
    fun lvlMax(lvlMax: Int): DeferredCrawlerRequestBuilder = this.apply { request.mLvlMax = lvlMax }
    fun allowedByDefault(allowedByDefault: AllowedByDefault): DeferredCrawlerRequestBuilder = this.apply {
        request.mAllowedByDefault = allowedByDefault
    }

    fun ignoreRef(v: Boolean) = this.apply { request.mIgnoreRef = v }
    fun maxLinks(count: Int) = this.apply { request.mLinkMaxCount = count }
    fun linkFollowedCss(css: Array<String>) = this.apply { request.mLinkFollowedCss = css }
    fun linkExcludedCss(css: Array<String>) = this.apply { request.mLinkExcludedCss = css }
    fun collectFromPages(v: Boolean) = this.apply { request.mCollectFromPages = v }
    fun ignoreLastSlash(v: Boolean) = this.apply { request.mIgnoreLastSlash = v }

    fun build(): DeferredServiceBuilder<CrawlerRequest, CrawlingResult> {
        when {
            request.mUrl == null -> request.mUrl = request.uri.toString()
            request.mUri == null -> request.mUri = URI.create(request.url)
        }
        deferred.job.params=request
        return deferred
    }

    fun async(): UserDeferred<CrawlingResult> {
        return build().async()
    }

    fun sync(): UserDeferred<CrawlingResult> {
        return build().sync()
    }

}