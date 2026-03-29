package com.gymtrack.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gymtrack.app.domain.model.Exercise
import com.gymtrack.app.domain.model.MuscleGroup
import com.gymtrack.app.presentation.theme.*

fun getMuscleGroupGradient(muscleGroup: MuscleGroup): List<Color> = when (muscleGroup) {
    MuscleGroup.CHEST -> listOf(Color(0xFFE53935), Color(0xFFB71C1C))
    MuscleGroup.BACK -> listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
    MuscleGroup.SHOULDERS -> listOf(Color(0xFFFF9800), Color(0xFFE65100))
    MuscleGroup.BICEPS -> listOf(Color(0xFF8E24AA), Color(0xFF4A148C))
    MuscleGroup.TRICEPS -> listOf(Color(0xFF8E24AA), Color(0xFF6A1B9A))
    MuscleGroup.FOREARMS -> listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))
    MuscleGroup.CORE -> listOf(Color(0xFF00ACC1), Color(0xFF006064))
    MuscleGroup.QUADRICEPS -> listOf(Color(0xFF43A047), Color(0xFF1B5E20))
    MuscleGroup.HAMSTRINGS -> listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
    MuscleGroup.GLUTES -> listOf(Color(0xFF558B2F), Color(0xFF33691E))
    MuscleGroup.CALVES -> listOf(Color(0xFF33691E), Color(0xFF1B5E20))
    MuscleGroup.FULL_BODY -> listOf(Color(0xFFFF6B35), Color(0xFFBF360C))
    MuscleGroup.CARDIO -> listOf(Color(0xFFFF1744), Color(0xFFD50000))
}

fun getExerciseIcon(exerciseName: String): ImageVector = when {
    exerciseName.contains("Bench", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Incline", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Fly", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Push-up", ignoreCase = true) -> Icons.Default.SportsGymnastics
    exerciseName.contains("Pull-up", ignoreCase = true) -> Icons.Default.SportsGymnastics
    exerciseName.contains("Row", ignoreCase = true) -> Icons.Default.Rowing
    exerciseName.contains("Lat Pulldown", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Deadlift", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Overhead Press", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Lateral", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Curl", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Pushdown", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Dips", ignoreCase = true) -> Icons.Default.SportsGymnastics
    exerciseName.contains("Squat", ignoreCase = true) -> Icons.Default.DirectionsRun
    exerciseName.contains("Leg Press", ignoreCase = true) -> Icons.Default.DirectionsRun
    exerciseName.contains("Lunge", ignoreCase = true) -> Icons.Default.DirectionsRun
    exerciseName.contains("Romanian", ignoreCase = true) -> Icons.Default.FitnessCenter
    exerciseName.contains("Leg Curl", ignoreCase = true) -> Icons.Default.DirectionsRun
    exerciseName.contains("Hip Thrust", ignoreCase = true) -> Icons.Default.SportsGymnastics
    exerciseName.contains("Plank", ignoreCase = true) -> Icons.Default.SelfImprovement
    exerciseName.contains("Crunch", ignoreCase = true) -> Icons.Default.SelfImprovement
    exerciseName.contains("Running", ignoreCase = true) -> Icons.Default.DirectionsRun
    exerciseName.contains("Jump Rope", ignoreCase = true) -> Icons.Default.DirectionsRun
    exerciseName.contains("Walking", ignoreCase = true) -> Icons.Default.DirectionsWalk
    exerciseName.contains("Yoga", ignoreCase = true) -> Icons.Default.SelfImprovement
    exerciseName.contains("Circuit", ignoreCase = true) -> Icons.Default.SportsKabaddi
    else -> Icons.Default.FitnessCenter
}

@Composable
fun ExerciseImage(
    exercise: Exercise,
    size: Dp = 48.dp,
    cornerRadius: Dp = 12.dp,
    iconSize: Dp = 24.dp
) {
    val gradient = getMuscleGroupGradient(exercise.muscleGroup)
    val icon = getExerciseIcon(exercise.name)

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = gradient,
                    start = Offset.Zero,
                    end = Offset(size.value, size.value)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Athlete silhouette effect via canvas
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.toPx() / 2
            val centerY = size.toPx() / 2

            // Subtle radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = size.toPx() * 0.4f
                ),
                radius = size.toPx() * 0.4f,
                center = Offset(centerX, centerY)
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = exercise.name,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun ExerciseImageLarge(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    cornerRadius: Dp = 16.dp
) {
    val gradient = getMuscleGroupGradient(exercise.muscleGroup)
    val icon = getExerciseIcon(exercise.name)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = gradient,
                    start = Offset.Zero,
                    end = Offset(200f, 200f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative circles
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Large circle - top right
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = w * 0.3f,
                center = Offset(w * 0.8f, h * 0.2f)
            )

            // Medium circle - bottom left
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = w * 0.2f,
                center = Offset(w * 0.15f, h * 0.75f)
            )

            // Small circle - center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w / 2, h / 2),
                    radius = w * 0.25f
                ),
                radius = w * 0.25f,
                center = Offset(w / 2, h / 2)
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = exercise.name,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(48.dp)
        )
    }
}
