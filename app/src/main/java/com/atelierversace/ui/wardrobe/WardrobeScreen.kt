package com.atelierversace.ui.wardrobe

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*

@Composable
fun WardrobeScreen(viewModel: WardrobeViewModel) {
    val wardrobe by viewModel.wardrobe.collectAsState()
    val recommendationState by viewModel.recommendationState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    var selectedPerfume by remember { mutableStateOf<PerfumeCloud?>(null) }
    var showRecommendationDialog by remember { mutableStateOf(false) }
    var userQuery by remember { mutableStateOf("") }

    val userId = remember {
        com.atelierversace.data.repository.AuthRepository().getCurrentUser()?.id ?: ""
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.initialize(userId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Cream, Color(0xFFF8F7FF), Color(0xFFF5F5F5))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Wardrobe",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 40.sp
                                ),
                                color = TextPrimary
                            )

                            IconButton(
                                onClick = { showRecommendationDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Get Recommendation",
                                    tint = SkyBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${wardrobe.size} fragrances in your collection",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (wardrobe.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyWardrobeContent()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(wardrobe) { perfume ->
                        PerfumeCard(
                            perfume = perfume,
                            isFavorite = viewModel.isFavorite(perfume.id ?: ""),
                            onToggleFavorite = {
                                viewModel.toggleFavorite(perfume.id ?: "")
                            },
                            onClick = { selectedPerfume = perfume }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    selectedPerfume?.let { perfume ->
        PerfumeDetailDialog(
            perfume = perfume,
            isFavorite = viewModel.isFavorite(perfume.id ?: ""),
            onToggleFavorite = {
                viewModel.toggleFavorite(perfume.id ?: "")
            },
            onDismiss = { selectedPerfume = null }
        )
    }

    if (showRecommendationDialog) {
        RecommendationDialog(
            query = userQuery,
            onQueryChange = { userQuery = it },
            onGetRecommendation = {
                viewModel.getRecommendation(userQuery)
            },
            onDismiss = {
                showRecommendationDialog = false
                userQuery = ""
            }
        )
    }

    when (val state = recommendationState) {
        is RecommendationState.Success -> {
            RecommendationResultDialog(
                perfume = state.perfume,
                reason = state.reason,
                onDismiss = {
                    viewModel.resetRecommendation()
                }
            )
        }
        is RecommendationState.Error -> {
            ErrorDialog(
                message = state.message,
                onDismiss = {
                    viewModel.resetRecommendation()
                }
            )
        }
        else -> {}
    }
}

@Composable
private fun EmptyWardrobeContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )

        Text(
            text = "Your wardrobe is empty",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Use Scent Lens to scan and add your first perfume",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PerfumeCard(
    perfume: PerfumeCloud,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f),
        backgroundColor = Color.White.copy(alpha = 0.25f),
        borderColor = Color.White.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (perfume.imageUri.isNotEmpty()) {
                    AsyncImage(
                        model = perfume.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        SkyBlue.copy(alpha = 0.2f),
                                        LightPeriwinkle.copy(alpha = 0.2f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = SkyBlue.copy(alpha = 0.4f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Surface(
                        onClick = onToggleFavorite,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavorite) Taupe else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = perfume.brand,
                    style = MaterialTheme.typography.labelMedium,
                    color = SkyBlue,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = perfume.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun PerfumeDetailDialog(
    perfume: PerfumeCloud,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.95f),
            borderColor = Color.White.copy(alpha = 0.6f),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            perfume.brand,
                            style = MaterialTheme.typography.labelLarge,
                            color = SkyBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            perfume.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavorite) Taupe else TextSecondary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                GlassDivider()
                Spacer(modifier = Modifier.height(20.dp))

                DetailSection("Analogy", perfume.analogy)
                Spacer(modifier = Modifier.height(16.dp))

                DetailSection("Core Feeling", perfume.coreFeeling)
                Spacer(modifier = Modifier.height(16.dp))

                DetailSection("Local Context", perfume.localContext)
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NotesRow("Top Notes", perfume.topNotes.split(",").map { it.trim() }, SkyBlue)
                    NotesRow("Middle Notes", perfume.middleNotes.split(",").map { it.trim() }, LightPeriwinkle)
                    NotesRow("Base Notes", perfume.baseNotes.split(",").map { it.trim() }, Taupe)
                }
            }
        }
    }
}

@Composable
private fun DetailSection(label: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Text(
            content,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}

@Composable
private fun NotesRow(label: String, notes: List<String>, color: Color) {
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
            notes.take(3).filter { it.isNotBlank() }.forEach { note ->
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

@Composable
private fun RecommendationDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    onGetRecommendation: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    "Get AI Recommendation",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )

                Text(
                    "Describe the occasion or your mood",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.3f),
                    borderColor = Color.White.copy(alpha = 0.5f)
                ) {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "e.g., casual day out, romantic dinner...",
                                color = TextSecondary.copy(alpha = 0.7f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = SkyBlue
                        ),
                        minLines = 2
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedGlassButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    GlassButton(
                        onClick = {
                            if (query.isNotBlank()) {
                                onGetRecommendation()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Get Recommendation", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationResultDialog(
    perfume: PerfumeCloud,
    reason: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.95f),
            borderColor = Color.White.copy(alpha = 0.6f),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "AI Recommendation",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                GlassBadge(
                    text = perfume.brand,
                    backgroundColor = SkyBlue.copy(alpha = 0.15f),
                    borderColor = SkyBlue.copy(alpha = 0.3f),
                    textColor = SkyBlue
                )

                Text(
                    perfume.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )

                GlassDivider()

                Text(
                    reason,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )

                GlassButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it!", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Taupe
                )

                Text(
                    "Oops!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )

                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                GlassButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Okay", color = Color.White)
                }
            }
        }
    }
}