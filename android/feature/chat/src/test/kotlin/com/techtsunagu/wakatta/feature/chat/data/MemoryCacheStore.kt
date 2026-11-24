package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.CacheEntry
import com.techtsunagu.wakatta.core.model.CacheStore
import java.util.concurrent.ConcurrentHashMap

/** In-memory CacheStore for tests. */
internal class MemoryCacheStore : CacheStore {
    private val entries = ConcurrentHashMap<String, CacheEntry>()
    override suspend fun get(key: String): CacheEntry? = entries[key]
    override suspend fun put(key: String, json: String, etag: String?) {
        entries[key] = CacheEntry(key, json, etag, System.currentTimeMillis())
    }
    override suspend fun evict(prefix: String) {
        entries.keys.removeAll { it.startsWith(prefix) }
    }
}
