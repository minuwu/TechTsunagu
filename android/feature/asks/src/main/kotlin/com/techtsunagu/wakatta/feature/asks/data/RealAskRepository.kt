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
 * Asks over `GET|POST /asks` and `POST /asks/{id}/forward`. A Foreman only reads the shop's Asks: the Senpai replies
 * through a link, not this app.
 */
internal class RealAskRepository(
    private val api: () -> AsksApi,
    private val session: SessionStore,
) : AskRepository {

    private val asks = MutableStateFlow<List<AskDto>>(emptyList())

    private val me get() = session.session.value?.user?.takeIf { it.role == Role.WORKER }

    /** Throws [LoadFailed] when the list cannot be loaded. The server decides whose Asks the caller sees. */
    override fun getAsks(workerId: String?): Flow<List<AskDto>> = asks.onStart { refresh() }

    private suspend fun refresh() {
        val senpai = if (me == null) null else api().pairOrNull()?.senpaiName
        val all = allPages { cursor -> api().asks(PAGE_LIMIT, cursor) }.orThrow()
        // The server lists newest first; the chat shows the oldest first.
        asks.value = all.reversed().map { it.toAsk(senpai) }
    }

    /** The Ask itself is saved: a name that cannot be loaded must not make it look lost. */
    private suspend fun senpaiName(): String? = try {
        api().pairOrNull()?.senpaiName
    } catch (_: LoadFailed) {
        null
    }

    override suspend fun sendAsk(workerId: String, request: AskCreateRequest): AppResult<AskDto> {
        val parts = parts(request) ?: return AppResult.Failure(AppError.Validation(listOf("text")))
        val created = when (val r = safeApiCall { api().createAsk(IdempotencyKey(request.idempotencyKey), parts) }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        // A question the Notices do not cover goes to the Senpai at once, as the screen promises; no pair (409) is not an error.
        val sent = if (created.covered) created else
            (safeApiCall { api().forward(created.id) } as? AppResult.Success)?.value ?: created
        val ask = sent.toAsk(senpaiName()).copy(kind = if (request.isVoice) AskKind.VOICE else AskKind.TEXT)
        asks.update { list -> list.filter { it.id != ask.id } + ask } // a retried Ask is the same Ask
        return AppResult.Success(ask)
    }

    override suspend fun forwardAsk(askId: String): AppResult<AskDto> {
        return safeApiCall { api().forward(askId) }.map { dto ->
            dto.toAsk(senpaiName()).also { ask -> asks.update { list -> list.map { if (it.id == askId) ask else it } } }
        }
    }

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
        val reply = senpaiReply
        val noticeId = sourceNoticeIds.firstOrNull()
        return AskDto(
            id = id,
            workerId = workerId,
            workerName = workerName,
            text = text,
            textJa = textJa,
            covered = covered,
            // shortcut: the server sends no notice title or excerpt, only its id and the answer, wire when the backend ships them.
            matchNotice = if (covered && answer != null && noticeId != null) NoticeMatchDto(noticeId, emptyMap(), answer, emptyMap()) else null,
            sourceNoticeIds = sourceNoticeIds,
            status = when {
                reply != null -> AskStatus.REPLIED
                covered -> AskStatus.ANSWERED
                forwarded -> AskStatus.FORWARDED
                else -> AskStatus.OPEN
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
