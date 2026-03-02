package com.antigravity.sudokusolver.dto;

public class SudokuExtractResponse {
    private String status;
    private ExtractData data;

    public SudokuExtractResponse() {
    }

    public SudokuExtractResponse(String status, ExtractData data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ExtractData getData() {
        return data;
    }

    public void setData(ExtractData data) {
        this.data = data;
    }

    public static class ExtractData {
        private char[][] grid;
        private boolean requiresManualVerification;

        public ExtractData() {
        }

        public ExtractData(char[][] grid, boolean requiresManualVerification) {
            this.grid = grid;
            this.requiresManualVerification = requiresManualVerification;
        }

        public char[][] getGrid() {
            return grid;
        }

        public void setGrid(char[][] grid) {
            this.grid = grid;
        }

        public boolean isRequiresManualVerification() {
            return requiresManualVerification;
        }

        public void setRequiresManualVerification(boolean requiresManualVerification) {
            this.requiresManualVerification = requiresManualVerification;
        }
    }
}
