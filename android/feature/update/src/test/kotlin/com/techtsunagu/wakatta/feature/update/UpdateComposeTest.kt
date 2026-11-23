package com.techtsunagu.wakatta.feature.update

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class UpdateComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun availableUpdateShowsVersionAndDownloadButton() {
        composeTestRule.setContent {
            UpdateContent(
                state = UpdateUiState(
                    phase = UpdatePhase.Available,
                    latestVersionName = "1.3.0",
                    canDownload = true,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Version 1.3.0 is available").assertIsDisplayed()
        composeTestRule.onNodeWithText("Download and install").assertIsDisplayed()
    }

    @Test
    fun hardUpdateShowsRequiredNoticeAndSignOut() {
        composeTestRule.setContent {
            UpdateContent(
                state = UpdateUiState(
                    phase = UpdatePhase.Available,
                    isHard = true,
                    latestVersionName = "2.0.0",
                    canDownload = true,
                ),
                onEvent = {},
                onSignOutClick = {},
            )
        }

        composeTestRule.onNodeWithText("Please update to keep using the app.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign out").assertIsDisplayed()
    }

    @Test
    fun downloadShowsProgressAndCancel() {
        composeTestRule.setContent {
            UpdateContent(
                state = UpdateUiState(phase = UpdatePhase.Downloading, progressPercent = 40),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Downloading... 40%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun upToDateStateDisplaysMessage() {
        composeTestRule.setContent {
            UpdateContent(
                state = UpdateUiState(phase = UpdatePhase.UpToDate),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("You have the latest version.").assertIsDisplayed()
    }

    @Test
    fun doneStateDisplaysMessage() {
        composeTestRule.setContent {
            UpdateContent(
                state = UpdateUiState(phase = UpdatePhase.Done),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Update complete.").assertIsDisplayed()
    }

    @Test
    fun failedStateShowsRetry() {
        composeTestRule.setContent {
            UpdateContent(
                state = UpdateUiState(phase = UpdatePhase.Failed, error = UpdateError.Network),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
}
