package com.techtsunagu.wakatta.feature.course

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.feature.course.domain.Door
import com.techtsunagu.wakatta.feature.course.domain.Progress
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class CourseComposeTest {

    @get:Rule
    val rule = createComposeRule()

    private fun shown(text: String) = rule.onAllNodesWithText(text)[0].assertIsDisplayed()

    private val item = CourseItem("c1", "core", 15, Line("指差呼称{しさこしょう}", "Pointing and calling"), Line("目で見る", "Look"), doorsDone = 2)
    private val view = CourseView(
        id = "c1", title = item.title,
        panels = listOf(PanelView(true, Line("はじめ", "Start"))),
        handbook = listOf(Line("手順", "Steps")),
        reel = listOf(Line("ヨシ！", "Yoshi")),
        drills = listOf(DrillItem("seq", Line("順番", "Order"))),
        inspect = listOf(InspectView("i1", Line("ヘルメット", "Helmet"), listOf(InspectSpotView(40, 20, Line("A"))), showsDefect = true)),
    )

    @Test
    fun loading() {
        rule.setContent { CourseContent(CourseUiState(isLoading = true), {}) }
        shown("Loading...")
    }

    @Test
    fun empty() {
        rule.setContent { CourseContent(CourseUiState(isLoading = false), {}) }
        shown("No courses yet.")
    }

    @Test
    fun error() {
        rule.setContent { CourseContent(CourseUiState(isLoading = false, error = AppError.NotFound), {}) }
        shown("This course is missing from the app. Update the app, then try again.")
        shown("Try again")
    }

    @Test
    fun courseList() {
        rule.setContent {
            CourseContent(CourseUiState(isLoading = false, hasTranslation = true, courses = listOf(item)), {})
        }
        shown("Pointing and calling")
        shown("2 of 4 doors")
    }

    @Test
    fun doorsHome() {
        rule.setContent {
            CourseContent(
                CourseUiState(isLoading = false, course = view, progress = Progress(read = true), hasTranslation = true),
                {},
            )
        }
        shown("Read")
        shown("Done")
    }

    @Test
    fun readDoor() {
        rule.setContent {
            CourseContent(
                CourseUiState(isLoading = false, course = view, door = Door.READ, hasTranslation = true),
                {},
            )
        }
        shown("I read it")
    }

    @Test
    fun saveErrorBanner() {
        rule.setContent {
            CourseContent(CourseUiState(isLoading = false, course = view, saveError = AppError.Server), {})
        }
        shown("Your progress could not be saved on this phone. Do this step again.")
    }
}
