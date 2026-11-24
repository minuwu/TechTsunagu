package com.techtsunagu.wakatta.feature.nearmiss.domain

import com.techtsunagu.wakatta.core.model.Tri
import java.io.File
import kotlinx.serialization.Serializable

@Serializable
enum class NearMissSeverity {
    LOW, MID, HIGH
}

@Serializable
enum class NearMissStatus {
    OPEN, ACK
}

/** Percent of the photo's width and height. */
@Serializable
data class PhotoMark(
    val x: Float,
    val y: Float,
)

@Serializable
data class NearMissDto(
    val id: String,
    val userId: String,
    val userName: String = "",
    val machineId: String,
    val text: Tri,
    /** Null when the report was sent without one (the web app). */
    val severity: NearMissSeverity? = null,
    val photoUrl: String? = null,
    val photoMark: PhotoMark? = null,
    val createdAt: String,
    val status: NearMissStatus = NearMissStatus.OPEN,
    val acknowledgedAt: String? = null,
    val acknowledgedBy: String? = null,
    val taskId: String? = null,
    val isQueuedOffline: Boolean = false,
)

data class NearMissCreateRequest(
    val machineId: String,
    val text: String,
    val severity: NearMissSeverity,
    val photoFile: File? = null,
    val photoUrl: String? = null,
    val photoMark: PhotoMark? = null,
    val audioFile: File? = null,
    val idempotencyKey: String,
)

@Serializable
data class AckResult(
    val id: String,
    val status: NearMissStatus,
    val acknowledgedAt: String,
    val acknowledgedBy: String,
)
