package com.antigravity.sudokusolver.dto;

import jakarta.validation.constraints.NotNull;

public class SudokuSolveRequest {

    @NotNull(message = "Grid cannot be null")
    private char[][] grid;

    public SudokuSolveRequest() {
    }

    public SudokuSolveRequest(char[][] grid) {
        this.grid = grid;
    }

    public char[][] getGrid() {
        return grid;
    }

    public void setGrid(char[][] grid) {
        this.grid = grid;
    }
}
