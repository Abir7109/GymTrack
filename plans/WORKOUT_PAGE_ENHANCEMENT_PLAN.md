# Workout Page Enhancement Plan

## Current State Analysis

The current workout page has:
- Basic exercise cards with sets, weight, reps input
- Simple rest timer
- Basic workout timer
- No historical data display
- No form visualization
- No muscle heatmap
- No plate calculator
- No RPE input
- No superset support
- No drop-set support
- No post-workout summary

## Implementation Phases

### Phase 1: Session Header (Control Tower) - HIGH PRIORITY
**Goal:** Provide macro view of workout progress

#### 1.1 Overall Progress Bar
- Visual indicator showing percentage of workout completed
- Calculate: (completed sets / total sets) * 100
- Display as horizontal progress bar with percentage text

#### 1.2 Volume Tracker
- Live counter of total weight lifted so far
- Calculate: sum of (weight * reps) for all completed sets
- Display prominently with animated counter
- Update in real-time as sets are completed

#### 1.3 Enhanced Timer Display
- Already have basic timer
- Add visual progress ring around timer
- Add estimated time remaining based on average set duration

**Files to modify:**
- `WorkoutScreen.kt` - Add new UI components
- `WorkoutViewModel.kt` - Add calculation logic
- `WorkoutUiState.kt` - Add new state fields

---

### Phase 2: Exercise Card (Instructional Hub) - MEDIUM PRIORITY
**Goal:** Provide instructional guidance for each exercise

#### 2.1 Historical Reference ("Last Time")
- Show previous session's performance for this exercise
- Display: "Last time: 80kg × 8 reps"
- Requires querying workout history from database

#### 2.2 Muscle Heatmap
- Small anatomical icon showing target muscles
- Primary muscles highlighted in one color
- Secondary muscles in another color
- Use existing muscle group data from Exercise model

#### 2.3 Form Visualization
- Add placeholder for exercise demonstration
- Can be static image or animated GIF
- Store image URLs in Exercise model or use placeholder

#### 2.4 Plate Calculator
- Button to open plate calculator modal
- Input: target weight
- Output: which plates to load on barbell
- Standard plate sizes: 20kg, 15kg, 10kg, 5kg, 2.5kg, 1.25kg

**Files to modify:**
- `WorkoutScreen.kt` - Add new UI components
- `WorkoutViewModel.kt` - Add history query logic
- `Exercise.kt` - Add image URL field if needed
- `WorkoutRepository.kt` - Add method to get previous performance

---

### Phase 3: Logging Table (Data Entry) - HIGH PRIORITY
**Goal:** High-speed, low-friction data entry

#### 3.1 Previous Set Ghosting
- Pre-fill input fields with light "ghost text" showing previous session's performance
- Show as placeholder text in weight/reps fields
- Data comes from historical records

#### 3.2 RPE Input (Rate of Perceived Exertion)
- Add dropdown or slider (1-10) after each set
- Store RPE value with each set
- If RPE = 10, suggest decreasing weight for next set

#### 3.3 Auto-Calculated 1RM
- Real-time calculation of Estimated 1-Rep Max
- Formula: weight × (1 + reps/30) (Epley formula)
- Display below weight/reps inputs
- Update as user types

#### 3.4 Enhanced Set Input
- Larger touch targets for weight/reps
- Swipe gestures for quick adjustments
- Haptic feedback on input

**Files to modify:**
- `WorkoutScreen.kt` - Enhance SetInputRow component
- `WorkoutViewModel.kt` - Add RPE tracking, 1RM calculation
- `ExerciseSetInput.kt` - Add RPE field
- `WorkoutRepository.kt` - Add method to get previous set data

---

### Phase 4: Rest & Recovery Engine - MEDIUM PRIORITY
**Goal:** Smart rest timing based on body needs

#### 4.1 Interstitial Timer
- When set is checked, show rest timer overlay
- Visual countdown with progress ring
- Skip button available

#### 4.2 Sound/Haptic Cues
- 30 seconds left: One short vibration
- 5 seconds left: Three short beeps
- Time up: One long pulse
- Use Android's Vibrator and SoundPool APIs

#### 4.3 Hydration Reminders
- Subtle prompts to drink water
- Based on workout intensity (total volume)
- Show every 10-15 minutes during active workout

#### 4.4 Smart Rest Suggestions
- Suggest rest time based on:
  - Exercise difficulty
  - Weight lifted (heavier = longer rest)
  - RPE of previous set

**Files to modify:**
- `WorkoutScreen.kt` - Enhance rest timer UI
- `WorkoutViewModel.kt` - Add smart rest logic
- `RestTimerService.kt` - Add sound/haptic cues
- `AndroidManifest.xml` - Add vibration permission

---

### Phase 5: Intelligence & Edge Cases - LOW PRIORITY
**Goal:** Handle real-world gym scenarios

#### 5.1 Exercise Swap Feature
- If machine is taken, swap exercise
- Suggest "Equivalent Alternative" based on:
  - Same muscle group
  - Same movement pattern
  - Similar difficulty
- Keep set/rep goals the same

#### 5.2 Superset Logic
- Link two exercises (A1 and A2)
- Visual bridge between cards
- No rest period between superset exercises
- Rest only after completing both

#### 5.3 Drop-Set Support
- Button to add "Special Set"
- Weight lowered immediately after failure
- Track drop-set separately in history

#### 5.4 Warm-up Sets
- Mark sets as warm-up
- Warm-up sets don't count toward volume
- Different visual styling

