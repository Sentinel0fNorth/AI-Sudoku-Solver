package com.antigravity.sudokusolver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates requests by checking for a hardcoded API Key Header.
 * Prevents unauthorized bots and scrapers from accessing the Gemini proxy
 * endpoint.
 */
@Component
@Order(2)
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Sudoku-Client-Secret";

    @Value("${sudoku.mobile.api-key}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String incomingApiKey = request.getHeader(API_KEY_HEADER);

        // Fail closed: if the header is missing, or does not perfectly match the
        // expected Secret Manager vault value, drop the request.
        if (incomingApiKey == null || !incomingApiKey.equals(expectedApiKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized: Invalid or missing API Key\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
