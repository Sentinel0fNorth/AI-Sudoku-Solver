package com.example.sudoku.ui.viewmodel

/**
 * Immutable UI state for the Sudoku screen.
 *
 * The grid is a 9×9 list of lists (row-major). Each cell is:
 * - ' ' (space) for empty
 * - '1'..'9' for a filled digit
 */
data class SudokuUiState(
    val grid: List<List<Char>> = List(9) { List(9) { ' ' } },
    val selectedCell: Pair<Int, Int>? = null,
    val conflictCells: Set<Pair<Int, Int>> = emptySet(),
    val originalCells: Set<Pair<Int, Int>> = emptySet(),
    val isScanning: Boolean = false,
    val isSolving: Boolean = false,
    val isOffline: Boolean = false,
    val isDarkTheme: Boolean = false,
    val error: String? = null
) {
    /** Solve is allowed when the grid has at least one digit and no conflicts. */
    val canSolve: Boolean
        get() = conflictCells.isEmpty() &&
                grid.any { row -> row.any { it != ' ' } }
}
