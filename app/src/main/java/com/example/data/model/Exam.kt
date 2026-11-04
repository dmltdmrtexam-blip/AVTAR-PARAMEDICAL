package com.example.data.model

data class QuestionOption(
    val key: String, // "A", "B", "C", "D"
    val text: String,
    val isCorrect: Boolean = false
)

data class Question(
    val id: String,
    val subject: String,
    val topic: String,
    val questionText: String,
    val options: List<QuestionOption>,
    val correctOptionKey: String,
    val explanation: String,
    val difficulty: String = "Medium",
    val isApproved: Boolean = true,
    val isBookmarked: Boolean = false
)

enum class PaletteStatus {
    NOT_VISITED,
    NOT_ANSWERED,
    ANSWERED,
    MARKED_FOR_REVIEW,
    ANSWERED_AND_MARKED_FOR_REVIEW
}

data class MockTest(
    val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val totalQuestions: Int = 50,
    val durationMinutes: Int = 60,
    val marksPerCorrect: Double = 1.0,
    val negativeMarks: Double = 0.25,
    val passingPercentage: Double = 50.0,
    val isProctored: Boolean = true,
    val allowInstantResults: Boolean = true,
    val questions: List<Question> = emptyList()
)

data class StudentCbtAnswer(
    val questionId: String,
    val selectedOptionKey: String? = null,
    val isMarkedForReview: Boolean = false,
    val timeSpentSeconds: Int = 0
)

data class TestResult(
    val id: String,
    val testId: String,
    val testTitle: String,
    val studentName: String,
    val totalQuestions: Int,
    val totalMarks: Double,
    val scoreObtained: Double,
    val percentage: Double,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val isPassed: Boolean,
    val timeTakenSeconds: Int,
    val submittedAt: String,
    val questionsReview: List<QuestionResultReview> = emptyList()
)

data class QuestionResultReview(
    val question: Question,
    val studentAnswerKey: String?,
    val isCorrect: Boolean
)
