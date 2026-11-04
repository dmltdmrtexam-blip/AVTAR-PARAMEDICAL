package com.example.data.model

data class CourseCategory(
    val id: String,
    val name: String,
    val code: String,
    val description: String,
    val iconName: String
)

data class CourseMaterial(
    val id: String,
    val courseId: String,
    val title: String,
    val description: String,
    val fileType: String = "PDF", // PDF, DRIVE_LINK, DOCS
    val fileUrl: String,
    val pageCount: Int = 24,
    val isFreePreview: Boolean = false,
    val lastReadPage: Int = 1
)

data class VideoLesson(
    val id: String,
    val courseId: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val durationSeconds: Int,
    val isFreePreview: Boolean = false,
    val progressSeconds: Int = 0,
    val playbackOrder: Int = 1
)

data class Course(
    val id: String,
    val categoryCode: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String = "",
    val price: Double,
    val discountPrice: Double,
    val validityDays: Int = 365,
    val isFree: Boolean = false,
    val isEnrolled: Boolean = false,
    val rating: Double = 4.9,
    val enrolledCount: Int = 412,
    val instructor: String = "Dr. Avtar & Expert Faculty",
    val materials: List<CourseMaterial> = emptyList(),
    val videos: List<VideoLesson> = emptyList()
)
