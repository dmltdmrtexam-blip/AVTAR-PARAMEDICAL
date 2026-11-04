package com.example.data.model

data class StudentProfile(
    val id: String,
    val fullName: String,
    val mobileNumber: String,
    val email: String,
    val rollNumber: String,
    val stream: String, // DMLT, DRT (Radiology), BMLT, OT Technology, ECG & Cardiology
    val avatarUrl: String = "",
    val isAdmin: Boolean = false,
    val isBlocked: Boolean = false,
    val streakDays: Int = 5,
    val deviceId: String = "DEV-ANDROID-001"
)

enum class UserRole {
    STUDENT,
    ADMIN,
    PROCTOR
}

data class AuthSession(
    val profile: StudentProfile?,
    val isAuthenticated: Boolean = false,
    val verificationOtp: String = ""
)
