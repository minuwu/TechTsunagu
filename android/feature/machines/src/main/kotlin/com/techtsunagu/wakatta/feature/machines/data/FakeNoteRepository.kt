package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.NoteStatus
import com.techtsunagu.wakatta.feature.machines.domain.NoteQuestionDto
import com.techtsunagu.wakatta.feature.machines.domain.NoteRepository
import com.techtsunagu.wakatta.feature.machines.domain.NoteStepDto
import com.techtsunagu.wakatta.feature.machines.domain.NoteVersionDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeNoteRepository(
    private val delayMs: Long = 200L
) : NoteRepository {

    @Inject
    constructor() : this(200L)

    private val versionsMap = mutableMapOf<String, MutableList<NoteVersionDto>>().apply {
        put(
            "mc1",
            mutableListOf(
                NoteVersionDto(
                    id = "nv_mc1_v2",
                    machineId = "mc1",
                    version = "v2",
                    status = NoteStatus.APPROVED,
                    updatedAt = "2026-11-09",
                    by = "田中 健一",
                    steps = listOf(
                        NoteStepDto(
                            index = 1,
                            icon = "ppe",
                            textJa = "保護メガネを着用する。回転中のチャック付近では手袋をしない。",
                            textEn = "Wear safety glasses. Do not wear gloves near the spinning chuck.",
                            textBn = "নিরাপত্তা চশমা পরুন। ঘূর্ণায়মান চাকের কাছে গ্লাভস পরবেন না।",
                            backJa = "保護眼鏡をつけてください。回っているチャックの近くでは手袋をつけないでください。"
                        ),
                        NoteStepDto(
                            index = 2,
                            icon = "lock",
                            textJa = "工具交換の前に、主軸を止めて電源をロックアウトする。",
                            textEn = "Stop the spindle and lock out the power before you change a tool.",
                            textBn = "টুল পরিবর্তনের আগে স্পিন্ডল থামিয়ে পাওয়ার লকআউট করুন।",
                            backJa = "工具を替える前に、スピンドルを止めて電源をロックしてください。"
                        ),
                        NoteStepDto(
                            index = 3,
                            icon = "hot",
                            textJa = "切粉は高温で鋭い。手ではなくフックを使う。",
                            textEn = "Metal chips are hot and sharp. Use the hook, never your hands.",
                            textBn = "ধাতব কুচি গরম ও ধারালো। হাত নয়, হুক ব্যবহার করুন।",
                            backJa = "金属の切りくずは熱くて鋭いです。手ではなくフックを使ってください。"
                        )
                    ),
                    questions = listOf(
                        NoteQuestionDto(
                            id = "q_mc1_1",
                            stepIndex = 3,
                            term = "切粉",
                            qJa = "切粉を取るときに何を使いますか？",
                            qEn = "What do you use to remove metal chips?",
                            qBn = "ধাতব কুচি সরাতে আপনি কী ব্যবহার করবেন?",
                            optionsJa = listOf("フック", "手袋", "素手"),
                            optionsEn = listOf("The hook", "Your gloves", "Your hands"),
                            optionsBn = listOf("হুক", "আপনার গ্লাভস", "আপনার খালি হাত"),
                            correctIndex = 0
                        ),
                        NoteQuestionDto(
                            id = "q_mc1_2",
                            stepIndex = 1,
                            term = "手袋",
                            qJa = "回転中のチャックの近くで手袋をしてよいですか？",
                            qEn = "Can you wear gloves near the spinning chuck?",
                            qBn = "ঘূর্ণায়মান চাকের কাছে কি গ্লাভস পরা যাবে?",
                            optionsJa = listOf("いいえ、してはいけない", "はい、いつも", "薄いものだけ"),
                            optionsEn = listOf("No, never", "Yes, always", "Only thin ones"),
                            optionsBn = listOf("না, কখনোই নয়", "হ্যাঁ, সর্বদা", "শুধুমাত্র পাতলাগুলো"),
                            correctIndex = 0
                        ),
                        NoteQuestionDto(
                            id = "q_mc1_3",
                            stepIndex = 2,
                            term = "ロックアウト",
                            qJa = "工具を交換する前に、何をしますか？",
                            qEn = "Before you change a tool, you…",
                            qBn = "টুল পরিবর্তনের আগে আপনি কী করবেন?",
                            optionsJa = listOf("主軸を止めてロックアウト", "主軸の回転を落とす", "友達に見てもらう"),
                            optionsEn = listOf("Stop the spindle and lock out", "Slow the spindle down", "Ask a coworker to watch"),
                            optionsBn = listOf("স্পিন্ডল থামিয়ে লকআউট করুন", "স্পিন্ডলের গতি কমান", "সহকর্মীকে দেখতে বলুন"),
                            correctIndex = 0
                        )
                    )
                )
            )
        )
        put(
            "mc2",
            mutableListOf(
                NoteVersionDto(
                    id = "nv_mc2_v3",
                    machineId = "mc2",
                    version = "v3",
                    status = NoteStatus.APPROVED,
                    updatedAt = "2026-11-14",
                    by = "田中 健一",
                    steps = listOf(
                        NoteStepDto(
                            index = 1,
                            icon = "ppe",
                            textJa = "安全靴・手袋・保護メガネを着用する。",
                            textEn = "Wear safety shoes, gloves and safety glasses.",
                            textBn = "নিরাপত্তা জুতো, গ্লাভস এবং সুরক্ষা চশমা পরিধান করুন।",
                            backJa = "安全靴、手袋、保護眼鏡を身につけてください。"
                        ),
                        NoteStepDto(
                            index = 2,
                            icon = "lock",
                            textJa = "プレスに手を入れる前に、電源をロックアウトし、自分の錠をスイッチに掛ける。",
                            textEn = "Lock out the power and put your own lock on the switch before you reach into the press.",
                            textBn = "প্রেসে হাত দেওয়ার আগে পাওয়ার লকআউট করুন এবং সুইচে নিজের তালা লাগান।",
                            backJa = "プレスに手を伸ばす前に、電源を切って、スイッチに自分のロックを付けてください。"
                        ),
                        NoteStepDto(
                            index = 3,
                            icon = "hot",
                            textJa = "運転後の金型は高温。10分間は触らない。",
                            textEn = "The die is hot after running. Do not touch it for 10 minutes.",
                            textBn = "কাজ করার পর ডাই গরম থাকে। ১০ মিনিট এটি স্পর্শ করবেন না।",
                            backJa = "金型は運転後に熱くなります。10分間は触れないでください。"
                        ),
                        NoteStepDto(
                            index = 4,
                            icon = "pinch",
                            textJa = "上型と下型の間の挟まれ点に手を入れない。",
                            textEn = "Keep your hands out of the pinch point between the upper and lower die.",
                            textBn = "উপরের এবং নিচের ডাই-এর মধ্যবর্তী চিপা অংশে হাত দেবেন না।",
                            backJa = "上と下の金型の間の挟み込み箇所から手を離しておいてください。"
                        )
                    ),
                    questions = listOf(
                        NoteQuestionDto(
                            id = "q_mc2_1",
                            stepIndex = 2,
                            term = "ロックアウト",
                            qJa = "プレスに手を入れる前に、何をしますか？",
                            qEn = "What do you do before you reach into the press?",
                            qBn = "প্রেসে হাত দেওয়ার আগে আপনি কী করবেন?",
                            optionsJa = listOf("電源をロックアウトする", "金型を拭く", "速度を上げる"),
                            optionsEn = listOf("Lock out the power", "Wipe the die", "Turn the speed up"),
                            optionsBn = listOf("পাওয়ার লকআউট করুন", "ডাই মুছুন", "গতি বাড়ান"),
                            correctIndex = 0
                        ),
                        NoteQuestionDto(
                            id = "q_mc2_2",
                            stepIndex = 3,
                            term = "金型",
                            qJa = "金型が運転を終えたところです。どうしますか？",
                            qEn = "The die just finished running. What do you do?",
                            qBn = "ডাই-এর কাজ মাত্র শেষ হয়েছে। আপনি কী করবেন?",
                            optionsJa = listOf("10分待つ", "触って確かめる", "水をかける"),
                            optionsEn = listOf("Wait 10 minutes", "Touch it to check", "Pour water on it"),
                            optionsBn = listOf("১০ মিনিট অপেক্ষা করুন", "ছুঁয়ে পরীক্ষা করুন", "এতে জল ঢালুন"),
                            correctIndex = 0
                        ),
                        NoteQuestionDto(
                            id = "q_mc2_3",
                            stepIndex = 4,
                            term = "挟まれ",
                            qJa = "手を近づけてはいけない場所はどこですか？",
                            qEn = "Where must your hands stay away from?",
                            qBn = "কোন জায়গা থেকে হাত দূরে রাখতে হবে?",
                            optionsJa = listOf("金型の間の挟まれ点", "操作盤", "オイル缶"),
                            optionsEn = listOf("The pinch point between the dies", "The control panel", "The oil can"),
                            optionsBn = listOf("ডাই-এর মাঝের চিপা অংশ", "নিয়ন্ত্রণ প্যানেল", "তেলের পাত্র"),
                            correctIndex = 0
                        )
                    )
                ),
                NoteVersionDto(
                    id = "nv_mc2_v2",
                    machineId = "mc2",
                    version = "v2",
                    status = NoteStatus.APPROVED,
                    updatedAt = "2026-10-01",
                    by = "田中 健一",
                    steps = listOf(
                        NoteStepDto(
                            index = 1,
                            icon = "ppe",
                            textJa = "手袋・保護メガネを着用する。",
                            textEn = "Wear gloves and safety glasses.",
                            textBn = "গ্লাভস এবং সুরক্ষা চশমা পরিধান করুন।",
                            backJa = "手袋と保護眼鏡をつけてください。"
                        ),
                        NoteStepDto(
                            index = 2,
                            icon = "lock",
                            textJa = "電源を切ってから作業する。",
                            textEn = "Turn off the power before work.",
                            textBn = "কাজের আগে পাওয়ার বন্ধ করুন।",
                            backJa = "作業前に電源を切ってください。"
                        )
                    ),
                    questions = emptyList()
                )
            )
        )
        put(
            "mc3",
            mutableListOf(
                NoteVersionDto(
                    id = "nv_mc3_v4",
                    machineId = "mc3",
                    version = "v4",
                    status = NoteStatus.APPROVED,
                    updatedAt = "2026-11-18",
                    by = "田中 健一",
                    steps = listOf(
                        NoteStepDto(
                            index = 1,
                            icon = "hot",
                            textJa = "ノズルとバレルは非常に高温(約200 °C)。絶対に触らない。",
                            textEn = "The nozzle and barrel are very hot (about 200 °C). Never touch them.",
                            textBn = "নোজল এবং ব্যারেল খুব গরম (প্রায় ২০০ ডিগ্রি সেলসিয়াস)। কখনোই স্পর্শ করবেন না।",
                            backJa = "ノズルとバレルはとても熱い(約200 °C)です。決して触れないでください。"
                        ),
                        NoteStepDto(
                            index = 2,
                            icon = "pinch",
                            textJa = "始める前に安全扉を閉める。金型エリアに手を入れない。",
                            textEn = "Close the safety door before you start. Keep your hands out of the mold area.",
                            textBn = "শুরু করার আগে নিরাপত্তা দরজা বন্ধ করুন। ছাঁচের অংশে হাত দেবেন না।",
                            backJa = "始める前に安全ドアを閉めてください。金型の範囲に手を入れないでください。"
                        ),
                        NoteStepDto(
                            index = 3,
                            icon = "ppe",
                            textJa = "パージのときは、耐熱手袋とフェイスシールドを着用する。",
                            textEn = "When purging, wear heat-resistant gloves and a face shield.",
                            textBn = "পার্জিং করার সময় তাপ-প্রতিরোধী গ্লাভস এবং ফেস শিল্ড পরিধান করুন।",
                            backJa = "パージするときは、耐熱グローブとフェイスシールドをつけてください。"
                        )
                    ),
                    questions = listOf(
                        NoteQuestionDto(
                            id = "q_mc3_1",
                            stepIndex = 1,
                            term = "ノズル",
                            qJa = "ノズルに触ってもよいですか？",
                            qEn = "Can you touch the nozzle?",
                            qBn = "নোজলে কি স্পর্শ করা যাবে?",
                            optionsJa = listOf("いいえ、とても高温", "はい、素手で", "はい、すぐなら"),
                            optionsEn = listOf("No, it is very hot", "Yes, with bare hands", "Yes, if it is quick"),
                            optionsBn = listOf("না, এটি অত্যন্ত গরম", "হ্যাঁ, খালি হাতে", "হ্যাঁ, দ্রুত হলে"),
                            correctIndex = 0
                        ),
                        NoteQuestionDto(
                            id = "q_mc3_2",
                            stepIndex = 2,
                            term = "安全扉",
                            qJa = "始める前に、何を閉めますか？",
                            qEn = "What must be closed before you start?",
                            qBn = "শুরু করার আগে কী বন্ধ করতে হবে?",
                            optionsJa = listOf("安全扉", "窓", "弁当箱"),
                            optionsEn = listOf("The safety door", "The window", "The lunch box"),
                            optionsBn = listOf("নিরাপত্তা দরজা", "জানালা", "টিফিন বাক্স"),
                            correctIndex = 0
                        ),
                        NoteQuestionDto(
                            id = "q_mc3_3",
                            stepIndex = 3,
                            term = "パージ",
                            qJa = "パージのとき、何を着用しますか？",
                            qEn = "What do you wear when purging?",
                            qBn = "পার্জিং করার সময় আপনি কী পরবেন?",
                            optionsJa = listOf("耐熱手袋とフェイスシールド", "サンダル", "特になし"),
                            optionsEn = listOf("Heat-resistant gloves and a face shield", "Sandals", "Nothing special"),
                            optionsBn = listOf("তাপ-প্রতিরোধী গ্লাভস এবং ফেস শিল্ড", "স্যান্ডেল", "বিশেষ কিছু নয়"),
                            correctIndex = 0
                        )
                    )
                )
            )
        )
    }

    override suspend fun getNoteVersion(machineId: String): AppResult<NoteVersionDto> {
        if (delayMs > 0) delay(delayMs)
        val list = versionsMap[machineId] ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(list.first())
    }

    override suspend fun getVersions(machineId: String): AppResult<List<NoteVersionDto>> {
        if (delayMs > 0) delay(delayMs)
        val list = versionsMap[machineId] ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(list)
    }

    override suspend fun getVersion(versionId: String): AppResult<NoteVersionDto> {
        if (delayMs > 0) delay(delayMs)
        val item = versionsMap.values.flatten().find { it.id == versionId } ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(item)
    }

    // Helper for testing version change conflicts
    fun triggerVersionUpdate(machineId: String, newVersionTag: String) {
        val list = versionsMap[machineId] ?: return
        val current = list.first()
        val updated = current.copy(
            id = "nv_${machineId}_$newVersionTag",
            version = newVersionTag,
            updatedAt = "2026-11-23"
        )
        list.add(0, updated)
    }
}
