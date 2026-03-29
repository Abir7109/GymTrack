@file:OptIn(ExperimentalMaterial3Api::class)

package com.gymtrack.app.presentation.screens.progress

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
import com.gymtrack.app.domain.model.*
import com.gymtrack.app.presentation.components.GlassBottomNavBar
import com.gymtrack.app.presentation.theme.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel(),
    selectedTab: Int = 3,
    onTabSelected: (Int) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val personalRecords by viewModel.personalRecords.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    
    val haptic = LocalHapticFeedback.current
    
    // Use selectedTab directly - this is the source of truth from MainActivity
    val selectedBottomNav = selectedTab
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progress",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddMeasurementDialog() },
                containerColor = ElectricBlue,
                contentColor = DarkBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Measurement")
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time Range Selector
            item {
                TimeRangeSelector(
                    selectedRange = selectedTimeRange,
                    onRangeSelected = { viewModel.updateTimeRange(it) }
                )
            }
            
            // Body Stats Overview
            item {
                BodyStatsOverview(
                    currentWeight = uiState.currentWeight,
                    weightChange = uiState.weightChange,
                    currentBodyFat = uiState.currentBodyFat,
                    bodyFatChange = uiState.bodyFatChange
                )
            }
            
            // Weight Chart
            item {
                WeightProgressChart(measurements = measurements)
            }
            
            // Body Composition
            item {
                BodyCompositionSection(measurements = measurements)
            }
            
            // Personal Records Section
            item {
                PersonalRecordsSection(records = personalRecords)
            }
            
            // Recent Measurements
            item {
                RecentMeasurementsSection(measurements = measurements)
            }
        }
        
        // Add Measurement Dialog
        if (uiState.showAddMeasurementDialog) {
            AddMeasurementDialog(
                onDismiss = { viewModel.hideAddMeasurementDialog() },
                onConfirm = { measurement ->
                    viewModel.addMeasurement(measurement)
                    viewModel.hideAddMeasurementDialog()
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

@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TimeRange.entries.toList()) { range ->
                val isSelected = selectedRange == range
                FilterChip(
                    selected = isSelected,
                    onClick = { onRangeSelected(range) },
                    label = { Text(range.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBlue,
                        selectedLabelColor = DarkBackground,
                        containerColor = Color.Transparent,
                        labelColor = TextSecondaryDark
                    )
                )
            }
        }
    }
}

@Composable
fun BodyStatsOverview(
    currentWeight: Float?,
    weightChange: Float?,
    currentBodyFat: Float?,
    bodyFatChange: Float?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Weight Card
        StatOverviewCard(
            modifier = Modifier.weight(1f),
            title = "Weight",
            value = currentWeight?.let { "${it.toInt()} kg" } ?: "-- kg",
            change = weightChange?.let { 
                val prefix = if (it > 0) "+" else ""
                "$prefix${it.toInt()} kg"
            },
            icon = Icons.Default.Scale,
            iconColor = NeonCyan
        )
        
        // Body Fat Card
        StatOverviewCard(
            modifier = Modifier.weight(1f),
            title = "Body Fat",
            value = currentBodyFat?.let { "${it.toInt()}%" } ?: "--%",
            change = bodyFatChange?.let {
                val prefix = if (it > 0) "+" else ""
                "$prefix${it.toInt()}%"
            },
            icon = Icons.Default.Percent,
            iconColor = ElectricPurple
        )
    }
}

@Composable
fun StatOverviewCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    change: String?,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
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
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryDark
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            
            change?.let {
                val isPositive = it.startsWith("+")
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPositive) ErrorRed else SuccessGreen
                )
            }
        }
    }
}