**Files to modify:**
- `WorkoutScreen.kt` - Add swap UI, superset UI
- `WorkoutViewModel.kt` - Add swap logic, superset logic
- `WorkoutExerciseItem.kt` - Add superset fields
- `ExerciseSetInput.kt` - Add warm-up field
- `ExerciseRepository.kt` - Add method to find alternatives

---

### Phase 6: Post-Workout Summary - HIGH PRIORITY
**Goal:** Celebrate achievements and track progress

#### 6.1 New Personal Records (PRs)
- Highlight any records broken during session
- Show badges/celebrations
- Compare with previous bests

#### 6.2 Total Tonnage Summary
- Display total weight lifted
- Show breakdown by exercise
- Animated counter for dramatic effect

#### 6.3 Workout Duration
- Total time spent
- Average set duration
- Rest time percentage

#### 6.4 Notes/Journaling
- Text field for workout notes
- "Felt tired today, lower back stiff"
- Show notes next time this workout is started
- Store in database with workout

#### 6.5 Muscle Group Summary
- Show which muscle groups were trained
- Volume per muscle group
- Suggest recovery time

**Files to modify:**
- `WorkoutScreen.kt` - Add summary screen
- `WorkoutViewModel.kt` - Add PR detection, summary calculation
- `WorkoutRepository.kt` - Add method to save notes
- `Workout.kt` - Add notes field
- New file: `WorkoutSummaryScreen.kt`

---

## Data Model Changes

### ExerciseSetInput (add fields)
```kotlin
data class ExerciseSetInput(
    val setNumber: Int,
    val weight: Float = 0f,
    val reps: Int = 0,
    val isCompleted: Boolean = false,
    val isWarmup: Boolean = false,
    val rpe: Int? = null,  // NEW: Rate of Perceived Exertion (1-10)
    val isDropSet: Boolean = false  // NEW: Drop-set indicator
)
```

### WorkoutExerciseItem (add fields)
```kotlin
data class WorkoutExerciseItem(
    val exercise: Exercise,
    val sets: List<ExerciseSetInput> = emptyList(),
    val restTimeSeconds: Int = 90,
    val notes: String = "",
    val supersetPartnerId: Long? = null,  // NEW: Superset partner
    val previousPerformance: PreviousPerformance? = null  // NEW: Historical data
)
```

### WorkoutUiState (add fields)
```kotlin
data class WorkoutUiState(
    val isLoading: Boolean = false,
    val isWorkoutActive: Boolean = false,
    val workoutName: String = "",
    val currentWorkoutId: Long? = null,
    val workoutExercises: List<WorkoutExerciseItem> = emptyList(),
    val showExercisePicker: Boolean = false,
    val error: String? = null,
    // NEW fields
    val totalVolume: Float = 0f,  // Total weight lifted
    val completionPercentage: Float = 0f,  // Workout progress
    val estimatedTimeRemaining: Int = 0,  // Seconds
    val showSummary: Boolean = false,  // Show post-workout summary
    val newPRs: List<PersonalRecord> = emptyList()  // New records achieved
)
```

### Exercise (add fields)
```kotlin
data class Exercise(
    val id: Long = 0,
    val name: String,
    val description: String,
    val instructions: String,
    val muscleGroup: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: Equipment,
    val difficulty: Difficulty,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val imageUrl: String? = null,  // NEW: Form visualization
    val videoUrl: String? = null,  // NEW: Video demonstration
    val plateCalculation: Boolean = false  // NEW: Whether to show plate calculator
)
```

---

## Implementation Order (Recommended)

### Sprint 1 (Core Functionality)
1. Phase 1: Session Header (Progress bar, Volume tracker)
2. Phase 3: Logging Table (Previous set ghosting, RPE input, 1RM calculation)
3. Phase 6: Post-Workout Summary (Basic summary, PR detection)

### Sprint 2 (Enhanced Experience)
1. Phase 2: Exercise Card (Historical reference, Muscle heatmap)
2. Phase 4: Rest & Recovery Engine (Smart rest, Sound/haptic cues)
3. Phase 6: Post-Workout Summary (Notes/journaling)

### Sprint 3 (Advanced Features)
1. Phase 2: Exercise Card (Plate calculator, Form visualization)
2. Phase 5: Intelligence (Exercise swap, Superset logic)
3. Phase 5: Intelligence (Drop-set support, Warm-up sets)

---

## Technical Considerations

### Performance
- Cache historical data to avoid repeated database queries
- Use Flow for reactive updates
- Debounce input fields to avoid excessive calculations

### Accessibility
- Large touch targets (minimum 48dp)
- High contrast for important information
- Screen reader support for all components
- Haptic feedback for confirmations

### Testing
- Unit tests for calculation logic (1RM, volume, progress)
- UI tests for input validation
- Integration tests for database queries

### Dependencies
- Lottie for animations (optional)
- SoundPool for audio cues
- Vibrator API for haptic feedback

---

## Estimated Effort

- **Phase 1:** 2-3 days
- **Phase 2:** 3-4 days
- **Phase 3:** 2-3 days
- **Phase 4:** 2-3 days
- **Phase 5:** 4-5 days
- **Phase 6:** 2-3 days

**Total:** 15-21 days for full implementation

---

## Next Steps

1. Review this plan with stakeholders
2. Prioritize phases based on user feedback
3. Create detailed technical specifications for each phase
4. Begin implementation starting with Phase 1
5. Conduct user testing after each phase
6. Iterate based on feedback
