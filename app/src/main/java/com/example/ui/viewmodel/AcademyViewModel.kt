package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AcademyRepository
import com.example.data.repository.PdfExtractorService
import com.example.data.repository.RazorpayPaymentService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AcademyViewModel(
    val repository: AcademyRepository = AcademyRepository()
) : ViewModel() {

    // Global navigation/tab selection
    val currentUser = repository.currentUser
    val isUserLoggedIn = repository.isUserLoggedIn
    val categories = repository.categories
    val courses = repository.courses
    val mockTests = repository.mockTests
    val testResults = repository.testResults
    val questionBank = repository.questionBank
    val paymentHistory = repository.paymentHistory
    val proctoringSessions = repository.proctoringSessions
    val announcements = repository.announcements
    val notifications = repository.notifications
    val dailyPracticeMcq = repository.dailyPracticeMcq
    val backendConfig = repository.backendConfig
    val auditLogs = repository.auditLogs
    val registeredStudents = repository.registeredStudents

    // Course Category Filter
    private val _selectedCategoryCode = MutableStateFlow("ALL")
    val selectedCategoryCode: StateFlow<String> = _selectedCategoryCode.asStateFlow()

    // Selected Course for Detail Screen
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    // Active PDF Document Viewer State
    private val _activePdfMaterial = MutableStateFlow<CourseMaterial?>(null)
    val activePdfMaterial: StateFlow<CourseMaterial?> = _activePdfMaterial.asStateFlow()
    val pdfCurrentPage = MutableStateFlow(1)
    val pdfZoomLevel = MutableStateFlow(1.0f)

    // Active Video Player State
    private val _activeVideoLesson = MutableStateFlow<VideoLesson?>(null)
    val activeVideoLesson: StateFlow<VideoLesson?> = _activeVideoLesson.asStateFlow()
    val isVideoPlaying = MutableStateFlow(true)
    val videoPlaybackSpeed = MutableStateFlow(1.0f)
    val videoCurrentPositionSeconds = MutableStateFlow(0)

    // ==========================================
    // CBT EXAM ENGINE STATE
    // ==========================================
    private val _activeExam = MutableStateFlow<MockTest?>(null)
    val activeExam: StateFlow<MockTest?> = _activeExam.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _studentAnswers = MutableStateFlow<Map<String, StudentCbtAnswer>>(emptyMap())
    val studentAnswers: StateFlow<Map<String, StudentCbtAnswer>> = _studentAnswers.asStateFlow()

    private val _examRemainingSeconds = MutableStateFlow(0)
    val examRemainingSeconds: StateFlow<Int> = _examRemainingSeconds.asStateFlow()

    private val _isExamProctoringActive = MutableStateFlow(true)
    val isExamProctoringActive: StateFlow<Boolean> = _isExamProctoringActive.asStateFlow()

    private val _isLocationVerified = MutableStateFlow(true)
    val isLocationVerified: StateFlow<Boolean> = _isLocationVerified.asStateFlow()

    private val _fullscreenWarningCount = MutableStateFlow(0)
    val fullscreenWarningCount: StateFlow<Int> = _fullscreenWarningCount.asStateFlow()

    private val _latestTestResult = MutableStateFlow<TestResult?>(null)
    val latestTestResult: StateFlow<TestResult?> = _latestTestResult.asStateFlow()

    private var examTimerJob: Job? = null

    // ==========================================
    // PDF QUESTION EXTRACTOR STATE (ADMIN)
    // ==========================================
    private val _extractedQuestions = MutableStateFlow<List<PdfExtractorService.ExtractedMcq>>(emptyList())
    val extractedQuestions: StateFlow<List<PdfExtractorService.ExtractedMcq>> = _extractedQuestions.asStateFlow()
    val isExtractingPdf = MutableStateFlow(false)

    // Toast / Feedback message
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun selectCategory(code: String) {
        _selectedCategoryCode.value = code
    }

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // ==========================================
    // AUTH LOGIC
    // ==========================================
    fun sendOtp(mobileNumber: String): String {
        val otp = repository.sendLoginOtp(mobileNumber)
        _statusMessage.value = "OTP sent to +91 $mobileNumber (Demo OTP: $otp)"
        return otp
    }

    fun verifyOtp(mobileNumber: String, otp: String): Boolean {
        val success = repository.verifyLoginOtp(mobileNumber, otp)
        if (success) {
            _statusMessage.value = "Login successful! Welcome back."
        } else {
            _statusMessage.value = "Invalid OTP. Please enter 8447 or 1234."
        }
        return success
    }

    fun registerStudent(name: String, mobile: String, email: String, stream: String): Boolean {
        val ok = repository.registerStudent(name, mobile, email, stream)
        if (ok) {
            _statusMessage.value = "Welcome to Avtar Paramedical Academy, $name!"
        }
        return ok
    }

    fun logout() {
        repository.logout()
        _statusMessage.value = "Logged out successfully"
    }

    fun toggleRoleForDemo() {
        val current = currentUser.value
        val newAdminState = !(current?.isAdmin ?: false)
        repository.switchRoleForDemo(newAdminState)
        _statusMessage.value = if (newAdminState) "Switched to Director & Admin mode" else "Switched to Student mode"
    }

    // ==========================================
    // LEARNING VIEWERS
    // ==========================================
    fun openPdfMaterial(material: CourseMaterial) {
        _activePdfMaterial.value = material
        pdfCurrentPage.value = material.lastReadPage
        pdfZoomLevel.value = 1.0f
    }

    fun closePdfViewer() {
        _activePdfMaterial.value = null
    }

    fun setPdfPage(page: Int) {
        val total = _activePdfMaterial.value?.pageCount ?: 1
        if (page in 1..total) {
            pdfCurrentPage.value = page
        }
    }

    fun zoomInPdf() {
        pdfZoomLevel.value = minOf(2.5f, pdfZoomLevel.value + 0.25f)
    }

    fun zoomOutPdf() {
        pdfZoomLevel.value = maxOf(0.75f, pdfZoomLevel.value - 0.25f)
    }

    fun openVideoLesson(lesson: VideoLesson) {
        _activeVideoLesson.value = lesson
        isVideoPlaying.value = true
        videoPlaybackSpeed.value = 1.0f
        videoCurrentPositionSeconds.value = lesson.progressSeconds
    }

    fun closeVideoPlayer() {
        _activeVideoLesson.value = null
    }

    fun toggleVideoPlayPause() {
        isVideoPlaying.value = !isVideoPlaying.value
    }

    fun seekVideoRelative(deltaSeconds: Int) {
        val current = videoCurrentPositionSeconds.value
        val total = _activeVideoLesson.value?.durationSeconds ?: 1
        videoCurrentPositionSeconds.value = (current + deltaSeconds).coerceIn(0, total)
    }

    fun setVideoSpeed(speed: Float) {
        videoPlaybackSpeed.value = speed
    }

    // ==========================================
    // CBT EXAM ENGINE
    // ==========================================
    fun startCbtExam(test: MockTest) {
        _activeExam.value = test
        _currentQuestionIndex.value = 0
        _studentAnswers.value = emptyMap()
        _examRemainingSeconds.value = test.durationMinutes * 60
        _fullscreenWarningCount.value = 0
        _isExamProctoringActive.value = test.isProctored

        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (_examRemainingSeconds.value > 0) {
                delay(1000)
                _examRemainingSeconds.value = _examRemainingSeconds.value - 1
            }
            // Auto submit when time expires
            submitCbtExam()
        }
    }

    fun selectQuestion(index: Int) {
        val total = _activeExam.value?.questions?.size ?: 0
        if (index in 0 until total) {
            _currentQuestionIndex.value = index
        }
    }

    fun selectExamOption(optionKey: String) {
        val exam = _activeExam.value ?: return
        val q = exam.questions.getOrNull(_currentQuestionIndex.value) ?: return
        val currentAns = _studentAnswers.value[q.id]
        val updated = (currentAns ?: StudentCbtAnswer(q.id)).copy(selectedOptionKey = optionKey)
        _studentAnswers.value = _studentAnswers.value + (q.id to updated)
    }

    fun clearExamResponse() {
        val exam = _activeExam.value ?: return
        val q = exam.questions.getOrNull(_currentQuestionIndex.value) ?: return
        val currentAns = _studentAnswers.value[q.id]
        if (currentAns != null) {
            val updated = currentAns.copy(selectedOptionKey = null)
            _studentAnswers.value = _studentAnswers.value + (q.id to updated)
        }
    }

    fun markExamForReviewAndNext() {
        val exam = _activeExam.value ?: return
        val q = exam.questions.getOrNull(_currentQuestionIndex.value) ?: return
        val currentAns = _studentAnswers.value[q.id]
        val updated = (currentAns ?: StudentCbtAnswer(q.id)).copy(isMarkedForReview = true)
        _studentAnswers.value = _studentAnswers.value + (q.id to updated)

        // Go next if possible
        if (_currentQuestionIndex.value < exam.questions.size - 1) {
            _currentQuestionIndex.value = _currentQuestionIndex.value + 1
        }
    }

    fun saveAndNext() {
        val exam = _activeExam.value ?: return
        if (_currentQuestionIndex.value < exam.questions.size - 1) {
            _currentQuestionIndex.value = _currentQuestionIndex.value + 1
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value = _currentQuestionIndex.value - 1
        }
    }

    fun getQuestionPaletteStatus(questionId: String): PaletteStatus {
        val ans = _studentAnswers.value[questionId] ?: return PaletteStatus.NOT_VISITED
        return when {
            ans.selectedOptionKey != null && ans.isMarkedForReview -> PaletteStatus.ANSWERED_AND_MARKED_FOR_REVIEW
            ans.isMarkedForReview -> PaletteStatus.MARKED_FOR_REVIEW
            ans.selectedOptionKey != null -> PaletteStatus.ANSWERED
            else -> PaletteStatus.NOT_ANSWERED
        }
    }

    fun logExamTabSwitchAlert() {
        _fullscreenWarningCount.value = _fullscreenWarningCount.value + 1
        val exam = _activeExam.value ?: return
        repository.logProctoringAlert(
            sessionId = "live_session_${currentUser.value?.id}",
            alertType = "FULLSCREEN_EXIT",
            details = "Candidate exited full screen (Warning #${_fullscreenWarningCount.value})"
        )
        _statusMessage.value = "⚠️ Proctor Alert: Full-screen exit detected! Recorded in audit log."
    }

    fun submitCbtExam(): TestResult? {
        examTimerJob?.cancel()
        val exam = _activeExam.value ?: return null
        val timeTaken = (exam.durationMinutes * 60) - _examRemainingSeconds.value
        val result = repository.submitExamAttempt(
            testId = exam.id,
            answers = _studentAnswers.value,
            timeTakenSeconds = maxOf(30, timeTaken)
        )
        _latestTestResult.value = result
        _activeExam.value = null
        return result
    }

    // ==========================================
    // PAYMENT GATEWAY & OFFLINE VERIFICATION
    // ==========================================
    fun enrollWithRazorpay(course: Course, method: PaymentMethod = PaymentMethod.UPI) {
        val mobile = currentUser.value?.mobileNumber ?: "9876543210"
        val order = RazorpayPaymentService.createOrder(course.id, course.title, course.discountPrice, mobile)
        val result = RazorpayPaymentService.processAndVerifyPayment(order, method)
        if (result.isVerified) {
            repository.enrollInCourse(course.id, result.transaction.transactionId, method, result.transaction.amount)
            _statusMessage.value = result.message
        }
    }

    fun submitOfflinePayment(course: Course, utrReference: String) {
        if (utrReference.isBlank()) {
            _statusMessage.value = "Please enter valid 12-digit UTR reference number"
            return
        }
        repository.submitOfflinePayment(course.id, course.title, course.discountPrice, utrReference)
        _statusMessage.value = "UTR reference submitted for Admin manual verification."
    }

    fun approveOfflinePayment(transactionId: String) {
        repository.approveOfflinePayment(transactionId)
        _statusMessage.value = "Payment approved & course access granted to student."
    }

    fun rejectOfflinePayment(transactionId: String) {
        repository.rejectOfflinePayment(transactionId)
        _statusMessage.value = "Payment marked rejected."
    }

    // ==========================================
    // PDF EXTRACTION & OCR (ADMIN)
    // ==========================================
    fun triggerSamplePdfExtraction() {
        isExtractingPdf.value = true
        viewModelScope.launch {
            delay(1200) // Realistic OCR processing simulation
            val sampleText = PdfExtractorService.getSamplePdfExamText()
            val extracted = PdfExtractorService.extractQuestionsFromText(sampleText, repository.questionBank.value)
            _extractedQuestions.value = extracted
            isExtractingPdf.value = false
            _statusMessage.value = "Successfully extracted ${extracted.size} MCQs from PDF! Ready for review."
        }
    }

    fun extractCustomText(text: String) {
        isExtractingPdf.value = true
        viewModelScope.launch {
            delay(800)
            val extracted = PdfExtractorService.extractQuestionsFromText(text, repository.questionBank.value)
            _extractedQuestions.value = extracted
            isExtractingPdf.value = false
            _statusMessage.value = "Parsed ${extracted.size} questions."
        }
    }

    fun approveAllExtractedQuestions(subject: String, topic: String) {
        val list = _extractedQuestions.value.map { it.toQuestion(subject, topic) }
        repository.approveExtractedQuestions(list)
        _extractedQuestions.value = emptyList()
        _statusMessage.value = "Added ${list.size} questions to official Question Bank!"
    }

    fun removeExtractedQuestion(index: Int) {
        val current = _extractedQuestions.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _extractedQuestions.value = current
        }
    }

    // ==========================================
    // OTHER ADMIN & CONFIG ACTIONS
    // ==========================================
    fun addCourse(title: String, categoryCode: String, desc: String, price: Double, discountPrice: Double, isFree: Boolean) {
        repository.addCourse(title, categoryCode, desc, price, discountPrice, isFree)
        _statusMessage.value = "Course '$title' created successfully."
    }

    fun deleteCourse(courseId: String) {
        repository.deleteCourse(courseId)
        _statusMessage.value = "Course removed."
    }

    fun toggleStudentBlock(studentId: String) {
        repository.toggleStudentBlockStatus(studentId)
    }

    fun postAnnouncement(title: String, content: String, category: String, isPinned: Boolean) {
        repository.postAnnouncement(title, content, category, isPinned)
        _statusMessage.value = "Announcement published across academy network."
    }

    fun saveBackendCredentials(url: String, key: String, rzpKey: String, fcmId: String) {
        repository.updateBackendConfig(url, key, rzpKey, fcmId)
        _statusMessage.value = "Supabase & Razorpay configurations synchronized successfully."
    }

    fun answerDailyPracticeMcq(optionKey: String) {
        repository.answerDailyMcq(optionKey)
    }
}
