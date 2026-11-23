package com.techtsunagu.wakatta.feature.course

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.feature.course.domain.Door
import com.techtsunagu.wakatta.feature.course.domain.Progress

/** A Japanese text (with `kanji{reading}` markup) and its translation; [tr] is empty for Japanese readers. */
data class Line(val ja: String, val tr: String = "")

data class CourseItem(
    val id: String,
    val category: String,
    val minutes: Int,
    val title: Line,
    val sub: Line,
    val doorsDone: Int,
)

data class PanelView(val foreman: Boolean, val text: Line)

data class DrillItem(val id: String, val name: Line)

data class InspectSpotView(val x: Int, val y: Int, val check: Line)

/** What the screen may know about an inspect item: the defect is drawn, never stated. */
data class InspectView(val id: String, val name: Line, val spots: List<InspectSpotView>, val showsDefect: Boolean)

/** What the screen may know about the open course; no answers. */
data class CourseView(
    val id: String,
    val title: Line,
    val panels: List<PanelView>,
    val handbook: List<Line>,
    val reel: List<Line>,
    val drills: List<DrillItem>,
    val inspect: List<InspectView>,
)

enum class DrillKind { SPOT, SEQ, REFLEX, RING, SLING }

/** The open drill as the screen shows it: labels and limits, no answers. */
data class DrillView(
    val id: String,
    val kind: DrillKind,
    val name: Line,
    val goal: Line,
    /** Spot items or sequence steps, in the order the drill stores them. */
    val labels: List<Line>,
    val wheels: Int,
    val secs: Int,
    val need: Int,
    val loadKg: Int,
    val wllKg: Int,
)

enum class ReadMode { COMIC, HANDBOOK }

data class ReelState(val index: Int = 0, val playing: Boolean = false, val slow: Boolean = false)

data class InspectState(
    val index: Int = 0,
    val gap: Int = 0,
    /** `<itemId>#<spot>` of every spot the user looked at. */
    val seen: List<String> = emptyList(),
    val right: List<String> = emptyList(),
    val wrong: Boolean = false,
)

data class CourseUiState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val saveError: AppError? = null,
    val courses: List<CourseItem> = emptyList(),
    val course: CourseView? = null,
    val progress: Progress = Progress(),
    val door: Door? = null,
    val hasTranslation: Boolean = false,
    val readMode: ReadMode = ReadMode.COMIC,
    val furigana: Boolean = true,
    val translation: Boolean = true,
    val reel: ReelState = ReelState(),
    val drill: DrillView? = null,
    val drillState: DrillState = DrillState(),
    val inspect: InspectState = InspectState(),
) {
    val isEmpty: Boolean get() = !isLoading && error == null && course == null && courses.isEmpty()
    val canGoBack: Boolean get() = course != null
}

sealed interface CourseEvent {
    data object Retry : CourseEvent
    data object Back : CourseEvent
    data object Pause : CourseEvent
    data class OpenCourse(val id: String) : CourseEvent
    data class OpenDoor(val door: Door) : CourseEvent
    data class SetReadMode(val mode: ReadMode) : CourseEvent
    data object ToggleFurigana : CourseEvent
    data object ToggleTranslation : CourseEvent
    data object FinishRead : CourseEvent
    data object ReelToggle : CourseEvent
    data object ReelReplay : CourseEvent
    data object ReelSlow : CourseEvent
    data object ReelTick : CourseEvent
    data class OpenDrill(val id: String) : CourseEvent
    data class DrillInput(val event: DrillEvent) : CourseEvent
    data class InspectItemSelected(val index: Int) : CourseEvent
    data class InspectGap(val gap: Int) : CourseEvent
    data class InspectSpotTapped(val spot: Int) : CourseEvent
    data class Judge(val reject: Boolean) : CourseEvent
    data object StartOver : CourseEvent
    data object DismissSaveError : CourseEvent
}

sealed interface CourseEffect {
    data object Exit : CourseEffect
}
