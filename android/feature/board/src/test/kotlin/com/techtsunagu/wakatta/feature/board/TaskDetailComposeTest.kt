package com.techtsunagu.wakatta.feature.board

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class TaskDetailComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleTask = Task(
        id = "k_detail",
        title = mapOf("en" to "Lathe Alignment Check", "ja" to "旋盤芯出し点検"),
        column = TaskColumn.DOING,
        assignees = listOf("u_rahim"),
        dueAt = "Today 14:00",
        machineId = "mc1",
        fromMessageId = "m1",
        version = 2,
    )

    private val sampleMachine = Machine(
        id = "mc1",
        code = "lathe-1",
        name = mapOf("en" to "Lathe 1", "ja" to "旋盤1号機"),
        currentVersionId = "v1",
        isReady = true,
    )

    private val sampleUser = User(
        id = "u_rahim",
        displayName = "Rahim Uddin",
        role = Role.WORKER,
        language = "bn",
    )

    @Test
    fun rendersLoadingState() {
        composeTestRule.setContent {
            TaskDetailContent(
                state = TaskDetailUiState(isLoading = true, task = null),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Loading tasks…").assertIsDisplayed()
    }

    @Test
    fun rendersEmptyStateWhenTaskNull() {
        composeTestRule.setContent {
            TaskDetailContent(
                state = TaskDetailUiState(isLoading = false, task = null),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("No tasks").assertIsDisplayed()
    }

    @Test
    fun rendersErrorStateWithRetry() {
        var retryClicked = false
        composeTestRule.setContent {
            TaskDetailContent(
                state = TaskDetailUiState(
                    isLoading = false,
                    task = null,
                    errorRes = R.string.board_error_not_found,
                ),
                onEvent = { event ->
                    if (event is TaskDetailEvent.Retry) retryClicked = true
                },
            )
        }

        composeTestRule.onNodeWithText("Task not found.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun rendersSuccessStateWithDetails() {
        var movedToColumn: TaskColumn? = null
        var navigatedMachine = false

        composeTestRule.setContent {
            TaskDetailContent(
                state = TaskDetailUiState(
                    isLoading = false,
                    task = sampleTask,
                    machine = sampleMachine,
                    machines = listOf(sampleMachine),
                    users = listOf(sampleUser),
                    isForeman = true,
                    currentUserLang = "en",
                ),
                onEvent = { event ->
                    if (event is TaskDetailEvent.ChangeColumn) movedToColumn = event.newColumn
                },
                onNavigateToMachine = { navigatedMachine = true },
            )
        }

        // Title
        composeTestRule.onNodeWithText("Lathe Alignment Check").assertIsDisplayed()

        // From chat chip
        composeTestRule.onNodeWithText("From chat").assertIsDisplayed()

        // Segmented control options
        composeTestRule.onNodeWithText("To do").assertIsDisplayed()
        composeTestRule.onNodeWithText("Doing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()

        // Meta info
        composeTestRule.onNodeWithText("Today 14:00").assertExists()
        composeTestRule.onNodeWithText("Lathe 1").performScrollTo().assertIsDisplayed()

        // Assignee
        composeTestRule.onNodeWithText("Rahim Uddin").assertExists()

        // Click segmented control to move to Done
        composeTestRule.onNodeWithText("Done").performClick()
        assertEquals(TaskColumn.DONE, movedToColumn)

        // Click machine
        composeTestRule.onNodeWithText("Lathe 1").performScrollTo().performClick()
        assertTrue(navigatedMachine)
    }

    @Test
    fun rendersConflictBannerWhenConflictIsTrue() {
        var reloadClicked = false
        composeTestRule.setContent {
            TaskDetailContent(
                state = TaskDetailUiState(
                    task = sampleTask,
                    isConflict = true,
                    currentUserLang = "en",
                ),
                onEvent = { event ->
                    if (event is TaskDetailEvent.Refresh) reloadClicked = true
                },
            )
        }

        composeTestRule.onNodeWithText("Task Conflict (HTTP 412)").assertIsDisplayed()
        composeTestRule.onNodeWithText("This task was updated by someone else. Please reload to see the latest version.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reload").assertIsDisplayed()

        composeTestRule.onNodeWithText("Reload").performClick()
        assertTrue(reloadClicked)
    }
}
