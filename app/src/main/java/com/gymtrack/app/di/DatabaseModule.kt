package com.gymtrack.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymtrack.app.data.local.dao.*
import com.gymtrack.app.data.local.database.GymTrackDatabase
import com.gymtrack.app.data.local.database.getDefaultExercises
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymTrackDatabase {
        return Room.databaseBuilder(
            context,
            GymTrackDatabase::class.java,
            "gymtrack_database"
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed exercises when database is first created
                    CoroutineScope(Dispatchers.IO).launch {
                        val exerciseDao = Room.databaseBuilder(
                            context,
                            GymTrackDatabase::class.java,
                            "gymtrack_database"
                        ).build().exerciseDao()
                        exerciseDao.insertExercises(getDefaultExercises())
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideUserDao(database: GymTrackDatabase): UserDao = database.userDao()

    @Provides
    fun provideExerciseDao(database: GymTrackDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideWorkoutDao(database: GymTrackDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideWorkoutExerciseDao(database: GymTrackDatabase): WorkoutExerciseDao = database.workoutExerciseDao()

    @Provides
    fun provideExerciseSetDao(database: GymTrackDatabase): ExerciseSetDao = database.exerciseSetDao()

    @Provides
    fun providePersonalRecordDao(database: GymTrackDatabase): PersonalRecordDao = database.personalRecordDao()

    @Provides
    fun provideBodyMeasurementDao(database: GymTrackDatabase): BodyMeasurementDao = database.bodyMeasurementDao()

    @Provides
    fun provideWorkoutTemplateDao(database: GymTrackDatabase): WorkoutTemplateDao = database.workoutTemplateDao()
}
