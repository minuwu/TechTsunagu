package com.techtsunagu.wakatta.feature.connect.data

import android.content.Context
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.TalkGrade
import com.techtsunagu.wakatta.core.model.ThreadSummary
import com.techtsunagu.wakatta.feature.connect.domain.ContactDto
import com.techtsunagu.wakatta.feature.connect.domain.ContactMessageDto
import com.techtsunagu.wakatta.feature.connect.domain.ContactRepository
import com.techtsunagu.wakatta.feature.connect.domain.ContactResolveDto
import com.techtsunagu.wakatta.feature.connect.domain.EntryAction
import com.techtsunagu.wakatta.feature.connect.domain.MyQrDto
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadIds
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadRepository
import com.techtsunagu.wakatta.feature.connect.domain.TalkEntryDto
import com.techtsunagu.wakatta.feature.connect.domain.TalkThreadDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.SecureRandom
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val MAX_TITLE = 40
private const val NOTICE_KEY = "talk_notice_ack"

private class Person(val key: String, val name: String, val language: String, val token: String) {
    val threadId get() = PersonalThreadIds.CONTACT_PREFIX + key
}

private class ContactState(val person: Person, var unread: Int, val messages: MutableList<ContactMessageDto>)

private class TalkState(val id: String, var title: String, val entries: MutableList<TalkEntryDto>)

/**
 * In-memory Contacts, Contact Threads and Talk Threads. One class because the chats list
 * ([getThreads]) mixes both kinds. All user-scoped: nothing here is visible to the Foreman.
 */
