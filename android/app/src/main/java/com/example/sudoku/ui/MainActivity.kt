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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sudoku.ui.screen.SudokuScreen
import com.example.sudoku.ui.theme.SudokuSolverTheme
import com.example.sudoku.ui.viewmodel.SudokuViewModel
import com.example.sudoku.ui.viewmodel.SudokuUiState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.io.File

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: SudokuViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // ── Camera capture state ────────────────────────────────────
            var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { success ->
                if (success && pendingCameraUri != null) {
                    viewModel.onPhotoCaptured(pendingCameraUri!!)
                }
            }

            val cameraPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    val uri = createTempImageUri()
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
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

    /**
     * Creates a temporary file in the cache directory and returns a content URI
     * via [FileProvider] for the camera to write the captured image to.
     */
    private fun createTempImageUri(): Uri {
        val tempFile = File.createTempFile(
            "sudoku_capture_", ".jpg", cacheDir
        ).apply { createNewFile() }

        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            tempFile
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_9")
@Composable
fun SudokuScreenPreview() {
    val sampleGrid = List(9) { row ->
        List(9) { col ->
            if (row == 0 && col == 0) '5'
            else if (row == 1 && col == 1) '3'
            else if (row == 8 && col == 8) '9'
            else ' '
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
