package com.techtsunagu.wakatta.core.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeGlossaryRepository(
    private val delayMs: Long = 200L
) : GlossaryRepository {

    @Inject
    constructor() : this(200L)


    private val terms = mutableListOf(
        GlossaryTerm(ja = "安全第一", reading = "あんぜんだいいち", bn = "নিরাপত্তা প্রথম", en = "Safety First"),
        GlossaryTerm(ja = "非常停止", reading = "ひじょうていし", bn = "জরুরি বিরতি", en = "Emergency Stop"),
        GlossaryTerm(ja = "保護具", reading = "ほごぐ", bn = "সুরক্ষা সরঞ্জাম", en = "Protective Gear"),
        GlossaryTerm(ja = "指差し呼称", reading = "ゆびさしこしょう", bn = "নির্দেশ করে নিশ্চিত করা", en = "Pointing and Calling"),
        GlossaryTerm(ja = "点検", reading = "てんけん", bn = "পরিদর্শন", en = "Inspection")
    )

    override suspend fun getTerms(): AppResult<List<GlossaryTerm>> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(terms.toList())
    }

    override suspend fun addTerm(
        termJa: String,
        readingJa: String,
        termBn: String,
        termEn: String
    ): AppResult<GlossaryTerm> {
        if (delayMs > 0) delay(delayMs)
        val term = GlossaryTerm(ja = termJa, reading = readingJa, bn = termBn, en = termEn)
        terms.add(term)
        return AppResult.Success(term)
    }
}
