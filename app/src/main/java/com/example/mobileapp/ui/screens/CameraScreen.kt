package com.example.mobileapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mobileapp.ui.model.Animal
import com.example.mobileapp.ui.model.AnimalPool
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import com.example.mobileapp.ui.sound.SoundManager
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
private data class FaceOverlay(val centerX: Float, val centerY: Float, val sizePx: Float)

private fun mapFaceToView(
    rect: RectF,
    imgW: Int, imgH: Int,
    rotation: Int,
    viewW: Int, viewH: Int,
    isFrontCamera: Boolean
): FaceOverlay {
    var nx: Float
    var ny: Float
    var nSize: Float

    when (rotation) {
        90 -> {
            nx = rect.centerY() / imgH
            ny = 1f - rect.centerX() / imgW
            nSize = rect.height() / imgH
        }
        180 -> {
            nx = 1f - rect.centerX() / imgW
            ny = 1f - rect.centerY() / imgH
            nSize = rect.width() / imgW
        }
        270 -> {
            nx = 1f - rect.centerY() / imgH
            ny = rect.centerX() / imgW
            nSize = rect.height() / imgH
        }
        else -> {
            nx = rect.centerX() / imgW
            ny = rect.centerY() / imgH
            nSize = rect.width() / imgW
        }
    }

    if (isFrontCamera) nx = 1f - nx

    val effW = if (rotation == 90 || rotation == 270) imgH.toFloat() else imgW.toFloat()
    val effH = if (rotation == 90 || rotation == 270) imgW.toFloat() else imgH.toFloat()

    val scale = maxOf(viewW / effW, viewH / effH)
    val scaledW = effW * scale
    val scaledH = effH * scale
    val ox = (viewW - scaledW) / 2f
    val oy = (viewH - scaledH) / 2f

    return FaceOverlay(
        centerX = ox + nx * scaledW,
        centerY = oy + ny * scaledH,
        sizePx  = nSize * scaledW
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onCaptured: (photoPath: String?, animal: Animal) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) { if (!hasPermission) launcher.launch(Manifest.permission.CAMERA) }

    //
Pre-pi
ck the animal so the player sees it on their face while scanning
    val filterAnimal = remember { AnimalPool.randomAnimal() }

    var progress by remember { mutableIntStateOf(0) }
    var captured by remember { mutableStateOf(false) }
    var savedPhotoPath by remember { mutableStateOf<String?>(null) }
    val firedCapture = remember { AtomicBoolean(false) }

    var faceRects by remember { mutableStateOf<List<RectF>>(emptyList()) }
    var imgW by remember { mutableIntStateOf(1) }
    var imgH by remember { mutableIntStateOf(1) }
    var imgRotation by remember { mutableIntStateOf(0) }
    var viewW by remember { mutableIntStateOf(0) }
    var viewH by remember { mutableIntStateOf(0) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    fun takePhoto() {
        val photoDir = File(context.filesDir, "captures")
        if (!photoDir.exists()) photoDir.mkdirs()
        val photoFile = File(photoDir, "pet_${System.currentTimeMillis()}.jpg")
        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    SoundManager.playShutter()
                    savedPhotoPath = photoFile.absolutePath
                    captured = true
                }
                override fun onError(exc: ImageCaptureException) { captured = true }
            }
        )
    }

    val detector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("You might get… ${filterAnimal.emoji} ${filterAnimal.name}") },
                navigationIcon = {
                    Button(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Back")
                    }
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (!hasPermission) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission required")
                }
                return@Column
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .onGloballyPositioned { coords ->
                        viewW = coords.size.width
                        viewH = coords.size.height
                    }
            ) {
                // Camera preview
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }

                        ProcessCameraProvider.getInstance(ctx).addListener({
                            val cameraProvider = ProcessCameraProvider.getInstance(ctx).get()
                            val preview = androidx.camera.core.Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
.build()

                            analysis.setAnalyzer(mainExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage == null) { imageProxy.close(); return@setAnalyzer }

                                imgW = imageProxy.width
                                imgH = imageProxy.height
                                imgRotation = imageProxy.imageInfo.rotationDegrees

                                val input = InputImage.fromMediaImage(
                                    mediaImage, imageProxy.imageInfo.rotationDegrees
                                )

                                detector.process(input)
                                    .addOnSuccessListener { faces ->
                                        faceRects = faces.map { RectF(it.boundingBox) }

                                        if (faces.isNotEmpty()) {
                                            progress = (progress + 2).coerceAtMost(100)
                                        } else {
                                            progress = (progress - 3).coerceAtLeast(0)
                                        }
                                        if (progress >= 100 && firedCapture.compareAndSet(false, true)) {
                                            takePhoto()
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_FRONT_CAMERA,
                                    preview, analysis, imageCapture
                                )
                            } catch (_: Exception) {}
                        }, mainExecutor)

                        previewView
                    }
                )

                // ── Snapchat-style emoji overlay ───────────────────────────
                if (viewW > 0 && viewH > 0) {
                    faceRects.forEach { rect ->
                        val overlay = mapFaceToView(
                            rect = rect,
                            imgW = imgW, imgH = imgH,
                            rotation = imgRotation,
                            viewW = viewW, viewH = viewH,
                            isFrontCamera = true
                        )

                        // Scale font size to match face width (90%), clamped sensibly
                        val fontSizeSp = (overlay.sizePx * 0.9f /
                                context.resources.displayMetrics.density)
                            .coerceIn(24f, 200f)

                        // Offset so emoji is centred horizontally and sits ON the face
                        val left = (overlay.centerX - overlay.sizePx / 2f).roundToInt()
                        val top  = (overlay.centerY - overlay.sizePx * 1.05f).roundToInt()

                        Text(
                            text = filterAnimal.emoji,
                            fontSize = fontSizeSp.sp,
                            modifier = Modifier
                                .offset { IntOffset(left, top) }
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                .drawWithContent {
                                    drawContent()
                                    drawRect(Color.Black, blendMode = BlendMode.SrcIn)
                                }
                        )
                    }
                }
            }

            // Progress / status area
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
                Text(text = "$progress%", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                if (captured) Text("Captured!") else Text("Hold still and keep your face in view…")
            }
        }
    }

    LaunchedEffect(captured) {
        if (captured) {
            delay(500)
            onCaptured(savedPhotoPath, filterAnimal)
        }
    }
}