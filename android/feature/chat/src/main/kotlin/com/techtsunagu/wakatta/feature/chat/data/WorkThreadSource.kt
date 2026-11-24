package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.ThreadSource
import com.techtsunagu.wakatta.core.model.ThreadSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class WorkThreadSource @Inject constructor(
    private val repository: ChatRepository,
) : ThreadSource {
    override fun getThreads(): Flow<List<ThreadSummary>> {
        return repository.getThreadSummaries()
    }
}
