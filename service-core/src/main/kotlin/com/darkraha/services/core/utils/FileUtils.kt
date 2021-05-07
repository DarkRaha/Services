package com.darkraha.services.core.utils

import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import java.io.IOException


object FileUtils {

//    fun interface OnFile {
//        fun onFile(file: File)
//    }

    @JvmStatic
    var bufferSize = 1024


    /**
     * A deep traversal from given directory to the deep, i.e. dir param will be first item.
     */
    @JvmStatic
    fun traverseDir(dir: File, onFile: ((File) -> Unit)? = null) {
        //dir.walkTopDown()
        Files.walk(dir.toPath()).use { stream ->
            stream.map { obj: Path -> obj.toFile() }
                .forEach {
                    onFile?.invoke(it)
                }
        }
    }


    /**
     * Calculate size of directory
     */
    @JvmStatic
    fun calcSize(file: File): Long {

        if (file.isFile) return file.length()
        var length = 0L

        traverseDir(file) {
            if (it.isFile) {
                length += it.length()
            }
        }

        return length
    }

    /**
     * A deep traversal from deep to the given directory, i.e. dir param will be last item.
     */
    @JvmStatic
    fun backTraverseDir(dir: File, onFile: ((File) -> Unit)? = null) {
        Files.walk(dir.toPath()).use { stream ->
            stream.sorted(Comparator.reverseOrder())
                .map { obj: Path -> obj.toFile() }
                .forEach {
                    onFile?.invoke(it)
                }
        }
    }

    @JvmStatic
    fun deleteDir(f: File, onFile: ((File) -> Unit)? = null) {
        backTraverseDir(f) {
            onFile?.invoke(it)
            it.delete()
        }
    }


    /**
     * Delete content of directory, but not directory itself.
     */
    @JvmStatic
     fun clearDir(dir: File, onFile: ((File) -> Unit)?=null) {
        backTraverseDir(dir) {
            if (dir != it) {
                onFile?.invoke(it)
                it.delete()
            }
        }
    }

    /**
     * Delete old files (not directories).
     */
    @JvmStatic
    fun deleteOldFiles(timeOld: Long, dir: File, onFile: ((File) -> Unit)? = null) {
        if (dir.isDirectory) {
            val curTime = System.currentTimeMillis()
            backTraverseDir(dir) {
                if (dir != it) {
                    if (it.isFile && (curTime - it.lastModified() >= timeOld)) {
                        onFile?.invoke(it)
                        it.delete()
                    }
                }
            }
        }
    }

    /**
     * Copy data from input stream to the output stream.
     * @param onCopy called on each write operation, accept count of bytes for writing
     */
    @Throws(IOException::class)
    fun copy(inputStream: InputStream, os: OutputStream, onCopy: ((Int) -> Unit)? = null): Long {
        var nread = 0L
        val buf = ByteArray(bufferSize)
        var n: Int
        while (inputStream.read(buf).also { n = it } > 0) {
            os.write(buf, 0, n)
            nread += n.toLong()
            onCopy?.invoke(n)
        }
        return nread
    }

    /**
     * Copy data from source file to destination file.
     * @param onCopy called on each write operation, accept count of bytes for writing
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyFile(srcFile: File, dstFile: File, onCopy: ((Int) -> Unit)? = null): Long {
        dstFile.getParentFile().mkdirs()
        BufferedInputStream(srcFile.inputStream()).use { inStream ->
            BufferedOutputStream(dstFile.outputStream()).use { outStream ->
                return copy(inStream, outStream, onCopy)
            }
        }
    }

    /**
     * Copy file to the specified directory.
     * @param onCopy called on each write operation, accept count of bytes for writing
     */
    @JvmStatic
    fun copyFileIntoDir(srcFile: File, dstDir: File, onCopy: ((Int) -> Unit)? = null): Long {
        if (!dstDir.isDirectory) {
            throw IllegalArgumentException("Directory expected, but got file ${dstDir.absolutePath}")
        }
        dstDir.mkdirs()
        val dstFile = File(dstDir, srcFile.name)
        return copyFile(srcFile, dstFile, onCopy)
    }

    /**
     * Copy file to the specified directory.
     * @param onCopy called on each write operation, accept count of bytes for writing
     */
    @JvmStatic
    fun moveFileIntoDir(srcFile: File, dstDir: File, onCopy: ((Int) -> Unit)? = null): Long {
        if (!dstDir.isDirectory) {
            throw IllegalArgumentException("Directory expected, but got file ${dstDir.absolutePath}")
        }
        dstDir.mkdirs()
        val dstFile = File(dstDir, srcFile.name)
        val ret = copyFile(srcFile, dstFile, onCopy)
        srcFile.delete()
        return ret
    }

    @JvmStatic
    fun copyIntoDir(src: File, dstDir: File, onFile: ((File) -> Unit)? = null, onCopy: ((Int) -> Unit)? = null) {

        if (src.isFile) {
            copyFileIntoDir(src, dstDir, onCopy)
        } else {
            val dst = File(dstDir, src.name)
            dst.mkdirs()
            traverseDir(src) {
                onFile?.invoke(it)
                if (it.isDirectory) {
                    dst.resolve(it.relativeTo(src)).mkdirs()
                } else {
                    copyFile(it, dst.resolve(it.relativeTo(src)), onCopy)
                }
            }
        }
    }


}