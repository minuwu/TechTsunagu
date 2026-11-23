package com.techtsunagu.wakatta.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.WakattaStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w412dp-h2000dp")
class HomeComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersLoadingState() {
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(isLoading = true),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun rendersErrorStateWithRetry() {
        var retried = false
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(error = "Network timeout"),
                onEvent = { event ->
                    if (event is HomeEvent.Retry) retried = true
                },
            )
        }

        composeTestRule.onNodeWithText("Error: Network timeout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().performClick()
        assertTrue(retried)
    }

    @Test
    fun rendersEmptyState() {
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(isEmpty = true),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("No content available").assertIsDisplayed()
    }

    @Test
    fun rendersForemanContentSuccess() {
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(
                    isForeman = true,
                    userName = "Tanaka",
                    dateText = "11月23日 (月)",
                    followUpCount = 1,
                    followUpDetailUser = "Rahim",
                    followUpDetailTerm = "Die",
                    tallyGreen = 5,
                    tallyOrange = 2,
                    tallyRed = 1,
                    tallyWait = 1,
                    openTasksCount = 3,
                    lockedMachinesCount = 1,
                    attentionCards = listOf(
                        AttentionCardItem(
                            id = "att1",
                            name = "Rahim Uddin",
                            message = "Die replacement procedure",
                            status = WakattaStatus.ORANGE,
                        )
                    ),
                    lockedMachines = listOf(
                        LockedMachineItem(
                            id = "mc2",
                            name = "Press #2",
                            lockedWorkersText = "Rahim",
                        )
                    ),
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Good morning, Tanaka").assertExists()
        composeTestRule.onNodeWithText("Follow-ups needed: 1").assertExists()
        composeTestRule.onNodeWithText("Today's Understanding").assertExists()
        composeTestRule.onNodeWithText("Quick Actions").assertExists()
        composeTestRule.onNodeWithText("Briefing").assertExists()
        composeTestRule.onNodeWithText("Near-miss").assertExists()
        composeTestRule.onNodeWithText("Analytics").assertExists()
        composeTestRule.onNodeWithText("Skill Matrix").assertExists()
        composeTestRule.onNodeWithText("Rahim Uddin").assertExists()
        composeTestRule.onNodeWithText("Unfinished Tasks").assertExists()
        composeTestRule.onNodeWithText("Press #2").assertExists()
    }

    @Test
    fun rendersWorkerContentSuccess() {
        composeTestRule.setContent {
            HomeContent(
                state = HomeUiState(
                    isForeman = false,
                    userName = "Rahim",
                    todayBriefing = BriefingItem(
                        id = "b1",
                        focusText = "Safety rules today",
                    ),
                    pendingChecks = listOf(
                        PendingCheckItem(
                            id = "chk1",
                            fromName = "Tanaka Kenichi",
                            time = "08:02",
                            text = "Please check the oil on machine 3",
                            threadId = "t_line2",
                        )
                    ),
                    myTasks = listOf(
                        HomeTaskItem(
                            id = "k1",
                            title = "Lathe oil inspection",
                            dueText = "09:00",
                        )
                    ),
                    myMachines = listOf(
                        HomeMachineItem(
                            id = "mc1",
                            name = "Lathe 1",
                            isReady = true,
                        )
                    ),
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Good morning, Rahim").assertExists()
        composeTestRule.onNodeWithText("Today's Briefing").assertExists()
        composeTestRule.onNodeWithText("Safety rules today").assertExists()
        composeTestRule.onNodeWithText("Checks Waiting for You").assertExists()
        composeTestRule.onNodeWithText("Please check the oil on machine 3").assertExists()
        composeTestRule.onNodeWithText("Answer Now").assertExists()
        composeTestRule.onNodeWithText("Report Near-miss").assertExists()
        composeTestRule.onNodeWithText("My Learning").assertExists()
        composeTestRule.onNodeWithText("Lathe oil inspection").assertExists()
        composeTestRule.onNodeWithText("Lathe 1").assertExists()
        composeTestRule.onNodeWithText("Scan Machine").assertExists()
    }
}
