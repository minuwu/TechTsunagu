package com.techtsunagu.wakatta.feature.board

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class BoardComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleTask = Task(
        id = "k_test",
        title = mapOf("en" to "Inspect Press Machine", "ja" to "プレス機点検"),
        column = TaskColumn.TODO,
        assignees = listOf("u_rahim"),
        dueAt = "Today 10:00",
        machineId = "mc2",
        fromMessageId = "m1",
        version = 1,
    )

    @Test
    fun rendersLoadingState() {
        composeTestRule.setContent {
            BoardContent(
                state = BoardUiState(isLoading = true),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Loading tasks…").assertIsDisplayed()
    }

    @Test
    fun rendersEmptyStateWhenNoTasks() {
        composeTestRule.setContent {
            BoardContent(
                state = BoardUiState(
                    isLoading = false,
                    todoTasks = emptyList(),
                    doingTasks = emptyList(),
                    doneTasks = emptyList(),
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("To do (0)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Doing (0)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done (0)").assertIsDisplayed()
        composeTestRule.onNodeWithText("No tasks to do · drop here").assertIsDisplayed()
    }

    @Test
    fun rendersErrorStateWithRetry() {
        var retryClicked = false
        composeTestRule.setContent {
            BoardContent(
                state = BoardUiState(
                    isLoading = false,
                    errorRes = R.string.board_error_offline,
                ),
                onEvent = { event ->
                    if (event is BoardEvent.Retry) retryClicked = true
                },
            )
        }

        composeTestRule.onNodeWithText("Offline. Please check network connection.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun rendersSuccessStateWithColumnsAndTasks() {
        var clickedTaskId: String? = null
        composeTestRule.setContent {
            BoardContent(
                state = BoardUiState(
                    isLoading = false,
                    todoTasks = listOf(sampleTask),
                    doingTasks = emptyList(),
                    doneTasks = emptyList(),
                    currentUserLang = "en",
                ),
                onEvent = {},
                onTaskClick = { clickedTaskId = it },
            )
        }

        composeTestRule.onNodeWithText("To do (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Doing (0)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done (0)").assertIsDisplayed()

        // Card contents
        composeTestRule.onNodeWithText("Inspect Press Machine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today 10:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("From chat").assertIsDisplayed()

        // Clicking the task card
        composeTestRule.onNodeWithText("Inspect Press Machine").performClick()
        org.junit.Assert.assertEquals("k_test", clickedTaskId)
    }

    @Test
    fun rendersAddTaskSheetWhenOpen() {
        composeTestRule.setContent {
            BoardContent(
                state = BoardUiState(
                    isLoading = false,
                    isAddTaskSheetOpen = true,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("New task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Assignees").assertIsDisplayed()
        composeTestRule.onNodeWithText("Due").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add task").assertExists()
    }
}
