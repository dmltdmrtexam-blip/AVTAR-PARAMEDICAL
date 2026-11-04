package com.example.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.data.model.SubjectScore
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AcademyViewModel
) {
    val results by viewModel.testResults.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val subjectScores = listOf(
        SubjectScore("Hematology & Blood Bank", 24, 21, 87),
        SubjectScore("Clinical Pathology & Urine", 18, 15, 83),
        SubjectScore("Radiography & X-Ray Physics", 20, 16, 80),
        SubjectScore("Microbiology & Parasitology", 16, 12, 75),
        SubjectScore("Clinical Biochemistry", 15, 11, 73),
        SubjectScore("Histotechnology", 12, 8, 66)
    )

    val leaderboard = listOf(
        LeaderboardEntry(1, "Deepak Joshi", "DMLT", 96.5, 14, "D"),
        LeaderboardEntry(2, "Ananya Sharma (You)", "DMLT", 94.0, 12, "A"),
        LeaderboardEntry(3, "Simran Walia", "DRT", 91.8, 15, "S"),
        LeaderboardEntry(4, "Kavita Mehra", "BMLT", 88.2, 10, "K"),
        LeaderboardEntry(5, "Arun Balakrishnan", "OT", 86.0, 11, "A")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Student Performance Analytics",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // High Level Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(title = "CBT Attempted", value = "${results.size + 4}", icon = Icons.Default.Assignment, tint = MedicalBlue, modifier = Modifier.weight(1f))
                    MetricCard(title = "Average Score", value = "84.2%", icon = Icons.Default.TrendingUp, tint = MedicalTeal, modifier = Modifier.weight(1f))
                    MetricCard(title = "Academy Rank", value = "#2 State", icon = Icons.Default.EmojiEvents, tint = GoldDark, modifier = Modifier.weight(1f))
                }
            }

            // Tab Selector: Subject Analysis vs Academy Leaderboard
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PureWhite,
                    contentColor = NavyPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Subject Breakdown", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("State Leaderboard", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Strong and Weak Subject Recommendation Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SuccessGreenLight),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("STRONG AREAS", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = SuccessGreen)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Hematology (87%)\nClinical Pathology (83%)", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = WarningOrangeLight),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("FOCUS NEEDED", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = WarningOrange)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Histotechnology (66%)\nBiochemistry (73%)", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Subject Accuracy Progress Bars
                items(subjectScores) { subject ->
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
                                Text(subject.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                Text("${subject.accuracyPercentage}%", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = if (subject.accuracyPercentage >= 80) SuccessGreen else MedicalBlue)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { subject.accuracyPercentage / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = if (subject.accuracyPercentage >= 80) SuccessGreen else MedicalBlue,
                                trackColor = SlateBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${subject.correctCount} correct out of ${subject.totalQuestions} questions attempted",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                // Academy State Leaderboard
                items(leaderboard) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry.rank == 2) GoldContainer else PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (entry.rank) {
                                            1 -> GoldAccent
                                            2 -> NavyPrimary
                                            3 -> Color(0xFFCD7F32) // Bronze
                                            else -> SlateBackground
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${entry.rank}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = if (entry.rank in 1..3) PureWhite else TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "Stream: ${entry.stream} • ${entry.testsCompleted} Tests Completed",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Text(
                                text = "${entry.score}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (entry.rank == 1) GoldDark else NavyPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
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
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NavyPrimary)
            Text(title, fontSize = 10.sp, color = TextSecondary)
        }
    }
}
