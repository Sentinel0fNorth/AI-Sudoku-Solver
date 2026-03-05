package com.example.sudoku.ui

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sudoku.ui.screen.CameraCaptureScreen
import com.example.sudoku.ui.screen.SudokuScreen
import com.example.sudoku.ui.theme.SudokuSolverTheme
import com.example.sudoku.ui.viewmodel.SudokuUiState
import com.example.sudoku.ui.viewmodel.SudokuViewModel
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: SudokuViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // ── Camera capture state ────────────────────────────────────
            // File to temporarily store the high-res captured image from CameraX
            var pendingCameraFile by remember { mutableStateOf<File?>(null) }
            // Controls whether to show the in-app CameraX compose overlay
            var showCameraPreview by remember { mutableStateOf(false) }

            val cameraPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    val tempFile = File.createTempFile("sudoku_capture_", ".jpg", cacheDir).apply { createNewFile() }
                    pendingCameraFile = tempFile
                    showCameraPreview = true
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Camera permission is required to scan puzzles.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // ── Gallery picker (modern Photo Picker — no storage perms needed) ──
            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                uri?.let { viewModel.onPhotoCaptured(it) }
            }

            // ── Compose UI ──────────────────────────────────────────────
            SudokuSolverTheme(darkTheme = uiState.isDarkTheme) {
                // If permission granted and file ready, show the in-app CameraX preview instead of the external launcher
                if (showCameraPreview && pendingCameraFile != null) {
                    val fileOptions = ImageCapture.OutputFileOptions.Builder(pendingCameraFile!!).build()

                    CameraCaptureScreen(
                        outputFileOptions = fileOptions,
                        onImageCaptured = {
                            showCameraPreview = false
                            viewModel.onPhotoCaptured(Uri.fromFile(pendingCameraFile!!))
                        },
                        onError = {
                            showCameraPreview = false
                            Toast.makeText(this@MainActivity, "Failed to capture image", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    SudokuScreen(
                        uiState = uiState,
                        onCellSelected = viewModel::onCellSelected,
                        onKeypadInput = viewModel::onKeypadInput,
                        onDeleteInput = viewModel::onDeleteInput,
                        onSolveClicked = viewModel::onSolveClicked,
                        onClearGrid = viewModel::onClearGrid,
                        onToggleTheme = viewModel::onToggleTheme,
                        onCameraClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onGalleryClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_9")
@Composable
fun SudokuScreenPreview() {
    val sampleGrid = List(9) { row ->
        List(9) { col ->
            when (row) {
                0 if col == 0 -> '5'
                1 if col == 1 -> '3'
                8 if col == 8 -> '9'
                else -> ' '
            }
        }
    }
    SudokuSolverTheme(dynamicColor = false) {
        SudokuScreen(
            uiState = SudokuUiState(
                grid = sampleGrid,
                originalCells = setOf(Pair(0, 0), Pair(1, 1), Pair(8, 8))
            ),
            onCellSelected = { _, _ -> },
            onKeypadInput = {},
            onDeleteInput = {},
            onSolveClicked = {},
            onClearGrid = {},
            onToggleTheme = {},
            onCameraClick = {},
            onGalleryClick = {}
        )
    }
}
