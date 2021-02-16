package com.darkraha.services.webcrawler

enum class AllowedByDefault {
    ALL, // include external url
    HOST, // url with same host as start url
    PARENT, // url with same parent as start url
    CHILDS, // url that are child of start url
}