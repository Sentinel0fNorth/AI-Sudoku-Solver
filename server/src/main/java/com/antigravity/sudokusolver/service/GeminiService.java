package com.antigravity.sudokusolver.service;

import com.antigravity.sudokusolver.exception.ImageNotRecognizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final String SYSTEM_PROMPT = """
            You are a highly accurate OCR data extraction system specialized in Sudoku puzzles. \
            Your ONLY job is to extract the visible numbers from the provided image and map them to a 9x9 grid.
            STRICT RULES:
            1. Do NOT attempt to solve the puzzle.
            2. Represent the grid as a 2D array of characters.
            3. Use the character ' ' (a single space) for any empty cell.
            4. If the provided image does not contain a recognizable Sudoku grid, output exactly this JSON: {"error": "IMAGE_NOT_RECOGNIZED"}
            5. Output ONLY raw, valid JSON. No markdown.\
            """;

    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;

    public GeminiService(RestClient geminiRestClient, ObjectMapper objectMapper) {
        this.geminiRestClient = geminiRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends the image to Gemini for OCR extraction and parses the response
     * into a char[][] grid.
     *
     * @param imageBytes the raw image bytes
     * @param mimeType   the MIME type of the image (e.g., "image/jpeg")
     * @return the extracted 9x9 char[][] grid
     * @throws ImageNotRecognizedException if Gemini cannot recognize a Sudoku grid
     */
    public char[][] extractGrid(byte[] imageBytes, String mimeType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Build the Gemini API request payload
        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", SYSTEM_PROMPT))),
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", "Extract the Sudoku grid from this image."),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", base64Image))))),
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"));

        // Call Gemini API
        String responseJson = geminiRestClient.post()
                .uri(":generateContent")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return parseGeminiResponse(responseJson);
    }

    private char[][] parseGeminiResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // Navigate: candidates[0].content.parts[0].text
            JsonNode textNode = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");

            if (textNode.isMissingNode()) {
                throw new ImageNotRecognizedException("Gemini returned an unexpected response structure.");
            }

            String extractedJson = textNode.asText();
            JsonNode parsed = objectMapper.readTree(extractedJson);

            // Check for the error sentinel
            if (parsed.has("error")) {
                throw new ImageNotRecognizedException(parsed.get("error").asText());
            }

            // Parse the grid — could be at root level or under a "grid" key
            JsonNode gridNode = parsed.has("grid") ? parsed.get("grid") : parsed;

            if (!gridNode.isArray() || gridNode.size() != 9) {
                throw new ImageNotRecognizedException("Gemini returned a malformed grid.");
            }

            char[][] grid = new char[9][9];
            for (int i = 0; i < 9; i++) {
                JsonNode rowNode = gridNode.get(i);
                if (!rowNode.isArray() || rowNode.size() != 9) {
                    throw new ImageNotRecognizedException("Gemini returned a malformed grid row.");
                }
                for (int j = 0; j < 9; j++) {
                    String cellValue = rowNode.get(j).asText();
                    grid[i][j] = cellValue.isBlank() ? ' ' : cellValue.charAt(0);
                }
            }

            return grid;

        } catch (JsonProcessingException e) {
            throw new ImageNotRecognizedException("Failed to parse Gemini response: " + e.getMessage());
        }
    }
}
