package com.techtsunagu.wakatta.feature.auth

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
class SignInComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersSignInContentPlaceholder() {
        composeTestRule.setContent {
            SignInContent(
                state = SignInUiState(),
                onEvent = {},
            )
        }


        composeTestRule.onNodeWithText("Wakatta?").assertIsDisplayed()
        composeTestRule.onNodeWithText("新しいショップを登録").assertExists()
    }
}
