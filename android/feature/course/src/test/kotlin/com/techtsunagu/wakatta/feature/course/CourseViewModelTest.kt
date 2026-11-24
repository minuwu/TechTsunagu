package com.techtsunagu.wakatta.feature.course

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.testing.MainDispatcherRule
import com.techtsunagu.wakatta.feature.course.domain.Door
import com.techtsunagu.wakatta.feature.course.domain.Progress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CourseViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = StubCourseRepository()
    private val speaker = StubSpeaker()
    private val tone = StubTone()

    private fun vm(session: TestSessionStore = TestSessionStore()) = CourseViewModel(repo, session, speaker, tone)

    private fun CourseViewModel.openCourse() = onEvent(CourseEvent.OpenCourse("c1"))

    @Test
    fun loadsCourseListWithTranslationForNonJapaneseUser() {
        val vm = vm()
        val s = vm.state.value
        assertFalse(s.isLoading)
        assertEquals(1, s.courses.size)
        assertTrue(s.hasTranslation)
        assertEquals("Pointing and calling", s.courses[0].title.tr)
        assertEquals("", vm(TestSessionStore(DemoSeed.demoForeman)).state.value.courses[0].title.tr)
    }

    @Test
    fun listFailureIsTypedAndRetryRecovers() {
        repo.listResult = AppResult.Failure(AppError.Offline)
        val vm = vm()
        assertEquals(AppError.Offline, vm.state.value.error)
        repo.listResult = AppResult.Success(listOf(summary))
        vm.onEvent(CourseEvent.Retry)
        assertNull(vm.state.value.error)
        assertEquals(1, vm.state.value.courses.size)
    }

    @Test
    fun backWalksUpFromDoorToListThenExits() = runTest {
        val vm = vm()
        vm.openCourse()
        vm.onEvent(CourseEvent.OpenDoor(Door.READ))
        vm.onEvent(CourseEvent.Back)
        assertNull(vm.state.value.door)
        assertNotNull(vm.state.value.course)
        vm.onEvent(CourseEvent.Back)
        assertNull(vm.state.value.course)
        vm.onEvent(CourseEvent.Back)
        assertEquals(CourseEffect.Exit, vm.effects.first())
    }

    @Test
    fun finishingReadSavesProgress() {
        val vm = vm()
        vm.openCourse()
        vm.onEvent(CourseEvent.FinishRead)
        assertEquals(listOf(Progress.KEY_READ), repo.saved)
        assertTrue(vm.state.value.progress.read)
    }

    @Test
    fun failedSaveShowsTypedErrorAndDoesNotCountAsDone() {
        repo.saveError = AppError.Server
        val vm = vm()
        vm.openCourse()
        vm.onEvent(CourseEvent.FinishRead)
        assertEquals(AppError.Server, vm.state.value.saveError)
        assertFalse(vm.state.value.progress.read)
        vm.onEvent(CourseEvent.DismissSaveError)
        assertNull(vm.state.value.saveError)
    }

    @Test
    fun courseOpenFailureIsTyped() {
        repo.courseResult = AppResult.Failure(AppError.NotFound)
        val vm = vm()
        vm.openCourse()
        assertEquals(AppError.NotFound, vm.state.value.error)
        assertNull(vm.state.value.course)
    }

    @Test
    fun reelPlaysEachLineAndFinishingTheLastSavesWatch() {
        val vm = vm()
        vm.openCourse()
        vm.onEvent(CourseEvent.OpenDoor(Door.WATCH))
        vm.onEvent(CourseEvent.ReelToggle)
        vm.onEvent(CourseEvent.ReelTick)
        assertEquals(1, vm.state.value.reel.index)
        assertEquals(listOf("ヨシ！", "OFF"), speaker.spoken)
        vm.onEvent(CourseEvent.ReelTick)
        assertFalse(vm.state.value.reel.playing)
        assertEquals(listOf(Progress.KEY_WATCH), repo.saved)
    }

    @Test
    fun pauseStopsReelAndSpeech() {
        val vm = vm()
        vm.openCourse()
        vm.onEvent(CourseEvent.OpenDoor(Door.WATCH))
        vm.onEvent(CourseEvent.ReelToggle)
        vm.onEvent(CourseEvent.Pause)
        assertFalse(vm.state.value.reel.playing)
        assertTrue(speaker.stops > 0)
    }

    @Test
    fun passingADrillSavesItOnce() {
        val vm = vm()
        vm.openCourse()
        vm.onEvent(CourseEvent.OpenDoor(Door.PLAY))
        vm.onEvent(CourseEvent.OpenDrill("seq"))
        listOf(0, 1, 2, 2).forEach { vm.onEvent(CourseEvent.DrillInput(DrillEvent.SeqTap(it))) }
        assertEquals(listOf("drill:seq"), repo.saved)
        assertTrue("seq" in vm.state.value.progress.drills)
    }

    @Test
    fun inspectNeedsEverySpotThenJudgesAgainstTheHiddenAnswer() {
        val vm = vm()
        vm.openCourse()
        vm.onEvent(CourseEvent.OpenDoor(Door.INSPECT))
        vm.onEvent(CourseEvent.Judge(true))
        assertFalse(vm.state.value.inspect.wrong)
        vm.onEvent(CourseEvent.InspectSpotTapped(0))
        vm.onEvent(CourseEvent.InspectSpotTapped(1))
        vm.onEvent(CourseEvent.Judge(false))
        assertTrue(vm.state.value.inspect.wrong)
        assertTrue(repo.saved.isEmpty())
        vm.onEvent(CourseEvent.Judge(true))
        assertFalse(vm.state.value.inspect.wrong)
        assertEquals(listOf(Progress.KEY_INSPECT), repo.saved)
    }

    @Test
    fun startOverClearsProgress() {
        val vm = vm()
        vm.openCourse()
        vm.onEvent(CourseEvent.FinishRead)
        vm.onEvent(CourseEvent.StartOver)
        assertEquals(1, repo.resets)
        assertFalse(vm.state.value.progress.read)
    }

    @Test
    fun ringTapPlaysTheWheelTone() {
        val vm = CourseViewModel(repo, TestSessionStore(), speaker, tone)
        repo.courseResult = AppResult.Success(sampleCourse().copy(drills = listOf(ringDrill)))
        vm.openCourse()
        vm.onEvent(CourseEvent.OpenDoor(Door.PLAY))
        vm.onEvent(CourseEvent.OpenDrill("ring"))
        vm.onEvent(CourseEvent.DrillInput(DrillEvent.RingTap(0)))
        vm.onEvent(CourseEvent.DrillInput(DrillEvent.RingTap(1)))
        assertEquals(listOf(1200, 320), tone.played)
    }
}
