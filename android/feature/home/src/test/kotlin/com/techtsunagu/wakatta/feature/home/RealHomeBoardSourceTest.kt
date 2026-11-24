package com.techtsunagu.wakatta.feature.home

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.home.data.HomeApi
import com.techtsunagu.wakatta.feature.home.data.HomeFollowUp
import com.techtsunagu.wakatta.feature.home.data.HomePendingCheck
import com.techtsunagu.wakatta.feature.home.data.HomeTeamMember
import com.techtsunagu.wakatta.feature.home.data.RealHomeBoardSource
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class RealHomeBoardSourceTest {
    private val server = MockWebServer().also { it.start() }
    private val session = TestSessionStore(Session("t", user = DemoSeed.demoForeman))
    private val api = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, session, debug = false))
        .create(HomeApi::class.java)
    private val source = RealHomeBoardSource { api }

    @After
    fun tearDown() = server.close()

    private fun data(json: String) = server.enqueue(
        MockResponse.Builder().body("""{"success":true,"data":$json,"message":"ok"}""").build(),
    )

    private fun <T> AppResult<T>.ok(): T = (this as AppResult.Success).value

    @Test
    fun onlyMachinesWithAFlaggedAssignmentAreLocked() = runBlocking {
        val rahim = """{"shift":"day","workerId":"12","displayName":"Rahim","status":"valid","flag":false}"""
        val nguyen = """{"shift":"day","workerId":"13","displayName":"Nguyen","status":"none","flag":true}"""
        data(
            """{"date":"2026-11-24","openConfusions":2,"machines":[""" +
                """{"id":"1","code":"a","name":{"ja":"A"},"versionNo":1,"openConfusions":0,"assignments":[$rahim]},""" +
                """{"id":"2","code":"b","name":{"ja":"B"},"versionNo":1,"openConfusions":1,"assignments":[$nguyen]}]}""",
        )
        val locked = source.getLockedMachines().ok()
        assertEquals(listOf("2"), locked.map { it.machineId })
        assertEquals(listOf("Nguyen"), locked[0].workerNames)
    }

    @Test
    fun onlyOpenFollowUpsCount() = runBlocking {
        data(
            """{"items":[{"id":"1","messageId":"9","userId":"3","displayName":"A","termJa":"金型","status":"open","note":"","createdAt":"x","resolvedAt":null},""" +
                """{"id":"2","messageId":"9","userId":"3","displayName":"B","termJa":"治具","status":"understood","note":"","createdAt":"x","resolvedAt":"y"}],"nextCursor":null}""",
        )
        assertEquals(listOf(HomeFollowUp("1", "A", "金型")), source.getOpenFollowUps().ok())
    }

    @Test
    fun pendingChecksComeFromOneCallThatMarksNothingRead() = runBlocking {
        data(
            """[{"messageId":"1","threadId":"5","threadTitle":"班","text":{"ja":"t1"},"sentAt":"2026-11-24T02:00:00Z","deadline":null},""" +
                """{"messageId":"2","threadId":"6","threadTitle":"山口 隆","text":{"ja":"t2"},"sentAt":"2026-11-24T01:00:00Z","deadline":"2026-11-24T09:00:00Z"}]""",
        )

        val pending = source.getPendingChecks().ok()

        assertEquals(
            listOf(
                HomePendingCheck("1", "5", "班", mapOf("ja" to "t1"), "2026-11-24T02:00:00Z"),
                HomePendingCheck("2", "6", "山口 隆", mapOf("ja" to "t2"), "2026-11-24T01:00:00Z"),
            ),
            pending,
        )
        // N2: no Thread poll, so opening Home no longer marks every chat as read.
        assertEquals(1, server.requestCount)
        assertEquals("/api/messages/unanswered", server.takeRequest().url.encodedPath)
    }

    @Test
    fun teamCountsValidAndExpiringMachines() = runBlocking {
        data("""{"machines":[],"rows":[{"workerId":"3","displayName":"A","cells":[{"machineId":"1","status":"valid","passedAt":null},{"machineId":"2","status":"expiring","passedAt":null},{"machineId":"3","status":"expired","passedAt":null}]}]}""")
        assertEquals(listOf(HomeTeamMember("3", "A", 2, 3)), source.getTeam().ok())
    }
}
