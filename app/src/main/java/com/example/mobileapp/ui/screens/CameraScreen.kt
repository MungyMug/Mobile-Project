package com.example.mobileapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onCaptured: (photoPath: String?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    var progress by remember { mutableIntStateOf(0) }
    var captured by remember { mutableStateOf(false) }
    var savedPhotoPath by remember { mutableStateOf<String?>(null) }
    val firedCapture = remember { AtomicBoolean(false) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    fun takePhoto() {
        val photoDir = File(context.filesDir, "captures")
        if (!photoDir.exists()) photoDir.mkdirs()
        val photoFile = File(photoDir, "pet_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    savedPhotoPath = photoFile.absolutePath
                    captured = true
                }
                override fun onError(exc: ImageCaptureException) {
                    captured = true
                }
            }
        )
    }

    val detector = remember {
        val opts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        FaceDetection.getClient(opts)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanning") },
                navigationIcon = {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp)
                    ) { Text("Back") }
                },
                actions = {
                    Button(
                        onClick = onSkip,
                        enabled = hasPermission,
                        modifier = Modifier.padding(end = 8.dp)
                    ) { Text("Skip") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!hasPermission) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission required")
                }
                return@Column
            }

            // Camera preview - use COMPATIBLE mode (TextureView) to avoid z-order issues
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = androidx.camera.core.Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            analysis.setAnalyzer(mainExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage == null) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val input = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                detector.process(input)
                                    .addOnSuccessListener { faces ->
                                        if (faces.isNotEmpty()) {
                                            progress = (progress + 2).coerceAtMost(100)
                                        } else {
                                            progress = (progress - 3).coerceAtLeast(0)
                                        }

                                        if (progress >= 100 && firedCapture.compareAndSet(false, true)) {
                                            takePhoto()
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            }

                            val selector = CameraSelector.DEFAULT_FRONT_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    analysis,
                                    imageCapture
                                )
                            } catch (_: Exception) {
                            }
                        }, mainExecutor)

                        previewView
                    }
                )
            }

            // Analysis UI area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Analyzing…", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(12.dp))

                if (captured) {
                    Text("Captured!")
                } else {
                    Text("Hold still and keep your face in view…")
                }
            }
        }
    }

    LaunchedEffect(captured) {
        if (captured) {
            delay(500)
            onCaptured(savedPhotoPath)
        }
    }
}
