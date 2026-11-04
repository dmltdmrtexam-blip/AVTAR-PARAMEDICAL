package com.example.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.data.model.CourseMaterial
import com.example.data.model.PaymentMethod
import com.example.data.model.VideoLesson
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    course: Course,
    viewModel: AcademyViewModel,
    onBack: () -> Unit,
    onOpenPdf: (CourseMaterial) -> Unit,
    onOpenVideo: (VideoLesson) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showPaymentModal by remember { mutableStateOf(false) }
    var showOfflineModal by remember { mutableStateOf(false) }
    var utrInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }

    val statusMsg by viewModel.statusMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Course Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        bottomBar = {
            if (!course.isEnrolled && !course.isFree) {
                Surface(
                    color = PureWhite,
                    shadowElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Course Fee", fontSize = 11.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "₹${course.discountPrice.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${course.price.toInt()}",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showOfflineModal = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Offline UTR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            }

                            Button(
                                onClick = { showPaymentModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("enroll_razorpay_button")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pay via Razorpay", color = NavyDark, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Surface(
                            color = NavyLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${course.categoryCode} MASTER PROGRAM",
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = course.title,
                            color = PureWhite,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = course.description,
                            color = GoldLight.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👨‍⚕️ ${course.instructor}", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("⏳ ${course.validityDays} Days Access", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tabs for Learning Content
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PureWhite,
                    contentColor = NavyPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Study PDFs (${course.materials.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Videos (${course.videos.size})", fontWeight = FontWeight.Bold) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (selectedTab == 0) {
                // PDF Materials Tab
                items(course.materials) { material ->
                    val isLocked = !course.isEnrolled && !material.isFreePreview
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .clickable(enabled = !isLocked) { onOpenPdf(material) }
                            .testTag("material_item_${material.id}"),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isLocked) SlateBackground else ErrorRedLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = if (isLocked) TextMuted else ErrorRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = material.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isLocked) TextSecondary else NavyPrimary
                                )
                                Text(
                                    text = "${material.pageCount} Pages • High Yield Paramedical Notes",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isLocked) {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted)
                            } else {
                                Icon(Icons.Default.Visibility, contentDescription = "Read", tint = MedicalTeal)
                            }
                        }
                    }
                }
            } else {
                // Videos Tab
                items(course.videos) { video ->
                    val isLocked = !course.isEnrolled && !video.isFreePreview
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .clickable(enabled = !isLocked) { onOpenVideo(video) }
                            .testTag("video_item_${video.id}"),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isLocked) SlateBackground else MedicalBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isLocked) TextMuted else MedicalBlue,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isLocked) TextSecondary else NavyPrimary
                                )
                                Text(
                                    text = "${video.durationSeconds / 60} mins • ${video.description}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isLocked) {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted)
                            } else {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Watch", tint = MedicalBlue)
                            }
                        }
                    }
                }
            }
        }
    }

    // Razorpay Instant Checkout Modal
    if (showPaymentModal) {
        AlertDialog(
            onDismissRequest = { showPaymentModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = MedicalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Razorpay Secure Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enrolling in: ${course.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Payable Amount: ₹${course.discountPrice.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = SuccessGreen,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Select Payment Gateway Option:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        PaymentMethod.UPI to "UPI (Google Pay, PhonePe, Paytm, BHIM)",
                        PaymentMethod.CARD to "Debit / Credit Card (Visa, RuPay, MasterCard)",
                        PaymentMethod.NETBANKING to "Net Banking (All Indian Banks)"
                    ).forEach { (method, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedPaymentMethod == method) MedicalBlueLight else SlateBackground)
                                .clickable { selectedPaymentMethod = method }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔒 256-bit SSL encrypted & HMAC-SHA256 signature verified",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.enrollWithRazorpay(course, selectedPaymentMethod)
                        showPaymentModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.testTag("confirm_razorpay_pay")
                ) {
                    Text("Pay ₹${course.discountPrice.toInt()}", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentModal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Offline Bank UTR Submission Modal
    if (showOfflineModal) {
        AlertDialog(
            onDismissRequest = { showOfflineModal = false },
            title = {
                Text("Submit Offline Bank / UPI Transfer", fontWeight = FontWeight.Bold, color = NavyPrimary)
            },
            text = {
                Column {
                    Text(
                        text = "Academy Bank Details:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NavyPrimary
                    )
                    Text("Bank: HDFC Bank\nA/C: 50200084472026\nIFSC: HDFC0001234\nUPI: avtarparamedical@hdfcbank", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = utrInput,
                        onValueChange = { utrInput = it },
                        label = { Text("12-Digit UTR / Transaction Ref") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("utr_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Once submitted, Academy Admin verifies the bank credit and approves access in audit portal.",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitOfflinePayment(course, utrInput)
                        showOfflineModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Submit for Verification", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfflineModal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
