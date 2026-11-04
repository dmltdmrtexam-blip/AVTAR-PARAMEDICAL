package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Course
import com.example.data.model.MockTest
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AcademyViewModel,
    onNavigateToCourses: () -> Unit,
    onNavigateToCourseDetail: (Course) -> Unit,
    onStartExam: (MockTest) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val mockTests by viewModel.mockTests.collectAsState()
    val dailyMcq by viewModel.dailyPracticeMcq.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showNotificationsSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_academy_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AVTAR PARAMEDICAL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "ACADEMY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldDark
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Demo Admin Switcher Button
                    FilledTonalButton(
                        onClick = { viewModel.toggleRoleForDemo() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (currentUser?.isAdmin == true) GoldAccent else NavyLight,
                            contentColor = if (currentUser?.isAdmin == true) NavyDark else PureWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("role_switcher_button")
                    ) {
                        Icon(
                            imageVector = if (currentUser?.isAdmin == true) Icons.Default.AdminPanelSettings else Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentUser?.isAdmin == true) "Admin" else "Student",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Notifications Icon with badge
                    IconButton(
                        onClick = { showNotificationsSheet = true },
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                val unread = notifications.count { !it.isRead }
                                if (unread > 0) {
                                    Badge(containerColor = ErrorRed) { Text("$unread") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = NavyPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Welcome Card Header
            item {
                WelcomeStudentBanner(
                    studentName = currentUser?.fullName ?: "Candidate",
                    rollNumber = currentUser?.rollNumber ?: "AVTAR-2024",
                    stream = currentUser?.stream ?: "DMLT",
                    streakDays = currentUser?.streakDays ?: 7
                )
            }

            // 2. Paramedical Hero Visual Graphic
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.banner_paramedical_hero),
                            contentDescription = "Paramedical Clinical & Imaging Center",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            NavyDark.copy(alpha = 0.85f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column(modifier = Modifier.widthIn(max = 240.dp)) {
                                Text(
                                    text = "Government & Board CBT Exam Series 2025",
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Lab, Radiology & OT Master Prep",
                                    color = PureWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onNavigateToCourses,
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Explore Courses", color = NavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Quick Stats Grid
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "Enrolled",
                        value = "${courses.count { it.isEnrolled }} Courses",
                        icon = Icons.Default.Book,
                        tint = MedicalTeal,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Practice",
                        value = "5/6 Solved",
                        icon = Icons.Default.CheckCircle,
                        tint = MedicalBlue,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Accuracy",
                        value = "83.3%",
                        icon = Icons.Default.TrendingUp,
                        tint = GoldDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Daily Practice MCQ Widget
            item {
                DailyMcqCard(
                    dailyMcq = dailyMcq,
                    onOptionSelected = { key -> viewModel.answerDailyPracticeMcq(key) }
                )
            }

            // 5. Active & Upcoming CBT Mock Tests
            item {
                SectionHeader(
                    title = "Active CBT Mock Tests",
                    actionText = "All Tests",
                    onActionClick = onNavigateToAnalytics
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mockTests) { test ->
                        CbtMockTestCard(
                            test = test,
                            onStart = { onStartExam(test) }
                        )
                    }
                }
            }

            // 6. Enrolled & Available Courses
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(
                    title = "Your Study Courses",
                    actionText = "Browse All",
                    onActionClick = onNavigateToCourses
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    courses.take(3).forEach { course ->
                        CourseDashboardRow(
                            course = course,
                            onClick = { onNavigateToCourseDetail(course) }
                        )
                    }
                }
            }

            // 7. Recent Academy Announcements
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "Academy Announcements",
                    actionText = null,
                    onActionClick = {}
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    announcements.forEach { announcement ->
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
                                    Surface(
                                        color = if (announcement.category == "Exam") ErrorRedLight else MedicalBlueLight,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = announcement.category.uppercase(),
                                            color = if (announcement.category == "Exam") ErrorRed else MedicalBlue,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = announcement.date,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = announcement.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = announcement.content,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 8. Help & Support Footer Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Academy Doubt & Technical Support",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "WhatsApp Faculty & Call Helpline active 9AM - 8PM",
                                color = GoldLight.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Notifications Bottom Sheet
    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsSheet = false },
            containerColor = PureWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Academy Notifications",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                )
                Spacer(modifier = Modifier.height(14.dp))

                notifications.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (item.type) {
                                    "COURSE" -> Icons.Default.Book
                                    "TEST" -> Icons.Default.Assignment
                                    "PAYMENT" -> Icons.Default.Payment
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(item.body, fontSize = 12.sp, color = TextSecondary)
                                Text(item.timestamp, fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun WelcomeStudentBanner(
    studentName: String,
    rollNumber: String,
    stream: String,
    streakDays: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GoldAccent)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentName.take(1).uppercase(),
                    color = NavyDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome back,",
                    color = GoldLight.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Text(
                    text = studentName,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stream,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(" • ", color = GoldLight)
                    Text(
                        text = rollNumber,
                        color = PureWhite.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            // Streak Badge
            Surface(
                color = WarningOrange.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streakDays Days",
                        color = WarningOrange,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = NavyPrimary)
            Text(title, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun DailyMcqCard(
    dailyMcq: com.example.data.model.DailyMcqPractice,
    onOptionSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MedicalTeal, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DAILY CLINICAL MCQ", fontWeight = FontWeight.Bold, color = MedicalTeal, fontSize = 11.sp)
                }
                Surface(
                    color = GoldContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+4 Points",
                        color = GoldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dailyMcq.question.questionText,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = NavyPrimary)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Options
            dailyMcq.question.options.forEach { opt ->
                val isSelected = dailyMcq.selectedOptionKey == opt.key
                val isCorrect = opt.isCorrect
                val showFeedback = dailyMcq.studentAnswered

                val containerColor = when {
                    showFeedback && isCorrect -> SuccessGreenLight
                    showFeedback && isSelected && !isCorrect -> ErrorRedLight
                    isSelected -> GoldContainer
                    else -> SlateBackground
                }

                val borderColor = when {
                    showFeedback && isCorrect -> SuccessGreen
                    showFeedback && isSelected && !isCorrect -> ErrorRed
                    isSelected -> GoldDark
                    else -> Color.Transparent
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .clickable(enabled = !dailyMcq.studentAnswered) { onOptionSelected(opt.key) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "(${opt.key})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (showFeedback && isCorrect) SuccessGreen else NavyPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = opt.text,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (showFeedback && isCorrect) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    } else if (showFeedback && isSelected && !isCorrect) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            AnimatedVisibility(visible = dailyMcq.studentAnswered) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MedicalTealLight.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Clinical Explanation:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF134E4A),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dailyMcq.question.explanation,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CbtMockTestCard(
    test: MockTest,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onStart() },
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = NavyLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "CBT ENGINE",
                        color = GoldAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (test.isProctored) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Proctored", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = test.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NavyPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "⏱ ${test.durationMinutes} Mins",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "📝 ${test.totalQuestions} Questions",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "⚖️ -${test.negativeMarks} Neg",
                    fontSize = 11.sp,
                    color = ErrorRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("start_exam_${test.id}")
            ) {
                Text("Start CBT Exam", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun CourseDashboardRow(
    course: Course,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (course.isEnrolled) Icons.Default.MenuBook else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (course.isEnrolled) GoldAccent else GoldLight,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NavyPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${course.materials.size} PDFs • ${course.videos.size} Videos • ${course.instructor}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = if (course.isEnrolled) SuccessGreenLight else GoldContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (course.isEnrolled) "Enrolled" else "₹${course.discountPrice.toInt()}",
                    color = if (course.isEnrolled) SuccessGreen else GoldDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String?,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
        )
        if (actionText != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MedicalBlue
                ),
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}
