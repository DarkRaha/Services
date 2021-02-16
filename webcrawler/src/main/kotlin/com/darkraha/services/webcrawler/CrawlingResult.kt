package com.darkraha.services.webcrawler

import java.util.*
import kotlin.collections.HashMap

interface CrawlingResult {
    val handledUrls: Map<String, HandledUrl>
}

class MutableCrawlingResult : CrawlingResult {
    val mHandledUrls = Collections.synchronizedMap(HashMap<String, HandledUrl>())

    fun isHandled(url: String) = mHandledUrls.get(url) != null

    override val handledUrls: Map<String, HandledUrl>
        get() = mHandledUrls
}

