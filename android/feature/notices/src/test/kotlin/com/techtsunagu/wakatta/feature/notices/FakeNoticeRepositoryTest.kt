package com.techtsunagu.wakatta.feature.notices

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.NoticeCategory
import com.techtsunagu.wakatta.core.model.NoticeSourceKind
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.notices.data.FakeNoticeRepository
import com.techtsunagu.wakatta.feature.notices.model.CheckQuestionWithAnswer
import com.techtsunagu.wakatta.feature.notices.model.EventDto
import com.techtsunagu.wakatta.feature.notices.model.NoticeCreateRequest
import com.techtsunagu.wakatta.feature.notices.model.StepDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeNoticeRepositoryTest {

    private lateinit var repository: FakeNoticeRepository

    @Before
    fun setUp() {
        repository = FakeNoticeRepository(delayMs = 0L)
    }

    @Test
    fun getNoticesForWorkerReturnsSeededNotices() = runTest {
        val result = repository.getNoticesForWorker()
        assertTrue(result is AppResult.Success)
        val notices = (result as AppResult.Success).value
        assertEquals(4, notices.size)
        assertEquals("n_burn", notices[0].id)
        assertEquals(NoticeCategory.GARBAGE, notices[0].category)
    }

    @Test
    fun getNoticesForPosterReturnsSeededNotices() = runTest {
        val result = repository.getNoticesForPoster()
        assertTrue(result is AppResult.Success)
        val notices = (result as AppResult.Success).value
        assertEquals(4, notices.size)
        assertTrue(notices[0].workerStatuses.isNotEmpty())
    }

    @Test
    fun getNoticeByIdReturnsNoticeOrNotFound() = runTest {
        val workerResult = repository.getNoticeForWorker("n_burn")
        assertTrue(workerResult is AppResult.Success)
        assertEquals("n_burn", (workerResult as AppResult.Success).value.id)

        val posterResult = repository.getNoticeForPoster("n_burn")
        assertTrue(posterResult is AppResult.Success)
        assertEquals(1, (posterResult as AppResult.Success).value.question.correctIndex)

        val notFound = repository.getNoticeForWorker("non_existent")
        assertTrue(notFound is AppResult.Failure)
    }

    @Test
    fun toggleReminderUpdatesState() = runTest {
        val toggleResult = repository.toggleReminder("ev_burn_1")
        assertTrue(toggleResult is AppResult.Success)
        assertTrue((toggleResult as AppResult.Success).value)

        val events = (repository.getUpcomingEvents() as AppResult.Success).value
        val ev = events.find { it.id == "ev_burn_1" }
        assertNotNull(ev)
        assertTrue(ev!!.isReminderOn)

        val toggleAgain = repository.toggleReminder("ev_burn_1")
        assertFalse((toggleAgain as AppResult.Success).value)
    }

    @Test
    fun markNoticeOpenedUpdatesOpenedFlag() = runTest {
        repository.markNoticeOpened("n_burn")
        val notice = (repository.getNoticeForWorker("n_burn") as AppResult.Success).value
        assertTrue(notice.opened)
    }

    @Test
    fun submitNoticeCheckGradesServerSide() = runTest {
        // n_burn correctIndex is 1
        // Test correct answer
        val correctResult = repository.submitNoticeCheck("n_burn", 1)
        assertTrue(correctResult is AppResult.Success)
        val correctData = (correctResult as AppResult.Success).value
        assertTrue(correctData.passed)
        assertEquals(WakattaStatus.GREEN, correctData.status)

        // Verify notice updated
        val notice = (repository.getNoticeForWorker("n_burn") as AppResult.Success).value
        assertEquals(WakattaStatus.GREEN, notice.wakattaStatus)
        assertEquals(1, notice.userAnswer)
        assertEquals(true, notice.passed)

        // Test wrong answer on n_res (correct index is 1, answer 0)
        val wrongResult = repository.submitNoticeCheck("n_res", 0)
        assertTrue(wrongResult is AppResult.Success)
        val wrongData = (wrongResult as AppResult.Success).value
        assertFalse(wrongData.passed)
        assertEquals(WakattaStatus.RED, wrongData.status)

        // Test "not sure" answer (-1) on n_drill
        val notSureResult = repository.submitNoticeCheck("n_drill", -1)
        assertTrue(notSureResult is AppResult.Success)
        val notSureData = (notSureResult as AppResult.Success).value
        assertFalse(notSureData.passed)
        assertEquals(WakattaStatus.ORANGE, notSureData.status)
    }

    @Test
    fun createDraftGeneratesSampleOrTextDraft() = runTest {
        val sampleReq = NoticeCreateRequest(
            sourceKind = NoticeSourceKind.PHOTO,
            sampleId = "smp_fire",
        )
        val sampleDraft = (repository.createDraft(sampleReq) as AppResult.Success).value
        assertEquals("draft_fire", sampleDraft.id)
        assertEquals("夜警・火の用心", sampleDraft.titleJa)
        assertTrue(sampleDraft.events.isNotEmpty())

        val textReq = NoticeCreateRequest(
            sourceKind = NoticeSourceKind.TEXT,
            text = "テストお知らせ",
        )
        val textDraft = (repository.createDraft(textReq) as AppResult.Success).value
        assertEquals("draft_clean", textDraft.id)
    }

    @Test
    fun publishNoticeRequiresConfirmedEvents() = runTest {
        val eventsWithUnconfirmed = listOf(
            EventDto(
                id = "ev_test_1",
                noticeId = "draft_1",
                title = mapOf("ja" to "イベント"),
                date = "2026-12-15",
                time = "10:00",
                confirmed = false,
            )
        )
        val question = CheckQuestionWithAnswer(
            id = "q_test",
            prompt = mapOf("ja" to "質問"),
            options = listOf(mapOf("ja" to "選択肢1"), mapOf("ja" to "選択肢2")),
            correctIndex = 0,
        )

        // Should refuse publishing when events are not confirmed
        val failedResult = repository.publishNotice(
            draftId = "draft_1",
            title = mapOf("ja" to "テストタイトル"),
            steps = listOf(StepDto(text = mapOf("ja" to "ステップ1"))),
            events = eventsWithUnconfirmed,
            question = question,
        )
        assertTrue(failedResult is AppResult.Success)
        val failData = (failedResult as AppResult.Success).value
        assertFalse(failData.success)
        assertEquals("EVENTS_NOT_CONFIRMED", failData.error)

        // Should succeed when confirmed
        val confirmedEvents = listOf(eventsWithUnconfirmed[0].copy(confirmed = true))
        val successResult = repository.publishNotice(
            draftId = "draft_1",
            title = mapOf("ja" to "テストタイトル"),
            steps = listOf(StepDto(text = mapOf("ja" to "ステップ1"))),
            events = confirmedEvents,
            question = question,
        )
        assertTrue(successResult is AppResult.Success)
        val successData = (successResult as AppResult.Success).value
        assertTrue(successData.success)
        assertNotNull(successData.noticeId)
    }

    @Test
    fun getTodayWordAndLifeSummaryReturnData() = runTest {
        val wordResult = repository.getTodayWord()
        assertTrue(wordResult is AppResult.Success)
        val word = (wordResult as AppResult.Success).value
        assertNotNull(word)
        assertEquals("ধন্যবাদ", word!!.term)

        val summaryResult = repository.getLifeSummary()
        assertTrue(summaryResult is AppResult.Success)
        assertEquals("佐藤 美咲", (summaryResult as AppResult.Success).value.senpaiName)
    }
}
