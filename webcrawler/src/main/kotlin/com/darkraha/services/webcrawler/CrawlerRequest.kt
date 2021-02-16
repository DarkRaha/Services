package com.darkraha.services.webcrawler

import java.net.URI

interface CrawlerRequest {
    val url: String
    val uri: URI
    val lvlMax: Int // maximum recursion level
    val linkMaxCount: Int // maximum count of handling links
    val allowedByDefault: AllowedByDefault
    val linkFollowedCss: Array<String>?
    val linkExcludedCss: Array<String>?
    val collectFromPages: Boolean
    val parentPath: String?
    val ignoreRef: Boolean // ignore after #
    val ignoreLastSlash: Boolean
}

class MutableCrawlerRequest : CrawlerRequest {
    var mCollectFromPages = false
    var mUrl: String? = null
    var mUri: URI? = null
    var mLvlMax = Int.MAX_VALUE
    var mAllowedByDefault = AllowedByDefault.CHILDS
    var mLinkFollowedCss: Array<String>? = null
    var mLinkExcludedCss: Array<String>? = null
    var mLinkMaxCount = 1000
    var mIgnoreRef: Boolean = true
    var mIgnoreLastSlash: Boolean = true

    override val ignoreLastSlash: Boolean
        get() = mIgnoreLastSlash

    override val ignoreRef: Boolean
        get() = mIgnoreRef

    override val parentPath: String? by lazy { mUri!!.parentPath() }

    override val linkMaxCount: Int
        get() = mLinkMaxCount

    override val allowedByDefault: AllowedByDefault
        get() = mAllowedByDefault

    override val lvlMax: Int
        get() = mLvlMax

    override val url: String
        get() = mUrl!!

    override val uri: URI
        get() = mUri!!

    override val linkFollowedCss: Array<String>?
        get() = mLinkFollowedCss

    override val linkExcludedCss: Array<String>?
        get() = mLinkExcludedCss

    override val collectFromPages: Boolean
        get() = mCollectFromPages
}