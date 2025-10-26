package com.atelierversace.ui.scent_lens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*

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
            .background(Brush.verticalGradient(colors = listOf(Cream, Color(0xFFF8F7FF), Color(0xFFF5F5F5))))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val currentState = state) {
                is ScentLensState.Idle -> {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.15f)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.6f),
                                        Color.White.copy(alpha = 0.3f)
                                    )
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.4f),
                                                Color.White.copy(alpha = 0.2f)
                                            )
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = SkyBlue
                                )
                            }

                            Text(
                                text = "Position bottle in frame",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    GlassButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Scan Fragrance",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        )
                    }
                }

                is ScentLensState.Loading -> {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = SkyBlue,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Analyzing fragrance...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedGlassButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        borderColor = TextSecondary.copy(alpha = 0.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Cancel",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                is ScentLensState.Success -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        backgroundColor = Color.White.copy(alpha = 0.25f),
                        borderColor = Color.White.copy(alpha = 0.5f),
                        cornerRadius = 28.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                SkyBlue.copy(alpha = 0.3f),
                                                SkyBlue.copy(alpha = 0.1f)
                                            )
                                        )
                                    )
                                    .border(1.5.dp, SkyBlue.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = SkyBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            GlassBadge(
                                text = currentState.brand,
                                backgroundColor = SkyBlue.copy(alpha = 0.15f),
                                borderColor = SkyBlue.copy(alpha = 0.3f),
                                textColor = SkyBlue
                            )

                            Text(
                                text = currentState.name,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )

                            GlassDivider()

                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GlassIconContainer(
                                    backgroundColor = SkyBlue.copy(alpha = 0.15f),
                                    borderColor = SkyBlue.copy(alpha = 0.3f),
                                    size = 36.dp
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = SkyBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = currentState.profile.analogy,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SkyBlue,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Core Feeling
                            Text(
                                text = currentState.profile.coreFeeling,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = LightPeriwinkle,
                                textAlign = TextAlign.Center
                            )

                            // Local Context
                            Text(
                                text = currentState.profile.localContext,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            GlassButton(
                                onClick = {
                                    viewModel.addToWardrobe(
                                        currentState.brand,
                                        currentState.name,
                                        currentState.profile,
                                        currentState.imageUri
                                    ) {
                                        onNavigateToWardrobe()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Add to Wardrobe",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }

                            OutlinedGlassButton(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = TextSecondary.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    "Cancel",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                is ScentLensState.Error -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        backgroundColor = Color.White.copy(alpha = 0.25f),
                        borderColor = Color.White.copy(alpha = 0.5f),
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Taupe.copy(alpha = 0.3f),
                                                Taupe.copy(alpha = 0.1f)
                                            )
                                        )
                                    )
                                    .border(1.5.dp, Taupe.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Taupe
                                )
                            }

                            Text(
                                text = "Unable to identify",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Taupe
                            )

                            Text(
                                text = currentState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            GlassButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Try Again",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            OutlinedGlassButton(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = TextSecondary.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    "Cancel",
                                    color = TextSecondary,
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