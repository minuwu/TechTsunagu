package com.techtsunagu.wakatta.feature.briefing.model

import com.techtsunagu.wakatta.core.model.CheckQuestion
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import kotlinx.serialization.Serializable

@Serializable
data class CheckQuestionWithAnswer(
    val id: String,
    val prompt: Tri,
    val options: List<Tri>,
    val correctIndex: Int,
    val termId: String? = null,
)

@Serializable
data class BriefingForForeman(
    val id: String,
    val text: Tri,
    val focusText: Tri,
    val changeText: Tri,
    val createdAt: String,
    val publishedAt: String? = null,
    val question: CheckQuestionWithAnswer,
    val status: String = "published",
    val needsEdit: Boolean = false,
)

/**
 * Worker-facing model. Per rule R-3, this model MUST NEVER contain `correctIndex`
 * or any answer evaluation key.
 */
@Serializable
data class BriefingForWorker(
    val id: String,
    val text: Tri,
    val focusText: Tri,
    val changeText: Tri,
    val createdAt: String,
    val publishedAt: String? = null,
    val question: CheckQuestion,
    val status: String = "published",
    val opened: Boolean = false,
    val passed: Boolean? = null,
    val userAnswer: Int? = null,
    val wakattaStatus: WakattaStatus = WakattaStatus.WAIT,
)

@Serializable
data class BriefingDraft(
    val id: String,
    val text: Tri,
    val focusText: Tri,
    val changeText: Tri,
    val question: CheckQuestionWithAnswer,
    val transcriptJa: String,
    val needsEdit: Boolean = false,
)

@Serializable
data class BriefingPublishRequest(
    val id: String,
    val text: Tri,
    val focusText: Tri,
    val changeText: Tri,
    val question: CheckQuestionWithAnswer,
)

@Serializable
data class BriefingWorkerStatus(
    val worker: WorkerInfo,
    val hasAttended: Boolean,
    val status: WakattaStatus,
    val answerIndex: Int? = null,
    val answerText: String? = null,
)

@Serializable
data class BriefingReceipts(
    val understood: List<WorkerInfo> = emptyList(),
    val openedOnly: List<WorkerInfo> = emptyList(),
    val notOpened: List<WorkerInfo> = emptyList(),
)
