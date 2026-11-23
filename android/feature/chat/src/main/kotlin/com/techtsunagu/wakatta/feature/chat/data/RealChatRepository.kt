package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CacheStore
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Chat over the live routes. A Foreman's `sendMessage` is the two server steps in one call (draft, then Send).
 * [fallback] (the Fake) answers only what has no route yet: dictation and "ask the Foreman".
 */
internal class RealChatRepository(
    private val api: () -> ChatApi,
    private val session: SessionStore,
    private val workers: WorkerRepository,
    private val glossary: GlossaryRepository,
    private val fallback: ChatRepository,
    private val pollMs: Long = POLL_MS,
    private val cache: CacheStore = MemoryCacheStore(),
) : ChatRepository {

    private val conditional = ConditionalGet(cache)

    // After our own write the poll loop wakes at once instead of waiting for the next tick.
    private val kick = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private var cachedNames: Map<String, String>? = null

    private val user: User? get() = session.session.value?.user

    override fun getThreads(): Flow<List<ThreadDto>> = flow {
        when (val result = safeApiCall { api().threads() }) {
            is AppResult.Success -> emit(result.value.map { it.toDto() })
            // The Chats list has no error channel yet, so a failed load shows the empty list; Refresh asks again.
            is AppResult.Failure -> emit(emptyList())
        }
    }

    // shortcut: the thread row has no last-message preview, wire when the backend ships it.
    override fun getThreadSummaries(): Flow<List<ThreadSummary>> =
        getThreads().map { list -> list.map { it.toSummary() } }

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
                .mapOk { page -> page.items.map { it.toUi(ctx) } }
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
        text: String,
        isVoice: Boolean,
        fromUserId: String,
        idempotencyKey: String,
    ): AppResult<ChatMessageUiModel> {
        val me = user ?: return AppResult.Failure(AppError.SessionExpired)
        // shortcut: dictation has no route here (audio would go in the `audio` part), so a voice message is sent as its text.
        val body = text.toRequestBody("text/plain; charset=utf-8".toMediaType())
        val draft = when (val r = safeApiCall { api().post(threadId, IdempotencyKey(idempotencyKey), body) }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        val ctx = MessageContext(me.id, me.language, names(me))
        // A Worker's message is sent at once; a Foreman's is a draft until Send. ALREADY_SENT on a retry means done.
        if (me.role != Role.FOREMAN || draft.sentAt != null) return done(draft.toUi(ctx))
        return when (val sent = safeApiCall { api().send(draft.id, draft.toSendBody(text)) }) {
            is AppResult.Success -> done(sent.value.toUi(ctx))
            is AppResult.Failure ->
                if ((sent.error as? AppError.Conflict)?.code == "ALREADY_SENT") done(draft.toUi(ctx)) else sent
        }
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

    // shortcut: no route for dictation, so the Fake answers with its sample text, wire when the backend ships it.
    override suspend fun transcribe(audio: File?): AppResult<String> = fallback.transcribe(audio)

    /**
     * shortcut: the server has no draft route (XREQ-022), so the dialog gets the Worker and the term from the
     * open Follow-up and empty text; the server writes the Re-explanation itself.
     */
    override suspend fun getReexplainDraft(messageId: String, workerId: String): AppResult<ReexplainDraft> {
        val followUp = when (val r = getFollowUps()) {
            is AppResult.Success -> r.value.firstOrNull { it.messageId == messageId && it.userId == workerId }
            is AppResult.Failure -> return r
        } ?: return AppResult.Failure(AppError.NotFound)
        val lang = (workers.getWorkers() as? AppResult.Success)?.value?.firstOrNull { it.id == workerId }?.language ?: "en"
        return AppResult.Success(
            ReexplainDraft(followUp.displayName, lang, followUp.termJa, "", emptyMap(), emptyMap(), emptyList()),
        )
    }

    /** shortcut: the server takes only the Worker and the term (`ReexplainIn`); the edited text is not sent, XREQ-022. */
    override suspend fun reexplain(messageId: String, request: ReexplainRequest): AppResult<Unit> {
        val result = safeApiCall { api().reexplain(messageId, ReexplainInDto(request.userId, request.termJa)) }
        if (result.isSuccess) kick.tryEmit(Unit)
        return result.mapOk { }
    }

    override suspend fun nudge(messageId: String, userId: String): AppResult<Unit> {
        val result = safeApiCallUnit { api().nudge(messageId, NudgeInDto(userId)) }
        if (result.isSuccess) kick.tryEmit(Unit)
        return result
    }

    // shortcut: "ask the Foreman" has no route (XREQ-022), so the Fake keeps it, wire when the backend ships it.
    override suspend fun askExplainAgain(messageId: String, userId: String): AppResult<Unit> =
        fallback.askExplainAgain(messageId, userId)

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
