package com.techtsunagu.wakatta.feature.course

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.stripFurigana
import com.techtsunagu.wakatta.feature.course.domain.Course
import com.techtsunagu.wakatta.feature.course.domain.CourseRepository
import com.techtsunagu.wakatta.feature.course.domain.CourseSummary
import com.techtsunagu.wakatta.feature.course.domain.Door
import com.techtsunagu.wakatta.feature.course.domain.Progress
import com.techtsunagu.wakatta.feature.course.domain.TonePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val JAPANESE = "ja-JP"

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val repository: CourseRepository,
    private val sessionStore: SessionStore,
    private val speaker: Speaker,
    private val tonePlayer: TonePlayer,
) : MviViewModel<CourseUiState, CourseEvent, CourseEffect>(CourseUiState()) {

    private var loadJob: Job? = null
    private var summaries: List<CourseSummary> = emptyList()
    private var progressById: Map<String, Progress> = emptyMap()
    private var course: Course? = null
    private var controller: DrillController? = null
    private var lang = "ja"

    init {
        loadList()
    }

    override fun onEvent(event: CourseEvent) {
        when (event) {
            CourseEvent.Retry -> course?.let { openCourse(it.summary.id) } ?: loadList()
            CourseEvent.Back -> back()
            CourseEvent.Pause -> stopReel()
            is CourseEvent.OpenCourse -> openCourse(event.id)
            is CourseEvent.OpenDoor -> openDoor(event.door)
            is CourseEvent.SetReadMode -> updateState { it.copy(readMode = event.mode) }
            CourseEvent.ToggleFurigana -> updateState { it.copy(furigana = !it.furigana) }
            CourseEvent.ToggleTranslation -> updateState { it.copy(translation = !it.translation) }
            CourseEvent.FinishRead -> record(Progress.KEY_READ)
            CourseEvent.ReelToggle -> if (state.value.reel.playing) stopReel() else playReel(state.value.reel.index)
            CourseEvent.ReelReplay -> playReel(0)
            CourseEvent.ReelSlow -> updateState { it.copy(reel = it.reel.copy(slow = !it.reel.slow)) }
            CourseEvent.ReelTick -> reelTick()
            is CourseEvent.OpenDrill -> openDrill(event.id)
            is CourseEvent.DrillInput -> drillInput(event.event)
            is CourseEvent.InspectItemSelected ->
                updateState { it.copy(inspect = it.inspect.copy(index = event.index, gap = 0, wrong = false)) }
            is CourseEvent.InspectGap -> updateState { it.copy(inspect = it.inspect.copy(gap = event.gap)) }
            is CourseEvent.InspectSpotTapped -> inspectSpot(event.spot)
            is CourseEvent.Judge -> judge(event.reject)
            CourseEvent.StartOver -> startOver()
            CourseEvent.DismissSaveError -> updateState { it.copy(saveError = null) }
        }
    }

    private fun loadList() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            lang = sessionStore.session.value?.user?.language ?: "ja"
            val list = when (val r = repository.getCourses()) {
                is AppResult.Failure -> return@launch fail(r.error)
                is AppResult.Success -> r.value
            }
            val progress = when (val r = repository.getProgress()) {
                is AppResult.Failure -> return@launch fail(r.error)
                is AppResult.Success -> r.value
            }
            summaries = list
            progressById = progress
            updateState { it.copy(isLoading = false, hasTranslation = lang != "ja", courses = coursesView()) }
        }
    }

    private fun fail(error: AppError) = updateState { it.copy(isLoading = false, error = error) }

    private fun coursesView() = summaries.map { it.toItem(lang, progressById[it.id]) }

    private fun openCourse(id: String) {
        loadJob?.cancel()
        stopReel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            when (val r = repository.getCourse(id)) {
                is AppResult.Failure -> fail(r.error)
                is AppResult.Success -> {
                    course = r.value
                    val progress = progressById[id] ?: Progress()
                    updateState {
                        it.copy(
                            isLoading = false, course = r.value.toView(lang), progress = progress, door = null,
                            drill = null, reel = ReelState(), inspect = InspectState(), saveError = null,
                        )
                    }
                }
            }
        }
    }

    private fun back() {
        val s = state.value
        stopReel()
        when {
            s.drill != null -> { controller = null; updateState { it.copy(drill = null) } }
            s.door != null -> updateState { it.copy(door = null) }
            s.course != null -> { course = null; updateState { it.copy(course = null, door = null, courses = coursesView()) } }
            else -> sendEffect(CourseEffect.Exit)
        }
    }

    private fun openDoor(door: Door) =
        updateState { it.copy(door = door, drill = null, reel = ReelState(), inspect = InspectState()) }

    private fun record(item: String) {
        val id = course?.summary?.id ?: return
        viewModelScope.launch {
            when (val r = repository.markDone(id, item)) {
                is AppResult.Failure -> updateState { it.copy(saveError = r.error) }
                is AppResult.Success -> {
                    val next = (progressById[id] ?: Progress()).with(item)
                    progressById = progressById + (id to next)
                    updateState { it.copy(progress = next, saveError = null) }
                }
            }
        }
    }

    private fun startOver() {
        val id = course?.summary?.id ?: return
        stopReel()
        viewModelScope.launch {
            when (val r = repository.resetProgress(id)) {
                is AppResult.Failure -> updateState { it.copy(saveError = r.error) }
                is AppResult.Success -> {
                    progressById = progressById - id
                    controller = null
                    updateState {
                        it.copy(progress = Progress(), door = null, drill = null, reel = ReelState(), inspect = InspectState(), saveError = null)
                    }
                }
            }
        }
    }

    // ---- Watch ----

    private fun speak(index: Int) {
        val text = state.value.course?.reel?.getOrNull(index)?.ja ?: return
        if (speaker.isAvailable(JAPANESE)) speaker.speak(stripFurigana(text), JAPANESE)
    }

    private fun playReel(index: Int) {
        updateState { it.copy(reel = it.reel.copy(index = index, playing = true)) }
        speak(index)
    }

    private fun stopReel() {
        if (state.value.reel.playing) updateState { it.copy(reel = it.reel.copy(playing = false)) }
        speaker.stop()
    }

    private fun reelTick() {
        val s = state.value
        if (!s.reel.playing) return
        if (s.reel.index < (s.course?.reel?.lastIndex ?: 0)) {
            playReel(s.reel.index + 1)
        } else {
            stopReel()
            record(Progress.KEY_WATCH)
        }
    }

    // ---- Play ----

    private fun openDrill(id: String) {
        val d = course?.drills?.firstOrNull { it.id == id } ?: return
        val c = DrillController(d)
        controller = c
        updateState { it.copy(drill = d.toView(lang), drillState = c.initial()) }
    }

    private fun drillInput(e: DrillEvent) {
        val c = controller ?: return
        val before = state.value.drillState
        if (e is DrillEvent.RingTap) c.tone(e.wheel)?.let { (hz, ms) -> tonePlayer.play(hz, ms) }
        val after = c.reduce(before, e)
        updateState { it.copy(drillState = after) }
        val id = state.value.drill?.id
        if (after.passed && !before.passed && id != null) record(Progress.KEY_DRILL + id)
    }

    // ---- Inspect ----

    private fun inspectSpot(spot: Int) {
        val item = course?.inspect?.getOrNull(state.value.inspect.index) ?: return
        val key = "${item.id}#$spot"
        updateState { s -> if (key in s.inspect.seen) s else s.copy(inspect = s.inspect.copy(seen = s.inspect.seen + key)) }
    }

    private fun judge(reject: Boolean) {
        val items = course?.inspect ?: return
        val item = items.getOrNull(state.value.inspect.index) ?: return
        val seenAll = item.spots.indices.all { "${item.id}#$it" in state.value.inspect.seen }
        if (!seenAll || item.id in state.value.inspect.right) return
        val ok = reject == item.defective
        val right = if (ok) state.value.inspect.right + item.id else state.value.inspect.right
        updateState { it.copy(inspect = it.inspect.copy(right = right, wrong = !ok)) }
        if (ok && items.all { it.id in right }) record(Progress.KEY_INSPECT)
    }

    override fun onCleared() {
        speaker.stop()
        super.onCleared()
    }
}
