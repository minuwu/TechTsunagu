package com.techtsunagu.wakatta.feature.course.domain

import com.techtsunagu.wakatta.core.model.Tri

/** Passing the "play" door needs this many drills. */
const val DRILLS_NEEDED = 3

data class CourseSummary(
    val id: String,
    val category: String,
    val level: Int,
    val minutes: Int,
    val title: Tri,
    val sub: Tri,
)

data class Panel(val who: String, val text: Tri)

data class SpotItem(val label: Tri, val hazard: Boolean)

data class InspectSpot(val x: Int, val y: Int, val check: Tri)

data class InspectItem(val id: String, val name: Tri, val defective: Boolean, val spots: List<InspectSpot>)

sealed interface Drill {
    val id: String
    val name: Tri
    val goal: Tri

    data class Spot(
        override val id: String, override val name: Tri, override val goal: Tri,
        val secs: Int, val need: Int, val items: List<SpotItem>,
    ) : Drill

    /** [steps] are in the correct order. */
    data class Seq(override val id: String, override val name: Tri, override val goal: Tri, val steps: List<Tri>) : Drill

    data class Reflex(override val id: String, override val name: Tri, override val goal: Tri) : Drill

    /** [wheels]: true is a sound wheel, false a cracked one. */
    data class Ring(override val id: String, override val name: Tri, override val goal: Tri, val wheels: List<Boolean>) : Drill

    data class Sling(
        override val id: String, override val name: Tri, override val goal: Tri,
        val loadKg: Int, val wllKg: Int,
    ) : Drill
}

data class Course(
    val summary: CourseSummary,
    val panels: List<Panel>,
    val handbook: List<Tri>,
    val reel: List<Tri>,
    val drills: List<Drill>,
    val inspect: List<InspectItem>,
)

enum class Door { READ, WATCH, PLAY, INSPECT }

/** What one user finished in one course. */
data class Progress(
    val read: Boolean = false,
    val watch: Boolean = false,
    val inspect: Boolean = false,
    val drills: Set<String> = emptySet(),
) {
    fun isDone(door: Door): Boolean = when (door) {
        Door.READ -> read
        Door.WATCH -> watch
        Door.INSPECT -> inspect
        Door.PLAY -> drills.size >= DRILLS_NEEDED
    }

    val doorsDone: Int get() = Door.entries.count { isDone(it) }
    val passport: Boolean get() = doorsDone == Door.entries.size

    fun with(item: String): Progress = when {
        item == KEY_READ -> copy(read = true)
        item == KEY_WATCH -> copy(watch = true)
        item == KEY_INSPECT -> copy(inspect = true)
        item.startsWith(KEY_DRILL) -> copy(drills = drills + item.removePrefix(KEY_DRILL))
        else -> this
    }

    companion object {
        const val KEY_READ = "read"
        const val KEY_WATCH = "watch"
        const val KEY_INSPECT = "inspect"
        const val KEY_DRILL = "drill:"

        fun of(items: Set<String>): Progress = items.fold(Progress()) { p, i -> p.with(i) }
    }
}
