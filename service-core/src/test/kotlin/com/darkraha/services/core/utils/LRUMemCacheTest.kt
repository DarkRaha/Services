package com.darkraha.services.core.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class LRUMemCacheTest {


    @Test
    fun cleanupEmpty() {
        val cache = LRUMemCache<String, String>(1024)
        val obj = TestSoftRef("a", 322)
        cache.put("a", obj)
        obj.cleared = true

        cache.cleanupEmpty()
        assertTrue(cache.size == 0)
        assertTrue(cache.memoryUsage == 0)

    }

    @Test
    fun cleanup() {
        val cache = LRUMemCache<String, String>(1024)

        for (i in 0..9) {
            cache.put(i.toString(), TestSoftRef(i.toString(), 100))
        }

        assertTrue(cache.memoryUsage == 1000)
        val big = TestSoftRef("big value", 500)
        cache.put("big", big)

        assertTrue(cache.memoryUsage < 1024)
        assertTrue(cache["big"]!!.get()=="big value")
        assertTrue(cache["8"]!!.get()=="8")
        assertTrue(cache["9"]!!.get()=="9")
        assertTrue(cache["0"]==null)
    }


    class TestSoftRef<T>(obj: T, size: Int = 0, sizeCalc: ((T) -> Int)? = null) : SoftRef<T>(obj, size, sizeCalc) {
        var cleared = false

        override fun get(): T? {
            return if (cleared) null else super.get()
        }
    }

}