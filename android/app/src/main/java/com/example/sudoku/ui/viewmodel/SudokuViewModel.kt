package com.example.sudoku.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudoku.data.NetworkMonitor
import com.example.sudoku.data.api.RetrofitClient
import com.example.sudoku.data.repository.SudokuRepository
import com.example.sudoku.domain.LocalSudokuSolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel managing Sudoku game state and orchestrating data operations
 * via [SudokuRepository].
 */
class SudokuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SudokuRepository(
        context = application.applicationContext,
        apiService = RetrofitClient.apiService
    )

    private val networkMonitor = NetworkMonitor(application.applicationContext)

    private val _uiState = MutableStateFlow(SudokuUiState())

    /** Observable UI state exposed to the Composable layer. */
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()

    init {
        // Observe network status changes on IO dispatcher (background)
        viewModelScope.launch(Dispatchers.IO) {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOffline = !online) }
            }
        }
    }

    // ── Cell Selection ──────────────────────────────────────────────────

    /** Sets the currently focused cell for keypad input. */
    fun onCellSelected(row: Int, col: Int) {
        _uiState.update { it.copy(selectedCell = Pair(row, col)) }
    }

    // ── Keypad Input ────────────────────────────────────────────────────

    /** Writes a digit into the currently selected cell. */
    fun onKeypadInput(digit: Char) {
        val selected = _uiState.value.selectedCell ?: return
        onCellEdited(selected.first, selected.second, digit)
    }

    /** Clears the currently selected cell (backspace). */
    fun onDeleteInput() {
        val selected = _uiState.value.selectedCell ?: return
        onCellEdited(selected.first, selected.second, ' ')
    }

    // ── Cell Editing ────────────────────────────────────────────────────

    /**
     * Updates a single cell in the grid and recomputes conflicts.
     *
     * @param row   row index (0-8)
     * @param col   column index (0-8)
     * @param value digit character ('1'-'9') or ' ' to clear
     */
    fun onCellEdited(row: Int, col: Int, value: Char) {
        _uiState.update { state ->
            val mutableGrid = state.grid.map { it.toMutableList() }.toMutableList()
            mutableGrid[row][col] = value
            val newGrid = mutableGrid.map { it.toList() }

            // Track this cell as user-entered (original) or remove if cleared
            val updatedOriginals = if (value != ' ') {
                state.originalCells + Pair(row, col)
            } else {
                state.originalCells - Pair(row, col)
            }

            state.copy(
                grid = newGrid,
                originalCells = updatedOriginals,
                conflictCells = computeConflicts(newGrid),
                error = null
            )
        }
    }

    // ── Grid Clear ──────────────────────────────────────────────────────

    /** Resets the entire grid to empty spaces. */
    fun onClearGrid() {
        _uiState.update {
            it.copy(
                grid = List(9) { List(9) { ' ' } },
                selectedCell = null,
                conflictCells = emptySet(),
                originalCells = emptySet(),
                error = null
            )
        }
    }

    // ── Theme Toggle ────────────────────────────────────────────────────

    /** Toggles between light and dark theme. */
    fun onToggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    // ── Photo Capture ───────────────────────────────────────────────────

    /**
     * Called when the user captures a photo of a Sudoku puzzle.
     * Sends the image to the backend for AI-powered grid extraction.
     */
    fun onPhotoCaptured(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }

            val file = uriToFile(uri)
            if (file == null) {
                _uiState.update {
                    it.copy(isScanning = false, error = "Unable to access the captured image.")
                }
                return@launch
            }

            repository.extractGrid(file)
                .onSuccess { extractedGrid ->
                    val gridList = extractedGrid.toListOfLists()
                    // Mark all extracted non-empty cells as originals
                    val originals = mutableSetOf<Pair<Int, Int>>()
                    for (i in gridList.indices) {
                        for (j in gridList[i].indices) {
                            if (gridList[i][j] != ' ') originals.add(Pair(i, j))
                        }
                    }
                    _uiState.update {
                        it.copy(
                            grid = gridList,
                            isScanning = false,
                            originalCells = originals,
                            conflictCells = computeConflicts(gridList),
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            error = throwable.message ?: "Grid extraction failed."
                        )
                    }
                }
        }
    }

    // ── Solving ─────────────────────────────────────────────────────────

    /** Attempts to solve the current grid (remote first, local fallback). */
    fun onSolveClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSolving = true, error = null) }

            val gridArray = _uiState.value.grid.toCharArrayGrid()

            repository.solveGrid(gridArray)
                .onSuccess { solvedGrid ->
                    _uiState.update {
                        it.copy(
                            grid = solvedGrid.toListOfLists(),
                            isSolving = false,
                            conflictCells = emptySet(),
                            error = null
                            // originalCells is intentionally preserved —
                            // solved cells are those NOT in originalCells
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSolving = false,
                            error = throwable.message ?: "Solving failed."
                        )
                    }
                }
        }
    }

    // ── Conflict Detection ──────────────────────────────────────────────

    /**
     * Scans every row, column, and 3×3 box for duplicate digits and
     * returns the set of cells involved in conflicts.
     */
    private fun computeConflicts(grid: List<List<Char>>): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()

        // Check rows
        for (i in 0 until 9) {
            val seen = mutableMapOf<Char, MutableList<Int>>()
            for (j in 0 until 9) {
                val c = grid[i][j]
                if (c != ' ') seen.getOrPut(c) { mutableListOf() }.add(j)
            }
            for ((_, cols) in seen) {
                if (cols.size > 1) cols.forEach { j -> conflicts.add(Pair(i, j)) }
            }
        }

        // Check columns
        for (j in 0 until 9) {
            val seen = mutableMapOf<Char, MutableList<Int>>()
            for (i in 0 until 9) {
                val c = grid[i][j]
                if (c != ' ') seen.getOrPut(c) { mutableListOf() }.add(i)
            }
            for ((_, rows) in seen) {
                if (rows.size > 1) rows.forEach { i -> conflicts.add(Pair(i, j)) }
            }
        }

        // Check 3×3 boxes
        for (boxRow in 0 until 3) {
            for (boxCol in 0 until 3) {
                val seen = mutableMapOf<Char, MutableList<Pair<Int, Int>>>()
                for (i in boxRow * 3 until boxRow * 3 + 3) {
                    for (j in boxCol * 3 until boxCol * 3 + 3) {
                        val c = grid[i][j]
                        if (c != ' ') seen.getOrPut(c) { mutableListOf() }.add(Pair(i, j))
                    }
                }
                for ((_, cells) in seen) {
                    if (cells.size > 1) conflicts.addAll(cells)
                }
            }
        }

        return conflicts
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun uriToFile(uri: Uri): File? {
        return try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("sudoku_capture_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun Array<CharArray>.toListOfLists(): List<List<Char>> =
        map { row -> row.toList() }

    private fun List<List<Char>>.toCharArrayGrid(): Array<CharArray> =
        Array(size) { i -> CharArray(this[i].size) { j -> this[i][j] } }
}
