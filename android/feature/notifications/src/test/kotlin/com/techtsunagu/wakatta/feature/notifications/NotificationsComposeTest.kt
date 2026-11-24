package com.techtsunagu.wakatta.feature.notifications

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
class NotificationsComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun listAndPermissionCardAreShown() {
        composeTestRule.setContent {
            NotificationsContent(
                state = NotificationsUiState(
                    isLoading = false,
                    items = listOf(NotificationRow("n1", "New message from Tanaka", isUnread = true)),
                    permission = PermissionPrompt.Ask,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("New message from Tanaka").assertIsDisplayed()
        composeTestRule.onNodeWithText("Turn on").assertIsDisplayed()
    }
}
