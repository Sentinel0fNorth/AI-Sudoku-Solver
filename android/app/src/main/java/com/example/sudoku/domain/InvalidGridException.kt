package com.example.sudoku.domain

/**
 * Thrown when the Sudoku grid fails structural validation
 * (duplicate digits in a row, column, or 3×3 box, or invalid characters).
 */
class InvalidGridException(message: String) : IllegalArgumentException(message)
