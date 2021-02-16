package com.darkraha.services.webcrawler

import org.jsoup.nodes.Document
import java.net.URI

class HandlingUri {
    var uri: URI? = null
    var url: String? = null
    var fromPage: URI? = null
    var lvl = 0
    lateinit var doc: Document
}