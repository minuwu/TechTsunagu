package com.techtsunagu.wakatta.feature.support.data

import android.content.Context
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.DueInterview
import com.techtsunagu.wakatta.core.model.InterviewMethod
import com.techtsunagu.wakatta.core.model.InterviewStatus
import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.feature.support.domain.InterviewCreateRequest
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueCalculator
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewPrepDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewQuestionItem
import com.techtsunagu.wakatta.feature.support.domain.InterviewRecordDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewRepository
import com.techtsunagu.wakatta.feature.support.domain.SupportLogData
import com.techtsunagu.wakatta.feature.support.domain.SupportLogRepository
import com.techtsunagu.wakatta.feature.support.domain.SupportLogSummary
import com.techtsunagu.wakatta.feature.support.domain.SupportMeetupDto
import com.techtsunagu.wakatta.feature.support.domain.SupportNoticeDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeInterviewRepository(
    private val outboxWriter: OutboxWriter,
    private val simulatedLatencyMs: Long = 300L,
) : InterviewRepository {

    @Inject
    constructor(outboxWriter: OutboxWriter) : this(outboxWriter, 300L)

    private val workersList: List<WorkerInfo> = DemoSeed.users
        .filter { it.role == Role.WORKER }
        .map { WorkerInfo(id = it.id, displayName = it.displayName, language = it.language) }

    // Seeded interviews from D.js
    private val interviews = mutableListOf(
        InterviewRecordDto(
            id = "iv1",
            workerId = "u_rahim",
            date = "2026-10-20",
            method = InterviewMethod.FACE,
            consent = false,
            noteJa = "仕事も生活も問題なし。日本語を勉強している。",
            recordedBy = "田中 健一",
        ),
        InterviewRecordDto(
            id = "iv2",
            workerId = "u_nguyen",
            date = "2026-08-28",
            method = InterviewMethod.ONLINE,
            consent = true,
            noteJa = "残業が多いと相談。来月の班の予定を見直す。",
            recordedBy = "田中 健一",
        ),
        InterviewRecordDto(
            id = "iv3",
            workerId = "u_siti",
            date = "2026-07-15",
            method = InterviewMethod.FACE,
            consent = false,
            noteJa = "寮の部屋の暖房について相談。",
            recordedBy = "田中 健一",
        ),
    )

    private val _dueInterviewsState = MutableStateFlow(computeDueList())

    override fun getDueInterviews(): Flow<List<DueInterview>> = _dueInterviewsState.asStateFlow()

    private fun computeDueList(): List<DueInterview> {
        return workersList.map { worker ->
            val last = interviews
                .filter { it.workerId == worker.id }
                .maxByOrNull { it.date }
                ?.date
            val dueDto = InterviewDueCalculator.calculateDue(worker.id, worker.displayName, last)
            InterviewDueCalculator.toDueInterview(dueDto)
        }
    }

    override suspend fun getAllWorkers(): AppResult<List<WorkerInfo>> {
        delay(simulatedLatencyMs)
        return AppResult.Success(workersList)
    }

    override suspend fun getDueInfo(workerId: String): AppResult<InterviewDueDto> {
        delay(simulatedLatencyMs)
        val worker = workersList.find { it.id == workerId }
            ?: return AppResult.Failure(AppError.NotFound)

        val lastDate = synchronized(interviews) {
            interviews.filter { it.workerId == workerId }.maxByOrNull { it.date }?.date
        }
        val dto = InterviewDueCalculator.calculateDue(worker.id, worker.displayName, lastDate)
        return AppResult.Success(dto)
    }

    override suspend fun getInterviewPrep(workerId: String): AppResult<InterviewPrepDto> {
        delay(simulatedLatencyMs)
        val worker = workersList.find { it.id == workerId }
            ?: return AppResult.Failure(AppError.NotFound)

        val lastDate = synchronized(interviews) {
            interviews.filter { it.workerId == workerId }.maxByOrNull { it.date }?.date
        } ?: "2026-08-24"

        // Context facts per worker ported from D.js
        val none = emptyList<String>()
        val facts = when (workerId) {
            "u_nguyen" -> PrepFacts(listOf("資源ごみの分け方"), listOf("ペットボトル"), none)
            "u_siti" -> PrepFacts(listOf("地区の防災訓練 12/7 9:00 公民館"), listOf("消火器"), none)
            "u_rahim" -> PrepFacts(none, none, listOf("安く仕送りできるところはどこですか？"))
            else -> PrepFacts(none, none, none)
        }
        val confusingNotices = facts.notices
        val terms = facts.terms
        val asks = facts.asks

        val prep = InterviewPrepDto(
            workerId = workerId,
            fromDate = lastDate,
            meetupsCount = if (workerId == "u_rahim") 2 else 1,
            noticesTotal = 4,
            noticesOk = if (workerId == "u_rahim") 2 else 3,
            confusingNotices = confusingNotices,
            confusionTerms = terms,
            forwardedAsks = asks,
            questions = INTERVIEW_QUESTIONS,
        )
        return AppResult.Success(prep)
    }

    override suspend fun getPastInterviews(workerId: String): AppResult<List<InterviewRecordDto>> {
        delay(simulatedLatencyMs)
        val list = synchronized(interviews) {
            interviews.filter { it.workerId == workerId }.sortedByDescending { it.date }
        }
        return AppResult.Success(list)
    }

    override suspend fun recordInterview(request: InterviewCreateRequest): AppResult<InterviewRecordDto> {
        delay(simulatedLatencyMs)

        // Validation rules from spec / D.js saveIv
        if (request.note.trim().isEmpty()) {
            return AppResult.Failure(AppError.Validation(listOf("note")))
        }
        if (request.date > InterviewDueCalculator.todayIso()) {
            return AppResult.Failure(AppError.Validation(listOf("date")))
        }
        if (request.method == InterviewMethod.ONLINE && !request.consent) {
            return AppResult.Failure(AppError.Validation(listOf("consent")))
        }

        val id = "iv" + System.currentTimeMillis().toString(36)
        val record = InterviewRecordDto(
            id = id,
            workerId = request.workerId,
            date = request.date,
            method = request.method,
            consent = request.consent,
            noteJa = request.note.trim(),
            recordedBy = "田中 健一",
        )

        synchronized(interviews) {
            interviews.add(0, record)
        }

        // Offline outbox queue requirement
        val payloadJson = """{"id":"$id","workerId":"${request.workerId}","date":"${request.date}","method":"${request.method}","consent":${request.consent},"note":"${request.note.trim().jsonEscaped()}","by":"田中 健一"}"""
        outboxWriter.enqueue("interview", UUID.randomUUID().toString(), payloadJson)

        _dueInterviewsState.value = computeDueList()

        return AppResult.Success(record)
    }
}

