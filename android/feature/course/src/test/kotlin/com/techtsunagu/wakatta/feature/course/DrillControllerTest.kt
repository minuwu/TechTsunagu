package com.techtsunagu.wakatta.feature.course

import com.techtsunagu.wakatta.feature.course.domain.Drill
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrillControllerTest {

    private var now = 0L
    private fun controller(d: Drill) = DrillController(d, Random(1)) { now }

    private fun DrillController.run(start: DrillState, vararg events: DrillEvent) = events.fold(start) { s, e -> reduce(s, e) }

    @Test
    fun spot_passesWhenEnoughHazardsFoundBeforeTimeout() {
        val c = controller(spotDrill)
        var s = c.run(c.initial(), DrillEvent.Start, DrillEvent.SpotTap(0), DrillEvent.SpotTap(1), DrillEvent.SpotTap(2))
        assertEquals(listOf(1), s.missTaps)
        assertEquals(DrillPhase.RUN, s.phase)
        s = c.run(s, DrillEvent.SpotTick, DrillEvent.SpotTick, DrillEvent.SpotTick)
        assertEquals(DrillPhase.DONE, s.phase)
        assertTrue(s.passed)
    }

    @Test
    fun spot_endsEarlyWhenAllHazardsFoundAndFailsWithoutEnough() {
        val c = controller(spotDrill)
        val all = c.run(c.initial(), DrillEvent.Start, DrillEvent.SpotTap(0), DrillEvent.SpotTap(2), DrillEvent.SpotTap(3))
        assertEquals(DrillPhase.DONE, all.phase)
        val none = c.run(c.initial(), DrillEvent.Start, DrillEvent.SpotTap(1), DrillEvent.SpotTick, DrillEvent.SpotTick, DrillEvent.SpotTick)
        assertEquals(DrillPhase.DONE, none.phase)
        assertFalse(none.passed)
    }

    @Test
    fun sling_passesOnlyWithinTheWireLimit() {
        val c = controller(slingDrill)
        val wide = c.run(c.initial(), DrillEvent.Lift)
        assertTrue(wide.tensionKg > slingDrill.wllKg)
        assertFalse(wide.passed)
        val narrow = c.run(c.initial(), DrillEvent.SlingAngle(60), DrillEvent.Lift)
        assertTrue(narrow.tensionKg <= slingDrill.wllKg)
        assertTrue(narrow.passed)
        assertFalse(c.run(narrow, DrillEvent.SlingAngle(120)).lifted)
    }

    @Test
    fun reflex_earlyHitFailsAndReactionTimeDecidesPass() {
        val c = controller(reflexDrill)
        assertEquals(ReflexPhase.EARLY, c.run(c.initial(), DrillEvent.Start, DrillEvent.ReflexHit).reflex)
        now = 1000
        val go = c.run(c.initial(), DrillEvent.Start, DrillEvent.ReflexGo)
        now = 1300
        assertTrue(c.reduce(go, DrillEvent.ReflexHit).passed)
        now = 1500
        val slow = c.reduce(go, DrillEvent.ReflexHit)
        assertEquals(500, slow.reactionMs)
        assertFalse(slow.passed)
    }

    @Test
    fun ring_needsEveryWheelAnsweredAndRight() {
        val c = controller(ringDrill)
        val half = c.run(c.initial(), DrillEvent.RingSay(0, true), DrillEvent.RingCheck)
        assertFalse(half.checked)
        val right = c.run(half, DrillEvent.RingSay(1, false), DrillEvent.RingCheck)
        assertTrue(right.passed)
        val wrong = c.run(right, DrillEvent.RingSay(1, true), DrillEvent.RingCheck)
        assertFalse(wrong.passed)
    }

    @Test
    fun seq_allowsOneMistake() {
        val c = controller(seqDrill)
        val oneMistake = c.run(c.initial(), DrillEvent.SeqTap(1), DrillEvent.SeqTap(0), DrillEvent.SeqTap(1), DrillEvent.SeqTap(2))
        assertEquals(1, oneMistake.mistakes)
        assertTrue(oneMistake.passed)
        val twoMistakes = c.run(c.initial(), DrillEvent.SeqTap(2), DrillEvent.SeqTap(1), DrillEvent.SeqTap(0), DrillEvent.SeqTap(1), DrillEvent.SeqTap(2))
        assertFalse(twoMistakes.passed)
        assertEquals(setOf(0, 1, 2), c.initial().order.toSet())
    }
}
