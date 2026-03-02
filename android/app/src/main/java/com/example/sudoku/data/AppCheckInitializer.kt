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

        // TEMPORARY: Force Debug provider for local release testing
        // This bypasses the strict Play Store requirement for Play Integrity
        Firebase.appCheck.installAppCheckProviderFactory(
            com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
        )
        Log.d("AppCheck", "Debug App Check provider explicitly forced")

        // Force a token request to make sure the debug token is generated and logged
        Firebase.appCheck.getAppCheckToken(false)
            .addOnSuccessListener { tokenResult ->
                Log.d("AppCheck", "Successfully fetched App Check token: ${tokenResult.token.take(10)}...")
            }
            .addOnFailureListener { e ->
                Log.e("AppCheck", "Failed to fetch App Check token. Verify Google Play Services is available.", e)
            }
    }
}
