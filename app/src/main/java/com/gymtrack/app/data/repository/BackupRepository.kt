package com.gymtrack.app.data.repository

import android.content.Context
import android.net.Uri
import com.gymtrack.app.data.local.dao.*
import com.gymtrack.app.data.local.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: ExerciseSetDao,
    private val personalRecordDao: PersonalRecordDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    suspend fun exportToJson(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject()
            
            // Export Users
            val users = userDao.getCurrentUser().first()?.let { listOf(it) } ?: emptyList()
            json.put("users", JSONArray().apply {
                users.forEach { user ->
                    put(JSONObject().apply {
                        put("id", user.id)
                        put("name", user.name)
                        put("email", user.email)
                        put("dateOfBirth", user.dateOfBirth)
                        put("gender", user.gender)
                        put("height", user.height)
                        put("weight", user.weight)
                        put("experienceLevel", user.experienceLevel)
                        put("preferredWorkoutStyles", user.preferredWorkoutStyles)
                        put("primaryGoal", user.primaryGoal)
                        put("createdAt", dateFormat.format(Date(user.createdAt)))
                        put("isOnboardingCompleted", user.isOnboardingCompleted)
                    })
                }
            })

            // Export Exercises
            val exercises = exerciseDao.getAllExercises().first()
            json.put("exercises", JSONArray().apply {
                exercises.forEach { ex ->
                    put(JSONObject().apply {
                        put("id", ex.id)
                        put("name", ex.name)
                        put("description", ex.description)
                        put("instructions", ex.instructions)
                        put("muscleGroup", ex.muscleGroup)
                        put("secondaryMuscles", ex.secondaryMuscles)
                        put("equipment", ex.equipment)
                        put("difficulty", ex.difficulty)
                        put("isCustom", ex.isCustom)
                    })
                }
            })

            // Export Workouts
            val workouts = workoutDao.getAllWorkouts().first()
            json.put("workouts", JSONArray().apply {
                workouts.forEach { wo ->
                    put(JSONObject().apply {
                        put("id", wo.id)
                        put("name", wo.name)
                        put("description", wo.description)
                        put("scheduledDate", wo.scheduledDate)
                        put("startedAt", wo.startedAt)
                        put("completedAt", wo.completedAt)
                        put("durationMinutes", wo.durationMinutes)
                        put("totalVolume", wo.totalVolume)
                        put("totalSets", wo.totalSets)
                    })
                }
            })

            // Export Personal Records
            val prs = personalRecordDao.getAllPersonalRecords().first()
            json.put("personalRecords", JSONArray().apply {
                prs.forEach { pr ->
                    put(JSONObject().apply {
                        put("id", pr.id)
                        put("exerciseId", pr.exerciseId)
                        put("weight", pr.weight)
                        put("reps", pr.reps)
                        put("oneRepMax", pr.oneRepMax)
                        put("achievedAt", dateFormat.format(Date(pr.achievedAt)))
                        put("recordType", pr.recordType)
                    })
                }
            })

            // Write to file
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toString(4).toByteArray())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromJson(uri: Uri): Result<ImportSummary> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                JSONObject(inputStream.bufferedReader().readText())
            } ?: return@withContext Result.failure(Exception("Failed to read file"))

            var usersImported = 0
            var exercisesImported = 0
            var workoutsImported = 0
            var prsImported = 0

            // Import Users
            json.optJSONArray("users")?.let { usersArray ->
                for (i in 0 until usersArray.length()) {
                    val userObj = usersArray.getJSONObject(i)
                    userDao.insertUser(
                        UserEntity(
                            id = 0,
                            name = userObj.getString("name"),
                            email = userObj.optString("email", ""),
                            dateOfBirth = userObj.optLong("dateOfBirth", 0),
                            gender = userObj.optString("gender", "MALE"),
                            height = userObj.optDouble("height", 0.0).toFloat(),
                            weight = userObj.optDouble("weight", 0.0).toFloat(),
                            experienceLevel = userObj.optString("experienceLevel", "INTERMEDIATE"),
                            preferredWorkoutStyles = userObj.optString("preferredWorkoutStyles", ""),
                            primaryGoal = userObj.optString("primaryGoal", "BUILD_MUSCLE"),
                            createdAt = parseDate(userObj.optString("createdAt", "")),
                            isOnboardingCompleted = userObj.optBoolean("isOnboardingCompleted", false)
                        )
                    )
                    usersImported++
                }
            }

            // Import Exercises
            json.optJSONArray("exercises")?.let { exercisesArray ->
                for (i in 0 until exercisesArray.length()) {
                    val exObj = exercisesArray.getJSONObject(i)
                    exerciseDao.insertExercise(
                        ExerciseEntity(
                            id = 0,
                            name = exObj.getString("name"),
                            description = exObj.optString("description", ""),
                            instructions = exObj.optString("instructions", ""),
                            muscleGroup = exObj.optString("muscleGroup", "CHEST"),
                            secondaryMuscles = exObj.optString("secondaryMuscles", ""),
                            equipment = exObj.optString("equipment", "BARBELL"),
                            difficulty = exObj.optString("difficulty", "INTERMEDIATE"),
                            isCustom = exObj.optBoolean("isCustom", true)
                        )
                    )
                    exercisesImported++
                }
            }

            // Import Workouts
            json.optJSONArray("workouts")?.let { workoutsArray ->
                for (i in 0 until workoutsArray.length()) {
                    val woObj = workoutsArray.getJSONObject(i)
                    workoutDao.insertWorkout(
                        WorkoutEntity(
                            id = 0,
                            name = woObj.getString("name"),
                            description = woObj.optString("description", ""),
                            scheduledDate = woObj.optLong("scheduledDate", 0),
                            startedAt = woObj.optLong("startedAt", 0),
                            completedAt = woObj.optLong("completedAt", 0),
                            durationMinutes = woObj.optInt("durationMinutes", 0),
                            totalVolume = woObj.optDouble("totalVolume", 0.0).toFloat(),
                            totalSets = woObj.optInt("totalSets", 0)
                        )
                    )
                    workoutsImported++
                }
            }

            // Import Personal Records
            json.optJSONArray("personalRecords")?.let { prsArray ->
                for (i in 0 until prsArray.length()) {
                    val prObj = prsArray.getJSONObject(i)
                    personalRecordDao.insertRecord(
                        PersonalRecordEntity(
                            id = 0,
                            exerciseId = prObj.getLong("exerciseId"),
                            weight = prObj.getDouble("weight").toFloat(),
                            reps = prObj.getInt("reps"),
                            oneRepMax = prObj.optDouble("oneRepMax", 0.0).toFloat(),
                            achievedAt = parseDate(prObj.optString("achievedAt", "")),
                            recordType = prObj.optString("recordType", "HEAVIEST_WEIGHT")
                        )
                    )
                    prsImported++
                }
            }

            Result.success(
                ImportSummary(
                    usersImported = usersImported,
                    exercisesImported = exercisesImported,
                    workoutsImported = workoutsImported,
                    personalRecordsImported = prsImported
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportWorkoutsToCsv(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val workouts = workoutDao.getAllWorkouts().first()
            val csv = StringBuilder()
            csv.appendLine("Workout Name,Date,Duration (min),Total Volume,Notes")

            workouts.forEach { wo ->
                val startedAt = wo.startedAt?.let { Date(it) } ?: Date()
                val duration = wo.durationMinutes
                csv.appendLine("${wo.name},${dateFormat.format(startedAt)},$duration,${wo.totalVolume},${wo.description}")
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csv.toString().toByteArray())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDatabaseSize(): Long = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("gymtrack.db")
        if (dbFile.exists()) dbFile.length() else 0L
    }

    private fun parseDate(dateString: String): Long {
        return try {
            if (dateString.isNotEmpty()) {
                dateFormat.parse(dateString)?.time ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

data class ImportSummary(
    val usersImported: Int,
    val exercisesImported: Int,
    val workoutsImported: Int,
    val personalRecordsImported: Int
)