@Singleton
class FakeConnectRepository(
    private val context: Context?,
    private val simulatedLatencyMs: Long,
) : ContactRepository, PersonalThreadRepository {

    @Inject
    constructor(@ApplicationContext context: Context) : this(context, 300L)

    // shortcut: seeded people stand in for accounts; the real app resolves a token on the server
    private val people = listOf(
        Person("mai", "田辺 舞", "ja", "WK-Q7KM2X"),
        Person("sato", "佐藤 美咲", "ja", "WK-ZC8NPW"),
        Person("nguyen", "Nguyen Van An", "vi", "WK-B3TD6Q"),
        Person("siti", "Siti Rahma", "id", "WK-HW5Y2N"),
    )

    private val lock = Any()
    private val random = SecureRandom()
    private var myToken = newToken()
    private var idCounter = 0
    private var listenCounter = 0
    private var noticeAcknowledged = false
    private val blockedTokens = mutableSetOf<String>()
    private val retiredTokens = mutableSetOf<String>()
    private val contacts = mutableListOf<ContactState>()
    private val talks = mutableListOf(seedTalk())
    private val threads = MutableStateFlow(summaries())

    override fun getThreads(): Flow<List<ThreadSummary>> = threads.asStateFlow()

    // ----- My QR and Contacts -----

    override suspend fun getMyQr(): AppResult<MyQrDto> {
        delay(simulatedLatencyMs)
        return AppResult.Success(MyQrDto(synchronized(lock) { myToken }))
    }

    override suspend fun rotateMyQr(): AppResult<MyQrDto> {
        delay(simulatedLatencyMs)
        return AppResult.Success(MyQrDto(synchronized(lock) { newToken().also { retiredTokens += myToken; myToken = it } }))
    }

    override suspend fun createInviteFile(): AppResult<File> {
        delay(simulatedLatencyMs)
        return try {
            val dir = context?.cacheDir ?: File(System.getProperty("java.io.tmpdir") ?: ".")
            val file = writeInviteImage(context, synchronized(lock) { myToken }, null, dir)
            AppResult.Success(file)
        } catch (e: java.io.IOException) {
            AppResult.Failure(AppError.Unknown(e.message))
        }
    }

    override suspend fun resolve(token: String): AppResult<ContactResolveDto> {
        delay(simulatedLatencyMs)
        return synchronized(lock) { personFor(token) }.map { ContactResolveDto(it.token, it.name, it.language) }
    }

    override suspend fun addContact(token: String): AppResult<ContactDto> {
        delay(simulatedLatencyMs)
        return synchronized(lock) {
            personFor(token).map { person ->
                val state = contacts.firstOrNull { it.person.key == person.key } ?: newContact(person)
                publish()
                state.toDto()
            }
        }
    }

    override suspend fun getContacts(): AppResult<List<ContactDto>> {
        delay(simulatedLatencyMs)
        return AppResult.Success(synchronized(lock) { contacts.map { it.toDto() } })
    }

    override suspend fun getContact(id: String): AppResult<ContactDto> {
        delay(simulatedLatencyMs)
        val state = synchronized(lock) { contacts.firstOrNull { it.person.threadId == id } }
        return state?.let { AppResult.Success(it.toDto()) } ?: AppResult.Failure(AppError.NotFound)
    }

    override suspend fun block(contactId: String): AppResult<Unit> {
        delay(simulatedLatencyMs)
        return synchronized(lock) {
            val state = contacts.firstOrNull { it.person.threadId == contactId }
                ?: return@synchronized AppResult.Failure(AppError.NotFound)
            contacts.remove(state)
            blockedTokens += state.person.token
            publish()
            AppResult.Success(Unit)
        }
    }

    override suspend fun getSamplePeople(): List<ContactResolveDto> =
        people.map { ContactResolveDto(it.token, it.name, it.language) }

    // ----- Contact Threads -----

    override suspend fun getMessages(contactId: String): AppResult<List<ContactMessageDto>> {
        delay(simulatedLatencyMs)
        return synchronized(lock) {
            val state = contacts.firstOrNull { it.person.threadId == contactId }
                ?: return@synchronized AppResult.Failure(AppError.NotFound)
            state.unread = 0
            publish()
            AppResult.Success(state.messages.toList())
        }
    }

    override suspend fun deleteMessage(contactId: String, messageId: String): AppResult<Unit> = synchronized(lock) {
        val state = contacts.firstOrNull { it.person.threadId == contactId } ?: return AppResult.Failure(AppError.NotFound)
        state.messages.removeAll { it.id == messageId }
        AppResult.Success(Unit)
    }

    override suspend fun clearMessages(contactId: String): AppResult<Unit> = synchronized(lock) {
        val state = contacts.firstOrNull { it.person.threadId == contactId } ?: return AppResult.Failure(AppError.NotFound)
        state.messages.clear()
        AppResult.Success(Unit)
    }

    override suspend fun sendMessage(contactId: String, text: String, srcLang: String): AppResult<ContactMessageDto> {
        delay(simulatedLatencyMs)
        if (text.isBlank()) return AppResult.Failure(AppError.Validation(listOf("text")))
        return synchronized(lock) {
            val state = contacts.firstOrNull { it.person.threadId == contactId }
                ?: return@synchronized AppResult.Failure(AppError.NotFound)
            val clean = text.trim()
            val mine = ContactMessageDto(
                id = nextId("cm"),
                fromMe = true,
                at = now(),
                ja = if (srcLang == "ja") clean else TalkScript.translateToJa(clean),
                en = if (srcLang == "ja") "" else clean,
            )
            val reply = TalkScript.replies[state.messages.size % TalkScript.replies.size]
            state.messages += mine
            state.messages += ContactMessageDto(nextId("cm"), false, now(), reply.first, reply.second)
            publish()
            AppResult.Success(mine)
        }
    }

    // ----- Talk Threads -----

    override suspend fun getTalks(): AppResult<List<TalkThreadDto>> {
        delay(simulatedLatencyMs)
        return AppResult.Success(synchronized(lock) { talks.map { it.toDto() } })
    }

    override suspend fun getTalk(id: String): AppResult<TalkThreadDto> {
        delay(simulatedLatencyMs)
        val talk = synchronized(lock) { talks.firstOrNull { it.id == id } }
        return talk?.let { AppResult.Success(it.toDto()) } ?: AppResult.Failure(AppError.NotFound)
    }

    override suspend fun createTalk(title: String): AppResult<TalkThreadDto> {
        delay(simulatedLatencyMs)
        val clean = title.trim()
        if (clean.isEmpty() || clean.length > MAX_TITLE) return AppResult.Failure(AppError.Validation(listOf("title")))
        return synchronized(lock) {
            val talk = TalkState(nextId(PersonalThreadIds.TALK_PREFIX), clean, mutableListOf())
            talks.add(0, talk)
            publish()
            AppResult.Success(talk.toDto())
        }
    }

    override suspend fun renameTalk(id: String, title: String): AppResult<TalkThreadDto> {
        delay(simulatedLatencyMs)
        val clean = title.trim()
        if (clean.isEmpty() || clean.length > MAX_TITLE) return AppResult.Failure(AppError.Validation(listOf("title")))
        return synchronized(lock) {
            val talk = talks.firstOrNull { it.id == id } ?: return@synchronized AppResult.Failure(AppError.NotFound)
            talk.title = clean
            publish()
            AppResult.Success(talk.toDto())
        }
    }

    /** Hard delete: the thread and every entry go. */
    override suspend fun deleteTalk(id: String): AppResult<Unit> {
        delay(simulatedLatencyMs)
        return synchronized(lock) {
            if (!talks.removeAll { it.id == id }) return@synchronized AppResult.Failure(AppError.NotFound)
            publish()
            AppResult.Success(Unit)
        }
    }

    override suspend fun getEntries(talkId: String): AppResult<List<TalkEntryDto>> {
        delay(simulatedLatencyMs)
        val talk = synchronized(lock) { talks.firstOrNull { it.id == talkId } }
            ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(synchronized(lock) { talk.entries.toList() })
    }

    override suspend fun addEntry(talkId: String, text: String): AppResult<TalkEntryDto> {
        delay(simulatedLatencyMs)
        return synchronized(lock) {
            val talk = talks.firstOrNull { it.id == talkId } ?: return@synchronized AppResult.Failure(AppError.NotFound)
            val utterance = TalkScript.utterances.firstOrNull { it.said == text }
                ?: TalkScript.utterances[listenCounter++ % TalkScript.utterances.size]
            val entry = utterance.toEntry(nextId("x"), talkId, now()).copy(said = text)
            talk.entries += entry
            publish()
            AppResult.Success(entry)
        }
    }

    override suspend fun deleteEntry(talkId: String, entryId: String): AppResult<Unit> {
        delay(simulatedLatencyMs)
        return synchronized(lock) {
            val talk = talks.firstOrNull { it.id == talkId } ?: return@synchronized AppResult.Failure(AppError.NotFound)
            if (!talk.entries.removeAll { it.id == entryId }) return@synchronized AppResult.Failure(AppError.NotFound)
            publish()
            AppResult.Success(Unit)
        }
    }

    suspend fun listen(talkId: String): AppResult<TalkEntryDto> {
        delay(simulatedLatencyMs)
        return synchronized(lock) {
            val talk = talks.firstOrNull { it.id == talkId } ?: return@synchronized AppResult.Failure(AppError.NotFound)
            val utterance = TalkScript.utterances[listenCounter++ % TalkScript.utterances.size]
            val entry = utterance.toEntry(nextId("x"), talkId, now())
            talk.entries += entry
            publish()
            AppResult.Success(entry)
        }
    }

    override suspend fun updateEntry(talkId: String, entryId: String, action: EntryAction): AppResult<TalkEntryDto> {
        delay(simulatedLatencyMs)
        return synchronized(lock) {
            val talk = talks.firstOrNull { it.id == talkId } ?: return@synchronized AppResult.Failure(AppError.NotFound)
            val index = talk.entries.indexOfFirst { it.id == entryId }
            if (index < 0) return@synchronized AppResult.Failure(AppError.NotFound)
            applyAction(talk.entries[index], action).map { updated ->
                talk.entries[index] = updated
                publish()
                updated
            }
        }
    }

    override suspend fun isNoticeAcknowledged(): Boolean {
        val prefs = context?.getSharedPreferences("connect", Context.MODE_PRIVATE)
        return prefs?.getBoolean(NOTICE_KEY, false) ?: synchronized(lock) { noticeAcknowledged }
    }

    override suspend fun acknowledgeNotice() {
        context?.getSharedPreferences("connect", Context.MODE_PRIVATE)?.edit()?.putBoolean(NOTICE_KEY, true)?.apply()
        synchronized(lock) { noticeAcknowledged = true }
    }

    // ----- helpers (call inside `lock`) -----

    private fun applyAction(entry: TalkEntryDto, action: EntryAction): AppResult<TalkEntryDto> {
        val utterance = TalkScript.utterances.first { it.said == entry.said }
        val invalid = { field: String -> AppResult.Failure(AppError.Validation(listOf(field))) }
        return when (action) {
            is EntryAction.Understand ->
                if (action.text.isBlank()) {
                    invalid("text")
                } else {
                    val text = action.text.trim()
                    AppResult.Success(entry.copy(ownText = text, skipped = false, grade = TalkScript.grade(text, utterance)))
                }
            EntryAction.ShowMeaning -> AppResult.Success(entry.copy(ownText = "", skipped = true, grade = TalkGrade.ORANGE))
            is EntryAction.Reply -> when {
                entry.grade == null -> invalid("grade")
                action.index !in entry.replyOptions.indices -> invalid("index")
                else -> AppResult.Success(entry.copy(replyJa = entry.replyOptions[action.index].ja, confirmed = null))
            }
            is EntryAction.Confirm ->
                if (entry.replyJa == null) invalid("reply") else AppResult.Success(entry.copy(confirmed = action.understood))
            EntryAction.RetryReply -> AppResult.Success(entry.copy(replyJa = null, confirmed = null))
        }
    }

    private fun personFor(token: String): AppResult<Person> {
        if (token in blockedTokens) return AppResult.Failure(AppError.Forbidden)
        if (token in retiredTokens) return AppResult.Failure(AppError.Gone)
        if (token == myToken) return AppResult.Failure(AppError.Conflict("OWN_QR"))
        val person = people.firstOrNull { it.token == token }
        return person?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.NotFound)
    }

    private fun newContact(person: Person): ContactState {
        val greeting = ContactMessageDto(nextId("cm"), false, now(), TalkScript.greeting.first, TalkScript.greeting.second)
        return ContactState(person, 1, mutableListOf(greeting)).also { contacts.add(0, it) }
    }

    private fun publish() {
        threads.value = summaries()
    }

    // Talk Threads never notify and never count as unread.
    private fun summaries(): List<ThreadSummary> = contacts.map { c ->
        val last = c.messages.lastOrNull()
        ThreadSummary(c.person.threadId, c.person.name.asTri(), last?.let { it.ja.ifBlank { it.en } }, c.unread, last?.at.orEmpty())
    } + talks.map { t ->
        val last = t.entries.lastOrNull()
        ThreadSummary(t.id, t.title.asTri(), last?.plainJa, 0, last?.at.orEmpty())
    }

    private fun seedTalk() = TalkState(
        id = "tt_demo",
        title = "山田さん（施設）",
        entries = mutableListOf(
            TalkScript.utterances[1].toEntry("x_seed", "tt_demo", "09:12").copy(
                ownText = "She wants one more cup of tea",
                grade = TalkGrade.GREEN,
                replyJa = TalkScript.utterances[1].replies[0].ja,
                confirmed = true,
            ),
        ),
    )

    private fun ContactState.toDto() = ContactDto(
        id = person.threadId,
        name = person.name,
        language = person.language,
        lastMessage = messages.lastOrNull()?.let { it.ja.ifBlank { it.en } },
        unreadCount = unread,
    )

    private fun TalkState.toDto() = TalkThreadDto(id, title, entries.size)

    private fun String.asTri() = mapOf("ja" to this, "en" to this, "bn" to this)

    private fun nextId(prefix: String) = "$prefix${++idCounter}"

    private fun now(): String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

    private fun newToken(): String = "WK-" + (1..6).map { "23456789ABCDEFGHJKMNPQRSTUVWXYZ"[random.nextInt(31)] }.joinToString("")
}

private inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}
