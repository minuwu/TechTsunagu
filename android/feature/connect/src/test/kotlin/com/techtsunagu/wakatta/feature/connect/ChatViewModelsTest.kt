package com.techtsunagu.wakatta.feature.connect

import com.techtsunagu.wakatta.core.media.DictationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatViewModelsTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = newRepo()

    // ----- scan -----

    @Test
    fun scanLoadsSamplePeopleAndPickingOneAsksToConfirm() = runTest {
        val vm = ConnectScanViewModel(repo, StubScanner())
        assertEquals(4, vm.state.value.samples.size)
        assertFalse(vm.state.value.isLoading)

        vm.onEvent(ConnectScanEvent.PickSample(repo.tokenOf("Siti Rahma")))
        assertEquals("Siti Rahma", vm.state.value.pending?.name)

        vm.onEvent(ConnectScanEvent.Confirm)
        val opened = vm.effects.first() as ConnectScanEffect.Opened
        assertTrue(opened.threadId.startsWith("tc_"))
        assertNull(vm.state.value.pending)
    }

    @Test
    fun scanCancelDropsThePendingPerson() = runTest {
        val vm = ConnectScanViewModel(repo, StubScanner())
        vm.onEvent(ConnectScanEvent.PickSample(repo.tokenOf("Siti Rahma")))
        vm.onEvent(ConnectScanEvent.Cancel)
        assertNull(vm.state.value.pending)
        assertTrue(repo.getContacts().value().isEmpty())
    }

    @Test
    fun manualCodeAcceptsBareTokenAndRejectsJunk() = runTest {
        val vm = ConnectScanViewModel(repo, StubScanner())
        vm.onEvent(ConnectScanEvent.SetCode("not a code"))
        vm.onEvent(ConnectScanEvent.ReadCode)
        assertEquals(ConnectError.BAD_CODE, vm.state.value.error)

        vm.onEvent(ConnectScanEvent.SetCode(repo.tokenOf("Nguyen Van An")))
        assertNull(vm.state.value.error)
        vm.onEvent(ConnectScanEvent.ReadCode)
        assertEquals("Nguyen Van An", vm.state.value.pending?.name)
    }

    @Test
    fun scannerResultResolvesAndUnknownOrBlockedShowErrors() = runTest {
        val token = repo.tokenOf("Siti Rahma")
        val vm = ConnectScanViewModel(repo, StubScanner(flowOf("wk:unknowntoken")))
        assertEquals(ConnectError.NOT_FOUND, vm.state.value.error)

        val contact = repo.addContact(token).value()
        repo.block(contact.id)
        vm.onEvent(ConnectScanEvent.PickSample(token))
        assertEquals(ConnectError.BLOCKED, vm.state.value.error)
    }

    @Test
    fun scannerResultWithAKnownTokenAsksToConfirm() = runTest {
        val vm = ConnectScanViewModel(repo, StubScanner(flowOf("wk:" + repo.tokenOf("田辺 舞"))))
        assertEquals("田辺 舞", vm.state.value.pending?.name)
    }

    // ----- contact thread -----

    private suspend fun contactId() = repo.addContact(repo.tokenOf("Siti Rahma")).value().id

    @Test
    fun contactChatLoadsTitleAndGreeting() = runTest {
        val vm = PChatViewModel(repo, repo)
        vm.onEvent(PChatEvent.Init(contactId(), "en"))
        val s = vm.state.value
        assertEquals("Siti Rahma", s.title)
        assertEquals(1, s.messages.size)
        assertFalse(s.isLoading)
    }

    @Test
    fun contactChatSendAppendsMineAndReply() = runTest {
        val vm = PChatViewModel(repo, repo)
        vm.onEvent(PChatEvent.Init(contactId(), "en"))
        vm.onEvent(PChatEvent.SetInput("Thank you"))
        vm.onEvent(PChatEvent.Send)
        assertEquals(3, vm.state.value.messages.size)
        assertEquals("", vm.state.value.input)
    }

    @Test
    fun contactChatBlockEmitsBlockedAndRemovesTheContact() = runTest {
        val vm = PChatViewModel(repo, repo)
        vm.onEvent(PChatEvent.Init(contactId(), "en"))
        vm.onEvent(PChatEvent.AskBlock)
        assertTrue(vm.state.value.confirmBlock)
        vm.onEvent(PChatEvent.ConfirmBlock)
        assertEquals(PChatEffect.Blocked, vm.effects.first())
        assertTrue(repo.getContacts().value().isEmpty())
    }

    @Test
    fun contactChatUnknownThreadShowsNotFound() = runTest {
        val vm = PChatViewModel(repo, repo)
        vm.onEvent(PChatEvent.Init("tc_missing", "en"))
        assertEquals(ConnectError.NOT_FOUND, vm.state.value.error)
        vm.onEvent(PChatEvent.AskBlock)
        vm.onEvent(PChatEvent.DismissBlock)
        assertFalse(vm.state.value.confirmBlock)
    }

    // ----- talk thread -----

    private fun talkVm(
        speaker: RecordingSpeaker = RecordingSpeaker(),
        dictation: StubDictationSource = StubDictationSource(events = flowOf(DictationEvent.Final("膝のいたかけん、今日は散歩はせんばい。"))),
    ) = TalkViewModel(repo, speaker, dictation)

    @Test
    fun talkLoadsTheSeededExchange() {
        val vm = talkVm()
        vm.onEvent(TalkEvent.Init("tt_demo", "en"))
        assertEquals("山田さん（施設）", vm.state.value.title)
        assertEquals(1, vm.state.value.entries.size)
    }

    @Test
    fun talkListenThenUnderstandReplyAndConfirm() = runTest {
        val vm = talkVm()
        vm.onEvent(TalkEvent.Init("tt_demo", "en"))
        vm.onEvent(TalkEvent.Listen)
        val entry = vm.state.value.entries.last()
        assertEquals(2, vm.state.value.entries.size)
        assertFalse(vm.state.value.isListening)

        vm.onEvent(TalkEvent.Understand(entry.id, "his knee hurts, no walk today"))
        vm.onEvent(TalkEvent.Reply(entry.id, 0))
        vm.onEvent(TalkEvent.Confirm(entry.id, true))
        val done = vm.state.value.entries.last()
        assertNotNull(done.grade)
        assertEquals(true, done.confirmed)
    }

    @Test
    fun talkListenWithDictationAddsEntry() = runTest {
        val dictation = StubDictationSource(
            available = true,
            events = flowOf(DictationEvent.Final("こんにちは")),
        )
        val vm = TalkViewModel(repo, RecordingSpeaker(), dictation)
        vm.onEvent(TalkEvent.Init("tt_demo", "en"))
        vm.onEvent(TalkEvent.Listen)
        assertFalse(vm.state.value.isListening)
        assertEquals("こんにちは", vm.state.value.entries.last().said)
        assertEquals(listOf("ja-JP"), dictation.started)
        assertTrue(dictation.isStopped)
    }

    @Test
    fun talkWithoutDictationSendsNothing() = runTest {
        val vm = talkVm(dictation = StubDictationSource(available = false))
        vm.onEvent(TalkEvent.Init("tt_demo", "en"))
        vm.onEvent(TalkEvent.Listen)
        assertEquals(TalkEffect.Failed(ConnectError.NO_SPEECH), vm.effects.first())
        assertEquals(1, vm.state.value.entries.size)
    }

    @Test
    fun talkStopKeepsTheWordsSpokenBeforeIt() = runTest {
        val events = MutableSharedFlow<DictationEvent>()
        val vm = talkVm(dictation = StubDictationSource(events = events))
        vm.onEvent(TalkEvent.Init("tt_demo", "en"))
        vm.onEvent(TalkEvent.Listen)
        vm.onEvent(TalkEvent.StopListening)
        assertFalse(vm.state.value.isListening)
        events.emit(DictationEvent.Final("はい"))
        assertEquals("はい", vm.state.value.entries.last().said)
    }

    @Test
    fun talkSayReadsAloudOrReportsNoSpeech() = runTest {
        val speaker = RecordingSpeaker()
        val vm = talkVm(speaker)
        vm.onEvent(TalkEvent.Init("tt_demo", "en"))
        vm.onEvent(TalkEvent.Say("x_seed", reply = true))
        assertEquals(1, speaker.spoken.size)

        val silent = talkVm(RecordingSpeaker(available = false))
        silent.onEvent(TalkEvent.Init("tt_demo", "en"))
        silent.onEvent(TalkEvent.Say("x_seed", reply = false))
        assertEquals(TalkEffect.Failed(ConnectError.NO_SPEECH), silent.effects.first())
    }

    @Test
    fun talkRenameValidatesThenSaves() {
        val vm = talkVm()
        vm.onEvent(TalkEvent.Init("tt_demo", "en"))
        vm.onEvent(TalkEvent.OpenRename)
        vm.onEvent(TalkEvent.SetName(""))
        vm.onEvent(TalkEvent.SaveName)
        assertTrue(vm.state.value.nameInvalid)

        vm.onEvent(TalkEvent.SetName("Mr. Yamada"))
        vm.onEvent(TalkEvent.SaveName)
        assertEquals("Mr. Yamada", vm.state.value.title)
        assertEquals(TalkDialog.NONE, vm.state.value.dialog)
    }

    @Test
    fun talkDeleteEmitsDeletedAndRemovesTheThread() = runTest {
        val vm = talkVm()
        vm.onEvent(TalkEvent.Init("tt_demo", "en"))
        vm.onEvent(TalkEvent.OpenDelete)
        vm.onEvent(TalkEvent.ConfirmDelete)
        assertEquals(TalkEffect.Deleted, vm.effects.first())
        assertTrue(repo.getTalks().value().isEmpty())
    }
}
