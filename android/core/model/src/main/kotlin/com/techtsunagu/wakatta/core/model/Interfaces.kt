package com.techtsunagu.wakatta.core.model

import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SessionStore {
    val session: StateFlow<Session?>
    suspend fun save(session: Session)
    suspend fun clear()
    fun getCachedToken(): String?
}

interface PanelTokenStore {
    suspend fun saveToken(token: String)
    suspend fun readToken(): String?
    suspend fun clearToken()
}

interface SettingsStore {
    val isDarkMode: StateFlow<Boolean?>
    val isLargeText: StateFlow<Boolean>
    val isOfflineHintEnabled: StateFlow<Boolean>
    val isNotificationEnabled: StateFlow<Boolean>
    val isOnboardingDone: StateFlow<Boolean>
    suspend fun setDarkMode(enabled: Boolean?)
    suspend fun setLargeText(enabled: Boolean)
    suspend fun setOfflineHint(enabled: Boolean)
    suspend fun setNotification(enabled: Boolean)
    suspend fun setOnboardingDone(done: Boolean)
}

interface LanguageManager {
    val currentLanguage: StateFlow<String>
    suspend fun setLanguage(lang: String)
}

enum class TopLevelTab {
    HOME, CHATS, BOARD, MACHINES, ME
}

interface Navigator {
    fun navigate(key: Any)
    fun back(): Boolean
    fun switchTab(tab: TopLevelTab)
    fun replaceAll(key: Any)
}

/**
 * Writes that must survive without a network. [kind] is `<name>:<userId>`, so on a shared phone
 * nothing of one person is sent or shown as another.
 */
interface OutboxWriter {
    suspend fun enqueue(kind: String, idempotencyKey: String, payloadJson: String)
    fun pendingCount(kind: String): Flow<Int>

    /** The oldest write the server refused, removed from the queue. The caller gives it back to the user. */
    suspend fun takeFailed(kind: String): String? = null
}

/** Sends the queued writes of one [name]. A feature adds it to the set with `@IntoSet`. */
interface OutboxSender {
    val name: String
    suspend fun send(userId: String, idempotencyKey: String, payloadJson: String): AppResult<Unit>
}

interface CacheStore {
    suspend fun get(key: String): CacheEntry?
    suspend fun put(key: String, json: String, etag: String?)
    suspend fun evict(prefix: String)
}

interface PhotoCapture {
    suspend fun captureOrPick(): File?
}

interface QrScanner {
    fun scanResults(): Flow<String>
}

interface QrEncoder {
    fun encode(content: String, width: Int, height: Int): Any?
}

interface ShareFile {
    fun share(file: File, mimeType: String)

    /** The file with a line of text (a link the chat app makes tappable). */
    fun share(file: File, mimeType: String, text: String) = share(file, mimeType)
}

interface Speaker {
    fun speak(text: String, languageTag: String)
    fun isAvailable(languageTag: String): Boolean
    fun stop()
}

interface MachineRepository {
    suspend fun getMachines(): AppResult<List<Machine>>
    suspend fun getMachine(id: String): AppResult<MachineDetail>
    suspend fun scan(machineId: String): AppResult<Unit>
}

interface SignOffRepository {
    suspend fun getSignOffs(workerId: String): AppResult<List<SignOff>>
    suspend fun getPassport(workerId: String): AppResult<Passport>
}

interface WorkerRepository {
    suspend fun getWorkers(): AppResult<List<WorkerInfo>>
}

interface GlossaryRepository {
    suspend fun getTerms(): AppResult<List<GlossaryTerm>>
    suspend fun addTerm(termJa: String, readingJa: String, termBn: String, termEn: String): AppResult<GlossaryTerm>
}

interface CheckRepository {
    suspend fun submitCheck(
        targetKind: CheckTargetKind,
        targetId: String,
        language: String,
        answers: List<Int>,
        idempotencyKey: String
    ): AppResult<CheckResult>
}

interface ReceiptRepository {
    suspend fun markOpened(targetKind: CheckTargetKind, targetId: String): AppResult<Unit>
    suspend fun getReceipts(targetKind: CheckTargetKind, targetId: String): AppResult<Receipts>
}

interface TaskRepository {
    suspend fun getTasks(): AppResult<List<Task>>
    suspend fun getTask(id: String): AppResult<Task>
    suspend fun createTask(title: Tri, assignees: List<String>, dueAt: String?): AppResult<Task>
    suspend fun moveTask(id: String, column: TaskColumn, assignees: List<String>?, dueAt: String?, version: Int): AppResult<Task>
}

interface AttentionSource {
    fun getAttentionCards(): Flow<List<AttentionCard>>
}

interface ThreadSource {
    fun getThreads(): Flow<List<ThreadSummary>>
}

interface EventSource {
    fun getUpcomingEvents(): Flow<List<UpcomingEvent>>
}

interface InterviewDueSource {
    fun getDueInterviews(): Flow<List<DueInterview>>
}

interface TodayWordSource {
    fun getTodayWord(): Flow<TodayWord?>
}

interface VersionGate {
    val state: StateFlow<VersionGateState>
    fun require()
}
