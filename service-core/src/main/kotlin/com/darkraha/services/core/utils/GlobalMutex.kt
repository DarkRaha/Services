package com.darkraha.services.core.utils


import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


/**
 * Create "global" Mutex for the monitor object. If object is no more monitored, then
 * mutex will be removed.
 *
 * For example, you have two download queries with same url. You can use url as monitored object.
 * As result one of query will be wait other.
 *
 * @author Rahul Verma
 */
object GlobalMutex {

    private val mMutex = Mutex()
    private val mutexes = ConcurrentHashMap<Any, Pair<AtomicInteger, Mutex>>()


    suspend fun lock(res: Any?, owner: Any) {
        res?.apply {
            mMutex.lock(owner)
            var p = mutexes.get(res)
            if (p == null) {
                p = Pair(AtomicInteger(0), Mutex())
                mutexes[this] = p
            }

            p.first.incrementAndGet()
            mMutex.unlock(owner)
            p.second.lock(owner)
        }
    }


    suspend fun unlock(res: Any?, owner: Any) {
        res?.also {
            mMutex.lock(owner)
            mutexes[it]?.apply {
                first.decrementAndGet().apply {
                    if (this == 0) {
                        mutexes.remove(it)
                    }
                }
                second.unlock(owner)
            }
            mMutex.unlock(owner)
        }
    }
}