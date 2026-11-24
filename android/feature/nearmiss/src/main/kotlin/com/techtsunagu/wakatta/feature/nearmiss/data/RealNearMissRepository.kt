package com.techtsunagu.wakatta.feature.nearmiss.data

import com.techtsunagu.wakatta.core.media.RecordingLimits
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.nearmiss.domain.AckResult
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissCreateRequest
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissDto
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import com.techtsunagu.wakatta.feature.nearmiss.domain.PhotoMark
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

private const val PAGE_LIMIT = 50
private const val PERCENT = 100f

/**
 * Near-miss Reports: the server gives a Worker their own reports and a Foreman the shop's.
 * The caller owns the Idempotency-Key and the offline queue (the screen queues a failed send in the Outbox with the same key).
 */
internal class RealNearMissRepository(private val api: () -> NearMissApi) : NearMissRepository {

    override suspend fun getNearMisses(): AppResult<List<NearMissDto>> =
        allPages { api().reports(PAGE_LIMIT, it) }.map { l -> l.map { it.toDto() } }

    override suspend fun getNearMiss(id: String): AppResult<NearMissDto> =
        safeApiCall { api().report(id) }.map { it.toDto() }

    override suspend fun createNearMiss(request: NearMissCreateRequest): AppResult<NearMissDto> {
        val text = request.text.trim()
        if (text.isEmpty() && request.audioFile == null) {
            return AppResult.Failure(AppError.Validation(listOf("text")))
        }
        val parts = buildList {
            add(MultipartBody.Part.createFormData("machineId", request.machineId))
            if (text.isNotEmpty()) {
                add(MultipartBody.Part.createFormData("text", text))
            }
            request.audioFile?.let {
                add(MultipartBody.Part.createFormData("audio", it.name, it.asRequestBody(RecordingLimits.MIME_TYPE.toMediaType())))
            }
            request.photoFile?.let {
                add(MultipartBody.Part.createFormData("photo", it.name, it.asRequestBody("image/jpeg".toMediaType())))
                request.photoMark?.let { mark ->
                    add(MultipartBody.Part.createFormData("photoMarkX", (mark.x / PERCENT).coerceIn(0f, 1f).toString()))
                    add(MultipartBody.Part.createFormData("photoMarkY", (mark.y / PERCENT).coerceIn(0f, 1f).toString()))
                }
            }
            add(MultipartBody.Part.createFormData("severity", request.severity.name.lowercase()))
        }
        return safeApiCall { api().report(IdempotencyKey(request.idempotencyKey), parts) }.map { it.toDto() }
    }

    override suspend fun acknowledge(id: String, acknowledgedBy: String): AppResult<AckResult> =
        safeApiCall { api().ack(id) }.map {
            AckResult(it.id, NearMissStatus.ACK, it.acknowledgedAt.orEmpty(), acknowledgedBy)
        }

    override suspend fun linkTask(nearMissId: String, taskId: String): AppResult<NearMissDto> =
        safeApiCall { api().linkTask(nearMissId, NearMissTaskInDto(taskId)) }.map { it.toDto() }
}

internal inline fun <A, B> AppResult<A>.map(transform: (A) -> B): AppResult<B> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

// The server does not say who acknowledged; the photo is read from its route.
internal fun NearMissOutDto.toDto() = NearMissDto(
    id = id,
    userId = workerId,
    userName = workerName,
    machineId = machineId,
    text = text,
    severity = NearMissSeverity.entries.find { it.name.equals(severity, ignoreCase = true) },
    photoUrl = photo?.let { ApiPaths.NEAR_MISS_PHOTO.replace("{report_id}", id) },
    photoMark = photoMark?.let { PhotoMark(it.x * PERCENT, it.y * PERCENT) },
    createdAt = createdAt,
    status = if (acknowledgedAt != null) NearMissStatus.ACK else NearMissStatus.OPEN,
    acknowledgedAt = acknowledgedAt,
    taskId = taskId,
)
