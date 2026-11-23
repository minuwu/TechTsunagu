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
)

data class ChatMessageUiModel(
    val id: String,
    val threadId: String,
    val fromUserId: String,
    val fromUserName: String,
    val isMine: Boolean,
    val kind: MessageKind,
    val text: Tri,
    val originalText: String,
    val translatedText: String,
    val time: String,
    val stage: Int = 5,
    val isVoice: Boolean = false,
    val voiceDuration: String? = null,
    val hasCheck: Boolean = false,
    val showOriginal: Boolean = false,
    // Worker specific
    val workerQuestions: List<CheckQuestionForWorker> = emptyList(),
    val workerAnswerStatus: WakattaStatus? = null,
    val workerAnswers: List<Int> = emptyList(),
    val isNudged: Boolean = false,
    val addedTasks: List<String> = emptyList(),
    // Foreman specific
    val workerStatuses: Map<String, WakattaStatus> = emptyMap(),
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
