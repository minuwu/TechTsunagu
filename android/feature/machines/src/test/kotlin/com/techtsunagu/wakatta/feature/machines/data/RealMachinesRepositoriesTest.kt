package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.PanelState
import com.techtsunagu.wakatta.core.model.Shift
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.machines.TestSessionStore
import com.techtsunagu.wakatta.feature.machines.TestSignOffRepository
import com.techtsunagu.wakatta.feature.machines.TestWorkerRepository
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealMachinesRepositoriesTest {
    private val server = MockWebServer().also { it.start() }
    private val session = TestSessionStore().apply { setForemanSession() }
    private val api: MachinesApi = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, session, debug = false))
        .create(MachinesApi::class.java)
    private val panels = RealPanelRepository({ api }, session, TestWorkerRepository(), TestSignOffRepository())

    @After
    fun tearDown() = server.close()

    private fun ok(data: String, etag: String? = null, code: Int = 200) = server.enqueue(
        MockResponse.Builder().code(code).apply {
            if (code != 304) body("""{"success":true,"data":$data,"message":"ok"}""")
            etag?.let { addHeader("ETag", it) }
        }.build(),
    )

    private val panelJson = """{"state":"ready","worker":{"id":"12","displayName":"Rahim"},"reason":null,"versionNo":3,"override":null}"""
    private val machineJson = """{"id":"2","code":"press-2","name":{"ja":"2号"},"currentVersionId":"41","activeWorkerId":null,"current":null}"""

    @Test
    fun pollSendsTheEtagAndAn304ReportsNotModified() = runBlocking {
        ok(panelJson, etag = "\"abc\"")
        ok(machineJson)
        val first = (panels.getPanelState("2") as AppResult.Success).value
        ok("null", etag = "\"abc\"", code = 304)
        val poll = (panels.getPanelState("2", first.etag) as AppResult.Success).value
        assertEquals(PanelState.READY, first.panel.state)
        assertEquals("\"abc\"", first.etag)
        assertFalse(first.isNotModified)
        assertTrue(poll.isNotModified)
        assertEquals(PanelState.READY, poll.panel.state)
        server.takeRequest()
        server.takeRequest()
        assertEquals("\"abc\"", server.takeRequest().headers["If-None-Match"])
    }

    @Test
    fun overrideSendsAnExplicitNullWorkerAndTheKeyAndItsIdIsKeptForRevoke() = runBlocking {
        ok("""{"id":"6","machineId":"2","workerId":null,"by":"Y","reason":"line stopped","startsAt":"a","endsAt":"b","revokedAt":null}""")
        val started = (panels.requestOverride("2", null, 60, "line stopped", "key-9") as AppResult.Success).value
        assertEquals("6", started.id)
        val request = server.takeRequest()
        assertEquals("""{"workerId":null,"minutes":60,"reason":"line stopped"}""", request.body?.utf8())
        assertEquals("key-9", request.headers["Idempotency-Key"])
        // The Panel answer has no override id: it comes back from memory.
        ok("""{"state":"override","worker":null,"reason":null,"versionNo":3,"override":{"by":"Y","until":"b","reason":"line stopped"}}""")
        ok(machineJson)
        val panel = (panels.getPanelState("2") as AppResult.Success).value.panel
        assertEquals("6", panel.override?.id)
    }

    @Test
    fun workerNoteHasNoAnswerKeyAndForemanNoteHasIt() = runBlocking {
        val notes = RealNoteRepository({ api }, session)
        val question = """"id":"q1","termId":null,"prompt":{"ja":"Q"},"options":[{"ja":"a"},{"ja":"b"},{"ja":"c"}]"""
        ok(
            """{"items":[{"id":"41","machineId":"2","versionNo":3,"status":"approved","recordedBy":"Y","approvedBy":"Y",""" +
                """"steps":[{"text":{"ja":"s"},"icon":null}],"questions":[{$question,"correctIndex":2}],""" +
                """"needsEdit":false,"createdAt":"2026-11-01T00:00:00Z","approvedAt":null}],"nextCursor":null}""",
        )
        assertEquals(2, (notes.getNoteVersion("2") as AppResult.Success).value.questions[0].correctIndex)
        session.setWorkerSession()
        ok("""{"id":"2","code":"p","name":{"ja":"2号"},"current":{"id":"41","versionNo":3,"steps":[],"questions":[{$question}]}}""")
        val worker = (notes.getNoteVersion("2") as AppResult.Success).value
        assertNull(worker.questions[0].correctIndex)
        assertEquals("41", worker.id)
    }

    @Test
    fun clearingASlotSendsNullAndAnswersNull() = runBlocking {
        val shifts = RealShiftAssignmentRepository({ api }, TestWorkerRepository())
        ok("null")
        val result = shifts.assign("2", Shift.DAY, null, null)
        assertEquals(AppResult.Success(null), result)
        assertEquals(
            """{"machineId":"2","shift":"day","date":null,"workerId":null}""",
            server.takeRequest().body?.utf8(),
        )
    }
}
