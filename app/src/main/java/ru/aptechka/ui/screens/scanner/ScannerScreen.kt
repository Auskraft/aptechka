package ru.aptechka.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.koin.androidx.compose.koinViewModel
import ru.aptechka.R
import ru.aptechka.ui.forms.Forms
import ru.aptechka.ui.navigation.Screen
import ru.aptechka.ui.theme.LocalDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    kitId: Long,
    navController: NavController,
    viewModel: ScannerViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    val result by viewModel.result.collectAsState()

    fun openManualAdd() {
        navController.popBackStack()
        navController.navigate(Screen.AddDrug.go(kitId))
    }

    fun openPrefilledAdd(catalogId: Long) {
        navController.popBackStack()
        navController.navigate(Screen.AddDrug.go(kitId, catalogId))
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            CameraPreview(
                onText = viewModel::onTextRecognized,
                modifier = Modifier.fillMaxSize(),
            )
            if (result == null) ScannerOverlay(onManual = { openManualAdd() })
        } else {
            PermissionRationale(
                onRequest = { launcher.launch(Manifest.permission.CAMERA) },
                onManual = { openManualAdd() },
            )
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f)),
        ) {
            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.scan_close), tint = Color.White)
        }
    }

    result?.let { res ->
        FoundSheet(
            result = res,
            onContinue = { openPrefilledAdd(res.match.id) },
            onRetry = { viewModel.reset() },
        )
    }
}

// ── Camera preview + ML Kit text recognition ──────────────────────────────────

@Composable
private fun CameraPreview(onText: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        val analyzer = TextAnalyzer(onText)
        future.addListener({
            provider = future.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer) }
            runCatching {
                provider?.unbindAll()
                provider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            provider?.unbindAll()
            analyzer.close()
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(factory = { previewView }, modifier = modifier)
}

private class TextAnalyzer(private val onText: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val media = image.image
        if (media == null) {
            image.close()
            return
        }
        val input = InputImage.fromMediaImage(media, image.imageInfo.rotationDegrees)
        recognizer.process(input)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotBlank()) onText(visionText.text)
            }
            .addOnCompleteListener { image.close() }
    }

    fun close() = recognizer.close()
}

// ── Overlay ───────────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.ScannerOverlay(onManual: () -> Unit) {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(300.dp, 150.dp)
            .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
    )
    Surface(
        color = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = 56.dp),
    ) {
        Text(
            text = stringResource(R.string.scan_hint),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
    Surface(
        onClick = onManual,
        color = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = stringResource(R.string.scan_fill_manually),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}

// ── Permission rationale ──────────────────────────────────────────────────────

@Composable
private fun BoxScope.PermissionRationale(onRequest: () -> Unit, onManual: () -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.QrCodeScanner, null, tint = Color.White, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.scan_permission_rationale),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest) { Text(stringResource(R.string.scan_grant_permission)) }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onManual) {
            Text(stringResource(R.string.scan_fill_manually), color = Color.White)
        }
    }
}

// ── Found bottom sheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoundSheet(result: ScanResult, onContinue: () -> Unit, onRetry: () -> Unit) {
    val dims = LocalDimens.current
    val match = result.match
    ModalBottomSheet(onDismissRequest = onRetry) {
        Column(
            modifier = Modifier.padding(dims.xxl).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dims.md),
        ) {
            Text(stringResource(R.string.scan_found_title), style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(dims.radiusSm))
                        .background(Forms.color(match.form)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Forms.icon(match.form), null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(dims.md))
                Column(Modifier.weight(1f)) {
                    Text(match.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${Forms.label(match.form)} · ${Forms.label(match.category)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.scan_continue))
            }
            TextButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.scan_not_it))
            }
            Spacer(Modifier.height(dims.sm))
        }
    }
}
