package com.techtsunagu.wakatta.feature.notices.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.NoticeCategory
import com.techtsunagu.wakatta.core.model.NoticeSourceKind
import com.techtsunagu.wakatta.core.model.NoticeStatus
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.feature.notices.model.CheckQuestionDto
import com.techtsunagu.wakatta.feature.notices.model.CheckQuestionWithAnswer
import com.techtsunagu.wakatta.feature.notices.model.CheckResultDto
import com.techtsunagu.wakatta.feature.notices.model.EventDto
import com.techtsunagu.wakatta.feature.notices.model.LifeSummary
import com.techtsunagu.wakatta.feature.notices.model.NoticeCreateRequest
import com.techtsunagu.wakatta.feature.notices.model.NoticeDraftDto
import com.techtsunagu.wakatta.feature.notices.model.NoticeForPoster
import com.techtsunagu.wakatta.feature.notices.model.NoticeForWorker
import com.techtsunagu.wakatta.feature.notices.model.NoticeWorkerStatus
import com.techtsunagu.wakatta.feature.notices.model.PublishResult
import com.techtsunagu.wakatta.feature.notices.model.StepDto
import com.techtsunagu.wakatta.feature.notices.model.TodayWordInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeNoticeRepository(
    private val delayMs: Long = 200L,
) : NoticeRepository {

    @Inject
    constructor() : this(200L)

    private val workers: List<WorkerInfo> = DemoSeed.users
        .filter { it.role == Role.WORKER }
        .map { WorkerInfo(id = it.id, displayName = it.displayName, language = it.language) }

    private val _noticesForWorker = MutableStateFlow<List<NoticeForWorker>>(emptyList())
    private val _noticesForPoster = MutableStateFlow<List<NoticeForPoster>>(emptyList())
    private val _allEvents = MutableStateFlow<List<EventDto>>(emptyList())
    private val correctAnswers = mutableMapOf<String, Int>()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val nBurnEvents = listOf(
            EventDto(
                id = "ev_burn_1",
                noticeId = "n_burn",
                title = mapOf("ja" to "燃えるごみの日", "en" to "Burnable garbage day", "bn" to "দাহ্য বর্জ্যের দিন"),
                date = "2026-11-26",
                time = "8:00",
                confirmed = true,
            ),
            EventDto(
                id = "ev_burn_2",
                noticeId = "n_burn",
                title = mapOf("ja" to "燃えるごみの日", "en" to "Burnable garbage day", "bn" to "দাহ্য বর্জ্যের দিন"),
                date = "2026-11-30",
                time = "8:00",
                confirmed = true,
            ),
            EventDto(
                id = "ev_burn_3",
                noticeId = "n_burn",
                title = mapOf("ja" to "燃えるごみの日", "en" to "Burnable garbage day", "bn" to "দাহ্য বর্জ্যের দিন"),
                date = "2026-12-03",
                time = "8:00",
                confirmed = true,
            ),
        )

        val nBurnQuestionWithAnswer = CheckQuestionWithAnswer(
            id = "q_burn",
            prompt = mapOf(
                "ja" to "何時までにごみを出しますか？",
                "en" to "By what time must the garbage be out?",
                "bn" to "কয়টার মধ্যে ময়লা ফেলতে হবে?",
            ),
            options = listOf(
                mapOf("ja" to "9:00", "en" to "9:00", "bn" to "৯:০০"),
                mapOf("ja" to "8:00", "en" to "8:00", "bn" to "৮:০০"),
                mapOf("ja" to "いつでも", "en" to "Any time", "bn" to "যেকোনো সময়"),
            ),
            correctIndex = 1,
        )

        val nBurnWorker = NoticeForWorker(
            id = "n_burn",
            title = mapOf(
                "ja" to "燃えるごみ 月・木 8:00まで",
                "en" to "Burnable garbage: Mon and Thu, by 8:00",
                "bn" to "দাহ্য বর্জ্য সোম ও বৃহস্পতি, ৮:০০ এর মধ্যে",
            ),
            category = NoticeCategory.GARBAGE,
            sourceKind = NoticeSourceKind.PHOTO,
            photoSeed = "lf-burn",
            from = "自治会",
            publishedAt = "2026-11-10",
            steps = listOf(
                StepDto(
                    text = mapOf(
                        "ja" to "月曜日と木曜日に出します。",
                        "en" to "Put it out on Monday and Thursday.",
                        "bn" to "সোমবার এবং বৃহস্পতিবার ফেলুন।",
                    ),
                    icon = "event",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "朝8:00までに、ごみ置き場に出します。",
                        "en" to "Take it to the garbage station by 8:00 a.m.",
                        "bn" to "সকাল ৮:০০ টার মধ্যে ময়লা ফেলার স্থানে দিন।",
                    ),
                    icon = "clock",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "長崎市の指定ごみ袋に入れて、口をしばります。",
                        "en" to "Use the official city garbage bag and tie it closed.",
                        "bn" to "সিটি করপোরেশনের নির্দিষ্ট ব্যাগে রাখুন ও মুখ বাঁধুন।",
                    ),
                    icon = "bag",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "8:00をすぎたら、出さないでください。",
                        "en" to "Do not put it out after 8:00.",
                        "bn" to "৮:০০ টার পর ময়লা ফেলবেন না।",
                    ),
                    icon = "warning",
                ),
            ),
            events = nBurnEvents,
            question = CheckQuestionDto(
                id = nBurnQuestionWithAnswer.id,
                prompt = nBurnQuestionWithAnswer.prompt,
                options = nBurnQuestionWithAnswer.options,
            ),
            opened = false,
            passed = null,
            userAnswer = null,
            wakattaStatus = WakattaStatus.WAIT,
        )

        val nBurnPoster = NoticeForPoster(
            id = "n_burn",
            title = nBurnWorker.title,
            category = NoticeCategory.GARBAGE,
            sourceKind = NoticeSourceKind.PHOTO,
            photoSeed = "lf-burn",
            from = "自治会",
            publishedAt = "2026-11-10",
            steps = nBurnWorker.steps,
            events = nBurnEvents,
            question = nBurnQuestionWithAnswer,
            status = NoticeStatus.PUBLISHED,
            workerStatuses = listOf(
                NoticeWorkerStatus(workers[0], WakattaStatus.WAIT, null, null),
                NoticeWorkerStatus(workers.getOrElse(1) { WorkerInfo("u_nguyen", "Nguyen", "vi") }, WakattaStatus.GREEN, 1, "8:00"),
                NoticeWorkerStatus(workers.getOrElse(2) { WorkerInfo("u_siti", "Siti", "id") }, WakattaStatus.GREEN, 1, "8:00"),
            ),
        )

        val nResEvents = listOf(
            EventDto(
                id = "ev_res_1",
                noticeId = "n_res",
                title = mapOf("ja" to "資源ごみの日", "en" to "Recyclables day", "bn" to "পুনর্ব্যবহারযোগ্য বর্জ্যের দিন"),
                date = "2026-11-25",
                time = "8:00",
                confirmed = true,
            ),
        )

        val nResQuestionWithAnswer = CheckQuestionWithAnswer(
            id = "q_res",
            prompt = mapOf(
                "ja" to "ペットボトルは何をはずしますか？",
                "en" to "What do you remove from a PET bottle?",
                "bn" to "প্লাস্টিক বোতল থেকে কী খুলতে হবে?",
            ),
            options = listOf(
                mapOf("ja" to "底", "en" to "The bottom", "bn" to "তলার অংশ"),
                mapOf("ja" to "ふたとラベル", "en" to "The cap and the label", "bn" to "ক্যাপ এবং লেবেল"),
                mapOf("ja" to "なにもしない", "en" to "Nothing", "bn" to "কিছুই না"),
            ),
            correctIndex = 1,
        )

        val nResWorker = NoticeForWorker(
            id = "n_res",
            title = mapOf(
                "ja" to "資源ごみの分け方",
                "en" to "How to sort recyclables",
                "bn" to "পুনর্ব্যবহারযোগ্য বর্জ্য কীভাবে বাছাই করবেন",
            ),
            category = NoticeCategory.GARBAGE,
            sourceKind = NoticeSourceKind.TEXT,
            sourceText = "【自治会より】資源ごみは水曜日です。びん・缶・ペットボトルはかるく洗って、べつべつの箱に入れてください。ペットボトルはふたとラベルをはずしてください。",
            sourceTranslation = mapOf(
                "en" to "From the association: Recyclables go out on Wednesday. Rinse bottles, cans and PET bottles lightly and put them in separate boxes. Take the cap and label off PET bottles.",
                "bn" to "কমিউনিটি থেকে: পুনর্ব্যবহারযোগ্য বর্জ্য বুধবার নেওয়া হয়। বোতল, ক্যান এবং প্লাস্টিক বোতল হালকা ধুয়ে আলাদা বাক্সে রাখুন। ক্যাপ ও লেবেল খুলে ফেলুন।",
            ),
            from = "自治会",
            publishedAt = "2026-11-12",
            steps = listOf(
                StepDto(
                    text = mapOf(
                        "ja" to "びん・缶・ペットボトルは、水でかるく洗います。",
                        "en" to "Rinse bottles, cans and PET bottles lightly with water.",
                        "bn" to "বোতল, ক্যান ও প্লাস্টিক বোতল জল দিয়ে হালকা ধুয়ে নিন।",
                    ),
                    icon = "water",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "ペットボトルは、ふたとラベルをはずします。",
                        "en" to "For PET bottles, remove the cap and the label.",
                        "bn" to "প্লাস্টিক বোতলের ক্যাপ ও লেবেল খুলে ফেলুন।",
                    ),
                    icon = "info",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "びん・缶・ペットボトルを、べつべつの箱に入れます。",
                        "en" to "Put bottles, cans and PET bottles in separate boxes.",
                        "bn" to "বোতল, ক্যান ও প্লাস্টিক বোতল আলাদা বাক্সে রাখুন।",
                    ),
                    icon = "bag",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "紙は、ひもでしばって出します。",
                        "en" to "Tie paper up with string.",
                        "bn" to "কাগজ দড়ি দিয়ে বেঁধে দিন।",
                    ),
                    icon = "paper",
                ),
            ),
            events = nResEvents,
            question = CheckQuestionDto(
                id = nResQuestionWithAnswer.id,
                prompt = nResQuestionWithAnswer.prompt,
                options = nResQuestionWithAnswer.options,
            ),
            opened = true,
            passed = true,
            userAnswer = 1,
            wakattaStatus = WakattaStatus.GREEN,
        )

        val nResPoster = NoticeForPoster(
            id = "n_res",
            title = nResWorker.title,
            category = NoticeCategory.GARBAGE,
            sourceKind = NoticeSourceKind.TEXT,
            sourceText = nResWorker.sourceText,
            sourceTranslation = nResWorker.sourceTranslation,
            from = "自治会",
            publishedAt = "2026-11-12",
            steps = nResWorker.steps,
            events = nResEvents,
            question = nResQuestionWithAnswer,
            status = NoticeStatus.PUBLISHED,
            workerStatuses = listOf(
                NoticeWorkerStatus(workers[0], WakattaStatus.GREEN, 1, "ふたとラベル"),
                NoticeWorkerStatus(workers.getOrElse(1) { WorkerInfo("u_nguyen", "Nguyen", "vi") }, WakattaStatus.ORANGE, -1, "わからない"),
                NoticeWorkerStatus(workers.getOrElse(2) { WorkerInfo("u_siti", "Siti", "id") }, WakattaStatus.GREEN, 1, "ふたとラベル"),
            ),
        )

        val nDrillEvents = listOf(
            EventDto(
                id = "ev_drill_1",
                noticeId = "n_drill",
                title = mapOf("ja" to "防災訓練（公民館）", "en" to "Disaster drill (community hall)", "bn" to "দুর্যোগ মহড়া (কমিউনিটি হল)"),
                date = "2026-12-07",
                time = "9:00",
                confirmed = true,
            ),
        )

        val nDrillQuestionWithAnswer = CheckQuestionWithAnswer(
            id = "q_drill",
            prompt = mapOf(
                "ja" to "12月7日はどこに行きますか？",
                "en" to "Where do you go on Dec 7?",
                "bn" to "৭ ডিসেম্বর কোথায় যাবেন?",
            ),
            options = listOf(
                mapOf("ja" to "市役所", "en" to "City office", "bn" to "সিটি অফিস"),
                mapOf("ja" to "公民館", "en" to "Community hall", "bn" to "কমিউনিটি হল"),
                mapOf("ja" to "工場", "en" to "Factory", "bn" to "কারখানা"),
            ),
            correctIndex = 1,
        )

        val nDrillWorker = NoticeForWorker(
            id = "n_drill",
            title = mapOf(
                "ja" to "地区の防災訓練 12/7 9:00 公民館",
                "en" to "Neighborhood disaster drill: Dec 7, 9:00, community hall",
                "bn" to "এলাকার দুর্যোগ মহড়া: ৭ ডিসেম্বর ৯:০০, কমিউনিটি হল",
            ),
            category = NoticeCategory.DISASTER,
            sourceKind = NoticeSourceKind.PHOTO,
            photoSeed = "lf-drill",
            from = "自治会",
            publishedAt = "2026-11-18",
            steps = listOf(
                StepDto(
                    text = mapOf(
                        "ja" to "12月7日（月）9:00に、公民館に行きます。",
                        "en" to "Go to the community hall at 9:00 on Monday, Dec 7.",
                        "bn" to "সোমবার ৭ ডিসেম্বর সকাল ৯:০০ টায় কমিউনিটি হলে যান।",
                    ),
                    icon = "place",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "まず、地震のときの逃げかたを練習します。",
                        "en" to "First, practice how to get safe in an earthquake.",
                        "bn" to "প্রথমে ভূমিকম্পের সময় কীভাবে বাঁচতে হয় তা অনুশীলন করুন।",
                    ),
                    icon = "warning",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "消火器の使いかたも練習します。",
                        "en" to "Also practice how to use a fire extinguisher.",
                        "bn" to "অগ্নি নির্বাপক যন্ত্র ব্যবহারের অনুশীলনও করুন।",
                    ),
                    icon = "warning",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "動きやすい服と、くつで行きます。",
                        "en" to "Wear comfortable clothes and shoes.",
                        "bn" to "সহজে চলাফেরা করা যায় এমন পোশাক ও জুতো পরুন।",
                    ),
                    icon = "info",
                ),
            ),
            events = nDrillEvents,
            question = CheckQuestionDto(
                id = nDrillQuestionWithAnswer.id,
                prompt = nDrillQuestionWithAnswer.prompt,
                options = nDrillQuestionWithAnswer.options,
            ),
            opened = false,
            passed = null,
            userAnswer = null,
            wakattaStatus = WakattaStatus.WAIT,
        )

        val nDrillPoster = NoticeForPoster(
            id = "n_drill",
            title = nDrillWorker.title,
            category = NoticeCategory.DISASTER,
            sourceKind = NoticeSourceKind.PHOTO,
            photoSeed = "lf-drill",
            from = "自治会",
            publishedAt = "2026-11-18",
            steps = nDrillWorker.steps,
            events = nDrillEvents,
            question = nDrillQuestionWithAnswer,
            status = NoticeStatus.PUBLISHED,
            workerStatuses = listOf(
                NoticeWorkerStatus(workers[0], WakattaStatus.WAIT, null, null),
                NoticeWorkerStatus(workers.getOrElse(1) { WorkerInfo("u_nguyen", "Nguyen", "vi") }, WakattaStatus.GREEN, 1, "公民館"),
                NoticeWorkerStatus(workers.getOrElse(2) { WorkerInfo("u_siti", "Siti", "id") }, WakattaStatus.RED, 0, "市役所"),
            ),
        )

        val nFestEvents = listOf(
            EventDto(
                id = "ev_fest_1",
                noticeId = "n_fest",
                title = mapOf("ja" to "秋祭り（神社）", "en" to "Autumn festival (shrine)", "bn" to "শরতের উৎসব (মন্দির)"),
                date = "2026-11-30",
                time = "17:00",
                confirmed = true,
            ),
        )

        val nFestQuestionWithAnswer = CheckQuestionWithAnswer(
            id = "q_fest",
            prompt = mapOf(
                "ja" to "秋祭りはどこでありますか？",
                "en" to "Where is the autumn festival?",
                "bn" to "শরতের উৎসব কোথায় হবে?",
            ),
            options = listOf(
                mapOf("ja" to "工場", "en" to "At the factory", "bn" to "কারখানায়"),
                mapOf("ja" to "神社", "en" to "At the shrine", "bn" to "মন্দিরে"),
                mapOf("ja" to "駅", "en" to "At the station", "bn" to "স্টেশনে"),
            ),
            correctIndex = 1,
        )

        val nFestWorker = NoticeForWorker(
            id = "n_fest",
            title = mapOf(
                "ja" to "秋祭り 11/30",
                "en" to "Autumn festival: Nov 30",
                "bn" to "শরতের উৎসব: ৩০ নভেম্বর",
            ),
            category = NoticeCategory.EVENT,
            sourceKind = NoticeSourceKind.VOICE,
            audioDuration = "0:24",
            sourceText = "自治会長より。11月30日、月曜日の夕方5時から、神社で秋祭りがあります。だれでも来てください。",
            sourceTranslation = mapOf(
                "en" to "From the association head: There is an autumn festival at the shrine on Monday, Nov 30, from 5 p.m. Everyone is welcome.",
                "bn" to "কমিউনিটি প্রধান থেকে: সোমবার ৩০ নভেম্বর বিকেল ৫টা থেকে মন্দিরে শরতের উৎসব হবে। সবাই আমন্ত্রিত।",
            ),
            from = "自治会",
            publishedAt = "2026-11-20",
            steps = listOf(
                StepDto(
                    text = mapOf(
                        "ja" to "11月30日（月）17:00から、神社で秋祭りがあります。",
                        "en" to "The autumn festival is at the shrine on Monday, Nov 30, from 5:00 p.m.",
                        "bn" to "সোমবার ৩০ নভেম্বর বিকেল ১৭:০০ টা থেকে মন্দিরে শরতের উৎসব হবে।",
                    ),
                    icon = "event",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "だれでも行けます。家族や友だちといっしょに来てください。",
                        "en" to "Everyone can come. Bring your family or friends.",
                        "bn" to "যে কেউ যেতে পারেন। পরিবার ও বন্ধুদের নিয়ে আসুন।",
                    ),
                    icon = "groups",
                ),
                StepDto(
                    text = mapOf(
                        "ja" to "食べものや飲みものは、お店で買えます。",
                        "en" to "You can buy food and drinks at the stalls.",
                        "bn" to "দোকান থেকে খাবার ও পানীয় কিনতে পারবেন।",
                    ),
                    icon = "bag",
                ),
            ),
            events = nFestEvents,
            question = CheckQuestionDto(
                id = nFestQuestionWithAnswer.id,
                prompt = nFestQuestionWithAnswer.prompt,
                options = nFestQuestionWithAnswer.options,
            ),
            opened = true,
            passed = true,
            userAnswer = 1,
            wakattaStatus = WakattaStatus.GREEN,
        )

        val nFestPoster = NoticeForPoster(
            id = "n_fest",
            title = nFestWorker.title,
            category = NoticeCategory.EVENT,
            sourceKind = NoticeSourceKind.VOICE,
            audioDuration = "0:24",
            sourceText = nFestWorker.sourceText,
            sourceTranslation = nFestWorker.sourceTranslation,
            from = "自治会",
            publishedAt = "2026-11-20",
            steps = nFestWorker.steps,
            events = nFestEvents,
            question = nFestQuestionWithAnswer,
            status = NoticeStatus.PUBLISHED,
            workerStatuses = listOf(
                NoticeWorkerStatus(workers[0], WakattaStatus.GREEN, 1, "神社"),
                NoticeWorkerStatus(workers.getOrElse(1) { WorkerInfo("u_nguyen", "Nguyen", "vi") }, WakattaStatus.GREEN, 1, "神社"),
                NoticeWorkerStatus(workers.getOrElse(2) { WorkerInfo("u_siti", "Siti", "id") }, WakattaStatus.GREEN, 1, "神社"),
            ),
        )

        correctAnswers["n_burn"] = nBurnQuestionWithAnswer.correctIndex
        correctAnswers["n_res"] = nResQuestionWithAnswer.correctIndex
        correctAnswers["n_drill"] = nDrillQuestionWithAnswer.correctIndex
        correctAnswers["n_fest"] = nFestQuestionWithAnswer.correctIndex

        _noticesForWorker.value = listOf(nBurnWorker, nResWorker, nDrillWorker, nFestWorker)
        _noticesForPoster.value = listOf(nBurnPoster, nResPoster, nDrillPoster, nFestPoster)
        _allEvents.value = (nBurnEvents + nResEvents + nDrillEvents + nFestEvents).sortedBy { it.date }
    }

    override fun observeNoticesForWorker(): Flow<List<NoticeForWorker>> = _noticesForWorker.asStateFlow()

    override suspend fun getNoticesForWorker(): AppResult<List<NoticeForWorker>> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(_noticesForWorker.value)
    }

    override suspend fun getNoticesForPoster(): AppResult<List<NoticeForPoster>> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(_noticesForPoster.value)
    }

    override suspend fun getNoticeForWorker(id: String): AppResult<NoticeForWorker> {
        if (delayMs > 0) delay(delayMs)
        val notice = _noticesForWorker.value.find { it.id == id }
        return if (notice != null) {
            AppResult.Success(notice)
        } else {
            AppResult.Failure(AppError.NotFound)
        }
    }

    override suspend fun getNoticeForPoster(id: String): AppResult<NoticeForPoster> {
        if (delayMs > 0) delay(delayMs)
        val notice = _noticesForPoster.value.find { it.id == id }
        return if (notice != null) {
            AppResult.Success(notice)
        } else {
            AppResult.Failure(AppError.NotFound)
        }
    }

    override suspend fun getUpcomingEvents(): AppResult<List<EventDto>> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(_allEvents.value)
    }

    override suspend fun toggleReminder(eventId: String): AppResult<Boolean> {
        if (delayMs > 0) delay(delayMs)
        var newState = false
        _allEvents.value = _allEvents.value.map { ev ->
            if (ev.id == eventId) {
                newState = !ev.isReminderOn
                ev.copy(isReminderOn = newState)
            } else {
                ev
            }
        }
        _noticesForWorker.value = _noticesForWorker.value.map { notice ->
            notice.copy(events = notice.events.map { ev ->
                if (ev.id == eventId) ev.copy(isReminderOn = newState) else ev
            })
        }
        return AppResult.Success(newState)
    }

    override suspend fun markNoticeOpened(id: String): AppResult<Unit> {
        if (delayMs > 0) delay(delayMs)
        _noticesForWorker.value = _noticesForWorker.value.map { notice ->
            if (notice.id == id && !notice.opened) {
                notice.copy(opened = true)
            } else {
                notice
            }
        }
        return AppResult.Success(Unit)
    }

    override suspend fun submitNoticeCheck(id: String, answerIndex: Int): AppResult<CheckResultDto> {
        if (delayMs > 0) delay(delayMs)
        val notice = _noticesForWorker.value.find { it.id == id }
            ?: return AppResult.Failure(AppError.NotFound)
        val correct = correctAnswers[id] ?: 0

        val (passed, status, msg) = when {
            answerIndex == -1 -> Triple(
                false,
                WakattaStatus.ORANGE,
                "Thanks for telling us. Your foreman will explain it again.",
            )
            answerIndex == correct -> Triple(
                true,
                WakattaStatus.GREEN,
                "Wakatta! That is right.",
            )
            else -> {
                val correctText = notice.question.options.getOrNull(correct)?.get("en")
                    ?: notice.question.options.getOrNull(correct)?.get("ja")
                    ?: ""
                Triple(
                    false,
                    WakattaStatus.RED,
                    "Not quite. The answer is \"$correctText\".",
                )
            }
        }

        _noticesForWorker.value = _noticesForWorker.value.map { n ->
            if (n.id == id) {
                n.copy(
                    opened = true,
                    passed = passed,
                    userAnswer = answerIndex,
                    wakattaStatus = status,
                )
            } else {
                n
            }
        }

        _noticesForPoster.value = _noticesForPoster.value.map { p ->
            if (p.id == id) {
                val chosenText = if (answerIndex >= 0) {
                    p.question.options.getOrNull(answerIndex)?.get("ja") ?: ""
                } else {
                    "わからない"
                }
                val updatedStatuses = p.workerStatuses.map { ws ->
                    if (ws.worker.id == "u_rahim") {
                        ws.copy(status = status, answerIndex = answerIndex, answerText = chosenText)
                    } else {
                        ws
                    }
                }
                p.copy(workerStatuses = updatedStatuses)
            } else {
                p
            }
        }

        val correctStr = notice.question.options.getOrNull(correct)?.get("ja")
        return AppResult.Success(
            CheckResultDto(
                passed = passed,
                status = status,
                message = msg,
                correctAnswerText = correctStr,
            )
        )
    }

    override suspend fun createDraft(request: NoticeCreateRequest): AppResult<NoticeDraftDto> {
        if (delayMs > 0) delay(delayMs)
        return when (request.sampleId) {
            "smp_fire" -> AppResult.Success(
                NoticeDraftDto(
                    id = "draft_fire",
                    sourceKind = NoticeSourceKind.TEXT,
                    sourceText = "12月20日から25日まで、毎晩20時より町内の火の用心夜警を行います。集合場所は公民館前です。",
                    seed = "lf-fire",
                    titleJa = "夜警・火の用心",
                    titleEn = "Night patrol: fire watch",
                    titleBn = "রাতের প্রহরা: অগ্নি সতর্কতা",
                    stepsJa = listOf(
                        "12月20日〜25日、毎晩20:00に公民館前に集合します。",
                        "拍子木を鳴らしながら、町内を巡回します。",
                        "暖かい服装と動きやすい靴で参加してください。",
                    ),
                    stepsEn = listOf(
                        "Gather at the community hall at 20:00 nightly, Dec 20–25.",
                        "Patrol the neighborhood sounding the wooden clappers.",
                        "Wear warm clothes and comfortable shoes.",
                    ),
                    events = listOf(
                        EventDto(
                            id = "ev_draft_fire_1",
                            noticeId = "draft_fire",
                            title = mapOf("ja" to "火の用心夜警", "en" to "Fire watch patrol", "bn" to "অগ্নি সতর্কতা প্রহরা"),
                            date = "2026-12-20",
                            time = "20:00",
                            confirmed = true,
                        )
                    ),
                    question = CheckQuestionWithAnswer(
                        id = "q_draft_fire",
                        prompt = mapOf("ja" to "集合場所はどこですか？", "en" to "Where is the meeting place?", "bn" to "জমায়েতের স্থান কোথায়?"),
                        options = listOf(
                            mapOf("ja" to "駅前", "en" to "Station", "bn" to "স্টেশন"),
                            mapOf("ja" to "公民館前", "en" to "Community hall", "bn" to "কমিউনিটি হল"),
                            mapOf("ja" to "東公園", "en" to "East Park", "bn" to "ইস্ট পার্ক"),
                        ),
                        correctIndex = 1,
                    ),
                )
            )
            "smp_recycle" -> AppResult.Success(
                NoticeDraftDto(
                    id = "draft_recycle",
                    sourceKind = NoticeSourceKind.TEXT,
                    sourceText = "12月6日（日）朝8時までに、新聞・段ボールをひもでしばって玄関前に出してください。",
                    seed = "lf-paper",
                    titleJa = "古紙回収",
                    titleEn = "Waste paper collection",
                    titleBn = "পুরনো কাগজ সংগ্রহ",
                    stepsJa = listOf(
                        "12月6日（日）朝8:00までに玄関前に出します。",
                        "新聞と段ボールは別々にひもで十字にしばります。",
                        "雨の日は濡れないようビニールシート等をかけてください。",
                    ),
                    stepsEn = listOf(
                        "Put it in front of your door by 8:00 a.m. on Sunday, Dec 6.",
                        "Tie newspaper and cardboard separately with string in a cross shape.",
                        "If it rains, cover with a sheet to keep it dry.",
                    ),
                    events = listOf(
                        EventDto(
                            id = "ev_draft_recycle_1",
                            noticeId = "draft_recycle",
                            title = mapOf("ja" to "古紙回収の日", "en" to "Waste paper collection", "bn" to "পুরনো কাগজ সংগ্রহ"),
                            date = "2026-12-06",
                            time = "8:00",
                            confirmed = true,
                        )
                    ),
                    question = CheckQuestionWithAnswer(
                        id = "q_draft_recycle",
                        prompt = mapOf("ja" to "紙はどうやって出しますか？", "en" to "How do you put out the paper?", "bn" to "কাগজ কীভাবে দেবেন?"),
                        options = listOf(
                            mapOf("ja" to "袋に入れる", "en" to "In a bag", "bn" to "ব্যাগে"),
                            mapOf("ja" to "ひもでしばる", "en" to "Tied with string", "bn" to "দড়ি দিয়ে বেঁধে"),
                            mapOf("ja" to "そのまま", "en" to "As is", "bn" to "যেমন আছে"),
                        ),
                        correctIndex = 1,
                    ),
                )
            )
            else -> AppResult.Success(
                NoticeDraftDto(
                    id = "draft_clean",
                    sourceKind = request.sourceKind,
                    sourceText = request.text ?: "12月13日（日）朝8時から、東公園で大掃除をします。軍手とゴミ袋を持ってきてください。",
                    seed = "lf-clean",
                    titleJa = "町内一斉清掃",
                    titleEn = "Neighborhood cleanup",
                    titleBn = "এলাকা পরিচ্ছন্নতা অভিযান",
                    stepsJa = listOf(
                        "12月13日（日）8:00に東公園に集まります。",
                        "公園の落ち葉拾いとゴミ拾いをします。",
                        "軍手を持参してください。ゴミ袋は配られます。",
                    ),
                    stepsEn = listOf(
                        "Meet at East Park at 8:00 on Sunday, Dec 13.",
                        "Pick up leaves and litter in the park.",
                        "Bring work gloves. Trash bags will be handed out.",
                    ),
                    events = listOf(
                        EventDto(
                            id = "ev_draft_clean_1",
                            noticeId = "draft_clean",
                            title = mapOf("ja" to "東公園で大掃除", "en" to "East Park Clean-up", "bn" to "ইস্ট পার্কে পরিচ্ছন্নতা"),
                            date = "2026-12-13",
                            time = "8:00",
                            confirmed = true,
                        )
                    ),
                    question = CheckQuestionWithAnswer(
                        id = "q_draft_clean",
                        prompt = mapOf("ja" to "何時から大掃除をしますか？", "en" to "What time does cleanup start?", "bn" to "কয়টা থেকে পরিচ্ছন্নতা শুরু?"),
                        options = listOf(
                            mapOf("ja" to "7:00", "en" to "7:00", "bn" to "৭:০০"),
                            mapOf("ja" to "8:00", "en" to "8:00", "bn" to "৮:০০"),
                            mapOf("ja" to "9:00", "en" to "9:00", "bn" to "৯:০০"),
                        ),
                        correctIndex = 1,
                    ),
                )
            )
        }
    }

    override suspend fun toggleEventConfirmed(eventId: String): AppResult<EventDto> {
        if (delayMs > 0) delay(delayMs)
        val event = _allEvents.value.find { it.id == eventId }
            ?: return AppResult.Failure(AppError.NotFound)
        val updated = event.copy(confirmed = !event.confirmed)
        _allEvents.value = _allEvents.value.map { if (it.id == eventId) updated else it }
        return AppResult.Success(updated)
    }

    override suspend fun publishNotice(
        draftId: String,
        title: Tri,
        steps: List<StepDto>,
        events: List<EventDto>,
        question: CheckQuestionWithAnswer,
    ): AppResult<PublishResult> {
        if (delayMs > 0) delay(delayMs)
        // Rule: Events must be confirmed before publishing!
        val unconfirmed = events.any { !it.confirmed }
        if (unconfirmed) {
            return AppResult.Success(
                PublishResult(
                    success = false,
                    error = "EVENTS_NOT_CONFIRMED",
                )
            )
        }

        val newId = "n_${System.currentTimeMillis().toString(36)}"
        correctAnswers[newId] = question.correctIndex

        val newWorkerNotice = NoticeForWorker(
            id = newId,
            title = title,
            category = NoticeCategory.EVENT,
            sourceKind = NoticeSourceKind.TEXT,
            from = "自治会",
            publishedAt = "2026-11-24",
            steps = steps,
            events = events.map { it.copy(noticeId = newId) },
            question = CheckQuestionDto(
                id = question.id,
                prompt = question.prompt,
                options = question.options,
            ),
            opened = false,
            passed = null,
            userAnswer = null,
            wakattaStatus = WakattaStatus.WAIT,
        )

        val newPosterNotice = NoticeForPoster(
            id = newId,
            title = title,
            category = NoticeCategory.EVENT,
            sourceKind = NoticeSourceKind.TEXT,
            from = "自治会",
            publishedAt = "2026-11-24",
            steps = steps,
            events = events.map { it.copy(noticeId = newId) },
            question = question,
            status = NoticeStatus.PUBLISHED,
            workerStatuses = workers.map { NoticeWorkerStatus(it, WakattaStatus.WAIT, null, null) },
        )

        _noticesForWorker.value = listOf(newWorkerNotice) + _noticesForWorker.value
        _noticesForPoster.value = listOf(newPosterNotice) + _noticesForPoster.value
        _allEvents.value = (events.map { it.copy(noticeId = newId) } + _allEvents.value).sortedBy { it.date }

        return AppResult.Success(PublishResult(success = true, noticeId = newId))
    }

    override suspend fun getTodayWord(): AppResult<TodayWordInfo?> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(
            TodayWordInfo(
                term = "ধন্যবাদ",
                reading = "dhonnobad",
                meaning = mapOf("ja" to "ありがとう", "en" to "Thank you", "bn" to "ধন্যবাদ"),
                workerName = "Rahim",
                languageName = "ベンガル語",
            )
        )
    }

    override suspend fun getLifeSummary(): AppResult<LifeSummary> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(LifeSummary())
    }
}
