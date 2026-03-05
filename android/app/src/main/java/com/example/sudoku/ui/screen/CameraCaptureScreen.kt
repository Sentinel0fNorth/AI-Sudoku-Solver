package com.example.sudoku.ui.screen

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun CameraCaptureScreen(
    outputFileOptions: ImageCapture.OutputFileOptions,
    onImageCaptured: () -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    // Explicitly enforce the rear-facing camera to bypass OEM intent-ignoring behavior
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    LaunchedEffect(cameraSelector) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        preview.surfaceProvider = previewView.surfaceProvider
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            { previewView },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            onClick = {
                imageCapture.takePicture(
                    outputFileOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            onImageCaptured()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraCapture", "Photo capture failed: ${exception.message}", exception)
                            onError(exception)
                        }
                    }
                )
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Camera,
                contentDescription = "Take Photo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            continuation.resume(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }
