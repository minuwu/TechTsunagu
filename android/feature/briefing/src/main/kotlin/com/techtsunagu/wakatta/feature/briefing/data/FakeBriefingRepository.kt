package com.techtsunagu.wakatta.feature.briefing.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckQuestion
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.feature.briefing.model.BriefingDraft
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForForeman
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForWorker
import com.techtsunagu.wakatta.feature.briefing.model.BriefingPublishRequest
import com.techtsunagu.wakatta.feature.briefing.model.BriefingReceipts
import com.techtsunagu.wakatta.feature.briefing.model.BriefingWorkerStatus
import com.techtsunagu.wakatta.feature.briefing.model.CheckQuestionWithAnswer
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeBriefingRepository(
    private val delayMs: Long = 200L,
) : BriefingRepository {

    @Inject
    constructor() : this(200L)

    private val workers: List<WorkerInfo> = DemoSeed.users
        .filter { it.role == Role.WORKER }
        .map { WorkerInfo(id = it.id, displayName = it.displayName, language = it.language) }

    private val _briefings = MutableStateFlow<List<BriefingForForeman>>(emptyList())
    val briefings: Flow<List<BriefingForForeman>> = _briefings.asStateFlow()

    // Map: briefingId -> workerId -> BriefingWorkerStatus
    private val _workerStatuses = mutableMapOf<String, MutableMap<String, BriefingWorkerStatus>>()

    init {
        val now = Instant.now()
        val yesterday = now.minus(1, ChronoUnit.DAYS).toString()
        val today = now.minus(2, ChronoUnit.HOURS).toString()

        val seedYesterday = BriefingForForeman(
            id = "b_seed",
            text = mapOf(
                "ja" to "昨日の朝礼: 3号機のノズルは高温です。ドアは完全に止まってから開けてください。",
                "en" to "Yesterday’s briefing: the nozzle on Machine 3 is very hot. Open the door only after it has fully stopped.",
                "bn" to "গতকালের ব্রিফিং: মেশিন ৩-এর নজল খুব গরম। মেশিন সম্পূর্ণ থামার পরেই দরজা খুলুন।",
            ),
            focusText = mapOf(
                "ja" to "3号機のノズルは高温。",
                "en" to "The nozzle on Machine 3 is very hot.",
                "bn" to "মেশিন ৩-এর নজল খুব গরম।",
            ),
            changeText = mapOf(
                "ja" to "ドアは完全に止まってから開ける。",
                "en" to "Open the door only after it has fully stopped.",
                "bn" to "মেশিন সম্পূর্ণ থামার পরেই দরজা খুলুন।",
            ),
            createdAt = yesterday,
            publishedAt = yesterday,
            question = CheckQuestionWithAnswer(
                id = "q_seed",
                prompt = mapOf(
                    "ja" to "3号機のドアはいつ開けますか？",
                    "en" to "When do you open the door of Machine 3?",
                    "bn" to "মেশিন ৩-এর দরজা কখন খুলবেন?",
                ),
                options = listOf(
                    mapOf("ja" to "完全に止まってから", "en" to "After it has fully stopped", "bn" to "সম্পূর্ণ থামার পর"),
                    mapOf("ja" to "いつでも", "en" to "Any time", "bn" to "যেকোনো সময়"),
                    mapOf("ja" to "運転中に", "en" to "While it runs", "bn" to "চলাকালীন"),
                ),
                correctIndex = 0,
            ),
        )

        val seedToday = BriefingForForeman(
            id = "b_today",
            text = mapOf(
                "ja" to "おはようございます。今日の安全の重点は、プレスでの手の挟まれ防止です。作業の前に、必ず安全カバーが閉まっているか確認してください。変更が一つあります。2号プレスの金型交換は午後に行います。午前中は使えません。",
                "en" to "Good morning. Today’s safety focus is preventing hand entrapment in the press. Before work, always check that the safety cover is closed. One change: die replacement on Press #2 will take place in the afternoon. It cannot be used in the morning.",
                "bn" to "সুপ্রভাত। আজকের নিরাপত্তার মূল বিষয় হলো প্রেসে হাত আটকে যাওয়া রোধ করা। কাজ শুরুর আগে অবশ্যই সেফティ কভার বন্ধ আছে কিনা পরীক্ষা করুন। একটি পরিবর্তন আছে: ২ নম্বর প্রেসের ডাই পরিবর্তন বিকেলে হবে। সকালে এটি ব্যবহার করা যাবে না।",
            ),
            focusText = mapOf(
                "ja" to "2号プレス: カバー確認。手の挟まれ防止。",
                "en" to "Press #2: check cover. Prevent hand pinch.",
                "bn" to "প্রেস #২: কভার পরীক্ষা। হাত আটকা রোধ।",
            ),
            changeText = mapOf(
                "ja" to "2号プレスの金型交換は午後。午前中は使用不可。",
                "en" to "Press #2 die change in the afternoon. Morning unavailable.",
                "bn" to "প্রেস #২ ডাই পরিবর্তন বিকেলে। সকালে ব্যবহার নিষেধ।",
            ),
            createdAt = today,
            publishedAt = today,
            question = CheckQuestionWithAnswer(
                id = "q_today",
                prompt = mapOf(
                    "ja" to "2号プレスの金型交換はいつですか？",
                    "en" to "When is die replacement on Press #2?",
                    "bn" to "প্রেস #২-এর ডাই পরিবর্তন কখন হবে?",
                ),
                options = listOf(
                    mapOf("ja" to "午後", "en" to "Afternoon", "bn" to "বিকেলে"),
                    mapOf("ja" to "午前", "en" to "Morning", "bn" to "সকালে"),
                    mapOf("ja" to "明日", "en" to "Tomorrow", "bn" to "কাল"),
                ),
                correctIndex = 0,
            ),
        )

        _briefings.value = listOf(seedToday, seedYesterday)

        // Seed worker statuses for b_seed
        val statusesSeed = mutableMapOf<String, BriefingWorkerStatus>()
        if (workers.isNotEmpty()) {
            // Rahim passed
            statusesSeed[workers[0].id] = BriefingWorkerStatus(
                worker = workers[0],
                hasAttended = true,
                status = WakattaStatus.GREEN,
                answerIndex = 0,
                answerText = "完全に止まってから",
            )
        }
        if (workers.size > 1) {
            // Nguyen wrong
            statusesSeed[workers[1].id] = BriefingWorkerStatus(
                worker = workers[1],
                hasAttended = true,
                status = WakattaStatus.RED,
                answerIndex = 1,
                answerText = "いつでも",
            )
        }
        if (workers.size > 2) {
            // Siti wait
            statusesSeed[workers[2].id] = BriefingWorkerStatus(
                worker = workers[2],
                hasAttended = false,
                status = WakattaStatus.WAIT,
                answerIndex = null,
                answerText = null,
            )
        }
        _workerStatuses["b_seed"] = statusesSeed

        // Seed worker statuses for b_today
        val statusesToday = mutableMapOf<String, BriefingWorkerStatus>()
        if (workers.isNotEmpty()) {
            // Rahim has not answered yet
            statusesToday[workers[0].id] = BriefingWorkerStatus(
                worker = workers[0],
                hasAttended = false,
                status = WakattaStatus.WAIT,
                answerIndex = null,
                answerText = null,
            )
        }
        if (workers.size > 1) {
            // Nguyen understood
            statusesToday[workers[1].id] = BriefingWorkerStatus(
                worker = workers[1],
                hasAttended = true,
                status = WakattaStatus.GREEN,
                answerIndex = 0,
                answerText = "午後",
            )
        }
        if (workers.size > 2) {
            // Siti was not sure
            statusesToday[workers[2].id] = BriefingWorkerStatus(
                worker = workers[2],
                hasAttended = true,
                status = WakattaStatus.ORANGE,
                answerIndex = -1,
                answerText = "わからない",
            )
        }
        _workerStatuses["b_today"] = statusesToday
    }

    override fun observeBriefings(): Flow<List<BriefingForForeman>> = briefings

    override suspend fun getBriefings(): AppResult<List<BriefingForForeman>> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(_briefings.value)
    }

    override suspend fun getBriefingForForeman(id: String?): AppResult<BriefingForForeman> {
        if (delayMs > 0) delay(delayMs)
        val list = _briefings.value
        val briefing = if (id != null) {
            list.find { it.id == id }
        } else {
            list.firstOrNull()
        }
        return if (briefing != null) {
            AppResult.Success(briefing)
        } else {
            AppResult.Failure(AppError.NotFound)
        }
    }

    override suspend fun getBriefingForWorker(id: String?): AppResult<BriefingForWorker> {
        if (delayMs > 0) delay(delayMs)
        val list = _briefings.value
        val foremanBriefing = if (id != null) {
            list.find { it.id == id }
        } else {
            list.firstOrNull()
        } ?: return AppResult.Failure(AppError.NotFound)

        val workerId = workers.firstOrNull()?.id ?: "u_rahim"
        val statusMap = _workerStatuses[foremanBriefing.id]
        val workerStatus = statusMap?.get(workerId)

        // Convert question: Rule R-3 requires dropping `correctIndex` for workers!
        val workerQuestion = CheckQuestion(
            id = foremanBriefing.question.id,
            termId = null,
            prompt = foremanBriefing.question.prompt,
            options = foremanBriefing.question.options,
        )

        val workerBriefing = BriefingForWorker(
            id = foremanBriefing.id,
            text = foremanBriefing.text,
            focusText = foremanBriefing.focusText,
            changeText = foremanBriefing.changeText,
            createdAt = foremanBriefing.createdAt,
            publishedAt = foremanBriefing.publishedAt,
            question = workerQuestion,
            status = foremanBriefing.status,
            opened = workerStatus?.hasAttended ?: false,
            passed = when (workerStatus?.status) {
                WakattaStatus.GREEN -> true
                WakattaStatus.ORANGE, WakattaStatus.RED -> false
                else -> null
            },
            userAnswer = workerStatus?.answerIndex,
            wakattaStatus = workerStatus?.status ?: WakattaStatus.WAIT,
        )
        return AppResult.Success(workerBriefing)
    }

    override suspend fun createDraft(
        text: String?,
        audioFile: File?,
        idempotencyKey: String,
    ): AppResult<BriefingDraft> {
        if (delayMs > 0) delay(delayMs)
        val transcript = text?.takeIf { it.isNotBlank() }
            ?: "おはようございます。今日の安全の重点は、プレスでの手の挟まれ防止です。作業の前に、必ず安全カバーが閉まっているか確認してください。変更が一つあります。2号プレスの金型交換は午後に行います。午前中は使えません。"

        val draft = BriefingDraft(
            id = "b_${Instant.now().toEpochMilli().toString(36)}",
            text = mapOf(
                "ja" to transcript,
                "en" to "Good morning. Today’s safety focus is preventing hand entrapment in the press. Before work, always check that the safety cover is closed. One change: die replacement on Press #2 will take place in the afternoon. It cannot be used in the morning.",
                "bn" to "সুপ্রভাত। আজকের নিরাপত্তার মূল বিষয় হলো প্রেসে হাত আটকে যাওয়া রোধ করা। কাজ শুরুর আগে অবশ্যই সেফটি কভার বন্ধ আছে কিনা পরীক্ষা করুন। একটি পরিবর্তন আছে: ২ নম্বর প্রেসের ডাই পরিবর্তন বিকেলে হবে। সকালে এটি ব্যবহার করা যাবে না।",
            ),
            focusText = mapOf(
                "ja" to "2号プレス: カバー確認。手の挟まれ防止。",
                "en" to "Press #2: check cover. Prevent hand pinch.",
                "bn" to "প্রেস #২: কভার পরীক্ষা। হাত আটকা রোধ।",
            ),
            changeText = mapOf(
                "ja" to "2号プレスの金型交換は午後。午前中は使用不可。",
                "en" to "Press #2 die change in the afternoon. Morning unavailable.",
                "bn" to "প্রেস #২ ডাই পরিবর্তন বিকেলে। সকালে ব্যবহার নিষেধ।",
            ),
            question = CheckQuestionWithAnswer(
                id = "q_new",
                prompt = mapOf(
                    "ja" to "2号プレスの金型交換はいつですか？",
                    "en" to "When is die replacement on Press #2?",
                    "bn" to "প্রেস #২-এর ডাই পরিবর্তন কখন হবে?",
                ),
                options = listOf(
                    mapOf("ja" to "午後", "en" to "Afternoon", "bn" to "বিকেলে"),
                    mapOf("ja" to "午前", "en" to "Morning", "bn" to "সকালে"),
                    mapOf("ja" to "明日", "en" to "Tomorrow", "bn" to "কাল"),
                ),
                correctIndex = 0,
            ),
            transcriptJa = transcript,
            needsEdit = false,
        )
        return AppResult.Success(draft)
    }

    override suspend fun publishBriefing(request: BriefingPublishRequest): AppResult<BriefingForForeman> {
        if (delayMs > 0) delay(delayMs)
        val now = Instant.now().toString()
        val published = BriefingForForeman(
            id = request.id,
            text = request.text,
            focusText = request.focusText,
            changeText = request.changeText,
            createdAt = now,
            publishedAt = now,
            question = request.question,
            status = "published",
            needsEdit = false,
        )

        // Reset worker statuses for this new briefing
        val statuses = mutableMapOf<String, BriefingWorkerStatus>()
        workers.forEach { worker ->
            statuses[worker.id] = BriefingWorkerStatus(
                worker = worker,
                hasAttended = false,
                status = WakattaStatus.WAIT,
                answerIndex = null,
                answerText = null,
            )
        }
        _workerStatuses[published.id] = statuses

        _briefings.value = listOf(published) + _briefings.value
        return AppResult.Success(published)
    }

    override suspend fun getReceipts(briefingId: String): AppResult<BriefingReceipts> {
        if (delayMs > 0) delay(delayMs)
        val statusMap = _workerStatuses[briefingId] ?: emptyMap()
        val understood = mutableListOf<WorkerInfo>()
        val openedOnly = mutableListOf<WorkerInfo>()
        val notOpened = mutableListOf<WorkerInfo>()

        workers.forEach { worker ->
            val status = statusMap[worker.id]
            when {
                status == null || !status.hasAttended -> notOpened.add(worker)
                status.status == WakattaStatus.GREEN -> understood.add(worker)
                else -> openedOnly.add(worker)
            }
        }
        return AppResult.Success(
            BriefingReceipts(
                understood = understood,
                openedOnly = openedOnly,
                notOpened = notOpened,
            )
        )
    }

    override suspend fun getWorkerStatuses(briefingId: String): AppResult<List<BriefingWorkerStatus>> {
        if (delayMs > 0) delay(delayMs)
        val statusMap = _workerStatuses[briefingId] ?: emptyMap()
        val list = workers.map { worker ->
            statusMap[worker.id] ?: BriefingWorkerStatus(
                worker = worker,
                hasAttended = false,
                status = WakattaStatus.WAIT,
            )
        }
        return AppResult.Success(list)
    }

    override suspend fun sendReexplanation(briefingId: String, workerId: String): AppResult<Unit> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(Unit)
    }

    override suspend fun markOpened(briefingId: String): AppResult<Unit> {
        if (delayMs > 0) delay(delayMs)
        val workerId = workers.firstOrNull()?.id ?: "u_rahim"
        val statusMap = _workerStatuses.getOrPut(briefingId) { mutableMapOf() }
        val current = statusMap[workerId]
        if (current == null) {
            val worker = workers.find { it.id == workerId } ?: WorkerInfo(workerId, "Rahim Uddin", "bn")
            statusMap[workerId] = BriefingWorkerStatus(
                worker = worker,
                hasAttended = true,
                status = WakattaStatus.WAIT,
            )
        } else if (!current.hasAttended) {
            statusMap[workerId] = current.copy(hasAttended = true)
        }
        return AppResult.Success(Unit)
    }

    override suspend fun recordWorkerAnswer(
        briefingId: String,
        workerId: String,
        answerIndex: Int,
        status: WakattaStatus,
    ): AppResult<Unit> {
        if (delayMs > 0) delay(delayMs)
        val statusMap = _workerStatuses.getOrPut(briefingId) { mutableMapOf() }
        val briefing = _briefings.value.find { it.id == briefingId }
        val answerText = if (answerIndex >= 0 && briefing != null && answerIndex < briefing.question.options.size) {
            briefing.question.options[answerIndex]["ja"]
                ?: briefing.question.options[answerIndex]["en"]
                ?: ""
        } else if (answerIndex == -1) {
            "わからない"
        } else {
            null
        }

        val worker = workers.find { it.id == workerId } ?: WorkerInfo(workerId, "Rahim Uddin", "bn")
        statusMap[workerId] = BriefingWorkerStatus(
            worker = worker,
            hasAttended = true,
            status = status,
            answerIndex = answerIndex,
            answerText = answerText,
        )
        return AppResult.Success(Unit)
    }
}
