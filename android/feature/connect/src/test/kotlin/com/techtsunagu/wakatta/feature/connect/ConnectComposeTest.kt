package com.techtsunagu.wakatta.feature.connect

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme
import com.techtsunagu.wakatta.feature.connect.data.TalkScript
import com.techtsunagu.wakatta.feature.connect.data.toEntry
import com.techtsunagu.wakatta.feature.connect.domain.ContactDto
import com.techtsunagu.wakatta.feature.connect.domain.ContactMessageDto
import com.techtsunagu.wakatta.feature.connect.domain.ContactResolveDto
import com.techtsunagu.wakatta.feature.connect.domain.TalkThreadDto
import com.techtsunagu.wakatta.core.model.TalkGrade
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ConnectComposeTest {

    @get:Rule
    val rule = createComposeRule()

    private fun show(content: @Composable () -> Unit) = rule.setContent { WakattaTheme { content() } }

    private fun assertText(text: String) = rule.onNodeWithText(text).assertIsDisplayed()

    /** For rows below the fold of a lazy list. */
    private fun assertAfterScroll(text: String) {
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(text))
        assertText(text)
    }

    private val loaded = ConnectUiState(isLoading = false, qrSide = 3, qrModules = List(9) { it % 2 == 0 })
    private val siti = ContactResolveDto("tok", "Siti Rahma", "id")
    private val greeting = ContactMessageDto("m1", false, "09:00", "はじめまして。", "Nice to meet you.")
    private val entry = TalkScript.utterances[1].toEntry("e1", "tt_demo", "09:12")

    // ----- connect -----

    @Test
    fun connectLoading() {
        show { ConnectContent(ConnectUiState(isLoading = true), {}) }
        assertText("Loading...")
    }

    @Test
    fun connectEmptyListsExplainWhatToDo() {
        show { ConnectContent(loaded, {}) }
        assertText("My QR")
        assertAfterScroll("No contacts yet. Scan someone's QR to connect.")
    }

    @Test
    fun connectError() {
        show { ConnectContent(ConnectUiState(isLoading = false, error = ConnectError.OFFLINE), {}) }
        assertText("No internet connection, so this could not be done. Check your connection and try again.")
    }

    @Test
    fun connectSuccessListsContactsAndTalks() {
        val state = loaded.copy(
            contacts = listOf(ContactDto("tc_siti", "Siti Rahma", "id")),
            talks = listOf(TalkThreadDto("tt_demo", "Mr. Yamada", 1)),
        )
        show { ConnectContent(state, {}) }
        assertAfterScroll("Siti Rahma")
    }

    @Test
    fun connectShowsThePrivacyNoticeDialog() {
        show { ConnectContent(loaded.copy(dialog = ConnectDialog.NOTICE), {}) }
        assertText("Before you record")
    }

    // ----- scan -----

    @Test
    fun scanLoading() {
        show { ConnectScanContent(ConnectScanUiState(isLoading = true), {}) }
        assertText("Loading...")
    }

    @Test
    fun scanEmptyStillOffersTheCodeField() {
        show { ConnectScanContent(ConnectScanUiState(isLoading = false), {}) }
        assertText("Type the code from the other person's QR, or tap a person below to try a scan.")
    }

    @Test
    fun scanError() {
        show { ConnectScanContent(ConnectScanUiState(isLoading = false, error = ConnectError.BAD_CODE), {}) }
        assertText("This is not a Wakatta? QR code. Check the code and try again.")
    }

    @Test
    fun scanSuccessAsksToConfirmThePerson() {
        show { ConnectScanContent(ConnectScanUiState(isLoading = false, samples = listOf(siti), pending = siti), {}) }
        assertText("Connect with Siti Rahma?")
    }

    // ----- contact thread -----

    @Test
    fun chatLoading() {
        show { PChatContent(PChatUiState(isLoading = true), {}) }
        assertText("Loading...")
    }

    @Test
    fun chatEmpty() {
        show { PChatContent(PChatUiState(isLoading = false, title = "Siti Rahma"), {}) }
        assertText("No messages yet. Say hello.")
    }

    @Test
    fun chatError() {
        show { PChatContent(PChatUiState(isLoading = false, error = ConnectError.NOT_FOUND), {}) }
        assertText("We could not find it. Check the QR or the code and try again.")
    }

    @Test
    fun chatSuccessShowsTranslationNote() {
        show { PChatContent(PChatUiState(isLoading = false, title = "Siti Rahma", messages = listOf(greeting)), {}) }
        assertText("Nice to meet you.")
        assertText("Original: はじめまして。")
    }

    // ----- talk thread -----

    @Test
    fun talkLoading() {
        show { TalkContent(TalkUiState(isLoading = true), {}) }
        assertText("Loading...")
    }

    @Test
    fun talkEmpty() {
        show { TalkContent(TalkUiState(isLoading = false, title = "Mr. Yamada"), {}) }
        assertText("Tap Listen and hold the phone near the person.")
    }

    @Test
    fun talkError() {
        show { TalkContent(TalkUiState(isLoading = false, error = ConnectError.SERVER), {}) }
        assertText("The server has a problem and could not finish. Try again later.")
    }

    @Test
    fun talkSuccessAsksForTheOwnersWords() {
        show { TalkContent(TalkUiState(isLoading = false, title = "Mr. Yamada", entries = listOf(entry)), {}) }
        assertText("What did they say? Write it in your own words")
    }

    @Test
    fun talkSuccessShowsGradeAndMeaning() {
        val graded = entry.copy(ownText = "more tea", grade = TalkGrade.GREEN)
        show { TalkContent(TalkUiState(isLoading = false, title = "Mr. Yamada", entries = listOf(graded)), {}) }
        assertText("Yes, you understood.")
    }
}
