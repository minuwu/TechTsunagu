package com.techtsunagu.wakatta.feature.insights.domain

import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.Tri

/** Counts behind the "one instruction, three wins" card. */
data class LoopStats(val flagged: Int, val explained: Int, val understood: Int, val records: Int)

data class FollowUp(
    val id: String,
    val workerId: String,
    val workerName: String,
    val term: String,
    val termEn: String,
    val question: String,
    val wrong: String?,
    val status: FollowUpStatus,
    val ageMinutes: Long,
)

data class FollowUpsData(val items: List<FollowUp>, val stats: LoopStats)

data class WorkerRef(val id: String, val name: String)

data class WorkerBar(val workerId: String, val name: String, val green: Int, val orange: Int, val red: Int)

data class TermStat(
    val term: String,
    val termEn: String,
    val workerCount: Int,
    val count: Int,
    /** Later half minus earlier half of the rows: positive means getting worse. */
    val trend: Int,
    val inGlossary: Boolean,
    val canPin: Boolean = !inGlossary,
)

data class HeatCell(val workerId: String, val count: Int, val agesMinutes: List<Long>)

data class HeatRow(val term: String, val cells: List<HeatCell>)

data class AnalyticsData(
    /** Understanding rate in percent, oldest week first, this week last. */
    val trend: List<Int>,
    val sent: Int,
    val understood: Int,
    val stats: LoopStats,
    /** Approved AI drafts with no Foreman edit, 0..1; null when there is no data yet. */
    val cleanAiShare: Double?,
    /** Checks passed on the first try, 0..1; null when there is no data yet. */
    val firstTryPassShare: Double?,
    val bars: List<WorkerBar>,
    val terms: List<TermStat>,
    val heatWorkers: List<WorkerRef>,
    val heat: List<HeatRow>,
)

data class SkillMachine(val id: String, val name: Tri)

data class SkillCell(
    val machineId: String,
    val status: SkillStatus,
    val signedAtMillis: Long?,
    val expiresAtMillis: Long?,
    val daysLeft: Int?,
    val version: String?,
)

data class SkillRow(val workerId: String, val workerName: String, val cells: List<SkillCell>)

data class SkillMatrix(val machines: List<SkillMachine>, val rows: List<SkillRow>)

data class LearningEntry(
    val term: String,
    val termEn: String,
    val missed: Int,
    val status: FollowUpStatus,
    val ageMinutes: Long?,
)

data class LearningHistory(
    val workerId: String,
    val workerName: String,
    val mastered: Int,
    val gaps: Int,
    val streak: Int,
    /** Newest first. */
    val entries: List<LearningEntry>,
)
