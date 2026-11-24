package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.TodayWord
import com.techtsunagu.wakatta.core.model.TodayWordSource
import com.techtsunagu.wakatta.feature.asks.model.TodayWordDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

// shortcut: nothing sets Today's Word on the server yet (XREQ-024), wire GET /today-word when it does.
@Singleton
class RealTodayWordSource @Inject constructor() : TodayWordSource {
    override fun getTodayWord(): Flow<TodayWord?> = flowOf(null)
    fun getTodayWordDto(): Flow<TodayWordDto?> = flowOf(null)
}
