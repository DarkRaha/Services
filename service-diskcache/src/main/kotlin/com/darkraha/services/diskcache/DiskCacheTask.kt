package com.darkraha.services.diskcache

import com.darkraha.services.core.extensions.encodeMd5
import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.MutableProgressData
import com.darkraha.services.core.job.Task
import com.darkraha.services.core.utils.FileUtils
import com.darkraha.services.core.worker.WorkerActions
import com.darkraha.services.diskcache.DiskCacheService.Companion.CMD_ADD
import com.darkraha.services.diskcache.DiskCacheService.Companion.CMD_ADD_OR_UPDATE
import com.darkraha.services.diskcache.DiskCacheService.Companion.CMD_CLEAN_OLD
import com.darkraha.services.diskcache.DiskCacheService.Companion.CMD_CLEAR_TMP
import com.darkraha.services.diskcache.DiskCacheService.Companion.CMD_MOVE_TO_STORAGE_PERSISTENT
import com.darkraha.services.diskcache.DiskCacheService.Companion.CMD_MOVE_TO_STORAGE
import com.darkraha.services.diskcache.DiskCacheService.Companion.CMD_REMOVE_KEY
import com.darkraha.services.diskcache.DiskCacheService.Companion.NOEXTENSION
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter


open class DiskCacheTask : Task<DiskCacheParams>() {
    override fun onTask(params: DiskCacheParams?, workerActions: WorkerActions<*>, jobResponse: JobResponse<*>) {


        when (jobResponse.getCmd()) {
            CMD_ADD -> {
                addFile(params, workerActions, jobResponse)
            }

            CMD_ADD_OR_UPDATE -> {
                addFile(params, workerActions, jobResponse, true)
            }

            CMD_MOVE_TO_STORAGE_PERSISTENT -> {
                moveKeyTo(params, workerActions, jobResponse)
            }

            CMD_MOVE_TO_STORAGE -> {
                moveKeyTo(params, workerActions, jobResponse)
            }

            CMD_CLEAR_TMP -> {
                clear(params, workerActions, jobResponse)
            }

            CMD_CLEAN_OLD -> {
                cleanOld(params, workerActions, jobResponse)
            }

            CMD_REMOVE_KEY -> {
                removeKey(params, workerActions, jobResponse)
            }

            else -> {
                throw IllegalStateException("Unknown diskcache command ${jobResponse.getCmd()}")
            }
        }
    }


    fun removeKey(params: DiskCacheParams?, workerActions: WorkerActions<*>, jobResponse: JobResponse<*>) {
        params!!.apply {
            val filename = key!!.encodeMd5()
            val progress = MutableProgressData().apply {
                mAction = "Remove files for key"
            }

            FileUtils.backTraverseDir(source!!) {
                if (it.isFile && it.name.startsWith(filename)) {
                    progress.mCurrentData = it
                    workerActions.notifyProgress(progress)
                    it.delete()
                }
            }
        }
    }


    fun cleanOld(params: DiskCacheParams?, workerActions: WorkerActions<*>, jobResponse: JobResponse<*>) {
        params!!.apply {
            val progress = MutableProgressData().apply {
                mAction = "Clean old files"
            }
            FileUtils.deleteOldFiles(oldTime, destination) {
                progress.mCurrentData = it
                workerActions.notifyProgress(progress)
            }
        }
    }


    fun clear(params: DiskCacheParams?, workerActions: WorkerActions<*>, jobResponse: JobResponse<*>) {
        val progress = MutableProgressData().apply {
            mAction = "Clear tmp directory"
        }
        params!!.apply {
            FileUtils.clearDir(source!!) {
                progress.mCurrentData = it
                workerActions.notifyProgress(progress)
            }
        }
    }

    /**
     * There are may be modifications files, i.e. with same key but with different extensions
     * or in different subdirectories.
     */
    fun moveKeyTo(params: DiskCacheParams?, workerActions: WorkerActions<*>, jobResponse: JobResponse<*>) {

        params!!.apply {
            val filename = key!!.encodeMd5()
            val progress = MutableProgressData().apply {
                mAction = "move files to ${destination}"
            }

            FileUtils.traverseDir(source!!) { itfile ->
                if (itfile.startsWith(filename)) {
                    val dst = destination.resolve(itfile.relativeTo(source!!))

                    progress.mCurrentData = itfile
                    progress.mTotal = itfile.length()
                    progress.mCurrent = 0

                    FileUtils.copyFile(itfile, dst) {
                        progress.mCurrent += it
                        workerActions.notifyProgress(progress)
                    }
                    itfile.delete()
                }
            }
        }
    }


    fun addFile(
        params: DiskCacheParams?,
        workerActions: WorkerActions<*>,
        jobResponse: JobResponse<*>,
        isReplace: Boolean = false
    ) {
        params!!.apply {
            destination.mkdirs()

            val dst = File(destination, key!!.encodeMd5() + "." + (source?.extension ?: NOEXTENSION))
            source!!.setLastModified(System.currentTimeMillis())

            if (!isReplace && dst.exists()) {
                workerActions.setReject("File ${source} already in diskcache.")
                return
            }

            val progress = MutableProgressData().apply {
                mAction = "Add file for key ${key}"
                mTotal = source!!.length()
                mCurrent = 0
            }

            FileUtils.copyFile(source!!, dst) {
                progress.apply {
                    mCurrent += it
                }
                workerActions.notifyProgress(progress)
            }
        }
    }

}