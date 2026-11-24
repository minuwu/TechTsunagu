package com.techtsunagu.wakatta.feature.course

import com.techtsunagu.wakatta.feature.course.domain.Drill
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.random.Random

enum class DrillPhase { IDLE, RUN, DONE }

enum class ReflexPhase { IDLE, WAIT, GO, EARLY, DONE }

/** One state for every drill kind; the fields a kind does not use stay at their defaults. */
data class DrillState(
    val phase: DrillPhase = DrillPhase.IDLE,
    val secsLeft: Int = 0,
    val goodTaps: List<Int> = emptyList(),
    val missTaps: List<Int> = emptyList(),
    val angle: Int = SLING_START_ANGLE,
    val tensionKg: Int = 0,
    val lifted: Boolean = false,
    val reflex: ReflexPhase = ReflexPhase.IDLE,
    val waitMs: Long = 0,
    val goAtMs: Long = 0,
    val reactionMs: Long = 0,
    val order: List<Int> = emptyList(),
    val picked: List<Int> = emptyList(),
    val mistakes: Int = 0,
    val saidSound: List<Int> = emptyList(),
    val saidCracked: List<Int> = emptyList(),
    val checked: Boolean = false,
    val passed: Boolean = false,
)

sealed interface DrillEvent {
    data object Start : DrillEvent
    data object Restart : DrillEvent
    data class SpotTap(val item: Int) : DrillEvent
    data object SpotTick : DrillEvent
    data class SlingAngle(val degrees: Int) : DrillEvent
    data object Lift : DrillEvent
    data object ReflexGo : DrillEvent
    data object ReflexHit : DrillEvent
    data class RingTap(val wheel: Int) : DrillEvent
    data class RingSay(val wheel: Int, val sound: Boolean) : DrillEvent
    data object RingCheck : DrillEvent
    data class SeqTap(val step: Int) : DrillEvent
}

const val REFLEX_LIMIT_MS = 400L
const val SLING_START_ANGLE = 100
private const val MAX_MISTAKES = 1
private const val SOUND_HZ = 1200
private const val CRACKED_HZ = 320

/** Grades one drill. All answers stay here; the screen only ever sees [DrillState]. */
class DrillController(
    private val drill: Drill,
    private val random: Random = Random.Default,
    private val nowMs: () -> Long = { System.nanoTime() / 1_000_000 },
) {

    fun initial(): DrillState = when (drill) {
        is Drill.Spot -> DrillState(secsLeft = drill.secs)
        is Drill.Sling -> DrillState(tensionKg = tension(drill, SLING_START_ANGLE))
        is Drill.Reflex -> DrillState()
        is Drill.Ring -> DrillState(order = drill.wheels.indices.shuffled(random))
        is Drill.Seq -> DrillState(order = drill.steps.indices.shuffled(random))
    }

    /** Frequency and length of the tone a ring-drill wheel makes. */
    fun tone(wheel: Int): Pair<Int, Int>? = (drill as? Drill.Ring)?.wheels?.getOrNull(wheel)
        ?.let { sound -> if (sound) SOUND_HZ to 1200 else CRACKED_HZ to 150 }

    fun reduce(s: DrillState, e: DrillEvent): DrillState = when (e) {
        DrillEvent.Restart -> initial()
        DrillEvent.Start -> start(s)
        is DrillEvent.SpotTap -> spotTap(s, e.item)
        DrillEvent.SpotTick -> spotTick(s)
        is DrillEvent.SlingAngle -> sling(s) { s.copy(angle = e.degrees, tensionKg = tension(it, e.degrees), lifted = false, passed = false) }
        DrillEvent.Lift -> sling(s) { s.copy(lifted = true, passed = s.tensionKg <= it.wllKg) }
        DrillEvent.ReflexGo -> if (s.reflex == ReflexPhase.WAIT) s.copy(reflex = ReflexPhase.GO, goAtMs = nowMs()) else s
        DrillEvent.ReflexHit -> reflexHit(s)
        is DrillEvent.RingTap -> s
        is DrillEvent.RingSay -> ring(s) { s.copy(saidSound = said(s.saidSound, e.wheel, e.sound), saidCracked = said(s.saidCracked, e.wheel, !e.sound)) }
        DrillEvent.RingCheck -> ring(s) { r -> ringCheck(s, r) }
        is DrillEvent.SeqTap -> seqTap(s, e.step)
    }

    private fun start(s: DrillState): DrillState = when (drill) {
        is Drill.Spot -> DrillState(phase = DrillPhase.RUN, secsLeft = drill.secs)
        is Drill.Reflex -> DrillState(reflex = ReflexPhase.WAIT, waitMs = random.nextLong(1000, 3000))
        else -> s
    }

    private fun spotTap(s: DrillState, item: Int): DrillState {
        val d = drill as? Drill.Spot ?: return s
        if (s.phase != DrillPhase.RUN || item in s.goodTaps || item in s.missTaps || item !in d.items.indices) return s
        val next = if (d.items[item].hazard) s.copy(goodTaps = s.goodTaps + item) else s.copy(missTaps = s.missTaps + item)
        return if (next.goodTaps.size >= d.items.count { it.hazard }) finishSpot(next, d) else next
    }

    private fun spotTick(s: DrillState): DrillState {
        val d = drill as? Drill.Spot ?: return s
        if (s.phase != DrillPhase.RUN) return s
        val left = s.secsLeft - 1
        return if (left <= 0) finishSpot(s.copy(secsLeft = 0), d) else s.copy(secsLeft = left)
    }

    private fun finishSpot(s: DrillState, d: Drill.Spot) = s.copy(phase = DrillPhase.DONE, passed = s.goodTaps.size >= d.need)

    private fun sling(s: DrillState, block: (Drill.Sling) -> DrillState): DrillState =
        (drill as? Drill.Sling)?.let(block) ?: s

    private fun tension(d: Drill.Sling, degrees: Int): Int = (d.loadKg / 2 / cos(degrees * PI / 360)).roundToInt()

    private fun reflexHit(s: DrillState): DrillState = when (s.reflex) {
        ReflexPhase.WAIT -> s.copy(reflex = ReflexPhase.EARLY)
        ReflexPhase.GO -> (nowMs() - s.goAtMs).let { ms ->
            s.copy(reflex = ReflexPhase.DONE, reactionMs = ms, passed = ms < REFLEX_LIMIT_MS)
        }
        else -> s
    }

    private fun ring(s: DrillState, block: (Drill.Ring) -> DrillState): DrillState =
        (drill as? Drill.Ring)?.let(block) ?: s

    private fun said(list: List<Int>, wheel: Int, add: Boolean) = list.filter { it != wheel } + if (add) listOf(wheel) else emptyList()

    private fun ringCheck(s: DrillState, r: Drill.Ring): DrillState {
        if (s.saidSound.size + s.saidCracked.size < r.wheels.size) return s
        val right = r.wheels.indices.all { (it in s.saidSound) == r.wheels[it] }
        return s.copy(checked = true, passed = right)
    }

    private fun seqTap(s: DrillState, step: Int): DrillState {
        val d = drill as? Drill.Seq ?: return s
        if (s.picked.size == d.steps.size) return s
        if (step != s.picked.size) return s.copy(mistakes = s.mistakes + 1)
        val picked = s.picked + step
        return s.copy(picked = picked, passed = picked.size == d.steps.size && s.mistakes <= MAX_MISTAKES)
    }
}
