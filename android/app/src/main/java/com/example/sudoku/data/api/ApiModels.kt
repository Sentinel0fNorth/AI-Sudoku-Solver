package com.example.sudoku.data.api

import com.google.gson.annotations.SerializedName

// ── Request DTOs ────────────────────────────────────────────────────────────────

/**
 * Matches the backend's `SudokuSolveRequest` DTO.
 * The grid is a 9×9 char array where ' ' (space) represents empty cells
 * and '1'-'9' represent filled digits.
 */
data class SudokuSolveRequest(
    val grid: Array<CharArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SudokuSolveRequest) return false
        return grid.contentDeepEquals(other.grid)
    }

    override fun hashCode(): Int = grid.contentDeepHashCode()
}

// ── Response DTOs ───────────────────────────────────────────────────────────────

/**
 * Matches the backend's `SudokuSolveResponse` DTO.
 */
data class SudokuSolveResponse(
    val grid: Array<CharArray>,
    val solveTimeMs: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SudokuSolveResponse) return false
        return grid.contentDeepEquals(other.grid) && solveTimeMs == other.solveTimeMs
    }

    override fun hashCode(): Int {
        var result = grid.contentDeepHashCode()
        result = 31 * result + solveTimeMs.hashCode()
        return result
    }
}

/**
 * Matches the backend's `SudokuExtractResponse` DTO.
 * Structure: { "status": "...", "data": { "grid": [...], "requiresManualVerification": ... } }
 */
data class SudokuExtractResponse(
    val status: String,
    val data: ExtractData
)

/**
 * Nested data block inside [SudokuExtractResponse].
 */
data class ExtractData(
    val grid: Array<CharArray>,
    @SerializedName("requiresManualVerification")
    val requiresManualVerification: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExtractData) return false
        return grid.contentDeepEquals(other.grid) &&
                requiresManualVerification == other.requiresManualVerification
    }

    override fun hashCode(): Int {
        var result = grid.contentDeepHashCode()
        result = 31 * result + requiresManualVerification.hashCode()
        return result
    }
}
