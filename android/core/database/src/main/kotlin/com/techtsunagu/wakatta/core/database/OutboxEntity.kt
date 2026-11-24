package com.techtsunagu.wakatta.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey val key: String,
    val kind: String,
    val payloadJson: String,
    val createdAt: Long,
    val attempts: Int = 0,
    val nextAttemptAt: Long = 0L,
    val state: String = STATE_PENDING
) {
    companion object {
        const val STATE_PENDING = "PENDING"
        const val STATE_SENDING = "SENDING"
        const val STATE_FAILED = "FAILED"
    }
}