@Composable
fun WeightProgressChart(measurements: List<BodyMeasurement>) {
    val sortedMeasurements = measurements.sortedBy { it.recordedAt }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (sortedMeasurements.isEmpty()) {
                EmptyChartMessage(message = "No weight data yet")
            } else {
                // Simple bar chart visualization
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val weights = sortedMeasurements.takeLast(7).mapNotNull { it.weight }
                    val maxWeight = weights.maxOrNull() ?: 100f
                    val minWeight = weights.minOrNull() ?: 50f
                    val range = (maxWeight - minWeight).coerceAtLeast(10f)
                    
                    weights.forEachIndexed { index, weight ->
                        val height = ((weight - minWeight) / range * 120).coerceAtLeast(10f)
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(height.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(ElectricBlue, NeonCyan)
                                        ),
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = weight.toInt().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
                
                // Legend
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    sortedMeasurements.lastOrNull()?.let {
                        Text(
                            text = formatDate(it.recordedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Text(
                        text = "kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun BodyCompositionSection(measurements: List<BodyMeasurement>) {
    val latestMeasurement = measurements.firstOrNull()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Body Measurements",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            if (latestMeasurement == null) {
                EmptyChartMessage(message = "Add your first measurement")
            } else {
                // Measurement grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MeasurementItem(
                        modifier = Modifier.weight(1f),
                        label = "Chest",
                        value = latestMeasurement.chest,
                        unit = "cm"
                    )
                    MeasurementItem(
                        modifier = Modifier.weight(1f),
                        label = "Waist",
                        value = latestMeasurement.waist,
                        unit = "cm"
                    )
                    MeasurementItem(
                        modifier = Modifier.weight(1f),
                        label = "Hips",
                        value = latestMeasurement.hips,
                        unit = "cm"
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MeasurementItem(
                        modifier = Modifier.weight(1f),
                        label = "Biceps",
                        value = latestMeasurement.biceps,
                        unit = "cm"
                    )
                    MeasurementItem(
                        modifier = Modifier.weight(1f),
                        label = "Thighs",
                        value = latestMeasurement.thighs,
                        unit = "cm"
                    )
                    MeasurementItem(
                        modifier = Modifier.weight(1f),
                        label = "Calves",
                        value = latestMeasurement.calves,
                        unit = "cm"
                    )
                }
            }
        }
    }
}

@Composable
fun MeasurementItem(
    modifier: Modifier = Modifier,
    label: String,
    value: Float?,
    unit: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value?.let { "${it.toInt()}" } ?: "--",
            style = MaterialTheme.typography.titleLarge,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$label ($unit)",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
fun PersonalRecordsSection(records: List<PersonalRecord>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Records",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = GoldColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (records.isEmpty()) {
                EmptyChartMessage(message = "No PRs yet - start lifting!")
            } else {
                records.take(5).forEach { record ->
                    PersonalRecordItem(record = record)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PersonalRecordItem(record: PersonalRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = GoldColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimaryDark,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formatDateTime(record.achievedAt),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${record.weight.toInt()} kg",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${record.reps} reps",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark
            )
        }
    }
}

@Composable
fun RecentMeasurementsSection(measurements: List<BodyMeasurement>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Measurements",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            if (measurements.isEmpty()) {
                EmptyChartMessage(message = "No measurements recorded")
            } else {
                measurements.take(5).forEach { measurement ->
                    MeasurementHistoryItem(measurement = measurement)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MeasurementHistoryItem(measurement: BodyMeasurement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = formatDate(measurement.recordedAt),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimaryDark
            )
            measurement.weight?.let {
                Text(
                    text = "${it.toInt()} kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark
                )
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            measurement.bodyFatPercentage?.let {
                Text(
                    text = "${it.toInt()}% BF",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricPurple
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }
}

@Composable
fun EmptyChartMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.BarChart,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementDialog(
    onDismiss: () -> Unit,
    onConfirm: (BodyMeasurement) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }
    var biceps by remember { mutableStateOf("") }
    var thighs by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDateTime.now()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Measurement", color = TextPrimaryDark)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date selector
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    colors = CardDefaults.outlinedCardColors(containerColor = DarkCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(selectedDate),
                            color = TextPrimaryDark
                        )
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = NeonCyan
                        )
                    }
                }
                
                // Weight
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCard,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    singleLine = true
                )
                
                // Body Fat
                OutlinedTextField(
                    value = bodyFat,
                    onValueChange = { bodyFat = it },
                    label = { Text("Body Fat (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCard,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    singleLine = true
                )
                
                // Body Measurements
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = chest,
                        onValueChange = { chest = it },
                        label = { Text("Chest") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCard,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = waist,
                        onValueChange = { waist = it },
                        label = { Text("Waist") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCard,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = biceps,
                        onValueChange = { biceps = it },
                        label = { Text("Biceps") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCard,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = thighs,
                        onValueChange = { thighs = it },
                        label = { Text("Thighs") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCard,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val measurement = BodyMeasurement(
                        recordedAt = selectedDate,
                        weight = weight.toFloatOrNull(),
                        bodyFatPercentage = bodyFat.toFloatOrNull(),
                        muscleMass = null,
                        waterPercentage = null,
                        chest = chest.toFloatOrNull(),
                        waist = waist.toFloatOrNull(),
                        hips = hips.toFloatOrNull(),
                        biceps = biceps.toFloatOrNull(),
                        thighs = thighs.toFloatOrNull(),
                        calves = null,
                        neck = null,
                        shoulders = null
                    )
                    onConfirm(measurement)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = DarkBackground
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        },
        containerColor = DarkSurface
    )
}

private fun formatDate(date: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return date.format(formatter)
}

private fun formatDateTime(date: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return date.format(formatter)
}
