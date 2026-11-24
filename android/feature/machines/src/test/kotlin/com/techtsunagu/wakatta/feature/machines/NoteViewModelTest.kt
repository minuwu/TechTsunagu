package com.techtsunagu.wakatta.feature.machines

import com.techtsunagu.wakatta.core.model.CheckResult
import com.techtsunagu.wakatta.feature.machines.data.FakeNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var checkRepository: TestCheckRepository
    private lateinit var speaker: TestSpeaker
    private lateinit var sessionStore: TestSessionStore
    private lateinit var viewModel: NoteViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        noteRepository = FakeNoteRepository(delayMs = 0L)
        checkRepository = TestCheckRepository()
        speaker = TestSpeaker()
        sessionStore = TestSessionStore()
        viewModel = NoteViewModel(
            noteRepository = noteRepository,
            checkRepository = checkRepository,
            machineRepository = TestMachineRepository(),
            signOffRepository = TestSignOffRepository(),
            speaker = speaker,
            sessionStore = sessionStore
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initLoadsNoteVersionsInReadPhase() = runTest {
        viewModel.onEvent(NoteEvent.Init("mc1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("mc1", state.machineId)
        assertEquals(NotePhase.READ, state.phase)
        assertNotNull(state.currentVersion)
        assertTrue(state.currentVersion!!.steps.isNotEmpty())
        assertTrue(state.availableVersions.isNotEmpty())
    }

    @Test
    fun selectVersionSwitchesActiveVersion() = runTest {
        viewModel.onEvent(NoteEvent.Init("mc2"))
        advanceUntilIdle()

        assertEquals("v3", viewModel.state.value.currentVersion?.version)

        viewModel.onEvent(NoteEvent.SelectVersion("v2"))
        advanceUntilIdle()

        assertEquals("v2", viewModel.state.value.currentVersion?.version)
    }

    @Test
    fun speakTextAndStopSpeakingCallsSpeaker() = runTest {
        viewModel.onEvent(NoteEvent.Init("mc1"))
        advanceUntilIdle()

        viewModel.onEvent(NoteEvent.SpeakText("主電源をONにします"))
        assertTrue(speaker.isCurrentlySpeaking)
        assertEquals(1, speaker.spokenUtterances.size)

        viewModel.onEvent(NoteEvent.StopSpeaking)
        assertFalse(speaker.isCurrentlySpeaking)
    }

    @Test
    fun startCheckSwitchesToQuizPhase() = runTest {
        viewModel.onEvent(NoteEvent.Init("mc1"))
        advanceUntilIdle()

        viewModel.onEvent(NoteEvent.StartCheck)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(NotePhase.QUIZ, state.phase)
        assertEquals(0, state.currentQuestionIndex)
        assertNull(state.selectedOptionIndex)
    }

    @Test
    fun quizPassTransitionsToPassPhaseAndEmitsCheckPassed() = runTest {
        viewModel.onEvent(NoteEvent.Init("mc1"))
        advanceUntilIdle()

        var effectReceived: NoteEffect? = null
        val job = launch {
            viewModel.effects.collect { effectReceived = it }
        }

        viewModel.onEvent(NoteEvent.StartCheck)
        advanceUntilIdle()

        val totalQuestions = viewModel.state.value.currentVersion!!.questions.size

        // Answer each question correctly
        for (i in 0 until totalQuestions) {
            val correctIdx = 0
            viewModel.onEvent(NoteEvent.SelectOption(correctIdx))
            viewModel.onEvent(NoteEvent.NextQuestion)
            advanceUntilIdle()
        }

        val state = viewModel.state.value
        assertEquals(NotePhase.PASS, state.phase)
        assertTrue(state.isSignedOff)
        assertEquals(NoteEffect.CheckPassed, effectReceived)
        job.cancel()
    }

    @Test
    fun quizFailTransitionsToFailPhaseWithReviewSteps() = runTest {
        checkRepository.outcome = CheckResult(
            passed = false,
            signOffGranted = false,
            wrongQuestions = listOf("q_mc2_1")
        )

        viewModel.onEvent(NoteEvent.Init("mc2"))
        advanceUntilIdle()

        viewModel.onEvent(NoteEvent.StartCheck)
        advanceUntilIdle()

        val totalQuestions = viewModel.state.value.currentVersion!!.questions.size

        // Answer wrong option on each question
        for (i in 0 until totalQuestions) {
            val wrongIdx = 1
            viewModel.onEvent(NoteEvent.SelectOption(wrongIdx))
            viewModel.onEvent(NoteEvent.NextQuestion)
            advanceUntilIdle()
        }

        val state = viewModel.state.value
        assertEquals(NotePhase.FAIL, state.phase)
        assertFalse(state.isSignedOff)
        assertTrue(state.wrongSteps.isNotEmpty())
        assertEquals(listOf(0), state.wrongQuestionIndices)
        assertEquals("nv_mc2_v3", checkRepository.submittedTargetId)

        // Return to read resets phase
        viewModel.onEvent(NoteEvent.ReturnToRead)
        assertEquals(NotePhase.READ, viewModel.state.value.phase)
    }

    @Test
    fun checkConflictSetsConflictPhase() = runTest {
        checkRepository.shouldFailWithConflict = true
        viewModel.onEvent(NoteEvent.Init("mc1"))
        advanceUntilIdle()

        viewModel.onEvent(NoteEvent.StartCheck)
        advanceUntilIdle()

        val totalQuestions = viewModel.state.value.currentVersion!!.questions.size
        for (i in 0 until totalQuestions) {
            viewModel.onEvent(NoteEvent.SelectOption(0))
            viewModel.onEvent(NoteEvent.NextQuestion)
            advanceUntilIdle()
        }

        val state = viewModel.state.value
        assertEquals(NotePhase.CONFLICT, state.phase)
    }

    private fun TestScope.answerAllQuestionsCorrectly() {
        val total = viewModel.state.value.currentVersion!!.questions.size
        repeat(total) {
            viewModel.onEvent(NoteEvent.SelectOption(0))
            viewModel.onEvent(NoteEvent.NextQuestion)
            advanceUntilIdle()
        }
    }

    @Test
    fun failedSubmitDoesNotSignOffAndRetryReusesTheKey() = runTest {
        checkRepository.shouldFail = true
        viewModel.onEvent(NoteEvent.Init("mc2"))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isSignedOff)
        var effectReceived: NoteEffect? = null
        val job = launch { viewModel.effects.collect { effectReceived = it } }

        viewModel.onEvent(NoteEvent.StartCheck)
        advanceUntilIdle()
        answerAllQuestionsCorrectly()

        assertEquals(NotePhase.QUIZ, viewModel.state.value.phase)
        assertFalse(viewModel.state.value.isSignedOff)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(NoteEffect.SubmitFailed, effectReceived)

        checkRepository.shouldFail = false
        viewModel.onEvent(NoteEvent.NextQuestion)
        advanceUntilIdle()

        assertEquals(NotePhase.PASS, viewModel.state.value.phase)
        assertTrue(viewModel.state.value.isSignedOff)
        assertEquals(2, checkRepository.submittedKeys.size)
        assertEquals(checkRepository.submittedKeys[0], checkRepository.submittedKeys[1])
        job.cancel()
    }

    @Test
    fun aNewCheckAttemptGetsANewKey() = runTest {
        checkRepository.shouldFail = true
        viewModel.onEvent(NoteEvent.Init("mc1"))
        advanceUntilIdle()
        viewModel.onEvent(NoteEvent.StartCheck)
        advanceUntilIdle()
        answerAllQuestionsCorrectly()

        viewModel.onEvent(NoteEvent.RetryCheck)
        advanceUntilIdle()
        answerAllQuestionsCorrectly()

        assertEquals(2, checkRepository.submittedKeys.size)
        assertNotEquals(checkRepository.submittedKeys[0], checkRepository.submittedKeys[1])
    }

    @Test
    fun signedOffStateComesFromTheSignOffsNotFromTheMachineId() = runTest {
        viewModel.onEvent(NoteEvent.Init("mc1"))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isSignedOff)
        assertEquals("旋盤1号機", viewModel.state.value.machineName)

        viewModel.onEvent(NoteEvent.Init("mc2"))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isSignedOff)
    }

    @Test
    fun viewModelHasOnlyHiltInjectableConstructor() {
        val constructors = NoteViewModel::class.java.constructors
        assertEquals(1, constructors.size)
        assertTrue(constructors.first().isAnnotationPresent(javax.inject.Inject::class.java))
    }
}

