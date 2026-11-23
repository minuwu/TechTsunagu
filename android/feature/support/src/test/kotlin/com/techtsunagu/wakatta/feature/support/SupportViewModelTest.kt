package com.techtsunagu.wakatta.feature.support

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DueInterview
import com.techtsunagu.wakatta.core.model.InterviewMethod
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.feature.support.domain.InterviewCreateRequest
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewPrepDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewRecordDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewRepository
import com.techtsunagu.wakatta.feature.support.domain.SupportLogData
import com.techtsunagu.wakatta.feature.support.domain.SupportLogRepository
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SupportViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private class OfflineInterviews : InterviewRepository {
        override fun getDueInterviews(): Flow<List<DueInterview>> = emptyFlow()
        override suspend fun getDueInfo(workerId: String): AppResult<InterviewDueDto> = AppResult.Failure(AppError.Offline)
        override suspend fun getInterviewPrep(workerId: String): AppResult<InterviewPrepDto> = AppResult.Failure(AppError.Offline)
        override suspend fun getPastInterviews(workerId: String): AppResult<List<InterviewRecordDto>> =
            AppResult.Failure(AppError.Offline)
        override suspend fun recordInterview(request: InterviewCreateRequest): AppResult<InterviewRecordDto> =
            AppResult.Failure(AppError.Offline)
        override suspend fun getAllWorkers(): AppResult<List<WorkerInfo>> = AppResult.Failure(AppError.Offline)
    }

    private class OfflineLog : SupportLogRepository {
        override suspend fun getSupportLog(workerId: String?, month: String?): AppResult<SupportLogData> =
            AppResult.Failure(AppError.Offline)
        override suspend fun exportSupportLogCsv(workerId: String?, month: String?): AppResult<File> =
            AppResult.Failure(AppError.Offline)
    }

    private fun interviewVm(repo: InterviewRepository = newInterviewRepo()) = InterviewViewModel(repo)

    @Test
    fun interviewInitWithoutWorkerPicksFirstWorker() {
        val vm = interviewVm()
        vm.onEvent(InterviewEvent.Init(null))
        val s = vm.state.value
        assertFalse(s.isLoading)
        assertEquals("u_rahim", s.selectedWorkerId)
        assertEquals(1, s.pastInterviews.size)
        assertEquals(3, s.workers.size)
    }

    @Test
    fun interviewSelectWorkerReloadsAndClearsForm() {
        val vm = interviewVm()
        vm.onEvent(InterviewEvent.Init("u_rahim"))
        vm.onEvent(InterviewEvent.SetNote("draft"))
        vm.onEvent(InterviewEvent.SelectWorker("u_siti"))
        assertEquals("u_siti", vm.state.value.selectedWorkerId)
        assertEquals("", vm.state.value.note)
        assertEquals("Siti Rahma", vm.state.value.dueInfo?.workerName)
    }

    @Test
    fun interviewSubmitValidatesBeforeSaving() {
        val vm = interviewVm()
        vm.onEvent(InterviewEvent.Init("u_siti"))
        vm.onEvent(InterviewEvent.SubmitInterview)
        assertEquals(InterviewIssue.NOTE_REQUIRED, vm.state.value.issue)

        vm.onEvent(InterviewEvent.SetNote("heating"))
        vm.onEvent(InterviewEvent.SetDate("2026-12-01"))
        vm.onEvent(InterviewEvent.SubmitInterview)
        assertEquals(InterviewIssue.FUTURE_DATE, vm.state.value.issue)

        vm.onEvent(InterviewEvent.SetDate("2026-11-20"))
        vm.onEvent(InterviewEvent.SetMethod(InterviewMethod.ONLINE))
        vm.onEvent(InterviewEvent.SubmitInterview)
        assertEquals(InterviewIssue.CONSENT_REQUIRED, vm.state.value.issue)
        assertEquals(1, vm.state.value.pastInterviews.size)
    }

    @Test
    fun interviewSubmitSavesEmitsSavedAndReloads() = runTest {
        val vm = interviewVm()
        vm.onEvent(InterviewEvent.Init("u_siti"))
        vm.onEvent(InterviewEvent.SetNote("heating"))
        vm.onEvent(InterviewEvent.SubmitInterview)

        assertEquals(InterviewEffect.Saved, vm.effects.first())
        val s = vm.state.value
        assertEquals(2, s.pastInterviews.size)
        assertEquals("", s.note)
        assertNull(s.issue)
        assertFalse(s.isSaving)
    }

    @Test
    fun interviewLoadFailureShowsError() {
        val vm = interviewVm(OfflineInterviews())
        vm.onEvent(InterviewEvent.Init("u_rahim"))
        assertEquals(SupportError.OFFLINE, vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun logInitSelectWorkerAndMonthReload() {
        val vm = SupportLogViewModel(newLogRepo(), RecordingShare())
        vm.onEvent(SupportLogEvent.Init(null))
        assertEquals("all", vm.state.value.workerId)
        assertEquals(3, vm.state.value.logData?.summary?.meetupCount)

        vm.onEvent(SupportLogEvent.SelectWorker("u_rahim"))
        vm.onEvent(SupportLogEvent.SelectMonth("2026-10"))
        val s = vm.state.value
        assertEquals("u_rahim", s.workerId)
        assertEquals(1, s.logData?.summary?.meetupCount)
        assertEquals(1, s.logData?.summary?.interviewCount)
        assertTrue(!s.isLoading && s.error == null)
    }

    @Test
    fun logExportSharesCsvAndEmitsExported() = runTest {
        val share = RecordingShare()
        val vm = SupportLogViewModel(newLogRepo(), share)
        vm.onEvent(SupportLogEvent.Init(null))
        vm.onEvent(SupportLogEvent.ExportCsv)

        assertEquals(SupportLogEffect.Exported, vm.effects.first())
        assertEquals("text/csv", share.shared.single().second)
        assertTrue(share.shared.single().first.exists())
        assertFalse(vm.state.value.isExporting)
        share.shared.single().first.delete()
    }

    @Test
    fun logLoadFailureMapsToOffline() {
        val vm = SupportLogViewModel(OfflineLog(), RecordingShare())
        vm.onEvent(SupportLogEvent.Init(null))
        assertEquals(SupportError.OFFLINE, vm.state.value.error)
    }
}
