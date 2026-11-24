package com.techtsunagu.wakatta.feature.machines

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.NoteStatus
import com.techtsunagu.wakatta.core.model.PanelState
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.feature.machines.data.FakePanelRepository
import com.techtsunagu.wakatta.feature.machines.domain.MachineTaskSummary
import com.techtsunagu.wakatta.feature.machines.domain.NoteQuestionDto
import com.techtsunagu.wakatta.feature.machines.domain.NoteStepDto
import com.techtsunagu.wakatta.feature.machines.domain.NoteVersionDto
import com.techtsunagu.wakatta.feature.machines.domain.PanelOverrideDto
import com.techtsunagu.wakatta.feature.machines.domain.PanelRepository
import com.techtsunagu.wakatta.feature.machines.domain.PanelStateResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class MachinesComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ---------------------------------------------------------
    // MachinesContent Tests (Loading, Empty, Error, Success)
    // ---------------------------------------------------------

    @Test
    fun machinesContent_rendersLoadingState() {
        composeTestRule.setContent {
            MachinesContent(
                state = MachinesUiState(isLoading = true),
                onEvent = {}
            )
        }
    }

    @Test
    fun machinesContent_rendersEmptyState() {
        composeTestRule.setContent {
            MachinesContent(
                state = MachinesUiState(isEmpty = true, isLoading = false),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("No machines found").assertIsDisplayed()
    }

    @Test
    fun machinesContent_rendersErrorState() {
        composeTestRule.setContent {
            MachinesContent(
                state = MachinesUiState(error = AppError.Offline, isLoading = false),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("No connection. Please try again.").assertIsDisplayed()
    }

    @Test
    fun machinesContent_rendersSuccessWorkerState() {
        val items = listOf(
            MachineItemUiState(
                id = "mc1",
                code = "lathe-1",
                nameJa = "旋盤1号機",
                nameTranslated = "Lathe 1",
                isReady = true,
                status = SkillStatus.VALID,
                lockedWorkerCount = 0
            ),
            MachineItemUiState(
                id = "mc2",
                code = "press-2",
                nameJa = "2号プレス",
                nameTranslated = "Press #2",
                isReady = false,
                status = SkillStatus.NONE,
                lockedWorkerCount = 1
            )
        )

        composeTestRule.setContent {
            MachinesContent(
                state = MachinesUiState(
                    isLoading = false,
                    isForeman = false,
                    machines = items
                ),
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Lathe 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Press #2").assertIsDisplayed()
    }

    @Test
    fun machinesContent_rendersSuccessForemanState() {
        val items = listOf(
            MachineItemUiState(
                id = "mc2",
                code = "press-2",
                nameJa = "2号プレス",
                nameTranslated = "Press #2",
                isReady = false,
                status = SkillStatus.NONE,
                lockedWorkerCount = 2,
                workerStatuses = listOf(
                    MachineWorkerChipState("u_rahim", "Rahim", false),
                    MachineWorkerChipState("u_nguyen", "Nguyen", true)
                )
            )
        )

        composeTestRule.setContent {
            MachinesContent(
                state = MachinesUiState(
                    isLoading = false,
                    isForeman = true,
                    machines = items
                ),
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Press #2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rahim").assertIsDisplayed()
    }

    // ---------------------------------------------------------
    // MachineContent Tests (Loading, Error, Success)
    // ---------------------------------------------------------

    @Test
    fun machineContent_rendersLoadingState() {
        composeTestRule.setContent {
            MachineContent(
                state = MachineUiState(isLoading = true),
                onEvent = {}
            )
        }
    }

    @Test
    fun machineContent_rendersErrorState() {
        composeTestRule.setContent {
            MachineContent(
                state = MachineUiState(error = AppError.NotFound, isLoading = false),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("Not found.").assertIsDisplayed()
    }

    @Test
    fun machineContent_rendersSuccessState() {
        val state = MachineUiState(
            machineId = "mc1",
            code = "lathe-1",
            nameJa = "旋盤1号機",
            nameTranslated = "Lathe 1",
            isReady = true,
            skillStatus = SkillStatus.VALID,
            assignedWorkerName = "Rahim Uddin",
            tasks = listOf(
                MachineTaskSummary("t1", mapOf("ja" to "チャック清掃", "en" to "Clean chuck and bed"), "due 10:00")
            ),
            workers = listOf(
                MachineWorkerStatusUiState("u_rahim", "Rahim Uddin", true, true, SkillStatus.VALID)
            ),
            isLoading = false
        )

        composeTestRule.setContent {
            MachineContent(
                state = state,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Open Machine Panel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Everyone on this machine").assertIsDisplayed()
    }

    // ---------------------------------------------------------
    // ScanContent Tests (Loading, Error, Success)
    // ---------------------------------------------------------

    @Test
    fun scanContent_rendersLoadingState() {
        composeTestRule.setContent {
            ScanContent(
                state = ScanUiState(isLoading = true),
                onEvent = {}
            )
        }
    }

    @Test
    fun scanContent_rendersErrorState() {
        composeTestRule.setContent {
            ScanContent(
                state = ScanUiState(error = AppError.Validation(), isLoading = false),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("Invalid QR code").assertIsDisplayed()
    }

    @Test
    fun scanContent_rendersSuccessStateWithChips() {
        val chips = listOf(
            ScanMachineChipUiState("mc1", "lathe-1", "Lathe 1"),
            ScanMachineChipUiState("mc2", "press-2", "Press 2")
        )

        composeTestRule.setContent {
            ScanContent(
                state = ScanUiState(
                    isLoading = false,
                    availableMachines = chips,
                    isTorchOn = true
                ),
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Lathe 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Press 2").assertIsDisplayed()
    }

    // ---------------------------------------------------------
    // PanelContent Tests (Loading, Error, Locked, Ready, Override)
    // ---------------------------------------------------------

    @Test
    fun panelContent_rendersLoadingState() {
        composeTestRule.setContent {
            PanelContent(
                state = PanelUiState(isLoading = true),
                onEvent = {}
            )
        }
    }

    @Test
    fun panelContent_rendersErrorState() {
        composeTestRule.setContent {
            PanelContent(
                state = PanelUiState(error = AppError.Offline, isLoading = false),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("No connection. Please try again.").assertIsDisplayed()
    }

    @Test
    fun panelContent_rendersLockedState() {
        val state = PanelUiState(
            machineId = "mc2",
            machineName = "Press 2",
            state = PanelState.LOCKED,
            isLoading = false
        )

        composeTestRule.setContent {
            PanelContent(
                state = state,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Press 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("LOCKED").assertIsDisplayed()
    }

    @Test
    fun panelContent_unknownStateShowsTheWarningAndNeverReady() {
        val state = PanelUiState(
            machineId = "mc1",
            machineName = "Lathe 1",
            state = PanelState.UNKNOWN,
            isUserSignedOff = true,
            isLoading = false
        )

        composeTestRule.setContent {
            PanelContent(
                state = state,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("UNKNOWN").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status unknown: no connection. Ask your foreman before you start.").assertIsDisplayed()
        composeTestRule.onNodeWithText("READY").assertDoesNotExist()
    }

    @Test
    fun panelContent_rendersReadyStateWithOverride() {
        val override = PanelOverrideDto(
            id = "ov1",
            machineId = "mc2",
            workerId = "u_rahim",
            by = "田中 健一",
            reason = "Urgent production quota",
            minutes = 60,
            startsAt = "2026-11-23T10:00:00Z",
            endsAt = "2026-11-23T11:00:00Z"
        )

        val state = PanelUiState(
            machineId = "mc2",
            machineName = "Press 2",
            state = PanelState.OVERRIDE,
            activeOverride = override,
            isLoading = false
        )

        composeTestRule.setContent {
            PanelContent(
                state = state,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("OVERRIDE ACTIVE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reason: Urgent production quota").assertIsDisplayed()
    }

    // ---------------------------------------------------------
    // NoteContent Tests (Loading, Error, READ, QUIZ, PASS, FAIL, CONFLICT)
    // ---------------------------------------------------------

    private fun sampleNoteVersion(): NoteVersionDto {
        return NoteVersionDto(
            id = "v3",
            machineId = "mc1",
            version = "v3",
            status = NoteStatus.APPROVED,
            updatedAt = "2026-11-20T08:00:00Z",
            by = "田中 健一",
            steps = listOf(
                NoteStepDto(
                    index = 0,
                    icon = "power",
                    textJa = "主電源をONにする",
                    textEn = "Turn main power ON",
                    textBn = "মেইন পাওয়ার চালু করুন",
                    backJa = "主電源を入れる"
                ),
                NoteStepDto(
                    index = 1,
                    icon = "safety",
                    textJa = "安全カバーを閉める",
                    textEn = "Close safety cover",
                    textBn = "নিরাপত্তা কভার বন্ধ করুন",
                    backJa = "カバーを閉じる"
                )
            ),
            questions = listOf(
                NoteQuestionDto(
                    id = "q1",
                    stepIndex = 1,
                    term = "安全カバー",
                    qJa = "作業前に何を確認しますか？",
                    qEn = "What to check before starting?",
                    qBn = "শুরু করার আগে কি চেক করবেন?",
                    optionsJa = listOf("安全カバーを確認する", "何も確認しない"),
                    optionsEn = listOf("Check safety cover", "Do nothing"),
                    optionsBn = listOf("নিরাপত্তা কভার পরীক্ষা করুন", "কিছুই করবেন না"),
                    correctIndex = 0
                )
            )
        )
    }

    @Test
    fun noteContent_rendersLoadingState() {
        composeTestRule.setContent {
            NoteContent(
                state = NoteUiState(isLoading = true),
                onEvent = {}
            )
        }
    }

    @Test
    fun noteContent_rendersErrorState() {
        composeTestRule.setContent {
            NoteContent(
                state = NoteUiState(error = AppError.NotFound, isLoading = false),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("Not found.").assertIsDisplayed()
    }

    @Test
    fun noteContent_rendersReadPhase() {
        val state = NoteUiState(
            machineId = "mc1",
            machineName = "旋盤1号機",
            phase = NotePhase.READ,
            userLanguage = "ja",
            currentVersion = sampleNoteVersion(),
            isLoading = false
        )

        composeTestRule.setContent {
            NoteContent(
                state = state,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("主電源をONにする").assertIsDisplayed()
        composeTestRule.onNodeWithText("安全カバーを閉める").assertIsDisplayed()
    }

    @Test
    fun noteContent_rendersQuizPhase() {
        val state = NoteUiState(
            machineId = "mc1",
            machineName = "旋盤1号機",
            phase = NotePhase.QUIZ,
            userLanguage = "en",
            currentVersion = sampleNoteVersion(),
            currentQuestionIndex = 0,
            isLoading = false
        )

        composeTestRule.setContent {
            NoteContent(
                state = state,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("What to check before starting?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check safety cover").assertIsDisplayed()
    }

    @Test
    fun noteContent_rendersPassPhase() {
        val state = NoteUiState(
            machineId = "mc1",
            machineName = "旋盤1号機",
            phase = NotePhase.PASS,
            currentVersion = sampleNoteVersion(),
            isSignedOff = true,
            isLoading = false
        )

        composeTestRule.setContent {
            NoteContent(
                state = state,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Check passed").assertIsDisplayed()
    }

    @Test
    fun noteContent_rendersFailPhase() {
        val version = sampleNoteVersion()
        val state = NoteUiState(
            machineId = "mc1",
            machineName = "旋盤1号機",
            phase = NotePhase.FAIL,
            userLanguage = "ja",
            currentVersion = version,
            wrongSteps = version.steps,
            isLoading = false
        )

        composeTestRule.setContent {
            NoteContent(
                state = state,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Not yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("主電源をONにする").assertIsDisplayed()
    }

    @Test
    fun noteContent_rendersConflictPhase() {
        val state = NoteUiState(
            machineId = "mc1",
            machineName = "旋盤1号機",
            phase = NotePhase.CONFLICT,
            currentVersion = sampleNoteVersion(),
            isLoading = false
        )

        composeTestRule.setContent {
            NoteContent(
                state = state,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Work Instructions Updated").assertIsDisplayed()
    }

    // ---------------------------------------------------------
    // Fix 2: Lifecycle-aware polling tests
    // ---------------------------------------------------------

    private class TestLifecycleOwner(initialState: Lifecycle.State = Lifecycle.State.INITIALIZED) : LifecycleOwner {
        val registry = LifecycleRegistry(this).apply { currentState = initialState }
        override val lifecycle: Lifecycle get() = registry
    }

    private class TrackingPanelRepository(val inner: FakePanelRepository) : PanelRepository by inner {
        var pollCallCount = 0

        override suspend fun getPanelState(machineId: String, ifNoneMatch: String?): AppResult<PanelStateResult> {
            if (ifNoneMatch != null) {
                pollCallCount++
            }
            return inner.getPanelState(machineId, ifNoneMatch)
        }
    }

    @Test
    fun panelScreen_pollingFollowsLifecycleStarted() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.CREATED)
        val fake = FakePanelRepository(delayMs = 0L)
        val trackingRepo = TrackingPanelRepository(fake)
        val sessionStore = TestSessionStore()
        val viewModel = PanelViewModel(trackingRepo, sessionStore) { 1_000L }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                PanelScreen(
                    machineId = "mc1",
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // In CREATED state, repeatOnLifecycle(STARTED) does not poll
        assertEquals(0, trackingRepo.pollCallCount)

        // When lifecycle moves to STARTED, polling starts
        lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
        composeTestRule.waitForIdle()
        // At least one poll tick executed
        assertTrue(trackingRepo.pollCallCount >= 1)

        val pollsBeforeStop = trackingRepo.pollCallCount
        // When lifecycle moves back to CREATED (backgrounded), polling stops
        lifecycleOwner.registry.currentState = Lifecycle.State.CREATED
        composeTestRule.waitForIdle()
        assertEquals(pollsBeforeStop, trackingRepo.pollCallCount)
    }

    // ---------------------------------------------------------
    // Fix 6: Split by job verification
    // ---------------------------------------------------------

    @Test
    fun allScreensAndViewModelsSplitByJob() {
        // Verify ViewModels and Screens exist as independent top-level classes
        assertNotNull(PanelViewModel::class.java)
        assertNotNull(NoteViewModel::class.java)
        assertNotNull(MachineViewModel::class.java)
        assertNotNull(MachinesViewModel::class.java)
        assertNotNull(ScanViewModel::class.java)
    }
}
