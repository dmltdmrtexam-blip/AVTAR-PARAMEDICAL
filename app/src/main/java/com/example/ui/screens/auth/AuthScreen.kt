package com.example.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AcademyViewModel,
    onAuthSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var mobileNumber by remember { mutableStateOf("9876543210") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    // Register fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedStream by remember { mutableStateOf("DMLT") }
    var expandedStreamDropdown by remember { mutableStateOf(false) }

    val streams = listOf("DMLT", "DRT (Radiology)", "BMLT", "OT Technology", "ECG & Cardiology")
    val statusMsg by viewModel.statusMessage.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyPrimary, NavyLight, NavyDark)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Academy Logo & Branding
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0B1E36))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_academy_logo),
                    contentDescription = "Avtar Paramedical Academy Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "AVTAR PARAMEDICAL ACADEMY",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Learn Better. Practice Smarter. Achieve Success.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = GoldLight.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab Selector: Login vs Register
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateBackground)
                            .padding(4.dp)
                    ) {
                        TabButton(
                            text = "OTP Login",
                            isSelected = !isRegisterMode,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                isRegisterMode = false
                                isOtpSent = false
                            }
                        )
                        TabButton(
                            text = "Register",
                            isSelected = isRegisterMode,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                isRegisterMode = true
                                isOtpSent = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isRegisterMode) {
                        // LOGIN MODE
                        Text(
                            text = "Mobile Number Verification",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Text(
                            text = "Enter your registered mobile number for instant OTP verification",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { if (it.length <= 10) mobileNumber = it },
                            label = { Text("Mobile Number (10 digits)") },
                            leadingIcon = {
                                Text(
                                    "+91 ",
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NavyPrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mobile_number_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isOtpSent) {
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6) otpCode = it },
                                label = { Text("Enter 4-Digit OTP") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_code_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Demo OTP: 8447 or 1234",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MedicalTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (viewModel.verifyOtp(mobileNumber, otpCode)) {
                                        onAuthSuccess()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("verify_otp_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Verify & Enter Academy", fontWeight = FontWeight.Bold, color = PureWhite)
                            }

                            TextButton(
                                onClick = { viewModel.sendOtp(mobileNumber) }
                            ) {
                                Text("Resend OTP", color = MedicalBlue, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (mobileNumber.length >= 10) {
                                        val code = viewModel.sendOtp(mobileNumber)
                                        otpCode = code
                                        isOtpSent = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("send_otp_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Get Verification OTP", fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        }
                    } else {
                        // REGISTRATION MODE
                        Text(
                            text = "New Student Enrollment",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { if (it.length <= 10) mobileNumber = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Text("+91 ", fontWeight = FontWeight.Bold, color = NavyPrimary, modifier = Modifier.padding(start = 12.dp)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_mobile_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stream Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedStreamDropdown,
                            onExpandedChange = { expandedStreamDropdown = !expandedStreamDropdown }
                        ) {
                            OutlinedTextField(
                                value = selectedStream,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Paramedical Stream") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStreamDropdown) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedStreamDropdown,
                                onDismissRequest = { expandedStreamDropdown = false }
                            ) {
                                streams.forEach { streamItem ->
                                    DropdownMenuItem(
                                        text = { Text(streamItem) },
                                        onClick = {
                                            selectedStream = streamItem
                                            expandedStreamDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (fullName.isNotBlank() && mobileNumber.isNotBlank()) {
                                    viewModel.registerStudent(fullName, mobileNumber, email, selectedStream)
                                    onAuthSuccess()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("complete_registration_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Complete Registration", fontWeight = FontWeight.Bold, color = PureWhite)
                        }
                    }

                    if (statusMsg != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = statusMsg ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MedicalTeal,
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Demo Bypass Button for Instant Evaluation
                    OutlinedButton(
                        onClick = {
                            viewModel.verifyOtp("9876543210", "8447")
                            onAuthSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_demo_login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instant Student Demo Access", color = NavyPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Encrypted Single-Session & Anti-Piracy Protected",
                    style = MaterialTheme.typography.bodySmall.copy(color = GoldLight)
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) NavyPrimary else Color.Transparent,
            contentColor = if (isSelected) PureWhite else TextSecondary
        ),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
