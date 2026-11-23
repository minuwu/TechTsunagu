package com.techtsunagu.wakatta.feature.course.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Finished items per course, kept apart for each signed-in user. */
interface CourseProgressStore {
    suspend fun load(userId: String): Map<String, Set<String>>
    suspend fun add(userId: String, courseId: String, item: String)
    suspend fun clear(userId: String, courseId: String)
}

private val Context.courseProgress by preferencesDataStore(name = "course_progress")

@Singleton
class DataStoreCourseProgressStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : CourseProgressStore {

    private fun key(userId: String, courseId: String) = stringSetPreferencesKey("$userId|$courseId")

    override suspend fun load(userId: String): Map<String, Set<String>> {
        val prefix = "$userId|"
        return context.courseProgress.data.first().asMap().entries
            .filter { it.key.name.startsWith(prefix) }
            .associate { it.key.name.removePrefix(prefix) to (it.value as Set<*>).filterIsInstance<String>().toSet() }
    }

    override suspend fun add(userId: String, courseId: String, item: String) {
        context.courseProgress.edit { it[key(userId, courseId)] = (it[key(userId, courseId)] ?: emptySet()) + item }
    }

    override suspend fun clear(userId: String, courseId: String) {
        context.courseProgress.edit { it.remove(key(userId, courseId)) }
    }
}
