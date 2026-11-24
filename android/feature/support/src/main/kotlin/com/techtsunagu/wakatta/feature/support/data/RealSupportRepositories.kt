package com.techtsunagu.wakatta.feature.support.data

import android.content.Context
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DueInterview
import com.techtsunagu.wakatta.core.model.InterviewMethod
import com.techtsunagu.wakatta.core.model.InterviewStatus
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.support.domain.InterviewCreateRequest
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueCalculator
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewPrepDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewRecordDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewRepository
import com.techtsunagu.wakatta.feature.support.domain.SupportLogData
import com.techtsunagu.wakatta.feature.support.domain.SupportLogRepository
import com.techtsunagu.wakatta.feature.support.domain.SupportLogSummary
import com.techtsunagu.wakatta.feature.support.domain.SupportMeetupDto
import com.techtsunagu.wakatta.feature.support.domain.SupportNoticeDto
import java.io.File
import java.io.IOException
import java.io.InterruptedIOException
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart

/**
 * The summary is read from the CSV export (Meet-ups, Notice checks, interview count) and `GET /interviews/due`.
 * shortcut: the CSV names Workers without an id, so two Workers with one name share rows, use the JSON summary of XREQ-024.
 */
internal class RealSupportLogRepository(
    private val api: () -> SupportApi,
    private val context: Context,
    private val workerRepository: WorkerRepository,
) : SupportLogRepository {

    override suspend fun getSupportLog(
        workerId: String?,
        month: String?,
    ): AppResult<SupportLogData> {
        val worker = workerId?.takeIf { it.isNotBlank() } ?: ALL
        val period = month?.takeIf { it.isNotBlank() } ?: YearMonth.now(TOKYO).toString()
        val workers = when (val r = workerRepository.getWorkers()) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        val due = when (val r = safeApiCall { api().getDueInterviews() }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        val (from, to) = computeDateRange(period)
        val rows = when (val r = api().supportLogRows(from, to)) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        val shown = workers.filter { worker == ALL || it.id == worker }
        val byName = shown.associateBy { it.displayName }
        val mine = rows.filter { it.worker in byName }
        val meetups = mine.filter { it.item == ITEM_EXCHANGE }.sortedByDescending { it.date }.mapIndexed { i, r ->
            SupportMeetupDto(
                id = "$i",
                workerId = byName.getValue(r.worker).id,
                workerName = r.worker,
                senpaiName = r.handling,
                kind = MEETUP_KINDS[r.content.substringBefore('\u3000')] ?: "other",
                date = r.date,
                noteJa = r.content.substringAfter('\u3000', ""),
                noteEn = "",
                recordedBy = r.source,
            )
        }
        val notices = mine.filter { it.item == ITEM_ORIENTATION }.groupBy { it.content }.map { (title, checks) ->
            SupportNoticeDto(
                id = title,
                titleJa = title,
                date = checks.maxOf { it.date },
                workerStatuses = checks.associate { byName.getValue(it.worker).id to noticeStatus(it.handling) },
            )
        }.sortedByDescending { it.date }
        val checks = notices.flatMap { it.workerStatuses.values }
        return AppResult.Success(
            SupportLogData(
                selectedWorkerId = worker,
                selectedMonth = period,
                availableMonths = listOf(ALL) + (0L until MONTHS).map { YearMonth.now(TOKYO).minusMonths(it).toString() },
                workers = workers,
                summary = SupportLogSummary(
                    interviewCount = mine.count { it.item == ITEM_INTERVIEW },
                    meetupCount = meetups.size,
                    noticeOkCount = checks.count { it == WakattaStatus.GREEN },
                    noticeTotalCount = checks.size,
                ),
                dueInterviews = due.filter { d -> shown.any { it.id == d.workerId } }.map { it.toDto() },
                meetups = meetups,
                notices = notices,
            ),
        )
    }

    override suspend fun exportSupportLogCsv(
        workerId: String?,
        month: String?,
    ): AppResult<File> {
        val (from, to) = computeDateRange(month)
        val body = when (val r = api().supportLogCsv(from, to)) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        return try {
            val file = File(context.cacheDir, "support-log-${month ?: ALL}.csv")
            file.writeBytes(body)
            AppResult.Success(file)
        } catch (_: IOException) {
            AppResult.Failure(AppError.Unknown("file"))
        }
    }

    private fun computeDateRange(month: String?): Pair<String, String> {
        val ym = runCatching { YearMonth.parse(month) }.getOrNull()
        val today = LocalDate.now(TOKYO)
        return if (ym == null) {
            today.minusYears(1).toString() to today.toString()
        } else {
            ym.atDay(1).toString() to ym.atEndOfMonth().toString()
        }
    }
}

private const val ALL = "all"
private const val MONTHS = 6L
private val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")
private val MEETUP_KINDS = mapOf("お茶・食事" to "coffee", "地域の行事" to "festival", "防災訓練" to "drill")

private fun noticeStatus(handling: String): WakattaStatus = when {
    handling.endsWith("不合格") -> WakattaStatus.RED
    handling.endsWith("合格") -> WakattaStatus.GREEN
    else -> WakattaStatus.WAIT
}

/** Not enveloped, so `safeApiCall` does not fit. Bytes, because the saved file keeps its BOM for Excel. */
private suspend fun SupportApi.supportLogCsv(from: String, to: String): AppResult<ByteArray> = try {
    val response = getSupportLogCsv(from, to)
    val body = response.body()
    when {
        response.isSuccessful && body != null -> AppResult.Success(body.bytes())
        response.code() == 401 -> AppResult.Failure(AppError.SessionExpired)
        response.code() == 403 -> AppResult.Failure(AppError.Forbidden)
        response.code() == 422 -> AppResult.Failure(AppError.Validation(emptyList()))
        response.code() >= 500 -> AppResult.Failure(AppError.Server)
        else -> AppResult.Failure(AppError.Unknown("HTTP ${response.code()}"))
    }
} catch (e: CancellationException) {
    throw e
} catch (_: InterruptedIOException) {
    AppResult.Failure(AppError.Timeout)
} catch (_: IOException) {
    AppResult.Failure(AppError.Offline)
}

private suspend fun SupportApi.supportLogRows(from: String, to: String): AppResult<List<SupportLogRow>> =
    when (val r = supportLogCsv(from, to)) {
        is AppResult.Success -> AppResult.Success(parseSupportLog(r.value.decodeToString()))
        is AppResult.Failure -> r
    }

private fun InterviewDueDtoWire.toDto() = InterviewDueDto(
    workerId = workerId,
    workerName = displayName,
    lastDate = lastHeldOn,
    dueDate = dueOn,
    status = when (status.lowercase()) {
        "ok" -> InterviewStatus.OK
        "soon" -> InterviewStatus.SOON
        else -> InterviewStatus.OVERDUE
    },
    daysLeft = runCatching { ChronoUnit.DAYS.between(LocalDate.now(TOKYO), LocalDate.parse(dueOn)).toInt() }.getOrDefault(0),
)

/**
 * Real repository for Periodic Interviews (定期面談) and due date tracking.
 */
internal class RealInterviewRepository(
    private val api: () -> SupportApi,
    private val session: SessionStore,
    private val workerRepository: WorkerRepository,
) : InterviewRepository {

    private val dueFlow = MutableStateFlow<List<DueInterview>>(emptyList())

    /** The route is for a Foreman only. */
    override fun getDueInterviews(): Flow<List<DueInterview>> = dueFlow.onStart {
        if (session.session.value?.user?.role == Role.FOREMAN) refreshDueList()
    }

    private suspend fun refreshDueList(): AppResult<List<InterviewDueDtoWire>> {
        val res = safeApiCall { api().getDueInterviews() }
        if (res is AppResult.Success) {
            dueFlow.value = res.value.map { DueInterview(it.workerId, it.displayName, it.dueOn) }
        }
        return res
    }

    override suspend fun getDueInfo(workerId: String): AppResult<InterviewDueDto> =
        when (val res = refreshDueList()) {
            is AppResult.Success -> res.value.firstOrNull { it.workerId == workerId }
                ?.let { AppResult.Success(it.toDto()) } ?: AppResult.Failure(AppError.NotFound)
            is AppResult.Failure -> res
        }

    /** Facts since the last interview, from the Support Log rows of this Worker. */
    override suspend fun getInterviewPrep(workerId: String): AppResult<InterviewPrepDto> {
        val name = when (val r = workerRepository.getWorkers()) {
            is AppResult.Success -> r.value.firstOrNull { it.id == workerId }?.displayName
            is AppResult.Failure -> return r
        } ?: return AppResult.Failure(AppError.NotFound)
        val today = LocalDate.now(TOKYO)
        val last = when (val r = getPastInterviews(workerId)) {
            is AppResult.Success -> r.value.maxOfOrNull { it.date }
            is AppResult.Failure -> return r
        }
        val from = minOf(last ?: today.minusMonths(INTERVIEW_MONTHS).toString(), today.toString())
        val rows = when (val r = api().supportLogRows(from, today.toString())) {
            is AppResult.Success -> r.value.filter { it.worker == name }
            is AppResult.Failure -> return r
        }
        val notices = rows.filter { it.item == ITEM_ORIENTATION }
        val consultations = rows.filter { it.item == ITEM_CONSULTATION }
        return AppResult.Success(
            InterviewPrepDto(
                workerId = workerId,
                fromDate = from,
                meetupsCount = rows.count { it.item == ITEM_EXCHANGE },
                noticesTotal = notices.size,
                noticesOk = notices.count { noticeStatus(it.handling) == WakattaStatus.GREEN },
                confusingNotices = notices.filter { noticeStatus(it.handling) == WakattaStatus.RED }.map { it.content }.distinct(),
                confusionTerms = consultations.filter { it.source == "Chat" }
                    .map { it.content.substringAfter('「').substringBefore('」') }.distinct(),
                forwardedAsks = consultations.filter { it.source == "Senpai" }.map { it.content },
                questions = INTERVIEW_QUESTIONS,
            ),
        )
    }

    override suspend fun getPastInterviews(workerId: String): AppResult<List<InterviewRecordDto>> {
        return when (val res = safeApiCall { api().getWorkerInterviews(workerId) }) {
            is AppResult.Success -> {
                val dtos = res.value.map { out ->
                    InterviewRecordDto(
                        id = out.id,
                        workerId = out.workerId,
                        date = out.heldOn,
                        method = if (out.method.lowercase() == "online") InterviewMethod.ONLINE else InterviewMethod.FACE,
                        consent = out.onlineConsentAt != null,
                        noteJa = out.noteJa,
                        recordedBy = out.recordedBy,
                    )
                }
                AppResult.Success(dtos)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun recordInterview(request: InterviewCreateRequest): AppResult<InterviewRecordDto> {
        if (request.note.trim().isEmpty()) {
            return AppResult.Failure(AppError.Validation(listOf("note")))
        }
        if (request.date > InterviewDueCalculator.todayIso()) {
            return AppResult.Failure(AppError.Validation(listOf("date")))
        }
        if (request.method == InterviewMethod.ONLINE && !request.consent) {
            return AppResult.Failure(AppError.Validation(listOf("consent")))
        }

        val body = InterviewInWire(
            heldOn = request.date,
            method = if (request.method == InterviewMethod.ONLINE) "online" else "face",
            onlineConsentAt = if (request.consent) "${request.date}T00:00:00Z" else null,
            noteJa = request.note.trim(),
        )

        val key = IdempotencyKey(UUID.randomUUID().toString())
        return when (val res = safeApiCall { api().recordInterview(request.workerId, key, body) }) {
            is AppResult.Success -> {
                val out = res.value
                refreshDueList()
                AppResult.Success(
                    InterviewRecordDto(
                        id = out.id,
                        workerId = out.workerId,
                        date = out.heldOn,
                        method = if (out.method.lowercase() == "online") InterviewMethod.ONLINE else InterviewMethod.FACE,
                        consent = out.onlineConsentAt != null,
                        noteJa = out.noteJa,
                        recordedBy = out.recordedBy,
                    ),
                )
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun getAllWorkers(): AppResult<List<WorkerInfo>> = workerRepository.getWorkers()

    private companion object {
        const val INTERVIEW_MONTHS = 3L
    }
}
