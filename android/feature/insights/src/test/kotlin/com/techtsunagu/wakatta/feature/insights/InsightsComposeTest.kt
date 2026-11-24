package com.techtsunagu.wakatta.feature.insights

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpsData
import com.techtsunagu.wakatta.feature.insights.domain.LearningHistory
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrix
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class InsightsComposeTest {

    @get:Rule
    val rule = createComposeRule()

    private fun shown(text: String) = rule.onAllNodesWithText(text)[0].assertIsDisplayed()

    // ---- Analytics ----

    @Test
    fun analytics_loading() {
        rule.setContent { AnalyticsContent(AnalyticsUiState(isLoading = true), {}) }
        shown("Loading...")
    }

    @Test
    fun analytics_empty() {
        rule.setContent {
            AnalyticsContent(AnalyticsUiState(isLoading = false, data = sampleAnalytics().copy(sent = 0, terms = emptyList())), {})
        }
        shown("Nothing to analyse yet. Numbers appear after Workers answer their first Checks.")
    }

    @Test
    fun analytics_error() {
        rule.setContent { AnalyticsContent(AnalyticsUiState(isLoading = false, error = AppError.Offline), {}) }
        shown("Try again")
    }

    @Test
    fun analytics_success() {
        rule.setContent { AnalyticsContent(AnalyticsUiState(isLoading = false, data = sampleAnalytics(), selectedPoint = 3), {}) }
        shown("Understanding rate, 4 weeks")
        shown("78%")
    }

    // ---- Skills ----

    @Test
    fun skills_loading() {
        rule.setContent { SkillsContent(SkillsUiState(isLoading = true), {}) }
        shown("Loading...")
    }

    @Test
    fun skills_empty() {
        rule.setContent { SkillsContent(SkillsUiState(isLoading = false, matrix = SkillMatrix(emptyList(), emptyList())), {}) }
        shown("No Workers or Machines to show yet.")
    }

    @Test
    fun skills_error() {
        rule.setContent { SkillsContent(SkillsUiState(isLoading = false, error = AppError.Forbidden), {}) }
        shown("Only a Foreman can see this page. Ask your Foreman if you need this information.")
    }

    @Test
    fun skills_success() {
        rule.setContent { SkillsContent(SkillsUiState(isLoading = false, matrix = sampleMatrix(), language = "en"), {}) }
        shown("Lathe 1")
        shown("9 d left")
    }

    // ---- Follow-ups ----

    @Test
    fun followUps_loading() {
        rule.setContent { FollowUpsContent(FollowUpsUiState(isLoading = true), {}) }
        shown("Loading...")
    }

    @Test
    fun followUps_empty() {
        rule.setContent {
            FollowUpsContent(FollowUpsUiState(isLoading = false, data = FollowUpsData(emptyList(), sampleStats)), {})
        }
        shown("No Follow-ups. Everyone understands.")
    }

    @Test
    fun followUps_error() {
        rule.setContent { FollowUpsContent(FollowUpsUiState(isLoading = false, error = AppError.Server), {}) }
        shown("Something went wrong and the page could not load. Wait a moment, then tap Try again.")
    }

    @Test
    fun followUps_success() {
        rule.setContent { FollowUpsContent(FollowUpsUiState(isLoading = false, data = sampleFollowUps()), {}) }
        shown("3号機オイル")
        shown("Wrong: Air on Machine 2")
    }

    // ---- Learning ----

    @Test
    fun learning_loading() {
        rule.setContent { LearningContent(LearningUiState(isLoading = true), {}) }
        shown("Loading...")
    }

    @Test
    fun learning_empty() {
        rule.setContent {
            LearningContent(LearningUiState(isLoading = false, history = LearningHistory("u", "Rahim Uddin", 0, 0, 0, emptyList())), {})
        }
        shown("Nothing here yet. Missed and learned words will show here.")
    }

    @Test
    fun learning_error() {
        rule.setContent { LearningContent(LearningUiState(isLoading = false, error = AppError.NotFound), {}) }
        shown("We could not find this person. Go back and choose again.")
    }

    @Test
    fun learning_success() {
        rule.setContent { LearningContent(LearningUiState(isLoading = false, history = sampleHistory(), isForeman = true), {}) }
        shown("Learning history: Rahim")
        shown("金型交換")
        shown("die change")
    }
}
