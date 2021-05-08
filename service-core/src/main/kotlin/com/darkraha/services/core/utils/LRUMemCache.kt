package com.darkraha.services.core.utils

import java.util.*

class LRUMemCache<K, V>(
    private val maxMemoryUsage: Int,
    initialCapacity: Int = 64,
    private val loadFactor: Float = 0.75f,

    ) : LinkedHashMap<K, SoftRef<V>>(initialCapacity, loadFactor, true) {
    var memoryUsage = 0
        private set


    override fun clear() {
        super.clear()
        memoryUsage = 0
    }

    override fun remove(key: K): SoftRef<V>? {
        return super.remove(key)?.apply {
            memoryUsage -= objSize
        }
    }

    override fun put(key: K, value: SoftRef<V>): SoftRef<V>? {
        if (value.objSize > maxMemoryUsage) {
            throw IllegalArgumentException("The value exceeds the maximum memory usage and cannot be stored.")
        }

        memoryUsage += value.objSize
        if (memoryUsage > maxMemoryUsage) {
            cleanup()
        }
        return super.put(key, value)
    }

    fun cleanup() {
        cleanupEmpty()
        val maxUsage = maxMemoryUsage * loadFactor

        if (memoryUsage > maxUsage) {
            val it = mutableListOf<K>().apply { addAll(keys) }.iterator()

            while (it.hasNext() && memoryUsage > maxUsage) {
                val key = it.next()
                remove(key)
            }
        }
    }

    fun cleanupEmpty() {
        val it = entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.get() == null) {
                it.remove()
                memoryUsage -= entry.value.objSize
            }
        }
    }

}