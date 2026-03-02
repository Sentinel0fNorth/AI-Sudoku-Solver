package com.antigravity.sudokusolver.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Initializes the Firebase Admin SDK on server startup.
 *
 * <ul>
 *   <li><b>GCP Cloud Run</b>: Uses Application Default Credentials automatically.</li>
 *   <li><b>Local development</b>: Uses a service account JSON key file
 *       specified via {@code firebase.credentials-path} in application.yml.</li>
 * </ul>
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @PostConstruct
    public void initFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options;

            if (credentialsPath != null && !credentialsPath.isBlank()) {
                // Local dev: load from service account key file
                log.info("Initializing Firebase from credentials file: {}", credentialsPath);
                try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                }
            } else {
                // GCP Cloud Run: Application Default Credentials
                log.info("Initializing Firebase with Application Default Credentials");
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
            }

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully");
        }
    }
}
