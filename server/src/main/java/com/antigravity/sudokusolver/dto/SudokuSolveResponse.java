package com.antigravity.sudokusolver.dto;

public class SudokuSolveResponse {
    private char[][] grid;
    private long solveTimeMs;

    public SudokuSolveResponse() {
    }

    public SudokuSolveResponse(char[][] grid, long solveTimeMs) {
        this.grid = grid;
        this.solveTimeMs = solveTimeMs;
    }

    public char[][] getGrid() {
        return grid;
    }

    public void setGrid(char[][] grid) {
        this.grid = grid;
    }

    public long getSolveTimeMs() {
        return solveTimeMs;
    }

    public void setSolveTimeMs(long solveTimeMs) {
        this.solveTimeMs = solveTimeMs;
    }
}
