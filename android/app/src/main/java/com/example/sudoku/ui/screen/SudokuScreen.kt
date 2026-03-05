@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sudoku.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.ui.viewmodel.SudokuUiState
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Root Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SudokuScreen(
    uiState: SudokuUiState,
    onCellSelected: (Int, Int) -> Unit,
    onKeypadInput: (Char) -> Unit,
    onDeleteInput: () -> Unit,
    onSolveClicked: () -> Unit,
    onClearGrid: () -> Unit,
    onToggleTheme: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Fire a review Snackbar when an extraction finishes (isScanning: true → false)
    var wasScanning by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isScanning) {
        if (wasScanning && !uiState.isScanning && uiState.grid.any { row -> row.any { it != ' ' } }) {
            snackbarHostState.showSnackbar("Review extracted digits. Tap any cell to correct mistakes.")
        }
        wasScanning = uiState.isScanning
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1 ── Offline Bar ───────────────────────────────────────────
            OfflineStatusBar(isOffline = uiState.isOffline)

            // 2 ── Header Row ────────────────────────────────────────────
            HeaderRow(
                isDarkTheme = uiState.isDarkTheme,
                onToggleTheme = onToggleTheme
            )

            // 3 ── Tools Row ─────────────────────────────────────────────
            ToolsRow(
                isOffline = uiState.isOffline,
                onCameraClick = {
                    if (uiState.isOffline) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Offline mode: Enter grid manually.")
                        }
                    } else {
                        showBottomSheet = true
                    }
                },
                onClearClick = onClearGrid
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4 ── Sudoku Grid ───────────────────────────────────────────
            SudokuGrid(
                grid = uiState.grid,
                selectedCell = uiState.selectedCell,
                conflictCells = uiState.conflictCells,
                originalCells = uiState.originalCells,
                isScanning = uiState.isScanning,
                onCellSelected = onCellSelected
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5 ── Solve Button ──────────────────────────────────────────
            SolveButton(
                isSolving = uiState.isSolving,
                canSolve = uiState.canSolve,
                onSolveClicked = onSolveClicked
            )

            // Error display
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 6 ── Input Keypad ──────────────────────────────────────────
            InputKeypad(
                onKeypadInput = onKeypadInput,
                onDeleteInput = onDeleteInput
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Camera/Gallery Bottom Sheet ─────────────────────────────────────
    if (showBottomSheet) {
        CameraBottomSheet(
            onDismiss = { showBottomSheet = false },
            onCameraClick = {
                showBottomSheet = false
                onCameraClick()
            },
            onGalleryClick = {
                showBottomSheet = false
                onGalleryClick()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Offline Status Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OfflineStatusBar(isOffline: Boolean) {
    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You are offline",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "Enter the grid manually.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Header Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeaderRow(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleTheme) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Rounded.LightMode
                else Icons.Rounded.DarkMode,
                contentDescription = "Toggle theme",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Tools Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ToolsRow(
    isOffline: Boolean,
    onCameraClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Camera button
        FilledTonalButton(
            onClick = onCameraClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isOffline
        ) {
            Icon(
                imageVector = if (isOffline) Icons.Rounded.NoPhotography
                else Icons.Rounded.CameraAlt,
                contentDescription = "Camera"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan")
        }

        // Clear button
        FilledTonalButton(
            onClick = onClearClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ClearAll,
                contentDescription = "Clear"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Sudoku Grid — "Cookie Cutter" Technique
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SudokuGrid(
    grid: List<List<Char>>,
    selectedCell: Pair<Int, Int>?,
    conflictCells: Set<Pair<Int, Int>>,
    originalCells: Set<Pair<Int, Int>>,
    isScanning: Boolean,
    onCellSelected: (Int, Int) -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        // Container surface — its background color becomes the grid lines
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 12.dp,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                for (bandRow in 0 until 3) {
                    // 3×3 band row
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        for (cellRow in 0 until 3) {
                            val i = bandRow * 3 + cellRow
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                for (bandCol in 0 until 3) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                                    ) {
                                        for (cellCol in 0 until 3) {
                                            val j = bandCol * 3 + cellCol
                                            SudokuCell(
                                                value = grid[i][j],
                                                row = i,
                                                col = j,
                                                isSelected = selectedCell == Pair(i, j),
                                                isConflict = Pair(i, j) in conflictCells,
                                                isOriginal = Pair(i, j) in originalCells,
                                                onClick = { if (!isScanning) onCellSelected(i, j) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    // Add 3.dp gap between 3×3 column bands
                                    if (bandCol < 2) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                }
                            }
                        }
                    }
                    // Add 3.dp gap between 3×3 row bands
                    if (bandRow < 2) {
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }
        }

        // Shimmer overlay when scanning
        if (isScanning) {
            ShimmerOverlay(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
            )
        }
    }
}

@Composable
private fun SudokuCell(
    value: Char,
    row: Int,
    col: Int,
    isSelected: Boolean,
    isConflict: Boolean,
    isOriginal: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 28.dp // 32.dp container minus 4.dp padding
    val shape = cornerShapeForCell(row, col, cornerRadius)

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    // Original/typed cells: bold dark text (onSurface)
    // Solved cells: dynamic accent color (tertiary)
    // Conflicts always override with error color
    val textColor = when {
        isConflict -> MaterialTheme.colorScheme.error
        isOriginal -> if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.tertiary
    }

    val weight = if (isOriginal) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (value != ' ') {
            Text(
                text = value.toString(),
                fontSize = 18.sp,
                fontWeight = weight,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Returns a [RoundedCornerShape] with curvature only on the outer corner
 * for the four grid corners, so they match the parent Surface's rounding.
 * All other cells get a rectangular (zero-radius) shape.
 */
private fun cornerShapeForCell(
    row: Int,
    col: Int,
    radius: androidx.compose.ui.unit.Dp
): androidx.compose.ui.graphics.Shape {
    return when (row) {
        0 if col == 0 -> RoundedCornerShape(topStart = radius)
        0 if col == 8 -> RoundedCornerShape(topEnd = radius)
        8 if col == 0 -> RoundedCornerShape(bottomStart = radius)
        8 if col == 8 -> RoundedCornerShape(bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shimmer Animation Overlay
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ShimmerOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            Color.Transparent
        ),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 600f, 600f)
    )

    Box(
        modifier = modifier.background(shimmerBrush)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Solve Button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SolveButton(
    isSolving: Boolean,
    canSolve: Boolean,
    onSolveClicked: () -> Unit
) {
    Button(
        onClick = onSolveClicked,
        modifier = Modifier
            .fillMaxWidth(0.55f)
            .height(52.dp),
        shape = CircleShape,
        enabled = canSolve && !isSolving,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isSolving) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = "SOLVE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. Input Keypad
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InputKeypad(
    onKeypadInput: (Char) -> Unit,
    onDeleteInput: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: 1 2 3 4 5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (digit in 1..5) {
                KeypadButton(
                    label = digit.toString(),
                    onClick = { onKeypadInput(digit.digitToChar()) }
                )
            }
        }
        // Row 2: 6 7 8 9 ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (digit in 6..9) {
                KeypadButton(
                    label = digit.toString(),
                    onClick = { onKeypadInput(digit.digitToChar()) }
                )
            }
            // Backspace
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = onDeleteInput
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Camera / Gallery Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CameraBottomSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Add Puzzle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Take Photo option
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = onCameraClick
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Take Photo",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Select from Gallery option
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = onGalleryClick
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Select from Gallery",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
