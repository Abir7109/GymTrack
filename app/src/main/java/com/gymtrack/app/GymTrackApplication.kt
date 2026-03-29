package com.gymtrack.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GymTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
