package com.example.ui.screens.cbt

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.TestResult
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultScreen(
    result: TestResult,
    onBackToHome: () -> Unit
) {
    var expandedReviewIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Examination Scorecard",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home", tint = NavyPrimary)
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Score Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = if (result.isPassed) SuccessGreenLight else ErrorRedLight,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (result.isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (result.isPassed) SuccessGreen else ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (result.isPassed) "QUALIFIED / PASSED" else "NEEDS IMPROVEMENT",
                                    color = if (result.isPassed) SuccessGreen else ErrorRed,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${result.scoreObtained} / ${result.totalMarks}",
                            color = GoldAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 34.sp
                        )

                        Text(
                            text = "Score Achieved (${result.percentage}%)",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stats breakdown row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyLight)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ResultStatCol(label = "Correct", count = "${result.correctCount}", color = SuccessGreen)
                            ResultStatCol(label = "Incorrect", count = "${result.incorrectCount}", color = ErrorRed)
                            ResultStatCol(label = "Skipped", count = "${result.unattemptedCount}", color = TextMuted)
                            ResultStatCol(label = "Time", count = "${result.timeTakenSeconds / 60}m ${result.timeTakenSeconds % 60}s", color = GoldLight)
                        }
                    }
                }
            }

            // Section Header: Detailed Question-by-Question Medical Review
            item {
                Text(
                    text = "Question Solutions & Medical Explanations",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // List of questions with correct answer & explanation
            itemsIndexed(result.questionsReview) { index, review ->
                val isExpanded = expandedReviewIndex == index

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedReviewIndex = if (isExpanded) null else index
                        }
                        .testTag("review_question_$index"),
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
                            Text(
                                text = "Q${index + 1}. ${review.question.subject}",
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                fontSize = 12.sp
                            )

                            Surface(
                                color = when {
                                    review.isCorrect -> SuccessGreenLight
                                    review.studentAnswerKey != null -> ErrorRedLight
                                    else -> SlateBackground
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = when {
                                        review.isCorrect -> "Correct (+1.0)"
                                        review.studentAnswerKey != null -> "Incorrect (-0.25)"
                                        else -> "Unattempted"
                                    },
                                    color = when {
                                        review.isCorrect -> SuccessGreen
                                        review.studentAnswerKey != null -> ErrorRed
                                        else -> TextSecondary
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = review.question.questionText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Correct Answer: Option (${review.question.correctOptionKey})",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        if (review.studentAnswerKey != null && !review.isCorrect) {
                            Text(
                                text = "Your Answer: Option (${review.studentAnswerKey})",
                                color = ErrorRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }

                        // Clinical Explanation (Expandable)
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MedicalTealLight.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Academy Clinical Explanation:",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF134E4A),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = review.question.explanation,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onBackToHome,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Return to Student Dashboard", color = PureWhite, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ResultStatCol(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}
