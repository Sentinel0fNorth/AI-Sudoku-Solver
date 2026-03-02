package com.antigravity.sudokusolver.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that validates the {@code X-Firebase-AppCheck} header
 * on every incoming request.
 *
 * <p>
 * App Check tokens are JWTs issued by Firebase. This filter verifies
 * them using the Firebase Admin SDK's token verification capabilities.
 * </p>
 *
 * <p>
 * Can be disabled for local development via {@code appcheck.enabled=false}
 * in application.yml.
 * </p>
 *
 * <p>
 * Runs before {@link RateLimitingFilter} (lower @Order = higher priority)
 * so unauthenticated requests are rejected before consuming rate-limit tokens.
 * </p>
 */
@Component
@Order(1)
public class AppCheckFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AppCheckFilter.class);
    private static final String APP_CHECK_HEADER = "X-Firebase-AppCheck";

    @Value("${appcheck.enabled:false}")
    private boolean enabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(APP_CHECK_HEADER);

        if (token == null || token.isBlank()) {
            log.warn("App Check: missing token from {}", request.getRemoteAddr());
            sendError(response, "Missing App Check token.");
            return;
        }

        try {
            // Verify the App Check token as a Firebase ID token
            FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
            FirebaseToken decodedToken = auth.verifyIdToken(token);
            log.debug("App Check: verified token for subject {}", decodedToken.getUid());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("App Check: invalid token from {} — {}", request.getRemoteAddr(), e.getMessage());
            sendError(response, "Invalid App Check token.");
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
