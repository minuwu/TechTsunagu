package com.techtsunagu.wakatta.feature.insights

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsData
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsRepository
import com.techtsunagu.wakatta.feature.insights.domain.FollowUp
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpRepository
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpsData
import com.techtsunagu.wakatta.feature.insights.domain.HeatCell
import com.techtsunagu.wakatta.feature.insights.domain.HeatRow
import com.techtsunagu.wakatta.feature.insights.domain.LearningEntry
import com.techtsunagu.wakatta.feature.insights.domain.LearningHistory
import com.techtsunagu.wakatta.feature.insights.domain.LearningRepository
import com.techtsunagu.wakatta.feature.insights.domain.LoopStats
import com.techtsunagu.wakatta.feature.insights.domain.SkillCell
import com.techtsunagu.wakatta.feature.insights.domain.SkillMachine
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrix
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrixRepository
import com.techtsunagu.wakatta.feature.insights.domain.SkillRow
import com.techtsunagu.wakatta.feature.insights.domain.TermStat
import com.techtsunagu.wakatta.feature.insights.domain.WorkerBar
import com.techtsunagu.wakatta.feature.insights.domain.WorkerRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TestSessionStore(user: User? = DemoSeed.demoForeman) : SessionStore {
    private val flow = MutableStateFlow(user?.let { Session("t", user = it) })
    override val session: StateFlow<Session?> = flow
    override suspend fun save(session: Session) { flow.value = session }
    override suspend fun clear() { flow.value = null }
    override fun getCachedToken(): String? = flow.value?.token
}

class TestGlossary(var pinned: List<String> = emptyList()) : GlossaryRepository {
    var failure: com.techtsunagu.wakatta.core.model.AppError? = null
    override suspend fun getTerms(): AppResult<List<GlossaryTerm>> =
        failure?.let { AppResult.Failure(it) } ?: AppResult.Success(pinned.map { GlossaryTerm(it, "", "", "") })

    override suspend fun addTerm(termJa: String, readingJa: String, termBn: String, termEn: String): AppResult<GlossaryTerm> =
        failure?.let { AppResult.Failure(it) } ?: AppResult.Success(GlossaryTerm(termJa, readingJa, termBn, termEn))
}

val sampleStats = LoopStats(flagged = 3, explained = 2, understood = 1, records = 7)

fun sampleAnalytics() = AnalyticsData(
    trend = listOf(62, 68, 74, 78),
    sent = 36,
    understood = 28,
    stats = sampleStats,
    cleanAiShare = 0.72,
    firstTryPassShare = 0.85,
    bars = listOf(WorkerBar("u_rahim", "Rahim Uddin", 14, 3, 1)),
    terms = listOf(
        TermStat("玉掛け", "slinging", 3, 4, -2, inGlossary = false),
        TermStat("治具", "jig", 2, 2, 1, inGlossary = true),
    ),
    heatWorkers = listOf(WorkerRef("u_rahim", "Rahim Uddin")),
    heat = listOf(HeatRow("玉掛け", listOf(HeatCell("u_rahim", 1, listOf(120L))))),
)

fun sampleFollowUps() = FollowUpsData(
    items = listOf(
        FollowUp("f1", "u_siti", "Siti Rahma", "3号機オイル", "Machine 3 oil", "What do you check first today?", "Air on Machine 2", FollowUpStatus.OPEN, 60),
        FollowUp("f2", "u_rahim", "Rahim Uddin", "金型交換", "die change", "What is die change?", null, FollowUpStatus.CLARIFIED, 2880),
    ),
    stats = sampleStats,
)

fun sampleMatrix() = SkillMatrix(
    machines = listOf(SkillMachine("mc1", mapOf("ja" to "旋盤1号機", "en" to "Lathe 1"))),
    rows = listOf(
        SkillRow(
            "u_rahim", "Rahim Uddin",
            listOf(SkillCell("mc1", SkillStatus.EXPIRING, 1_000L, 2_000L, 9, "v1")),
        ),
    ),
)

fun sampleHistory(workerId: String = "u_rahim") = LearningHistory(
    workerId = workerId,
    workerName = "Rahim Uddin",
    mastered = 1,
    gaps = 1,
    streak = 5,
    entries = listOf(
        LearningEntry("金型交換", "die change", 2, FollowUpStatus.CLARIFIED, 2880),
        LearningEntry("切粉", "chips", 1, FollowUpStatus.UNDERSTOOD, 11520),
    ),
)

class StubAnalytics(
    var result: AppResult<AnalyticsData> = AppResult.Success(sampleAnalytics()),
    var pinResult: AppResult<Unit> = AppResult.Success(Unit),
) : AnalyticsRepository {
    var loads = 0
    val pinned = mutableListOf<String>()
    override suspend fun getAnalytics(): AppResult<AnalyticsData> { loads++; return result }
    override suspend fun pinTerm(term: String, termEn: String): AppResult<Unit> { pinned += term; return pinResult }
}

class StubFollowUps(var result: AppResult<FollowUpsData> = AppResult.Success(sampleFollowUps())) : FollowUpRepository {
    val clarified = mutableListOf<String>()
    override suspend fun getFollowUps(): AppResult<FollowUpsData> = result
    override suspend fun clarify(followUpId: String, note: String): AppResult<Unit> {
        clarified += followUpId
        return AppResult.Success(Unit)
    }
}

class StubSkills(var result: AppResult<SkillMatrix> = AppResult.Success(sampleMatrix())) : SkillMatrixRepository {
    override suspend fun getMatrix(): AppResult<SkillMatrix> = result
}

class StubLearning(var failure: com.techtsunagu.wakatta.core.model.AppError? = null) : LearningRepository {
    val asked = mutableListOf<String>()
    override suspend fun getHistory(workerId: String): AppResult<LearningHistory> {
        asked += workerId
        return failure?.let { AppResult.Failure(it) } ?: AppResult.Success(sampleHistory(workerId))
    }
}
