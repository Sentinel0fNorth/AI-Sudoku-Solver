package com.example.sudoku.data

import android.util.Log
import com.example.sudoku.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

/**
 * Initializes Firebase App Check.
 *
 * - **Release builds**: Uses [PlayIntegrityAppCheckProviderFactory] to verify
 *   the device and app integrity via Google Play Integrity API.
 * - **Debug builds**: Uses the Debug provider, which prints a debug token
 *   in Logcat that must be registered in the Firebase Console for testing.
 *
 * Call this once from [com.example.sudoku.SudokuApp.onCreate].
 */
object AppCheckInitializer {

    fun initialize(app: android.app.Application) {
        Firebase.initialize(app)

        if (BuildConfig.DEBUG) {
            // Debug provider: logs a token in Logcat to register in Firebase Console
            Firebase.appCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
            )
            Log.d("AppCheck", "Debug App Check provider installed")
        } else {
            Firebase.appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}
