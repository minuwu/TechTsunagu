package com.techtsunagu.wakatta.feature.briefing

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme
import com.techtsunagu.wakatta.core.model.CheckQuestion
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.feature.briefing.model.BriefingDraft
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForForeman
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForWorker
import com.techtsunagu.wakatta.feature.briefing.model.BriefingReceipts
import com.techtsunagu.wakatta.feature.briefing.model.BriefingWorkerStatus
import com.techtsunagu.wakatta.feature.briefing.model.CheckQuestionWithAnswer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w412dp-h2000dp")
class BriefingComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleForemanBriefing = BriefingForForeman(
        id = "b_1",
        text = mapOf("ja" to "安全第一", "en" to "Safety first"),
        focusText = mapOf("ja" to "手の挟まれ防止", "en" to "Prevent hand entrapment"),
        changeText = mapOf("ja" to "金型交換は午後", "en" to "Die change in the afternoon"),
        createdAt = "2026-11-23 08:00",
        publishedAt = "2026-11-23 08:00",
        question = CheckQuestionWithAnswer(
            id = "q_1",
            prompt = mapOf("ja" to "金型交換はいつですか？", "en" to "When is the die change?"),
            options = listOf(
                mapOf("ja" to "午後", "en" to "Afternoon"),
                mapOf("ja" to "午前", "en" to "Morning"),
            ),
            correctIndex = 0,
        ),
    )

    private val sampleWorkerBriefing = BriefingForWorker(
        id = "b_1",
        text = mapOf("ja" to "安全第一", "en" to "Safety first"),
        focusText = mapOf("ja" to "手の挟まれ防止", "en" to "Prevent hand entrapment"),
        changeText = mapOf("ja" to "金型交換は午後", "en" to "Die change in the afternoon"),
        createdAt = "2026-11-23 08:00",
        publishedAt = "2026-11-23 08:00",
        question = CheckQuestion(
            id = "q_1",
            termId = null,
            prompt = mapOf("ja" to "金型交換はいつですか？", "en" to "When is the die change?"),
            options = listOf(
                mapOf("ja" to "午後", "en" to "Afternoon"),
                mapOf("ja" to "午前", "en" to "Morning"),
            ),
        ),
    )

    private val sampleDraft = BriefingDraft(
        id = "d_1",
        text = mapOf("ja" to "話した内容", "en" to "Spoken content"),
        focusText = mapOf("ja" to "安全確認", "en" to "Safety focus text"),
        changeText = mapOf("ja" to "変更点", "en" to "One change text"),
        question = CheckQuestionWithAnswer(
            id = "q_d",
            prompt = mapOf("ja" to "質問文", "en" to "Question prompt"),
            options = listOf(
                mapOf("ja" to "正解", "en" to "Correct option"),
                mapOf("ja" to "不正解", "en" to "Wrong option"),
            ),
            correctIndex = 0,
        ),
        transcriptJa = "話した日本語のテキスト",
    )

    // --- BriefingNewContent Tests ---

    @Test
    fun briefingNew_rendersLoadingState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingNewContent(
                    state = BriefingNewUiState(isLoading = true),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun briefingNew_rendersErrorState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingNewContent(
                    state = BriefingNewUiState(
                        error = "Network failure",
                        phase = BriefingNewPhase.IDLE,
                        pastBriefings = emptyList(),
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Network failure").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun briefingNew_rendersIdleState_withPastBriefings() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingNewContent(
                    state = BriefingNewUiState(
                        phase = BriefingNewPhase.IDLE,
                        pastBriefings = listOf(sampleForemanBriefing),
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Tap mic to speak today's briefing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Past Briefings").assertIsDisplayed()
        composeTestRule.onNodeWithText("手の挟まれ防止").assertIsDisplayed()
    }

    @Test
    fun briefingNew_rendersTranslatingState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingNewContent(
                    state = BriefingNewUiState(phase = BriefingNewPhase.TRANSLATING),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Translating to English and generating check question...").assertIsDisplayed()
    }

    @Test
    fun briefingNew_rendersReadyDraftState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingNewContent(
                    state = BriefingNewUiState(
                        phase = BriefingNewPhase.READY,
                        draft = sampleDraft,
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Spoken content (Japanese)").assertIsDisplayed()
        composeTestRule.onNodeWithText("話した日本語のテキスト").assertIsDisplayed()
        composeTestRule.onNodeWithText("Safety focus text").assertIsDisplayed()
        composeTestRule.onNodeWithText("One change text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Question prompt").assertIsDisplayed()
        composeTestRule.onNodeWithText("Redo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Publish").assertIsDisplayed()
    }

    @Test
    fun briefingNew_rendersConfirmPublishDialog() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingNewContent(
                    state = BriefingNewUiState(
                        phase = BriefingNewPhase.READY,
                        draft = sampleDraft,
                        showPublishConfirm = true,
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Publish morning briefing?").assertIsDisplayed()
        composeTestRule.onNodeWithText("All workers will receive the translated briefing and check question.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    // --- BriefingBoardContent Tests ---

    @Test
    fun briefingBoard_rendersLoadingState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingBoardContent(
                    state = BriefingBoardUiState(isLoading = true),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun briefingBoard_rendersErrorState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingBoardContent(
                    state = BriefingBoardUiState(error = "Connection lost", briefing = null),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Connection lost").assertIsDisplayed()
    }

    @Test
    fun briefingBoard_rendersEmptyState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingBoardContent(
                    state = BriefingBoardUiState(isEmpty = true, briefing = null),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("No briefings yet").assertIsDisplayed()
    }

    @Test
    fun briefingBoard_rendersSuccessState() {
        val worker = WorkerInfo(id = "w1", displayName = "Rahim Uddin", language = "bn")
        val statuses = listOf(
            BriefingWorkerStatus(
                worker = worker,
                hasAttended = true,
                status = WakattaStatus.GREEN,
                answerText = "Afternoon",
            ),
        )

        composeTestRule.setContent {
            WakattaTheme {
                BriefingBoardContent(
                    state = BriefingBoardUiState(
                        briefing = sampleForemanBriefing,
                        workerStatuses = statuses,
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("手の挟まれ防止").assertIsDisplayed()
        composeTestRule.onNodeWithText("Attendance and understanding per worker").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rahim Uddin").assertIsDisplayed()
        composeTestRule.onNodeWithText("Attended").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check Question").assertIsDisplayed()
    }

    // --- BriefingCardContent Tests ---

    @Test
    fun briefingCard_rendersLoadingState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingCardContent(
                    state = BriefingCardUiState(isLoading = true),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun briefingCard_rendersErrorState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingCardContent(
                    state = BriefingCardUiState(error = "Briefing expired", briefing = null),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Briefing expired").assertIsDisplayed()
    }

    @Test
    fun briefingCard_rendersEmptyState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingCardContent(
                    state = BriefingCardUiState(isEmpty = true, briefing = null),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("No briefings yet").assertIsDisplayed()
    }

    @Test
    fun briefingCard_rendersUnansweredSuccessState() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingCardContent(
                    state = BriefingCardUiState(
                        briefing = sampleWorkerBriefing,
                        isAnswered = false,
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Tanaka Kenichi (Foreman)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Prevent hand entrapment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Die change in the afternoon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read aloud").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 quick question").assertIsDisplayed()
        composeTestRule.onNodeWithText("When is the die change?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Afternoon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Morning").assertIsDisplayed()
        composeTestRule.onNodeWithText("Not sure").assertIsDisplayed()
    }

    @Test
    fun briefingCard_rendersAnsweredState_green() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingCardContent(
                    state = BriefingCardUiState(
                        briefing = sampleWorkerBriefing,
                        isAnswered = true,
                        selectedOptionIndex = 0,
                        status = WakattaStatus.GREEN,
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Wakatta! You understood.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
    }

    @Test
    fun briefingCard_rendersAnsweredState_orange() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingCardContent(
                    state = BriefingCardUiState(
                        briefing = sampleWorkerBriefing,
                        isAnswered = true,
                        selectedOptionIndex = -1,
                        status = WakattaStatus.ORANGE,
                        correctAnswerText = "Afternoon",
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Tanaka will explain it again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Answer: Afternoon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
    }

    @Test
    fun briefingCard_rendersAnsweredState_red() {
        composeTestRule.setContent {
            WakattaTheme {
                BriefingCardContent(
                    state = BriefingCardUiState(
                        briefing = sampleWorkerBriefing,
                        isAnswered = true,
                        selectedOptionIndex = 1,
                        status = WakattaStatus.RED,
                        correctAnswerText = "Afternoon",
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Not quite. Tanaka will explain it again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Answer: Afternoon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
    }
}
