package com.techtsunagu.wakatta.feature.connect.data

import android.content.Context
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CacheStore
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.TalkGrade
import com.techtsunagu.wakatta.core.model.ThreadSummary
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.ConditionalGet
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.core.network.safeApiCallUnit
import com.techtsunagu.wakatta.feature.connect.domain.ContactDto
import com.techtsunagu.wakatta.feature.connect.domain.ContactMessageDto
import com.techtsunagu.wakatta.feature.connect.domain.ContactRepository
import com.techtsunagu.wakatta.feature.connect.domain.ContactResolveDto
import com.techtsunagu.wakatta.feature.connect.domain.EntryAction
import com.techtsunagu.wakatta.feature.connect.domain.MyQrDto
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadIds
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadRepository
import com.techtsunagu.wakatta.feature.connect.domain.ReplyOption
import com.techtsunagu.wakatta.feature.connect.domain.TalkEntryDto
import com.techtsunagu.wakatta.feature.connect.domain.TalkThreadDto
import com.techtsunagu.wakatta.feature.connect.domain.TermPair
import com.techtsunagu.wakatta.feature.connect.formatContactTime
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.Response

private const val PREFS_NAME = "wakatta_connect_prefs"
private const val KEY_MY_QR_TOKEN = "my_qr_token"

/**
 * Real repository for My QR, Contacts, Contact Thread messages, and Talk Threads.
 */
