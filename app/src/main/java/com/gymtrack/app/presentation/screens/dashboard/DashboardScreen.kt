package com.gymtrack.app.presentation.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtrack.app.domain.model.Workout
import com.gymtrack.app.domain.model.Exercise
import com.gymtrack.app.presentation.components.ExerciseImage
import com.gymtrack.app.presentation.theme.*
import kotlin.math.sin
import kotlin.math.cos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToCommunity: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Use selectedTab directly - this is the source of truth from MainActivity
    val selectedBottomNav = selectedTab
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Header with gradient
            item {
                HeroHeader(
                    userName = uiState.user?.name ?: "Athlete",
                    currentStreak = uiState.currentStreak,
                    onProfileClick = { onTabSelected(5) },
                )
            }
            
            // Stats Row - 4 cards
            item {
                StatsRow(
                    workoutsThisWeek = uiState.workoutsThisWeek,
                    totalMinutes = uiState.totalMinutesThisWeek,
                    currentStreak = uiState.currentStreak,
                    totalWorkouts = uiState.totalWorkouts
                )
            }
            
            // Progress Card with actual progress
            item {
                WeeklyProgressCard(workoutsThisWeek = uiState.workoutsThisWeek)
            }
            
            // Quick Actions - Large buttons
            item {
                QuickActionsGrid(
                    onStartWorkout = onNavigateToLibrary,
                    onBrowseExercises = onNavigateToLibrary,
                    onViewProgress = onNavigateToProgress,
                    onCommunity = onNavigateToCommunity
                )
            }
            
            // Suggested Exercises based on user goal
            if (uiState.suggestedExercises.isNotEmpty()) {
                item {
                    Text(
                        text = "Recommended For You",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.suggestedExercises) { exercise ->
                            SuggestedExerciseCard(exercise = exercise, onClick = onNavigateToLibrary, userWeight = uiState.user?.weight ?: 70f)
                        }
                    }
                }
            }
            
            // Recent Workouts Section
            item {
                Text(
                    text = "Recent Workouts",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (uiState.recentWorkouts.isEmpty()) {
                item {
                    EmptyStateCard(onStartWorkout = onNavigateToLibrary)
                }
            } else {
                items(uiState.recentWorkouts.take(3)) { workout ->
                    ModernWorkoutCard(workout = workout, onClick = { })
                }
            }
            
            // Tips Section
            item {
                TipsCard()
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun HeroHeader(userName: String, currentStreak: Int, onProfileClick: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "pulse"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D1B2A),
                            ElectricBlue.copy(alpha = 0.3f),
                            Color(0xFF1A1A2E)
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                        )
                )
        )
        
        // Decorative fitness figure - large faded dumbbell icon background
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = ElectricBlue.copy(alpha = pulseAlpha),
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 20.dp, y = 10.dp)
        )
        
        // Decorative running figure silhouette
        Icon(
            imageVector = Icons.Default.DirectionsRun,
            contentDescription = null,
            tint = NeonCyan.copy(alpha = pulseAlpha * 0.7f),
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-30).dp, y = 10.dp)
        )
        
        // Small decorative elements
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = EnergeticOrange.copy(alpha = 0.4f),
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-60).dp, y = 20.dp)
        )
        
        Icon(
            imageVector = Icons.Default.MonitorHeart,
            contentDescription = null,
            tint = NeonGreen.copy(alpha = 0.3f),
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.TopStart)
                .offset(x = 120.dp, y = 15.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                ElectricBlue.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                if (currentStreak > 0) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = EnergeticOrange.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = EnergeticOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentStreak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EnergeticOrange
                            )
                        }
                    }
                }
            }
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonGreen.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(NeonGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ready to train",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    workoutsThisWeek: Int,
    totalMinutes: Int,
    currentStreak: Int,
    totalWorkouts: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Default.FitnessCenter,
            value = "$workoutsThisWeek",
            label = "This Week",
            color = ElectricBlue,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.Timer,
            value = "$totalMinutes",
            label = "Minutes",
            color = NeonCyan,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.LocalFireDepartment,
            value = "$currentStreak",
            label = "Streak",
            color = EnergeticOrange,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.EmojiEvents,
            value = "$totalWorkouts",
            label = "Total",
            color = ElectricPurple,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark
            )
        }
    }
}

