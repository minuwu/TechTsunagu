package com.techtsunagu.wakatta.feature.notices

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.notices.data.FakeNoticeRepository
import com.techtsunagu.wakatta.feature.notices.data.NoticesApi
import com.techtsunagu.wakatta.feature.notices.data.RealNoticeRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealNoticeRepositoryTest {
    private class Store(user: com.techtsunagu.wakatta.core.model.User) : SessionStore {
        private val s = MutableStateFlow<Session?>(Session("tok", user = user))
        override val session: StateFlow<Session?> = s
        override suspend fun save(session: Session) { s.value = session }
        override suspend fun clear() { s.value = null }
        override fun getCachedToken(): String? = s.value?.token
    }

    private val server = MockWebServer().also { it.start() }
    private val workerStore = Store(DemoSeed.demoWorker)
    private val api: NoticesApi = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, workerStore, debug = false))
        .create(NoticesApi::class.java)
    private val repo = RealNoticeRepository({ api }, workerStore, FakeNoticeRepository(0L)) { LocalDate.of(2026, 11, 24) }

    @After
    fun tearDown() = server.close()

    private fun ok(data: String) = server.enqueue(
        MockResponse.Builder().code(200).body("""{"success":true,"data":$data,"message":"ok"}""").build(),
    )

    private fun notice(passed: String) = """{"id":"7","posterId":"1","category":"garbage","sourceKind":"voice",
        "sourceJa":"燃えるごみ","photo":null,"title":{"ja":"ごみ","en":"Garbage"},"steps":[{"ja":"出す","en":"Put out"}],
        "events":[{"id":"11","title":{"ja":"ごみ"},"startsAt":"2026-11-26T08:00:00+09:00","rrule":null,"confirmed":true,
        "nextDates":["2026-11-26T08:00:00+09:00"]}],"status":"published","needsEdit":false,"createdAt":"2026-11-10T01:00:00Z",
        "publishedAt":"2026-11-10T02:00:00Z","question":{"id":"q1","termId":null,"prompt":{"ja":"?"},
        "options":[{"ja":"a"},{"ja":"b"},{"ja":"c"}]},"opened":true,"passed":$passed}"""

    @Test
    fun listFollowsTheCursorAndMapsTheWorkerShape() = runBlocking {
        ok("""{"items":[${notice("true")}],"nextCursor":"c2"}""")
        ok("""{"items":[${notice("null")}],"nextCursor":null}""")
        val list = (repo.getNoticesForWorker() as AppResult.Success).value
        assertEquals(2, list.size)
        val first = list[0]
        assertEquals(WakattaStatus.GREEN, first.wakattaStatus)
        assertEquals(WakattaStatus.WAIT, list[1].wakattaStatus)
        assertEquals("2026-11-10", first.publishedAt)
        assertEquals("2026-11-26", first.events[0].date)
        assertEquals("8:00", first.events[0].time)
        assertEquals("7", first.events[0].noticeId)
        server.takeRequest()
        assertEquals("c2", server.takeRequest().url.queryParameter("cursor"))
    }

    @Test
    fun eventOccurrencesOfOneEventGetUniqueIdsAndAskFor62DaysAtMost() = runBlocking {
        ok(
            """[{"eventId":"11","noticeId":"7","title":{"ja":"ごみ"},"at":"2026-11-26T08:00:00+09:00"},
               {"eventId":"11","noticeId":"7","title":{"ja":"ごみ"},"at":"2026-11-30T08:00:00+09:00"}]""",
        )
        val events = (repo.getUpcomingEvents() as AppResult.Success).value
        assertEquals(2, events.map { it.id }.toSet().size)
        val url = server.takeRequest().url
        assertEquals("2026-11-24", url.queryParameter("from"))
        assertEquals("2027-01-24", url.queryParameter("to"))
        assertTrue(repo.toggleReminder("11@2026-11-26T08:00:00+09:00").let { (it as AppResult.Success).value })
    }

    @Test
    fun checkIsReadBackAndNotSureStaysOnThePhone() = runBlocking {
        ok(notice("false"))
        val wrong = (repo.submitNoticeCheck("7", 1) as AppResult.Success).value
        assertEquals(WakattaStatus.RED, wrong.status)
        assertNull(wrong.correctAnswerText)
        val unsure = (repo.submitNoticeCheck("7", -1) as AppResult.Success).value
        assertEquals(WakattaStatus.ORANGE, unsure.status)
        assertEquals(1, server.requestCount)

        ok(notice("null"))
        assertTrue((repo.submitNoticeCheck("7", 1) as AppResult.Failure).error is AppError.Unknown)
    }

    @Test
    fun foremanKeepsTheFakeAndMakesNoCall() = runBlocking {
        val foreman = RealNoticeRepository({ api }, Store(DemoSeed.demoForeman), FakeNoticeRepository(0L))
        assertTrue(foreman.getNoticesForWorker() is AppResult.Success)
        assertEquals(0, server.requestCount)
    }
}
