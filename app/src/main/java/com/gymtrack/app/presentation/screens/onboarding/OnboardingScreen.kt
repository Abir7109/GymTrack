package com.gymtrack.app.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtrack.app.domain.model.*
import com.gymtrack.app.presentation.theme.*
import com.gymtrack.app.presentation.components.SimpleGlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// Onboarding Screen with 5 Phases
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit
) {
    val user by viewModel.user.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState()
    
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 5
    val progress = (currentStep + 1).toFloat() / totalSteps
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding")
    val backgroundShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        DeepBlue,
                        CyberBlue,
                        ElectricPurple,
                        GradientStart,
                        DeepBlue
                    ),
                    center = Offset(0.5f, 0.5f)
                )
            )
    ) {
        if (isLoading) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Progress Indicator - Neon Glow
                OnboardingProgressIndicator(currentStep = currentStep, totalSteps = totalSteps)
                
                // Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                        },
                        label = "step"
                    ) { step ->
                        when (step) {
                            0 -> ProfileSyncPhase(
                                onComplete = { name, gender, dob -> 
                                    viewModel.updateName(name)
                                    viewModel.updateGender(gender)
                                    dob?.let { viewModel.updateDateOfBirth(it) }
                                    currentStep = 1 
                                }
                            )
                            1 -> StatsLabPhase(
                                onComplete = { height, weight, bodyFat -> 
                                    viewModel.updateHeight(height)
                                    viewModel.updateWeight(weight)
                                    currentStep = 2 
                                }
                            )
                            2 -> EngineCheckPhase(
                                onComplete = { experience, weeklyDays, styles -> 
                                    viewModel.updateExperienceLevel(experience)
                                    viewModel.updateWeeklyWorkoutDays(weeklyDays)
                                    viewModel.updateWorkoutStyles(styles)
                                    currentStep = 3 
                                }
                            )
                            3 -> GoalCalibrationPhase(
                                onComplete = { goal -> 
                                    viewModel.updatePrimaryGoal(goal)
                                    currentStep = 4 
                                }
                            )
                            4 -> ProcessingPhase(
                                onComplete = onOnboardingComplete
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingProgressIndicator(currentStep: Int, totalSteps: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STEP ${currentStep + 1} OF $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondaryDark
            )
            
            // Neon percentage indicator
            val progress = (currentStep + 1).toFloat() / totalSteps
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Neon Glow Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(DarkCard, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ElectricBlue, NeonCyan, ElectricPurple)
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Neon dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val isActive = index <= currentStep
                val isCurrent = index == currentStep
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isCurrent) 12.dp else 8.dp)
                        .background(
                            color = if (isActive) NeonCyan else DarkCard,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = NeonCyan,
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Loading your profile...",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryDark
            )
        }
    }
}

