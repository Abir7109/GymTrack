package com.gymtrack.app.presentation.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtrack.app.domain.model.*
import com.gymtrack.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    selectedTab: Int = 5,
    onTabSelected: (Int) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showRestTimerPicker by remember { mutableStateOf(false) }
    var showMeasurementPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.titleLarge, color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimaryDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileHeader(user = user, onEditClick = { showEditDialog = true })
            }
            item { StatsSummary(user = user) }
            item { GoalsSection(user = user) }
            item {
                SettingsSection(
                    settings = settings,
                    onDarkModeChange = { viewModel.updateDarkMode(it) },
                    onNotificationsChange = { viewModel.updateNotifications(it) },
                    onAutoStartTimerChange = { viewModel.updateAutoStartTimer(it) },
                    onRestTimerClick = { showRestTimerPicker = true },
                    onMeasurementClick = { showMeasurementPicker = true }
                )
            }
            item {
                AccountSection(onSignOut = { showSignOutDialog = true })
            }
            item { AppInfoSection() }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showEditDialog && user != null) {
        EditProfileDialog(
            user = user!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedUser ->
                viewModel.updateUser(updatedUser)
                showEditDialog = false
            }
        )
    }

    if (showRestTimerPicker) {
        RestTimerPickerDialog(
            currentValue = settings.restTimerDefault,
            onDismiss = { showRestTimerPicker = false },
            onConfirm = {
                viewModel.updateRestTimerDefault(it)
                showRestTimerPicker = false
            }
        )
    }

    if (showMeasurementPicker) {
        MeasurementUnitDialog(
            currentUnit = settings.measurementUnit,
            onDismiss = { showMeasurementPicker = false },
            onConfirm = {
                viewModel.updateMeasurementUnit(it)
                showMeasurementPicker = false
            }
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out", color = TextPrimaryDark) },
            text = { Text("This will delete all your data and restart onboarding. Continue?", color = TextSecondaryDark) },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel", color = TextSecondaryDark) }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun ProfileHeader(user: User?, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(100.dp).background(
                    brush = Brush.radialGradient(colors = listOf(ElectricBlue, ElectricPurple.copy(alpha = 0.5f))),
                    shape = CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.name?.take(2)?.uppercase() ?: "GT",
                    style = MaterialTheme.typography.headlineLarge,
                    color = DarkBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(user?.name ?: "Guest User", style = MaterialTheme.typography.headlineSmall, color = TextPrimaryDark, fontWeight = FontWeight.Bold)

            user?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(it.experienceLevel.name.lowercase().replaceFirstChar { c -> c.uppercase() }) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = ElectricBlue.copy(alpha = 0.2f), labelColor = NeonCyan)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(it.primaryGoal.name.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() }) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = ElectricPurple.copy(alpha = 0.2f), labelColor = NeonPurple)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (it.height != null) {
                        Text("${it.height!!.toInt()} cm", color = TextSecondaryDark, style = MaterialTheme.typography.bodySmall)
                    }
                    if (it.weight != null) {
                        Text("${it.weight!!.toInt()} kg", color = TextSecondaryDark, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onEditClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                border = BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile")
            }
        }
    }
}

@Composable
fun StatsSummary(user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStatItem(value = user?.weeklyWorkoutDays?.toString() ?: "--", label = "Days/Week", icon = Icons.Default.CalendarMonth, color = NeonCyan)
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = DarkSurfaceVariant)
            ProfileStatItem(value = user?.preferredWorkoutStyles?.size?.toString() ?: "--", label = "Styles", icon = Icons.Default.FitnessCenter, color = ElectricPurple)
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = DarkSurfaceVariant)
            ProfileStatItem(value = user?.height?.toInt()?.toString() ?: "--", label = "Height (cm)", icon = Icons.Default.Height, color = EnergeticOrange)
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = DarkSurfaceVariant)
            ProfileStatItem(value = user?.weight?.toInt()?.toString() ?: "--", label = "Weight (kg)", icon = Icons.Default.Scale, color = SuccessGreen)
        }
    }
}

