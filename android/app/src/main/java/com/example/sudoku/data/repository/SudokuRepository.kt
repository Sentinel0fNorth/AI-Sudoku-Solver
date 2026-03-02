package com.example.sudoku.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.sudoku.data.api.SudokuApiService
import com.example.sudoku.data.api.SudokuSolveRequest
import com.example.sudoku.domain.LocalSudokuSolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Single source of truth for Sudoku operations.
 *
 * Routes between the remote [SudokuApiService] and the local [LocalSudokuSolver]
 * based on network availability, returning [Result] wrappers for clean error handling.
 */
class SudokuRepository(
    private val context: Context,
    private val apiService: SudokuApiService
) {

    // ── Extraction (online only — requires backend AI) ──────────────────────

    /**
     * Sends the captured image to the backend for AI grid extraction.
     * Returns [Result.failure] when offline since extraction requires the server.
     */
    suspend fun extractGrid(imageFile: File): Result<Array<CharArray>> =
        withContext(Dispatchers.IO) {
            if (!isNetworkAvailable()) {
                return@withContext Result.failure(
                    IllegalStateException("Network unavailable. Grid extraction requires an internet connection.")
                )
            }

            runCatching {
                val mediaType = "image/jpeg".toMediaTypeOrNull()
                val requestBody = imageFile.asRequestBody(mediaType)
                val part = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)

                val response = apiService.extractGrid(part)
                response.data.grid
            }
        }

    // ── Solving (online or offline) ─────────────────────────────────────────

    /**
     * Solves the given 9×9 grid.
     * - **Online**: delegates to the backend API.
     * - **Offline**: falls back to [LocalSudokuSolver] on [Dispatchers.Default].
     */
    suspend fun solveGrid(grid: Array<CharArray>): Result<Array<CharArray>> {
        return if (isNetworkAvailable()) {
            solveRemote(grid)
        } else {
            solveLocal(grid)
        }
    }

    private suspend fun solveRemote(grid: Array<CharArray>): Result<Array<CharArray>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.solveGrid(SudokuSolveRequest(grid))
                response.grid
            }
        }

    private suspend fun solveLocal(grid: Array<CharArray>): Result<Array<CharArray>> =
        withContext(Dispatchers.Default) {
            runCatching {
                LocalSudokuSolver.solve(grid)
            }
        }

    // ── Network check ───────────────────────────────────────────────────────

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
