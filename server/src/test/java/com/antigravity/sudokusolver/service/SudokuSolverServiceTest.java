package com.antigravity.sudokusolver.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SudokuSolverServiceTest {

    private final SudokuSolverService solverService = new SudokuSolverService();

    @Test
    public void testSolveValidGrid() {
        char[][] grid = {
                { '5', '3', ' ', ' ', '7', ' ', ' ', ' ', ' ' },
                { '6', ' ', ' ', '1', '9', '5', ' ', ' ', ' ' },
                { ' ', '9', '8', ' ', ' ', ' ', ' ', '6', ' ' },
                { '8', ' ', ' ', ' ', '6', ' ', ' ', ' ', '3' },
                { '4', ' ', ' ', '8', ' ', '3', ' ', ' ', '1' },
                { '7', ' ', ' ', ' ', '2', ' ', ' ', ' ', '6' },
                { ' ', '6', ' ', ' ', ' ', ' ', '2', '8', ' ' },
                { ' ', ' ', ' ', '4', '1', '9', ' ', ' ', '5' },
                { ' ', ' ', ' ', ' ', '8', ' ', ' ', '7', '9' }
        };

        char[][] result = solverService.solve(grid);

        assertNotNull(result);

        // Check a few known cells from the solution of this specific puzzle
        assertEquals('5', result[0][0]);
        assertEquals('3', result[0][1]);
        assertEquals('4', result[0][2]);
        assertEquals('6', result[0][3]);
        assertEquals('7', result[0][4]);

        // Ensure no empty cells remain
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                assertNotEquals(' ', result[i][j]);
            }
        }
    }

    @Test
    public void testSolveUnsolvableGrid() {
        char[][] grid = {
                { '5', '5', ' ', ' ', '7', ' ', ' ', ' ', ' ' }, // Two 5s in the first row makes it invalid/unsolvable
                { '6', ' ', ' ', '1', '9', '5', ' ', ' ', ' ' },
                { ' ', '9', '8', ' ', ' ', ' ', ' ', '6', ' ' },
                { '8', ' ', ' ', ' ', '6', ' ', ' ', ' ', '3' },
                { '4', ' ', ' ', '8', ' ', '3', ' ', ' ', '1' },
                { '7', ' ', ' ', ' ', '2', ' ', ' ', ' ', '6' },
                { ' ', '6', ' ', ' ', ' ', ' ', '2', '8', ' ' },
                { ' ', ' ', ' ', '4', '1', '9', ' ', ' ', '5' },
                { ' ', ' ', ' ', ' ', '8', ' ', ' ', '7', '9' }
        };

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            solverService.solve(grid);
        });

        assertEquals("Unsolvable Sudoku grid provided.", exception.getMessage());
    }
}
