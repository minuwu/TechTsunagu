package com.techtsunagu.wakatta.feature.course

import com.techtsunagu.wakatta.feature.course.data.CourseParser
import com.techtsunagu.wakatta.feature.course.domain.Drill
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseParserTest {

    private val dir = File("src/main/assets/courses")

    @Test
    fun everyBundledCourseParsesAndHasTheFourDoors() {
        val summaries = CourseParser.parseSummaries(File(dir, "index.json").readText())
        assertEquals(dir.listFiles()!!.count { it.name != "index.json" }, summaries.size)
        summaries.forEach { s ->
            val c = CourseParser.parseCourse(File(dir, "${s.id}.json").readText())
            assertEquals(s.id, c.summary.id)
            assertTrue("${s.id} panels", c.panels.isNotEmpty())
            assertTrue("${s.id} reel", c.reel.isNotEmpty())
            assertTrue("${s.id} drills", c.drills.size >= 3)
            assertTrue("${s.id} inspect", c.inspect.isNotEmpty() && c.inspect.all { it.spots.isNotEmpty() })
        }
    }

    @Test
    fun drillKindsKeepTheirAnswers() {
        val c = CourseParser.parseCourse(File(dir, "tamagake.json").readText())
        val sling = c.drills.filterIsInstance<Drill.Sling>().single()
        assertEquals(2000, sling.loadKg)
        assertEquals(1200, sling.wllKg)
        val machine = CourseParser.parseCourse(File(dir, "machine-hazards.json").readText())
        assertEquals(listOf(true, false, true, true, false), machine.drills.filterIsInstance<Drill.Ring>().single().wheels)
    }
}
