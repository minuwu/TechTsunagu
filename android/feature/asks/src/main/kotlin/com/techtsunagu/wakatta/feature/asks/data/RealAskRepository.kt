package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.media.RecordingLimits
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.AskDto
import com.techtsunagu.wakatta.feature.asks.model.AskKind
import com.techtsunagu.wakatta.feature.asks.model.AskStatus
import com.techtsunagu.wakatta.feature.asks.model.NoticeMatchDto
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

/**
 * A Worker's Asks over `GET|POST /asks` and `POST /asks/{id}/forward`. The Foreman's inbox and the Senpai's
 * reply keep the [fake]: `GET /asks` is Worker-only and the Senpai replies through a link token, not this app.
 * shortcut: Foreman reads and replies are Fake, wire when the backend lets a Foreman list Asks (XREQ-024).
 */
internal class RealAskRepository(
    private val api: () -> AsksApi,
    private val session: SessionStore,
    private val fake: AskRepository,
) : AskRepository {

    private val asks = MutableStateFlow<List<AskDto>>(emptyList())

    private val me get() = session.session.value?.user?.takeIf { it.role == Role.WORKER }

    override fun getAsks(workerId: String?): Flow<List<AskDto>> =
        if (me == null) fake.getAsks(workerId) else asks.onStart { refresh() }

    private suspend fun refresh() {
        val senpai = api().pairOrNull()?.senpaiName
        val all = allPages { cursor -> api().asks(PAGE_LIMIT, cursor) }
        // The server lists newest first; the chat shows the oldest first.
        if (all is AppResult.Success) asks.value = all.value.reversed().map { it.toAsk(senpai) }
    }

    override suspend fun sendAsk(workerId: String, request: AskCreateRequest): AppResult<AskDto> {
        if (me == null) return fake.sendAsk(workerId, request)
        val parts = parts(request) ?: return AppResult.Failure(AppError.Validation(listOf("text")))
        val created = when (val r = safeApiCall { api().createAsk(IdempotencyKey(request.idempotencyKey), parts) }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        // A question the Notices do not cover goes to the Senpai at once, as the screen promises; no pair (409) is not an error.
        val sent = if (created.covered) created else
            (safeApiCall { api().forward(created.id) } as? AppResult.Success)?.value ?: created
        val ask = sent.toAsk(api().pairOrNull()?.senpaiName).copy(kind = if (request.isVoice) AskKind.VOICE else AskKind.TEXT)
        asks.update { it + ask }
        return AppResult.Success(ask)
    }

    override suspend fun forwardAsk(askId: String): AppResult<AskDto> {
        if (me == null) return fake.forwardAsk(askId)
        return safeApiCall { api().forward(askId) }.map { dto ->
            dto.toAsk(api().pairOrNull()?.senpaiName).also { ask -> asks.update { list -> list.map { if (it.id == askId) ask else it } } }
        }
    }

    // shortcut: no route lets a Foreman answer an Ask (the Senpai replies by link token), wire when the backend ships it.
    override suspend fun replyAsk(askId: String, replyJa: String, replyEn: String, replyBn: String): AppResult<AskDto> =
        fake.replyAsk(askId, replyJa, replyEn, replyBn)

    /** A voice Ask sends the recording, a text Ask sends the text; the server needs at least one part. */
    private fun parts(request: AskCreateRequest): List<MultipartBody.Part>? {
        request.audioFile?.let {
            val body = it.asRequestBody(RecordingLimits.MIME_TYPE.toMediaType())
            return listOf(MultipartBody.Part.createFormData("audio", it.name, body))
        }
        val text = request.text.trim().ifEmpty { return null }
        return listOf(MultipartBody.Part.createFormData("text", text))
    }

    private fun AskOutDto.toAsk(senpaiName: String?): AskDto {
        val user = checkNotNull(me)
        val reply = senpaiReply
        val noticeId = sourceNoticeIds.firstOrNull()
        return AskDto(
            id = id,
            workerId = user.id,
            workerName = user.displayName,
            text = text,
            covered = covered,
            // shortcut: the server sends no notice title or excerpt, only its id and the answer, wire when the backend ships them.
            matchNotice = if (covered && answer != null && noticeId != null) NoticeMatchDto(noticeId, emptyMap(), answer, emptyMap()) else null,
            sourceNoticeIds = sourceNoticeIds,
            status = when {
                reply != null -> AskStatus.REPLIED
                covered -> AskStatus.ANSWERED
                else -> AskStatus.FORWARDED // shortcut: an Ask that nobody could forward (no Senpai) still shows as forwarded.
            },
            senpaiName = senpaiName,
            senpaiEnName = senpaiName,
            replyJa = reply?.get("ja"),
            replyEn = reply?.get("en"),
            replyBn = reply?.get("bn"),
            createdAt = runCatching { OffsetDateTime.parse(createdAt).atZoneSameInstant(TOKYO).format(STAMP) }.getOrDefault(createdAt),
        )
    }

    private companion object {
        const val PAGE_LIMIT = 200
        val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")
        val STAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
    }
}

internal inline fun <A, B> AppResult<A>.map(transform: (A) -> B): AppResult<B> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}
