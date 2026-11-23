package com.techtsunagu.wakatta.feature.connect

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.connect.domain.ContactRepository
import com.techtsunagu.wakatta.feature.connect.domain.MyQrDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConnectViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = newRepo()
    private val share = RecordingShare()
    // lazy: the ViewModel must be built after the rule installed the test main dispatcher
    private val vm by lazy { ConnectViewModel(repo, repo, StubEncoder(), share) }

    @Test
    fun loadShowsQrContactsAndSeededTalk() {
        vm.onEvent(ConnectEvent.Load)
        val s = vm.state.value
        assertFalse(s.isLoading)
        assertEquals(3, s.qrSide)
        assertEquals(9, s.qrModules.size)
        assertTrue(s.contacts.isEmpty())
        assertEquals(1, s.talks.size)
    }

    @Test
    fun loadFailureShowsError() {
        val failing = object : ContactRepository by repo {
            override suspend fun getMyQr(): AppResult<MyQrDto> = AppResult.Failure(AppError.Offline)
        }
        val broken = ConnectViewModel(failing, repo, StubEncoder(), share)
        broken.onEvent(ConnectEvent.Load)
        assertEquals(ConnectError.OFFLINE, broken.state.value.error)
        assertFalse(broken.state.value.isLoading)
    }

    @Test
    fun rotateKeepsTheQrAndSaysSo() = runTest {
        vm.onEvent(ConnectEvent.Load)
        vm.onEvent(ConnectEvent.Rotate)
        assertEquals(ConnectEffect.Rotated, vm.effects.first())
        assertEquals(3, vm.state.value.qrSide)
    }

    @Test
    fun shareHandsTheInviteFileToTheShareSheet() {
        vm.onEvent(ConnectEvent.Share)
        val (file, mime) = share.shared.single()
        assertEquals("text/plain", mime)
        file.delete()
    }

    @Test
    fun newTalkShowsTheNoticeOnceThenTheNameForm() {
        vm.onEvent(ConnectEvent.Load)
        vm.onEvent(ConnectEvent.NewTalk)
        assertEquals(ConnectDialog.NOTICE, vm.state.value.dialog)
        vm.onEvent(ConnectEvent.AcknowledgeNotice)
        assertEquals(ConnectDialog.NEW_TALK, vm.state.value.dialog)

        val second = ConnectViewModel(repo, repo, StubEncoder(), share)
        second.onEvent(ConnectEvent.Load)
        second.onEvent(ConnectEvent.NewTalk)
        assertEquals(ConnectDialog.NEW_TALK, second.state.value.dialog)
    }

    @Test
    fun createTalkRejectsBadNamesThenOpensTheNewThread() = runTest {
        vm.onEvent(ConnectEvent.Load)
        vm.onEvent(ConnectEvent.CreateTalk)
        assertTrue(vm.state.value.nameInvalid)

        vm.onEvent(ConnectEvent.SetTalkName("Mrs. Ito"))
        assertFalse(vm.state.value.nameInvalid)
        vm.onEvent(ConnectEvent.CreateTalk)
        val opened = vm.effects.first() as ConnectEffect.OpenThread
        assertTrue(opened.threadId.startsWith("tt_"))
        assertEquals(ConnectDialog.NONE, vm.state.value.dialog)
        assertEquals(2, vm.state.value.talks.size)
    }

    @Test
    fun listsFollowChangesMadeOnOtherScreens() = runTest {
        vm.onEvent(ConnectEvent.Load)
        repo.addContact(repo.tokenOf("Siti Rahma"))
        assertEquals(1, vm.state.value.contacts.size)
    }
}
