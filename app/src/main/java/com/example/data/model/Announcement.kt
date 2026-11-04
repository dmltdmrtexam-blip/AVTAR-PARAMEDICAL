package com.example.data.model

data class AcademyAnnouncement(
    val id: String,
    val title: String,
    val content: String,
    val category: String, // "Exam", "Course", "General", "Urgent"
    val date: String,
    val isPinned: Boolean = false
)

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: String,
    val type: String, // "MOCK_TEST", "COURSE", "PAYMENT", "SYSTEM"
    val isRead: Boolean = false
)

data class BackendConfig(
    val supabaseUrl: String = "https://avtar-paramedical.supabase.co",
    val supabaseAnonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_anon_key_for_avtar_paramedical_academy",
    val razorpayKeyId: String = "rzp_live_AvtarAcademyKey99",
    val fcmSenderId: String = "844730203419",
    val isLiveConnected: Boolean = true
)
