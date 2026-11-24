package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.MessageKind
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.ThreadSummary
import com.techtsunagu.wakatta.core.model.User
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

internal fun WireThread.toDto(me: User? = null): ThreadDto {
    val displayTitle = if (me != null && me.role == Role.WORKER) {
        val myName = me.displayName.trim()
        val matchesMyName = myName.isNotEmpty() && titleJa.trim().equals(myName, ignoreCase = true)
        if (matchesMyName) "職長" else titleJa
    } else {
        titleJa
    }
    return ThreadDto(
        id = id,
        titleJa = displayTitle,
        kind = kind,
        memberIds = memberIds,
        unread = unread,
        lastMessageAt = listStamp(lastMessageAt),
        lastMessage = lastMessage?.let { pick(it.text, me?.language ?: "ja") },
        worstStatus = wkStatus?.let { wireEnum(it, WakattaStatus.UNKNOWN) },
        lastIsVoice = lastMessage?.kind == "voice",
        lastFromMe = me != null && lastMessage?.fromUserId == me.id,
    )
}

internal fun ThreadDto.toSummary(me: User? = null): ThreadSummary {
    val titleTri = if (me != null && me.role == Role.WORKER && (titleJa == "職長" || (me.displayName.isNotBlank() && titleJa.trim().equals(me.displayName.trim(), ignoreCase = true)))) {
        mapOf("ja" to "職長", "en" to "Foreman", "bn" to "ফোরম্যান")
    } else {
        mapOf("ja" to titleJa)
    }
    return ThreadSummary(
        id = id,
        title = titleTri,
        lastMessage = lastMessage,
        unreadCount = unread,
        updatedAt = lastMessageAt.orEmpty(),
        isVoice = lastIsVoice,
        worstStatus = worstStatus,
        lastFromMe = lastFromMe,
        membersCount = memberIds.size,
    )
}

private fun WireMessage.base(ctx: MessageContext): ChatMessageUiModel {
    val kind = wireEnum(kind, MessageKind.UNKNOWN)
    // the sender's own words; Japanese (or the transcript) when that language has no text yet
    val original = text[language].orEmpty().ifEmpty { text["ja"].orEmpty() }.ifEmpty { transcriptJa.orEmpty() }
    return ChatMessageUiModel(
        id = id,
        threadId = threadId,
        fromUserId = fromUserId,
        fromUserName = fromDisplayName ?: ctx.names[fromUserId].orEmpty(),
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
        needsEdit = needsEdit,
        workerStatuses = results.associate { it.userId to (wireStatus(it.status) ?: WakattaStatus.WAIT) },
        workerAnswerMap = results.mapNotNull { r -> r.answers?.let { r.userId to it } }.toMap(),
        workerRead = results.filter { it.read }.map { it.userId }.toSet(),
        workerNudged = results.filter { it.nudged }.map { it.userId }.toSet(),
    )
}

internal fun WireWorkerMessage.toUi(ctx: MessageContext): ChatMessageUiModel = base(ctx).copy(
    hasCheck = questions.isNotEmpty(),
    workerQuestions = questions.map { CheckQuestionForWorker(it.id, it.termId, it.prompt, it.options) },
    workerAnswerStatus = wireStatus(myStatus),
    isNudged = nudged,
    workerAnswers = myAnswers.orEmpty(),
    answerKey = answerKey.orEmpty(),
    asked = asked,
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