@Singleton
class FakeSupportLogRepository(
    private val interviewRepository: InterviewRepository,
    private val context: Context? = null,
    private val simulatedLatencyMs: Long = 300L,
) : SupportLogRepository {

    @Inject
    constructor(
        interviewRepository: InterviewRepository,
        @ApplicationContext context: Context,
    ) : this(interviewRepository, context, 300L)

    private val workersList = DemoSeed.users
        .filter { it.role == Role.WORKER }
        .map { WorkerInfo(id = it.id, displayName = it.displayName, language = it.language) }

    // Seeded meetups with Senpai from D.js
    private val meetupsList = listOf(
        SupportMeetupDto(
            id = "mu1",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            senpaiName = "佐藤 美咲",
            kind = "coffee",
            date = "2026-10-27",
            noteJa = "カフェで日本語の練習",
            noteEn = "Japanese practice at a cafe",
            recordedBy = "佐藤 美咲",
        ),
        SupportMeetupDto(
            id = "mu2",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            senpaiName = "佐藤 美咲",
            kind = "coffee",
            date = "2026-11-05",
            noteJa = "給料明細の見かたを説明",
            noteEn = "Explained how to read the pay slip",
            recordedBy = "佐藤 美咲",
        ),
        SupportMeetupDto(
            id = "mu3",
            workerId = "u_nguyen",
            workerName = "Nguyen Van An",
            senpaiName = "佐藤 美咲",
            kind = "coffee",
            date = "2026-11-12",
            noteJa = "病院の行きかたを相談",
            noteEn = "Talked about how to visit a clinic",
            recordedBy = "佐藤 美咲",
        ),
        SupportMeetupDto(
            id = "mu4",
            workerId = "u_siti",
            workerName = "Siti Rahma",
            senpaiName = "佐藤 美咲",
            kind = "drill",
            date = "2026-11-16",
            noteJa = "駅前で避難経路を確認",
            noteEn = "Checked the evacuation route near the station",
            recordedBy = "佐藤 美咲",
        ),
    )

    // Seeded notices and check responses from D.js
    private val noticesList = listOf(
        SupportNoticeDto(
            id = "n_burn",
            titleJa = "燃えるごみ 月・木 8:00まで",
            date = "2026-11-10",
            workerStatuses = mapOf(
                "u_rahim" to WakattaStatus.WAIT,
                "u_nguyen" to WakattaStatus.GREEN,
                "u_siti" to WakattaStatus.GREEN,
            ),
        ),
        SupportNoticeDto(
            id = "n_res",
            titleJa = "資源ごみの分け方",
            date = "2026-11-12",
            workerStatuses = mapOf(
                "u_rahim" to WakattaStatus.GREEN,
                "u_nguyen" to WakattaStatus.ORANGE,
                "u_siti" to WakattaStatus.GREEN,
            ),
        ),
        SupportNoticeDto(
            id = "n_drill",
            titleJa = "地区の防災訓練 12/7 9:00 公民館",
            date = "2026-11-18",
            workerStatuses = mapOf(
                "u_rahim" to WakattaStatus.WAIT,
                "u_nguyen" to WakattaStatus.GREEN,
                "u_siti" to WakattaStatus.RED,
            ),
        ),
        SupportNoticeDto(
            id = "n_fest",
            titleJa = "秋祭り 11/30",
            date = "2026-11-20",
            workerStatuses = mapOf(
                "u_rahim" to WakattaStatus.GREEN,
                "u_nguyen" to WakattaStatus.GREEN,
                "u_siti" to WakattaStatus.GREEN,
            ),
        ),
    )

    private val availableMonths = listOf("all", "2026-11", "2026-10", "2026-08", "2026-07")

    override suspend fun getSupportLog(
        workerId: String?,
        month: String?,
    ): AppResult<SupportLogData> {
        delay(simulatedLatencyMs)

        val targetWorkerId = if (workerId.isNullOrBlank()) "all" else workerId
        val targetMonth = if (month.isNullOrBlank()) "2026-11" else month

        val activeWorkers = if (targetWorkerId == "all") {
            workersList
        } else {
            workersList.filter { it.id == targetWorkerId }
        }

        // Fetch interviews from interviewRepository
        val allInterviews = mutableListOf<InterviewRecordDto>()
        for (w in workersList) {
            when (val res = interviewRepository.getPastInterviews(w.id)) {
                is AppResult.Success -> allInterviews.addAll(res.value)
                is AppResult.Failure -> {}
            }
        }

        // Filter interviews
        val filteredInterviews = allInterviews.filter { iv ->
            (targetWorkerId == "all" || iv.workerId == targetWorkerId) &&
                (targetMonth == "all" || iv.date.startsWith(targetMonth))
        }.sortedByDescending { it.date }

        // Filter meetups
        val filteredMeetups = meetupsList.filter { mu ->
            (targetWorkerId == "all" || mu.workerId == targetWorkerId) &&
                (targetMonth == "all" || mu.date.startsWith(targetMonth))
        }.sortedByDescending { it.date }

        // Filter notices
        val filteredNotices = noticesList.filter { n ->
            targetMonth == "all" || n.date.startsWith(targetMonth)
        }.sortedByDescending { it.date }

        // Due info for active workers
        val dueList = activeWorkers.map { w ->
            val last = allInterviews.filter { it.workerId == w.id }.maxByOrNull { it.date }?.date
            InterviewDueCalculator.calculateDue(w.id, w.displayName, last)
        }

        // Check counts
        var okCount = 0
        var totalCount = 0
        for (n in filteredNotices) {
            for (w in activeWorkers) {
                totalCount++
                if (n.workerStatuses[w.id] == WakattaStatus.GREEN) {
                    okCount++
                }
            }
        }

        val summary = SupportLogSummary(
            interviewCount = filteredInterviews.size,
            meetupCount = filteredMeetups.size,
            noticeOkCount = okCount,
            noticeTotalCount = totalCount,
        )

        return AppResult.Success(
            SupportLogData(
                selectedWorkerId = targetWorkerId,
                selectedMonth = targetMonth,
                availableMonths = availableMonths,
                workers = workersList,
                summary = summary,
                dueInterviews = dueList,
                meetups = filteredMeetups,
                notices = filteredNotices,
            ),
        )
    }

    override suspend fun exportSupportLogCsv(
        workerId: String?,
        month: String?,
    ): AppResult<File> {
        delay(simulatedLatencyMs)
        val logResult = getSupportLog(workerId, month)
        if (logResult is AppResult.Failure) {
            return AppResult.Failure(logResult.error)
        }
        val data = (logResult as AppResult.Success).value

        return try {
            val cacheDir = context?.cacheDir ?: File(System.getProperty("java.io.tmpdir") ?: ".")
            val file = File(cacheDir, "support-log-${data.selectedMonth}-${data.selectedWorkerId}.csv")
            FileOutputStream(file).use { fos ->
                // UTF-8 with BOM
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.appendLine("特定技能1号 生活支援実施記録 (Support Log)")
                    val period = if (data.selectedMonth == "all") "全期間" else data.selectedMonth
                    val target = if (data.selectedWorkerId == "all") "作業者全員" else workersList.find { it.id == data.selectedWorkerId }?.displayName ?: data.selectedWorkerId
                    writer.appendLine("期間,${escapeCsv(period)}")
                    writer.appendLine("対象,${escapeCsv("$target · 登録支援機関への報告用")}")
                    writer.appendLine("概要,定期面談 ${data.summary.interviewCount}件 · 先輩との面談 ${data.summary.meetupCount}件 · お知らせ確認 ${data.summary.noticeOkCount}/${data.summary.noticeTotalCount}")
                    writer.appendLine()

                    // Section 1: Periodic Interviews
                    writer.appendLine("[定期面談 (Periodic Interviews)]")
                    writer.appendLine("日付,作業者,実施方法,本人の同意,内容・対応,記録者")
                    val allInterviews = mutableListOf<InterviewRecordDto>()
                    for (w in workersList) {
                        if (data.selectedWorkerId == "all" || w.id == data.selectedWorkerId) {
                            when (val res = interviewRepository.getPastInterviews(w.id)) {
                                is AppResult.Success -> allInterviews.addAll(res.value)
                                is AppResult.Failure -> {}
                            }
                        }
                    }
                    val filteredIvs = allInterviews.filter { iv ->
                        data.selectedMonth == "all" || iv.date.startsWith(data.selectedMonth)
                    }
                    if (filteredIvs.isEmpty()) {
                        writer.appendLine("なし,,,,,")
                    } else {
                        for (iv in filteredIvs) {
                            val wName = workersList.find { it.id == iv.workerId }?.displayName ?: iv.workerId
                            val methodStr = if (iv.method == InterviewMethod.ONLINE) "オンライン" else "対面"
                            val consentStr = if (iv.consent) "同意あり" else "-"
                            writer.appendLine(
                                "${escapeCsv(iv.date)},${escapeCsv(wName)},${escapeCsv(methodStr)},${escapeCsv(consentStr)},${escapeCsv(iv.noteJa)},${escapeCsv(iv.recordedBy)}",
                            )
                        }
                    }
                    writer.appendLine()

                    // Section 2: Meet-ups with Senpai
                    writer.appendLine("[先輩との面談 (Meet-ups with Senpai)]")
                    writer.appendLine("日付,作業者,先輩,種類,内容,記録者")
                    if (data.meetups.isEmpty()) {
                        writer.appendLine("なし,,,,,")
                    } else {
                        for (mu in data.meetups) {
                            writer.appendLine(
                                "${escapeCsv(mu.date)},${escapeCsv(mu.workerName)},${escapeCsv(mu.senpaiName)},${escapeCsv(mu.kind)},${escapeCsv(mu.noteJa)},${escapeCsv(mu.recordedBy)}",
                            )
                        }
                    }
                    writer.appendLine()

                    // Section 3: Notice confirmations
                    writer.appendLine("[お知らせ確認 (Notice Confirmations)]")
                    writer.appendLine("日付,お知らせ名,作業者,確認結果")
                    if (data.notices.isEmpty()) {
                        writer.appendLine("なし,,,")
                    } else {
                        for (n in data.notices) {
                            val includedWorkers = if (data.selectedWorkerId == "all") {
                                workersList
                            } else {
                                workersList.filter { it.id == data.selectedWorkerId }
                            }
                            for (w in includedWorkers) {
                                val st = when (n.workerStatuses[w.id]) {
                                    WakattaStatus.GREEN -> "わかった (Understood)"
                                    WakattaStatus.ORANGE -> "要確認 (Needs confirmation)"
                                    WakattaStatus.RED -> "未理解 (Not understood)"
                                    WakattaStatus.WAIT, null, WakattaStatus.UNKNOWN -> "未回答 (Not answered)"
                                }
                                writer.appendLine(
                                    "${escapeCsv(n.date)},${escapeCsv(n.titleJa)},${escapeCsv(w.displayName)},${escapeCsv(st)}",
                                )
                            }
                        }
                    }
                    writer.flush()
                }
            }
            AppResult.Success(file)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown("Failed to generate CSV: ${e.message}"))
        }
    }

    /**
     * Escapes CSV cell to prevent formula injection (=, +, -, @) and wraps in quotes if needed.
     */
    private fun escapeCsv(raw: String): String {
        var str = raw.replace("\"", "\"\"")
        if (str.startsWith("=") || str.startsWith("+") || str.startsWith("-") || str.startsWith("@")) {
            str = "'$str"
        }
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            "\"$str\""
        } else {
            str
        }
    }
}

