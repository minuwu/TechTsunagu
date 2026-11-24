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
class MeComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersLoadingState() {
        composeTestRule.setContent {
            MeContent(
                state = MeUiState(isLoading = true),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun rendersErrorStateWithRetry() {
        var retried = false
        composeTestRule.setContent {
            MeContent(
                state = MeUiState(error = "Connection lost"),
                onEvent = { event ->
                    if (event is MeEvent.Retry) retried = true
                },
            )
        }

        composeTestRule.onNodeWithText("Could not load this. Check the network and try again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().performClick()
        assertTrue(retried)
    }

    @Test
    fun rendersForemanMeContent() {
        composeTestRule.setContent {
            MeContent(
                state = MeUiState(
                    isForeman = true,
                    userName = "Tanaka Kenichi",
                    teamMembers = listOf(
                        TeamMemberItem(id = "u_rahim", name = "Rahim Uddin", permittedCount = 2, totalCount = 3),
                    ),
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Tanaka Kenichi").assertExists()
        composeTestRule.onNodeWithText("Foreman").assertExists()
        composeTestRule.onNodeWithText("Team").assertExists()
        composeTestRule.onNodeWithText("Rahim Uddin").assertExists()
        composeTestRule.onNodeWithText("2/3 machines permitted").assertExists()
        composeTestRule.onNodeWithText("Open Skill Matrix").assertExists()
        composeTestRule.onNodeWithText("Settings").assertExists()
        composeTestRule.onNodeWithText("Language").assertExists()
        composeTestRule.onNodeWithText("Dark Mode").assertExists()
        composeTestRule.onNodeWithText("Large Text").assertExists()
        composeTestRule.onNodeWithText("Notification Preferences").assertExists()
        composeTestRule.onNodeWithText("Sign Out").assertExists()
    }

    @Test
    fun rendersWorkerMeContent() {
        composeTestRule.setContent {
            MeContent(
                state = MeUiState(
                    isForeman = false,
                    userName = "Rahim Uddin",
                    passportMachines = listOf(
                        PassportMachineItem(
                            id = "mc1",
                            name = "Lathe 1",
                            isSignedOff = true,
                            signedOffDetail = "v1 · 11/22",
                        ),
                        PassportMachineItem(
                            id = "mc2",
                            name = "Press #2",
                            isSignedOff = false,
                            signedOffDetail = "",
                        ),
                    ),
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Rahim Uddin").assertExists()
        composeTestRule.onNodeWithText("Worker").assertExists()
        composeTestRule.onNodeWithText("Skill Passport").assertExists()
        composeTestRule.onNodeWithText("Lathe 1").assertExists()
        composeTestRule.onNodeWithText("Press #2").assertExists()
        composeTestRule.onNodeWithText("Not signed off yet").assertExists()
        composeTestRule.onNodeWithText("Take Check").assertExists()
        composeTestRule.onNodeWithText("My Learning").assertExists()
    }
}
