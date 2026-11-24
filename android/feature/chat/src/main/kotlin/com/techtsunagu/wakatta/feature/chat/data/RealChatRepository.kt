package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CacheStore
import com.techtsunagu.wakatta.core.model.appLanguage
import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.ThreadSummary
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ConditionalGet
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.core.network.safeApiCallUnit
import com.techtsunagu.wakatta.feature.chat.model.AnswerResultDto
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel
import com.techtsunagu.wakatta.feature.chat.model.FollowUpDto
import com.techtsunagu.wakatta.feature.chat.model.ReexplainDraft
import com.techtsunagu.wakatta.feature.chat.model.ReexplainRequest
import com.techtsunagu.wakatta.feature.chat.model.ThreadDto
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

/** Chat over the live routes. */
internal class RealChatRepository(
    private val api: () -> ChatApi,
    private val session: SessionStore,
    private val workers: WorkerRepository,
    private val glossary: GlossaryRepository,
    private val cache: CacheStore,
    private val pollMs: Long = POLL_MS,
) : ChatRepository {

    private val conditional = ConditionalGet(cache)

    // After our own write the poll loop wakes at once instead of waiting for the next tick.
    private val kick = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private var cachedNames: Map<String, String>? = null

    private val user: User? get() = session.session.value?.user

    private val askingInFlight = ConcurrentHashMap.newKeySet<String>()

    // The unsent drafts as the server gave them: Send must echo their questions and Tasks.
    private val drafts = ConcurrentHashMap<String, WireSenderMessage>()

    override fun getThreads(): Flow<List<ThreadDto>> = flow {
        when (val result = safeApiCall { api().threads() }) {
            is AppResult.Success -> {
                val me = user
                emit(result.value.map { it.toDto(me) })
            }
            // The Chats list has no error channel yet, so a failed load shows the empty list; Refresh asks again.
            is AppResult.Failure -> emit(emptyList())
        }
    }

    // shortcut: the thread row has no last-message preview, wire when the backend ships it.
    override fun getThreadSummaries(): Flow<List<ThreadSummary>> = flow {
        when (val result = safeApiCall { api().threads() }) {
            is AppResult.Success -> {
                val me = user
                emit(result.value.map { it.toDto(me).toSummary(me) })
            }
            is AppResult.Failure -> emit(emptyList())
        }
    }

    /**
     * Polls every [pollMs] with `If-None-Match`. The first failure ends the flow (the screen shows Retry); later
     * network failures are skipped. shortcut: no `after` cursor, so a thread past the server cap of messages
     * (MAX_MESSAGES) would not show the newest, wire `after` when threads grow that long.
     */
    override fun getMessages(threadId: String): Flow<List<ChatMessageUiModel>> = flow {
        val key = "chat:$threadId"
        cache.evict(key)
        var shownEtag: String? = null
        var loaded = false
        while (true) {
            val me = user ?: throw ChatLoadException(AppError.SessionExpired)
            when (val result = poll(key, threadId, me)) {
                is AppResult.Success -> {
                    val etag = cache.get(key)?.etag
                    if (!loaded || etag != shownEtag) emit(result.value)
                    shownEtag = etag
                    loaded = true
                }
                is AppResult.Failure -> if (!loaded || result.error.isFinal()) throw ChatLoadException(result.error)
            }
            withTimeoutOrNull(pollMs) { kick.first() }
        }
    }

    private suspend fun poll(key: String, threadId: String, me: User): AppResult<List<ChatMessageUiModel>> {
        val ctx = MessageContext(me.id, me.language, names(me))
        return if (me.role == Role.FOREMAN) {
            conditional.get(key, WirePage.serializer(WireSenderMessage.serializer())) { api().messages(threadId, it) }
                .mapOk { page -> page.items.map { it.toUiKeepingDraft(ctx) } }
        } else {
            conditional.get(key, WirePage.serializer(WireWorkerMessage.serializer())) { api().messages(threadId, it) }
                .mapOk { page -> page.items.map { it.toUi(ctx) } }
        }
    }

    /** shortcut: a Worker may not list accounts (403), so only their own name shows, wire when messages carry the sender name. */
    private suspend fun names(me: User): Map<String, String> {
        cachedNames?.let { return it }
        val others = if (me.role == Role.FOREMAN) (workers.getWorkers() as? AppResult.Success)?.value.orEmpty() else emptyList()
        val map = others.associate { it.id to it.displayName } + (me.id to me.displayName)
        if (me.role != Role.FOREMAN || others.isNotEmpty()) cachedNames = map
        return map
    }

    override suspend fun sendMessage(
        threadId: String,
        text: String?,
        audio: File?,
        isVoice: Boolean,
        fromUserId: String,
        idempotencyKey: String,
    ): AppResult<ChatMessageUiModel> {
        val me = user ?: return AppResult.Failure(AppError.SessionExpired)
        val hasText = !text.isNullOrBlank()
        val audioProvided = audio != null
        if (hasText == audioProvided || (audioProvided && (!audio!!.exists() || audio.length() <= 0))) {
            return AppResult.Failure(AppError.Validation(listOf("body")))
        }

        val textPart = if (hasText) MultipartBody.Part.createFormData("text", text!!) else null
        val audioPart = if (audioProvided) {
            val file = audio!!
            val fileBody = file.asRequestBody("audio/mp4".toMediaType())
            MultipartBody.Part.createFormData("audio", file.name, fileBody)
        } else null

        val draft = when (val r = safeApiCall {
            api().post(
                id = threadId,
                key = IdempotencyKey(idempotencyKey),
                text = textPart,
                audio = audioPart,
            )
        }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        return done(draft.toUiKeepingDraft(MessageContext(me.id, me.language, names(me))))
    }

    override suspend fun sendDraft(messageId: String): AppResult<ChatMessageUiModel> {
        val me = user ?: return AppResult.Failure(AppError.SessionExpired)
        val draft = drafts[messageId] ?: return AppResult.Failure(AppError.NotFound)
        val body = draft.toSendBody()
        if (body.text["ja"].isNullOrBlank()) return AppResult.Failure(AppError.Validation(listOf("text")))
        val ctx = MessageContext(me.id, me.language, names(me))
        return when (val sent = safeApiCall { api().send(messageId, body) }) {
            is AppResult.Success -> done(sent.value.toUiKeepingDraft(ctx))
            is AppResult.Failure -> {
                // a retry of a Send that did arrive
                if ((sent.error as? AppError.Conflict)?.code != "ALREADY_SENT") return sent
                drafts.remove(messageId)
                done(draft.toUi(ctx).copy(isDraft = false, needsEdit = false))
            }
        }
    }

    private fun WireSenderMessage.toUiKeepingDraft(ctx: MessageContext): ChatMessageUiModel {
        if (sentAt == null) drafts[id] = this else drafts.remove(id)
        return toUi(ctx)
    }

    private fun done(message: ChatMessageUiModel): AppResult<ChatMessageUiModel> {
        kick.tryEmit(Unit)
        return AppResult.Success(message)
    }

    override suspend fun submitAnswers(
        messageId: String,
        userId: String,
        answers: List<Int>,
        idempotencyKey: String,
    ): AppResult<AnswerResultDto> {
        // The route takes no Idempotency-Key (one answer per message and Worker): a repeat is safe on its own.
        val result = safeApiCall { api().answers(messageId, AnswersInDto(answers)) }.mapOk { it.toDto() }
        if (result.isSuccess) kick.tryEmit(Unit)
        return result
    }

    override suspend fun editDraft(
        messageId: String,
        textJa: String,
    ): AppResult<ChatMessageUiModel> {
        val me = user ?: return AppResult.Failure(AppError.SessionExpired)
        val ctx = MessageContext(me.id, me.language, names(me))
        return when (val r = safeApiCall { api().editDraft(messageId, DraftEditDto(textJa)) }) {
            is AppResult.Success -> {
                kick.tryEmit(Unit)
                AppResult.Success(r.value.toUiKeepingDraft(ctx))
            }
            is AppResult.Failure -> {
                if ((r.error as? AppError.Conflict)?.code == "ALREADY_SENT") {
                    kick.tryEmit(Unit)
                }
                r
            }
        }
    }

    override suspend fun transcribe(audio: File, language: String): AppResult<String> = safeApiCall {
        api().transcribe(
            language = MultipartBody.Part.createFormData("language", language),
            audio = MultipartBody.Part.createFormData("audio", audio.name, audio.asRequestBody("audio/mp4".toMediaType())),
        )
    }.mapOk { it.text }

    override suspend fun getReexplainDraft(messageId: String, workerId: String): AppResult<ReexplainDraft> {
        val followUp = when (val r = getFollowUps()) {
            is AppResult.Success -> r.value.firstOrNull { it.messageId == messageId && it.userId == workerId }
            is AppResult.Failure -> return r
        } ?: return AppResult.Failure(AppError.NotFound)
        val workerInfo = (workers.getWorkers() as? AppResult.Success)?.value?.firstOrNull { it.id == workerId }
        val lang = appLanguage(workerInfo?.language)
        val name = followUp.displayName.ifEmpty { workerInfo?.displayName.orEmpty() }
        val draft = when (val r = safeApiCall {
            api().reexplainDraft(messageId, ReexplainInDto(userId = workerId, termJa = followUp.termJa))
        }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        val easyJa = draft.text["ja"].orEmpty()
        val explanation = draft.text.filterKeys { it != "ja" }
        return AppResult.Success(
            ReexplainDraft(
                workerName = name,
                workerLang = lang,
                termJa = followUp.termJa,
                easyJa = easyJa,
                explanation = explanation,
                newQuestionPrompt = draft.question.prompt,
                newQuestionOptions = draft.question.options,
                newQuestionCorrectIndex = draft.question.correctIndex,
            ),
        )
    }

    override suspend fun reexplain(messageId: String, request: ReexplainRequest): AppResult<Unit> {
        val textMap = buildMap<String, String> {
            if (request.easyJa.isNotBlank()) put("ja", request.easyJa)
            putAll(request.explanation)
        }
        val hasText = textMap.isNotEmpty()
        val hasQuestion = request.newQuestionPrompt.isNotEmpty()
        if (hasText != hasQuestion) {
            return AppResult.Failure(AppError.Validation(listOf("text", "question")))
        }
        val body = if (hasText && hasQuestion) {
            val question = WireSenderQuestion(
                id = "q1",
                termId = null,
                prompt = request.newQuestionPrompt,
                options = request.newQuestionOptions,
                correctIndex = request.newQuestionCorrectIndex,
            )
            ReexplainInDto(
                userId = request.userId,
                termJa = request.termJa,
                text = textMap,
                question = question,
            )
        } else {
            ReexplainInDto(
                userId = request.userId,
                termJa = request.termJa,
            )
        }
        val result = safeApiCall { api().reexplain(messageId, body) }
        if (result.isSuccess) kick.tryEmit(Unit)
        return result.mapOk { }
    }

    override suspend fun nudge(messageId: String, userId: String): AppResult<Unit> {
        val result = safeApiCallUnit { api().nudge(messageId, NudgeInDto(userId)) }
        if (result.isSuccess) kick.tryEmit(Unit)
        return result
    }

    override suspend fun askExplainAgain(
        messageId: String,
        userId: String,
        termJa: String,
    ): AppResult<Unit> {
        if (!askingInFlight.add(messageId)) {
            return AppResult.Success(Unit)
        }
        try {
            val result = safeApiCall { api().ask(messageId, AskForemanInDto(termJa)) }
            if (result.isSuccess) kick.tryEmit(Unit)
            return result.mapOk { }
        } finally {
            askingInFlight.remove(messageId)
        }
    }

    override suspend fun getTerms(): AppResult<List<GlossaryTerm>> = glossary.getTerms()

    override suspend fun addTerm(termJa: String, readingJa: String, termBn: String, termEn: String) =
        glossary.addTerm(termJa, readingJa, termBn, termEn)

    /** Foreman only; a Worker gets an empty list instead of a 403 (XREQ-020). First 200 only. */
    override suspend fun getFollowUps(): AppResult<List<FollowUpDto>> {
        if (user?.role != Role.FOREMAN) return AppResult.Success(emptyList())
        return safeApiCall { api().followUps(PAGE_LIMIT) }.mapOk { page -> page.items.map { it.toDto() } }
    }

    private fun AppError.isFinal() = this == AppError.SessionExpired || this == AppError.Forbidden || this == AppError.NotFound

    companion object {
        const val POLL_MS = 2_000L
        private const val PAGE_LIMIT = 200
    }
}

/** Ends the message flow; `ChatViewModel` turns it into its error state. */
internal class ChatLoadException(val error: AppError) : Exception(error.toString())
