package com.techtsunagu.wakatta.feature.machines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun NoteQuizBody(
    state: NoteUiState,
    onEvent: (NoteEvent) -> Unit,
) {
    val version = state.currentVersion ?: return
    val questions = version.questions
    val currentQ = questions.getOrNull(state.currentQuestionIndex) ?: return

    val prompt = when (state.userLanguage) {
        "bn" -> currentQ.qBn
        "en" -> currentQ.qEn
        else -> currentQ.qJa
    }
    val options = when (state.userLanguage) {
        "bn" -> currentQ.optionsBn
        "en" -> currentQ.optionsEn
        else -> currentQ.optionsJa
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Dots progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                questions.forEachIndexed { i, _ ->
                    val dotColor = when {
                        i < state.currentQuestionIndex -> MaterialTheme.colorScheme.primary
                        i == state.currentQuestionIndex -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(10.dp)
                            .background(color = dotColor, shape = CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.machines_note_question_count, state.currentQuestionIndex + 1, questions.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prompt,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Options List
            options.forEachIndexed { j, optionText ->
                val isSelected = state.selectedOptionIndex == j
                val isAnswerSubmitted = state.isAnswerSubmitted

                val (borderColor, containerColor) = if (isSelected) {
                    MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.surface
                }

                Surface(
                    onClick = { onEvent(NoteEvent.SelectOption(j)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .height(56.dp)
                        .border(1.5.dp, borderColor, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = containerColor,
                    enabled = !isAnswerSubmitted
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (state.isAnswerSubmitted) {
            Button(
                onClick = { onEvent(NoteEvent.NextQuestion) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = if (state.currentQuestionIndex < questions.size - 1) {
                        stringResource(R.string.machines_note_next_question)
                    } else {
                        stringResource(R.string.machines_note_see_result)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
