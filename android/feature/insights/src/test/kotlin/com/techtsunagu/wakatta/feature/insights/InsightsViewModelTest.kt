package com.techtsunagu.wakatta.feature.insights

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class InsightsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    // ---- Analytics ----

    @Test
    fun analytics_loadsAndSelectsThisWeek() {
        val vm = AnalyticsViewModel(StubAnalytics())
        val s = vm.state.value
        assertFalse(s.isLoading)
        assertNull(s.error)
        assertEquals(3, s.selectedPoint)
        assertFalse(s.isEmpty)
    }

    @Test
    fun analytics_failureShowsTypedErrorAndDropsOldData() {
        val repo = StubAnalytics()
        val vm = AnalyticsViewModel(repo)
        repo.result = AppResult.Failure(AppError.Offline)
        vm.onEvent(AnalyticsEvent.Retry)
        assertEquals(AppError.Offline, vm.state.value.error)
        assertNull(vm.state.value.data)
        repo.result = AppResult.Success(sampleAnalytics())
        vm.onEvent(AnalyticsEvent.Retry)
        assertNull(vm.state.value.error)
        assertNotNull(vm.state.value.data)
        assertEquals(3, repo.loads)
    }

    @Test
    fun analytics_selectPointAndCell() {
        val vm = AnalyticsViewModel(StubAnalytics())
        vm.onEvent(AnalyticsEvent.SelectPoint(1))
        vm.onEvent(AnalyticsEvent.SelectCell("玉掛け", "u_rahim"))
        assertEquals(1, vm.state.value.selectedPoint)
        assertEquals("玉掛け", vm.state.value.selectedTerm)
        assertEquals("u_rahim", vm.state.value.selectedWorkerId)
    }

    @Test
    fun analytics_pinMarksTermInGlossary() {
        val repo = StubAnalytics()
        val vm = AnalyticsViewModel(repo)
        vm.onEvent(AnalyticsEvent.PinTerm("玉掛け"))
        assertEquals(listOf("玉掛け"), repo.pinned)
        assertNull(vm.state.value.pinningTerm)
        assertTrue(vm.state.value.data!!.terms.first { it.term == "玉掛け" }.inGlossary)
    }

    @Test
    fun analytics_pinFailureKeepsTermOpenAndShowsError() {
        val repo = StubAnalytics(pinResult = AppResult.Failure(AppError.Server))
        val vm = AnalyticsViewModel(repo)
        vm.onEvent(AnalyticsEvent.PinTerm("玉掛け"))
        assertEquals(AppError.Server, vm.state.value.pinError)
        assertFalse(vm.state.value.data!!.terms.first { it.term == "玉掛け" }.inGlossary)
        assertNull(vm.state.value.pinningTerm)
    }

    // ---- Follow-ups ----

    @Test
    fun followUps_loadsAndFiltersByStatus() {
        val vm = FollowUpsViewModel(StubFollowUps())
        assertEquals(2, vm.state.value.data!!.items.size)
        assertEquals(FollowUpStatus.OPEN, vm.state.value.filter)
        vm.onEvent(FollowUpsEvent.SelectFilter(FollowUpStatus.CLARIFIED))
        assertEquals(FollowUpStatus.CLARIFIED, vm.state.value.filter)
    }

    @Test
    fun followUps_failureThenRetry() {
        val repo = StubFollowUps(AppResult.Failure(AppError.Forbidden))
        val vm = FollowUpsViewModel(repo)
        assertEquals(AppError.Forbidden, vm.state.value.error)
        repo.result = AppResult.Success(sampleFollowUps())
        vm.onEvent(FollowUpsEvent.Retry)
        assertNull(vm.state.value.error)
        assertNotNull(vm.state.value.data)
    }

    // ---- Skills ----

    @Test
    fun skills_loadsWithSessionLanguageAndSelectsCell() {
        val vm = SkillsViewModel(StubSkills(), TestSessionStore(DemoSeed.demoWorker))
        assertEquals("bn", vm.state.value.language)
        vm.onEvent(SkillsEvent.SelectCell("u_rahim", "mc1"))
        assertEquals("mc1", vm.state.value.selectedMachineId)
        vm.onEvent(SkillsEvent.DismissCell)
        assertNull(vm.state.value.selectedWorkerId)
    }

    @Test
    fun skills_failureShowsTypedError() {
        val vm = SkillsViewModel(StubSkills(AppResult.Failure(AppError.Timeout)), TestSessionStore())
        assertEquals(AppError.Timeout, vm.state.value.error)
        assertNull(vm.state.value.matrix)
    }

    // ---- Learning ----

    @Test
    fun learning_foremanOpensRequestedWorker() {
        val repo = StubLearning()
        val vm = LearningViewModel(repo, TestSessionStore(DemoSeed.demoForeman))
        vm.onEvent(LearningEvent.Init("u_siti"))
        assertEquals(listOf("u_siti"), repo.asked)
        assertTrue(vm.state.value.isForeman)
    }

    @Test
    fun learning_workerAlwaysGetsOwnHistory() {
        val repo = StubLearning()
        val vm = LearningViewModel(repo, TestSessionStore(DemoSeed.demoWorker))
        vm.onEvent(LearningEvent.Init("u_siti"))
        assertEquals(listOf("u_rahim"), repo.asked)
        assertFalse(vm.state.value.isForeman)
    }

    @Test
    fun learning_noSessionIsTypedError() {
        val vm = LearningViewModel(StubLearning(), TestSessionStore(null))
        vm.onEvent(LearningEvent.Init(null))
        assertEquals(AppError.SessionExpired, vm.state.value.error)
    }

    @Test
    fun learning_failureThenRetryReloadsSameWorker() {
        val repo = StubLearning(AppError.NotFound)
        val vm = LearningViewModel(repo, TestSessionStore(DemoSeed.demoForeman))
        vm.onEvent(LearningEvent.Init("u_siti"))
        assertEquals(AppError.NotFound, vm.state.value.error)
        repo.failure = null
        vm.onEvent(LearningEvent.Retry)
        assertNull(vm.state.value.error)
        assertEquals(listOf("u_siti", "u_siti"), repo.asked)
    }

    @Test
    fun learning_initTwiceWithSameWorkerLoadsOnce() {
        val repo = StubLearning()
        val vm = LearningViewModel(repo, TestSessionStore(DemoSeed.demoForeman))
        vm.onEvent(LearningEvent.Init("u_siti"))
        vm.onEvent(LearningEvent.Init("u_siti"))
        assertEquals(1, repo.asked.size)
    }
}
