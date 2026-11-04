package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AcademyViewModel,
    onNavigateToConfig: () -> Unit
) {
    var adminTab by remember { mutableStateOf(0) }

    val courses by viewModel.courses.collectAsState()
    val paymentHistory by viewModel.paymentHistory.collectAsState()
    val registeredStudents by viewModel.registeredStudents.collectAsState()
    val proctoringSessions by viewModel.proctoringSessions.collectAsState()
    val extractedQuestions by viewModel.extractedQuestions.collectAsState()
    val isExtracting by viewModel.isExtractingPdf.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var newCourseTitle by remember { mutableStateOf("") }
    var newCourseCat by remember { mutableStateOf("DMLT") }
    var newCoursePrice by remember { mutableStateOf("1999") }
    var newCourseDiscount by remember { mutableStateOf("999") }

    var customOcrText by remember { mutableStateOf("") }
    var showCustomOcrInput by remember { mutableStateOf(false) }

    val pendingPayments = paymentHistory.filter { it.status == PaymentStatus.PENDING_VERIFICATION }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Academy Admin Portal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            )
                            Text(
                                text = "Directorate of Operations & CBT Control",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToConfig,
                        modifier = Modifier.testTag("admin_config_icon")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Config", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(paddingValues)
        ) {
            // Admin KPI Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatCard(title = "Students", value = "${registeredStudents.size}", icon = Icons.Default.People, modifier = Modifier.weight(1f))
                AdminStatCard(title = "Courses", value = "${courses.size}", icon = Icons.Default.Book, modifier = Modifier.weight(1f))
                AdminStatCard(title = "Pending UTR", value = "${pendingPayments.size}", icon = Icons.Default.PendingActions, tint = WarningOrange, modifier = Modifier.weight(1f))
                AdminStatCard(title = "Live Exams", value = "${proctoringSessions.size}", icon = Icons.Default.Videocam, tint = ErrorRed, modifier = Modifier.weight(1f))
            }

            // Tab Navigation Row
            ScrollableTabRow(
                selectedTabIndex = adminTab,
                containerColor = PureWhite,
                contentColor = NavyPrimary,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "Courses (${courses.size})",
                    "PDF Extractor (${extractedQuestions.size})",
                    "Payments (${pendingPayments.size})",
                    "Students (${registeredStudents.size})",
                    "Live Proctoring",
                    "Audit Logs"
                ).forEachIndexed { index, tabName ->
                    Tab(
                        selected = adminTab == index,
                        onClick = { adminTab = index },
                        text = {
                            Text(
                                text = tabName,
                                fontWeight = if (adminTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TAB 0: COURSES MANAGER
            if (adminTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddCourseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("admin_add_course_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = PureWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create New Course", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }

                    items(courses) { course ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                    Text("${course.categoryCode} • ₹${course.discountPrice.toInt()} • ${course.enrolledCount} enrolled", fontSize = 11.sp, color = TextSecondary)
                                }
                                IconButton(
                                    onClick = { viewModel.deleteCourse(course.id) },
                                    modifier = Modifier.testTag("delete_course_${course.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: PDF QUESTION EXTRACTOR & OCR
            else if (adminTab == 1) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Automated MCQ & PDF Extraction Engine",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "Regex and NLP question parser with option/answer identification and duplicate detection.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.triggerSamplePdfExtraction() },
                                        enabled = !isExtracting,
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("extract_pdf_button")
                                    ) {
                                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isExtracting) "Extracting..." else "Sample Exam PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { showCustomOcrInput = !showCustomOcrInput },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Paste OCR Text", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                    }
                                }

                                AnimatedVisibility(visible = showCustomOcrInput) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        OutlinedTextField(
                                            value = customOcrText,
                                            onValueChange = { customOcrText = it },
                                            placeholder = { Text("Paste raw MCQ exam questions here...") },
                                            modifier = Modifier.fillMaxWidth().height(100.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                viewModel.extractCustomText(customOcrText)
                                                showCustomOcrInput = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Parse & Extract Questions", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (extractedQuestions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.approveAllExtractedQuestions("Medical Lab Tech", "Hematology & Staining") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("approve_all_extracted_button")
                                    ) {
                                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = PureWhite)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Approve & Ingest All (${extractedQuestions.size}) into Bank", color = PureWhite, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(extractedQuestions) { index, mcq ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Q${mcq.rawNumber}. Extracted Question", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                                    Surface(
                                        color = if (mcq.isDuplicateWarning) WarningOrangeLight else SuccessGreenLight,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (mcq.isDuplicateWarning) "Possible Duplicate" else "Confidence: ${(mcq.confidenceScore * 100).toInt()}%",
                                            color = if (mcq.isDuplicateWarning) WarningOrange else SuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(mcq.questionText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("(A) ${mcq.optionA}", fontSize = 11.sp, color = TextSecondary)
                                Text("(B) ${mcq.optionB}", fontSize = 11.sp, color = TextSecondary)
                                Text("(C) ${mcq.optionC}", fontSize = 11.sp, color = TextSecondary)
                                Text("(D) ${mcq.optionD}", fontSize = 11.sp, color = TextSecondary)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Detected Correct Answer: Option (${mcq.detectedAnswer})",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                if (mcq.detectedExplanation != null) {
                                    Text(
                                        text = "Exp: ${mcq.detectedExplanation}",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { viewModel.removeExtractedQuestion(index) }) {
                                        Text("Reject", color = ErrorRed, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: OFFLINE PAYMENTS APPROVAL
            else if (adminTab == 2) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (paymentHistory.isEmpty()) {
                        item {
                            Text("No transactions recorded yet.", color = TextSecondary, modifier = Modifier.padding(16.dp))
                        }
                    }

                    items(paymentHistory) { tx ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tx.transactionId, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                                    Surface(
                                        color = when (tx.status) {
                                            PaymentStatus.COMPLETED -> SuccessGreenLight
                                            PaymentStatus.PENDING_VERIFICATION -> WarningOrangeLight
                                            else -> ErrorRedLight
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = tx.status.name,
                                            color = when (tx.status) {
                                                PaymentStatus.COMPLETED -> SuccessGreen
                                                PaymentStatus.PENDING_VERIFICATION -> WarningOrange
                                                else -> ErrorRed
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(tx.courseTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                Text("Amount: ₹${tx.amount.toInt()} via ${tx.method.name} • ${tx.timestamp}", fontSize = 11.sp, color = TextSecondary)

                                if (tx.status == PaymentStatus.PENDING_VERIFICATION) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.approveOfflinePayment(tx.transactionId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Approve & Enroll", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.rejectOfflinePayment(tx.transactionId) },
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reject", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: REGISTERED STUDENTS
            else if (adminTab == 3) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(registeredStudents) { std ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (std.isBlocked) ErrorRedLight else MedicalBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(std.fullName.take(1), fontWeight = FontWeight.Bold, color = if (std.isBlocked) ErrorRed else MedicalBlue)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(std.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                    Text("${std.stream} • ${std.rollNumber} • +91 ${std.mobileNumber}", fontSize = 11.sp, color = TextSecondary)
                                }

                                TextButton(
                                    onClick = { viewModel.toggleStudentBlock(std.id) }
                                ) {
                                    Text(
                                        text = if (std.isBlocked) "Unblock" else "Block",
                                        color = if (std.isBlocked) SuccessGreen else ErrorRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 4: LIVE PROCTORING MONITOR
            else if (adminTab == 4) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(proctoringSessions) { session ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(session.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                    }

                                    Surface(
                                        color = if (session.tabSwitchCount > 0) ErrorRedLight else SuccessGreenLight,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${session.tabSwitchCount} Exit Alerts",
                                            color = if (session.tabSwitchCount > 0) ErrorRed else SuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Exam: ${session.testTitle}", fontSize = 11.sp, color = TextSecondary)
                                Text("Camera: Authorized • GPS Lat/Long: ${session.latitude}, ${session.longitude}", fontSize = 10.sp, color = MedicalTeal)

                                if (session.suspiciousEvents.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    session.suspiciousEvents.forEach { ev ->
                                        Text("⚠️ [${ev.timestamp}] ${ev.details}", fontSize = 10.sp, color = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 5: AUDIT LOGS
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(auditLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = log,
                                fontSize = 11.sp,
                                color = NavyPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Course Modal Dialog
    if (showAddCourseDialog) {
        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text("Create Academy Course", fontWeight = FontWeight.Bold, color = NavyPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCourseTitle,
                        onValueChange = { newCourseTitle = it },
                        label = { Text("Course Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCourseCat,
                        onValueChange = { newCourseCat = it },
                        label = { Text("Category Code (DMLT, DRT, BMLT, OT)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCoursePrice,
                            onValueChange = { newCoursePrice = it },
                            label = { Text("Original Price") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newCourseDiscount,
                            onValueChange = { newCourseDiscount = it },
                            label = { Text("Discounted Price") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCourseTitle.isNotBlank()) {
                            viewModel.addCourse(
                                title = newCourseTitle,
                                categoryCode = newCourseCat.uppercase(),
                                desc = "Full paramedical comprehensive video lecture series and mock examinations.",
                                price = newCoursePrice.toDoubleOrNull() ?: 1999.0,
                                discountPrice = newCourseDiscount.toDoubleOrNull() ?: 999.0,
                                isFree = false
                            )
                            showAddCourseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Create Course", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = NavyPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = NavyPrimary)
            Text(title, fontSize = 9.sp, color = TextSecondary)
        }
    }
}
