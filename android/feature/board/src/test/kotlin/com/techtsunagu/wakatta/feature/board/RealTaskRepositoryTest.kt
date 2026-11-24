package com.techtsunagu.wakatta.feature.board

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.board.data.RealTaskRepository
import com.techtsunagu.wakatta.feature.board.data.TaskApi
import com.techtsunagu.wakatta.feature.board.data.dueWire
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealTaskRepositoryTest {
    private val server = MockWebServer().also { it.start() }
    private val api: TaskApi = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, TestSessionStore(), debug = false))
        .create(TaskApi::class.java)
    private val repo = RealTaskRepository({ api }, ZoneId.of("UTC"))

    @After
    fun tearDown() = server.close()

    private fun ok(data: String, code: Int = 200) = server.enqueue(
        MockResponse.Builder().code(code).body("""{"success":true,"data":$data,"message":"ok"}""").build(),
    )

    private fun task(id: String = "3", column: String = "todo", version: Int = 1) =
        """{"id":"$id","title":{"ja":"t"},"column":"$column","assigneeIds":["12"],"dueAt":"2026-11-24T09:30:00Z",""" +
            """"machineId":"2","fromMessageId":null,"version":$version,"updatedAt":"2026-11-24T00:00:00Z"}"""

    @Test
    fun listFollowsTheCursorAndMapsColumnAndDue() = runBlocking {
        ok("""{"items":[${task("1")}],"nextCursor":"c2"}""")
        ok("""{"items":[${task("2", "doing")}],"nextCursor":null}""")
        val tasks = (repo.getTasks() as AppResult.Success).value
        assertEquals(listOf("1", "2"), tasks.map { it.id })
        assertEquals(TaskColumn.DOING, tasks[1].column)
        assertEquals("11/24 09:30", tasks[0].dueAt)
        server.takeRequest()
        assertTrue(server.takeRequest().url.toString().contains("cursor=c2"))
    }

    @Test
    fun moveSendsIfMatchAndOnlyTheColumnAndAStaleVersionIsPreconditionFailed() = runBlocking {
        ok(task("3", "done", 5))
        val moved = (repo.moveTask("3", TaskColumn.DONE, null, "stale text", 4) as AppResult.Success).value
        assertEquals(5, moved.version)
        val request = server.takeRequest()
        assertEquals("4", request.headers["If-Match"])
        assertEquals("""{"column":"done"}""", request.body?.utf8())

        server.enqueue(
            MockResponse.Builder().code(412)
                .body("""{"success":false,"error":{"code":"VERSION_CONFLICT","message":"moved"}}""").build(),
        )
        assertEquals(AppResult.Failure(AppError.PreconditionFailed), repo.moveTask("3", TaskColumn.TODO, null, null, 4))
    }

    @Test
    fun aRetriedCreateReusesTheKeyAndANewOneGetsAnother() = runBlocking {
        server.enqueue(MockResponse.Builder().code(500).build())
        ok(task("9"), code = 201)
        ok(task("10"), code = 201)
        val title = mapOf("ja" to "t")
        assertTrue(repo.createTask(title, listOf("12"), null).let { it is AppResult.Failure })
        assertTrue(repo.createTask(title, listOf("12"), null).isSuccess)
        assertTrue(repo.createTask(title, listOf("12"), null).isSuccess)
        val first = server.takeRequest()
        val retry = server.takeRequest()
        val again = server.takeRequest()
        assertEquals(first.headers["Idempotency-Key"], retry.headers["Idempotency-Key"])
        assertTrue(retry.headers["Idempotency-Key"] != again.headers["Idempotency-Key"])
        assertEquals("""{"title":{"ja":"t"},"assigneeIds":["12"]}""", first.body?.utf8())
    }

    @Test
    fun dueLabelsBecomeEvening18hOfThatDay() {
        val tuesday = ZonedDateTime.of(2026, 11, 24, 9, 0, 0, 0, ZoneId.of("UTC"))
        assertEquals("2026-11-24T18:00:00Z", dueWire("Today", tuesday))
        assertEquals("2026-11-25T18:00:00Z", dueWire("Tomorrow", tuesday))
        assertEquals("2026-11-27T18:00:00Z", dueWire("Fri", tuesday))
        assertEquals("2026-12-01T10:00:00Z", dueWire("2026-12-01T10:00:00Z", tuesday))
        assertNull(dueWire("whenever", tuesday))
        assertNull(dueWire(null, tuesday))
    }
}
