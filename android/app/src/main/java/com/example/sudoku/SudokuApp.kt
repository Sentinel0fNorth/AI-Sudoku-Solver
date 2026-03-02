package com.example.sudoku

import android.app.Application
import com.example.sudoku.data.AppCheckInitializer
import com.google.android.material.color.DynamicColors

/**
 * Application class that initializes global services on startup:
 * - Material You dynamic colors (Samsung OEM fix)
 * - Firebase App Check
 */
class SudokuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        AppCheckInitializer.initialize(this)
    }
}
