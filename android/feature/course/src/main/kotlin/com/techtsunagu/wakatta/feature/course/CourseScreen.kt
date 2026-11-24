package com.techtsunagu.wakatta.feature.course

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.stripFurigana
import com.techtsunagu.wakatta.feature.course.domain.Door

@Composable
fun CourseScreen(
    onBackClick: () -> Unit = {},
    viewModel: CourseViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it == CourseEffect.Exit) onBackClick() }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onEvent(CourseEvent.Pause) }
    BackHandler(enabled = state.canGoBack) { viewModel.onEvent(CourseEvent.Back) }
    CourseContent(state = state, onEvent = viewModel::onEvent)
}

@StringRes
internal fun AppError.courseMessageRes(): Int = when (this) {
    AppError.NotFound -> R.string.course_error_not_found
    AppError.SessionExpired -> R.string.course_error_session
    else -> R.string.course_error_generic
}

@StringRes
internal fun Door.nameRes(): Int = when (this) {
    Door.READ -> R.string.course_door_read
    Door.WATCH -> R.string.course_door_watch
    Door.PLAY -> R.string.course_door_play
    Door.INSPECT -> R.string.course_door_inspect
}

@StringRes
internal fun Door.subRes(): Int = when (this) {
    Door.READ -> R.string.course_door_read_sub
    Door.WATCH -> R.string.course_door_watch_sub
    Door.PLAY -> R.string.course_door_play_sub
    Door.INSPECT -> R.string.course_door_inspect_sub
}

@Composable
fun CourseContent(
    state: CourseUiState,
    onEvent: (CourseEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = state.course?.title?.let { stripFurigana(it.ja) }
        ?: stringResource(R.string.course_screen_title)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { WakattaTopBar(title = title, onBackClick = { onEvent(CourseEvent.Back) }) },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            if (state.saveError != null) SaveErrorText()
            val course = state.course
            when {
                state.isLoading -> LoadingState(message = stringResource(R.string.course_loading))
                state.error != null -> ErrorState(
                    message = stringResource(state.error.courseMessageRes()),
                    onRetry = { onEvent(CourseEvent.Retry) },
                    retryLabel = stringResource(R.string.course_retry),
                )
                state.isEmpty -> EmptyState(
                    title = stringResource(R.string.course_screen_title),
                    message = stringResource(R.string.course_empty),
                )
                course == null -> CourseList(state, onEvent)
                state.door == null -> DoorsHome(state, onEvent)
                else -> DoorBody(state, course, onEvent)
            }
        }
    }
}

@Composable
private fun DoorBody(state: CourseUiState, course: CourseView, onEvent: (CourseEvent) -> Unit) {
    when (state.door) {
        Door.READ -> ReadDoor(state, course, onEvent)
        Door.WATCH -> WatchDoor(state, course, onEvent)
        Door.PLAY -> PlayDoor(state, course, onEvent)
        Door.INSPECT -> InspectDoor(state, course, onEvent)
        null -> Unit
    }
}
