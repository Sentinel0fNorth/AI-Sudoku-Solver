package com.example.sudoku.domain

/**
 * Local Sudoku solver using an optimized backtracking algorithm with
 * MRV (Minimum Remaining Values) heuristic.
 *
 * This is a **direct Kotlin port** of the backend's `SudokuSolverService.java`
 * to enable offline solving when the device has no network connectivity.
 *
 * Grid convention:
 * - ' ' (space char) → empty cell
 * - '1'..'9'        → filled digit
 */
object LocalSudokuSolver {

    /**
     * Validates a 9×9 Sudoku grid for structural correctness.
     *
     * This is a **direct Kotlin port** of the backend's `SudokuValidator.isValid()`.
     * It checks that:
     * - The grid is non-null and exactly 9×9.
     * - Every filled cell contains a character in '1'..'9'.
     * - No row, column, or 3×3 subgrid contains duplicate digits.
     *
     * @param board the 9×9 grid to validate
     * @return `true` if the grid is structurally valid, `false` otherwise
     */
    fun isValid(board: Array<CharArray>?): Boolean {
        if (board == null || board.size != 9) return false

        val row = Array(9) { BooleanArray(9) }
        val col = Array(9) { BooleanArray(9) }
        val box = Array(9) { BooleanArray(9) }

        for (i in 0 until 9) {
            if (board[i].size != 9) return false

            for (j in 0 until 9) {
                if (board[i][j] != ' ') {
                    val num = board[i][j] - '1'
                    val boxIndex = (i / 3) * 3 + (j / 3)

                    // Reject characters outside '1'-'9'
                    if (num < 0 || num > 8) return false

                    if (row[i][num] || col[j][num] || box[boxIndex][num]) return false

                    row[i][num] = true
                    col[j][num] = true
                    box[boxIndex][num] = true
                }
            }
        }
        return true
    }

    /**
     * Solves the given 9×9 grid in-place on a deep copy and returns the solved copy.
     *
     * @param grid the 9×9 `Array<CharArray>` puzzle
     * @return a new fully-solved grid
     * @throws IllegalArgumentException if the puzzle is unsolvable
     */
    fun solve(grid: Array<CharArray>): Array<CharArray> {
        // Validate before spending CPU on backtracking
        if (!isValid(grid)) {
            throw InvalidGridException("Invalid Sudoku grid: contains duplicates or invalid characters.")
        }

        // Deep-copy so the original grid is never mutated
        val board = Array(9) { i -> grid[i].copyOf() }

        // Constraint tracking: row[i][num], col[j][num], box[boxIdx][num]
        val row = Array(9) { BooleanArray(9) }
        val col = Array(9) { BooleanArray(9) }
        val box = Array(9) { BooleanArray(9) }

        // Initialize constraints from pre-filled cells
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (board[i][j] != ' ') {
                    val num = board[i][j] - '1'
                    row[i][num] = true
                    col[j][num] = true
                    box[(i / 3) * 3 + j / 3][num] = true
                }
            }
        }

        if (backtrack(board, row, col, box)) {
            return board
        }

        throw IllegalArgumentException("Unsolvable Sudoku grid provided.")
    }

    /**
     * Recursive backtracking with constraint propagation.
     */
    private fun backtrack(
        board: Array<CharArray>,
        row: Array<BooleanArray>,
        col: Array<BooleanArray>,
        box: Array<BooleanArray>
    ): Boolean {
        val cell = selectCell(board, row, col, box)
            ?: return true // No empty cells → solved

        val r = cell[0]
        val c = cell[1]
        val boxIndex = (r / 3) * 3 + c / 3

        for (num in 0 until 9) {
            if (!row[r][num] && !col[c][num] && !box[boxIndex][num]) {
                board[r][c] = (num + '1'.code).toChar()
                row[r][num] = true
                col[c][num] = true
                box[boxIndex][num] = true

                if (backtrack(board, row, col, box)) return true

                // Undo
                board[r][c] = ' '
                row[r][num] = false
                col[c][num] = false
                box[boxIndex][num] = false
            }
        }
        return false
    }

    /**
     * MRV heuristic: picks the empty cell with the fewest valid candidates.
     * Returns `null` when no empty cells remain (puzzle is solved).
     */
    private fun selectCell(
        board: Array<CharArray>,
        row: Array<BooleanArray>,
        col: Array<BooleanArray>,
        box: Array<BooleanArray>
    ): IntArray? {
        var minOptions = 10
        var bestCell: IntArray? = null

        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (board[i][j] == ' ') {
                    val options = countOptions(i, j, row, col, box)
                    if (options < minOptions) {
                        minOptions = options
                        bestCell = intArrayOf(i, j)
                        if (options == 1) return bestCell // Perfect candidate
                    }
                }
            }
        }
        return bestCell
    }

    /**
     * Counts how many digits (1-9) are still valid for cell (r, c).
     */
    private fun countOptions(
        r: Int,
        c: Int,
        row: Array<BooleanArray>,
        col: Array<BooleanArray>,
        box: Array<BooleanArray>
    ): Int {
        val boxIndex = (r / 3) * 3 + c / 3
        var count = 0
        for (num in 0 until 9) {
            if (!row[r][num] && !col[c][num] && !box[boxIndex][num]) {
                count++
            }
        }
        return count
    }
}
