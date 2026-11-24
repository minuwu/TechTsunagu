package com.techtsunagu.wakatta.feature.briefing.data

import com.techtsunagu.wakatta.core.media.RecordingLimits
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckQuestion
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.briefing.model.BriefingDraft
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForForeman
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForWorker
import com.techtsunagu.wakatta.feature.briefing.model.BriefingPublishRequest
import com.techtsunagu.wakatta.feature.briefing.model.BriefingReceipts
import com.techtsunagu.wakatta.feature.briefing.model.BriefingWorkerStatus
import com.techtsunagu.wakatta.feature.briefing.model.CheckQuestionWithAnswer
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

/**
 * Briefings over `POST /briefings/draft`, `POST|GET /briefings`, `GET /briefings/{id}/receipts`.
 * The Check and the opened receipt go through the shared `CheckRepository` and `ReceiptRepository`, so
 * [markOpened] and [recordWorkerAnswer] have nothing to send here.
 * shortcut: the server has no focus or change text, so both are cut from the Briefing text, wire when the backend ships them.
 */
internal class RealBriefingRepository(
    private val api: () -> BriefingApi,
    private val session: SessionStore,
) : BriefingRepository {

    private val isForeman get() = session.session.value?.user?.role == Role.FOREMAN

    override fun observeBriefings(): Flow<List<BriefingForForeman>> = flow {
        (getBriefings() as? AppResult.Success)?.let { emit(it.value) }
    }

    override suspend fun getBriefings(): AppResult<List<BriefingForForeman>> =
        if (isForeman) allPages { api().foremanPage(PAGE_LIMIT, it) }.map { l -> l.map { it.toForeman() } }
        else allPages { api().workerPage(PAGE_LIMIT, it) }.map { l -> l.map { it.toForeman() } }

    override suspend fun getBriefingForForeman(id: String?): AppResult<BriefingForForeman> =
        getBriefings().pick(id) { it.id }

    override suspend fun getBriefingForWorker(id: String?): AppResult<BriefingForWorker> =
        allPages { api().workerPage(PAGE_LIMIT, it) }.pick(id) { it.id }.map { it.toWorker() }

    override suspend fun createDraft(text: String?, audioFile: File?, idempotencyKey: String): AppResult<BriefingDraft> {
        val part = audioFile?.let {
            MultipartBody.Part.createFormData("audio", it.name, it.asRequestBody(RecordingLimits.MIME_TYPE.toMediaType()))
        } ?: text?.trim()?.takeIf { it.isNotEmpty() }?.let { MultipartBody.Part.createFormData("text", it) }
            ?: return AppResult.Failure(AppError.Validation(listOf("text")))
        return safeApiCall { api().draft(IdempotencyKey(idempotencyKey), listOf(part)) }.map { it.toDraft() }
    }

    // The draft id is the stable key of its one publish, so a retry never publishes twice.
    override suspend fun publishBriefing(request: BriefingPublishRequest): AppResult<BriefingForForeman> {
        val q = request.question
        val body = PublishDto(request.id, request.text, QuestionDto(q.id, q.termId, q.prompt, q.options, q.correctIndex))
        return safeApiCall { api().publish(IdempotencyKey("publish-${request.id}"), body) }.map { it.toForeman() }
    }

    override suspend fun getReceipts(briefingId: String): AppResult<BriefingReceipts> =
        safeApiCall { api().receipts(briefingId) }.map {
            BriefingReceipts(it.understood.map(::info), it.openedOnly.map(::info), it.notOpened.map(::info))
        }

    // shortcut: the server sends only the three receipt groups, no answers; opened-only shows ORANGE, wire when it sends answers.
    override suspend fun getWorkerStatuses(briefingId: String): AppResult<List<BriefingWorkerStatus>> =
        getReceipts(briefingId).map { r ->
            r.understood.map { BriefingWorkerStatus(it, true, WakattaStatus.GREEN) } +
                r.openedOnly.map { BriefingWorkerStatus(it, true, WakattaStatus.ORANGE) } +
                r.notOpened.map { BriefingWorkerStatus(it, false, WakattaStatus.WAIT) }
        }

    // shortcut: no route re-explains a Briefing to one Worker, wire when the backend ships it.
    override suspend fun sendReexplanation(briefingId: String, workerId: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun markOpened(briefingId: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun recordWorkerAnswer(
        briefingId: String,
        workerId: String,
        answerIndex: Int,
        status: WakattaStatus,
    ): AppResult<Unit> = AppResult.Success(Unit)

    private fun info(w: ReceiptWorkerDto) = WorkerInfo(w.id, w.name, language = "") // shortcut: receipts carry no language.

    private companion object {
        const val PAGE_LIMIT = 50
    }
}

private inline fun <T> AppResult<List<T>>.pick(id: String?, idOf: (T) -> String): AppResult<T> = when (this) {
    is AppResult.Failure -> this
    is AppResult.Success -> (if (id == null) value.firstOrNull() else value.find { idOf(it) == id })
        ?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.NotFound)
}

internal inline fun <A, B> AppResult<A>.map(transform: (A) -> B): AppResult<B> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

/** First sentence of each language is the focus, the rest the change; a one-sentence text stays whole in both. */
internal fun splitFocusChange(text: Tri): Pair<Tri, Tri> {
    val parts = text.mapValues { (_, t) -> t.trim().split(SENTENCE_END, limit = 2) }
    return parts.mapValues { it.value.first() } to parts.mapValues { it.value.last() }
}

private val SENTENCE_END = Regex("(?<=[.。!?！？])\\s*")

private fun QuestionDto.toModel() = CheckQuestionWithAnswer(id, prompt, options, correctIndex, termId)

private fun ForemanBriefingDto.toForeman(): BriefingForForeman {
    val (focus, change) = splitFocusChange(text)
    return BriefingForForeman(id, text, focus, change, createdAt, publishedAt, question.toModel(), status, needsEdit)
}

private fun ForemanBriefingDto.toDraft(): BriefingDraft {
    val (focus, change) = splitFocusChange(text)
    return BriefingDraft(id, text, focus, change, question.toModel(), transcriptJa, needsEdit)
}

// A Worker's list has no answers: correctIndex 0 is a placeholder, and Worker screens read getBriefingForWorker.
private fun WorkerBriefingDto.toForeman(): BriefingForForeman {
    val (focus, change) = splitFocusChange(text)
    val q = CheckQuestionWithAnswer(question.id, question.prompt, question.options, correctIndex = 0, termId = question.termId)
    return BriefingForForeman(id, text, focus, change, createdAt, publishedAt, q, status)
}

private fun WorkerBriefingDto.toWorker(): BriefingForWorker {
    val (focus, change) = splitFocusChange(text)
    return BriefingForWorker(
        id, text, focus, change, createdAt, publishedAt,
        question = CheckQuestion(question.id, question.termId, question.prompt, question.options),
        status = status,
        opened = opened,
        passed = passed,
        // shortcut: the server does not say which option the Worker chose, wire when it does.
        wakattaStatus = when (passed) { true -> WakattaStatus.GREEN; false -> WakattaStatus.RED; null -> WakattaStatus.WAIT },
    )
}
