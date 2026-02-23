package com.example.mobileapp.ui.model

import android.content.Context
import java.time.LocalDate

object StreakStorage {
    private const val PREFS_NAME = "petsafari_prefs"
    private const val KEY_LAST_DATE = "streak_last_date"
    private const val KEY_STREAK = "capture_streak"

    fun recordCapture(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        val currentStreak = prefs.getInt(KEY_STREAK, 0)
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()

        val newStreak = when (lastDate) {
            today     -> currentStreak          // already captured today, no change
            yesterday -> currentStreak + 1      // captured yesterday, increment
            else      -> 1                      // missed a day or first ever
        }

        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_STREAK, newStreak)
            .apply()

        return newStreak
    }

    fun getStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_DATE, null) ?: return 0
        val streak = prefs.getInt(KEY_STREAK, 0)
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        // Streak expires if you haven't captured today or yesterday
        return if (lastDate == today || lastDate == yesterday) streak else 0
    }
}