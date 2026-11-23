package com.techtsunagu.wakatta.feature.course.domain

import com.techtsunagu.wakatta.core.model.AppResult

interface CourseRepository {
    suspend fun getCourses(): AppResult<List<CourseSummary>>
    suspend fun getCourse(id: String): AppResult<Course>

    /** Progress of the signed-in user, by course id. */
    suspend fun getProgress(): AppResult<Map<String, Progress>>

    /** Records one finished item ([Progress.KEY_READ], `drill:<id>`, ...) for the signed-in user. */
    suspend fun markDone(courseId: String, item: String): AppResult<Unit>

    suspend fun resetProgress(courseId: String): AppResult<Unit>
}

/** Plays a short sine tone (the "ring" drill). */
interface TonePlayer {
    fun play(hz: Int, millis: Int)
}
