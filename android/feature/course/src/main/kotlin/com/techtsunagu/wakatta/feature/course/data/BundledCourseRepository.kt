package com.techtsunagu.wakatta.feature.course.data

import android.content.Context
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.feature.course.domain.Course
import com.techtsunagu.wakatta.feature.course.domain.CourseRepository
import com.techtsunagu.wakatta.feature.course.domain.CourseSummary
import com.techtsunagu.wakatta.feature.course.domain.Progress
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Course content ships in the APK (assets/courses); only the progress is stored, on this phone. */
@Singleton
class BundledCourseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: CourseProgressStore,
    private val sessionStore: SessionStore,
) : CourseRepository {

    override suspend fun getCourses(): AppResult<List<CourseSummary>> =
        attempt { CourseParser.parseSummaries(asset("index")) }

    override suspend fun getCourse(id: String): AppResult<Course> =
        attempt { CourseParser.parseCourse(asset(id)) }

    override suspend fun getProgress(): AppResult<Map<String, Progress>> = withUser { user ->
        store.load(user).mapValues { Progress.of(it.value) }
    }

    override suspend fun markDone(courseId: String, item: String): AppResult<Unit> =
        withUser { store.add(it, courseId, item) }

    override suspend fun resetProgress(courseId: String): AppResult<Unit> =
        withUser { store.clear(it, courseId) }

    private suspend fun <T> withUser(block: suspend (String) -> T): AppResult<T> {
        val user = sessionStore.session.value?.user?.id ?: return AppResult.Failure(AppError.SessionExpired)
        return attempt { block(user) }
    }

    private suspend fun asset(name: String): String = withContext(Dispatchers.IO) {
        context.assets.open("courses/$name.json").bufferedReader().use { it.readText() }
    }

    private suspend fun <T> attempt(block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: FileNotFoundException) {
        AppResult.Failure(AppError.NotFound)
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown(e.javaClass.simpleName))
    }
}
