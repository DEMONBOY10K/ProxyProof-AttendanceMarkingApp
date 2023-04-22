package com.mact.proxyproof.schedule

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.mact.proxyproof.schedule.data.AppDatabase
import com.mact.proxyproof.schedule.data.CourseRepository
import com.mact.proxyproof.schedule.ui.preferences.PREFERENCE_THEME


class MyApplication : Application() {

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getInstance(this) }
    val courseRepository by lazy { CourseRepository(database.courseDao()) }

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val nightModeValue = sharedPreferences?.getString(PREFERENCE_THEME, "-1")?.toInt()
        AppCompatDelegate.setDefaultNightMode(nightModeValue!!)
    }

}