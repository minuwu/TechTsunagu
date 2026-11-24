package com.techtsunagu.wakatta.feature.support.domain

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DueInterview
import com.techtsunagu.wakatta.core.model.InterviewDueSource
import com.techtsunagu.wakatta.core.model.InterviewMethod
import com.techtsunagu.wakatta.core.model.InterviewStatus
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Support Log data and CSV export.
 */
interface SupportLogRepository {
    suspend fun getSupportLog(
        workerId: String? = null,
        month: String? = null,
    ): AppResult<SupportLogData>

    suspend fun exportSupportLogCsv(
        workerId: String? = null,
        month: String? = null,
    ): AppResult<File>
}

/**
 * Repository interface for Periodic Interviews (定期面談) and prep facts.
 * Also implements [InterviewDueSource] to provide due interview notices to other modules.
 */
interface InterviewRepository : InterviewDueSource {
    suspend fun getDueInfo(workerId: String): AppResult<InterviewDueDto>
    suspend fun getInterviewPrep(workerId: String): AppResult<InterviewPrepDto>
    suspend fun getPastInterviews(workerId: String): AppResult<List<InterviewRecordDto>>
    suspend fun recordInterview(request: InterviewCreateRequest): AppResult<InterviewRecordDto>
    suspend fun getAllWorkers(): AppResult<List<WorkerInfo>>
}

// ---------------------------------------------------------------------------
// Domain Models / DTOs
// ---------------------------------------------------------------------------

data class SupportLogSummary(
    val interviewCount: Int,
    val meetupCount: Int,
    val noticeOkCount: Int,
    val noticeTotalCount: Int,
)

data class SupportMeetupDto(
    val id: String,
    val workerId: String,
    val workerName: String,
    val senpaiName: String,
    val kind: String,
    val date: String,
    val noteJa: String,
    val noteEn: String,
    val recordedBy: String,
)

data class SupportNoticeDto(
    val id: String,
    val titleJa: String,
    val date: String,
    val workerStatuses: Map<String, WakattaStatus>,
)

data class SupportLogData(
    val selectedWorkerId: String,
    val selectedMonth: String,
    val availableMonths: List<String>,
    val workers: List<WorkerInfo>,
    val summary: SupportLogSummary,
    val dueInterviews: List<InterviewDueDto>,
    val meetups: List<SupportMeetupDto>,
    val notices: List<SupportNoticeDto>,
)

data class InterviewDueDto(
    val workerId: String,
    val workerName: String,
    val lastDate: String?,
    val dueDate: String?,
    val status: InterviewStatus,
    val daysLeft: Int,
)

data class InterviewQuestionItem(
    val ja: String,
    val en: String,
    val bn: String,
)

data class InterviewPrepDto(
    val workerId: String,
    val fromDate: String,
    val meetupsCount: Int,
    val noticesTotal: Int,
    val noticesOk: Int,
    val confusingNotices: List<String>,
    val confusionTerms: List<String>,
    val forwardedAsks: List<String>,
    val questions: List<InterviewQuestionItem>,
)

data class InterviewRecordDto(
    val id: String,
    val workerId: String,
    val date: String,
    val method: InterviewMethod,
    val consent: Boolean,
    val noteJa: String,
    val recordedBy: String,
)

data class InterviewCreateRequest(
    val workerId: String,
    val date: String,
    val method: InterviewMethod,
    val consent: Boolean,
    val note: String,
)

// ---------------------------------------------------------------------------
// Calculation helpers (addMonths with month-end clamp, ivDue logic)
// ---------------------------------------------------------------------------

object InterviewDueCalculator {
    const val DEMO_TODAY_ISO = "2026-11-24"

    fun todayIso(): String = LocalDate.now(ZoneId.of("Asia/Tokyo")).toString()

    /**
     * Adds [months] to [isoDate] (YYYY-MM-DD) with month-end clamping.
     * E.g. 2026-08-31 + 3 months -> 2026-11-30.
     */
    fun addMonths(isoDate: String, months: Long): String {
        return try {
            val parsed = LocalDate.parse(isoDate)
            parsed.plusMonths(months).toString()
        } catch (_: DateTimeParseException) {
            isoDate
        }
    }

    /**
     * Computes the due status for a worker given their last interview date.
     */
    fun calculateDue(
        workerId: String,
        workerName: String,
        lastDate: String?,
        todayIso: String = DEMO_TODAY_ISO,
    ): InterviewDueDto {
        if (lastDate.isNullOrBlank()) {
            return InterviewDueDto(
                workerId = workerId,
                workerName = workerName,
                lastDate = null,
                dueDate = null,
                status = InterviewStatus.OVERDUE,
                daysLeft = -999,
            )
        }

        val dueDateIso = addMonths(lastDate, 3)
        val today = try {
            LocalDate.parse(todayIso)
        } catch (_: Exception) {
            LocalDate.of(2026, 11, 24)
        }
        val due = try {
            LocalDate.parse(dueDateIso)
        } catch (_: Exception) {
            today
        }

        val left = ChronoUnit.DAYS.between(today, due).toInt()
        val status = when {
            left < 0 -> InterviewStatus.OVERDUE
            left <= 14 -> InterviewStatus.SOON
            else -> InterviewStatus.OK
        }

        return InterviewDueDto(
            workerId = workerId,
            workerName = workerName,
            lastDate = lastDate,
            dueDate = dueDateIso,
            status = status,
            daysLeft = left,
        )
    }

    fun toDueInterview(dueDto: InterviewDueDto): DueInterview {
        return DueInterview(
            workerId = dueDto.workerId,
            workerName = dueDto.workerName,
            dueDate = dueDto.dueDate ?: "なし",
        )
    }
}
