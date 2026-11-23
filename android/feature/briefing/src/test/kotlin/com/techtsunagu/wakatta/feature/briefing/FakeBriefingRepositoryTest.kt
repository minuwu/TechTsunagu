package com.techtsunagu.wakatta.feature.briefing

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckQuestion
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.briefing.data.FakeBriefingRepository
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForWorker
import com.techtsunagu.wakatta.feature.briefing.model.BriefingPublishRequest
import com.techtsunagu.wakatta.feature.briefing.model.CheckQuestionWithAnswer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeBriefingRepositoryTest {

    private lateinit var repository: FakeBriefingRepository

    @Before
    fun setUp() {
        repository = FakeBriefingRepository(delayMs = 0L)
    }

    @Test
    fun getBriefings_returnsSeededBriefings() = runTest {
        val result = repository.getBriefings()
        assertTrue(result is AppResult.Success)
        val list = (result as AppResult.Success).value
        assertTrue(list.isNotEmpty())
        assertEquals("b_today", list[0].id)
    }

    @Test
    fun getBriefingForForeman_returnsByIdAndLatest() = runTest {
        val seedResult = repository.getBriefingForForeman("b_seed")
        assertTrue(seedResult is AppResult.Success)
        assertEquals("b_seed", (seedResult as AppResult.Success).value.id)

        val latestResult = repository.getBriefingForForeman(null)
        assertTrue(latestResult is AppResult.Success)
        assertEquals("b_today", (latestResult as AppResult.Success).value.id)

        val notFoundResult = repository.getBriefingForForeman("non_existent")
        assertTrue(notFoundResult is AppResult.Failure)
    }

    @Test
    fun getBriefingForWorker_returnsWithoutCorrectIndex() = runTest {
        val result = repository.getBriefingForWorker("b_today")
        assertTrue(result is AppResult.Success)
        val workerBriefing = (result as AppResult.Success).value
        assertEquals("b_today", workerBriefing.id)
        assertNotNull(workerBriefing.question)
    }

    @Test
    fun createDraft_generatesDraftWithExpectedFields() = runTest {
        val result = repository.createDraft(
            text = "テスト朝礼",
            audioFile = null,
            idempotencyKey = "key_1",
        )
        assertTrue(result is AppResult.Success)
        val draft = (result as AppResult.Success).value
        assertEquals("テスト朝礼", draft.transcriptJa)
        assertEquals(0, draft.question.correctIndex)
        assertFalse(draft.needsEdit)
    }

    @Test
    fun publishBriefing_prependsPublishedBriefing() = runTest {
        val request = BriefingPublishRequest(
            id = "b_new_123",
            text = mapOf("ja" to "配信朝礼", "en" to "Published briefing"),
            focusText = mapOf("ja" to "重点", "en" to "Focus"),
            changeText = mapOf("ja" to "変更", "en" to "Change"),
            question = CheckQuestionWithAnswer(
                id = "q_1",
                prompt = mapOf("ja" to "質問", "en" to "Question"),
                options = listOf(mapOf("ja" to "正解", "en" to "Correct")),
                correctIndex = 0,
            ),
        )
        val result = repository.publishBriefing(request)
        assertTrue(result is AppResult.Success)
        assertEquals("b_new_123", (result as AppResult.Success).value.id)

        val list = (repository.getBriefings() as AppResult.Success).value
        assertEquals("b_new_123", list.first().id)
    }

    @Test
    fun getReceipts_and_getWorkerStatuses_categorizeCorrectly() = runTest {
        val receiptsResult = repository.getReceipts("b_seed")
        assertTrue(receiptsResult is AppResult.Success)
        val receipts = (receiptsResult as AppResult.Success).value
        assertTrue(receipts.understood.isNotEmpty())

        val statusesResult = repository.getWorkerStatuses("b_seed")
        assertTrue(statusesResult is AppResult.Success)
        val statuses = (statusesResult as AppResult.Success).value
        assertEquals(3, statuses.size)
    }

    @Test
    fun recordWorkerAnswer_updatesStatus() = runTest {
        val answerResult = repository.recordWorkerAnswer(
            briefingId = "b_today",
            workerId = "u_rahim",
            answerIndex = 0,
            status = WakattaStatus.GREEN,
        )
        assertTrue(answerResult is AppResult.Success)

        val statusesResult = repository.getWorkerStatuses("b_today")
        val statuses = (statusesResult as AppResult.Success).value
        val rahimStatus = statuses.find { it.worker.id == "u_rahim" }
        assertNotNull(rahimStatus)
        assertEquals(WakattaStatus.GREEN, rahimStatus?.status)
        assertEquals(0, rahimStatus?.answerIndex)
        assertTrue(rahimStatus?.hasAttended == true)
    }

    @Test
    fun sendReexplanation_succeeds() = runTest {
        val result = repository.sendReexplanation("b_seed", "u_nguyen")
        assertTrue(result is AppResult.Success)
    }

    @Test
    fun markOpened_updatesAttendance() = runTest {
        val markResult = repository.markOpened("b_today")
        assertTrue(markResult is AppResult.Success)

        val statuses = (repository.getWorkerStatuses("b_today") as AppResult.Success).value
        val rahim = statuses.find { it.worker.id == "u_rahim" }
        assertTrue(rahim?.hasAttended == true)
    }

    /**
     * Rule R-3 reflection check:
     * Asserts that Worker-facing models (`BriefingForWorker` and `CheckQuestion`)
     * NEVER contain a field for the answer key (`correctIndex`).
     */
    @Test
    fun ruleR3_workerFacingModelsHaveNoAnswerField() {
        val workerBriefingFields = BriefingForWorker::class.java.declaredFields.map { it.name }
        assertFalse("BriefingForWorker must not have correctIndex", workerBriefingFields.contains("correctIndex"))
        assertFalse("BriefingForWorker must not have answer", workerBriefingFields.contains("answer"))

        val checkQuestionFields = CheckQuestion::class.java.declaredFields.map { it.name }
        assertFalse("CheckQuestion must not have correctIndex", checkQuestionFields.contains("correctIndex"))
        assertFalse("CheckQuestion must not have answer", checkQuestionFields.contains("answer"))
    }
}
