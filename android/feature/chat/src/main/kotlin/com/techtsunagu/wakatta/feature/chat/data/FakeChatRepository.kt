package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.model.MessageKind
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.ThreadSummary
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.chat.model.AnswerResultDto
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel
import com.techtsunagu.wakatta.feature.chat.model.CheckQuestionForSender
import com.techtsunagu.wakatta.feature.chat.model.CheckQuestionForWorker
import com.techtsunagu.wakatta.feature.chat.model.FollowUpDto
import com.techtsunagu.wakatta.feature.chat.model.ReexplainDraft
import com.techtsunagu.wakatta.feature.chat.model.ReexplainRequest
import com.techtsunagu.wakatta.feature.chat.model.ThreadDto
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class FakeChatRepository @Inject constructor() : ChatRepository {

    private var simulatedLatencyMs: Long = 250L

    fun setLatency(latencyMs: Long) {
        simulatedLatencyMs = latencyMs
    }

    private val threadsFlow = MutableStateFlow<List<ThreadDto>>(createInitialThreads())
    private val messagesFlow = MutableStateFlow<Map<String, List<ChatMessageUiModel>>>(createInitialMessages())
    private val glossaryFlow = MutableStateFlow<List<GlossaryTerm>>(createInitialGlossary())
    private val followUpsFlow = MutableStateFlow<List<FollowUpDto>>(createInitialFollowUps())

    // Internal answer keys kept strictly server-side (Rule R-3)
    private val serverAnswerKeys = mutableMapOf<String, List<Int>>(
        "m1" to listOf(0),
    )

    override val debugUserIds: Map<Role, String> = mapOf(Role.FOREMAN to "u_tanaka", Role.WORKER to "u_rahim")

    override fun getThreads(): Flow<List<ThreadDto>> = threadsFlow.asStateFlow()

    override fun getThreadSummaries(): Flow<List<ThreadSummary>> {
        return threadsFlow.map { list ->
            list.map { th ->
                ThreadSummary(
                    id = th.id,
                    title = mapOf("ja" to th.titleJa, "en" to (th.titleEn ?: th.titleJa)),
                    lastMessage = th.lastMessage,
                    unreadCount = th.unread,
                    updatedAt = th.lastMessageAt ?: "",
                )
            }
        }
    }

    override fun getMessages(threadId: String): Flow<List<ChatMessageUiModel>> {
        return messagesFlow.map { it[threadId] ?: emptyList() }
    }

    override suspend fun sendMessage(
        threadId: String,
        text: String?,
        audio: File?,
        isVoice: Boolean,
        fromUserId: String,
        idempotencyKey: String,
    ): AppResult<ChatMessageUiModel> {
        delay(simulatedLatencyMs)

        val id = "m_" + System.currentTimeMillis().toString(36)
        val isForeman = fromUserId == "u_tanaka"
        val actualIsVoice = isVoice || audio != null
        val kind = if (actualIsVoice) MessageKind.VOICE else MessageKind.TEXT
        val safeText = text.orEmpty()

        val (jaText, enText, bnText) = if (actualIsVoice && safeText.isBlank()) {
            Triple(
                DEMO_DICTATION,
                "Tomorrow from 8:00 we change the die on Press #2. Rahim and Nguyen, gather at 7:45. Before the change, always lock out the power and wear safety shoes and gloves. When you finish, fill in the inspection sheet and report to me.",
                "আগামীকাল সকাল ৮টা থেকে প্রেস ২-এর ডাই পরিবর্তন করব। রহিম এবং নগুয়েন, ৭:৪৫-এ একত্রিত হবেন। পরিবর্তনের আগে অবশ্যই বিদ্যুৎ লকআউট করবেন এবং নিরাপত্তা জুতো ও গ্লাভস পরবেন। শেষ হলে পরিদর্শন পত্র পূরণ করে আমাকে রিপোর্ট করবেন।",
            )
        } else {
            Triple(
                safeText,
                if (isForeman) "(auto-translated) $safeText" else safeText,
                if (isForeman) "(অনূদিত) $safeText" else safeText,
            )
        }

        val questionsForSender = if (actualIsVoice && isForeman) {
            listOf(
                CheckQuestionForSender(
                    id = "q1",
                    termId = null,
                    prompt = mapOf(
                        "ja" to "明日は何時に集合しますか？",
                        "en" to "What time do you gather tomorrow?",
                        "bn" to "আগামীকাল কখন একত্রিত হবেন?",
                    ),
                    options = listOf(
                        mapOf("ja" to "7:30", "en" to "7:30", "bn" to "৭:৩০"),
                        mapOf("ja" to "7:45", "en" to "7:45", "bn" to "৭:৪৫"),
                        mapOf("ja" to "8:00", "en" to "8:00", "bn" to "৮:০০"),
                    ),
                    correctIndex = 1,
                ),
                CheckQuestionForSender(
                    id = "q2",
                    termId = null,
                    prompt = mapOf(
                        "ja" to "金型交換の前に、必ず何をしますか？",
                        "en" to "What must you do before the die change?",
                        "bn" to "ডাই পরিবর্তনের আগে অবশ্যই কী করবেন?",
                    ),
                    options = listOf(
                        mapOf("ja" to "プレスを一度動かす", "en" to "Run the press once", "bn" to "প্রেস একবার চালান"),
                        mapOf("ja" to "電源をロックアウトする", "en" to "Lock out the power", "bn" to "বিদ্যুৎ লকআউট করুন"),
                        mapOf("ja" to "事務所に電話する", "en" to "Call the office", "bn" to "অফিসে ফোন করুন"),
                    ),
                    correctIndex = 1,
                ),
                CheckQuestionForSender(
                    id = "q3",
                    termId = "金型交換",
                    prompt = mapOf(
                        "ja" to "「金型交換」の意味は？",
                        "en" to "What is 金型交換 (kanagata kōkan)?",
                        "bn" to "金型交換 (ডাই পরিবর্তন)-এর অর্থ কী?",
                    ),
                    options = listOf(
                        mapOf("ja" to "オイル交換", "en" to "Oil change", "bn" to "তেল পরিবর্তন"),
                        mapOf("ja" to "シフト交代", "en" to "Shift change", "bn" to "শিফট পরিবর্তন"),
                        mapOf("ja" to "金型の交換", "en" to "Die change", "bn" to "ডাই পরিবর্তন"),
                    ),
                    correctIndex = 2,
                ),
            )
        } else {
            emptyList()
        }

        // Store server answer keys privately (Rule R-3)
        if (questionsForSender.isNotEmpty()) {
            serverAnswerKeys[id] = questionsForSender.map { it.correctIndex }
        }

        // Strip answer key for worker model
        val questionsForWorker = questionsForSender.map {
            CheckQuestionForWorker(
                id = it.id,
                termId = it.termId,
                prompt = it.prompt,
                options = it.options,
            )
        }

        val initialWorkerStatuses = if (questionsForSender.isNotEmpty()) {
            mapOf("u_rahim" to WakattaStatus.WAIT, "u_nguyen" to WakattaStatus.WAIT, "u_siti" to WakattaStatus.WAIT)
        } else {
            emptyMap()
        }

        val newMsg = ChatMessageUiModel(
            id = id,
            threadId = threadId,
            fromUserId = fromUserId,
            fromUserName = if (isForeman) "田中 健一 (Foreman)" else "Rahim Uddin",
            isMine = isForeman,
            kind = kind,
            text = mapOf("ja" to jaText, "en" to enText, "bn" to bnText),
            originalText = jaText,
            translatedText = enText,
            time = "08:40",
            stage = if (isForeman) 3 else 5,
            isDraft = isForeman,
            isVoice = actualIsVoice,
            voiceDuration = if (actualIsVoice) "0:12" else null,
            hasCheck = questionsForSender.isNotEmpty(),
            workerQuestions = questionsForWorker,
            workerStatuses = initialWorkerStatuses,
            workerNames = DEMO_WORKER_NAMES,
            senderQuestions = questionsForSender,
            addedTasks = if (actualIsVoice) listOf("2号プレス 金型交換", "交換前 ロックアウト・保護具", "点検表記入・田中へ報告") else emptyList(),
        )

        val currentMap = messagesFlow.value.toMutableMap()
        val list = (currentMap[threadId] ?: emptyList()) + newMsg
        currentMap[threadId] = list
        messagesFlow.value = currentMap

        // Update thread preview
        updateThreadLastMessage(threadId, if (actualIsVoice) "明日の朝8時から、2号プレスの金型交換をします..." else safeText, actualIsVoice)

        return AppResult.Success(newMsg)
    }

    override suspend fun sendDraft(messageId: String): AppResult<ChatMessageUiModel> {
        val (threadId, list) = messagesFlow.value.entries.firstOrNull { (_, l) -> l.any { it.id == messageId } }
            ?: return AppResult.Failure(AppError.NotFound)
        val sent = list.first { it.id == messageId }.copy(stage = 5, isDraft = false, needsEdit = false)
        messagesFlow.value = messagesFlow.value + (threadId to list.map { if (it.id == messageId) sent else it })
        return AppResult.Success(sent)
    }

    override suspend fun submitAnswers(
        messageId: String,
        userId: String,
        answers: List<Int>,
        idempotencyKey: String,
    ): AppResult<AnswerResultDto> {
        delay(simulatedLatencyMs)

        val correctAnswers = serverAnswerKeys[messageId] ?: listOf(1, 1, 2)
        val correctCount = answers.filterIndexed { index, ans ->
            index < correctAnswers.size && ans == correctAnswers[index]
        }.size

        // Grading logic strictly inside repository (Rule R-3):
        // All correct -> GREEN
        // None correct (or all "not sure" -1) -> RED
        // Some correct -> ORANGE
        val status = when {
            correctCount == correctAnswers.size -> WakattaStatus.GREEN
            correctCount == 0 -> WakattaStatus.RED
            else -> WakattaStatus.ORANGE
        }

        val followUpIds = mutableListOf<String>()
        if (status != WakattaStatus.GREEN) {
            val fuId = "fu_" + System.currentTimeMillis().toString(36)
            followUpIds.add(fuId)
            val newFu = FollowUpDto(
                id = fuId,
                messageId = messageId,
                userId = userId,
                displayName = "Rahim Uddin",
                termJa = "金型交換",
                status = FollowUpStatus.OPEN,
                note = "Worker selected wrong answer for die change / lockout check",
                createdAt = "2026-11-23T08:45:00Z",
            )
            followUpsFlow.value = followUpsFlow.value + newFu
        }

        // Update message state in memory
        val currentMap = messagesFlow.value.toMutableMap()
        for ((thId, msgList) in currentMap) {
            val updated = msgList.map { m ->
                if (m.id == messageId) {
                    val newStatuses = m.workerStatuses.toMutableMap()
                    newStatuses[userId] = status
                    m.copy(
                        workerStatuses = newStatuses,
                        workerAnswerStatus = status,
                        workerAnswers = answers,
                        isNudged = false,
                    )
                } else {
                    m
                }
            }
            currentMap[thId] = updated
        }
        messagesFlow.value = currentMap

        return AppResult.Success(AnswerResultDto(status = status, followUpIds = followUpIds))
    }

    override suspend fun editDraft(
        messageId: String,
        textJa: String,
    ): AppResult<ChatMessageUiModel> {
        delay(simulatedLatencyMs)
        val currentMap = messagesFlow.value.toMutableMap()
        for ((thId, msgList) in currentMap) {
            val idx = msgList.indexOfFirst { it.id == messageId }
            if (idx != -1) {
                val original = msgList[idx]
                val updated = original.copy(
                    text = original.text + ("ja" to textJa),
                    originalText = textJa,
                    translatedText = textJa,
                )
                val newList = msgList.toMutableList()
                newList[idx] = updated
                currentMap[thId] = newList
                messagesFlow.value = currentMap
                return AppResult.Success(updated)
            }
        }
        val defaultMsg = ChatMessageUiModel(
            id = messageId,
            threadId = "t_line2",
            fromUserId = "u_tanaka",
            fromUserName = "田中 健一 (Foreman)",
            isMine = true,
            kind = MessageKind.TEXT,
            text = mapOf("ja" to textJa),
            originalText = textJa,
            translatedText = textJa,
            time = "08:00",
        )
        return AppResult.Success(defaultMsg)
    }

    override suspend fun transcribe(audio: File, language: String): AppResult<String> {
        delay(simulatedLatencyMs)
        return AppResult.Success(DEMO_DICTATION)
    }

    override suspend fun getReexplainDraft(messageId: String, workerId: String): AppResult<ReexplainDraft> {
        delay(simulatedLatencyMs)
        val followUp = followUpsFlow.value.firstOrNull { it.messageId == messageId && it.userId == workerId }
            ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(
            ReexplainDraft(
                workerName = followUp.displayName,
                workerLang = "en",
                termJa = followUp.termJa,
                easyJa = "明日はプレス機の金型を交換します。新しい金属の型に変える作業です。",
                explanation = mapOf("en" to "Tomorrow we will exchange the die on the press machine. It means replacing with a new metal mold."),
                newQuestionPrompt = mapOf("ja" to "「金型交換」は何をすることですか？", "en" to "What does 'die change' mean?"),
                newQuestionOptions = listOf(
                    mapOf("ja" to "油を入れる", "en" to "Refill oil"),
                    mapOf("ja" to "金属の型を新しく変える", "en" to "Change metal die mold"),
                    mapOf("ja" to "掃除する", "en" to "Clean up"),
                ),
                newQuestionCorrectIndex = 1,
            ),
        )
    }

    override suspend fun reexplain(
        messageId: String,
        request: ReexplainRequest,
    ): AppResult<Unit> {
        delay(simulatedLatencyMs)

        // Mark follow up resolved
        followUpsFlow.value = followUpsFlow.value.map { fu ->
            if (fu.messageId == messageId && fu.userId == request.userId) {
                fu.copy(status = FollowUpStatus.CLARIFIED, note = request.easyJa, resolvedAt = "2026-11-23T08:50:00Z")
            } else {
                fu
            }
        }

        // Add re-explanation message to thread
        val currentMap = messagesFlow.value.toMutableMap()
        for ((thId, msgList) in currentMap) {
            val original = msgList.find { it.id == messageId }
            if (original != null) {
                val reexplainMsg = ChatMessageUiModel(
                    id = "re_" + System.currentTimeMillis().toString(36),
                    threadId = thId,
                    fromUserId = "u_tanaka",
                    fromUserName = "田中 健一 (Foreman)",
                    isMine = true,
                    kind = MessageKind.REEXPLAIN,
                    text = mapOf(
                        "ja" to "【再説明】" + request.easyJa,
                        "en" to "[Re-explanation] " + pick(request.explanation, "en"),
                        "bn" to "[পুনরায় ব্যাখ্যা] " + pick(request.explanation, "bn"),
                    ),
                    originalText = request.easyJa,
                    translatedText = pick(request.explanation, "en"),
                    time = "08:50",
                    stage = 5,
                    hasCheck = true,
                    workerQuestions = listOf(
                        CheckQuestionForWorker(
                            id = "re_q1",
                            termId = request.termJa,
                            prompt = request.newQuestionPrompt,
                            options = request.newQuestionOptions,
                        ),
                    ),
                    workerStatuses = mapOf(request.userId to WakattaStatus.WAIT),
                )
                currentMap[thId] = msgList + reexplainMsg
            }
        }
        messagesFlow.value = currentMap

        return AppResult.Success(Unit)
    }

    override suspend fun nudge(messageId: String, userId: String): AppResult<Unit> {
        delay(simulatedLatencyMs)

        val currentMap = messagesFlow.value.toMutableMap()
        for ((thId, msgList) in currentMap) {
            val updated = msgList.map { m ->
                if (m.id == messageId) {
                    m.copy(isNudged = true)
                } else {
                    m
                }
            }
            currentMap[thId] = updated
        }
        messagesFlow.value = currentMap

        return AppResult.Success(Unit)
    }

    override suspend fun askExplainAgain(
        messageId: String,
        userId: String,
        termJa: String,
    ): AppResult<Unit> {
        delay(simulatedLatencyMs)

        val fuId = "fu_ask_" + System.currentTimeMillis().toString(36)
        val newFu = FollowUpDto(
            id = fuId,
            messageId = messageId,
            userId = userId,
            displayName = "Rahim Uddin",
            termJa = termJa,
            status = FollowUpStatus.OPEN,
            note = "Worker requested re-explanation from Foreman",
            createdAt = "2026-11-23T08:52:00Z",
        )
        followUpsFlow.value = followUpsFlow.value + newFu

        val currentMap = messagesFlow.value.toMutableMap()
        for ((thId, msgList) in currentMap) {
            if (msgList.any { it.id == messageId }) {
                val askMsg = ChatMessageUiModel(
                    id = "ask_" + System.currentTimeMillis().toString(36),
                    threadId = thId,
                    fromUserId = userId,
                    fromUserName = if (userId == "u_rahim") "Rahim Uddin" else "Worker",
                    isMine = true,
                    kind = MessageKind.ASK,
                    text = mapOf("ja" to termJa),
                    originalText = termJa,
                    translatedText = termJa,
                    time = "08:52",
                )
                currentMap[thId] = msgList + askMsg
            }
        }
        messagesFlow.value = currentMap

        return AppResult.Success(Unit)
    }

    override suspend fun getTerms(): AppResult<List<GlossaryTerm>> {
        delay(simulatedLatencyMs)
        return AppResult.Success(glossaryFlow.value)
    }

    override suspend fun addTerm(
        termJa: String,
        readingJa: String,
        termBn: String,
        termEn: String,
    ): AppResult<GlossaryTerm> {
        delay(simulatedLatencyMs)
        val newTerm = GlossaryTerm(ja = termJa, reading = readingJa, bn = termBn, en = termEn)
        glossaryFlow.value = glossaryFlow.value + newTerm
        return AppResult.Success(newTerm)
    }

    override suspend fun getFollowUps(): AppResult<List<FollowUpDto>> {
        delay(simulatedLatencyMs)
        return AppResult.Success(followUpsFlow.value)
    }

    private companion object {
        val DEMO_WORKER_NAMES = mapOf("u_rahim" to "Rahim Uddin", "u_nguyen" to "Nguyen Van An", "u_siti" to "Siti Rahma")
        const val DEMO_DICTATION = "明日の朝8時から、2号プレスの金型交換をします。ラヒムさんとグエンさんは、7時45分に集合してください。交換の前に、必ず電源をロックアウトして、安全靴と手袋をつけてください。終わったら点検表を書いて、私に報告してください。"
    }

    private fun updateThreadLastMessage(threadId: String, lastMsg: String, isVoice: Boolean) {
        threadsFlow.value = threadsFlow.value.map { th ->
            if (th.id == threadId) {
                th.copy(
                    lastMessage = lastMsg,
                    lastMessageAt = "08:40",
                    unread = 0,
                )
            } else {
                th
            }
        }
    }

    private fun createInitialThreads(): List<ThreadDto> {
        return listOf(
            ThreadDto(
                id = "t_line2",
                titleJa = "Line 2 · プレス班",
                titleEn = "Line 2 · Press team",
                kind = "group",
                memberIds = listOf("u_tanaka", "u_rahim", "u_nguyen", "u_siti"),
                unread = 2,
                lastMessageAt = "08:02",
                lastMessage = "今日は3号機の油の点検を先にしてください。",
                worstStatus = WakattaStatus.ORANGE,
            ),
            ThreadDto(
                id = "t_rahim",
                titleJa = "田中 健一 (Foreman)",
                titleEn = "Tanaka Kenichi (Foreman)",
                kind = "direct",
                memberIds = listOf("u_tanaka", "u_rahim"),
                unread = 0,
                lastMessageAt = "昨日",
                lastMessage = "明日の点検表、ありがとう。",
                worstStatus = WakattaStatus.GREEN,
            ),
            ThreadDto(
                id = "t_safety",
                titleJa = "安全連絡",
                titleEn = "Safety notices",
                kind = "group",
                memberIds = listOf("u_tanaka", "u_rahim", "u_nguyen", "u_siti"),
                unread = 0,
                lastMessageAt = "10:00",
                lastMessage = "安全第一",
                worstStatus = null,
            ),
        )
    }

    private fun createInitialMessages(): Map<String, List<ChatMessageUiModel>> {
        val q1 = CheckQuestionForWorker(
            id = "q_m1",
            termId = "油圧オイル",
            prompt = mapOf(
                "ja" to "今日最初に何を点検しますか？",
                "en" to "What do you check first today?",
                "bn" to "আজ প্রথমে কী পরীক্ষা করবেন?",
            ),
            options = listOf(
                mapOf("ja" to "3号機のオイル", "en" to "Oil on Machine 3", "bn" to "মেশিন ৩-এর তেল"),
                mapOf("ja" to "2号機のエア", "en" to "Air on Machine 2", "bn" to "মেশিন ২-এর বাতাস"),
                mapOf("ja" to "安全靴", "en" to "Safety shoes", "bn" to "নিরাপত্তা জুতো"),
            ),
        )

        val m1SenderQuestions = listOf(
            CheckQuestionForSender(
                id = "q_m1",
                termId = "油圧オイル",
                prompt = q1.prompt,
                options = q1.options,
                correctIndex = 0,
            ),
        )

        val tLine2Messages = listOf(
            ChatMessageUiModel(
                id = "m1",
                threadId = "t_line2",
                fromUserId = "u_tanaka",
                fromUserName = "田中 健一 (Foreman)",
                isMine = true,
                kind = MessageKind.VOICE,
                text = mapOf(
                    "ja" to "今日は3号機の油の点検を先にしてください。オイルが少ないときは、私に言ってください。",
                    "en" to "Today, check the oil on Machine 3 first. If the oil is low, tell me.",
                    "bn" to "আজ প্রথমে মেশিন ৩-এর তেল পরীক্ষা করুন। তেল কম থাকলে আমাকে বলুন।",
                ),
                originalText = "今日は3号機の油の点検を先にしてください。オイルが少ないときは、私に言ってください。",
                translatedText = "Today, check the oil on Machine 3 first. If the oil is low, tell me.",
                time = "08:02",
                stage = 5,
                isVoice = true,
                voiceDuration = "0:14",
                hasCheck = true,
                workerQuestions = listOf(q1),
                senderQuestions = m1SenderQuestions,
                workerStatuses = mapOf(
                    "u_rahim" to WakattaStatus.GREEN,
                    "u_nguyen" to WakattaStatus.GREEN,
                    "u_siti" to WakattaStatus.ORANGE,
                ),
                workerNames = DEMO_WORKER_NAMES,
                workerAnswerStatus = WakattaStatus.GREEN,
                workerAnswers = listOf(0),
                addedTasks = listOf("3号機 オイル点検"),
            ),
            ChatMessageUiModel(
                id = "m1a",
                threadId = "t_line2",
                fromUserId = "u_siti",
                fromUserName = "Siti Rahma",
                isMine = false,
                kind = MessageKind.ASK,
                text = mapOf(
                    "ja" to "どのオイルをどこで点検しますか？もう一度教えてください。",
                    "en" to "Which oil do I check, and where? Please tell me again.",
                    "bn" to "কোন তেল কোথায় পরীক্ষা করব? দয়া করে আবার বলুন।",
                ),
                originalText = "どのオイルをどこで点検しますか？もう一度教えてください。",
                translatedText = "Which oil do I check, and where? Please tell me again.",
                time = "08:06",
                stage = 5,
                hasCheck = false,
            ),
            ChatMessageUiModel(
                id = "m2",
                threadId = "t_line2",
                fromUserId = "u_siti",
                fromUserName = "Siti Rahma",
                isMine = false,
                kind = MessageKind.TEXT,
                text = mapOf(
                    "ja" to "機械3のオイル、少ないです。",
                    "en" to "Machine 3 oil is low.",
                    "bn" to "মেশিন ৩-এর তেল কম আছে।",
                ),
                originalText = "機械3のオイル、少ないです。",
                translatedText = "Machine 3 oil is low.",
                time = "08:15",
                stage = 5,
                hasCheck = false,
            ),
            ChatMessageUiModel(
                id = "m2a",
                threadId = "t_line2",
                fromUserId = "u_tanaka",
                fromUserName = "田中 健一 (Foreman)",
                isMine = true,
                kind = MessageKind.TEXT,
                text = mapOf(
                    "ja" to "ありがとう、シティさん。すぐに補充してください。終わったら教えてね。",
                    "en" to "Thank you, Siti. Please refill it right away. Tell me when you are done.",
                    "bn" to "ধন্যবাদ সিটি। অবিলম্বে এটি পূরণ করুন। শেষ হলে আমাকে জানান।",
                ),
                originalText = "ありがとう、シティさん。すぐに補充してください。終わったら教えてね。",
                translatedText = "Thank you, Siti. Please refill it right away. Tell me when you are done.",
                time = "08:17",
                stage = 5,
                hasCheck = false,
            ),
        )

        val tRahimMessages = listOf(
            ChatMessageUiModel(
                id = "m3",
                threadId = "t_rahim",
                fromUserId = "u_tanaka",
                fromUserName = "田中 健一 (Foreman)",
                isMine = true,
                kind = MessageKind.TEXT,
                text = mapOf(
                    "ja" to "明日の点検表、ありがとう。",
                    "en" to "Thanks for tomorrow’s inspection sheet.",
                    "bn" to "আগামীকালের পরিদর্শন পত্রের জন্য ধন্যবাদ।",
                ),
                originalText = "明日の点検表、ありがとう。",
                translatedText = "Thanks for tomorrow’s inspection sheet.",
                time = "昨日",
                stage = 5,
                hasCheck = false,
            ),
        )

        return mapOf(
            "t_line2" to tLine2Messages,
            "t_rahim" to tRahimMessages,
            "t_safety" to emptyList(),
        )
    }

    private fun createInitialGlossary(): List<GlossaryTerm> {
        return listOf(
            GlossaryTerm(
                ja = "金型交換",
                reading = "かながたこうかん",
                en = "Die change",
                bn = "ডাই পরিবর্তন",
            ),
            GlossaryTerm(
                ja = "ロックアウト",
                reading = "ろっくあうと",
                en = "Lockout",
                bn = "লকআউট",
            ),
            GlossaryTerm(
                ja = "安全靴",
                reading = "あんぜんぐつ",
                en = "Safety shoes",
                bn = "নিরাপত্তা জুতো",
            ),
            GlossaryTerm(
                ja = "点検表",
                reading = "てんけんひょう",
                en = "Inspection sheet",
                bn = "পরিদর্শন পত্র",
            ),
            GlossaryTerm(
                ja = "油圧オイル",
                reading = "ゆあつおいる",
                en = "Hydraulic oil",
                bn = "হাইড্রলিক তেল",
            ),
        )
    }

    private fun createInitialFollowUps(): List<FollowUpDto> {
        return listOf(
            FollowUpDto(
                id = "fu1",
                messageId = "m1",
                userId = "u_siti",
                displayName = "Siti Rahma",
                termJa = "油圧オイル",
                status = FollowUpStatus.OPEN,
                note = "Selected Air on Machine 2 instead of Oil",
                createdAt = "2026-11-23T08:05:00Z",
            ),
        )
    }
}
