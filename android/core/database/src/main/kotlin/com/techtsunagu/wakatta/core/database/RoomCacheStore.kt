package com.techtsunagu.wakatta.core.database

import com.techtsunagu.wakatta.core.model.CacheEntry
import com.techtsunagu.wakatta.core.model.CacheStore

class RoomCacheStore(
    private val cacheEntryDao: CacheEntryDao
) : CacheStore {

    override suspend fun get(key: String): CacheEntry? {
        return cacheEntryDao.get(key)?.toModel()
    }

    override suspend fun put(key: String, json: String, etag: String?) {
        val entity = CacheEntryEntity(
            key = key,
            json = json,
            etag = etag,
            fetchedAt = System.currentTimeMillis()
        )
        cacheEntryDao.insert(entity)
    }

    override suspend fun evict(prefix: String) {
        cacheEntryDao.deleteByPrefix(prefix)
    }
}
