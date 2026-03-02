package com.antigravity.sudokusolver.service;

import org.springframework.stereotype.Component;

@Component
public class SudokuValidator {

    /**
     * Validates a 9x9 Sudoku grid for structural correctness and duplicate entries.
     * Uses the same constraint-array pattern as SudokuSolverService for
     * consistency.
     *
     * @param board the 9x9 char[][] grid (' ' for empty, '1'-'9' for filled)
     * @return true if the grid is valid, false otherwise
     */
    public boolean isValid(char[][] board) {
        if (board == null || board.length != 9) {
            return false;
        }

        boolean[][] row = new boolean[9][9];
        boolean[][] col = new boolean[9][9];
        boolean[][] box = new boolean[9][9];

        for (int i = 0; i < 9; i++) {
            if (board[i] == null || board[i].length != 9) {
                return false;
            }
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != ' ') {
                    int num = board[i][j] - '1';
                    int boxIndex = (i / 3) * 3 + (j / 3);

                    // Reject characters outside '1'-'9'
                    if (num < 0 || num > 8) {
                        return false;
                    }

                    if (row[i][num] || col[j][num] || box[boxIndex][num]) {
                        return false;
                    }

                    row[i][num] = col[j][num] = box[boxIndex][num] = true;
                }
            }
        }
        return true;
    }
}
