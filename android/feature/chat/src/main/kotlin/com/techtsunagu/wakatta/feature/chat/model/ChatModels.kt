package com.techtsunagu.wakatta.feature.chat.model

import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.model.MessageKind
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.WakattaStatus

enum class ThreadFilter {
    ALL, UNREAD
}

enum class ComposerStatus {
    IDLE, RECORDING, COUNTDOWN, EDITING, SENDING
}

data class ThreadUiModel(
    val id: String,
    val title: Tri,
    val lastMessage: String,
    val lastMessageAt: String,
    val unreadCount: Int = 0,
    val isVoice: Boolean = false,
    val worstStatus: WakattaStatus? = null,
    val isGroup: Boolean = true,
    val membersCount: Int = 2,
    val kind: String = "group",
    val lastFromMe: Boolean = false,
)

data class ChatMessageUiModel(
    val id: String,
    val threadId: String,
    val fromUserId: String,
    val fromUserName: String,
    val fromForeman: Boolean = false,
    val isMine: Boolean,
    val kind: MessageKind,
    val text: Tri,
    val originalText: String,
    val translatedText: String,
    val time: String,
    val stage: Int = 5,
    /** The Foreman's own message that has not gone to the Workers yet. */
    val isDraft: Boolean = false,
    /** A draft the AI could not translate. */
    val needsEdit: Boolean = false,
    val isVoice: Boolean = false,
    val voiceDuration: String? = null,
    val hasCheck: Boolean = false,
    val showOriginal: Boolean = false,
    // Worker specific
    val workerQuestions: List<CheckQuestionForWorker> = emptyList(),
    val workerAnswerStatus: WakattaStatus? = null,
    val workerAnswers: List<Int> = emptyList(),
    val isNudged: Boolean = false,
    /** The correct option of each question; the server sends it only after this Worker answered. */
    val answerKey: List<Int> = emptyList(),
    /** This Worker already asked the Foreman to explain the message again. */
    val asked: Boolean = false,
    val addedTasks: List<String> = emptyList(),
    // Foreman specific
    val workerStatuses: Map<String, WakattaStatus> = emptyMap(),
    val workerAnswerMap: Map<String, List<Int>> = emptyMap(),
    val workerRead: Set<String> = emptySet(),
    val workerNudged: Set<String> = emptySet(),
    val isWkFoldExpanded: Boolean = false,
    val workerNames: Map<String, String> = emptyMap(),
    val senderQuestions: List<CheckQuestionForSender> = emptyList(),
)

data class ReexplainDialogState(
    val messageId: String,
    val workerId: String,
    val workerName: String,
    val workerLang: String,
    val confusingTerm: String,
    val easyJaText: String,
    val explanation: Tri,
    val newQuestionPrompt: Tri,
    val newQuestionOptions: List<Tri>,
    val correctOptionIndex: Int = 0,
)

data class CountdownState(
    val seconds: Int = 4,
    val progress: Float = 1.0f,
)
