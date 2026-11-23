package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CacheEntry
import com.techtsunagu.wakatta.core.model.CacheStore
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.MessageKind
import com.techtsunagu.wakatta.core.model.ThreadSummary
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.chat.model.AnswerResultDto
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel
import com.techtsunagu.wakatta.feature.chat.model.CheckQuestionForSender
import com.techtsunagu.wakatta.feature.chat.model.CheckQuestionForWorker
import com.techtsunagu.wakatta.feature.chat.model.FollowUpDto
import com.techtsunagu.wakatta.feature.chat.model.ThreadDto
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

/** Who is looking: decides `isMine`, the text language and the sender names. */
internal class MessageContext(val myId: String, val myLang: String, val names: Map<String, String>)

internal inline fun <reified E : Enum<E>> wireEnum(wire: String?, fallback: E): E =
    enumValues<E>().firstOrNull { it.name.equals(wire, ignoreCase = true) } ?: fallback

internal fun wireStatus(wire: String?): WakattaStatus? =
    if (wire == null) null else wireEnum(wire, WakattaStatus.UNKNOWN)

internal inline fun <A, B> AppResult<A>.mapOk(transform: (A) -> B): AppResult<B> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

private fun instant(iso: String): Instant? = runCatching {
    runCatching { OffsetDateTime.parse(iso).toInstant() }.getOrElse { LocalDateTime.parse(iso).toInstant(ZoneOffset.UTC) }
}.getOrNull()

private val clockFormat = DateTimeFormatter.ofPattern("HH:mm")
private val dayFormat = DateTimeFormatter.ofPattern("M/d")

/** `HH:mm` in the phone's time zone; blank when the server sent nothing readable. */
internal fun clock(iso: String?): String =
    iso?.let(::instant)?.atZone(ZoneId.systemDefault())?.format(clockFormat).orEmpty()

/** The Chats list shows the time for today and the day otherwise. */
internal fun listStamp(iso: String?, today: java.time.LocalDate = java.time.LocalDate.now()): String {
    val at = iso?.let(::instant)?.atZone(ZoneId.systemDefault()) ?: return ""
    return at.format(if (at.toLocalDate() == today) clockFormat else dayFormat)
}

internal fun WireThread.toDto() = ThreadDto(
    id = id,
    titleJa = titleJa,
    kind = kind,
    memberIds = memberIds,
    unread = unread,
    lastMessageAt = listStamp(lastMessageAt),
)

internal fun ThreadDto.toSummary() = ThreadSummary(
    id = id,
    title = mapOf("ja" to titleJa),
    lastMessage = lastMessage,
    unreadCount = unread,
    updatedAt = lastMessageAt.orEmpty(),
)

private fun WireMessage.base(ctx: MessageContext): ChatMessageUiModel {
    val kind = wireEnum(kind, MessageKind.UNKNOWN)
    val original = text["ja"].orEmpty().ifEmpty { transcriptJa.orEmpty() }
    return ChatMessageUiModel(
        id = id,
        threadId = threadId,
        fromUserId = fromUserId,
        fromUserName = ctx.names[fromUserId].orEmpty(),
        isMine = fromUserId == ctx.myId,
        kind = kind,
        text = text,
        originalText = original,
        translatedText = pick(text, ctx.myLang),
        time = clock(sentAt ?: createdAt),
        stage = stage,
        isVoice = kind == MessageKind.VOICE,
        workerNames = ctx.names,
    )
}

internal fun WireSenderMessage.toUi(ctx: MessageContext): ChatMessageUiModel {
    val forSender = questions.map { CheckQuestionForSender(it.id, it.termId, it.prompt, it.options, it.correctIndex) }
    return base(ctx).copy(
        hasCheck = questions.isNotEmpty(),
        senderQuestions = forSender,
        workerQuestions = questions.map { CheckQuestionForWorker(it.id, it.termId, it.prompt, it.options) },
        addedTasks = taskDrafts.map { pick(it.title, ctx.myLang) },
    )
}

internal fun WireWorkerMessage.toUi(ctx: MessageContext): ChatMessageUiModel = base(ctx).copy(
    hasCheck = questions.isNotEmpty(),
    workerQuestions = questions.map { CheckQuestionForWorker(it.id, it.termId, it.prompt, it.options) },
    workerAnswerStatus = wireStatus(myStatus),
    isNudged = nudged,
)

internal fun WireAnswerResult.toDto() =
    AnswerResultDto(wireEnum(status, WakattaStatus.UNKNOWN), followUpIds)

internal fun WireFollowUp.toDto() = FollowUpDto(
    id = id,
    messageId = messageId,
    userId = userId,
    displayName = displayName,
    termJa = termJa,
    status = wireEnum(status, FollowUpStatus.UNKNOWN),
    note = note,
    createdAt = createdAt,
    resolvedAt = resolvedAt,
)

internal fun WireSenderMessage.toSendBody(typed: String) = SendInDto(
    // A draft the AI could not translate comes back with an empty text: send what the Foreman typed.
    text = text.ifEmpty { mapOf("ja" to typed) },
    questions = questions,
    tasks = taskDrafts,
)

/** shortcut: the app binds no persistent CacheStore yet, so the poll ETag and body live in memory only. */
internal class MemoryCacheStore : CacheStore {
    private val entries = ConcurrentHashMap<String, CacheEntry>()
    override suspend fun get(key: String): CacheEntry? = entries[key]
    override suspend fun put(key: String, json: String, etag: String?) {
        entries[key] = CacheEntry(key, json, etag, System.currentTimeMillis())
    }
    override suspend fun evict(prefix: String) {
        entries.keys.removeAll { it.startsWith(prefix) }
    }
}
