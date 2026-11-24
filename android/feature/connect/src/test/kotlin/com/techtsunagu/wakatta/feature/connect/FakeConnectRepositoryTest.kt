package com.techtsunagu.wakatta.feature.connect

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.TalkGrade
import com.techtsunagu.wakatta.feature.connect.domain.EntryAction
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadIds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeConnectRepositoryTest {

    private val repo = newRepo()

    private suspend fun connectSiti() = repo.addContact(repo.tokenOf("Siti Rahma")).value()

    @Test
    fun myQrIsAnOpaqueTokenAndRotationChangesIt() = runTest {
        val first = repo.getMyQr().value().token
        assertTrue(Regex("WK-[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6}").matches(first))
        assertFalse(first.contains("u_"))
        assertNotEquals(first, repo.rotateMyQr().value().token)
        assertEquals(repo.getMyQr().value().token, repo.getMyQr().value().token)
        // someone scanning the replaced QR is told it was replaced, not that it was never there
        assertEquals(AppResult.Failure(com.techtsunagu.wakatta.core.model.AppError.Gone), repo.resolve(first))
    }

    @Test
    fun resolveShowsNameAndLanguageOnly() = runTest {
        val person = repo.resolve(repo.tokenOf("Nguyen Van An")).value()
        assertEquals("Nguyen Van An", person.name)
        assertEquals("vi", person.language)
        assertEquals(AppResult.Failure(AppError.NotFound), repo.resolve("nothing"))
    }

    @Test
    fun addingTheSamePersonTwiceKeepsOneContactThread() = runTest {
        val first = connectSiti()
        val again = connectSiti()
        assertEquals(first.id, again.id)
        assertTrue(first.id.startsWith(PersonalThreadIds.CONTACT_PREFIX))
        assertEquals(1, repo.getContacts().value().size)
        assertEquals(1, first.unreadCount)
        assertEquals(1, repo.getMessages(first.id).value().size)
        assertEquals(0, repo.getContact(first.id).value().unreadCount)
    }

    @Test
    fun sendingTranslatesAndTheContactAnswers() = runTest {
        val contact = connectSiti()
        val mine = repo.sendMessage(contact.id, "Thank you", "en").value()
        assertEquals("ありがとうございます。", mine.ja)
        assertEquals("Thank you", mine.en)
        assertEquals(3, repo.getMessages(contact.id).value().size)
        assertEquals(AppResult.Failure(AppError.Validation(listOf("text"))), repo.sendMessage(contact.id, " ", "en"))
    }

    @Test
    fun blockingIsSilentAndRemovesTheThread() = runTest {
        val token = repo.tokenOf("Siti Rahma")
        val contact = connectSiti()
        assertTrue(repo.block(contact.id) is AppResult.Success)

        assertEquals(AppResult.Failure(AppError.Forbidden), repo.resolve(token))
        assertEquals(AppResult.Failure(AppError.Forbidden), repo.addContact(token))
        assertEquals(AppResult.Failure(AppError.NotFound), repo.sendMessage(contact.id, "hi", "en"))
        assertTrue(repo.getContacts().value().isEmpty())
        assertFalse(repo.getThreads().first().any { it.id == contact.id })
    }

    @Test
    fun talkTitleMustBeOneToFortyCharacters() = runTest {
        val validation = AppResult.Failure(AppError.Validation(listOf("title")))
        assertEquals(validation, repo.createTalk("  "))
        assertEquals(validation, repo.createTalk("x".repeat(41)))
        val talk = repo.createTalk("x".repeat(40)).value()
        assertTrue(talk.id.startsWith(PersonalThreadIds.TALK_PREFIX))
        assertEquals(validation, repo.renameTalk(talk.id, ""))
        assertEquals("Mr. Sato", repo.renameTalk(talk.id, " Mr. Sato ").value().title)
    }

    @Test
    fun deletingATalkIsAHardDeleteAndTalksNeverNotify() = runTest {
        val talk = repo.createTalk("Mrs. Ito").value()
        repo.listen(talk.id)
        val summaries = repo.getThreads().first()
        assertTrue(summaries.filter { PersonalThreadIds.isTalk(it.id) }.all { it.unreadCount == 0 })

        assertTrue(repo.deleteTalk(talk.id) is AppResult.Success)
        assertEquals(AppResult.Failure(AppError.NotFound), repo.getTalk(talk.id))
        assertEquals(AppResult.Failure(AppError.NotFound), repo.getEntries(talk.id))
        assertFalse(repo.getThreads().first().any { it.id == talk.id })
    }

    @Test
    fun gradingComparesTheOwnersWordsWithTheMeaning() = runTest {
        val talk = repo.createTalk("Mr. Yamada").value()
        val entry = repo.listen(talk.id).value()
        suspend fun grade(text: String) = repo.updateEntry(talk.id, entry.id, EntryAction.Understand(text)).value().grade

        assertEquals(TalkGrade.GREEN, grade("His knee hurts so he will not walk today"))
        assertEquals(TalkGrade.ORANGE, grade("something about his knee"))
        assertEquals(TalkGrade.RED, grade("he likes bananas"))
    }

    @Test
    fun entryActionsFollowTheExchangeSteps() = runTest {
        val talk = repo.createTalk("Mr. Yamada").value()
        val id = repo.listen(talk.id).value().id
        suspend fun update(action: EntryAction) = repo.updateEntry(talk.id, id, action)

        assertEquals(AppResult.Failure(AppError.Validation(listOf("text"))), update(EntryAction.Understand(" ")))
        assertEquals(AppResult.Failure(AppError.Validation(listOf("grade"))), update(EntryAction.Reply(0)))

        val skipped = update(EntryAction.ShowMeaning).value()
        assertTrue(skipped.skipped)
        assertEquals(TalkGrade.ORANGE, skipped.grade)

        assertEquals(AppResult.Failure(AppError.Validation(listOf("index"))), update(EntryAction.Reply(9)))
        assertEquals(null, update(EntryAction.Reply(0)).value().confirmed)
        assertEquals(false, update(EntryAction.Confirm(false)).value().confirmed)
        val retried = update(EntryAction.RetryReply).value()
        assertEquals(null, retried.replyJa)
        assertEquals(AppResult.Failure(AppError.Validation(listOf("reply"))), update(EntryAction.Confirm(true)))
    }

    @Test
    fun listeningCyclesThroughTheCannedUtterances() = runTest {
        val talk = repo.createTalk("Mr. Yamada").value()
        val said = List(4) { repo.listen(talk.id).value().said }
        assertEquals(said[0], said[3])
        assertEquals(3, said.toSet().size)
        assertEquals(4, repo.getTalk(talk.id).value().entryCount)
    }

    @Test
    fun addAndDeleteEntry() = runTest {
        val talk = repo.createTalk("Mr. Yamada").value()
        val entry = repo.addEntry(talk.id, "Hello from mic").value()
        assertEquals("Hello from mic", entry.said)
        assertEquals(1, repo.getEntries(talk.id).value().size)

        assertTrue(repo.deleteEntry(talk.id, entry.id) is AppResult.Success)
        assertTrue(repo.getEntries(talk.id).value().isEmpty())
        assertEquals(AppResult.Failure(AppError.NotFound), repo.deleteEntry(talk.id, entry.id))
    }

    @Test
    fun noticeIsStoredOnce() = runTest {
        assertFalse(repo.isNoticeAcknowledged())
        repo.acknowledgeNotice()
        assertTrue(repo.isNoticeAcknowledged())
    }

    @Test
    fun inviteFileIsAQrImage() = runTest {
        val file = repo.createInviteFile().value()
        val bytes = file.readBytes()
        file.delete()
        assertEquals("wakatta-my-qr.png", file.name)
        assertEquals("PNG", String(bytes, 1, 3))
        assertTrue(bytes.size > 100)
    }

    @Test
    fun myQrIsTheAppLinkAPhoneCameraOpens() = runTest {
        val token = repo.getMyQr().value().token
        val link = com.techtsunagu.wakatta.feature.connect.data.inviteLink(token)
        assertEquals("https://wakatta-api.agreeablestone-840cfb57.japaneast.azurecontainerapps.io/c/$token", link)
        assertEquals(com.techtsunagu.wakatta.core.media.QrPayload.MyQr(token), com.techtsunagu.wakatta.core.media.QrPayload.parse(link))
    }
}
