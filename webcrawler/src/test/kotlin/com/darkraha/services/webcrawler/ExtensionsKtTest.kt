package com.darkraha.services.webcrawler

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URI

internal class ExtensionsKtTest{


    @Test
    fun testParentPath(){
        val uri1 = URI.create("https://socode4.com")
        val uri2 = URI.create("https://socode4.com/")
        val uri3 = URI.create("https://socode4.com/articles/en/kotlin/reference/numbers")
        val uri4 = URI.create("https://socode4.com/articles/en/kotlin/reference")
        val uri5 = URI.create("https://socode4.com/articles/")
        val uri6 = URI.create("https://socode4.com/articles")
        val uri7 = URI.create("https://socode4.com/articles/en/kotlin/reference/")

        assertTrue(uri1.parentPath()==null)
        assertTrue(uri2.parentPath()==null)
        assertTrue(uri3.parentPath()=="/articles/en/kotlin/reference/")
        assertTrue(uri4.parentPath()=="/articles/en/kotlin/")
        assertTrue(uri5.parentPath()=="/")
        assertTrue(uri6.parentPath()=="/")
        assertTrue(uri7.parentPath()=="/articles/en/kotlin/")
    }

}