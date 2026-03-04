# Sudoku Solver (Portfolio Project)

A full-stack mobile application that allows users to seamlessly solve Sudoku puzzles using an Android app with a backend built in Spring Boot. This repository serves as a portfolio project, demonstrating modern Android development practices (Jetpack Compose) combined with a robust, production-ready backend architecture.

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Features](#features)
- [Security implementation](#security-implementation)
- [Project Setup](#project-setup)
    - [Prerequisites](#prerequisites)
    - [Backend (Spring Boot) Setup](#backend-spring-boot-setup)
    - [Android App Setup](#android-app-setup)
- [License](#license)

## Architecture Overview

>[!NOTE]
> *Placeholder: Insert an Architecture Diagram (e.g., Mermaid) or data flow illustration here showing the connection between the Android Client, the Secure API layer, and the Spring Boot Backend.*

The project is split into two primary components:
1. **Android Client (`/android`)**: A native mobile application built using Kotlin and Jetpack Compose.
2. **Server Backend (`/server`)**: A Java Spring Boot application that handles the core Sudoku solving algorithm and exposes a secure REST API.

## Screenshots

>[!NOTE]
> *Placeholder: Insert Application Screenshots / GIFs here.*
> - *Screenshot 1: The Splash Screen and Custom App Icon.*
> - *Screenshot 2: The Camera/Gallery Grid Extraction feature.*
> - *GIF: The app instantly solving a Sudoku puzzle.*

## Features

- **Jetpack Compose UI**: Modern, declarative UI on Android.
- **Material You Dynamic Theming**: Adaptive color schemes that respect device settings.
- **AI Grid Extraction**: Integrates with the Gemini API to automatically extract Sudoku grids from device camera photos.
- **Offline Manual Fallback**: Includes a local Sudoku solving algorithm on the Android client for use when the device is fully offline.
- **Fast Solving Algorithm**: A custom backtracking algorithm optimized for performance in the backend.
- **Secure API Access**: Custom API key filtering and rate limiting to prevent abuse.

## Security implementation

This project features a resume-optimized security architecture demonstrating practical and robust security paradigms:

- **API Key Filtering**: Incoming requests to the backend are intercepted by a custom `ApiKeyFilter` to ensure a valid API key is present.
- **GCP Secret Manager Integration**: Production API keys are securely stored and retrieved using Google Cloud Platform Secret Manager, eliminating hardcoded secrets in the backend repository.
- **Rate Limiting**: An in-memory token bucket rate limiter protects the API from DDoS attacks and potential scraping, maintaining high availability.
- **Client-Side Obfuscation**: The Android app utilizes R8 minification to obfuscate code and protect the API key from trivial decompilation during distribution.

## Project Setup

### Prerequisites

- [Java JDK 21+](https://www.oracle.com/java/technologies/downloads/#java21)
- [Android Studio](https://developer.android.com/studio)
- A Google Cloud Platform (GCP) account (for deploying/testing production secrets).

### Backend (Spring Boot) Setup

1. Navigate to the `server/` directory.
2. Open the project in IntelliJ IDEA or your preferred IDE.
3. Configure your local application properties (e.g., `application-dev.yml`) depending on the environment you are running.
4. Run the project using `mvn spring-boot:run` (or your IDE's run configuration).

**GCP Secret Manager for Production:**
If deploying with the `prod` profile, ensure your GCP project is properly configured and the `SPRING_CLOUD_GCP_SECRETMANAGER_PROJECT_ID` environment variable is set.
The service account running your backend must be granted the **Secret Manager Secret Accessor** (`roles/secretmanager.secretAccessor`) IAM role to read these values. 
You must also create the following secrets in your GCP project's Secret Manager:
- `gemini-api-key`: Your actual Gemini API key for AI grid extraction.
- `sudoku-mobile-api-key`: The secure key that matches the `MOBILE_API_KEY` expected from the Android client.

### Android App Setup

1. Open the `android/` directory in Android Studio.
2. Create a file named `local.properties` in the root of the `android` project directory (this file should be ignored by Git).
3. Add your backend API key to `local.properties` to allow local builds to authenticate. If you plan to build the **Release APK**, you must also provide your Keystore signing configurations, as they are dynamically read by Gradle to protect the raw passwords:
   ```properties
   MOBILE_API_KEY=your_actual_api_key_here
   KEYSTORE_PASSWORD=your_keystore_password
   KEY_ALIAS=my-key-alias
   KEY_PASSWORD=your_key_password
   ```
4. Sync the Gradle project.
5. Build and run the app on an emulator or physical device.

## Deployment (Google Cloud Run)

## Deployment (Google Cloud Run)

The Spring Boot backend is designed for serverless deployment on Google Cloud Run. If you wish to host your own instance of the backend, follow these steps:

1. **Create a GCP Project**: Set up a new project in the [Google Cloud Console](https://console.cloud.google.com/) and enable the **Cloud Run API** and **Secret Manager API**.
2. **Configure Secrets**: In Secret Manager, create `gemini-api-key` and `sudoku-mobile-api-key` (as detailed in the [GCP Secret Manager](#backend-spring-boot-setup) section).
3. **Grant Permissions**: Ensure the Default Compute Service Account (which Cloud Run uses) has the `Secret Manager Secret Accessor` IAM role.
4. **Export Environment Variable**: Locally, set your project ID:
   ```bash
   export SPRING_CLOUD_GCP_SECRETMANAGER_PROJECT_ID="your-gcp-project-id"
   ```
5. **Build and Push the Image**: Use Google Jib to build the container image and push it to the Google Container Registry (GCR):
   ```bash
   mvn compile jib:build -Dimage=gcr.io/your-gcp-project-id/sudokusolver
   ```
6. **Deploy to Cloud Run**: Deploy the image using the `prod` Spring profile:
   ```bash
   gcloud run deploy sudokusolver \
     --image gcr.io/your-gcp-project-id/sudokusolver \
     --platform managed \
     --allow-unauthenticated \
     --set-env-vars="SPRING_PROFILES_ACTIVE=prod"
   ```
7. **Update Android Client**: Once deployed, the console will output a public Service URL (e.g., `https://sudokusolver...run.app/`). Copy this URL and update the `BACKEND_URL` in your `android/app/build.gradle.kts` file.

## Tests

To ensure the algorithmic integrity of the application, unit tests are provided for both the Spring Boot backend and the Android client.

### Backend Tests (Spring Boot)
The tests are located in `server/src/test/java/.../SudokuSolverServiceTest.java` and can be run using `mvn test`.
They cover:
- `testSolveValidGrid`: Verifies that the MRV backtracking algorithm successfully solves a standard 9x9 grid and replaces all empty spaces.
- `testSolveUnsolvableGrid`: Ensures that an `IllegalArgumentException` is thrown when an unsolvable grid (e.g., with inherent constraint violations like two 5s in the same row) is submitted.

### Frontend Tests (Android)
The tests are located in `android/app/src/test/java/.../LocalSudokuSolverTest.kt` and can be run via Android Studio or `./gradlew testDebugUnitTest`.
They cover:
- `testIsValid_validGrid`: Verifies the structural validation logic correctly accepts a valid grid.
- `testIsValid_invalidGridWithDuplicateInRow`: Ensures grid validation properly catches row/column/box rule violations.
- `testSolve_validGrid`: Validates the offline fallback solving algorithm correctly solves a puzzle locally.
- `testSolve_invalidGrid`: Ensures that submitting an invalid grid to the local solver correctly throws an `InvalidGridException`.

## License

[MIT License](LICENSE)
