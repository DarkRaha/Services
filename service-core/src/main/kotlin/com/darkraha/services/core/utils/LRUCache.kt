package com.darkraha.services.core.utils

import java.util.LinkedHashMap


class LRUCache<K, V>(
    initialCapacity: Int,
    loadFactor: Float,
    private val maxEntries: Int
) : LinkedHashMap<K, V>(initialCapacity, loadFactor, true) {
    constructor(
        initialCapacity: Int,
        maxEntries: Int
    ) : this(initialCapacity, DEFAULT_LOAD_FACTOR, maxEntries)

    constructor(maxEntries: Int) : this(DEFAULT_INITIAL_CAPACITY, maxEntries)

    constructor(
        m: Map<out K, V>,
        maxEntries: Int
    ) : this(m.size, maxEntries) {
        putAll(m)
    }

    override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
        return size > maxEntries
    }

    companion object {
        private const val DEFAULT_INITIAL_CAPACITY = 16
        private const val DEFAULT_LOAD_FACTOR = 0.75f
    }
}