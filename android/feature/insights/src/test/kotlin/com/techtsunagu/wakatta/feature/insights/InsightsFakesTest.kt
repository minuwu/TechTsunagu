package com.techtsunagu.wakatta.feature.insights

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.feature.insights.data.FakeAnalyticsRepository
import com.techtsunagu.wakatta.feature.insights.data.FakeFollowUpRepository
import com.techtsunagu.wakatta.feature.insights.data.FakeLearningRepository
import com.techtsunagu.wakatta.feature.insights.data.FakeSkillMatrixRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsFakesTest {

    private val followUps = FakeFollowUpRepository(0)

    private fun <T> AppResult<T>.value(): T = (this as AppResult.Success).value

    @Test
    fun followUps_statsCountExplainedAndUnderstood() = runTest {
        val stats = followUps.getFollowUps().value().stats
        assertEquals(3, stats.flagged)
        assertEquals(2, stats.explained)
        assertEquals(0, stats.understood)
        assertEquals(6, stats.records)
    }

    @Test
    fun analytics_ranksTermsAndComputesTrendAndGlossary() = runTest {
        val repo = FakeAnalyticsRepository(0, followUps, TestGlossary(pinned = listOf("治具")))
        val data = repo.getAnalytics().value()
        val top = data.terms.first()
        assertEquals("玉掛け", top.term)
        assertEquals(4, top.count)
        assertEquals(3, top.workerCount)
        assertEquals(-2, top.trend)
        assertTrue(data.terms.first { it.term == "治具" }.inGlossary)
        assertFalse(top.inGlossary)
        assertEquals(listOf(62, 68, 74, 78), data.trend)
        val cell = data.heat.first { it.term == "玉掛け" }.cells.first { it.workerId == "u_siti" }
        assertEquals(2, cell.count)
    }

    @Test
    fun analytics_glossaryFailureIsReturnedNotIgnored() = runTest {
        val glossary = TestGlossary().apply { failure = AppError.Offline }
        val result = FakeAnalyticsRepository(0, followUps, glossary).getAnalytics()
        assertEquals(AppError.Offline, result.errorOrNull())
    }

    @Test
    fun skillMatrix_statusFollowsSignOffAge() = runTest {
        val matrix = FakeSkillMatrixRepository(0) { 1_000_000_000_000L }.getMatrix().value()
        fun cell(w: String, m: String) = matrix.rows.first { it.workerId == w }.cells.first { it.machineId == m }
        assertEquals(SkillStatus.VALID, cell("u_rahim", "mc1").status)
        assertEquals(SkillStatus.EXPIRING, cell("u_nguyen", "mc1").status)
        assertEquals(9, cell("u_nguyen", "mc1").daysLeft)
        assertEquals(SkillStatus.NONE, cell("u_rahim", "mc2").status)
    }

    @Test
    fun learning_buildsGapsAndMasteredFromConfusionsAndFollowUps() = runTest {
        val history = FakeLearningRepository(0, followUps).getHistory("u_siti").value()
        assertEquals("3号機オイル", history.entries.first().term)
        assertEquals(FollowUpStatus.OPEN, history.entries.first().status)
        assertEquals(2, history.gaps)
        assertEquals(4, history.mastered)
        assertEquals(2, history.streak)
    }

    @Test
    fun learning_unknownWorkerIsNotFound() = runTest {
        val result = FakeLearningRepository(0, followUps).getHistory("nobody")
        assertEquals(AppError.NotFound, result.errorOrNull())
    }
}
