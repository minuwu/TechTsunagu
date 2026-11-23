package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.EventSource
import com.techtsunagu.wakatta.core.model.UpcomingEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeEventSource @Inject constructor() : EventSource {

    private val events = listOf(
        UpcomingEvent(
            id = "ev1",
            title = mapOf(
                "ja" to "防災訓練",
                "en" to "Disaster drill",
                "bn" to "দুর্যোগ মহড়া"
            ),
            startsAt = "2026-12-06T09:00:00+09:00"
        ),
        UpcomingEvent(
            id = "ev2",
            title = mapOf(
                "ja" to "ごみ収集日（燃えるごみ）",
                "en" to "Garbage collection (Burnable)",
                "bn" to "ময়লা সংগ্রহ (দাহ্য)"
            ),
            startsAt = "2026-11-24T08:00:00+09:00"
        )
    )

    override fun getUpcomingEvents(): Flow<List<UpcomingEvent>> = flowOf(events)
}
