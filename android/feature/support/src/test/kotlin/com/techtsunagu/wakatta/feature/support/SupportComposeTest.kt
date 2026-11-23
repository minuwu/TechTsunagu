package com.techtsunagu.wakatta.feature.support

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme
import com.techtsunagu.wakatta.core.model.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SupportComposeTest {

    @get:Rule
    val rule = createComposeRule()

    private val interviews = newInterviewRepo()
    private val logData = runBlocking { (newLogRepo(interviews).getSupportLog(null, null) as AppResult.Success).value }
    private val ivState = runBlocking {
        InterviewUiState(
            isLoading = false,
            selectedWorkerId = "u_rahim",
            workers = (interviews.getAllWorkers() as AppResult.Success).value,
            dueInfo = (interviews.getDueInfo("u_rahim") as AppResult.Success).value,
            prepInfo = (interviews.getInterviewPrep("u_rahim") as AppResult.Success).value,
            pastInterviews = (interviews.getPastInterviews("u_rahim") as AppResult.Success).value,
        )
    }

    private fun logContent(state: SupportLogUiState) = rule.setContent {
        WakattaTheme { SupportLogContent(state = state, onEvent = {}) }
    }

    private fun ivContent(state: InterviewUiState) = rule.setContent {
        WakattaTheme { InterviewContent(state = state, onEvent = {}) }
    }

    @Test
    fun logLoading() {
        logContent(SupportLogUiState(isLoading = true))
        rule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun logEmpty() {
        logContent(SupportLogUiState(isLoading = false, logData = null))
        rule.onNodeWithText("No records found").assertIsDisplayed()
    }

    @Test
    fun logError() {
        logContent(SupportLogUiState(isLoading = false, error = SupportError.OFFLINE))
        rule.onNodeWithText("No internet connection, so the data could not be loaded. Check your connection and tap Retry.")
            .assertIsDisplayed()
    }

    @Test
    fun logSuccess() {
        logContent(SupportLogUiState(isLoading = false, logData = logData))
        rule.onNodeWithText("All Time").assertIsDisplayed()
        rule.onNodeWithText("Rahim").assertIsDisplayed()
    }

    @Test
    fun interviewLoading() {
        ivContent(InterviewUiState(isLoading = true))
        rule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun interviewEmpty() {
        ivContent(InterviewUiState(isLoading = false))
        rule.onNodeWithText("No records found").assertIsDisplayed()
    }

    @Test
    fun interviewError() {
        ivContent(InterviewUiState(isLoading = false, error = SupportError.TIMEOUT))
        rule.onNodeWithText("The server took too long to answer. Wait a moment and tap Retry.").assertIsDisplayed()
    }

    @Test
    fun interviewSuccess() {
        ivContent(ivState)
        rule.onNodeWithText("Rahim").assertIsDisplayed()
    }
}