@Composable
fun ProfileStatItem(value: String, label: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@Composable
fun GoalsSection(user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Current Goal", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Flag, contentDescription = null, tint = NeonCyan)
            }
            Spacer(Modifier.height(12.dp))
            user?.let { currentUser ->
                val goalDesc = when (currentUser.primaryGoal) {
                    FitnessGoal.MUSCLE_GAIN -> "Build muscle and increase strength"
                    FitnessGoal.FAT_LOSS -> "Burn fat and get lean"
                    FitnessGoal.STRENGTH -> "Maximize strength gains"
                    FitnessGoal.MAINTENANCE -> "Maintain current physique"
                    FitnessGoal.FLEXIBILITY -> "Improve flexibility and mobility"
                    FitnessGoal.ENDURANCE -> "Build cardiovascular endurance"
                    FitnessGoal.ATHLETIC_PERFORMANCE -> "Improve athletic performance"
                }
                Card(colors = CardDefaults.cardColors(containerColor = ElectricBlue.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(currentUser.primaryGoal.name.replace("_", " "), style = MaterialTheme.typography.titleMedium, color = NeonCyan, fontWeight = FontWeight.Bold)
                        Text(goalDesc, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryDark, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    settings: ProfileSettings,
    onDarkModeChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onAutoStartTimerChange: (Boolean) -> Unit,
    onRestTimerClick: () -> Unit,
    onMeasurementClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Settings", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            SettingsToggleItem(Icons.Default.DarkMode, "Dark Mode", "Use dark theme", settings.darkModeEnabled, onDarkModeChange)
            Divider(color = DarkSurfaceVariant)
            SettingsToggleItem(Icons.Default.Notifications, "Notifications", "Workout reminders", settings.notificationsEnabled, onNotificationsChange)
            Divider(color = DarkSurfaceVariant)
            SettingsToggleItem(Icons.Default.Timer, "Auto Start Timer", "Start rest timer automatically", settings.autoStartTimer, onAutoStartTimerChange)
            Divider(color = DarkSurfaceVariant)
            SettingsClickableItem(Icons.Default.Timer, "Default Rest Time", "${settings.restTimerDefault} seconds", onRestTimerClick)
            Divider(color = DarkSurfaceVariant)
            SettingsClickableItem(Icons.Default.Straighten, "Measurement Unit", settings.measurementUnit, onMeasurementClick)
        }
    }
}

@Composable
private fun SettingsToggleItem(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = TextPrimaryDark)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = ElectricBlue.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun SettingsClickableItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = TextPrimaryDark)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
fun AccountSection(onSignOut: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Account", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            SettingsClickableItem(Icons.Default.Info, "About", "GymTrack v1.0.0", {})
            Divider(color = DarkSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { onSignOut() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Text("Sign Out", style = MaterialTheme.typography.bodyLarge, color = ErrorRed)
            }
        }
    }
}

@Composable
fun AppInfoSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(8.dp))
            Text("GymTrack", style = MaterialTheme.typography.titleLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
        }
        Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = TextMuted)
    }
}

@Composable
fun EditProfileDialog(user: User, onDismiss: () -> Unit, onSave: (User) -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var height by remember { mutableStateOf(user.height ?: 170f) }
    var weight by remember { mutableStateOf(user.weight ?: 70f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", color = TextPrimaryDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan, unfocusedBorderColor = DarkCard,
                        focusedTextColor = TextPrimaryDark, unfocusedTextColor = TextPrimaryDark,
                        focusedLabelColor = NeonCyan, unfocusedLabelColor = TextSecondaryDark,
                        cursorColor = NeonCyan
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Text("Height: ${height.toInt()} cm", color = NeonCyan, fontWeight = FontWeight.Bold)
                Slider(
                    value = height,
                    onValueChange = { height = it },
                    valueRange = 120f..220f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = ElectricBlue, inactiveTrackColor = DarkCard)
                )
                Text("Weight: ${weight.toInt()} kg", color = NeonCyan, fontWeight = FontWeight.Bold)
                Slider(
                    value = weight,
                    onValueChange = { weight = it },
                    valueRange = 30f..200f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = ElectricBlue, inactiveTrackColor = DarkCard)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(user.copy(name = name, height = height, weight = weight))
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = DarkBackground)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun RestTimerPickerDialog(currentValue: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var selectedValue by remember { mutableIntStateOf(currentValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Default Rest Time", color = TextPrimaryDark) },
        text = {
            Column {
                Text("${selectedValue}s", style = MaterialTheme.typography.displaySmall, color = NeonCyan, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))
                Slider(
                    value = selectedValue.toFloat(),
                    onValueChange = { selectedValue = it.toInt() },
                    valueRange = 15f..300f,
                    steps = 18,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = ElectricBlue, inactiveTrackColor = DarkCard)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(30, 60, 90, 120, 180).forEach { time ->
                        TextButton(onClick = { selectedValue = time }) {
                            Text("${time}s", color = if (selectedValue == time) NeonCyan else TextSecondaryDark)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedValue) }, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = DarkBackground)) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) } },
        containerColor = DarkSurface
    )
}

@Composable
fun MeasurementUnitDialog(currentUnit: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var selected by remember { mutableStateOf(currentUnit) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Measurement Unit", color = TextPrimaryDark) },
        text = {
            Column {
                listOf("kg", "lbs").forEach { unit ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { selected = unit }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == unit,
                            onClick = { selected = unit },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(unit.uppercase(), color = if (selected == unit) NeonCyan else TextPrimaryDark, fontWeight = if (selected == unit) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected) }, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = DarkBackground)) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) } },
        containerColor = DarkSurface
    )
}
