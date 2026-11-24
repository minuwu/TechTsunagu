package com.techtsunagu.wakatta.feature.course

import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.course.domain.Course
import com.techtsunagu.wakatta.feature.course.domain.CourseSummary
import com.techtsunagu.wakatta.feature.course.domain.Drill
import com.techtsunagu.wakatta.feature.course.domain.InspectItem
import com.techtsunagu.wakatta.feature.course.domain.Progress

internal fun line(tri: Tri, lang: String): Line {
    val ja = tri["ja"].orEmpty()
    val tr = if (lang == "ja") "" else pick(tri, lang)
    return Line(ja, if (tr == ja) "" else tr)
}

internal fun CourseSummary.toItem(lang: String, progress: Progress?) =
    CourseItem(id, category, minutes, line(title, lang), line(sub, lang), progress?.doorsDone ?: 0)

internal fun Course.toView(lang: String) = CourseView(
    id = summary.id,
    title = line(summary.title, lang),
    panels = panels.map { PanelView(it.who == "foreman", line(it.text, lang)) },
    handbook = handbook.map { line(it, lang) },
    reel = reel.map { line(it, lang) },
    drills = drills.map { DrillItem(it.id, line(it.name, lang)) },
    inspect = inspect.map { it.toView(lang) },
)

private fun InspectItem.toView(lang: String) = InspectView(
    id = id,
    name = line(name, lang),
    spots = spots.map { InspectSpotView(it.x, it.y, line(it.check, lang)) },
    showsDefect = defective,
)

internal fun Drill.toView(lang: String): DrillView {
    val base = DrillView(id, DrillKind.REFLEX, line(name, lang), line(goal, lang), emptyList(), 0, 0, 0, 0, 0)
    return when (this) {
        is Drill.Spot -> base.copy(kind = DrillKind.SPOT, labels = items.map { line(it.label, lang) }, secs = secs, need = need)
        is Drill.Seq -> base.copy(kind = DrillKind.SEQ, labels = steps.map { line(it, lang) })
        is Drill.Reflex -> base
        is Drill.Ring -> base.copy(kind = DrillKind.RING, wheels = wheels.size)
        is Drill.Sling -> base.copy(kind = DrillKind.SLING, loadKg = loadKg, wllKg = wllKg)
    }
}
