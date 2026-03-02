package com.antigravity.sudokusolver.service;

import org.springframework.stereotype.Service;

@Service
public class SudokuSolverService {

    /**
     * Solves the given Sudoku grid using an optimized backtracking algorithm
     * with MRV (Minimum Remaining Values) heuristic for cell selection.
     *
     * @param grid the 9x9 char[][] grid (' ' for empty, '1'-'9' for filled)
     * @return the fully solved grid
     * @throws IllegalArgumentException if the grid is unsolvable
     */
    public char[][] solve(char[][] grid) {
        char[][] solvedGrid = new char[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(grid[i], 0, solvedGrid[i], 0, 9);
        }

        boolean[][] row = new boolean[9][9];
        boolean[][] col = new boolean[9][9];
        boolean[][] box = new boolean[9][9];

        // Initialize constraints from pre-filled cells
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (solvedGrid[i][j] != ' ') {
                    int num = solvedGrid[i][j] - '1';
                    row[i][num] = true;
                    col[j][num] = true;
                    box[(i / 3) * 3 + j / 3][num] = true;
                }
            }
        }

        if (backtrack(solvedGrid, row, col, box)) {
            return solvedGrid;
        }

        throw new IllegalArgumentException("Unsolvable Sudoku grid provided.");
    }

    private boolean backtrack(char[][] board, boolean[][] row, boolean[][] col, boolean[][] box) {
        int[] cell = selectCell(board, row, col, box);
        if (cell == null)
            return true; // solved

        int r = cell[0], c = cell[1];
        int boxIndex = (r / 3) * 3 + c / 3;

        for (int num = 0; num < 9; num++) {
            if (!row[r][num] && !col[c][num] && !box[boxIndex][num]) {
                board[r][c] = (char) (num + '1');
                row[r][num] = col[c][num] = box[boxIndex][num] = true;

                if (backtrack(board, row, col, box))
                    return true;

                // Undo
                board[r][c] = ' ';
                row[r][num] = col[c][num] = box[boxIndex][num] = false;
            }
        }
        return false;
    }

    /**
     * MRV heuristic: picks the empty cell with the fewest valid candidates.
     * This dramatically prunes the search tree compared to naive left-to-right
     * scanning.
     */
    private int[] selectCell(char[][] board, boolean[][] row, boolean[][] col, boolean[][] box) {
        int minOptions = 10;
        int[] bestCell = null;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == ' ') {
                    int options = countOptions(i, j, row, col, box);
                    if (options < minOptions) {
                        minOptions = options;
                        bestCell = new int[] { i, j };
                        if (options == 1)
                            return bestCell; // perfect candidate
                    }
                }
            }
        }
        return bestCell;
    }

    private int countOptions(int r, int c, boolean[][] row, boolean[][] col, boolean[][] box) {
        int boxIndex = (r / 3) * 3 + c / 3;
        int count = 0;
        for (int num = 0; num < 9; num++) {
            if (!row[r][num] && !col[c][num] && !box[boxIndex][num]) {
                count++;
            }
        }
        return count;
    }
}
