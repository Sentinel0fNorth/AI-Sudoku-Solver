package com.antigravity.sudokusolver.controller;

import com.antigravity.sudokusolver.dto.SudokuExtractResponse;
import com.antigravity.sudokusolver.dto.SudokuSolveRequest;
import com.antigravity.sudokusolver.dto.SudokuSolveResponse;
import com.antigravity.sudokusolver.service.GeminiService;
import com.antigravity.sudokusolver.service.SudokuSolverService;
import com.antigravity.sudokusolver.service.SudokuValidator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/sudoku")
public class SudokuController {

    private final SudokuSolverService solverService;
    private final SudokuValidator validator;
    private final GeminiService geminiService;

    public SudokuController(SudokuSolverService solverService,
            SudokuValidator validator,
            GeminiService geminiService) {
        this.solverService = solverService;
        this.validator = validator;
        this.geminiService = geminiService;
    }

    @PostMapping("/extract")
    public ResponseEntity<SudokuExtractResponse> extractGrid(
            @RequestParam("image") MultipartFile imageFile) throws IOException {

        byte[] imageBytes = imageFile.getBytes();
        String mimeType = imageFile.getContentType() != null
                ? imageFile.getContentType()
                : "image/jpeg";

        char[][] grid = geminiService.extractGrid(imageBytes, mimeType);

        return ResponseEntity.ok(new SudokuExtractResponse(
                "success",
                new SudokuExtractResponse.ExtractData(grid, true)));
    }

    @PostMapping("/solve")
    public ResponseEntity<SudokuSolveResponse> solveGrid(
            @Valid @RequestBody SudokuSolveRequest request) {

        char[][] grid = request.getGrid();

        if (!validator.isValid(grid)) {
            throw new IllegalArgumentException("Invalid Sudoku grid provided.");
        }

        long startTime = System.currentTimeMillis();
        char[][] solvedGrid = solverService.solve(grid);
        long endTime = System.currentTimeMillis();

        return ResponseEntity.ok(new SudokuSolveResponse(solvedGrid, endTime - startTime));
    }
}
