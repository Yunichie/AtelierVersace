package com.atelierversace.ui.scent_lens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atelierversace.ui.components.GlassCard

@Composable
fun ScentLensScreen(
    viewModel: ScentLensViewModel,
    onNavigateToWardrobe: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                viewModel.analyzePerfume(bitmap, it.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F5F5),
                        Color(0xFFF8F7FF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val currentState = state) {
                is ScentLensState.Idle -> {
                    // Camera Frame
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .background(
                                Color.White.copy(alpha = 0.3f),
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFFBBBBBB)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Position bottle in frame",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8E93)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    GlassCard(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF6B4EFF),
                                            Color(0xFF8B6EFF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Scan Fragrance",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                is ScentLensState.Loading -> {
                    CircularProgressIndicator(
                        color = Color(0xFF6B4EFF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Analyzing fragrance...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF8E8E93)
                    )
                }

                is ScentLensState.Success -> {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = currentState.brand,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF8E8E93)
                            )

                            Text(
                                text = currentState.name,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF2D2D2D),
                                textAlign = TextAlign.Center
                            )

                            Divider(
                                color = Color(0xFFE0E0E0),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Text(
                                text = currentState.profile.analogy,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF6B4EFF),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = currentState.profile.coreFeeling,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFFFF6B9D),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = currentState.profile.localContext,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8E93),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            GlassCard(
                                onClick = {
                                    viewModel.addToWardrobe(
                                        currentState.brand,
                                        currentState.name,
                                        currentState.profile,
                                        currentState.imageUri
                                    )
                                    onNavigateToWardrobe()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF6B4EFF),
                                                    Color(0xFF8B6EFF)
                                                )
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Add to Wardrobe",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                is ScentLensState.Error -> {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Unable to identify",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFFF6B9D)
                            )
                            Text(
                                text = currentState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8E93),
                                textAlign = TextAlign.Center
                            )

                            GlassCard(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.5.dp,
                                            color = Color(0xFF6B4EFF).copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Try Again",
                                        color = Color(0xFF6B4EFF),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
