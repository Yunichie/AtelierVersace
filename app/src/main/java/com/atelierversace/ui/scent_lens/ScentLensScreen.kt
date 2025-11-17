package com.atelierversace.ui.scent_lens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*

@Composable
fun ScentLensScreen(
    viewModel: ScentLensViewModel,
    onNavigateToWardrobe: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var userId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val authRepo = com.atelierversace.data.repository.AuthRepository()
        val user = authRepo.getCurrentUser()
        userId = user?.id ?: ""
        println("DEBUG - ScentLensScreen userId: $userId")
    }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            showImageSourceDialog = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            capturedBitmap = it
            imageUri = null

            println("DEBUG - Captured bitmap: ${it.width}x${it.height}")
            viewModel.analyzePerfume(it, "captured_image")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            capturedBitmap = null

            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    println("DEBUG - Gallery bitmap: ${bitmap.width}x${bitmap.height}")
                    capturedBitmap = bitmap
                    viewModel.analyzePerfume(bitmap, it.toString())
                } else {
                    println("ERROR - Failed to decode bitmap from URI")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Failed to load image: ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SoftPeriwinkle, Color(0xFFF8F7FF), Color(0xFFF5F5F5))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Scent Lens",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scan your perfume bottle to discover its personality",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                },
                label = "scent_lens_state"
            ) { currentState ->
                when (currentState) {
                    is ScentLensState.Idle -> {
                        IdleContent(
                            onCaptureClick = {
                                if (hasCameraPermission) {
                                    showImageSourceDialog = true
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        )
                    }

                    is ScentLensState.Loading -> {
                        LoadingContent(
                            capturedBitmap = capturedBitmap
                        )
                    }

                    is ScentLensState.Success -> {
                        SuccessContent(
                            brand = currentState.brand,
                            name = currentState.name,
                            profile = currentState.profile,
                            capturedBitmap = capturedBitmap,
                            onAddToWardrobe = {
                                if (userId.isEmpty()) {
                                    println("ERROR - No userId available")
                                    viewModel.reset()
                                } else {
                                    println("DEBUG - Adding to wardrobe with userId: $userId")
                                    viewModel.addToWardrobe(
                                        brand = currentState.brand,
                                        name = currentState.name,
                                        profile = currentState.profile,
                                        imageUri = currentState.imageUri,
                                        userId = userId,
                                        context = context,
                                        onComplete = onNavigateToWardrobe
                                    )
                                }
                            },
                            onScanAnother = {
                                viewModel.reset()
                                imageUri = null
                                capturedBitmap = null
                            }
                        )
                    }

                    is ScentLensState.Error -> {
                        ErrorContent(
                            message = currentState.message,
                            onRetry = {
                                viewModel.reset()
                                imageUri = null
                                capturedBitmap = null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showImageSourceDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.padding(32.dp),
                backgroundColor = Color.White.copy(alpha = 0.95f),
                borderColor = Color.White.copy(alpha = 0.6f),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Choose Image Source",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    GlassButton(
                        onClick = {
                            showImageSourceDialog = false
                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo", color = Color.White)
                    }

                    OutlinedGlassButton(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = IceBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery", color = IceBlue)
                    }

                    TextButton(
                        onClick = { showImageSourceDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleContent(onCaptureClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        GlassCard(
            modifier = Modifier
                .size(280.dp)
                .padding(16.dp),
            backgroundColor = Color.White.copy(alpha = 0.2f),
            borderColor = Color.White.copy(alpha = 0.4f),
            cornerRadius = 140.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = IceBlue.copy(alpha = 0.5f)
                )
            }
        }

        GlassButton(
            onClick = onCaptureClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Scan Perfume",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun LoadingContent(
    capturedBitmap: Bitmap?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            backgroundColor = Color.White.copy(alpha = 0.25f),
            borderColor = Color.White.copy(alpha = 0.4f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Text(
            text = "Analyzing your perfume...",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SuccessContent(
    brand: String,
    name: String,
    profile: PersonaProfile,
    capturedBitmap: Bitmap?,
    onAddToWardrobe: () -> Unit,
    onScanAnother: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            backgroundColor = Color.White.copy(alpha = 0.25f),
            borderColor = Color.White.copy(alpha = 0.4f)
        ) {
            if (capturedBitmap != null) {
                Image(
                    bitmap = capturedBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.25f),
            borderColor = Color.White.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = brand,
                    style = MaterialTheme.typography.titleMedium,
                    color = IceBlue,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
            }
        }

        PersonaProfileCard(profile = profile)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedGlassButton(
                onClick = onScanAnother,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan Another", color = TextSecondary, fontSize = 14.sp)
            }

            GlassButton(
                onClick = onAddToWardrobe,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Wardrobe", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Cornflower.copy(alpha = 0.5f)
        )

        Text(
            text = "Oops!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        GlassButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again", color = Color.White)
        }
    }
}

@Composable
private fun PersonaProfileCard(profile: PersonaProfile) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.25f),
        borderColor = Color.White.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Analogy",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    profile.analogy,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }

            GlassDivider()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Core Feeling",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    profile.coreFeeling,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }

            GlassDivider()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotesSection("Top Notes", profile.topNotes, IceBlue)
                NotesSection("Middle Notes", profile.middleNotes, Periwinkle)
                NotesSection("Base Notes", profile.baseNotes, Cornflower)
            }
        }
    }
}

@Composable
private fun NotesSection(label: String, notes: List<String>, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            notes.take(3).forEach { note ->
                GlassBadge(
                    text = note,
                    backgroundColor = color.copy(alpha = 0.15f),
                    borderColor = color.copy(alpha = 0.3f),
                    textColor = color
                )
            }
        }
    }
}