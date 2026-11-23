package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.AskDto
import com.techtsunagu.wakatta.feature.asks.model.AskKind
import com.techtsunagu.wakatta.feature.asks.model.AskStatus
import com.techtsunagu.wakatta.feature.asks.model.NoticeMatchDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class FakeAskRepository(
    private var simulatedLatencyMs: Long = 250L,
) : AskRepository {

    @Inject
    constructor() : this(250L)

    fun setLatency(latencyMs: Long) {
        simulatedLatencyMs = latencyMs
    }

    private val asksFlow = MutableStateFlow<List<AskDto>>(createInitialAsks())

    override fun getAsks(workerId: String?): Flow<List<AskDto>> {
        return asksFlow.map { list ->
            if (workerId == null) list else list.filter { it.workerId == workerId }
        }
    }

    override suspend fun sendAsk(workerId: String, request: AskCreateRequest): AppResult<AskDto> {
        delay(simulatedLatencyMs)
        val text = request.text.trim()
        if (text.isEmpty()) {
            return AppResult.Failure(AppError.Validation(listOf("text")))
        }

        val id = "ak_" + System.currentTimeMillis().toString(36)
        val match = matchNoticeByQuery(text)
        val kind = if (request.isVoice) AskKind.VOICE else AskKind.TEXT
        val status = if (match != null) AskStatus.ANSWERED else AskStatus.FORWARDED

        val newAsk = AskDto(
            id = id,
            workerId = workerId,
            workerName = resolveWorkerName(workerId),
            text = text,
            textJa = translateQueryToJa(text),
            kind = kind,
            covered = match != null,
            matchNotice = match,
            sourceNoticeIds = if (match != null) listOf(match.id) else emptyList(),
            status = status,
            senpaiName = if (match == null) "佐藤 美咲" else null,
            senpaiEnName = if (match == null) "Sato Misaki" else null,
            createdAt = "11/24 09:30",
        )

        val current = asksFlow.value.toMutableList()
        current.add(newAsk)
        asksFlow.value = current
        return AppResult.Success(newAsk)
    }

    override suspend fun forwardAsk(askId: String): AppResult<AskDto> {
        delay(simulatedLatencyMs)
        val current = asksFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == askId }
        if (index < 0) return AppResult.Failure(AppError.NotFound)

        val ask = current[index]
        if (ask.workerId == "u_unpaired") {
            return AppResult.Failure(AppError.Conflict("NO_ACTIVE_PAIR"))
        }

        val updated = ask.copy(
            status = AskStatus.FORWARDED,
            senpaiName = "佐藤 美咲",
            senpaiEnName = "Sato Misaki",
        )
        current[index] = updated
        asksFlow.value = current
        return AppResult.Success(updated)
    }

    override suspend fun replyAsk(
        askId: String,
        replyJa: String,
        replyEn: String,
        replyBn: String,
    ): AppResult<AskDto> {
        delay(simulatedLatencyMs)
        val current = asksFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == askId }
        if (index < 0) return AppResult.Failure(AppError.NotFound)

        val ask = current[index]
        val updated = ask.copy(
            status = AskStatus.REPLIED,
            replyJa = replyJa,
            replyEn = replyEn,
            replyBn = replyBn,
            senpaiName = "佐藤 美咲",
            senpaiEnName = "Sato Misaki",
        )
        current[index] = updated
        asksFlow.value = current
        return AppResult.Success(updated)
    }

    private fun resolveWorkerName(workerId: String): String = when (workerId) {
        "u_rahim" -> "Rahim Uddin"
        "u_nguyen" -> "Nguyen Van An"
        "u_siti" -> "Siti Rahma"
        else -> "Rahim Uddin"
    }

    private fun translateQueryToJa(q: String): String = when {
        q.contains("garbage", ignoreCase = true) || q.contains("ময়লা") -> "ごみの日はいつですか？"
        q.contains("drill", ignoreCase = true) || q.contains("মহড়া") -> "訓練はどこですか？"
        q.contains("bus", ignoreCase = true) || q.contains("বাস") -> "バスの定期券はどうやって買いますか？"
        q.contains("money", ignoreCase = true) || q.contains("টাকা") -> "安く仕送りできるところはどこですか？"
        else -> "$q（原文）"
    }

    private fun matchNoticeByQuery(q: String): NoticeMatchDto? {
        val low = q.lowercase()
        return when {
            listOf("garbage", "trash", "rubbish", "burnable", "ごみ", "ゴミ", "燃える", "বর্জ্য", "ময়লা").any { low.contains(it) } ->
                NoticeMatchDto(
                    id = "n_burn",
                    title = mapOf(
                        "ja" to "燃えるごみ 月・木 8:00まで",
                        "en" to "Burnable garbage: Mon and Thu, by 8:00",
                        "bn" to "পোড়ানো বর্জ্য: সোম ও বৃহস্পতি, ৮:০০ মধ্যে",
                    ),
                    answer = mapOf(
                        "ja" to "燃えるごみは月曜日と木曜日の朝8:00までに出します。次は木曜日です。",
                        "en" to "Burnable garbage goes out on Monday and Thursday, by 8:00 a.m. The next one is Thu, Nov 26.",
                        "bn" to "পোড়ানো বর্জ্য সোম ও বৃহস্পতিবার সকাল ৮:০০ টার মধ্যে বের করতে হবে।",
                    ),
                    snippet = mapOf(
                        "ja" to "朝8:00までに、ごみ置き場に出します。",
                        "en" to "Take it to the garbage station by 8:00 a.m.",
                        "bn" to "সকাল ৮:০০ টার মধ্যে ময়লা ফেলার স্থানে নিয়ে যান।",
                    ),
                )
            listOf("sort", "separate", "recycle", "pet", "bottle", "cans", "資源", "ペットボトル", "分け", "বাছাই").any { low.contains(it) } ->
                NoticeMatchDto(
                    id = "n_res",
                    title = mapOf(
                        "ja" to "資源ごみの分け方",
                        "en" to "How to sort recyclables",
                        "bn" to "রিসাইকেলযোগ্য বর্জ্য কীভাবে বাছাই করবেন",
                    ),
                    answer = mapOf(
                        "ja" to "びん・缶・ペットボトルはかるく洗い、ペットボトルのふたとラベルをはずして別々の箱に入れます。",
                        "en" to "Rinse bottles, cans and PET bottles, take the cap and label off PET bottles, and put each kind in its own box.",
                        "bn" to "বোতল, ক্যান এবং পিইটি বোতল ধুয়ে ফেলুন, ক্যাপ ও লেবেল খুলে আলাদা বাক্সে রাখুন।",
                    ),
                    snippet = mapOf(
                        "ja" to "ペットボトルは、ふたとラベルをはずします。",
                        "en" to "For PET bottles, remove the cap and the label.",
                        "bn" to "পিইটি বোতলের ক্ষেত্রে ক্যাপ ও লেবেল খুলে ফেলুন।",
                    ),
                )
            listOf("drill", "disaster", "earthquake", "evacuate", "防災", "訓練", "避難", "地震", "মহড়া").any { low.contains(it) } ->
                NoticeMatchDto(
                    id = "n_drill",
                    title = mapOf(
                        "ja" to "地区の防災訓練 12/7 9:00 公民館",
                        "en" to "Neighborhood disaster drill: Dec 7, 9:00, community hall",
                        "bn" to "মহল্লার দুর্যোগ মহড়া: ৭ ডিসেম্বর, ৯:০০, কমিউনিটি হল",
                    ),
                    answer = mapOf(
                        "ja" to "防災訓練は12月7日（月）9:00から公民館で行われます。",
                        "en" to "The disaster drill is on Mon, Dec 7 at 9:00 at the community hall.",
                        "bn" to "দুর্যোগ মহড়া সোমবার, ৭ ডিসেম্বর সকাল ৯:০০ টায় কমিউনিটি হলে অনুষ্ঠিত হবে।",
                    ),
                    snippet = mapOf(
                        "ja" to "12月7日（月）9:00に、公民館に行きます。",
                        "en" to "Go to the community hall at 9:00 on Monday, Dec 7.",
                        "bn" to "৭ ডিসেম্বর সোমবার সকাল ৯:০০ টায় কমিউনিটি হলে যান।",
                    ),
                )
            listOf("festival", "matsuri", "祭り", "祭", "秋祭り", "shrine", "উৎসব").any { low.contains(it) } ->
                NoticeMatchDto(
                    id = "n_fest",
                    title = mapOf(
                        "ja" to "秋祭り 11/30",
                        "en" to "Autumn festival: Nov 30",
                        "bn" to "শরতের উৎসব: ৩০ নভেম্বর",
                    ),
                    answer = mapOf(
                        "ja" to "秋祭りは11月30日（月）17:00から神社で行われます。誰でも参加できます。",
                        "en" to "The autumn festival is at the shrine on Mon, Nov 30, from 5:00 p.m. Everyone can come.",
                        "bn" to "শরতের উৎসব সোমবার, ৩০ নভেম্বর বিকেল ৫:০০ টায় মন্দিরে অনুষ্ঠিত হবে।",
                    ),
                    snippet = mapOf(
                        "ja" to "11月30日（月）17:00から、神社で秋祭りがあります。",
                        "en" to "The autumn festival is at the shrine on Monday, Nov 30, from 5:00 p.m.",
                        "bn" to "৩০ নভেম্বর সোমবার বিকাল ৫:০০ টা থেকে মন্দিরে উৎসব হবে।",
                    ),
                )
            else -> null
        }
    }

    private fun createInitialAsks(): List<AskDto> = listOf(
        AskDto(
            id = "ak1",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            text = "When is garbage day?",
            textJa = "ごみの日はいつですか？",
            kind = AskKind.TEXT,
            covered = true,
            matchNotice = NoticeMatchDto(
                id = "n_burn",
                title = mapOf(
                    "ja" to "燃えるごみ 月・木 8:00まで",
                    "en" to "Burnable garbage: Mon and Thu, by 8:00",
                    "bn" to "পোড়ানো বর্জ্য: সোম ও বৃহস্পতি, ৮:০০ মধ্যে",
                ),
                answer = mapOf(
                    "ja" to "燃えるごみは月曜日と木曜日の朝8:00までに出します。次は木曜日です。",
                    "en" to "Burnable garbage goes out on Monday and Thursday, by 8:00 a.m. The next one is Thu, Nov 26.",
                    "bn" to "পোড়ানো বর্জ্য সোম ও বৃহস্পতিবার সকাল ৮:০০ টার মধ্যে বের করতে হবে।",
                ),
                snippet = mapOf(
                    "ja" to "朝8:00までに、ごみ置き場に出します。",
                    "en" to "Take it to the garbage station by 8:00 a.m.",
                    "bn" to "সকাল ৮:০০ টার মধ্যে ময়লা ফেলার স্থানে নিয়ে যান।",
                ),
            ),
            sourceNoticeIds = listOf("n_burn"),
            status = AskStatus.ANSWERED,
            createdAt = "11/20 18:10",
        ),
        AskDto(
            id = "ak2",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            text = "Where can I send money home cheaply?",
            textJa = "安く仕送りできるところはどこですか？",
            kind = AskKind.VOICE,
            covered = false,
            matchNotice = null,
            status = AskStatus.REPLIED,
            senpaiName = "佐藤 美咲",
            senpaiEnName = "Sato Misaki",
            replyJa = "ゆうちょ銀行は手数料が安いですよ。今度、いっしょに行きましょう。",
            replyEn = "Japan Post Bank has low fees. Let us go together next time.",
            replyBn = "জাপান পোস্ট ব্যাংকে ফি কম। পরের বার চলুন একসাথে যাই।",
            createdAt = "11/21 20:30",
        ),
        AskDto(
            id = "ak3",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            text = "How do I get a bus pass?",
            textJa = "バスの定期券はどうやって買いますか？",
            kind = AskKind.TEXT,
            covered = false,
            matchNotice = null,
            status = AskStatus.FORWARDED,
            senpaiName = "佐藤 美咲",
            senpaiEnName = "Sato Misaki",
            createdAt = "11/22 14:15",
        ),
    )
}
