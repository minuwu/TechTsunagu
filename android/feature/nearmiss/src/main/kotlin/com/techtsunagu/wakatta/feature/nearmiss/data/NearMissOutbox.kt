package com.techtsunagu.wakatta.feature.nearmiss.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.OutboxSender
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissCreateRequest
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.PhotoMark
import java.io.File
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val OUTBOX_NAME = "nearmiss"

internal fun nearMissOutboxKind(userId: String) = "$OUTBOX_NAME:$userId"

/** A report written without a network. [photo] is a file of this app. */
@Serializable
internal data class QueuedNearMiss(
    val machineId: String,
    val text: String,
    val severity: NearMissSeverity,
    val photo: String? = null,
    val photoMark: PhotoMark? = null,
) {
    fun encode(): String = Json.encodeToString(serializer(), this)

    companion object {
        fun of(r: NearMissCreateRequest) = QueuedNearMiss(r.machineId, r.text, r.severity, r.photoFile?.path, r.photoMark)

        fun decode(json: String): QueuedNearMiss? = try {
            Json.decodeFromString(serializer(), json)
        } catch (_: SerializationException) {
            null
        }
    }
}

class NearMissOutboxSender @Inject constructor(private val repository: NearMissRepository) : OutboxSender {
    override val name = OUTBOX_NAME

    override suspend fun send(userId: String, idempotencyKey: String, payloadJson: String): AppResult<Unit> {
        val report = QueuedNearMiss.decode(payloadJson) ?: return AppResult.Failure(AppError.Validation())
        return repository.createNearMiss(
            NearMissCreateRequest(
                machineId = report.machineId,
                text = report.text,
                severity = report.severity,
                // The system may have cleared the cache; the report is worth more than its photo.
                photoFile = report.photo?.let(::File)?.takeIf { it.exists() },
                photoMark = report.photoMark,
                idempotencyKey = idempotencyKey,
            ),
        ).map { }
    }
}
