package com.techtsunagu.wakatta.feature.connect.data

import com.techtsunagu.wakatta.core.model.TalkGrade
import com.techtsunagu.wakatta.feature.connect.domain.ReplyOption
import com.techtsunagu.wakatta.feature.connect.domain.TalkEntryDto
import com.techtsunagu.wakatta.feature.connect.domain.TermPair

/**
 * An utterance the Fake "hears", ported from the prototype `UTTER`.
 * [must] are idea groups the owner's words are compared with; [need] groups must match for green.
 */
internal class Utterance(
    val said: String,
    val plainJa: String,
    val en: String,
    val terms: List<TermPair>,
    val need: Int,
    val needsStaff: Boolean,
    val must: List<List<String>>,
    val hintJa: String,
    val hintEn: String,
    val replies: List<ReplyOption>,
)

internal object TalkScript {
    // shortcut: three canned Kyushu-dialect lines stand in for speech recognition; have a native speaker check them before care-staff use
    val utterances = listOf(
        Utterance(
            said = "膝のいたかけん、今日は散歩はせんばい。",
            plainJa = "膝が痛いので、今日は散歩をしません。",
            en = "My knee hurts, so I will not go for a walk today.",
            terms = listOf(TermPair("いたか", "痛い"), TermPair("けん", "ので"), TermPair("せんばい", "しません")),
            need = 3,
            needsStaff = true,
            must = listOf(
                listOf("knee", "leg"),
                listOf("hurt", "pain", "sore", "ache"),
                listOf("walk", "stroll", "outside"),
                listOf("not", "n't", "no ", "skip", "cancel"),
            ),
            hintJa = "何が痛いのか、何をしないのかを確かめましょう。",
            hintEn = "Check what hurts and what they will not do.",
            replies = listOf(
                ReplyOption("Then let us rest today. Shall I call the nurse?", "では、今日は休みましょう。看護師さんを呼びましょうか？"),
                ReplyOption("I understand. Would you like some tea?", "わかりました。お茶はいかがですか？"),
            ),
        ),
        Utterance(
            said = "お茶ばもう一杯ちょうだい。",
            plainJa = "お茶をもう一杯ください。",
            en = "Please give me one more cup of tea.",
            terms = listOf(TermPair("お茶ば", "お茶を")),
            need = 2,
            needsStaff = false,
            must = listOf(listOf("tea"), listOf("more", "another", "again")),
            hintJa = "何を、どのくらい頼まれたか確かめましょう。",
            hintEn = "Check what they asked for and how much.",
            replies = listOf(
                ReplyOption("Yes, I will bring it now.", "はい、すぐにお持ちします。"),
                ReplyOption("Hot or cold tea?", "温かいお茶と冷たいお茶、どちらがいいですか？"),
            ),
        ),
        Utterance(
            said = "ばってん、今日はあんまり食欲のなかと。",
            plainJa = "でも、今日はあまり食欲がありません。",
            en = "But I do not have much appetite today.",
            terms = listOf(TermPair("ばってん", "でも"), TermPair("のなか", "がない")),
            need = 2,
            needsStaff = true,
            must = listOf(
                listOf("appetite", "hungry", "eat", "food"),
                listOf("not", "n't", "no ", "little", "low", "lack"),
                listOf("today"),
            ),
            hintJa = "何がどうなのか、いつのことかを確かめましょう。",
            hintEn = "Check what is wrong and when.",
            replies = listOf(
                ReplyOption("Would you like something light, like soup?", "スープなど、軽いものはいかがですか？"),
                ReplyOption("I will tell the staff. Is that okay?", "職員に伝えますね。いいですか？"),
            ),
        ),
    )

    // shortcut: counts matching ideas; the real app asks the AI to compare the owner's words with the transcript
    fun grade(text: String, u: Utterance): TalkGrade {
        val low = " ${text.lowercase()} "
        val matched = u.must.count { group -> group.any { low.contains(it) } }
        return when {
            matched >= u.need -> TalkGrade.GREEN
            matched > 0 -> TalkGrade.ORANGE
            else -> TalkGrade.RED
        }
    }

    // shortcut: a four-phrase lookup stands in for the translate pipeline
    private val phrases = listOf(
        Regex("\\b(hello|hi)\\b", RegexOption.IGNORE_CASE) to "こんにちは！",
        Regex("thank", RegexOption.IGNORE_CASE) to "ありがとうございます。",
        Regex("festival|matsuri", RegexOption.IGNORE_CASE) to "お祭り、楽しみです。",
        Regex("\\b(yes|ok|sure)\\b", RegexOption.IGNORE_CASE) to "はい、いいですよ。",
    )

    fun translateToJa(text: String): String = phrases.firstOrNull { it.first.containsMatchIn(text) }?.second.orEmpty()

    val greeting = "はじめまして。QRでつながれてうれしいです。" to "Nice to meet you. I am glad we connected by QR."

    // shortcut: the contact answers with a canned line; the real app delivers to the other person's phone
    val replies = listOf(
        "ありがとう！お祭り、いっしょに行きましょう。" to "Thank you! Let us go to the festival together.",
        "いいですね。また連絡します。" to "Sounds good. I will message you again.",
    )
}

internal fun Utterance.toEntry(id: String, talkId: String, at: String) = TalkEntryDto(
    id = id,
    talkId = talkId,
    at = at,
    said = said,
    plainJa = plainJa,
    meaning = mapOf("ja" to plainJa, "en" to en),
    terms = terms,
    needsStaff = needsStaff,
    ownText = null,
    skipped = false,
    grade = null,
    hint = mapOf("ja" to hintJa, "en" to hintEn),
    replyOptions = replies,
    replyJa = null,
    confirmed = null,
)
