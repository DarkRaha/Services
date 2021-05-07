package com.darkraha.services.diskcache

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class DiskCacheServiceTest {

    @Test
    fun addGetFile(@TempDir tempDir: File) {
        var success = false
        val srv = DiskCacheService(File(tempDir, "diskcache"))
        val file1 = File(srv.tmp, "file1.html")
        file1.writeText("1")
        srv.addFile("htpps://google.com", file1).onSuccess {
            success = true
        }.await()

        val file = srv.getFile("htpps://google.com")
        assertTrue(file != null)
        assertTrue(success)
        assertTrue(file!!.length() > 0)
        val content = file.readText()
        assertTrue(content == "1")
    }


    @Test
    fun addRemoveFile(@TempDir tempDir: File) {
        var successAdd = false
        var successRemove = false

        val srv = DiskCacheService(File(tempDir, "diskcache"))
        val file1 = File(srv.tmp, "file1.html")
        file1.writeText("1")
        val key = "htpps://google.com"
        srv.addFile(key, file1).onSuccess { successAdd = true }.await()
        srv.removeKey(key).onSuccess { successRemove = true }.await()
        assertTrue(successAdd)
        assertTrue(successRemove)
        assertTrue(srv.getFile(key) == null)

    }


    @Test
    fun clearTmp(@TempDir tempDir: File) {
        var success = false
        val srv = DiskCacheService(File(tempDir, "diskcache"))
        val file1 = File(srv.tmp, "file1.html")
        file1.writeText("1")
        assertTrue(file1.exists())
        srv.clearTemporary().onSuccess { success = true }.await()
        assertFalse(file1.exists())
        assertTrue(success)
    }

}