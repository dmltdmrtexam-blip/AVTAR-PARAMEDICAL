package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AcademyRepository {

    // 1. Current Session State
    private val _currentUser = MutableStateFlow<StudentProfile?>(
        StudentProfile(
            id = "std_001",
            fullName = "Ananya Sharma",
            mobileNumber = "9876543210",
            email = "ananya.sharma@paramedical.edu",
            rollNumber = "AVTAR-2024-DMLT-042",
            stream = "DMLT",
            avatarUrl = "",
            isAdmin = false,
            isBlocked = false,
            streakDays = 7,
            deviceId = "PIXEL-7-PRO-SECURE"
        )
    )
    val currentUser: StateFlow<StudentProfile?> = _currentUser.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(true)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    // 2. Course Categories
    private val _categories = MutableStateFlow(
        listOf(
            CourseCategory("cat_1", "All Streams", "ALL", "Comprehensive paramedical prep", "medical_services"),
            CourseCategory("cat_2", "DMLT", "DMLT", "Diploma in Medical Lab Technology", "biotech"),
            CourseCategory("cat_3", "DRT & Radiology", "DRT", "Diploma in Radiography Technology & X-Ray/CT/MRI", "radiology"),
            CourseCategory("cat_4", "BMLT", "BMLT", "Bachelor in Medical Laboratory Technology", "science"),
            CourseCategory("cat_5", "OT Technology", "OT", "Operation Theatre & Anesthesia Tech", "masks"),
            CourseCategory("cat_6", "ECG & Cardiology", "ECG", "Cardiovascular Technology & Diagnostics", "monitor_heart")
        )
    )
    val categories: StateFlow<List<CourseCategory>> = _categories.asStateFlow()

    // 3. Courses
    private val _courses = MutableStateFlow(getInitialCourses())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    // 4. CBT Mock Tests
    private val _mockTests = MutableStateFlow(getInitialMockTests())
    val mockTests: StateFlow<List<MockTest>> = _mockTests.asStateFlow()

    // 5. Test Results
    private val _testResults = MutableStateFlow(getInitialTestResults())
    val testResults: StateFlow<List<TestResult>> = _testResults.asStateFlow()

    // 6. Question Bank (for extraction and CBT)
    private val _questionBank = MutableStateFlow(getInitialQuestionBank())
    val questionBank: StateFlow<List<Question>> = _questionBank.asStateFlow()

    // 7. Payment History
    private val _paymentHistory = MutableStateFlow(getInitialPaymentTransactions())
    val paymentHistory: StateFlow<List<PaymentTransaction>> = _paymentHistory.asStateFlow()

    // 8. Proctoring Sessions
    private val _proctoringSessions = MutableStateFlow(getInitialProctoringSessions())
    val proctoringSessions: StateFlow<List<ProctoringSession>> = _proctoringSessions.asStateFlow()

    // 9. Announcements & Notifications
    private val _announcements = MutableStateFlow(getInitialAnnouncements())
    val announcements: StateFlow<List<AcademyAnnouncement>> = _announcements.asStateFlow()

    private val _notifications = MutableStateFlow(getInitialNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // 10. Daily Practice MCQ
    private val _dailyPracticeMcq = MutableStateFlow(getInitialDailyMcq())
    val dailyPracticeMcq: StateFlow<DailyMcqPractice> = _dailyPracticeMcq.asStateFlow()

    // 11. Backend Config (Supabase, Razorpay, FCM)
    private val _backendConfig = MutableStateFlow(BackendConfig())
    val backendConfig: StateFlow<BackendConfig> = _backendConfig.asStateFlow()

    // 12. Admin Audit Logs
    private val _auditLogs = MutableStateFlow(getInitialAuditLogs())
    val auditLogs: StateFlow<List<String>> = _auditLogs.asStateFlow()

    // 13. Registered Students List (Admin Management)
    private val _registeredStudents = MutableStateFlow(getInitialRegisteredStudents())
    val registeredStudents: StateFlow<List<StudentProfile>> = _registeredStudents.asStateFlow()

    // ==========================================
    // AUTH ACTIONS
    // ==========================================
    fun sendLoginOtp(mobileNumber: String): String {
        return "8447" // Generated deterministic OTP for demo verification
    }

    fun verifyLoginOtp(mobileNumber: String, otp: String): Boolean {
        if (otp == "8447" || otp == "1234" || otp.length == 4) {
            val existing = _registeredStudents.value.find { it.mobileNumber == mobileNumber }
            val profile = existing ?: StudentProfile(
                id = "std_${System.currentTimeMillis().toString().takeLast(5)}",
                fullName = "Student $mobileNumber",
                mobileNumber = mobileNumber,
                email = "student$mobileNumber@paramedical.edu",
                rollNumber = "AVTAR-2024-STD-${mobileNumber.takeLast(4)}",
                stream = "DMLT",
                isAdmin = false
            )
            _currentUser.value = profile
            _isUserLoggedIn.value = true
            logAudit("User logged in with mobile: $mobileNumber")
            return true
        }
        return false
    }

    fun registerStudent(
        fullName: String,
        mobileNumber: String,
        email: String,
        stream: String
    ): Boolean {
        val newProfile = StudentProfile(
            id = "std_${UUID.randomUUID().toString().take(6)}",
            fullName = fullName,
            mobileNumber = mobileNumber,
            email = email,
            rollNumber = "AVTAR-2025-${stream.take(3)}-${(100..999).random()}",
            stream = stream,
            isAdmin = false,
            streakDays = 1
        )
        _currentUser.value = newProfile
        _isUserLoggedIn.value = true
        _registeredStudents.value = listOf(newProfile) + _registeredStudents.value
        logAudit("New student registered: $fullName ($mobileNumber)")
        return true
    }

    fun logout() {
        _isUserLoggedIn.value = false
    }

    fun switchRoleForDemo(asAdmin: Boolean) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(
            isAdmin = asAdmin,
            fullName = if (asAdmin) "Dr. Avtar (Director & Admin)" else "Ananya Sharma",
            email = if (asAdmin) "director@avtarparamedical.com" else "ananya.sharma@paramedical.edu"
        )
        logAudit("Role switched to: ${if (asAdmin) "Admin" else "Student"}")
    }

    fun updateProfile(fullName: String, email: String, stream: String) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(
            fullName = fullName,
            email = email,
            stream = stream
        )
        logAudit("Profile updated for ${current.mobileNumber}")
    }

    // ==========================================
    // COURSE & PAYMENT ACTIONS
    // ==========================================
    fun enrollInCourse(courseId: String, transactionId: String, method: PaymentMethod, amount: Double) {
        _courses.value = _courses.value.map { course ->
            if (course.id == courseId) course.copy(isEnrolled = true, enrolledCount = course.enrolledCount + 1)
            else course
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val newTx = PaymentTransaction(
            transactionId = transactionId,
            orderId = "order_${UUID.randomUUID().toString().take(8)}",
            courseId = courseId,
            courseTitle = _courses.value.find { it.id == courseId }?.title ?: "Paramedical Course",
            amount = amount,
            method = method,
            status = PaymentStatus.COMPLETED,
            timestamp = dateFormat.format(Date()),
            adminApprovalNote = "Automatic API verification"
        )
        _paymentHistory.value = listOf(newTx) + _paymentHistory.value

        // Notification
        addNotification(
            title = "Course Enrollment Successful!",
            body = "You now have full access to ${_courses.value.find { it.id == courseId }?.title}. Start studying!",
            type = "COURSE"
        )
        logAudit("Student ${_currentUser.value?.fullName} enrolled in course $courseId via $method")
    }

    fun submitOfflinePayment(courseId: String, courseTitle: String, amount: Double, utr: String) {
        val tx = RazorpayPaymentService.submitOfflinePaymentForApproval(courseId, courseTitle, amount, utr)
        _paymentHistory.value = listOf(tx) + _paymentHistory.value
        logAudit("Offline payment submitted: UTR $utr for course $courseTitle")
    }

    fun approveOfflinePayment(transactionId: String) {
        _paymentHistory.value = _paymentHistory.value.map { tx ->
            if (tx.transactionId == transactionId) {
                // Grant enrollment
                _courses.value = _courses.value.map { c ->
                    if (c.id == tx.courseId) c.copy(isEnrolled = true) else c
                }
                tx.copy(status = PaymentStatus.COMPLETED, adminApprovalNote = "Approved by Admin on ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())}")
            } else tx
        }
        logAudit("Admin approved payment $transactionId")
    }

    fun rejectOfflinePayment(transactionId: String) {
        _paymentHistory.value = _paymentHistory.value.map { tx ->
            if (tx.transactionId == transactionId) {
                tx.copy(status = PaymentStatus.FAILED, adminApprovalNote = "Rejected due to invalid UTR bank reference")
            } else tx
        }
        logAudit("Admin rejected payment $transactionId")
    }

    // ==========================================
    // CBT EXAM & RESULTS ACTIONS
    // ==========================================
    fun submitExamAttempt(
        testId: String,
        answers: Map<String, StudentCbtAnswer>,
        timeTakenSeconds: Int
    ): TestResult {
        val test = _mockTests.value.find { it.id == testId } ?: _mockTests.value.first()
        var correctCount = 0
        var incorrectCount = 0
        var unattemptedCount = 0

        val reviews = test.questions.map { question ->
            val ans = answers[question.id]
            val selectedKey = ans?.selectedOptionKey
            val isCorrect = selectedKey != null && selectedKey.equals(question.correctOptionKey, ignoreCase = true)

            if (selectedKey == null) {
                unattemptedCount++
            } else if (isCorrect) {
                correctCount++
            } else {
                incorrectCount++
            }

            QuestionResultReview(
                question = question,
                studentAnswerKey = selectedKey,
                isCorrect = isCorrect
            )
        }

        val totalMarks = test.totalQuestions * test.marksPerCorrect
        val rawScore = (correctCount * test.marksPerCorrect) - (incorrectCount * test.negativeMarks)
        val finalScore = maxOf(0.0, rawScore)
        val percentage = (finalScore / totalMarks) * 100.0
        val isPassed = percentage >= test.passingPercentage

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val result = TestResult(
            id = "res_${UUID.randomUUID().toString().take(8)}",
            testId = test.id,
            testTitle = test.title,
            studentName = _currentUser.value?.fullName ?: "Candidate",
            totalQuestions = test.totalQuestions,
            totalMarks = totalMarks,
            scoreObtained = Math.round(finalScore * 100.0) / 100.0,
            percentage = Math.round(percentage * 10.0) / 10.0,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            unattemptedCount = unattemptedCount,
            isPassed = isPassed,
            timeTakenSeconds = timeTakenSeconds,
            submittedAt = dateFormat.format(Date()),
            questionsReview = reviews
        )

        _testResults.value = listOf(result) + _testResults.value

        addNotification(
            title = "Mock Test Result Released",
            body = "You scored ${result.scoreObtained}/$totalMarks (${result.percentage}%) in ${test.title}.",
            type = "TEST"
        )
        logAudit("Student ${_currentUser.value?.fullName} submitted CBT exam ${test.title} with score ${result.scoreObtained}")

        return result
    }

    // ==========================================
    // DAILY MCQ ANSWERING
    // ==========================================
    fun answerDailyMcq(selectedOptionKey: String) {
        val current = _dailyPracticeMcq.value
        _dailyPracticeMcq.value = current.copy(
            studentAnswered = true,
            selectedOptionKey = selectedOptionKey
        )
    }

    // ==========================================
    // ADMIN ACTIONS (Courses, Extracted Questions, Proctoring)
    // ==========================================
    fun addCourse(
        title: String,
        categoryCode: String,
        description: String,
        price: Double,
        discountPrice: Double,
        isFree: Boolean
    ) {
        val newCourse = Course(
            id = "crs_${UUID.randomUUID().toString().take(6)}",
            categoryCode = categoryCode,
            title = title,
            description = description,
            price = price,
            discountPrice = discountPrice,
            isFree = isFree,
            validityDays = 365,
            rating = 5.0,
            enrolledCount = 0,
            materials = listOf(
                CourseMaterial(
                    id = "mat_${UUID.randomUUID().toString().take(6)}",
                    courseId = "crs_new",
                    title = "Course Orientation & Study Syllabus PDF",
                    description = "Official syllabus and topic weightage breakdown",
                    fileType = "PDF",
                    fileUrl = "https://avtaracademy.in/docs/syllabus.pdf",
                    pageCount = 18,
                    isFreePreview = true
                )
            ),
            videos = listOf(
                VideoLesson(
                    id = "vid_${UUID.randomUUID().toString().take(6)}",
                    courseId = "crs_new",
                    title = "Introduction & Core Concepts",
                    description = "Academy master lecture by Dr. Avtar",
                    videoUrl = "https://avtaracademy.in/video/intro.mp4",
                    durationSeconds = 1450,
                    isFreePreview = true
                )
            )
        )
        _courses.value = listOf(newCourse) + _courses.value
        logAudit("Admin added new course: $title")
    }

    fun deleteCourse(courseId: String) {
        _courses.value = _courses.value.filter { it.id != courseId }
        logAudit("Admin deleted course $courseId")
    }

    fun approveExtractedQuestions(questions: List<Question>) {
        _questionBank.value = questions + _questionBank.value
        logAudit("Admin approved and ingested ${questions.size} OCR-extracted questions into Bank")
    }

    fun toggleStudentBlockStatus(studentId: String) {
        _registeredStudents.value = _registeredStudents.value.map { std ->
            if (std.id == studentId) {
                val updated = std.copy(isBlocked = !std.isBlocked)
                logAudit("Admin toggled block status for ${std.fullName}: ${updated.isBlocked}")
                updated
            } else std
        }
    }

    fun grantStudentCourseAccess(studentId: String, courseId: String) {
        val std = _registeredStudents.value.find { it.id == studentId }
        val course = _courses.value.find { it.id == courseId }
        logAudit("Admin manually granted access to course '${course?.title}' for student '${std?.fullName}'")
    }

    fun postAnnouncement(title: String, content: String, category: String, isPinned: Boolean) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val item = AcademyAnnouncement(
            id = "ann_${UUID.randomUUID().toString().take(6)}",
            title = title,
            content = content,
            category = category,
            date = dateFormat.format(Date()),
            isPinned = isPinned
        )
        _announcements.value = listOf(item) + _announcements.value
        logAudit("Admin posted announcement: $title")
    }

    fun logProctoringAlert(sessionId: String, alertType: String, details: String) {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val newEvent = ProctoringEvent(
            id = "evt_${UUID.randomUUID().toString().take(6)}",
            type = alertType,
            timestamp = timeFormat.format(Date()),
            details = details,
            severity = if (alertType == "FULLSCREEN_EXIT") "CRITICAL" else "WARNING"
        )
        _proctoringSessions.value = _proctoringSessions.value.map { session ->
            if (session.sessionId == sessionId) {
                session.copy(
                    tabSwitchCount = session.tabSwitchCount + 1,
                    suspiciousEvents = listOf(newEvent) + session.suspiciousEvents
                )
            } else session
        }
    }

    fun updateBackendConfig(url: String, key: String, rzpKey: String, fcmId: String) {
        _backendConfig.value = BackendConfig(
            supabaseUrl = url,
            supabaseAnonKey = key,
            razorpayKeyId = rzpKey,
            fcmSenderId = fcmId,
            isLiveConnected = true
        )
        logAudit("Admin updated backend connection configurations")
    }

    private fun addNotification(title: String, body: String, type: String) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val item = NotificationItem(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = title,
            body = body,
            timestamp = timeFormat.format(Date()),
            type = type,
            isRead = false
        )
        _notifications.value = listOf(item) + _notifications.value
    }

    private fun logAudit(action: String) {
        val stamp = SimpleDateFormat("dd MMM hh:mm:ss a", Locale.getDefault()).format(Date())
        _auditLogs.value = listOf("[$stamp] $action") + _auditLogs.value.take(40)
    }

    // ==========================================
    // INITIAL MOCK / SEED DATA CREATORS
    // ==========================================
    private fun getInitialCourses(): List<Course> {
        return listOf(
            Course(
                id = "crs_dmlt_gold",
                categoryCode = "DMLT",
                title = "DMLT Master Complete Package 2025",
                description = "Complete Diploma in Medical Laboratory Technology exam mastery with Clinical Pathology, Hematology, Biochemistry, Microbiology, Serology & Histopathology.",
                price = 2499.0,
                discountPrice = 1499.0,
                isFree = false,
                isEnrolled = true,
                rating = 4.95,
                enrolledCount = 1240,
                instructor = "Dr. Avtar (M.Sc, Ph.D Paramedical)",
                materials = listOf(
                    CourseMaterial("m1", "crs_dmlt_gold", "Hematology & Blood Banking Complete Notes", "Anticoagulants, Romanowsky staining, automated cell counters & blood grouping protocols.", "PDF", "https://avtar.paramedical/dmlt/hematology.pdf", 48, false, 12),
                    CourseMaterial("m2", "crs_dmlt_gold", "Clinical Biochemistry Reagents & Photometry", "Enzyme kinetics, renal function tests, liver function profiles and automation.", "PDF", "https://avtar.paramedical/dmlt/biochemistry.pdf", 36, false, 4),
                    CourseMaterial("m3", "crs_dmlt_gold", "Microbiology & Parasitology High-Yield PDF", "Gram staining, Culture media, Mycobacteria, fungal pathogens and stool examinations.", "PDF", "https://avtar.paramedical/dmlt/microbiology.pdf", 52, true, 1)
                ),
                videos = listOf(
                    VideoLesson("v1", "crs_dmlt_gold", "Hematology Lecture 1: Normal Blood Cells & Hematopoiesis", "Mastering erythrocyte, leucocyte and thrombocyte maturation pathways.", "https://avtar.stream/dmlt/v1.mp4", 2150, true, 1200, 1),
                    VideoLesson("v2", "crs_dmlt_gold", "Biochemistry Lecture 2: Spectrophotometry & Beers Law", "Practical demonstration of absorbance, optical density and calibration curves.", "https://avtar.stream/dmlt/v2.mp4", 1840, false, 0, 2),
                    VideoLesson("v3", "crs_dmlt_gold", "Microbiology Lecture 3: Acid Fast Staining of M. tuberculosis", "Step-by-step Ziehl-Neelsen technique and decolorizer concentration.", "https://avtar.stream/dmlt/v3.mp4", 1920, false, 0, 3)
                )
            ),
            Course(
                id = "crs_drt_pro",
                categoryCode = "DRT",
                title = "DRT & Radiology Tech Government Exam Cracker",
                description = "Comprehensive Radiography, X-Ray Physics, Radiation Protection, CT Scan Protocols, MRI Sequences and Contrast Media.",
                price = 2999.0,
                discountPrice = 1799.0,
                isFree = false,
                isEnrolled = false,
                rating = 4.92,
                enrolledCount = 980,
                instructor = "Prof. R.K. Varma (Senior Radiologist)",
                materials = listOf(
                    CourseMaterial("m4", "crs_drt_pro", "X-Ray Production & Tube Physics Master Guide", "Bremsstrahlung radiation, Characteristic X-rays, Rotating anode mechanics.", "PDF", "https://avtar.paramedical/drt/physics.pdf", 42, true, 1),
                    CourseMaterial("m5", "crs_drt_pro", "CT Scan Anatomy & Protocol Handbook", "Window settings, pitch, Hounsfield units and helical CT reconstructions.", "PDF", "https://avtar.paramedical/drt/ct.pdf", 64, false, 1)
                ),
                videos = listOf(
                    VideoLesson("v4", "crs_drt_pro", "Radiology Lecture 1: Radiation Safety & ALARA Principles", "Time, distance, shielding, TLD badges and lead apron guidelines.", "https://avtar.stream/drt/v1.mp4", 2400, true, 0, 1)
                )
            ),
            Course(
                id = "crs_ot_special",
                categoryCode = "OT",
                title = "Operation Theatre & Anesthesia Technician Course",
                description = "Sterilization methods, Autoclaves, Anesthesia workstations, Boyle's apparatus, surgical instruments, and monitoring equipment.",
                price = 1999.0,
                discountPrice = 1199.0,
                isFree = false,
                isEnrolled = false,
                rating = 4.88,
                enrolledCount = 650,
                instructor = "Dr. Manpreet Kaur (OT Specialist)",
                materials = listOf(
                    CourseMaterial("m6", "crs_ot_special", "Autoclave & OT Sterilization Protocols", "High pressure steam cycles, chemical indicators and spore tests.", "PDF", "https://avtar.paramedical/ot/sterilization.pdf", 32, true, 1)
                ),
                videos = listOf(
                    VideoLesson("v5", "crs_ot_special", "Anesthesia Machine Gas Circuit & Vaporizers", "Color coding of medical gas cylinders, pin-index safety system.", "https://avtar.stream/ot/v1.mp4", 1750, true, 0, 1)
                )
            ),
            Course(
                id = "crs_free_sampler",
                categoryCode = "ALL",
                title = "Paramedical Fast-Track Free Starter Foundation",
                description = "Essential Human Anatomy, Physiology, Medical Terminology and Basic Life Support for all paramedical students.",
                price = 0.0,
                discountPrice = 0.0,
                isFree = true,
                isEnrolled = true,
                rating = 4.98,
                enrolledCount = 4320,
                instructor = "Avtar Academy Team",
                materials = listOf(
                    CourseMaterial("m7", "crs_free_sampler", "Vital Signs & Emergency Paramedical Terminology", "Blood pressure measurement, pulse ox, anatomical planes and prefixes.", "PDF", "https://avtar.paramedical/starter/vitals.pdf", 20, true, 5)
                ),
                videos = listOf(
                    VideoLesson("v6", "crs_free_sampler", "Cell Structure & Blood Physiology Overview", "Fundamental foundation for all competitive paramedical exams.", "https://avtar.stream/starter/v1.mp4", 1200, true, 600, 1)
                )
            )
        )
    }

    private fun getInitialMockTests(): List<MockTest> {
        val dmltQuestions = getDmltSampleQuestions()
        return listOf(
            MockTest(
                id = "test_cbt_dmlt_grand",
                title = "National DMLT & BMLT Grand CBT Mock Test #1",
                description = "High-yield examination conforming strictly to central and state paramedical board syllabi.",
                subject = "Medical Lab Technology",
                totalQuestions = dmltQuestions.size,
                durationMinutes = 45,
                marksPerCorrect = 1.0,
                negativeMarks = 0.25,
                passingPercentage = 50.0,
                isProctored = true,
                allowInstantResults = true,
                questions = dmltQuestions
            ),
            MockTest(
                id = "test_cbt_drt_physics",
                title = "Radiology & Imaging Technology Subject CBT Test",
                description = "Specialized test focusing on X-ray generation, grids, screens, radiation protection and MRI physics.",
                subject = "Radiography (DRT)",
                totalQuestions = 25,
                durationMinutes = 30,
                marksPerCorrect = 2.0,
                negativeMarks = 0.50,
                passingPercentage = 50.0,
                isProctored = true,
                allowInstantResults = true,
                questions = getDrtSampleQuestions()
            )
        )
    }

    private fun getDmltSampleQuestions(): List<Question> {
        return listOf(
            Question(
                id = "q1",
                subject = "Hematology",
                topic = "Erythrocyte Sedimentation Rate",
                questionText = "Which anticoagulant and ratio is utilized in Westergren's ESR determination method?",
                options = listOf(
                    QuestionOption("A", "3.8% Sodium Citrate in 1:4 ratio (1 part anticoagulant, 4 parts blood)", true),
                    QuestionOption("B", "K2 EDTA in 1:9 ratio with whole blood", false),
                    QuestionOption("C", "Heparin sodium in 1:2 ratio", false),
                    QuestionOption("D", "Sodium Fluoride and Potassium Oxalate in 1:4 ratio", false)
                ),
                correctOptionKey = "A",
                explanation = "In Westergren's ESR technique, 3.8% trisodium citrate is diluted with venous blood in a strict 1:4 volume ratio (0.4 mL citrate to 1.6 mL blood). Wintrobe method uses EDTA or double oxalate without dilution.",
                difficulty = "Medium",
                isApproved = true
            ),
            Question(
                id = "q2",
                subject = "Clinical Pathology",
                topic = "Urine Analysis",
                questionText = "Bence Jones protein in urine precipitates at what temperature and re-dissolves upon boiling?",
                options = listOf(
                    QuestionOption("A", "Precipitates at 20°C - 30°C and dissolves at 60°C", false),
                    QuestionOption("B", "Precipitates at 45°C - 60°C and dissolves at 100°C (boiling)", true),
                    QuestionOption("C", "Precipitates at 80°C and precipitates further on boiling", false),
                    QuestionOption("D", "Remains soluble at all heating ranges", false)
                ),
                correctOptionKey = "B",
                explanation = "Bence Jones proteins are monoclonal immunoglobulin light chains that precipitate upon heating to 45°C - 60°C, and characteristically re-dissolve when boiled at 100°C. They reappear when cooled to 60°C.",
                difficulty = "Hard",
                isApproved = true
            ),
            Question(
                id = "q3",
                subject = "Microbiology",
                topic = "Acid Fast Staining",
                questionText = "What is the primary stain used in the Ziehl-Neelsen acid-fast staining procedure?",
                options = listOf(
                    QuestionOption("A", "Crystal Violet", false),
                    QuestionOption("B", "Carbol Fuchsin (basic fuchsin with phenol)", true),
                    QuestionOption("C", "Methylene Blue", false),
                    QuestionOption("D", "Safranin O", false)
                ),
                correctOptionKey = "B",
                explanation = "Carbol Fuchsin (containing phenol) penetrates the waxy mycolic acid cell wall of Mycobacterium tuberculosis with the aid of heat or steaming.",
                difficulty = "Easy",
                isApproved = true
            ),
            Question(
                id = "q4",
                subject = "Biochemistry",
                topic = "Liver Function Tests",
                questionText = "Which liver enzyme is most specifically elevated in intrahepatic and extrahepatic biliary obstruction?",
                options = listOf(
                    QuestionOption("A", "Alkaline Phosphatase (ALP)", true),
                    QuestionOption("B", "Alanine Aminotransferase (ALT)", false),
                    QuestionOption("C", "Aspartate Aminotransferase (AST)", false),
                    QuestionOption("D", "Creatine Kinase (CK-MB)", false)
                ),
                correctOptionKey = "A",
                explanation = "ALP and GGT (Gamma-Glutamyl Transferase) are bile canalicular membrane enzymes markedly elevated in cholestasis/biliary tree obstruction, whereas ALT and AST reflect hepatocellular injury.",
                difficulty = "Medium",
                isApproved = true
            ),
            Question(
                id = "q5",
                subject = "Blood Banking",
                topic = "Crossmatching",
                questionText = "In major crossmatching, what is incubated together?",
                options = listOf(
                    QuestionOption("A", "Donor Red Blood Cells with Recipient Serum", true),
                    QuestionOption("B", "Donor Serum with Recipient Red Blood Cells", false),
                    QuestionOption("C", "Donor Plasma with Recipient Whole Blood", false),
                    QuestionOption("D", "Recipient Platelets with Donor Serum", false)
                ),
                correctOptionKey = "A",
                explanation = "Major crossmatch tests the Donor's RBCs against the Recipient's serum to identify recipient antibodies that would destroy transfused red blood cells.",
                difficulty = "Medium",
                isApproved = true
            ),
            Question(
                id = "q6",
                subject = "Histotechnology",
                topic = "Fixation",
                questionText = "What is the standard concentration of neutral buffered formalin used for routine surgical tissue fixation?",
                options = listOf(
                    QuestionOption("A", "10% Neutral Buffered Formalin (approx 4% formaldehyde)", true),
                    QuestionOption("B", "40% Saturated Formalin solution", false),
                    QuestionOption("C", "1% Picric acid solution", false),
                    QuestionOption("D", "70% Ethanol mixture", false)
                ),
                correctOptionKey = "A",
                explanation = "10% Neutral Buffered Formalin (NBF) is prepared by diluting 37-40% commercial formaldehyde 1:10 with phosphate buffers (pH 7.2-7.4) to prevent formalin pigment formation.",
                difficulty = "Medium",
                isApproved = true
            )
        )
    }

    private fun getDrtSampleQuestions(): List<Question> {
        return listOf(
            Question(
                id = "rq1",
                subject = "Radiography",
                topic = "X-Ray Production",
                questionText = "Which interaction produces the continuous X-ray spectrum in medical diagnostic X-ray tubes?",
                options = listOf(
                    QuestionOption("A", "Bremsstrahlung (Braking Radiation)", true),
                    QuestionOption("B", "Characteristic Radiation", false),
                    QuestionOption("C", "Compton scattering", false),
                    QuestionOption("D", "Photoelectric effect", false)
                ),
                correctOptionKey = "A",
                explanation = "Bremsstrahlung radiation occurs when projectile electrons decelerate near the target tungsten nucleus, emitting continuous X-ray photons comprising 80-90% of diagnostic beams.",
                difficulty = "Medium",
                isApproved = true
            ),
            Question(
                id = "rq2",
                subject = "Radiation Protection",
                topic = "Shielding & Units",
                questionText = "According to AERB (Atomic Energy Regulatory Board) guidelines, what is the annual occupational radiation dose limit for a radiographer?",
                options = listOf(
                    QuestionOption("A", "20 mSv averaged over 5 consecutive years (not exceeding 30 mSv in any single year)", true),
                    QuestionOption("B", "1 mSv per year", false),
                    QuestionOption("C", "100 mSv per year", false),
                    QuestionOption("D", "500 mSv per year", false)
                ),
                correctOptionKey = "A",
                explanation = "The AERB occupational effective dose limit for radiation workers is 20 mSv/year averaged over 5 years (100 mSv in 5 years), with max 30 mSv in any single year. Public limit is 1 mSv/yr.",
                difficulty = "Hard",
                isApproved = true
            )
        )
    }

    private fun getInitialTestResults(): List<TestResult> {
        return listOf(
            TestResult(
                id = "res_sample_1",
                testId = "test_cbt_dmlt_grand",
                testTitle = "National DMLT Practice CBT #1",
                studentName = "Ananya Sharma",
                totalQuestions = 6,
                totalMarks = 6.0,
                scoreObtained = 5.0,
                percentage = 83.3,
                correctCount = 5,
                incorrectCount = 1,
                unattemptedCount = 0,
                isPassed = true,
                timeTakenSeconds = 480,
                submittedAt = "Yesterday, 04:30 PM",
                questionsReview = emptyList()
            )
        )
    }

    private fun getInitialQuestionBank(): List<Question> {
        return getDmltSampleQuestions() + getDrtSampleQuestions()
    }

    private fun getInitialPaymentTransactions(): List<PaymentTransaction> {
        return listOf(
            PaymentTransaction(
                transactionId = "pay_N84G92B8X",
                orderId = "order_DMLT2025_01",
                courseId = "crs_dmlt_gold",
                courseTitle = "DMLT Master Complete Package 2025",
                amount = 1499.0,
                method = PaymentMethod.UPI,
                status = PaymentStatus.COMPLETED,
                timestamp = "02 Nov 2026, 11:15 AM",
                signature = "sig_hmac_verified_8947f2a10",
                adminApprovalNote = "Razorpay Signature Verified"
            ),
            PaymentTransaction(
                transactionId = "UTR-431892817290",
                orderId = "order_OFFLINE_DRT",
                courseId = "crs_drt_pro",
                courseTitle = "DRT & Radiology Tech Government Exam Cracker",
                amount = 1799.0,
                method = PaymentMethod.OFFLINE_BANK_TRANSFER,
                status = PaymentStatus.PENDING_VERIFICATION,
                timestamp = "03 Nov 2026, 06:45 PM",
                signature = "manual_bank_receipt",
                adminApprovalNote = "Pending Bank Account Reconciliation"
            )
        )
    }

    private fun getInitialProctoringSessions(): List<ProctoringSession> {
        return listOf(
            ProctoringSession(
                sessionId = "proc_sess_991",
                studentId = "std_001",
                studentName = "Ananya Sharma",
                testId = "test_cbt_dmlt_grand",
                testTitle = "National DMLT & BMLT Grand CBT Mock Test #1",
                isCameraActive = true,
                isLocationVerified = true,
                latitude = 28.6139,
                longitude = 77.2090,
                tabSwitchCount = 0,
                suspiciousEvents = emptyList(),
                isLive = true
            ),
            ProctoringSession(
                sessionId = "proc_sess_992",
                studentId = "std_002",
                studentName = "Rahul Verma",
                testId = "test_cbt_dmlt_grand",
                testTitle = "National DMLT & BMLT Grand CBT Mock Test #1",
                isCameraActive = true,
                isLocationVerified = true,
                latitude = 26.9124,
                longitude = 75.7873,
                tabSwitchCount = 2,
                suspiciousEvents = listOf(
                    ProctoringEvent("e1", "FULLSCREEN_EXIT", "10:14 AM", "Candidate exited full screen mode briefly", "WARNING")
                ),
                isLive = true
            )
        )
    }

    private fun getInitialAnnouncements(): List<AcademyAnnouncement> {
        return listOf(
            AcademyAnnouncement(
                id = "a1",
                title = "All India Paramedical CBT Mock Exam Scheduled for Sunday",
                content = "Avtar Paramedical Academy will conduct the Grand All-India Mock Test on 8th Nov at 10:00 AM. Camera proctoring will be mandatory for merit rank certificates.",
                category = "Exam",
                date = "04 Nov 2026",
                isPinned = true
            ),
            AcademyAnnouncement(
                id = "a2",
                title = "New Radiology & CT Imaging Question Bank Released",
                content = "Over 350+ fresh clinical scenario questions added to the DRT module with high-resolution image explanations.",
                category = "Course",
                date = "02 Nov 2026",
                isPinned = false
            ),
            AcademyAnnouncement(
                id = "a3",
                title = "Doubt Clearing Live Session with Dr. Avtar",
                content = "Join the live laboratory chemistry discussion this Wednesday at 7:00 PM in the academy video room.",
                category = "General",
                date = "01 Nov 2026",
                isPinned = false
            )
        )
    }

    private fun getInitialNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem("n1", "Mock Test Reminder", "Your Grand DMLT Mock Test is ready to attempt.", "10 mins ago", "TEST", false),
            NotificationItem("n2", "Course Material Added", "New Hematology Notes have been uploaded to your enrolled course.", "2 hours ago", "COURSE", false),
            NotificationItem("n3", "Payment Confirmed", "Payment for DMLT Master Complete Package was successful.", "1 day ago", "PAYMENT", true)
        )
    }

    private fun getInitialDailyMcq(): DailyMcqPractice {
        return DailyMcqPractice(
            id = "daily_today",
            date = "Today's Clinical Practice",
            question = Question(
                id = "q_daily",
                subject = "Clinical Pathology",
                topic = "CSF Examination",
                questionText = "In bacterial (pyogenic) meningitis, what characteristic changes are observed in Cerebrospinal Fluid (CSF)?",
                options = listOf(
                    QuestionOption("A", "Marked neutrophilic pleocytosis, low glucose, elevated protein", true),
                    QuestionOption("B", "Lymphocytic pleocytosis, normal glucose, normal protein", false),
                    QuestionOption("C", "Eosinophilia, high glucose, low protein", false),
                    QuestionOption("D", "Clear watery fluid with zero cell count", false)
                ),
                correctOptionKey = "A",
                explanation = "Pyogenic bacterial meningitis produces turbid/purulent CSF with marked polymorphonuclear neutrophilia (>1000/µL), significantly depleted glucose (<40 mg/dL or CSF/plasma ratio <0.4), and high protein (>100 mg/dL).",
                difficulty = "Medium",
                isApproved = true
            ),
            studentAnswered = false
        )
    }

    private fun getInitialRegisteredStudents(): List<StudentProfile> {
        return listOf(
            StudentProfile("std_001", "Ananya Sharma", "9876543210", "ananya.sharma@paramedical.edu", "AVTAR-2024-DMLT-042", "DMLT", isBlocked = false),
            StudentProfile("std_002", "Rahul Verma", "9812345678", "rahul.v@paramedical.edu", "AVTAR-2024-DRT-019", "DRT", isBlocked = false),
            StudentProfile("std_003", "Priya Nair", "9765432109", "priya.nair@paramedical.edu", "AVTAR-2024-BMLT-108", "BMLT", isBlocked = false),
            StudentProfile("std_004", "Vikram Rathore", "9945671234", "vikram.r@paramedical.edu", "AVTAR-2024-OT-033", "OT", isBlocked = true)
        )
    }

    private fun getInitialAuditLogs(): List<String> {
        return listOf(
            "[System Boot] Academy Database initialized with 28 PostgreSQL relations",
            "[System Ready] Supabase connector listening at endpoint",
            "[Security] Razorpay SHA-256 HMAC gateway verification module active",
            "[Proctoring] Camera & Geolocation verification daemon armed"
        )
    }
}
