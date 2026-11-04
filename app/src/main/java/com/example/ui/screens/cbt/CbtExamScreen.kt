package com.example.ui.screens.cbt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockTest
import com.example.data.model.PaletteStatus
import com.example.data.model.TestResult
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbtExamScreen(
    test: MockTest,
    viewModel: AcademyViewModel,
    onSubmitComplete: (TestResult) -> Unit,
    onCancelExam: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val studentAnswers by viewModel.studentAnswers.collectAsState()
    val remainingSeconds by viewModel.examRemainingSeconds.collectAsState()
    val isProctorActive by viewModel.isExamProctoringActive.collectAsState()
    val warningCount by viewModel.fullscreenWarningCount.collectAsState()

    var showPaletteDrawer by remember { mutableStateOf(false) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }

    val currentQuestion = test.questions.getOrNull(currentIndex)
    val totalQuestions = test.questions.size

    val answeredCount = studentAnswers.values.count { it.selectedOptionKey != null && !it.isMarkedForReview }
    val markedReviewCount = studentAnswers.values.count { it.isMarkedForReview && it.selectedOptionKey == null }
    val answeredAndMarkedCount = studentAnswers.values.count { it.isMarkedForReview && it.selectedOptionKey != null }
    val notAnsweredCount = totalQuestions - (answeredCount + markedReviewCount + answeredAndMarkedCount)

    val currentAnswer = currentQuestion?.let { studentAnswers[it.id] }

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val isTimeLow = mins < 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = test.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            ),
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Candidate: ${currentUser?.rollNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Text(" • ", color = TextMuted)
                            Text(
                                text = "Q ${currentIndex + 1} of $totalQuestions",
                                style = MaterialTheme.typography.bodySmall.copy(color = MedicalTeal, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                },
                actions = {
                    // Timer Chip
                    Surface(
                        color = if (isTimeLow) ErrorRedLight else NavyLight,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isTimeLow) ErrorRed else GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                color = if (isTimeLow) ErrorRed else PureWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Question Palette Toggle Button
                    IconButton(
                        onClick = { showPaletteDrawer = true },
                        modifier = Modifier.testTag("toggle_palette_button")
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = "Palette", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        bottomBar = {
            // CBT Bottom Navigation Action Bar
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Button
                        OutlinedButton(
                            onClick = { viewModel.previousQuestion() },
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Previous", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Clear Response Button
                        TextButton(
                            onClick = { viewModel.clearExamResponse() },
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Clear", color = TextSecondary, fontSize = 11.sp)
                        }

                        // Mark for Review & Next Button
                        OutlinedButton(
                            onClick = { viewModel.markExamForReviewAndNext() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PaletteMarkedReview),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Review & Next", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Save & Next Button
                        Button(
                            onClick = { viewModel.saveAndNext() },
                            colors = ButtonDefaults.buttonColors(containerColor = PaletteAnswered),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp).testTag("save_and_next_button")
                        ) {
                            Text("Save & Next", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Final Submit Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showSubmitConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("final_submit_exam_button")
                        ) {
                            Text("Submit Exam & View Scorecard", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Live Proctoring Status Bar
                if (isProctorActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PROCTORING ACTIVE (Live Camera & GPS Verified)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalTeal
                                )
                            }

                            if (warningCount > 0) {
                                Surface(color = ErrorRedLight, shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = "$warningCount Warnings",
                                        color = ErrorRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (currentQuestion != null) {
                    // Question Statement Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = SlateBackground,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${currentQuestion.subject} • ${currentQuestion.topic}",
                                        color = NavyPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = "+${test.marksPerCorrect} / -${test.negativeMarks} Marks",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Question ${currentIndex + 1}:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GoldDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQuestion.questionText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = NavyPrimary,
                                    lineHeight = 22.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                            HorizontalDivider(color = SlateBackground)
                            Spacer(modifier = Modifier.height(12.dp))

                            // 4 MCQ Options
                            currentQuestion.options.forEach { opt ->
                                val isSelected = currentAnswer?.selectedOptionKey == opt.key

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) MedicalBlueLight else SlateBackground)
                                        .border(
                                            1.dp,
                                            if (isSelected) MedicalBlue else Color(0xFFE2E8F0),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.selectExamOption(opt.key) }
                                        .padding(12.dp)
                                        .testTag("option_${opt.key}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.selectExamOption(opt.key) },
                                        colors = RadioButtonDefaults.colors(selectedColor = MedicalBlue)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${opt.key})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MedicalBlue else NavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opt.text,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Question Palette Drawer Bottom Sheet
    if (showPaletteDrawer) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteDrawer = false },
            containerColor = PureWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Question Palette & Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Legend Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendItem(color = PaletteAnswered, label = "$answeredCount Answered", modifier = Modifier.weight(1f))
                    LegendItem(color = PaletteNotAnswered, label = "$notAnsweredCount Not Visited", modifier = Modifier.weight(1f))
                    LegendItem(color = PaletteMarkedReview, label = "$markedReviewCount Review", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    itemsIndexed(test.questions) { index, q ->
                        val status = viewModel.getQuestionPaletteStatus(q.id)
                        val color = when (status) {
                            PaletteStatus.ANSWERED -> PaletteAnswered
                            PaletteStatus.MARKED_FOR_REVIEW -> PaletteMarkedReview
                            PaletteStatus.ANSWERED_AND_MARKED_FOR_REVIEW -> PaletteAnsweredMarked
                            else -> PaletteNotAnswered
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .clickable {
                                    viewModel.selectQuestion(index)
                                    showPaletteDrawer = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Final Submit Confirmation Dialog
    if (showSubmitConfirmation) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            title = {
                Text("Confirm Final Submission", fontWeight = FontWeight.Bold, color = NavyPrimary)
            },
            text = {
                Column {
                    Text("Are you sure you want to finish and submit the test?")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("• Answered: $answeredCount", fontWeight = FontWeight.Bold, color = PaletteAnswered)
                    Text("• Marked for Review: ${markedReviewCount + answeredAndMarkedCount}", color = PaletteMarkedReview)
                    Text("• Unattempted: $notAnsweredCount", color = PaletteNotAnswered)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Once submitted, your responses cannot be altered. Detailed scorecard and medical explanations will be calculated immediately.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmation = false
                        val result = viewModel.submitCbtExam()
                        if (result != null) {
                            onSubmitComplete(result)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.testTag("confirm_submit_button")
                ) {
                    Text("Confirm & Submit", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmation = false }) {
                    Text("Resume Test", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String, modifier: Modifier = Modifier) {
    Surface(
        color = SlateBackground,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}
