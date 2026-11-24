package com.techtsunagu.wakatta.feature.insights

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.insights.data.InsightsApi
import com.techtsunagu.wakatta.feature.insights.data.RealAnalyticsRepository
import com.techtsunagu.wakatta.feature.insights.data.RealFollowUpRepository
import com.techtsunagu.wakatta.feature.insights.data.RealLearningRepository
import com.techtsunagu.wakatta.feature.insights.data.RealSkillMatrixRepository
import java.time.Instant
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealInsightsRepositoriesTest {

    private val server = MockWebServer().also { it.start() }
    private val sessionStore = TestSessionStore(DemoSeed.demoWorker)
    private val api: InsightsApi = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, sessionStore, debug = false))
        .create(InsightsApi::class.java)

    private val fixedNow = Instant.parse("2026-11-24T12:00:00Z")

    @After
    fun tearDown() = server.close()

    private fun ok(data: String, code: Int = 200) = server.enqueue(
        MockResponse.Builder().code(code).body("""{"success":true,"data":$data,"message":"ok"}""").build(),
    )

    // ---- LearningRepository ----

    @Test
    fun learning_workerGetsOwnHistory() = runBlocking {
        ok(
            """{
            "streakDays": 5,
            "nextCursor": null,
            "items": [
                {
                    "followUpId": "f1",
                    "termJa": "金型交換",
                    "status": "clarified",
                    "missedAt": "2026-11-24T10:00:00Z",
                    "clarifiedAt": "2026-11-24T11:00:00Z",
                    "understoodAt": null
                },
                {
                    "followUpId": "f2",
                    "termJa": "切粉",
                    "status": "understood",
                    "missedAt": "2026-11-23T12:00:00Z",
                    "clarifiedAt": null,
                    "understoodAt": "2026-11-23T14:00:00Z"
                }
            ]
        }""",
        )

        val repo = RealLearningRepository({ api }, sessionStore, now = { fixedNow })
        val result = repo.getHistory("any_worker_id")
        assertTrue(result.isSuccess)
        val history = (result as AppResult.Success).value
        assertEquals(5, history.streak)
        assertEquals(1, history.mastered)
        assertEquals(1, history.gaps)
        assertEquals(2, history.entries.size)
        assertEquals("金型交換", history.entries[0].term)
        assertEquals(FollowUpStatus.CLARIFIED, history.entries[0].status)
        assertEquals(120L, history.entries[0].ageMinutes)
        assertEquals(1, history.entries[0].missed)

        val request = server.takeRequest()
        assertTrue(request.url.toString().contains("/api/workers/me/learning"))
    }

    @Test
    fun learning_foremanOpeningAnotherWorkerReturnsEmpty() = runBlocking {
        val foremanUser = User(
            id = "f_yamaguchi",
            displayName = "Yamaguchi",
            role = Role.FOREMAN,
            language = "ja",
            shopId = "SASEBO01",
            districtId = "MINATO26",
        )
        val foremanStore = TestSessionStore(foremanUser)
        val repo = RealLearningRepository({ api }, foremanStore, now = { fixedNow })

        val result = repo.getHistory("u_other_worker")
        assertTrue(result.isSuccess)
        val history = (result as AppResult.Success).value
        assertEquals(0, history.streak)
        assertEquals(0, history.mastered)
        assertEquals(0, history.gaps)
        assertTrue(history.entries.isEmpty())
        // Verified: server was never touched because route does not exist
        assertEquals(0, server.requestCount)
    }

    // ---- AnalyticsRepository ----

    @Test
    fun analytics_loadsUnderstandingAndHeatmap() = runBlocking {
        ok(
            """{
            "days": 30,
            "trend": [
                {"day": "2026-11-01", "green": 5, "orange": 1, "red": 0},
                {"day": "2026-11-08", "green": 8, "orange": 2, "red": 0}
            ],
            "perWorker": [
                {"workerId": "w1", "displayName": "Worker One", "green": 10, "orange": 2, "red": 1}
            ],
            "topTerms": [
                {"termJa": "玉掛け", "count": 4}
            ]
        }""",
        )

        ok(
            """{
            "machines": [
                {"id": "m1", "code": "press-1", "name": {"ja": "プレス1号機", "en": "Press 1"}}
            ],
            "rows": [
                {
                    "termId": "t1",
                    "ja": "玉掛け",
                    "reading": "たまがけ",
                    "translations": {"en": "slinging"},
                    "cells": [{"machineId": "m1", "count": 2}],
                    "total": 2
                }
            ]
        }""",
        )

        val followUps = StubFollowUps(AppResult.Success(sampleFollowUps()))
        val glossary = TestGlossary(pinned = listOf("玉掛け"))
        val repo = RealAnalyticsRepository({ api }, followUps, glossary)

        val result = repo.getAnalytics()
        assertTrue(result.isSuccess)
        val data = (result as AppResult.Success).value

        assertEquals(16, data.sent)
        assertEquals(13, data.understood)
        assertEquals(1, data.bars.size)
        assertEquals("w1", data.bars[0].workerId)
        assertEquals(1, data.terms.size)
        assertEquals("玉掛け", data.terms[0].term)
        assertTrue(data.terms[0].inGlossary)
        assertFalse(data.terms[0].canPin)
        assertEquals(1, data.heatWorkers.size)
        assertEquals("m1", data.heatWorkers[0].id)
        assertEquals(1, data.heat.size)
        assertEquals("玉掛け", data.heat[0].term)
        assertEquals(1, data.heat[0].cells.size)
        assertEquals(2, data.heat[0].cells[0].count)
    }

    @Test
    fun analytics_pinTermReturnsValidationFailureBecauseRequiredFieldsMissing() = runBlocking {
        val followUps = StubFollowUps()
        val repo = RealAnalyticsRepository({ api }, followUps, TestGlossary())
        val result = repo.pinTerm("玉掛け", "slinging")
        assertTrue(result.isFailure)
        assertEquals(AppError.Validation(), (result as AppResult.Failure).error)
    }

    // ---- SkillMatrixRepository ----

    @Test
    fun skillMatrix_loadsAndMapsStatusAndDates() = runBlocking {
        ok(
            """{
            "machines": [
                {"id": "mc1", "code": "lathe-1", "name": {"ja": "旋盤1号機", "en": "Lathe 1"}}
            ],
            "rows": [
                {
                    "workerId": "w1",
                    "displayName": "Worker 1",
                    "cells": [
                        {"machineId": "mc1", "status": "valid", "passedAt": "2026-11-20T00:00:00Z"}
                    ]
                }
            ]
        }""",
        )

        val repo = RealSkillMatrixRepository({ api }, now = { fixedNow })
        val result = repo.getMatrix()
        assertTrue(result.isSuccess)
        val matrix = (result as AppResult.Success).value
        assertEquals(1, matrix.machines.size)
        assertEquals("mc1", matrix.machines[0].id)
        assertEquals(1, matrix.rows.size)
        val cell = matrix.rows[0].cells[0]
        assertEquals(SkillStatus.VALID, cell.status)
        assertNotNull(cell.signedAtMillis)
        assertNotNull(cell.daysLeft)
    }

    // ---- FollowUpRepository ----

    @Test
    fun followUps_loadsAndComputesStats() = runBlocking {
        ok(
            """{
            "items": [
                {
                    "id": "fu1",
                    "messageId": "msg1",
                    "userId": "w1",
                    "displayName": "Worker 1",
                    "termJa": "金型交換",
                    "status": "clarified",
                    "note": "What is die change?",
                    "createdAt": "2026-11-24T11:00:00Z",
                    "resolvedAt": null
                },
                {
                    "id": "fu2",
                    "messageId": "msg2",
                    "userId": "w2",
                    "displayName": "Worker 2",
                    "termJa": "切粉",
                    "status": "open",
                    "note": "What are chips?",
                    "createdAt": "2026-11-24T10:00:00Z",
                    "resolvedAt": null
                }
            ],
            "nextCursor": null
        }""",
        )

        val repo = RealFollowUpRepository({ api }, now = { fixedNow })
        val result = repo.getFollowUps()
        assertTrue(result.isSuccess)
        val data = (result as AppResult.Success).value
        assertEquals(2, data.items.size)
        assertEquals(FollowUpStatus.CLARIFIED, data.items[0].status)
        assertEquals(FollowUpStatus.OPEN, data.items[1].status)
        assertEquals(60L, data.items[0].ageMinutes)
        assertEquals(2, data.stats.flagged)
        assertEquals(1, data.stats.explained)
        assertEquals(0, data.stats.understood)
        assertEquals(-1, data.stats.records)
    }

    @Test
    fun followUps_serverErrorReturnsFailure() = runBlocking {
        server.enqueue(MockResponse.Builder().code(500).build())
        val repo = RealFollowUpRepository({ api }, now = { fixedNow })
        val result = repo.getFollowUps()
        assertTrue(result.isFailure)
        assertEquals(AppError.Server, (result as AppResult.Failure).error)
    }
}
