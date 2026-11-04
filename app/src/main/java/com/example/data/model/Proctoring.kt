package com.example.data.model

data class ProctoringSession(
    val sessionId: String,
    val studentId: String,
    val studentName: String,
    val testId: String,
    val testTitle: String,
    val isCameraActive: Boolean,
    val isLocationVerified: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val tabSwitchCount: Int = 0,
    val suspiciousEvents: List<ProctoringEvent> = emptyList(),
    val isLive: Boolean = true
)

data class ProctoringEvent(
    val id: String,
    val type: String, // "FULLSCREEN_EXIT", "APP_BACKGROUND", "MULTIPLE_LOGIN_ATTEMPT"
    val timestamp: String,
    val details: String,
    val severity: String = "WARNING" // INFO, WARNING, CRITICAL
)
