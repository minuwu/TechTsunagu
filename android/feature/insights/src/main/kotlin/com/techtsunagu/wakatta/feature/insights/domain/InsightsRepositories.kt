package com.techtsunagu.wakatta.feature.insights.domain

import com.techtsunagu.wakatta.core.model.AppResult

interface AnalyticsRepository {
    suspend fun getAnalytics(): AppResult<AnalyticsData>

    /** Adds a confused term to the shop Glossary. */
    suspend fun pinTerm(term: String, termEn: String): AppResult<Unit>
}

interface SkillMatrixRepository {
    suspend fun getMatrix(): AppResult<SkillMatrix>
}

interface FollowUpRepository {
    suspend fun getFollowUps(): AppResult<FollowUpsData>

    /** Foreman: the term was explained (PATCH /follow-ups/{id}). Already understood: `AppError.Conflict`. */
    suspend fun clarify(followUpId: String, note: String = ""): AppResult<Unit>
}

interface LearningRepository {
    suspend fun getHistory(workerId: String): AppResult<LearningHistory>
}
