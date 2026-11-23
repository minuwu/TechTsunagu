package com.techtsunagu.wakatta.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.techtsunagu.wakatta.core.model.CacheEntry

@Entity(tableName = "cache_entry")
data class CacheEntryEntity(
    @PrimaryKey val key: String,
    val json: String,
    val etag: String? = null,
    val fetchedAt: Long = 0L
) {
    fun toModel(): CacheEntry = CacheEntry(
        key = key,
        json = json,
        etag = etag,
        fetchedAt = fetchedAt
    )

    companion object {
        fun fromModel(model: CacheEntry): CacheEntryEntity = CacheEntryEntity(
            key = model.key,
            json = model.json,
            etag = model.etag,
            fetchedAt = model.fetchedAt
        )
    }
}
