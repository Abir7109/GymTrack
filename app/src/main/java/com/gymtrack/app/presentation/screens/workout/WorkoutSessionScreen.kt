package com.gymtrack.app.presentation.screens.workout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtrack.app.domain.model.*
import com.gymtrack.app.presentation.components.ExerciseImageLarge
import com.gymtrack.app.presentation.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    exercise: Exercise,
    onFinish: () -> Unit,
    viewModel: WorkoutSessionViewModel = hiltViewModel()
) {
    LaunchedEffect(exercise) { viewModel.setExercise(exercise) }
    val state by viewModel.uiState.collectAsState()

    val bg = Brush.verticalGradient(listOf(Color(0xFF0D0D0D), Color(0xFF1A1A2E)))

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        when (state.workoutPhase) {
            WorkoutPhase.PRE_START -> PreStartContent(exercise = exercise, onStart = { viewModel.startWorkout() }, viewModel = viewModel)
            WorkoutPhase.ACTIVE -> ActiveContent(state = state, viewModel = viewModel, onFinish = onFinish)
            WorkoutPhase.REST -> RestContent(state = state, onSkip = { viewModel.skipRest() })
            WorkoutPhase.FINISHED -> FinishedContent(state = state, onFinish = { viewModel.finishWorkout(onFinish) })
        }
    }
}

@Composable
private fun PreStartContent(exercise: Exercise, onStart: () -> Unit, viewModel: WorkoutSessionViewModel? = null) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val difficultyColor = when (exercise.difficulty) {
        Difficulty.BEGINNER -> SuccessGreen
        Difficulty.INTERMEDIATE -> WarningYellow
        Difficulty.ADVANCED -> ErrorRed
    }

    val muscleColor = getExerciseMuscleColor(exercise.muscleGroup)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }) {
            Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.align(Alignment.Start).size(28.dp))
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, 200)) + scaleIn(tween(800, 200), initialScale = 0.8f)) {
            ExerciseImageLarge(
                exercise = exercise,
                modifier = Modifier.size(130.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, 400)) + slideInVertically(tween(800, 400)) { 30 }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(exercise.name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(exercise.muscleGroup.name.replace("_", " "), color = muscleColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(28.dp))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, 600)) + slideInVertically(tween(800, 600)) { 30 }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    InfoCard("Difficulty", exercise.difficulty.name, difficultyColor, modifier = Modifier.weight(1f))
                    InfoCard("Equipment", exercise.equipment.name.replace("_", " "), ElectricBlue, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    val estCalPerMin = if (viewModel != null) {
                        val state by viewModel.uiState.collectAsState()
                        exercise.calorieBurnRate * 3.5f * state.userWeight / 200f
                    } else 0f
                    InfoCard("Est. Burn", "%.0f cal/min".format(estCalPerMin), EnergeticOrange, modifier = Modifier.weight(1f))
                    InfoCard("Target", exercise.muscleGroup.name.replace("_", " "), muscleColor, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Configuration card: Body Weight, Sets, Reps
        AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, 700)) + slideInVertically(tween(800, 700)) { 30 }) {
            if (viewModel != null) {
                val state by viewModel.uiState.collectAsState()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Configuration", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        // Body Weight
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Body Weight", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("Used for calorie calculation", color = TextSecondaryDark, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                                SmallButton("-") { viewModel.setUserWeight(state.userWeight - 1f) }
                                Text(
                                    "%.0f kg".format(state.userWeight),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                SmallButton("+") { viewModel.setUserWeight(state.userWeight + 1f) }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Number of Sets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sets", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                                SmallButton("-") { viewModel.updateTotalSets(state.totalSets - 1) }
                                Text(
                                    "${state.totalSets}",
                                    color = ElectricBlue,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                SmallButton("+") { viewModel.updateTotalSets(state.totalSets + 1) }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Target Reps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target Reps", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                                SmallButton("-") { viewModel.updateTargetReps(state.targetReps - 1) }
                                Text(
                                    "${state.targetReps}",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                SmallButton("+") { viewModel.updateTargetReps(state.targetReps + 1) }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, 800)) + slideInVertically(tween(800, 800)) { 30 }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Description", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(exercise.description, color = TextSecondaryDark, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                Text("Instructions", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)), shape = RoundedCornerShape(16.dp)) {
                    Text(exercise.instructions, color = TextSecondaryDark, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
                }
            }
        }

        if (exercise.secondaryMuscles.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, 900))) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Secondary Muscles", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        exercise.secondaryMuscles.forEach { m ->
                            SuggestionChip(onClick = {}, label = { Text(m.name.replace("_"," "), color = getExerciseMuscleColor(m)) }, colors = SuggestionChipDefaults.suggestionChipColors(containerColor = getExerciseMuscleColor(m).copy(alpha = 0.15f)))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, 1000)) + scaleIn(tween(800, 1000), initialScale = 0.85f)) {
            PulsingStartButton(onClick = onStart)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PulsingExerciseIcon(muscleColor: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(1f, 1.12f, infiniteRepeatable(tween(1200), RepeatMode.Reverse))
    val alpha by infiniteTransition.animateFloat(0.3f, 0.6f, infiniteRepeatable(tween(1200), RepeatMode.Reverse))

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
        Box(Modifier.fillMaxSize().scale(scale).background(muscleColor.copy(alpha = alpha), CircleShape))
        Box(Modifier.size(90.dp).background(muscleColor.copy(alpha = 0.25f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.FitnessCenter, null, tint = muscleColor, modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun PulsingStartButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val glow by infiniteTransition.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(1000), RepeatMode.Reverse))

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp).shadow(24.dp, RoundedCornerShape(20.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(10.dp))
        Text("START WORKOUT", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF0D0D0D))
    }
}

