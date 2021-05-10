package com.darkraha.services.endecodefiles.decoder

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FileDecoderServiceTest {
    @Test
    fun decode(@TempDir tempDir: File) {

        FileDecoderService.newInstance().also {
            it.addDecoder("txt", String::class.java, object : Decoder<String>() {

                override fun decode(file: File, param: Any?): String {
                    return file.readText()
                }

                override fun calcSize(obj: String?): Int {
                    return obj?.length ?: 0
                }
            })

            it.addDecoder("txt", Txt::class.java, object : Decoder<Txt>() {

                override fun decode(file: File, param: Any?): Txt {
                    return Txt().apply {
                        content = file.readText()
                    }
                }

                override fun calcSize(obj: Txt?): Int {
                    return obj?.content?.length ?: 0
                }
            })

            val file = File(tempDir, "a.txt")
            file.writeText("content1")

            val file2 = File(tempDir, "b.txt")
            file2.writeText("content2")

            var decoded: String? = null
            it.decode(file, String::class.java).onSuccess {
                decoded = it.getResult()
            }.await()


            decoded = null
            it.decode(file2, Txt::class.java).onSuccess {
                decoded = it.getResult()?.content
            }.await()

            //println("decoded $decoded")
            assertTrue(decoded == "content2")
        }
    }

    class Txt {
        var content = ""
    }
}