// Phase 1: Profile Sync
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSyncPhase(
    onComplete: (name: String, gender: Gender, dob: java.time.LocalDate?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    
    val haptic = LocalHapticFeedback.current
    
    SimpleGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon with glow
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(GlassBackground, Color.Transparent)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = NeonCyan
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Initializing Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Let's get to know you",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryDark
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Name Input with glass effect
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkCard,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedLabelColor = NeonCyan,
                    unfocusedLabelColor = TextSecondaryDark,
                    cursorColor = NeonCyan
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Gender Picker with Wheel Picker effect
            Text(
                text = "Gender",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondaryDark,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Gender.entries.filter { it != Gender.PREFER_NOT_TO_SAY }.forEach { gender ->
                    val isSelected = selectedGender == gender
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            selectedGender = gender
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        label = { Text(gender.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue.copy(alpha = 0.3f),
                            selectedLabelColor = NeonCyan,
                            containerColor = DarkCard,
                            labelColor = TextSecondaryDark
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Continue Button with gradient
            Button(
                onClick = { 
                    if (name.isNotBlank() && selectedGender != null) {
                        onComplete(name, selectedGender!!, null)
                    }
                },
                enabled = name.isNotBlank() && selectedGender != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = DarkBackground,
                    disabledContainerColor = DarkCard,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Phase 2: Stats Lab
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsLabPhase(
    onComplete: (height: Float?, weight: Float?, bodyFat: Float?) -> Unit
) {
    var height by remember { mutableFloatStateOf(170f) }
    var weight by remember { mutableFloatStateOf(70f) }
    var bodyFat by remember { mutableFloatStateOf(15f) }
    
    val haptic = LocalHapticFeedback.current
    
    SimpleGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "The Stats Lab",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Fine-tune your biometrics",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryDark
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Height Ruler
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Height: ${height.toInt()} cm",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                
                Slider(
                    value = height,
                    onValueChange = { 
                        height = it
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    valueRange = 120f..220f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = ElectricBlue,
                        inactiveTrackColor = DarkCard
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weight Ruler
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Weight: ${weight.toInt()} kg",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                
                Slider(
                    value = weight,
                    onValueChange = { 
                        weight = it
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    valueRange = 30f..200f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = ElectricBlue,
                        inactiveTrackColor = DarkCard
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Body Fat (Optional)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Body Fat: ${bodyFat.toInt()}% (optional)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondaryDark
                )
                
                Slider(
                    value = bodyFat,
                    onValueChange = { bodyFat = it },
                    valueRange = 5f..50f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = ElectricBlue,
                        inactiveTrackColor = DarkCard
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Visual reference silhouettes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    "Lean" to 10f,
                    "Athletic" to 18f,
                    "Average" to 25f
                ).forEach { (label, value) ->
                    val isSelected = kotlin.math.abs(bodyFat - value) < 3f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { bodyFat = value }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) ElectricBlue.copy(alpha = 0.5f)
                                    else DarkCard,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else TextSecondaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) NeonCyan else TextSecondaryDark
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onComplete(height, weight, bodyFat) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Phase 3: Engine Check
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EngineCheckPhase(
    onComplete: (experience: ExperienceLevel, weeklyDays: Int, styles: List<WorkoutStyle>) -> Unit
) {
    var selectedExperience by remember { mutableStateOf<ExperienceLevel?>(null) }
    var selectedDays by remember { mutableIntStateOf(3) }
    var selectedStyles by remember { mutableStateOf(setOf<WorkoutStyle>()) }
    
    val haptic = LocalHapticFeedback.current
    
    SimpleGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Engine Check",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "What's your starting point?",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryDark
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Experience Level Cards
            Text(
                text = "Experience Level",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondaryDark,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExperienceLevel.entries.forEach { level ->
                    val isSelected = selectedExperience == level
                    val description = when(level) {
                        ExperienceLevel.BEGINNER -> "0-6 months"
                        ExperienceLevel.INTERMEDIATE -> "1-2 years"
                        ExperienceLevel.ADVANCED -> "3+ years"
                    }
                    
                    Card(
                        onClick = { 
                            selectedExperience = level
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 100.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ElectricBlue.copy(alpha = 0.3f) else DarkCard
                        ),
                        border = if (isSelected) BorderStroke(2.dp, NeonCyan) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                when(level) {
                                    ExperienceLevel.BEGINNER -> Icons.Default.SentimentSatisfied
                                    ExperienceLevel.INTERMEDIATE -> Icons.Default.FitnessCenter
                                    ExperienceLevel.ADVANCED -> Icons.Default.EmojiEvents
                                },
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else TextSecondaryDark,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = level.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) NeonCyan else TextSecondaryDark,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Weekly Days - Scrollable for smaller screens
            Text(
                text = "Training Days Per Week",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondaryDark,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                (1..7).forEach { day ->
                    val isSelected = selectedDays == day
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isSelected) ElectricBlue else DarkCard,
                                CircleShape
                            )
                            .clickable { 
                                selectedDays = day
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            color = if (isSelected) DarkBackground else TextSecondaryDark,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Workout Styles
            Text(
                text = "Preferred Styles",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondaryDark,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkoutStyle.entries.take(6).forEach { style ->
                    val isSelected = style in selectedStyles
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            selectedStyles = if (style in selectedStyles) {
                                selectedStyles - style
                            } else {
                                selectedStyles + style
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        label = { Text(style.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple.copy(alpha = 0.3f),
                            selectedLabelColor = NeonPurple,
                            containerColor = DarkCard,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (selectedExperience != null && selectedStyles.isNotEmpty()) {
                        onComplete(selectedExperience!!, selectedDays, selectedStyles.toList())
                    }
                },
                enabled = selectedExperience != null && selectedStyles.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = DarkBackground,
                    disabledContainerColor = DarkCard,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Phase 4: Goal Calibration
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCalibrationPhase(
    onComplete: (goal: FitnessGoal) -> Unit
) {
    var selectedGoal by remember { mutableStateOf<FitnessGoal?>(null) }
    
    val haptic = LocalHapticFeedback.current
    
    val goals = listOf(
        GoalOption(FitnessGoal.MUSCLE_GAIN, "Build Armor", "Maximize muscle growth", Icons.Default.FitnessCenter),
        GoalOption(FitnessGoal.FAT_LOSS, "Lean & Mean", "Burn fat efficiently", Icons.Default.LocalFireDepartment),
        GoalOption(FitnessGoal.STRENGTH, "Peak Performance", "Maximize strength", Icons.Default.EmojiEvents),
        GoalOption(FitnessGoal.MAINTENANCE, "Health First", "Stay healthy long-term", Icons.Default.Favorite)
    )
    
    SimpleGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Destination",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "What are you working toward?",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryDark
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            goals.forEach { goal ->
                val isSelected = selectedGoal == goal.goal
                Card(
                    onClick = { 
                        selectedGoal = goal.goal
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) ElectricBlue.copy(alpha = 0.3f) else DarkCard
                    ),
                    border = if (isSelected) BorderStroke(2.dp, NeonCyan) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (isSelected) ElectricBlue.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                goal.icon,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else TextSecondaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) NeonCyan else TextPrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = goal.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }
                        
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (selectedGoal != null) {
                        onComplete(selectedGoal!!)
                    }
                },
                enabled = selectedGoal != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = DarkBackground,
                    disabledContainerColor = DarkCard,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Finish Setup", fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class GoalOption(
    val goal: FitnessGoal,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// Phase 5: Processing Animation
@Composable
fun ProcessingPhase(
    onComplete: () -> Unit
) {
    var currentMessage by remember { mutableIntStateOf(0) }
    val messages = listOf(
        "Calculating macro baselines...",
        "Optimizing training splits...",
        "Setting up your personal arena...",
        "Preparing your dashboard...",
        "Almost ready!"
    )
    
    LaunchedEffect(Unit) {
        while (currentMessage < messages.size - 1) {
            delay(1500)
            currentMessage++
        }
        delay(2000)
        onComplete()
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "processing")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated core
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(pulse)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue,
                            ElectricPurple.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonCyan, ElectricBlue)
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = messages.getOrElse(currentMessage) { messages.last() },
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimaryDark
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(4.dp),
            color = NeonCyan,
            trackColor = DarkCard
        )
    }
}

// Glass Card Component
@Composable
fun SimpleGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = GlassBackgroundDark.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        content()
    }
}
