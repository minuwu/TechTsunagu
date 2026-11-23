package com.techtsunagu.wakatta.feature.connect.domain

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.TalkGrade
import com.techtsunagu.wakatta.core.model.ThreadSource
import com.techtsunagu.wakatta.core.model.Tri
import java.io.File

/** Personal thread ids: a Talk Thread starts with `tt_`, a Contact Thread with `tc_`. */
object PersonalThreadIds {
    const val TALK_PREFIX = "tt_"
    const val CONTACT_PREFIX = "tc_"
    fun isTalk(id: String): Boolean = id.startsWith(TALK_PREFIX)
}

// ---------------------------------------------------------------------------
// DTOs (mirror MOBILE_API_CONTRACT 4.9; wire names are still UNKNOWN there)
// ---------------------------------------------------------------------------

/** My QR: an opaque, revocable token. Never a user id. */
data class MyQrDto(val token: String)

/** What a scan shows before you connect: name and language only. */
data class ContactResolveDto(val token: String, val name: String, val language: String)

/** A Contact; [id] is also the id of the Contact Thread. */
data class ContactDto(
    val id: String,
    val name: String,
    val language: String,
    val lastMessage: String? = null,
    val unreadCount: Int = 0,
)

/** One chat bubble; [ja] and [en] hold the original and its translation (either may be blank). */
data class ContactMessageDto(
    val id: String,
    val fromMe: Boolean,
    val at: String,
    val ja: String,
    val en: String,
)

data class TalkThreadDto(val id: String, val title: String, val entryCount: Int)

data class TermPair(val from: String, val to: String)

data class ReplyOption(val en: String, val ja: String)

/**
 * One exchange in a Talk Thread. [grade] is null until the owner wrote or looked at the meaning;
 * [confirmed] is null until the other person answered.
 */
data class TalkEntryDto(
    val id: String,
    val talkId: String,
    val at: String,
    val said: String,
    val plainJa: String,
    val meaning: Tri,
    val terms: List<TermPair>,
    val needsStaff: Boolean,
    val ownText: String?,
    val skipped: Boolean,
    val grade: TalkGrade?,
    val hint: Tri,
    val replyOptions: List<ReplyOption>,
    val replyJa: String?,
    val confirmed: Boolean?,
)

/** What the owner can do to an entry (the PATCH of E-085). */
sealed interface EntryAction {
    data class Understand(val text: String) : EntryAction
    data object ShowMeaning : EntryAction
    data class Reply(val index: Int) : EntryAction
    data class Confirm(val understood: Boolean) : EntryAction
    data object RetryReply : EntryAction
}

// ---------------------------------------------------------------------------
// Repositories
// ---------------------------------------------------------------------------

interface ContactRepository {
    suspend fun getMyQr(): AppResult<MyQrDto>

    /** Makes a new token; the old one stops resolving. */
    suspend fun rotateMyQr(): AppResult<MyQrDto>

    /** Writes My QR as a small file for the share sheet. */
    suspend fun createInviteFile(): AppResult<File>

    /** Name and language for a token. Forbidden when you blocked that person, NotFound when unknown. */
    suspend fun resolve(token: String): AppResult<ContactResolveDto>

    /** Connects; an existing Contact is returned again instead of a duplicate. */
    suspend fun addContact(token: String): AppResult<ContactDto>
    suspend fun getContacts(): AppResult<List<ContactDto>>
    suspend fun getContact(id: String): AppResult<ContactDto>

    /** Silent: the blocked person is told nothing. Removes the Contact Thread. */
    suspend fun block(contactId: String): AppResult<Unit>

    /** Mock only: people whose QR you can "scan" without a camera. A real repository returns an empty list. */
    suspend fun getSamplePeople(): List<ContactResolveDto>
}

/** Contact Threads and Talk Threads; also feeds the chats list as a [ThreadSource]. */
interface PersonalThreadRepository : ThreadSource {
    suspend fun getMessages(contactId: String): AppResult<List<ContactMessageDto>>
    suspend fun sendMessage(contactId: String, text: String, srcLang: String): AppResult<ContactMessageDto>

    suspend fun getTalks(): AppResult<List<TalkThreadDto>>
    suspend fun getTalk(id: String): AppResult<TalkThreadDto>
    suspend fun createTalk(title: String): AppResult<TalkThreadDto>
    suspend fun renameTalk(id: String, title: String): AppResult<TalkThreadDto>
    suspend fun deleteTalk(id: String): AppResult<Unit>
    suspend fun getEntries(talkId: String): AppResult<List<TalkEntryDto>>

    /** Listens for one utterance and returns the new entry. */
    suspend fun listen(talkId: String): AppResult<TalkEntryDto>
    suspend fun updateEntry(talkId: String, entryId: String, action: EntryAction): AppResult<TalkEntryDto>

    /** The one-time notice before the first Talk Thread. */
    suspend fun isNoticeAcknowledged(): Boolean
    suspend fun acknowledgeNotice()
}
