package com.darkraha.services.core.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * Create "global" ReentrantLock for the monitor object. If object is no more monitored, then
 * ReentrantLock will be removed.
 *
 * For example, you have several queries to work with same file. You can use file as monitored object.
 * As result one of query will be wait other.
 *
 *
 * @author Rahul Verma
 */
object GlobalLock {

    private val mLock = ReentrantLock()
    private val locks = ConcurrentHashMap<Any, ReentrantLock>()

    /**
     * Lock with ReentrantLock object.
     */
    fun lock(res: Any?) {
        res?.let {
            mLock.lock()
            var ret: ReentrantLock? = locks.get(it)

            if (ret == null) {
                ret = ReentrantLock()
                locks.put(it, ret)
            }
            mLock.unlock()
            ret.lock()
        }
    }


    fun unlock(res: Any?) {
        res?.let {
            mLock.lock()
            locks[it]?.apply {
                if (holdCount > 0) {
                    unlock()
                    if (!isLocked) {
                        locks.remove(this)
                    }
                }
            }
            mLock.unlock()
        }
    }
}
