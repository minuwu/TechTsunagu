package com.techtsunagu.wakatta.core.model

import kotlinx.serialization.Serializable

/** The app ships Japanese and English only; any other profile language (e.g. "bn") reads in English. */
fun appLanguage(tag: String?): String = if (tag == "ja") "ja" else "en"

@Serializable
data class User(
    val id: String,
    val displayName: String,
    val role: Role,
    val language: String,
    val shopId: String? = null,
    val districtId: String? = null,
    /** The code Workers sign in with; a Foreman shares it (ADR 0017). */
    val shopCode: String? = null,
)

@Serializable
data class Session(
    val token: String,
    val expiresAt: String? = null,
    val user: User,
    val mustChangePin: Boolean = false
)

@Serializable
data class Machine(
    val id: String,
    val code: String,
    val name: Tri,
    val currentVersionId: String? = null,
    val activeWorkerId: String? = null,
    val isReady: Boolean = false
)

@Serializable
data class MachineDetail(
    val id: String,
    val code: String,
    val name: Tri,
    val currentVersionId: String? = null,
    val steps: List<Step> = emptyList(),
    val questions: List<CheckQuestion> = emptyList()
)

@Serializable
data class Step(
    val text: Tri,
    val icon: String? = null
)

@Serializable
data class CheckQuestion(
    val id: String,
    val termId: String? = null,
    val prompt: Tri,
    val options: List<Tri>
)

@Serializable
data class CheckResult(
    val passed: Boolean,
    val wrongQuestions: List<String> = emptyList(),
    val signOffGranted: Boolean = false
)

@Serializable
data class SignOff(
    val machineId: String,
    val machineName: Tri,
    val passedAt: String,
    val expiresAt: String? = null,
    val status: SkillStatus = SkillStatus.VALID
)

@Serializable
data class Passport(
    val workerId: String,
    val workerName: String,
    val signOffs: List<SignOff> = emptyList()
)

@Serializable
data class WorkerInfo(
    val id: String,
    val displayName: String,
    val language: String
)

@Serializable
data class GlossaryTerm(
    val ja: String,
    val reading: String,
    val bn: String,
    val en: String
)

@Serializable
data class Receipts(
    val understood: List<WorkerInfo> = emptyList(),
    val openedOnly: List<WorkerInfo> = emptyList(),
    val notOpened: List<WorkerInfo> = emptyList()
)

@Serializable
data class Task(
    val id: String,
    val title: Tri,
    val column: TaskColumn,
    val assignees: List<String> = emptyList(),
    val dueAt: String? = null,
    val machineId: String? = null,
    val fromMessageId: String? = null,
    val version: Int = 1
)

@Serializable
data class CacheEntry(
    val key: String,
    val json: String,
    val etag: String? = null,
    val fetchedAt: Long = 0L
)

@Serializable
data class AttentionCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val severity: WakattaStatus,
    val routeKey: String
)

@Serializable
data class ThreadSummary(
    val id: String,
    val title: Tri,
    val lastMessage: String? = null,
    val unreadCount: Int = 0,
    val updatedAt: String = ""
)

@Serializable
data class UpcomingEvent(
    val id: String,
    val title: Tri,
    val startsAt: String
)

@Serializable
data class DueInterview(
    val workerId: String,
    val workerName: String,
    val dueDate: String
)

@Serializable
data class TodayWord(
    val term: String,
    val reading: String,
    val meaning: Tri
)

@Serializable
data class VersionInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val minSupportedVersion: String
)

sealed interface VersionGateState {
    data object UpToDate : VersionGateState
    data class Soft(val info: VersionInfo) : VersionGateState
    data class Hard(val info: VersionInfo) : VersionGateState
}
