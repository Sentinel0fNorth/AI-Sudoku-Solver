package com.example.sudoku.domain

import org.junit.Assert.*
import org.junit.Test

class LocalSudokuSolverTest {

    @Test
    fun testIsValid_validGrid_returnsTrue() {
        val grid = Array(9) { CharArray(9) { ' ' } }
        grid[0][0] = '5'
        grid[1][0] = '6'
        
        assertTrue(LocalSudokuSolver.isValid(grid))
    }

    @Test
    fun testIsValid_invalidGridWithDuplicateInRow_returnsFalse() {
        val grid = Array(9) { CharArray(9) { ' ' } }
        grid[0][0] = '5'
        grid[0][1] = '5'
        
        assertFalse(LocalSudokuSolver.isValid(grid))
    }

    @Test
    fun testSolve_validGrid_returnsSolvedGrid() {
        val grid = arrayOf(
            charArrayOf('5', '3', ' ', ' ', '7', ' ', ' ', ' ', ' '),
            charArrayOf('6', ' ', ' ', '1', '9', '5', ' ', ' ', ' '),
            charArrayOf(' ', '9', '8', ' ', ' ', ' ', ' ', '6', ' '),
            charArrayOf('8', ' ', ' ', ' ', '6', ' ', ' ', ' ', '3'),
            charArrayOf('4', ' ', ' ', '8', ' ', '3', ' ', ' ', '1'),
            charArrayOf('7', ' ', ' ', ' ', '2', ' ', ' ', ' ', '6'),
            charArrayOf(' ', '6', ' ', ' ', ' ', ' ', '2', '8', ' '),
            charArrayOf(' ', ' ', ' ', '4', '1', '9', ' ', ' ', '5'),
            charArrayOf(' ', ' ', ' ', ' ', '8', ' ', ' ', '7', '9')
        )

        val result = LocalSudokuSolver.solve(grid)

        assertEquals('5', result[0][0])
        assertEquals('3', result[0][1])
        assertEquals('4', result[0][2])
        assertEquals('6', result[0][3])
        assertEquals('7', result[0][4])
        
        // Ensure the grid is fully filled
        for (row in result) {
            for (cell in row) {
                assertNotEquals(' ', cell)
            }
        }
    }

    @Test(expected = InvalidGridException::class)
    fun testSolve_invalidGrid_throwsException() {
        val grid = Array(9) { CharArray(9) { ' ' } }
        grid[0][0] = '5'
        grid[0][1] = '5' // Invalid

        LocalSudokuSolver.solve(grid)
    }
}
