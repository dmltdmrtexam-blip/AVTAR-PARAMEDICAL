package com.example.ui.screens.learning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoLesson
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    lesson: VideoLesson,
    viewModel: AcademyViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isPlaying by viewModel.isVideoPlaying.collectAsState()
    val currentSec by viewModel.videoCurrentPositionSeconds.collectAsState()
    val playbackSpeed by viewModel.videoPlaybackSpeed.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    val totalSec = lesson.durationSeconds

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        ),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDark)
                .padding(paddingValues)
        ) {
            // Video Simulation Screen Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color.Black)
                    .clickable { showControls = !showControls },
                contentAlignment = Alignment.Center
            ) {
                // Background visual art
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OndemandVideo,
                        contentDescription = null,
                        tint = GoldAccent.copy(alpha = 0.5f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AVTAR PARAMEDICAL MASTERCLASS",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Candidate: ${currentUser?.fullName} (${currentUser?.rollNumber})",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }

                // Video Overlay Controls
                if (showControls) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        // Center Playback Action Buttons
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.seekVideoRelative(-10) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = PureWhite)
                            }

                            IconButton(
                                onClick = { viewModel.toggleVideoPlayPause() },
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(GoldAccent, CircleShape)
                                    .testTag("video_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = NavyDark,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.seekVideoRelative(10) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = PureWhite)
                            }
                        }

                        // Bottom Seek Bar & Timestamp
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Slider(
                                value = if (totalSec > 0) currentSec.toFloat() / totalSec.toFloat() else 0f,
                                onValueChange = { fraction ->
                                    viewModel.seekVideoRelative(((fraction * totalSec) - currentSec).toInt())
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldAccent,
                                    activeTrackColor = GoldAccent,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth().height(20.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val currentMin = currentSec / 60
                                val currentRemSec = currentSec % 60
                                val totalMin = totalSec / 60
                                val totalRemSec = totalSec % 60

                                Text(
                                    text = String.format("%02d:%02d / %02d:%02d", currentMin, currentRemSec, totalMin, totalRemSec),
                                    color = PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                // Speed Selector Pill
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                        Surface(
                                            color = if (playbackSpeed == speed) GoldAccent else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.clickable { viewModel.setVideoSpeed(speed) }
                                        ) {
                                            Text(
                                                text = "${speed}x",
                                                color = if (playbackSpeed == speed) NavyDark else PureWhite,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Video Details and Study Notes below
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PureWhite)
                    .padding(18.dp)
            ) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SlateBackground)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Key Lecture Highlights:",
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    "Standard Operating Procedures (SOP) demonstrated on actual clinical equipment.",
                    "Common diagnostic pitfalls and examiner viva questions highlighted.",
                    "Download accompanying lecture summary notes from the course materials tab."
                ).forEach { bullet ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MedicalTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(bullet, fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}
