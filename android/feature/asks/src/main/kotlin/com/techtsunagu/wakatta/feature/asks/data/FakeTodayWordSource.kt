package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.TodayWord
import com.techtsunagu.wakatta.core.model.TodayWordSource
import com.techtsunagu.wakatta.feature.asks.model.TodayWordDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeTodayWordSource @Inject constructor() : TodayWordSource {

    private val wordDtoFlow = MutableStateFlow(
        TodayWordDto(
            workerId = "u_rahim",
            workerName = "Rahim",
            languageName = "ベンガル語",
            word = "ধন্যবাদ",
            reading = "dhonnobad",
            meaningJa = "ありがとう",
            meaningEn = "Thank you",
            meaningBn = "ধন্যবাদ",
        ),
    )

    private val todayWordFlow = MutableStateFlow<TodayWord?>(
        TodayWord(
            term = "ধন্যবাদ",
            reading = "dhonnobad",
            meaning = mapOf(
                "ja" to "ありがとう",
                "en" to "Thank you",
                "bn" to "ধন্যবাদ",
            ),
        ),
    )

    override fun getTodayWord(): Flow<TodayWord?> = todayWordFlow.asStateFlow()

    fun getTodayWordDto(): Flow<TodayWordDto> = wordDtoFlow.asStateFlow()
}