@Composable
private fun InfoCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(label, color = TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ACTIVE CONTENT
@Composable
private fun ActiveContent(state: WorkoutSessionUiState, viewModel: WorkoutSessionViewModel, onFinish: () -> Unit) {
    val allSetsCompleted = state.setLogs.size >= state.totalSets && state.isPaused
    val progress = if (state.totalSets > 0) state.setLogs.size.toFloat() / state.totalSets else 0f

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))

        Text("ACTIVE WORKOUT", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        Text(state.exercise?.name ?: "", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)

        Spacer(Modifier.height(28.dp))

        AnimatedTimerCircle(elapsedSeconds = state.elapsedSeconds)

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatBox("Calories", "%.1f".format(state.calories), "kcal", EnergeticOrange, modifier = Modifier.weight(1f))
            StatBox("Set", "${state.setLogs.size}/${state.totalSets}", "sets", ElectricBlue, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        if (allSetsCompleted) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("All Sets Complete!", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("${state.setLogs.size} sets done", color = TextSecondaryDark)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.addExtraSet() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Extra Set", fontWeight = FontWeight.Bold, color = Color(0xFF0D0D0D))
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.showFinishDialog() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ErrorRed)
                    ) {
                        Icon(Icons.Default.Stop, null, tint = ErrorRed)
                        Spacer(Modifier.width(8.dp))
                        Text("Finish Workout", fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Set ${state.currentSet} of ${state.totalSets}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Target: ${state.targetReps} reps @ ${"%.1f".format(state.currentWeight)} kg", color = TextSecondaryDark, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Weight (kg)", color = TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SmallButton("-") { viewModel.updateWeight(state.currentWeight - 2.5f) }
                                Text("%.1f".format(state.currentWeight), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 12.dp))
                                SmallButton("+") { viewModel.updateWeight(state.currentWeight + 2.5f) }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reps", color = TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SmallButton("-") { viewModel.updateReps(state.currentReps - 1) }
                                Text("${state.currentReps}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 12.dp))
                                SmallButton("+") { viewModel.updateReps(state.currentReps + 1) }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.completeSet() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = state.currentReps > 0
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Complete Set", fontWeight = FontWeight.Bold, color = Color(0xFF0D0D0D))
                    }
                }
            }
        }

        if (state.setLogs.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("Completed Sets", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            state.setLogs.forEach { log ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Set ${log.setNumber}", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("${log.reps} reps x ${"%.1f".format(log.weight)} kg", color = TextSecondaryDark)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (state.isRunning) {
                OutlinedButton(onClick = { viewModel.pauseWorkout() }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, TextSecondaryDark)) {
                    Icon(Icons.Default.Pause, null, tint = Color.White); Spacer(Modifier.width(6.dp)); Text("Pause", color = Color.White)
                }
            } else if (state.isPaused) {
                Button(onClick = { viewModel.resumeWorkout() }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))) {
                    Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Resume", color = Color(0xFF0D0D0D), fontWeight = FontWeight.Bold)
                }
            }
            OutlinedButton(onClick = { viewModel.showFinishDialog() }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, ErrorRed)) {
                Icon(Icons.Default.Stop, null, tint = ErrorRed); Spacer(Modifier.width(6.dp)); Text("Finish", color = ErrorRed)
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (state.showFinishDialog) {
        FinishDialog(onConfirm = { viewModel.finishWorkout(onFinish) }, onDismiss = { viewModel.dismissFinishDialog() })
    }
}

