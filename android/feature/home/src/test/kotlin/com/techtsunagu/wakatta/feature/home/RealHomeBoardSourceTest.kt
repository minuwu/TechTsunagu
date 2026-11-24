package com.techtsunagu.wakatta.feature.home

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.home.data.HomeApi
import com.techtsunagu.wakatta.feature.home.data.HomeFollowUp
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
    fun pendingChecksAreSentUnansweredMessagesWithAQuestion() = runBlocking {
        data("""[{"id":"5","titleJa":"班","kind":"group","memberIds":[],"unread":1,"lastMessageAt":"x"},{"id":"6","titleJa":"空","kind":"group","memberIds":[],"unread":0,"lastMessageAt":null}]""")
        fun m(id: String, sent: String?, questions: String, status: String?) =
            """{"id":"$id","text":{"ja":"t$id"},"sentAt":${sent?.let { "\"$it\"" }},"questions":$questions,"myStatus":${status?.let { "\"$it\"" }}}"""
        val q = """[{"id":"q","prompt":{},"options":[]}]"""
        data("""{"items":[${m("1", "2026-11-24T01:00:00Z", q, null)},${m("2", "2026-11-24T01:00:00Z", q, "green")},${m("3", "2026-11-24T01:00:00Z", "[]", null)},${m("4", null, q, null)}]}""")
        assertEquals(listOf("1" to "5"), source.getPendingChecks().ok().map { it.id to it.threadId })
        assertEquals(2, server.requestCount)
    }

    @Test
    fun teamCountsValidAndExpiringMachines() = runBlocking {
        data("""{"machines":[],"rows":[{"workerId":"3","displayName":"A","cells":[{"machineId":"1","status":"valid","passedAt":null},{"machineId":"2","status":"expiring","passedAt":null},{"machineId":"3","status":"expired","passedAt":null}]}]}""")
        assertEquals(listOf(HomeTeamMember("3", "A", 2, 3)), source.getTeam().ok())
    }
}
