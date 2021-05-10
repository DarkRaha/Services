package com.darkraha.services.diskcache

import com.darkraha.services.core.deferred.UserDeferred
import com.darkraha.services.core.extensions.encodeMd5
import com.darkraha.services.core.service.TypedService
import com.darkraha.services.core.utils.LRUCache

import java.io.File
import java.io.FilenameFilter

open class DiskCacheService(val root: File = File("diskcache")) :
    TypedService<DiskCacheParams>(DiskCacheParams::class.java, DiskCacheTask()),
    DiskCache {

    protected val cache = LRUCache<String, File>(64)


    val storage = File(root, "storage")

    /**
     * Files from this directory will be excluded from clean from old files.
     */
    val storagePersistent = File(root, "persistent")

    /**
     * Temporary directory can be used for the file operations
     */
    val tmp: File = File(root, "tmp")

    init {
        root.mkdirs()
        storage.mkdirs()
        storagePersistent.mkdirs()
        tmp.mkdirs()
    }


    override fun addFile(key: String, file: File, isPersistent: Boolean, grp: String?): UserDeferred<Unit> {
        val params = DiskCacheParams().also {
            it.key = key
            it.source = file
            it.destination = if (isPersistent) storagePersistent else storage
            grp?.apply {
                it.destination = File(it.destination, this)
            }
        }

        return newTypedDeferred(params, Unit::class.java).cmd(CMD_ADD).idObject(key).async()
    }

    override fun addFileOrUpdate(key: String, file: File, isPersistent: Boolean, grp: String?): UserDeferred<Unit> {
        val params = DiskCacheParams().also {
            it.key = key
            it.source = file
            it.destination = if (isPersistent) storagePersistent else storage
            grp?.apply {
                it.destination = File(it.destination, this)
            }
        }

        return newTypedDeferred(params, Unit::class.java).cmd(CMD_ADD_OR_UPDATE).idObject(key).async()
    }

    override fun removeKey(key: String, isPersistent: Boolean, grp: String?): UserDeferred<Unit> {
        val params = DiskCacheParams().also {
            it.key = key
            it.source = if (isPersistent) storagePersistent else storage
            it.destination = it.source!!
            grp?.apply {
                it.destination = File(it.destination, this)
            }
        }
        return newTypedDeferred(params, Unit::class.java).cmd(CMD_REMOVE_KEY).idObject(key).async()
    }

    override fun removeOldFiles(timeOldMs: Long): UserDeferred<Unit> {
        val params = DiskCacheParams().also {
            it.destination = storage
            it.oldTime = timeOldMs
        }
        return newTypedDeferred(params, Unit::class.java).cmd(CMD_CLEAN_OLD).async()
    }

    override fun getFile(key: String, grp: String?): File? {
        cache.get(key)?.apply { return this }


        val encoded = key.encodeMd5()
        var dir = if (grp == null) storage else File(storage, grp)

        getFileWithName(encoded, dir)?.apply {
            cache[key] = this
            return this
        }

        dir = if (grp == null) storagePersistent else File(storagePersistent, grp)

        getFileWithName(encoded, dir)?.apply {
            cache[key] = this
            return this
        }

        return null
    }

    override fun getFileExt(key: String, fileExtension: String?, grp: String?): File? {
        val encoded = key.encodeMd5()
        val ext = fileExtension ?: NOEXTENSION
        var dir = if (grp == null) storage else File(storage, grp)
        File(dir, encoded + "." + ext).takeIf { it.exists() }?.apply {
            cache[key] = this
            return this
        }

        dir = if (grp == null) storagePersistent else File(storagePersistent, grp)

        File(dir, encoded + "." + ext).takeIf { it.exists() }?.apply {
            cache[key] = this
            return this
        }

        return null
    }

    override fun toNonPersistent(key: String): UserDeferred<Unit> {
        val params = DiskCacheParams().also {
            it.key = key
            it.source = storagePersistent
            it.destination = storage
        }
        return newTypedDeferred(params, Unit::class.java).cmd(CMD_MOVE_TO_STORAGE).async()
    }

    override fun toPersistent(key: String): UserDeferred<Unit> {
        val params = DiskCacheParams().also {
            it.key = key
            it.source = storage
            it.destination = storagePersistent
        }
        return newTypedDeferred(params, Unit::class.java).cmd(CMD_MOVE_TO_STORAGE_PERSISTENT).async()
    }

    override fun clearTemporary(): UserDeferred<Unit> {
        val params = DiskCacheParams().also {
            it.source = tmp
        }
        return newTypedDeferred(params, Unit::class.java).cmd(CMD_CLEAR_TMP).async()
    }


    protected fun getFileWithName(filename: String, dir: File): File? {
        val filter = object : FilenameFilter {
            override fun accept(dir: File?, name: String?): Boolean {
                return name?.startsWith(filename) ?: false
            }
        }

        dir.listFiles(filter)?.takeIf { it.size > 0 }?.apply {
            return this[0]
        }

        return null
    }

    override fun getTemporaryDir(): File = tmp

    companion object {
        val CMD_ADD_OR_UPDATE = "DiskCacheService.ADD_OR_UPDATE"
        val CMD_ADD = "DiskCacheService.ADD"
        val CMD_REMOVE_KEY = "DiskCacheService.REMOVE_KEY"
        val CMD_MOVE_TO_STORAGE_PERSISTENT = "DiskCacheService.MOVE_TO_STORAGE_PERSISTENT"
        val CMD_MOVE_TO_STORAGE = "DiskCacheService.MOVE_TO_STORAGE"
        val CMD_CLEAN_OLD = "DiskCacheService.CLEAN_OLD"
        val CMD_CLEAR_TMP = "DiskCacheService.CLEAR_TMP"
        val NOEXTENSION = "noext"
    }
}