@Composable
private fun AnimatedTimerCircle(elapsedSeconds: Int) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeStr = "%02d:%02d".format(minutes, seconds)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color(0xFF1C1C1E), radius = size.minDimension / 2)
            drawCircle(color = Color(0xFF00E5FF).copy(alpha = 0.15f), radius = size.minDimension / 2 - 8f)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(timeStr, color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.displayMedium)
            Text("elapsed", color = TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StatBox(title: String, value: String, unit: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, color = color, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium)
            Text(unit, color = color.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SmallButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(36.dp).background(Color(0xFF2D2D2D), CircleShape).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
}

// REST CONTENT
@Composable
private fun RestContent(state: WorkoutSessionUiState, onSkip: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse))
    val progress = if (state.restDuration > 0) state.restSecondsRemaining.toFloat() / state.restDuration else 0f

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("REST", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        Text("Next: Set ${state.currentSet}", color = Color.White, style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(40.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFFFB300),
                strokeWidth = 8.dp,
                trackColor = Color(0xFF2D2D2D),
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${state.restSecondsRemaining}", color = Color.White.copy(alpha = alpha), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.displayLarge)
                Text("seconds", color = TextSecondaryDark)
            }
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.FastForward, null)
            Spacer(Modifier.width(8.dp))
            Text("Skip Rest", fontWeight = FontWeight.Bold, color = Color(0xFF0D0D0D))
        }
    }
}

// FINISHED CONTENT
@Composable
private fun FinishedContent(state: WorkoutSessionUiState, onFinish: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible = visible, enter = scaleIn(tween(600, 200)) + fadeIn(tween(600, 200))) {
            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD700), modifier = Modifier.size(80.dp))
        }
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(visible = visible, enter = fadeIn(tween(600, 400))) {
            Text("Workout Complete!", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(600, 600)) + slideInVertically(tween(600, 600)) { 30 }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryRow("Duration", "${state.elapsedSeconds / 60}m ${state.elapsedSeconds % 60}s")
                SummaryRow("Calories Burned", "%.1f kcal".format(state.calories))
                SummaryRow("Sets Completed", "${state.setLogs.size}")
                SummaryRow("Total Reps", "${state.setLogs.sumOf { it.reps }}")
                SummaryRow("Total Volume", "%.1f kg".format(state.setLogs.sumOf { (it.weight * it.reps).toDouble() }))
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(600, 800))) {
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Home, null)
                Spacer(Modifier.width(8.dp))
                Text("Back to Dashboard", fontWeight = FontWeight.Bold, color = Color(0xFF0D0D0D), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1C1C1E), RoundedCornerShape(14.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondaryDark)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FinishDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        title = { Text("Finish Workout?", color = Color.White) },
        text = { Text("Are you sure you want to end this workout session?", color = TextSecondaryDark) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Finish", color = ErrorRed, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) } }
    )
}

private fun getExerciseMuscleColor(muscleGroup: MuscleGroup): Color = when (muscleGroup) {
    MuscleGroup.CHEST -> ChestColor
    MuscleGroup.BACK -> BackColor
    MuscleGroup.SHOULDERS -> ShouldersColor
    MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS -> ArmsColor
    MuscleGroup.CORE -> CoreColor
    MuscleGroup.QUADRICEPS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES -> LegsColor
    MuscleGroup.FULL_BODY, MuscleGroup.CARDIO -> CardioColor
}
