package com.techtsunagu.wakatta.feature.chat.model

import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.MessageKind
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.WakattaStatus
import kotlinx.serialization.Serializable

@Serializable
data class ThreadDto(
    val id: String,
    val titleJa: String,
    val titleEn: String? = null,
    val kind: String = "group",
    val memberIds: List<String> = emptyList(),
    val unread: Int = 0,
    val lastMessageAt: String? = null,
    val lastMessage: String? = null,
    val worstStatus: WakattaStatus? = null,
)

@Serializable
data class TaskDraftDto(
    val title: Tri,
    val assigneeIds: List<String> = emptyList(),
    val dueAt: String? = null,
    val machineId: String? = null,
)

/**
 * Worker-facing Check Question: strictly has NO correctIndex or answer fields (Rule R-3).
 */
@Serializable
data class CheckQuestionForWorker(
    val id: String,
    val termId: String? = null,
    val prompt: Tri,
    val options: List<Tri>,
)

@Serializable
data class CheckQuestionForSender(
    val id: String,
    val termId: String? = null,
    val prompt: Tri,
    val options: List<Tri>,
    val correctIndex: Int,
)

@Serializable
data class MessageForSenderDto(
    val id: String,
    val threadId: String,
    val fromUserId: String,
    val kind: MessageKind,
    val language: String = "ja",
    val stage: Int = 5,
    val needsEdit: Boolean = false,
    val transcriptJa: String? = null,
    val text: Tri,
    val toUserId: String? = null,
    val refMessageId: String? = null,
    val createdAt: String,
    val sentAt: String? = null,
    val questions: List<CheckQuestionForSender> = emptyList(),
    val taskDrafts: List<TaskDraftDto> = emptyList(),
    val workerStatuses: Map<String, WakattaStatus> = emptyMap(),
)

@Serializable
data class MessageForWorkerDto(
    val id: String,
    val threadId: String,
    val fromUserId: String,
    val kind: MessageKind,
    val language: String = "ja",
    val stage: Int = 5,
    val needsEdit: Boolean = false,
    val transcriptJa: String? = null,
    val text: Tri,
    val toUserId: String? = null,
    val refMessageId: String? = null,
    val createdAt: String,
    val sentAt: String? = null,
    val questions: List<CheckQuestionForWorker> = emptyList(),
    val myStatus: WakattaStatus? = null,
    val nudged: Boolean = false,
)

@Serializable
data class MessagesPageDto(
    val threadId: String,
    val senderMessages: List<MessageForSenderDto> = emptyList(),
    val workerMessages: List<MessageForWorkerDto> = emptyList(),
)

@Serializable
data class SendRequest(
    val text: Tri,
    val questions: List<CheckQuestionForSender> = emptyList(),
    val tasks: List<TaskDraftDto> = emptyList(),
)

@Serializable
data class AnswersRequest(
    val answers: List<Int>,
)

@Serializable
data class AnswerResultDto(
    val status: WakattaStatus,
    val followUpIds: List<String> = emptyList(),
)

@Serializable
data class ReexplainRequest(
    val userId: String,
    val termJa: String,
    val easyJa: String,
    val explanation: Tri,
    val newQuestionPrompt: Tri,
    val newQuestionOptions: List<Tri>,
    val newQuestionCorrectIndex: Int = 0,
)

/** The Re-explanation the Foreman reviews before sending (terms and name come from the Follow-up). */
@Serializable
data class ReexplainDraft(
    val workerName: String,
    val workerLang: String,
    val termJa: String,
    val easyJa: String,
    val explanation: Tri,
    val newQuestionPrompt: Tri,
    val newQuestionOptions: List<Tri>,
    val newQuestionCorrectIndex: Int = 0,
)

@Serializable
data class NudgeRequest(
    val userId: String,
)

@Serializable
data class FollowUpDto(
    val id: String,
    val messageId: String,
    val userId: String,
    val displayName: String,
    val termJa: String,
    val status: FollowUpStatus,
    val note: String = "",
    val createdAt: String,
    val resolvedAt: String? = null,
)
