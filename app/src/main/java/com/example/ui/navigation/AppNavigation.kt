package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.data.model.CourseMaterial
import com.example.data.model.MockTest
import com.example.data.model.TestResult
import com.example.data.model.VideoLesson
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.cbt.CbtExamScreen
import com.example.ui.screens.cbt.TestResultScreen
import com.example.ui.screens.courses.CourseDetailScreen
import com.example.ui.screens.courses.CoursesScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.learning.PdfViewerScreen
import com.example.ui.screens.learning.VideoPlayerScreen
import com.example.ui.screens.settings.ConfigScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Home", Icons.Filled.Home, Icons.Outlined.Home),
    COURSES("Courses", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    ANALYTICS("Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    ADMIN("Admin Portal", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

sealed class ScreenDestination {
    data object Main : ScreenDestination()
    data class CourseDetail(val course: Course) : ScreenDestination()
    data class PdfView(val material: CourseMaterial) : ScreenDestination()
    data class VideoView(val lesson: VideoLesson) : ScreenDestination()
    data class CbtExam(val test: MockTest) : ScreenDestination()
    data class CbtResult(val result: TestResult) : ScreenDestination()
    data object Config : ScreenDestination()
}

@Composable
fun AppNavigation(
    viewModel: AcademyViewModel
) {
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    var currentDestination by remember { mutableStateOf<ScreenDestination>(ScreenDestination.Main) }

    // If not authenticated, present the professional Auth / OTP Screen
    if (!isLoggedIn) {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = {
                currentDestination = ScreenDestination.Main
                currentTab = MainTab.DASHBOARD
            }
        )
        return
    }

    // Secondary Screen Back Navigation Handler
    if (currentDestination !is ScreenDestination.Main) {
        BackHandler {
            when (currentDestination) {
                is ScreenDestination.CbtExam -> {
                    // Prevent accidental exam exit or confirm cancellation
                    currentDestination = ScreenDestination.Main
                }
                is ScreenDestination.CourseDetail -> currentDestination = ScreenDestination.Main
                is ScreenDestination.PdfView -> {
                    viewModel.closePdfViewer()
                    currentDestination = ScreenDestination.Main
                }
                is ScreenDestination.VideoView -> {
                    viewModel.closeVideoPlayer()
                    currentDestination = ScreenDestination.Main
                }
                is ScreenDestination.CbtResult -> currentDestination = ScreenDestination.Main
                is ScreenDestination.Config -> currentDestination = ScreenDestination.Main
                else -> currentDestination = ScreenDestination.Main
            }
        }
    }

    // Render Active Destination
    when (val dest = currentDestination) {
        is ScreenDestination.CourseDetail -> {
            CourseDetailScreen(
                course = dest.course,
                viewModel = viewModel,
                onBack = { currentDestination = ScreenDestination.Main },
                onOpenPdf = { mat -> currentDestination = ScreenDestination.PdfView(mat) },
                onOpenVideo = { vid -> currentDestination = ScreenDestination.VideoView(vid) }
            )
        }

        is ScreenDestination.PdfView -> {
            PdfViewerScreen(
                material = dest.material,
                viewModel = viewModel,
                onBack = {
                    viewModel.closePdfViewer()
                    currentDestination = ScreenDestination.Main
                }
            )
        }

        is ScreenDestination.VideoView -> {
            VideoPlayerScreen(
                lesson = dest.lesson,
                viewModel = viewModel,
                onBack = {
                    viewModel.closeVideoPlayer()
                    currentDestination = ScreenDestination.Main
                }
            )
        }

        is ScreenDestination.CbtExam -> {
            CbtExamScreen(
                test = dest.test,
                viewModel = viewModel,
                onSubmitComplete = { result ->
                    currentDestination = ScreenDestination.CbtResult(result)
                },
                onCancelExam = { currentDestination = ScreenDestination.Main }
            )
        }

        is ScreenDestination.CbtResult -> {
            TestResultScreen(
                result = dest.result,
                onBackToHome = { currentDestination = ScreenDestination.Main }
            )
        }

        is ScreenDestination.Config -> {
            ConfigScreen(
                viewModel = viewModel,
                onBack = { currentDestination = ScreenDestination.Main }
            )
        }

        ScreenDestination.Main -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = PureWhite,
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        MainTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) NavyPrimary else TextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp,
                                        color = if (isSelected) NavyPrimary else TextSecondary
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = GoldContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                        when (tab) {
                            MainTab.DASHBOARD -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToCourses = { currentTab = MainTab.COURSES },
                                    onNavigateToCourseDetail = { course ->
                                        currentDestination = ScreenDestination.CourseDetail(course)
                                    },
                                    onStartExam = { test ->
                                        viewModel.startCbtExam(test)
                                        currentDestination = ScreenDestination.CbtExam(test)
                                    },
                                    onNavigateToAnalytics = { currentTab = MainTab.ANALYTICS },
                                    onNavigateToAdmin = { currentTab = MainTab.ADMIN }
                                )
                            }

                            MainTab.COURSES -> {
                                CoursesScreen(
                                    viewModel = viewModel,
                                    onCourseClick = { course ->
                                        currentDestination = ScreenDestination.CourseDetail(course)
                                    }
                                )
                            }

                            MainTab.ANALYTICS -> {
                                AnalyticsScreen(viewModel = viewModel)
                            }

                            MainTab.ADMIN -> {
                                AdminDashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToConfig = { currentDestination = ScreenDestination.Config }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
