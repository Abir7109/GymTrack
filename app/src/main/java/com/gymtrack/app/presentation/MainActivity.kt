package com.gymtrack.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.gymtrack.app.presentation.theme.GymTrackTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gymtrack.app.data.repository.DataStoreManager
import com.gymtrack.app.presentation.navigation.Screen
import com.gymtrack.app.presentation.screens.dashboard.DashboardScreen
import com.gymtrack.app.presentation.screens.onboarding.OnboardingScreen
import com.gymtrack.app.presentation.screens.onboarding.OnboardingViewModel
import com.gymtrack.app.presentation.screens.library.LibraryScreen
import com.gymtrack.app.presentation.screens.progress.ProgressScreen
import com.gymtrack.app.presentation.screens.community.CommunityScreen
import com.gymtrack.app.presentation.screens.profile.ProfileScreen
import com.gymtrack.app.presentation.screens.workout.WorkoutSessionScreen
import com.gymtrack.app.domain.model.Exercise
import com.gymtrack.app.presentation.theme.DarkBackground
import com.gymtrack.app.presentation.theme.NeonCyan
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by dataStoreManager.darkModeFlow.collectAsState(initial = true)
            GymTrackTheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GymTrackApp()
                }
            }
        }
    }
}

@Composable
fun GymTrackApp() {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val isOnboardingDone by onboardingViewModel.isOnboardingDone.collectAsState()

    // Track selected tab for bottom navigation
    var selectedTab by remember { mutableIntStateOf(0) }

    // Track exercise to add when navigating from library
    var exerciseToAdd by remember { mutableStateOf<com.gymtrack.app.domain.model.Exercise?>(null) }

    // Show loading screen until the one-shot DB check completes
    if (isOnboardingDone == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NeonCyan)
        }
        return
    }

    val startDestination = if (isOnboardingDone == true) {
        Screen.Dashboard.route
    } else {
        Screen.Onboarding.route
    }

    // Navigation callbacks for bottom nav
    val onTabSelected: (Int) -> Unit = { tab ->
        selectedTab = tab
        val route = when (tab) {
            0 -> Screen.Dashboard.route
            1 -> Screen.Library.route
            2 -> Screen.Progress.route
            3 -> Screen.Community.route
            4 -> Screen.Profile.route
            5 -> Screen.Profile.route
            else -> Screen.Dashboard.route
        }
        navController.navigate(route) {
            popUpTo(Screen.Dashboard.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onOnboardingComplete = {
                    onboardingViewModel.saveOnboarding {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                onNavigateToLibrary = { onTabSelected(1) },
                onNavigateToProgress = { onTabSelected(2) },
                onNavigateToCommunity = { onTabSelected(3) },
                onNavigateToProfile = { onTabSelected(4) }
            )
        }
        
        
        composable(Screen.Library.route) {
            LibraryScreen(
                selectedTab = 1,
                onTabSelected = onTabSelected,
                onNavigateBack = { navController.popBackStack() },
                onStartWorkoutWithExercise = { exercise ->
                    // Store selected exercise and navigate to workout session
                    exerciseToAdd = exercise
                    navController.navigate(Screen.WorkoutSession.route) {
                        // Ensure we keep Dashboard in back stack
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                    }
                }
            )
        }
        
        composable(Screen.WorkoutSession.route) {
            // Guard against null exercise
            val exercise = exerciseToAdd ?: return@composable
            WorkoutSessionScreen(
                exercise = exercise,
                onFinish = {
                    // Reset stored exercise and navigate back to Dashboard
                    exerciseToAdd = null
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Progress.route) {
            ProgressScreen(
                selectedTab = 2,
                onTabSelected = onTabSelected,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Community.route) {
            CommunityScreen(
                selectedTab = 3,
                onTabSelected = onTabSelected,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                selectedTab = 4,
                onTabSelected = onTabSelected,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
