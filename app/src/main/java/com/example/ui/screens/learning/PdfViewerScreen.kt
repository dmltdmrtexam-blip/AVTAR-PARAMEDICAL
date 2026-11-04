package com.example.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourseMaterial
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    material: CourseMaterial,
    viewModel: AcademyViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentPage by viewModel.pdfCurrentPage.collectAsState()
    val zoomLevel by viewModel.pdfZoomLevel.collectAsState()

    val totalPages = material.pageCount
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = material.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Page $currentPage of $totalPages • Zoom ${(zoomLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.zoomOutPdf() }) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = NavyPrimary)
                    }
                    IconButton(onClick = { viewModel.zoomInPdf() }) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        bottomBar = {
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setPdfPage(currentPage - 1) },
                        enabled = currentPage > 1,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("pdf_prev_page")
                    ) {
                        Icon(Icons.Default.NavigateBefore, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    Text(
                        text = "$currentPage / $totalPages",
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        fontSize = 14.sp
                    )

                    Button(
                        onClick = { viewModel.setPdfPage(currentPage + 1) },
                        enabled = currentPage < totalPages,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("pdf_next_page")
                    ) {
                        Text("Next", color = PureWhite)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.NavigateNext, contentDescription = null, tint = PureWhite)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE2E8F0))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Document Page Simulation Container
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Actual Clinical Medical Content for page
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AVTAR PARAMEDICAL ACADEMY",
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary,
                                fontSize = (12 * zoomLevel).sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "SECTION: $currentPage",
                                fontWeight = FontWeight.Bold,
                                color = GoldDark,
                                fontSize = (11 * zoomLevel).sp
                            )
                        }

                        HorizontalDivider(
                            color = NavyLight,
                            thickness = 2.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            text = when (currentPage) {
                                1 -> "CHAPTER 1: PRINCIPLES OF CLINICAL LABORATORY SPECIMEN PROCESSING"
                                2 -> "CHAPTER 2: COMPLETE BLOOD COUNT (CBC) & AUTOMATED HEMATOLOGY ANALYZERS"
                                3 -> "CHAPTER 3: X-RAY TUBE ANODE THERMAL DYNAMICS & HIGH-VOLTAGE GENERATORS"
                                else -> "MODULE STUDY NOTES & EXAMINATION SUMMARY (PAGE $currentPage)"
                            },
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = (15 * zoomLevel).sp,
                            lineHeight = (20 * zoomLevel).sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // High Yield Clinical Points
                        Surface(
                            color = SlateBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "EXAM CLINICAL PEARL (HIGH YIELD):",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MedicalTeal,
                                    fontSize = (11 * zoomLevel).sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "1. Phlebotomy Order of Draw: Blood Cultures -> Citrate (Light Blue 1:9) -> Serum Tube (Red/Gold) -> Heparin (Green) -> EDTA (Lavender/Purple) -> Fluoride (Gray).\n" +
                                            "2. EDTA prevents coagulation by chelating ionized calcium (Ca2+).\n" +
                                            "3. Excessive EDTA causes erythrocyte shrinkage, falsely lowering the hematocrit (PCV).",
                                    fontSize = (12 * zoomLevel).sp,
                                    lineHeight = (18 * zoomLevel).sp,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Detailed Anatomy & Analytical Protocols:\n\n" +
                                    "Specimens must be inverted 8 to 10 times immediately after collection to ensure thorough mixing with anticoagulant granules. In automated 5-part differential analyzers, electrical impedance is utilized for volumetric cell sizing (Coulter principle), while flow cytometry laser scatter (forward scatter for volume, side scatter for nuclear internal complexity) differentiates neutrophils, lymphocytes, monocytes, eosinophils, and basophils.\n\n" +
                                    "Quality Control: Run daily normal, low, and high commercial whole-blood controls with Levey-Jennings chart tracking and Westgard multi-rules compliance.",
                            fontSize = (13 * zoomLevel).sp,
                            lineHeight = (22 * zoomLevel).sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(30.dp))
                    }

                    // STUDENT ANTI-PIRACY SECURITY WATERMARK (Translucent overlay)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val studentInfo = "${currentUser?.fullName} • +91 ${currentUser?.mobileNumber} • ROLL: ${currentUser?.rollNumber}"
                        repeat(4) {
                            Text(
                                text = "AVTAR PARAMEDICAL ACADEMY\n$studentInfo\nCONFIDENTIAL - DO NOT DISTRIBUTE",
                                color = Color.Gray.copy(alpha = 0.12f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(vertical = 40.dp)
                                    .rotate(-20f)
                            )
                        }
                    }
                }
            }
        }
    }
}
