@file:OptIn(ExperimentalMaterial3Api::class)

package com.gymtrack.app.presentation.screens.library

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtrack.app.domain.model.*
import com.gymtrack.app.domain.repository.ExerciseRepository
import com.gymtrack.app.presentation.components.ExerciseImage
import com.gymtrack.app.presentation.components.ExerciseImageLarge
import com.gymtrack.app.presentation.components.GlassBottomNavBar
import com.gymtrack.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    selectedTab: Int = 1,
    onTabSelected: (Int) -> Unit = {},
    onNavigateBack: () -> Unit,
    onStartWorkoutWithExercise: (Exercise) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val haptic = LocalHapticFeedback.current
    
    // Group exercises by muscle group
    val groupedExercises = uiState.filteredExercises.groupBy { it.muscleGroup }
    
    // Use selectedTab directly - this is the source of truth from MainActivity
    val selectedBottomNav = selectedTab
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Exercise Library",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimaryDark)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavoriteOnly() }) {
                        Icon(
                            if (uiState.showFavoritesOnly) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (uiState.showFavoritesOnly) HotPink else TextSecondaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = { },
                active = false,
                onActiveChange = { },
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = SearchBarDefaults.colors(
                    containerColor = DarkCard
                ),
                shape = RoundedCornerShape(16.dp)
            ) {}
            
            // Filter Chips
            FilterSection(
                selectedMuscleGroup = uiState.selectedMuscleGroup,
                selectedEquipment = uiState.selectedEquipment,
                selectedDifficulty = uiState.selectedDifficulty,
                onMuscleGroupSelected = { viewModel.updateSelectedMuscleGroup(it) },
                onEquipmentSelected = { viewModel.updateSelectedEquipment(it) },
                onDifficultySelected = { viewModel.updateSelectedDifficulty(it) }
            )
            
            // Exercise Count
            Text(
                text = "${uiState.filteredExercises.size} exercises",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondaryDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Exercises List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedExercises.forEach { (muscleGroup, muscleExercises) ->
                    item {
                        MuscleGroupHeader(muscleGroup = muscleGroup, count = muscleExercises.size)
                    }
                    
                    items(
                        items = muscleExercises,
                        key = { it.id }
                    ) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onFavoriteClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleFavorite(exercise.id)
                            },
                            onClick = { viewModel.selectExercise(exercise) }
                        )
                    }
                }
            }
        }
        
        // Exercise Detail Bottom Sheet
        uiState.selectedExercise?.let { exercise ->
            ExerciseDetailBottomSheet(
                exercise = exercise,
                onDismiss = { viewModel.clearSelectedExercise() },
                onFavoriteClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleFavorite(exercise.id)
                },
                onStartWorkout = { selectedExercise ->
                    onStartWorkoutWithExercise(selectedExercise)
                }
            )
        }
        
        // Bottom Navigation
        GlassBottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    placeholder: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = DarkCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    placeholder()
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        color = TextPrimaryDark,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            trailingIcon()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    selectedMuscleGroup: MuscleGroup?,
    selectedEquipment: Equipment?,
    selectedDifficulty: Difficulty?,
    onMuscleGroupSelected: (MuscleGroup?) -> Unit,
    onEquipmentSelected: (Equipment?) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit
) {
    var showMuscleFilter by remember { mutableStateOf(false) }
    var showEquipmentFilter by remember { mutableStateOf(false) }
    var showDifficultyFilter by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // Muscle Group Filter
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selectedMuscleGroup != null,
                    onClick = { showMuscleFilter = !showMuscleFilter },
                    label = { 
                        Text(selectedMuscleGroup?.name?.replace("_", " ") ?: "Muscle Group") 
                    },
                    trailingIcon = {
                        Icon(
                            if (showMuscleFilter) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBlue.copy(alpha = 0.3f),
                        selectedLabelColor = NeonCyan,
                        containerColor = DarkCard,
                        labelColor = TextSecondaryDark
                    )
                )
            }
            
            item {
                FilterChip(
                    selected = selectedEquipment != null,
                    onClick = { showEquipmentFilter = !showEquipmentFilter },
                    label = { 
                        Text(selectedEquipment?.name?.replace("_", " ") ?: "Equipment") 
                    },
                    trailingIcon = {
                        Icon(
                            if (showEquipmentFilter) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricPurple.copy(alpha = 0.3f),
                        selectedLabelColor = NeonPurple,
                        containerColor = DarkCard,
                        labelColor = TextSecondaryDark
                    )
                )
            }
            
            item {
                FilterChip(
                    selected = selectedDifficulty != null,
                    onClick = { showDifficultyFilter = !showDifficultyFilter },
                    label = { 
                        Text(selectedDifficulty?.name ?: "Level") 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EnergeticOrange.copy(alpha = 0.3f),
                        selectedLabelColor = NeonOrange,
                        containerColor = DarkCard,
                        labelColor = TextSecondaryDark
                    )
                )
            }
        }
        
        // Expanded Filters
        AnimatedVisibility(visible = showMuscleFilter) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedMuscleGroup == null,
                        onClick = { onMuscleGroupSelected(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue.copy(alpha = 0.3f),
                            selectedLabelColor = NeonCyan,
                            containerColor = DarkCard,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
                items(MuscleGroup.entries.toList()) { muscle ->
                    FilterChip(
                        selected = selectedMuscleGroup == muscle,
                        onClick = { onMuscleGroupSelected(muscle) },
                        label = { Text(muscle.name.replace("_", " ")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue.copy(alpha = 0.3f),
                            selectedLabelColor = NeonCyan,
                            containerColor = DarkCard,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
            }
        }
        
        AnimatedVisibility(visible = showEquipmentFilter) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedEquipment == null,
                        onClick = { onEquipmentSelected(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple.copy(alpha = 0.3f),
                            selectedLabelColor = NeonPurple,
                            containerColor = DarkCard,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
                items(Equipment.entries.toList()) { equipment ->
                    FilterChip(
                        selected = selectedEquipment == equipment,
                        onClick = { onEquipmentSelected(equipment) },
                        label = { Text(equipment.name.replace("_", " ")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple.copy(alpha = 0.3f),
                            selectedLabelColor = NeonPurple,
                            containerColor = DarkCard,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
            }
        }
        
        AnimatedVisibility(visible = showDifficultyFilter) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDifficulty == null,
                    onClick = { onDifficultySelected(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EnergeticOrange.copy(alpha = 0.3f),
                        selectedLabelColor = NeonOrange,
                        containerColor = DarkCard,
                        labelColor = TextSecondaryDark
                    )
                )
                Difficulty.entries.forEach { difficulty ->
                    FilterChip(
                        selected = selectedDifficulty == difficulty,
                        onClick = { onDifficultySelected(difficulty) },
                        label = { Text(difficulty.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EnergeticOrange.copy(alpha = 0.3f),
                            selectedLabelColor = NeonOrange,
                            containerColor = DarkCard,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MuscleGroupHeader(muscleGroup: MuscleGroup, count: Int) {
    val color = when (muscleGroup) {
        MuscleGroup.CHEST -> ChestColor
        MuscleGroup.BACK -> BackColor
        MuscleGroup.QUADRICEPS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES -> LegsColor
        MuscleGroup.SHOULDERS -> ShouldersColor
        MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS -> ArmsColor
        MuscleGroup.CORE -> CoreColor
        else -> ElectricBlue
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = muscleGroup.name.replace("_", " "),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    val difficultyColor = when (exercise.difficulty) {
        Difficulty.BEGINNER -> SuccessGreen
        Difficulty.INTERMEDIATE -> WarningYellow
        Difficulty.ADVANCED -> ErrorRed
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise Image
            ExerciseImage(exercise = exercise, size = 48.dp)
            
            Spacer(Modifier.width(12.dp))
            
            // Exercise Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Difficulty
                    Box(
                        modifier = Modifier
                            .background(
                                difficultyColor.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = exercise.difficulty.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = difficultyColor
                        )
                    }
                    
                    // Equipment
                    Text(
                        text = exercise.equipment.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
            
            // Favorite Button
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    if (exercise.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (exercise.isFavorite) HotPink else TextMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailBottomSheet(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
    onStartWorkout: (Exercise) -> Unit
) {
    val difficultyColor = when (exercise.difficulty) {
        Difficulty.BEGINNER -> SuccessGreen
        Difficulty.INTERMEDIATE -> WarningYellow
        Difficulty.ADVANCED -> ErrorRed
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Exercise Image at top
            ExerciseImageLarge(
                exercise = exercise,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        // Difficulty Badge
                        AssistChip(
                            onClick = { },
                            label = { Text(exercise.difficulty.name) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = difficultyColor.copy(alpha = 0.2f),
                                labelColor = difficultyColor
                            )
                        )
                        
                        // Equipment Badge
                        AssistChip(
                            onClick = { },
                            label = { Text(exercise.equipment.name.replace("_", " ")) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = ElectricBlue.copy(alpha = 0.2f),
                                labelColor = ElectricBlue
                            )
                        )
                    }
                }
                
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        if (exercise.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (exercise.isFavorite) HotPink else TextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Description
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryDark
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Instructions
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = exercise.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimaryDark,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Muscle Groups
            Text(
                text = "Target Muscles",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = { },
                    label = { Text(exercise.muscleGroup.name.replace("_", " ")) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = getMuscleGroupColor(exercise.muscleGroup).copy(alpha = 0.2f),
                        labelColor = getMuscleGroupColor(exercise.muscleGroup)
                    )
                )
                
                exercise.secondaryMuscles.forEach { muscle ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(muscle.name.replace("_", " ")) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = getMuscleGroupColor(muscle).copy(alpha = 0.2f),
                            labelColor = getMuscleGroupColor(muscle)
                        )
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Add to Workout Button
            Button(
                onClick = { onStartWorkout(exercise) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start Workout with This", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

fun getMuscleGroupColor(muscleGroup: MuscleGroup): Color {
    return when (muscleGroup) {
        MuscleGroup.CHEST -> ChestColor
        MuscleGroup.BACK -> BackColor
        MuscleGroup.SHOULDERS -> ShouldersColor
        MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS -> ArmsColor
        MuscleGroup.CORE -> CoreColor
        MuscleGroup.QUADRICEPS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES -> LegsColor
        MuscleGroup.FULL_BODY, MuscleGroup.CARDIO -> CardioColor
    }
}
