package com.techtsunagu.wakatta.feature.notices.model

import com.techtsunagu.wakatta.core.model.NoticeCategory
import com.techtsunagu.wakatta.core.model.NoticeSourceKind
import com.techtsunagu.wakatta.core.model.NoticeStatus
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import java.io.File
import kotlinx.serialization.Serializable

@Serializable
data class StepDto(
    val text: Tri,
    val icon: String? = null,
)

@Serializable
data class CheckQuestionDto(
    val id: String,
    val termId: String? = null,
    val prompt: Tri,
    val options: List<Tri>,
)

@Serializable
data class CheckQuestionWithAnswer(
    val id: String,
    val termId: String? = null,
    val prompt: Tri,
    val options: List<Tri>,
    val correctIndex: Int,
)

@Serializable
data class EventDto(
    val id: String,
    val noticeId: String,
    val title: Tri,
    val date: String,
    val time: String,
    val confirmed: Boolean = true,
    val isReminderOn: Boolean = false,
)

@Serializable
data class NoticeWorkerStatus(
    val worker: WorkerInfo,
    val status: WakattaStatus,
    val answerIndex: Int? = null,
    val answerText: String? = null,
)

@Serializable
data class NoticeForWorker(
    val id: String,
    val title: Tri,
    val category: NoticeCategory,
    val sourceKind: NoticeSourceKind,
    val sourceText: String? = null,
    val sourceTranslation: Tri = emptyMap(),
    val audioDuration: String? = null,
    val photoSeed: String? = null,
    val from: String = "自治会",
    val publishedAt: String,
    val steps: List<StepDto> = emptyList(),
    val events: List<EventDto> = emptyList(),
    val question: CheckQuestionDto,
    val opened: Boolean = false,
    val passed: Boolean? = null,
    val userAnswer: Int? = null,
    val wakattaStatus: WakattaStatus = WakattaStatus.WAIT,
)

@Serializable
data class NoticeForPoster(
    val id: String,
    val title: Tri,
    val category: NoticeCategory,
    val sourceKind: NoticeSourceKind,
    val sourceText: String? = null,
    val sourceTranslation: Tri = emptyMap(),
    val audioDuration: String? = null,
    val photoSeed: String? = null,
    val from: String = "自治会",
    val publishedAt: String? = null,
    val steps: List<StepDto> = emptyList(),
    val events: List<EventDto> = emptyList(),
    val question: CheckQuestionWithAnswer,
    val status: NoticeStatus = NoticeStatus.PUBLISHED,
    val needsEdit: Boolean = false,
    val workerStatuses: List<NoticeWorkerStatus> = emptyList(),
)

@Serializable
data class NoticeDraftDto(
    val id: String,
    val sourceKind: NoticeSourceKind,
    val sourceText: String,
    val seed: String = "",
    val titleJa: String,
    val titleEn: String,
    val titleBn: String = "",
    val stepsJa: List<String> = emptyList(),
    val stepsEn: List<String> = emptyList(),
    val stepsBn: List<String> = emptyList(),
    val events: List<EventDto> = emptyList(),
    val question: CheckQuestionWithAnswer,
    val needsEdit: Boolean = false,
)

data class NoticeCreateRequest(
    val sourceKind: NoticeSourceKind,
    val text: String? = null,
    val audioFile: File? = null,
    val photoFile: File? = null,
    val sampleId: String? = null,
)

@Serializable
data class CheckResultDto(
    val passed: Boolean,
    val status: WakattaStatus,
    val message: String,
    val correctAnswerText: String? = null,
)

@Serializable
data class PublishResult(
    val success: Boolean,
    val noticeId: String? = null,
    val error: String? = null,
)

@Serializable
data class TodayWordInfo(
    val term: String,
    val reading: String,
    val meaning: Tri,
    val workerName: String,
    val languageName: String,
)

@Serializable
data class LifeSummary(
    val senpaiName: String = "佐藤 美咲",
    val senpaiInitials: String = "佐",
    val senpaiColor: Long = 0xFFAD1457,
    val nextMeetDate: String? = "2026-12-07",
    val nextMeetKind: String? = "drill",
    val senpaiPairsCount: Int = 3,
    val meetupsCount: Int = 4,
    val forwardedAsksCount: Int = 1,
    val interviewsDueCount: Int = 2,
)
