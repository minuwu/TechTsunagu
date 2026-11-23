package com.techtsunagu.wakatta.feature.asks.model

import com.techtsunagu.wakatta.core.model.Tri
import java.io.File
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
enum class AskKind {
    TEXT, VOICE
}

@Serializable
enum class AskStatus {
    ANSWERED, FORWARDED, REPLIED
}

@Serializable
data class NoticeMatchDto(
    val id: String,
    val title: Tri,
    val answer: Tri,
    val snippet: Tri,
)

@Serializable
data class AskDto(
    val id: String,
    val workerId: String,
    val workerName: String,
    val text: String,
    val textJa: String? = null,
    val kind: AskKind = AskKind.TEXT,
    val covered: Boolean = false,
    val matchNotice: NoticeMatchDto? = null,
    val sourceNoticeIds: List<String> = emptyList(),
    val status: AskStatus = AskStatus.FORWARDED,
    val senpaiName: String? = null,
    val senpaiEnName: String? = null,
    val replyJa: String? = null,
    val replyEn: String? = null,
    val replyBn: String? = null,
    val createdAt: String,
)

/** [idempotencyKey] is made once per user action, so a retry of the same request reuses it. */
data class AskCreateRequest(
    val text: String,
    val isVoice: Boolean = false,
    val audioFile: File? = null,
    val idempotencyKey: String = UUID.randomUUID().toString(),
)
