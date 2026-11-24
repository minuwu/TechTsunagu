package com.techtsunagu.wakatta.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w412dp-h2000dp")
class OnboardingComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersForemanStep0() {
        var copyClicked = false
        composeTestRule.setContent {
            OnboardingContent(
                state = OnboardingUiState(
                    isForeman = true,
                    step = 0,
                    teamCode = "PRESS-2F-4821",
                ),
                onEvent = { event ->
                    if (event is OnboardingEvent.CopyTeamCode) copyClicked = true
                },
            )
        }

        composeTestRule.onNodeWithText("Team Code").assertExists()
        composeTestRule.onNodeWithText("PRESS-2F-4821").assertExists()
        composeTestRule.onNodeWithText("Copy Code").assertExists().performClick()
        assertTrue(copyClicked)
    }

    @Test
    fun rendersForemanStep1() {
        composeTestRule.setContent {
            OnboardingContent(
                state = OnboardingUiState(
                    isForeman = true,
                    step = 1,
                    micGranted = false,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Use Microphone").assertExists()
        composeTestRule.onNodeWithText("Grant Microphone").assertExists()
        composeTestRule.onNodeWithText("Start").assertExists()
    }

    @Test
    fun rendersWorkerStep0Join() {
        composeTestRule.setContent {
            OnboardingContent(
                state = OnboardingUiState(
                    isForeman = false,
                    step = 0,
                    isJoined = false,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Join Your Team").assertExists()
        composeTestRule.onNodeWithText("Tap to scan").assertExists()
    }

    @Test
    fun rendersWorkerStep1Language() {
        composeTestRule.setContent {
            OnboardingContent(
                state = OnboardingUiState(
                    isForeman = false,
                    step = 1,
                    selectedLanguage = "en",
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Choose Your Language").assertExists()
        composeTestRule.onNodeWithText("English").assertExists()
        composeTestRule.onNodeWithText("日本語").assertExists()
    }

    @Test
    fun rendersWorkerStep2Check() {
        composeTestRule.setContent {
            OnboardingContent(
                state = OnboardingUiState(
                    isForeman = false,
                    step = 2,
                    isAnswerCorrect = false,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Try a Check").assertExists()
        composeTestRule.onNodeWithText("Which one protects your head at work?").assertExists()
        composeTestRule.onNodeWithText("Helmet").assertExists()
        composeTestRule.onNodeWithText("Gloves").assertExists()
        composeTestRule.onNodeWithText("Boots").assertExists()
    }

    @Test
    fun rendersWorkerStep2Celebration() {
        composeTestRule.setContent {
            OnboardingContent(
                state = OnboardingUiState(
                    isForeman = false,
                    step = 2,
                    isAnswerCorrect = true,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("You're ready!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
    }
}
