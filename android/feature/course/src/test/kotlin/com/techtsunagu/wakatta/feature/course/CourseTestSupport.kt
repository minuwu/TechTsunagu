package com.techtsunagu.wakatta.feature.course

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.feature.course.domain.Course
import com.techtsunagu.wakatta.feature.course.domain.CourseRepository
import com.techtsunagu.wakatta.feature.course.domain.CourseSummary
import com.techtsunagu.wakatta.feature.course.domain.Drill
import com.techtsunagu.wakatta.feature.course.domain.InspectItem
import com.techtsunagu.wakatta.feature.course.domain.InspectSpot
import com.techtsunagu.wakatta.feature.course.domain.Panel
import com.techtsunagu.wakatta.feature.course.domain.Progress
import com.techtsunagu.wakatta.feature.course.domain.SpotItem
import com.techtsunagu.wakatta.feature.course.domain.TonePlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun tri(ja: String, en: String = ja) = mapOf("ja" to ja, "en" to en)

val spotDrill = Drill.Spot(
    "spot", tri("点検"), tri("見つける"), secs = 3, need = 2,
    items = listOf(SpotItem(tri("a"), true), SpotItem(tri("b"), false), SpotItem(tri("c"), true), SpotItem(tri("d"), true)),
)
val seqDrill = Drill.Seq("seq", tri("順番"), tri("並べる"), listOf(tri("one"), tri("two"), tri("three")))
val ringDrill = Drill.Ring("ring", tri("砥石"), tri("聞く"), listOf(true, false))
val slingDrill = Drill.Sling("sling", tri("玉掛け"), tri("吊る"), loadKg = 2000, wllKg = 1200)
val reflexDrill = Drill.Reflex("reflex", tri("反射"), tri("押す"))

val summary = CourseSummary("c1", "core", 1, 15, tri("指差呼称{しさこしょう}", "Pointing and calling"), tri("目で見る", "Look"))

fun sampleCourse() = Course(
    summary = summary,
    panels = listOf(Panel("foreman", tri("はじめ", "Start")), Panel("worker", tri("はい", "Yes"))),
    handbook = listOf(tri("手順", "Steps")),
    reel = listOf(tri("ヨシ！", "Yoshi"), tri("OFF", "Off")),
    drills = listOf(spotDrill, seqDrill, reflexDrill),
    inspect = listOf(
        InspectItem("i1", tri("ヘルメット", "Helmet"), defective = true, spots = listOf(InspectSpot(40, 20, tri("A")), InspectSpot(60, 30, tri("B")))),
    ),
)

class TestSessionStore(user: User? = DemoSeed.demoWorker) : SessionStore {
    private val flow = MutableStateFlow(user?.let { Session("t", user = it) })
    override val session: StateFlow<Session?> = flow
    override suspend fun save(session: Session) { flow.value = session }
    override suspend fun clear() { flow.value = null }
    override fun getCachedToken(): String? = flow.value?.token
}

class StubCourseRepository : CourseRepository {
    var listResult: AppResult<List<CourseSummary>> = AppResult.Success(listOf(summary))
    var courseResult: AppResult<Course> = AppResult.Success(sampleCourse())
    var progress: MutableMap<String, Progress> = mutableMapOf()
    var saveError: AppError? = null
    val saved = mutableListOf<String>()
    var resets = 0

    override suspend fun getCourses() = listResult
    override suspend fun getCourse(id: String) = courseResult
    override suspend fun getProgress(): AppResult<Map<String, Progress>> = AppResult.Success(progress.toMap())
    override suspend fun markDone(courseId: String, item: String): AppResult<Unit> {
        saveError?.let { return AppResult.Failure(it) }
        saved += item
        progress[courseId] = (progress[courseId] ?: Progress()).with(item)
        return AppResult.Success(Unit)
    }
    override suspend fun resetProgress(courseId: String): AppResult<Unit> {
        resets++
        progress.remove(courseId)
        return AppResult.Success(Unit)
    }
}

class StubSpeaker : Speaker {
    val spoken = mutableListOf<String>()
    var stops = 0
    override fun speak(text: String, languageTag: String) { spoken += text }
    override fun isAvailable(languageTag: String) = true
    override fun stop() { stops++ }
}

class StubTone : TonePlayer {
    val played = mutableListOf<Int>()
    override fun play(hz: Int, millis: Int) { played += hz }
}
