package com.techtsunagu.wakatta.feature.notices.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.NoticeCategory
import com.techtsunagu.wakatta.core.model.NoticeSourceKind
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.notices.model.CheckQuestionDto
import com.techtsunagu.wakatta.feature.notices.model.CheckQuestionWithAnswer
import com.techtsunagu.wakatta.feature.notices.model.CheckResultDto
import com.techtsunagu.wakatta.feature.notices.model.EventDto
import com.techtsunagu.wakatta.feature.notices.model.LifeSummary
import com.techtsunagu.wakatta.feature.notices.model.NoticeCreateRequest
import com.techtsunagu.wakatta.feature.notices.model.NoticeDraftDto
import com.techtsunagu.wakatta.feature.notices.model.NoticeForPoster
import com.techtsunagu.wakatta.feature.notices.model.NoticeForWorker
import com.techtsunagu.wakatta.feature.notices.model.PublishResult
import com.techtsunagu.wakatta.feature.notices.model.StepDto
import com.techtsunagu.wakatta.feature.notices.model.TodayWordInfo
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * A Worker reads Notices and Events from the live server.
 * A role that the server refuses gets an empty or not-available state, never Fake content.
 */
internal class RealNoticeRepository(
    private val api: () -> NoticesApi,
    private val session: SessionStore,
    @Suppress("UNUSED_PARAMETER") fake: NoticeRepository? = null,
    private val today: () -> LocalDate = { LocalDate.now(TOKYO) },
) : NoticeRepository {

    private val isWorker get() = session.session.value?.user?.role == Role.WORKER

    // shortcut: the server has no reminder route, so reminders live in memory only, wire to local alarms later.
    private val reminders = ConcurrentHashMap.newKeySet<String>()

    override fun observeNoticesForWorker(): Flow<List<NoticeForWorker>> =
        if (!isWorker) flowOf(emptyList())
        else flow { (getNoticesForWorker() as? AppResult.Success)?.let { emit(it.value) } }

    override suspend fun getNoticesForWorker(): AppResult<List<NoticeForWorker>> {
        if (!isWorker) return AppResult.Success(emptyList())
        val all = mutableListOf<WorkerNoticeDto>()
        var cursor: String? = null
        do {
            val page = when (val r = safeApiCall { api().notices(PAGE_LIMIT, cursor) }) {
                is AppResult.Success -> r.value
                is AppResult.Failure -> return r
            }
            all += page.items
            cursor = page.nextCursor
        } while (cursor != null)
        return AppResult.Success(all.map { it.toNotice(reminders) })
    }

    override suspend fun getNoticesForPoster(): AppResult<List<NoticeForPoster>> =
        AppResult.Success(emptyList())

    override suspend fun getNoticeForWorker(id: String): AppResult<NoticeForWorker> =
        if (!isWorker) AppResult.Failure(AppError.Forbidden)
        else when (val r = safeApiCall { api().notice(id) }) {
            is AppResult.Success -> AppResult.Success(r.value.toNotice(reminders))
            is AppResult.Failure -> r
        }

    override suspend fun getNoticeForPoster(id: String): AppResult<NoticeForPoster> =
        AppResult.Failure(AppError.Forbidden)

    override suspend fun getUpcomingEvents(): AppResult<List<EventDto>> {
        if (!isWorker) return AppResult.Success(emptyList())
        val from = today()
        return when (val r = safeApiCall { api().events(from.toString(), from.plusDays(RANGE_DAYS).toString()) }) {
            is AppResult.Success -> AppResult.Success(r.value.map { it.toEvent(reminders) }.sortedBy { it.date + it.time })
            is AppResult.Failure -> r
        }
    }

    override suspend fun toggleReminder(eventId: String): AppResult<Boolean> {
        if (!isWorker) return AppResult.Success(false)
        val key = eventId.substringBefore('@')
        val on = reminders.add(key)
        if (!on) reminders.remove(key)
        return AppResult.Success(on)
    }

    /** The ViewModel already posts the Receipt through the shared repository; a second call would only duplicate it. */
    override suspend fun markNoticeOpened(id: String): AppResult<Unit> =
        AppResult.Success(Unit)

    /**
     * The shared CheckRepository already graded the answer on the server (the ViewModel calls it first), so this
     * reads the saved result back. The server rejects "not sure" (-1), so that one stays on the phone.
     * shortcut: the Worker never gets the correct option, so a wrong answer carries no correctAnswerText.
     */
    override suspend fun submitNoticeCheck(id: String, answerIndex: Int): AppResult<CheckResultDto> {
        if (!isWorker) return AppResult.Failure(AppError.Forbidden)
        if (answerIndex == NOT_SURE) {
            return AppResult.Success(CheckResultDto(false, WakattaStatus.ORANGE, "Your foreman will explain it again."))
        }
        val notice = when (val r = getNoticeForWorker(id)) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        val passed = notice.passed ?: return AppResult.Failure(AppError.Unknown("check not saved"))
        return AppResult.Success(
            CheckResultDto(passed, notice.wakattaStatus, if (passed) "Wakatta! That is right." else "Not quite."),
        )
    }

    override suspend fun createDraft(request: NoticeCreateRequest): AppResult<NoticeDraftDto> =
        AppResult.Failure(AppError.Forbidden)

    override suspend fun toggleEventConfirmed(eventId: String): AppResult<EventDto> =
        AppResult.Failure(AppError.Forbidden)

    override suspend fun publishNotice(
        draftId: String,
        title: Tri,
        steps: List<StepDto>,
        events: List<EventDto>,
        question: CheckQuestionWithAnswer,
    ): AppResult<PublishResult> = AppResult.Failure(AppError.Forbidden)

    override suspend fun getTodayWord(): AppResult<TodayWordInfo?> = AppResult.Success(null)

    override suspend fun getLifeSummary(): AppResult<LifeSummary> = AppResult.Success(LifeSummary())

    private companion object {
        const val PAGE_LIMIT = 200
        const val RANGE_DAYS = 61L // the server accepts at most 62 days
        const val NOT_SURE = -1
    }
}

