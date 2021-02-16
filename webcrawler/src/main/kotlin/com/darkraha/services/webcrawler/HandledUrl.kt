package com.darkraha.services.webcrawler

class HandledUrl() {
    constructor(src: HandlingUri, addFromPage: Boolean) :this(){
        url = src.url!!
        minLvl = src.lvl
        if (addFromPage) {
            src.fromPage?.apply {
                addFromPage(toString())
            }
        }
    }

    var exception: Exception? = null
    val isError
        get() = exception == null

    var url: String = ""
    var minLvl = 0

    /**
     * List of urls where this page was found
     */
    var fromPages: MutableSet<String>? = null

    fun addFromPage(url: String) {
        fromPages = (fromPages ?: HashSet()).apply { add(url) }
    }
}