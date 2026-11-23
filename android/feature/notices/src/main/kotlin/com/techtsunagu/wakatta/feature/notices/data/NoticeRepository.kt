package com.techtsunagu.wakatta.feature.notices.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Tri
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
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {
    fun observeNoticesForWorker(): Flow<List<NoticeForWorker>>
    suspend fun getNoticesForWorker(): AppResult<List<NoticeForWorker>>
    suspend fun getNoticesForPoster(): AppResult<List<NoticeForPoster>>
    suspend fun getNoticeForWorker(id: String): AppResult<NoticeForWorker>
    suspend fun getNoticeForPoster(id: String): AppResult<NoticeForPoster>
    suspend fun getUpcomingEvents(): AppResult<List<EventDto>>
    suspend fun toggleReminder(eventId: String): AppResult<Boolean>
    suspend fun markNoticeOpened(id: String): AppResult<Unit>
    suspend fun submitNoticeCheck(id: String, answerIndex: Int): AppResult<CheckResultDto>
    suspend fun createDraft(request: NoticeCreateRequest): AppResult<NoticeDraftDto>
    suspend fun toggleEventConfirmed(eventId: String): AppResult<EventDto>
    suspend fun publishNotice(
        draftId: String,
        title: Tri,
        steps: List<StepDto>,
        events: List<EventDto>,
        question: CheckQuestionWithAnswer,
    ): AppResult<PublishResult>
    suspend fun getTodayWord(): AppResult<TodayWordInfo?>
    suspend fun getLifeSummary(): AppResult<LifeSummary>
}
