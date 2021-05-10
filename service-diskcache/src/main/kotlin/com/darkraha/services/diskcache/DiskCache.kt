package com.darkraha.services.diskcache

import com.darkraha.services.core.deferred.UserDeferred
import java.io.File
import java.util.concurrent.TimeUnit

interface DiskCache {

    /**
     * Add file to the diskcache storage if it is not stored.
     */
    fun addFile(key: String, file: File, isPersistent: Boolean = false, grp: String?=null): UserDeferred<Unit>

    fun addFileOrUpdate(key: String, file: File, isPersistent: Boolean = false, grp: String?=null): UserDeferred<Unit>

    /**
     * Remove files by key from diskcache storage.
     */
    fun removeKey(key: String, isPersistent: Boolean = false, grp: String?=null): UserDeferred<Unit>

    fun removeOldFiles(timeOldMs: Long = TimeUnit.DAYS.toMillis(10)): UserDeferred<Unit>

    /**
     * Get file by its key.
     */
    fun getFile(key: String, grp: String? = null): File?

    /**
     * Allows to get file when a file extension is known. More fast then getFile method.
     */
    fun getFileExt(key: String, fileExtension: String? = null, grp: String? = null): File?

    /**
     * Moves the files matching the key to persistent storage.
     * Thus, they will not be removed during the cleaning operation.
     */
    fun toPersistent(key: String): UserDeferred<Unit>

    fun toNonPersistent(key: String): UserDeferred<Unit>

    fun clearTemporary(): UserDeferred<Unit>

    fun getTemporaryDir(): File
}