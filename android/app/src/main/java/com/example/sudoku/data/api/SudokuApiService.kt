package com.example.sudoku.data.api

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit service interface matching the backend's [SudokuController].
 *
 * Base URL should point to the server root (e.g. "https://<host>/").
 */
interface SudokuApiService {

    /**
     * Sends an image to the backend for AI-powered grid extraction.
     * Maps to: POST /api/v1/sudoku/extract (multipart/form-data, field "image").
     */
    @Multipart
    @POST("api/v1/sudoku/extract")
    suspend fun extractGrid(
        @Part image: MultipartBody.Part
    ): SudokuExtractResponse

    /**
     * Sends a 9×9 grid to the backend for solving.
     * Maps to: POST /api/v1/sudoku/solve (application/json body).
     */
    @POST("api/v1/sudoku/solve")
    suspend fun solveGrid(
        @Body request: SudokuSolveRequest
    ): SudokuSolveResponse
}
