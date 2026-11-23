package com.techtsunagu.wakatta.feature.nearmiss.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.nearmiss.domain.AckResult
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissCreateRequest
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissDto
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import java.util.concurrent.CopyOnWriteArrayList
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

/**
 * Near-miss reports over `POST|GET /near-misses`, `POST /near-misses/{id}/ack`. The caller owns the
 * Idempotency-Key and the offline queue (the screen queues a failed send in the Outbox with the same key).
 * shortcut: a Worker cannot list their own reports (`GET /near-misses` is Foreman-only), so a Worker sees what
 * they sent in this session; wire when the backend ships it (XREQ-023).
 */
internal class RealNearMissRepository(
    private val api: () -> NearMissApi,
    private val session: SessionStore,
) : NearMissRepository {

    private val sent = CopyOnWriteArrayList<NearMissDto>()

    private val isForeman get() = session.session.value?.user?.role == Role.FOREMAN

    override suspend fun getNearMisses(): AppResult<List<NearMissDto>> =
        if (isForeman) allPages { api().reports(PAGE_LIMIT, it) }.map { l -> l.map { it.toDto() } }
        else AppResult.Success(sent.toList())

    override suspend fun getNearMiss(id: String): AppResult<NearMissDto> =
        when (val all = getNearMisses()) {
            is AppResult.Failure -> all
            is AppResult.Success -> all.value.find { it.id == id }?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.NotFound)
        }

    override suspend fun createNearMiss(request: NearMissCreateRequest): AppResult<NearMissDto> {
        val text = request.text.trim().ifEmpty { return AppResult.Failure(AppError.Validation(listOf("text"))) }
        val parts = buildList {
            add(MultipartBody.Part.createFormData("machineId", request.machineId))
            add(MultipartBody.Part.createFormData("text", text))
            request.photoFile?.let {
                add(MultipartBody.Part.createFormData("photo", it.name, it.asRequestBody("image/jpeg".toMediaType())))
            }
        }
        return safeApiCall { api().report(IdempotencyKey(request.idempotencyKey), parts) }.map { out ->
            // shortcut: no severity on the server, the Worker keeps the one just picked.
            out.toDto(request.severity).also { sent.add(0, it) }
        }
    }

    override suspend fun acknowledge(id: String, acknowledgedBy: String): AppResult<AckResult> =
        safeApiCall { api().ack(id) }.map {
            AckResult(it.id, NearMissStatus.ACK, it.acknowledgedAt.orEmpty(), acknowledgedBy)
        }

    // shortcut: no Task link on the server, the screen then shows the link locally, wire when the backend ships it (XREQ-023).
    override suspend fun linkTask(nearMissId: String, taskId: String): AppResult<NearMissDto> =
        AppResult.Failure(AppError.Unknown("Task link is not on the server"))

    private companion object {
        const val PAGE_LIMIT = 50
    }
}

internal inline fun <A, B> AppResult<A>.map(transform: (A) -> B): AppResult<B> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

// shortcut: no severity (MID), no photo mark and no acknowledger id on the server; the photo is read from its route.
internal fun NearMissOutDto.toDto(severity: NearMissSeverity = NearMissSeverity.MID) = NearMissDto(
    id = id,
    userId = workerId,
    machineId = machineId,
    text = text,
    severity = severity,
    photoUrl = photo?.let { ApiPaths.NEAR_MISS_PHOTO.replace("{report_id}", id) },
    createdAt = createdAt,
    status = if (acknowledgedAt != null) NearMissStatus.ACK else NearMissStatus.OPEN,
    acknowledgedAt = acknowledgedAt,
)