internal class RealConnectRepository(
    private val api: () -> ConnectApi,
    private val session: SessionStore,
    private val context: Context?,
    private val cache: CacheStore,
) : ContactRepository, PersonalThreadRepository {

    private val conditional = ConditionalGet(cache)

    // One key per user action: a failed action sent again keeps its key, so the server does not make it twice.
    private val keys = ConcurrentHashMap<String, String>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val contactSummaries = MutableStateFlow<List<ThreadSummary>>(emptyList())
    private val talkSummaries = MutableStateFlow<List<ThreadSummary>>(emptyList())

    init {
        scope.launch {
            session.session.collect { currentSession ->
                if (currentSession == null) {
                    contactSummaries.value = emptyList()
                    talkSummaries.value = emptyList()
                }
            }
        }
    }

    private fun qrPrefKey(): String? {
        val uid = session.session.value?.user?.id ?: return null
        return "${KEY_MY_QR_TOKEN}_$uid"
    }

    private fun getStoredQrToken(): String? {
        val key = qrPrefKey() ?: return null
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs?.getString(key, null)
    }

    private fun saveStoredQrToken(token: String) {
        val key = qrPrefKey() ?: return
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(key, token)
            ?.apply()
    }

    private suspend fun <T> once(action: String, call: suspend (IdempotencyKey) -> Response<ApiEnvelope<T>>): AppResult<T> {
        val owned = "${session.session.value?.user?.id}:$action"
        val key = IdempotencyKey(keys.getOrPut(owned) { UUID.randomUUID().toString() })
        return safeApiCall { call(key) }.also { if (it is AppResult.Success) keys.remove(owned) }
    }

    private fun messagesKey(contactId: String) = "contact_messages:${session.session.value?.user?.id}:$contactId"

    // ----- My QR and Contacts (ContactRepository) -----

    override suspend fun getMyQr(): AppResult<MyQrDto> {
        val cached = getStoredQrToken()
        return when (val res = safeApiCall { api().myQr(QrInWire(rotate = false)) }) {
            is AppResult.Success -> {
                val token = res.value.token
                if (!token.isNullOrBlank()) {
                    saveStoredQrToken(token)
                    AppResult.Success(MyQrDto(token))
                } else if (!cached.isNullOrBlank()) {
                    AppResult.Success(MyQrDto(cached))
                } else {
                    rotateMyQr()
                }
            }
            is AppResult.Failure -> {
                if (!cached.isNullOrBlank()) AppResult.Success(MyQrDto(cached)) else res
            }
        }
    }

    override suspend fun rotateMyQr(): AppResult<MyQrDto> {
        return when (val res = safeApiCall { api().myQr(QrInWire(rotate = true)) }) {
            is AppResult.Success -> {
                val token = res.value.token
                if (token.isNullOrEmpty()) {
                    AppResult.Failure(AppError.Unknown("no QR token"))
                } else {
                    saveStoredQrToken(token)
                    AppResult.Success(MyQrDto(token))
                }
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun createInviteFile(): AppResult<File> {
        val token = when (val qr = getMyQr()) {
            is AppResult.Success -> qr.value.token
            is AppResult.Failure -> return qr
        }
        return try {
            val dir = context?.cacheDir ?: File(System.getProperty("java.io.tmpdir") ?: ".")
            val file = writeInviteImage(context, token, session.session.value?.user?.displayName, dir)
            AppResult.Success(file)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e.message))
        }
    }

    override suspend fun resolve(token: String): AppResult<ContactResolveDto> {
        return when (val res = safeApiCall { api().resolve(token) }) {
            is AppResult.Success -> {
                AppResult.Success(
                    ContactResolveDto(
                        token = token,
                        name = res.value.displayName,
                        language = res.value.language,
                    ),
                )
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun addContact(token: String): AppResult<ContactDto> {
        return when (val res = once("contact:$token") { api().addContact(it, ContactCreateWire(token)) }) {
            is AppResult.Success -> {
                val out = res.value
                val dto = ContactDto(
                    id = PersonalThreadIds.CONTACT_PREFIX + out.id,
                    name = out.displayName,
                    language = out.language,
                    lastMessage = out.lastMessageAt,
                    unreadCount = out.unread,
                )
                contactSummaries.value = contactSummaries.value.filter { it.id != dto.id } + ThreadSummary(
                    id = dto.id,
                    title = mapOf("ja" to dto.name, "en" to dto.name, "bn" to dto.name),
                    unreadCount = dto.unreadCount,
                    updatedAt = dto.lastMessage.orEmpty(),
                )
                AppResult.Success(dto)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun getContacts(): AppResult<List<ContactDto>> {
        return when (val res = safeApiCall { api().listContacts(limit = 100) }) {
            is AppResult.Success -> {
                val list = res.value.items.filterNot { it.hidden }.map {
                    ContactDto(
                        id = PersonalThreadIds.CONTACT_PREFIX + it.id,
                        name = it.displayName,
                        language = it.language,
                        lastMessage = it.lastMessageAt,
                        unreadCount = it.unread,
                    )
                }
                updateContactSummaries(list)
                AppResult.Success(list)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun getContact(id: String): AppResult<ContactDto> {
        val targetId = if (id.startsWith(PersonalThreadIds.CONTACT_PREFIX)) id else PersonalThreadIds.CONTACT_PREFIX + id
        return when (val res = getContacts()) {
            is AppResult.Success -> {
                val found = res.value.find { it.id == targetId }
                if (found != null) AppResult.Success(found) else AppResult.Failure(AppError.NotFound)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun block(contactId: String): AppResult<Unit> {
        val realId = contactId.removePrefix(PersonalThreadIds.CONTACT_PREFIX)
        return when (val res = safeApiCallUnit { api().block(BlockInWire(realId)) }) {
            is AppResult.Success -> {
                cache.evict(messagesKey(realId))
                contactSummaries.value = contactSummaries.value.filter { it.id != contactId }
                AppResult.Success(Unit)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun getSamplePeople(): List<ContactResolveDto> = emptyList()

    private fun updateContactSummaries(contacts: List<ContactDto>) {
        contactSummaries.value = contacts.map { c ->
            ThreadSummary(
                id = c.id,
                title = mapOf("ja" to c.name, "en" to c.name, "bn" to c.name),
                unreadCount = c.unreadCount,
                updatedAt = c.lastMessage.orEmpty(),
            )
        }
    }

    // ----- Contact Threads & ThreadSource (PersonalThreadRepository) -----

    override fun getThreads(): Flow<List<ThreadSummary>> {
        return combine(contactSummaries, talkSummaries) { contacts, talks ->
            contacts + talks
        }.onStart {
            // The Chats list shows what is known at once and the server's list when it comes.
            scope.launch {
                getContacts()
                getTalks()
            }
        }
    }

    override suspend fun getMessages(contactId: String): AppResult<List<ContactMessageDto>> {
        val realId = contactId.removePrefix(PersonalThreadIds.CONTACT_PREFIX)
        val key = messagesKey(realId)
        return when (val res = conditional.get(key, ContactMessagesPageWire.serializer()) { ifNoneMatch ->
            api().readMessages(realId, ifNoneMatch)
        }) {
            is AppResult.Success -> {
                val dtos = res.value.items.map { out ->
                    val ja = if (out.srcLang == "ja") out.textSrc else out.text
                    val en = if (out.srcLang == "ja") out.text else out.textSrc
                    ContactMessageDto(
                        id = out.id,
                        fromMe = out.fromMe,
                        at = out.createdAt,
                        ja = ja,
                        en = en,
                    )
                }
                AppResult.Success(dtos)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun sendMessage(contactId: String, text: String, srcLang: String): AppResult<ContactMessageDto> {
        if (text.isBlank()) return AppResult.Failure(AppError.Validation(listOf("text")))
        val realId = contactId.removePrefix(PersonalThreadIds.CONTACT_PREFIX)
        val clean = text.trim()
        val body = ContactMessageInWire(text = clean, lang = srcLang)
        return when (val res = once("message:$realId:$body") { api().sendMessage(realId, it, body) }) {
            is AppResult.Success -> {
                cache.evict(messagesKey(realId))
                val out = res.value
                val ja = if (out.srcLang == "ja") out.textSrc else out.text
                val en = if (out.srcLang == "ja") out.text else out.textSrc
                AppResult.Success(
                    ContactMessageDto(
                        id = out.id,
                        fromMe = out.fromMe,
                        at = out.createdAt,
                        ja = ja,
                        en = en,
                    ),
                )
            }
            is AppResult.Failure -> res
        }
    }

    // ----- Talk Threads (Real API) -----

    private fun updateTalkSummaries(items: List<TalkThreadOutWire>) {
        talkSummaries.value = items.map {
            ThreadSummary(
                id = PersonalThreadIds.TALK_PREFIX + it.id,
                title = mapOf("ja" to it.title, "en" to it.title, "bn" to it.title),
                unreadCount = 0,
                updatedAt = it.updatedAt,
            )
        }
    }

    override suspend fun getTalks(): AppResult<List<TalkThreadDto>> {
        return when (val res = safeApiCall { api().listTalks(limit = 100) }) {
            is AppResult.Success -> {
                val list = res.value.items.map {
                    TalkThreadDto(
                        id = PersonalThreadIds.TALK_PREFIX + it.id,
                        title = it.title,
                        entryCount = it.entryCount,
                    )
                }
                updateTalkSummaries(res.value.items)
                AppResult.Success(list)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun getTalk(id: String): AppResult<TalkThreadDto> {
        val targetId = if (id.startsWith(PersonalThreadIds.TALK_PREFIX)) id else PersonalThreadIds.TALK_PREFIX + id
        return when (val res = getTalks()) {
            is AppResult.Success -> {
                val found = res.value.find { it.id == targetId }
                if (found != null) AppResult.Success(found) else AppResult.Failure(AppError.NotFound)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun createTalk(title: String): AppResult<TalkThreadDto> {
        val clean = title.trim()
        if (clean.length !in 1..40) return AppResult.Failure(AppError.Validation(listOf("title")))
        return when (val res = once("talk:$clean") { api().createTalk(it, TalkThreadInWire(clean)) }) {
            is AppResult.Success -> {
                val out = res.value
                val dto = TalkThreadDto(
                    id = PersonalThreadIds.TALK_PREFIX + out.id,
                    title = out.title,
                    entryCount = out.entryCount,
                )
                talkSummaries.value = talkSummaries.value.filter { it.id != dto.id } + ThreadSummary(
                    id = dto.id,
                    title = mapOf("ja" to dto.title, "en" to dto.title, "bn" to dto.title),
                    unreadCount = 0,
                    updatedAt = "",
                )
                AppResult.Success(dto)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun renameTalk(id: String, title: String): AppResult<TalkThreadDto> {
        val clean = title.trim()
        if (clean.length !in 1..40) return AppResult.Failure(AppError.Validation(listOf("title")))
        val realId = id.removePrefix(PersonalThreadIds.TALK_PREFIX)
        return when (val res = safeApiCall { api().renameTalk(realId, TalkThreadInWire(clean)) }) {
            is AppResult.Success -> {
                val out = res.value
                val dto = TalkThreadDto(
                    id = PersonalThreadIds.TALK_PREFIX + out.id,
                    title = out.title,
                    entryCount = out.entryCount,
                )
                talkSummaries.value = talkSummaries.value.map {
                    if (it.id == dto.id) it.copy(title = mapOf("ja" to dto.title, "en" to dto.title, "bn" to dto.title)) else it
                }
                AppResult.Success(dto)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun deleteTalk(id: String): AppResult<Unit> {
        val realId = id.removePrefix(PersonalThreadIds.TALK_PREFIX)
        return when (val res = safeApiCallUnit { api().deleteTalk(realId) }) {
            is AppResult.Success -> {
                talkSummaries.value = talkSummaries.value.filter { it.id != id }
                AppResult.Success(Unit)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun getEntries(talkId: String): AppResult<List<TalkEntryDto>> {
        val realId = talkId.removePrefix(PersonalThreadIds.TALK_PREFIX)
        return when (val res = safeApiCall { api().listEntries(realId, limit = 50) }) {
            is AppResult.Success -> {
                val dtos = res.value.items.map { out -> mapEntry(talkId, out) }
                AppResult.Success(dtos)
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun addEntry(talkId: String, text: String): AppResult<TalkEntryDto> {
        val realId = talkId.removePrefix(PersonalThreadIds.TALK_PREFIX)
        val clean = text.trim()
        if (clean.isEmpty()) return AppResult.Failure(AppError.Validation(listOf("text")))
        val part = okhttp3.MultipartBody.Part.createFormData("text", clean)
        return when (val res = once("entry:$realId:$clean") { api().addEntry(realId, it, part) }) {
            is AppResult.Success -> {
                scope.launch { getTalks() }
                AppResult.Success(mapEntry(talkId, res.value))
            }
            is AppResult.Failure -> res
        }
    }

    override suspend fun deleteEntry(talkId: String, entryId: String): AppResult<Unit> {
        val realId = talkId.removePrefix(PersonalThreadIds.TALK_PREFIX)
        return safeApiCallUnit { api().deleteEntry(realId, entryId) }
    }

    override suspend fun updateEntry(talkId: String, entryId: String, action: EntryAction): AppResult<TalkEntryDto> {
        val realId = talkId.removePrefix(PersonalThreadIds.TALK_PREFIX)
        val body = when (action) {
            is EntryAction.Understand -> {
                val clean = action.text.trim()
                if (clean.isEmpty()) return AppResult.Failure(AppError.Validation(listOf("text")))
                TalkEntryUpdateWire(ownText = clean)
            }
            is EntryAction.Reply -> {
                var replyJa = action.replyJa
                if (replyJa.isBlank()) {
                    val entries = getEntries(talkId)
                    val entry = (entries as? AppResult.Success)?.value?.find { it.id == entryId }
                    replyJa = entry?.replyOptions?.getOrNull(action.index)?.ja.orEmpty()
                }
                if (replyJa.isBlank()) return AppResult.Failure(AppError.Validation(listOf("index")))
                TalkEntryUpdateWire(replyJa = replyJa)
            }
            is EntryAction.Confirm -> {
                TalkEntryUpdateWire(confirmed = action.understood)
            }
            EntryAction.ShowMeaning -> {
                return when (val entries = getEntries(talkId)) {
                    is AppResult.Success -> {
                        val entry = entries.value.find { it.id == entryId }
                        if (entry != null) {
                            AppResult.Success(entry.copy(ownText = "", skipped = true, grade = entry.grade ?: TalkGrade.ORANGE))
                        } else {
                            AppResult.Failure(AppError.NotFound)
                        }
                    }
                    is AppResult.Failure -> entries
                }
            }
            EntryAction.RetryReply -> {
                return when (val entries = getEntries(talkId)) {
                    is AppResult.Success -> {
                        val entry = entries.value.find { it.id == entryId }
                        if (entry != null) {
                            AppResult.Success(entry.copy(replyJa = null, confirmed = null))
                        } else {
                            AppResult.Failure(AppError.NotFound)
                        }
                    }
                    is AppResult.Failure -> entries
                }
            }
        }
        return when (val res = safeApiCall { api().updateEntry(realId, entryId, body) }) {
            is AppResult.Success -> AppResult.Success(mapEntry(talkId, res.value))
            is AppResult.Failure -> res
        }
    }

    private fun mapEntry(talkId: String, out: TalkEntryOutWire): TalkEntryDto {
        val grade = when (out.grade?.lowercase()) {
            "green" -> TalkGrade.GREEN
            "orange" -> TalkGrade.ORANGE
            "red" -> TalkGrade.RED
            else -> null
        }
        val plainJa = out.plainJa.orEmpty()
        val meaning = out.meaning.orEmpty()
        return TalkEntryDto(
            id = out.id,
            talkId = talkId,
            at = formatContactTime(out.createdAt) ?: out.createdAt,
            said = out.transcript,
            plainJa = plainJa,
            meaning = mapOf("ja" to plainJa, "en" to meaning, "bn" to meaning),
            terms = out.terms.map {
                TermPair(
                    from = it.dialect,
                    to = if (it.meaning.isNotBlank()) "${it.plain} (${it.meaning})" else it.plain,
                    plain = it.plain,
                    meaning = it.meaning,
                )
            },
            needsStaff = out.needsStaff,
            ownText = out.ownText,
            skipped = out.ownText == null && grade != null,
            grade = grade,
            hint = mapOf("ja" to plainJa, "en" to meaning, "bn" to meaning),
            replyOptions = out.replyOptions.map { ReplyOption(ja = it.ja, meaning = it.meaning) },
            replyJa = out.replyJa,
            confirmed = out.confirmed,
            needsEdit = out.needsEdit,
        )
    }

    override suspend fun isNoticeAcknowledged(): Boolean {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uid = session.session.value?.user?.id.orEmpty()
        return prefs?.getBoolean("notice_ack_$uid", false) ?: false
    }

    override suspend fun acknowledgeNotice() {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uid = session.session.value?.user?.id.orEmpty()
        prefs?.edit()?.putBoolean("notice_ack_$uid", true)?.apply()
    }
}
