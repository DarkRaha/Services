package com.darkraha.services.endecodefiles.encoder

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FileEncoderServiceTest {

    @Test
    fun encode(@TempDir tempDir: File) {

        FileEncoderService.newInstance().also {
            it.addEncoder("txt", String::class.java, object : Encoder<String>() {
                override fun encode(obj: String, encodeParam: Any?, dstFile: File): File {
                    dstFile.writeText(obj)
                    return dstFile
                }
            })

            it.addEncoder("txt", Txt::class.java, object : Encoder<Txt>() {
                override fun encode(obj: Txt, encodeParam: Any?, dstFile: File): File {
                    dstFile.writeText(obj.content)
                    return dstFile
                }
            })

            val file = File(tempDir, "a.txt")
            val file2 = File(tempDir, "b.txt")

            it.encode(file, "hello").await().apply {
                assertTrue(getFile()!!.readText() == "hello")
            }

            it.encode(file2, Txt("Hello World")).await().apply {
                assertTrue(getFile()!!.readText() == "Hello World")
            }
        }
    }

    class Txt(val content: String = "")
}



