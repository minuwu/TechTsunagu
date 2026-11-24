package com.techtsunagu.wakatta.feature.support

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.InterviewMethod
import com.techtsunagu.wakatta.core.model.InterviewStatus
import com.techtsunagu.wakatta.feature.support.domain.InterviewCreateRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeRepositoryTest {

    private fun request(
        note: String = "ok",
        date: String = "2026-11-20",
        method: InterviewMethod = InterviewMethod.FACE,
        consent: Boolean = false,
    ) = InterviewCreateRequest("u_siti", date, method, consent, note)

    @Test
    fun seededDueStatesAreOkSoonOverdue() = runTest {
        val repo = newInterviewRepo()
        assertEquals(InterviewStatus.OK, (repo.getDueInfo("u_rahim") as AppResult.Success).value.status)
        assertEquals(InterviewStatus.SOON, (repo.getDueInfo("u_nguyen") as AppResult.Success).value.status)
        assertEquals(InterviewStatus.OVERDUE, (repo.getDueInfo("u_siti") as AppResult.Success).value.status)
    }

    @Test
    fun unknownWorkerIsNotFound() = runTest {
        assertEquals(AppResult.Failure(AppError.NotFound), newInterviewRepo().getDueInfo("nobody"))
    }

    @Test
    fun recordRejectsBlankNoteFutureDateAndOnlineWithoutConsent() = runTest {
        val repo = newInterviewRepo()
        assertEquals(AppResult.Failure(AppError.Validation(listOf("note"))), repo.recordInterview(request(note = " ")))
        assertEquals(AppResult.Failure(AppError.Validation(listOf("date"))), repo.recordInterview(request(date = "2999-01-01")))
        assertEquals(
            AppResult.Failure(AppError.Validation(listOf("consent"))),
            repo.recordInterview(request(method = InterviewMethod.ONLINE)),
        )
    }

    @Test
    fun recordClearsOverdueQueuesOutboxAndEscapesJson() = runTest {
        val outbox = RecordingOutbox()
        val repo = newInterviewRepo(outbox)
        assertTrue(repo.recordInterview(request(note = "say \"hi\"\nbye")) is AppResult.Success)

        assertEquals(InterviewStatus.OK, (repo.getDueInfo("u_siti") as AppResult.Success).value.status)
        assertTrue(outbox.payloads.single().contains("say \\\"hi\\\"\\nbye"))
        assertEquals("2027-02-20", repo.getDueInterviews().first().first { it.workerId == "u_siti" }.dueDate)
    }

    @Test
    fun csvStartsWithUtf8BomAndHasThreeSections() = runTest {
        val file = (newLogRepo().exportSupportLogCsv(null, "all") as AppResult.Success).value
        val bytes = file.readBytes()
        file.delete()
        assertEquals(listOf(0xEF, 0xBB, 0xBF), bytes.take(3).map { it.toInt() and 0xFF })
        val text = bytes.decodeToString()
        assertTrue(text.contains("[定期面談") && text.contains("[先輩との面談") && text.contains("[お知らせ確認"))
    }

    @Test
    fun logCountsFollowWorkerAndMonthFilters() = runTest {
        val repo = newLogRepo()
        val all = (repo.getSupportLog(null, "all") as AppResult.Success).value.summary
        assertEquals(3, all.interviewCount)
        assertEquals(4, all.meetupCount)
        val rahimNov = (repo.getSupportLog("u_rahim", "2026-11") as AppResult.Success).value.summary
        assertEquals(0, rahimNov.interviewCount)
        assertEquals(1, rahimNov.meetupCount)
    }
}