@Composable
private fun WeeklyProgressCard(workoutsThisWeek: Int, weeklyGoal: Int = 3) {
    val progress = if (weeklyGoal > 0) (workoutsThisWeek.toFloat() / weeklyGoal).coerceIn(0f, 1f) else 0f
    val isCompleted = workoutsThisWeek >= weeklyGoal
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Goal",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = if (isCompleted) NeonGreen else NeonCyan
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = if (isCompleted) NeonGreen else NeonCyan,
                trackColor = DarkSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isCompleted) "Goal completed! ($workoutsThisWeek/$weeklyGoal)" else "$workoutsThisWeek of $weeklyGoal workouts",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onStartWorkout: () -> Unit,
    onBrowseExercises: () -> Unit,
    onViewProgress: () -> Unit,
    onCommunity: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                icon = Icons.Default.PlayArrow,
                title = "Start Workout",
                subtitle = "Begin a new session",
                color = ElectricBlue,
                onClick = onStartWorkout,
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                icon = Icons.Default.Search,
                title = "Exercises",
                subtitle = "Browse library",
                color = NeonCyan,
                onClick = onBrowseExercises,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                icon = Icons.Default.TrendingUp,
                title = "Progress",
                subtitle = "View analytics",
                color = ElectricPurple,
                onClick = onViewProgress,
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                icon = Icons.Default.Groups,
                title = "Community",
                subtitle = "Connect with others",
                color = EnergeticOrange,
                onClick = onCommunity,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Large background fitness icon for each action
    val bgIcon = when (title) {
        "Start Workout" -> Icons.Default.SportsMartialArts
        "Exercises" -> Icons.Default.SportsGymnastics
        "Progress" -> Icons.Default.ShowChart
        "Community" -> Icons.Default.People
        else -> icon
    }
    
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Large background image icon
            Icon(
                imageVector = bgIcon,
                contentDescription = null,
                tint = color.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ModernWorkoutCard(
    workout: Workout,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${workout.workoutExercises.size} exercises • ${workout.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = workout.completedAt?.let {
                        java.time.format.DateTimeFormatter.ofPattern("MMM dd").format(it)
                    } ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryDark
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(onStartWorkout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Large decorative fitness illustration
            Icon(
                imageVector = Icons.Default.SportsMartialArts,
                contentDescription = null,
                tint = ElectricBlue.copy(alpha = 0.06f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 10.dp)
            )
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = NeonCyan.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-10).dp, y = (-10).dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(ElectricBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = ElectricBlue.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No workouts yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start your first workout to begin tracking",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onStartWorkout,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Workout")
                }
            }
        }
    }
}

@Composable
private fun TipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ElectricPurple.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Large decorative sports image
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = NeonPurple.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 15.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(ElectricPurple.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsKabaddi,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tip of the Day",
                        style = MaterialTheme.typography.titleSmall,
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Remember to rest! Recovery is just as important as training.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestedExerciseCard(exercise: Exercise, onClick: () -> Unit, userWeight: Float = 70f) {
    val muscleColor = when (exercise.muscleGroup) {
        com.gymtrack.app.domain.model.MuscleGroup.CHEST -> ChestColor
        com.gymtrack.app.domain.model.MuscleGroup.BACK -> BackColor
        com.gymtrack.app.domain.model.MuscleGroup.SHOULDERS -> ShouldersColor
        com.gymtrack.app.domain.model.MuscleGroup.BICEPS, com.gymtrack.app.domain.model.MuscleGroup.TRICEPS, com.gymtrack.app.domain.model.MuscleGroup.FOREARMS -> ArmsColor
        com.gymtrack.app.domain.model.MuscleGroup.CORE -> CoreColor
        com.gymtrack.app.domain.model.MuscleGroup.QUADRICEPS, com.gymtrack.app.domain.model.MuscleGroup.HAMSTRINGS, com.gymtrack.app.domain.model.MuscleGroup.GLUTES, com.gymtrack.app.domain.model.MuscleGroup.CALVES -> LegsColor
        else -> CardioColor
    }
    
    val muscleIcon = when (exercise.muscleGroup) {
        com.gymtrack.app.domain.model.MuscleGroup.CHEST -> Icons.Default.FitnessCenter
        com.gymtrack.app.domain.model.MuscleGroup.BACK -> Icons.Default.Rowing
        com.gymtrack.app.domain.model.MuscleGroup.SHOULDERS -> Icons.Default.SportsMma
        com.gymtrack.app.domain.model.MuscleGroup.BICEPS, com.gymtrack.app.domain.model.MuscleGroup.TRICEPS -> Icons.Default.SportsHandball
        com.gymtrack.app.domain.model.MuscleGroup.CORE -> Icons.Default.SportsGymnastics
        com.gymtrack.app.domain.model.MuscleGroup.QUADRICEPS, com.gymtrack.app.domain.model.MuscleGroup.HAMSTRINGS, com.gymtrack.app.domain.model.MuscleGroup.GLUTES, com.gymtrack.app.domain.model.MuscleGroup.CALVES -> Icons.Default.DirectionsRun
        else -> Icons.Default.LocalFireDepartment
    }
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Large background fitness image
            Icon(
                imageVector = muscleIcon,
                contentDescription = null,
                tint = muscleColor.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 5.dp)
            )
            
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                ExerciseImage(
                    exercise = exercise,
                    size = 44.dp,
                    cornerRadius = 12.dp,
                    iconSize = 22.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.muscleGroup.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = muscleColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = EnergeticOrange,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "%.0f cal/min".format(exercise.calorieBurnRate * 3.5f * userWeight / 200f),
                        style = MaterialTheme.typography.labelSmall,
                        color = EnergeticOrange
                    )
                }
            }
        }
    }
}