private fun String.jsonEscaped(): String = buildString {
    for (c in this@jsonEscaped) when (c) {
        '"' -> append("\\\"")
        '\\' -> append("\\\\")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> append(c)
    }
}

private data class PrepFacts(val notices: List<String>, val terms: List<String>, val asks: List<String>)

/** The questions of every Periodic Interview (定期面談). */
internal val INTERVIEW_QUESTIONS = listOf(
    InterviewQuestionItem(
        ja = "仕事で困っていることはありますか？",
        en = "Is anything difficult at work?",
        bn = "কাজে কি কোনো সমস্যা হচ্ছে?",
    ),
    InterviewQuestionItem(
        ja = "機械や作業の説明で、わかりにくい言葉はありますか？",
        en = "Are there words in the instructions you do not understand?",
        bn = "মেশিন বা কাজের নির্দেশনায় কোনো কঠিন শব্দ আছে কি?",
    ),
    InterviewQuestionItem(
        ja = "最近のお知らせで、わからなかったことはありますか？",
        en = "Was anything unclear in the recent notices?",
        bn = "সাম্প্রতিক নোটিশে কি কিছু বুঝতে সমস্যা হয়েছিল?",
    ),
    InterviewQuestionItem(
        ja = "給料・住まい・健康で、相談したいことはありますか？",
        en = "Do you want to talk about pay, housing or health?",
        bn = "বেতন, বাসস্থান বা স্বাস্থ্য নিয়ে কিছু বলার আছে?",
    ),
    InterviewQuestionItem(
        ja = "先輩や職場の人との関係はどうですか？",
        en = "How are things with your Senpai and coworkers?",
        bn = "সিনিয়র বা সহকর্মীদের সাথে সম্পর্ক কেমন?",
    ),
    InterviewQuestionItem(
        ja = "法律に違反することやハラスメントなど、困っていることはありませんか？",
        en = "Is anything illegal or unfair happening, such as harassment?",
        bn = "আইন লঙ্ঘন বা হয়রানির মতো কোনো সমস্যা হচ্ছে কি?",
    ),
)
