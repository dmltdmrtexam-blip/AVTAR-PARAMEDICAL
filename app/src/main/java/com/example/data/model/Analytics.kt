package com.example.data.model

data class SubjectScore(
    val subject: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val accuracyPercentage: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val studentName: String,
    val stream: String,
    val score: Double,
    val testsCompleted: Int,
    val avatarInitial: String
)

data class StudentPerformanceAnalytics(
    val testsAttempted: Int,
    val averageScorePercent: Double,
    val highestScore: Double,
    val overallAccuracy: Double,
    val avgTimePerQuestionSeconds: Int,
    val weakSubjects: List<String>,
    val strongSubjects: List<String>,
    val subjectBreakdown: List<SubjectScore>,
    val recentScoreTrends: List<Double>
)

data class DailyMcqPractice(
    val id: String,
    val date: String,
    val question: Question,
    val studentAnswered: Boolean = false,
    val selectedOptionKey: String? = null
)
