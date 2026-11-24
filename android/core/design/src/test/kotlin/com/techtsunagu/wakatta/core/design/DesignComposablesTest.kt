package com.techtsunagu.wakatta.core.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.TriText
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaChip
import com.techtsunagu.wakatta.core.design.component.WakattaListItem
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme
import com.techtsunagu.wakatta.core.model.WakattaStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class DesignComposablesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleTri = mapOf(
        "ja" to "安全第一",
        "en" to "Safety First",
        "bn" to "নিরাপত্তা প্রথম",
    )

    @Composable
    private fun RenderAllComposables() {
        Column {
            WakattaTopBar(title = "TopBar Title", onBackClick = {})
            WakattaCard(onClick = {}) {
                Text("Card content inside")
            }
            WakattaListItem(
                headlineContent = { Text("List item") },
                supportingContent = { Text("Supporting") },
                leadingContent = { Avatar(name = "Tanaka") },
                trailingContent = { StatusChip(status = WakattaStatus.GREEN) },
                onClick = {},
            )
            Row {
                WakattaChip(text = "Chip 1", selected = true, onClick = {})
                WakattaChip(text = "Chip 2", selected = false, onClick = {})
            }
            Row {
                StatusChip(status = WakattaStatus.GREEN)
                StatusChip(status = WakattaStatus.ORANGE)
                StatusChip(status = WakattaStatus.RED)
                StatusChip(status = WakattaStatus.WAIT)
            }
            Avatar(name = "Rahim")
            TriText(tri = sampleTri, lang = "ja")
            TriText(tri = sampleTri, lang = "en")
            TriText(tri = sampleTri, lang = "bn")
            EmptyState(title = "Empty", message = "No data available", actionLabel = "Add", onActionClick = {})
            LoadingState(message = "Loading items")
            ErrorState(message = "Error occurred", onRetry = {})
        }
    }

    @Test
    fun renderEveryComposableInLightMode() {
        composeTestRule.setContent {
            WakattaTheme(darkTheme = false) {
                RenderAllComposables()
            }
        }
    }

    @Test
    fun renderEveryComposableInDarkMode() {
        composeTestRule.setContent {
            WakattaTheme(darkTheme = true) {
                RenderAllComposables()
            }
        }
    }
}
