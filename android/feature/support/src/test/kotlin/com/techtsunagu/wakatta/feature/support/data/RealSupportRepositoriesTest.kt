package com.techtsunagu.wakatta.feature.support.data

import android.content.Context
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.InterviewMethod
import com.techtsunagu.wakatta.core.model.InterviewStatus
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.support.domain.InterviewCreateRequest
import com.techtsunagu.wakatta.core.model.WakattaStatus
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealSupportRepositoriesTest {

    private class TestStore(user: User) : SessionStore {
        private val s = MutableStateFlow<Session?>(Session("test_token", user = user))
        override val session: StateFlow<Session?> = s
        override suspend fun save(session: Session) { s.value = session }
        override suspend fun clear() { s.value = null }
        override fun getCachedToken(): String? = s.value?.token
    }

    private class TestWorkerRepo : WorkerRepository {
        override suspend fun getWorkers(): AppResult<List<WorkerInfo>> =
            AppResult.Success(listOf(WorkerInfo("12", "Rahim Uddin", "bn")))
    }

    private val server = MockWebServer().also { it.start() }
    private val store = TestStore(DemoSeed.demoForeman)
    private val api: SupportApi = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, store, debug = false))
        .create(SupportApi::class.java)

    private val tempDir = File(System.getProperty("java.io.tmpdir") ?: ".", "support_test_${System.currentTimeMillis()}").also { it.mkdirs() }

    private val mockContext = object : ContextWrapper(tempDir) {}

    private val logRepo = RealSupportLogRepository({ api }, mockContext.toContext(), TestWorkerRepo())
    private val interviewRepo = RealInterviewRepository({ api }, store, TestWorkerRepo())

    @After
    fun tearDown() {
        server.close()
        tempDir.deleteRecursively()
    }

    private fun ok(data: String, code: Int = 200) = server.enqueue(
        MockResponse.Builder().code(code).body("""{"success":true,"data":$data,"message":"ok"}""").build(),
    )

    @Test
    fun exportSupportLogCsvDownloadsAndSavesFile() = runBlocking {
        val csvContent = "\uFEFF特定技能1号 生活支援実施記録\n期間,2026-11\n"
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .setHeader("Content-Type", "text/csv; charset=utf-8")
                .body(csvContent)
                .build(),
        )

        val res = logRepo.exportSupportLogCsv("all", "2026-11")
        assertTrue(res is AppResult.Success)
        val file = (res as AppResult.Success).value
        assertTrue(file.exists())
        assertEquals(csvContent, file.readText(Charsets.UTF_8))

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertTrue(req.url.encodedPath.contains("support-log.csv"))
        assertEquals("2026-11-01", req.url.queryParameter("from"))
        assertEquals("2026-11-30", req.url.queryParameter("to"))
    }

    private fun csv(vararg rows: String) = server.enqueue(
        MockResponse.Builder().body(
            "\uFEFF支援記録 2026-11-01〜2026-11-30\r\nこの記録に含まれないもの\r\n\r\n" +
                "支援項目,日時,氏名,内容,対応・相手,記録元\r\n" + rows.joinToString("") { it + "\r\n" },
        ).build(),
    )

    @Test
    fun supportLogIsBuiltFromTheLiveRoutes() = runBlocking {
        ok("""[{"workerId":"12","displayName":"Rahim Uddin","lastHeldOn":null,"dueOn":"2026-11-20","status":"overdue"}]""")
        csv(
            "8 日本人との交流促進,2026-11-12,Rahim Uddin,\"お茶・食事\u3000駅前, \"\"カフェ\"\"\n二人\",佐藤,Senpai",
            "10 定期的な面談,2026-11-10,Rahim Uddin,面談（対面）,山口,Foreman",
            "4 生活オリエンテーション,2026-11-09 10:00,Rahim Uddin,ごみ,理解確認：合格,Notice",
            "4 生活オリエンテーション,2026-11-08 10:00,Rahim Uddin,訓練,理解確認：不合格,Notice",
            "8 日本人との交流促進,2026-11-12,Other Person,その他,佐藤,Worker",
        )
        val data = (logRepo.getSupportLog("12", "2026-11") as AppResult.Success).value
        assertEquals(listOf("12"), data.dueInterviews.map { it.workerId })
        assertEquals(1, data.summary.interviewCount)
        assertEquals(1, data.summary.noticeOkCount)
        assertEquals(2, data.summary.noticeTotalCount)
        val meetup = data.meetups.single()
        assertEquals("coffee", meetup.kind)
        assertEquals("駅前, \"カフェ\"\n二人", meetup.noteJa)
        assertEquals("佐藤", meetup.senpaiName)
        assertEquals(WakattaStatus.RED, data.notices.first { it.titleJa == "訓練" }.workerStatuses["12"])
    }

    @Test
    fun interviewPrepCountsFactsSinceTheLastInterview() = runBlocking {
        ok("""[{"id":"1","workerId":"12","heldOn":"2026-10-20","method":"face","onlineConsentAt":null,"noteJa":"","recordedBy":"9","createdAt":"2026-10-20T10:00:00Z"}]""")
        csv(
            "7 相談・苦情への対応,2026-11-12 09:00,Rahim Uddin,「バリ取り」の確認（今日はバリ取り）,山口,Chat",
            "7 相談・苦情への対応,2026-11-11 09:00,Rahim Uddin,'=送金,佐藤：未回答,Senpai",
        )
        val prep = (interviewRepo.getInterviewPrep("12") as AppResult.Success).value
        assertEquals("2026-10-20", prep.fromDate)
        assertEquals(listOf("バリ取り"), prep.confusionTerms)
        assertEquals(listOf("=送金"), prep.forwardedAsks)
        server.takeRequest()
        assertEquals("2026-10-20", server.takeRequest().url.queryParameter("from"))
    }

    @Test
    fun aFailedCsvIsAnErrorNotEmptyData() = runBlocking {
        server.enqueue(MockResponse.Builder().code(403).build())
        assertEquals(AppResult.Failure(AppError.Forbidden), logRepo.exportSupportLogCsv("all", "2026-11"))
    }

    @Test
    fun getDueInfoMapsServerResponse() = runBlocking {
        ok("""[{"workerId":"12","displayName":"Rahim Uddin","lastHeldOn":"2026-08-20","dueOn":"2026-11-20","status":"soon"}]""")

        val res = interviewRepo.getDueInfo("12")
        assertTrue(res is AppResult.Success)
        val dto = (res as AppResult.Success).value
        assertEquals("12", dto.workerId)
        assertEquals("Rahim Uddin", dto.workerName)
        assertEquals("2026-11-20", dto.dueDate)
        assertEquals(InterviewStatus.SOON, dto.status)
    }

    @Test
    fun getPastInterviewsMapsServerResponse() = runBlocking {
        ok("""[{"id":"iv1","workerId":"12","heldOn":"2026-10-20","method":"face","onlineConsentAt":null,"noteJa":"順調","recordedBy":"山口","createdAt":"2026-10-20T10:00:00Z"}]""")

        val res = interviewRepo.getPastInterviews("12")
        assertTrue(res is AppResult.Success)
        val list = (res as AppResult.Success).value
        assertEquals(1, list.size)
        assertEquals("iv1", list[0].id)
        assertEquals(InterviewMethod.FACE, list[0].method)
        assertEquals(false, list[0].consent)
        assertEquals("順調", list[0].noteJa)
    }

    @Test
    fun recordInterviewSendsValidPayloadAndReusesIdempotencyKey() = runBlocking {
        ok("""{"id":"iv_new","workerId":"12","heldOn":"2026-11-20","method":"online","onlineConsentAt":"2026-11-20T00:00:00Z","noteJa":"相談あり","recordedBy":"山口","createdAt":"2026-11-20T10:00:00Z"}""", code = 201)

        val req = InterviewCreateRequest(
            workerId = "12",
            date = "2026-11-20",
            method = InterviewMethod.ONLINE,
            consent = true,
            note = "相談あり",
        )

        val res = interviewRepo.recordInterview(req)
        assertTrue(res is AppResult.Success)
        val out = (res as AppResult.Success).value
        assertEquals("iv_new", out.id)
        assertEquals(InterviewMethod.ONLINE, out.method)
        assertEquals(true, out.consent)

        val serverReq = server.takeRequest()
        assertEquals("POST", serverReq.method)
        assertNotNull(serverReq.headers["Idempotency-Key"])
        val bodyText = serverReq.body?.utf8().orEmpty()
        assertTrue(bodyText.contains("\"heldOn\":\"2026-11-20\""))
        assertTrue(bodyText.contains("\"method\":\"online\""))
    }

    @Test
    fun recordInterviewValidatesInputsBeforeCallingServer() = runBlocking {
        val emptyNote = InterviewCreateRequest("12", "2026-11-20", InterviewMethod.FACE, false, "   ")
        val res1 = interviewRepo.recordInterview(emptyNote)
        assertTrue(res1 is AppResult.Failure)
        assertTrue((res1 as AppResult.Failure).error is AppError.Validation)

        val onlineNoConsent = InterviewCreateRequest("12", "2026-11-20", InterviewMethod.ONLINE, false, "note")
        val res2 = interviewRepo.recordInterview(onlineNoConsent)
        assertTrue(res2 is AppResult.Failure)
        assertTrue((res2 as AppResult.Failure).error is AppError.Validation)

        assertEquals(0, server.requestCount)
    }
}

private open class ContextWrapper(val dir: File) {
    fun toContext(): Context = object : android.content.ContextWrapper(null) {
        override fun getCacheDir(): File = dir
    }
}
