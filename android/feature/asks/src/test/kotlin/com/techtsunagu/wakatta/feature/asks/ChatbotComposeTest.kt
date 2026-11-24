package com.techtsunagu.wakatta.feature.asks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeAnswerDto
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeCheckDto
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeSourceDto
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Ask Wakatta (M7): the three kinds of answer and the practice question. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w411dp-h1600dp")
class ChatbotComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val covered = KnowledgeAnswerDto(
        question = "Why stop the machine?",
        covered = true,
        answer = mapOf("ja" to "止めてください。", "en" to "Stop it before cleaning."),
        sources = listOf(
            KnowledgeSourceDto("s1", "safety", "For Work Safety and Health", 15, "https://example.com/x.pdf", "Source: MHLW"),
        ),
        check = KnowledgeCheckDto(
            prompt = mapOf("ja" to "何をしますか", "en" to "What do you do first?"),
            options = listOf(mapOf("en" to "Stop the machine"), mapOf("en" to "Keep it running"), mapOf("en" to "Wear gloves")),
            correctIndex = 0,
        ),
    )

    @Test
    fun aCoveredAnswerShowsItsSourcePageAndThePracticeQuestion() {
        val events = mutableListOf<ChatbotEvent>()
        composeTestRule.setContent {
            ChatbotContent(state = ChatbotUiState(turns = listOf(ChatbotTurn(1, covered))), onEvent = { events += it })
        }
        composeTestRule.onNodeWithText("Stop it before cleaning.").assertIsDisplayed()
        composeTestRule.onNodeWithText("For Work Safety and Health · p. 15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Source: MHLW").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep it running").performScrollTo().performClick()
        assertEquals(ChatbotEvent.PickOption(1, 1), events.last())
    }

    @Test
    fun aWrongPickShowsTheRightAnswer() {
        composeTestRule.setContent {
            ChatbotContent(state = ChatbotUiState(turns = listOf(ChatbotTurn(1, covered, picked = 1))), onEvent = {})
        }
        composeTestRule.onNodeWithText("Not quite. The right answer: Stop the machine").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun anEmergencyIsTheRedCard() {
        val card = KnowledgeAnswerDto("My friend is bleeding", covered = true, emergency = true, answer = mapOf("en" to "Call 119."))
        composeTestRule.setContent {
            ChatbotContent(state = ChatbotUiState(turns = listOf(ChatbotTurn(1, card))), onEvent = {})
        }
        composeTestRule.onNodeWithText("Emergency").assertIsDisplayed()
        composeTestRule.onNodeWithText("Call 119.").assertIsDisplayed()
    }

    @Test
    fun notCoveredOffersTheSenpai() {
        val events = mutableListOf<ChatbotEvent>()
        val none = KnowledgeAnswerDto("Can you recommend a ramen shop?", covered = false)
        composeTestRule.setContent {
            ChatbotContent(state = ChatbotUiState(turns = listOf(ChatbotTurn(4, none))), onEvent = { events += it })
        }
        composeTestRule.onNodeWithText("No guide covers this question.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ask my Senpai").performClick()
        assertEquals(ChatbotEvent.AskSenpai(4), events.last())
    }
}