internal val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")

private inline fun <reified E : Enum<E>> enumOf(wire: String, unknown: E): E =
    enumValues<E>().firstOrNull { it.name.equals(wire, ignoreCase = true) } ?: unknown

private fun String.inTokyo() = OffsetDateTime.parse(this).atZoneSameInstant(TOKYO)

private fun java.time.ZonedDateTime.clock() = "%d:%02d".format(hour, minute)

internal fun EventOutDto.toEvent(reminders: Set<String>): EventDto {
    val at = (nextDates.firstOrNull() ?: startsAt).inTokyo()
    return EventDto(id, "", title, at.toLocalDate().toString(), at.clock(), confirmed, id in reminders)
}

/** One row per occurrence: the same Event repeats, so the id carries the time to keep list keys unique. */
internal fun EventOccurrenceDto.toEvent(reminders: Set<String>): EventDto {
    val t = at.inTokyo()
    return EventDto("$eventId@$at", noticeId, title, t.toLocalDate().toString(), t.clock(), true, eventId in reminders)
}

// shortcut: the server has no sender name, audio length, translation of the source or flyer seed, so those stay default.
internal fun WorkerNoticeDto.toNotice(reminders: Set<String>) = NoticeForWorker(
    id = id,
    title = title,
    category = enumOf(category, NoticeCategory.UNKNOWN),
    sourceKind = enumOf(sourceKind, NoticeSourceKind.UNKNOWN),
    sourceText = sourceJa.ifEmpty { null },
    publishedAt = (publishedAt ?: createdAt).take(DATE_LENGTH),
    steps = steps.map { StepDto(it) },
    events = events.map { it.toEvent(reminders).copy(noticeId = id) },
    question = CheckQuestionDto(question.id, question.termId, question.prompt, question.options),
    opened = opened,
    passed = passed,
    wakattaStatus = when (passed) {
        true -> WakattaStatus.GREEN
        false -> WakattaStatus.RED
        null -> WakattaStatus.WAIT
    },
)

private const val DATE_LENGTH = 10
