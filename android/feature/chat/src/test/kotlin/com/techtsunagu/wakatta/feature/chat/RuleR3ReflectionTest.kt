package com.techtsunagu.wakatta.feature.chat

import com.techtsunagu.wakatta.feature.chat.model.CheckQuestionForWorker
import com.techtsunagu.wakatta.feature.chat.model.MessageForWorkerDto
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Rule R-3 Enforcement Test:
 * "A Worker-facing model has no field for the right answer (correctIndex).
 * Grading and Wakatta Status come from the server; a Fake computes them inside the Fake,
 * never in a ViewModel or composable. A reflection test asserts the field is absent."
 */
class RuleR3ReflectionTest {

    @Test
    fun checkQuestionForWorkerHasNoCorrectIndexOrAnswerField() {
        val forbiddenNames = setOf(
            "correctindex",
            "correct_index",
            "correctanswer",
            "correct_answer",
            "answer",
            "answers",
            "answerindex",
        )

        val declaredFields = CheckQuestionForWorker::class.java.declaredFields.map { it.name.lowercase() }
        for (field in declaredFields) {
            assertFalse(
                "Violation of Rule R-3: CheckQuestionForWorker contains forbidden answer field '$field'",
                forbiddenNames.contains(field),
            )
        }
    }

    @Test
    fun messageForWorkerDtoHasNoAnswerKeyField() {
        val forbiddenNames = setOf(
            "correctindex",
            "correct_index",
            "correctanswer",
            "correct_answer",
            "answerkey",
        )

        val declaredFields = MessageForWorkerDto::class.java.declaredFields.map { it.name.lowercase() }
        for (field in declaredFields) {
            assertFalse(
                "Violation of Rule R-3: MessageForWorkerDto contains forbidden answer key field '$field'",
                forbiddenNames.contains(field),
            )
        }
    }
}
