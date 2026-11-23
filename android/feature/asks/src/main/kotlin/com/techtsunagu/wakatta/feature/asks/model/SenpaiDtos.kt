package com.techtsunagu.wakatta.feature.asks.model

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class SenpaiPairDto(
    val id: String,
    val workerId: String,
    val workerName: String,
    val workerLanguage: String = "bn",
    val senpaiName: String,
    val senpaiEnName: String? = null,
    val kind: String = "coworker",
    val meetupCount: Int = 0,
    val lastMeetupDate: String? = null,
    val createdAt: String? = null,
    val expiresAt: String? = null,
)

@Serializable
data class SenpaiPairCreateRequest(
    val workerId: String,
    val senpaiName: String,
    val kind: String = "coworker",
)

@Serializable
data class SenpaiPairCreatedDto(
    val id: String,
    val workerId: String,
    val senpaiName: String,
    val kind: String = "coworker",
    val token: String,
)

@Serializable
data class MeetupDto(
    val id: String,
    val workerId: String,
    val workerName: String,
    val senpaiId: String,
    val senpaiName: String,
    val kind: String, // coffee, drill, festival, other
    val date: String, // YYYY-MM-DD
    val note: String,
    val loggedBy: String = "worker", // worker or senpai
)

data class MeetupCreateRequest(
    val workerId: String? = null,
    val kind: String,
    val date: String,
    val note: String,
    val idempotencyKey: String = UUID.randomUUID().toString(),
)

@Serializable
data class TodayWordDto(
    val workerId: String,
    val workerName: String,
    val languageName: String,
    val word: String,
    val reading: String,
    val meaningJa: String,
    val meaningEn: String? = null,
    val meaningBn: String? = null,
)
