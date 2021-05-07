package com.darkraha.services.core.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileUtilsTest {


    fun defaultFileTree(tempDir: File) {
        val topFile = File(tempDir, "topfile.txt")
        val srcdir1: File = File(tempDir, "subdir1")
        srcdir1.mkdirs()

        val srcdir2: File = File(tempDir, "subdir2")
        srcdir2.mkdirs()

        val file1 = File(srcdir1, "file1.txt")
        val file2 = File(srcdir1, "file2.txt")
        val file3 = File(srcdir2, "file3.txt")
        val file4 = File(srcdir2, "file4.txt")
        file1.writeText("1")
        file2.writeText("2")
        file3.writeText("3")
        file4.writeText("4")
        topFile.writeText("5")
    }

    @Test
    fun calcDirSize(@TempDir tempDir: File) {
        defaultFileTree(tempDir)
        val size = FileUtils.calcSize(tempDir)
        println(size)
        assertTrue(size == 5L)
    }

    @Test
    fun deleteDir(@TempDir tempDir: File) {
        defaultFileTree(tempDir)
        assertFalse(tempDir.delete())
        FileUtils.deleteDir(tempDir)
        assertFalse(tempDir.exists())
    }

    @Test
    fun clearDir(@TempDir tempDir: File) {
        defaultFileTree(tempDir)
        assertTrue(tempDir.listFiles()!!.size == 3)
        FileUtils.clearDir(tempDir)
        assertTrue(tempDir.listFiles()!!.size == 0)
        assertTrue(tempDir.exists())
    }

    @Test
    fun copyFile(@TempDir tempDir: File) {
        defaultFileTree(tempDir)
        val src = File(tempDir, "subdir1/file1.txt")
        val dst = File(tempDir, "subdir3/file1.txt")
        assertFalse(dst.exists())
        FileUtils.copyFile(src, dst)
        assertTrue(src.exists())
        assertTrue(dst.exists())
        assertTrue(dst.length() == src.length())
    }

    @Test
    fun copyFileIntoDir(@TempDir tempDir: File) {
        defaultFileTree(tempDir)
        val src = File(tempDir, "subdir1/file1.txt")
        val dst = File(tempDir, "subdir3")

        dst.mkdirs()
        FileUtils.copyFileIntoDir(src, dst)
        assertTrue(src.exists())
        assertTrue(dst.exists())
        val dstFile = File(dst, "file1.txt")
        assertTrue(dstFile.exists())
        assertTrue(dstFile.length() == src.length())
    }

    @Test
    fun copyIntoDir(@TempDir tempDir: File) {

        val srcDir = File(tempDir, "tmp1")
        val dstDir = File(tempDir, "tmp2")

        defaultFileTree(srcDir)

        assertTrue(dstDir.listFiles() == null)
        FileUtils.copyIntoDir(srcDir, dstDir)

        assertTrue(dstDir.listFiles()!!.size == 1)
        assertTrue(dstDir.listFiles()!![0].name == srcDir.name)
        assertTrue(dstDir.listFiles()!![0].listFiles()!!.size == 3)
    }

    @Test
    fun deleteOldFiles(@TempDir tempDir: File) {
        defaultFileTree(tempDir)
        val oldFile = File(tempDir, "subdir1/file2.txt")
        val nonOldFile = File(tempDir, "subdir1/file1.txt")
        assertTrue(oldFile.exists())
        oldFile.setLastModified(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(20))
        FileUtils.deleteOldFiles(TimeUnit.DAYS.toMillis(10), tempDir)
        assertTrue(nonOldFile.exists())
        assertFalse(oldFile.exists())
    }

    @Test
    fun traverse(@TempDir tempDir: File) {
        defaultFileTree(tempDir)

        val lst = mutableListOf<File>()

        FileUtils.traverseDir(tempDir) {
            lst.add(it)
        }
        assertTrue(lst.size == 8)
        assertTrue(lst[0] == tempDir)
    }


    @Test
    fun backtraverse(@TempDir tempDir: File) {
        defaultFileTree(tempDir)

        val lst = mutableListOf<File>()

        FileUtils.backTraverseDir(tempDir) {
            lst.add(it)
        }

        assertTrue(lst.size == 8)
        assertTrue(lst.last() == tempDir)
    }


    @Test
    fun genFile(@TempDir tempDir: File) {
        val str = "https://socode4.com/articыыles/htm/robot.txt#we?a=345"
        val str1 = "https://socode4.com/articыыles/htm/robot#we?a=345"

        assertTrue(FileUtils.genFile(str, null, null) == File("robot.txt"))
        assertTrue(FileUtils.genFile(str1, "text/plain", null) == File("robot.txt"))
        assertTrue(FileUtils.genFile(str1, "text/plain", tempDir) == File(tempDir, "robot.txt"))
        assertTrue(FileUtils.genFile(str1, null, tempDir) == File(tempDir, "robot.bin"))
    }

}