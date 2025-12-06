package com.atelierversace.ui.wardrobe

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.atelierversace.data.remote.LayeringRecommendation
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*

@Composable
fun WardrobeScreen(viewModel: WardrobeViewModel) {
    val wardrobe by viewModel.wardrobe.collectAsState()
    val recommendationState by viewModel.recommendationState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val savedLayerings by viewModel.savedLayerings.collectAsState()
    val isLoadingLayerings by viewModel.isLoadingLayerings.collectAsState()

    var selectedPerfume by remember { mutableStateOf<PerfumeCloud?>(null) }
    var showRecommendationDialog by remember { mutableStateOf(false) }
    var userQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    val userId = remember {
        com.atelierversace.data.repository.AuthRepository().getCurrentUser()?.id ?: ""
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.initialize(userId)
        }
    }

    CenteredGradientBackground {
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
                                    MintGreen.copy(alpha = 0.15f),
                                    SunsetOrange.copy(alpha = 0.1f),
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
                                    tint = Cornflower,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (selectedTab == 0)
                                "${wardrobe.size} fragrances in your collection"
                            else
                                "${savedLayerings.size} saved layering combinations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TabChip(
                                text = "My Perfumes",
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            TabChip(
                                text = "Saved Layerings",
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith
                            fadeOut() + slideOutHorizontally()
                },
                label = "wardrobe_tabs"
            ) { tab ->
                when (tab) {
                    0 -> MyPerfumesTab(
                        wardrobe = wardrobe,
                        favoriteIds = favoriteIds,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onPerfumeClick = { selectedPerfume = it }
                    )
                    1 -> SavedLayeringsTab(
                        savedLayerings = savedLayerings,
                        isLoading = isLoadingLayerings,
                        onRemove = { viewModel.removeSavedLayering(it) }
                    )
                }
            }
        }
    }

    selectedPerfume?.let { perfume ->
        val isFavorite = favoriteIds.contains(perfume.id ?: "")

        PerfumeDetailDialog(
            perfume = perfume,
            isFavorite = isFavorite,
            onToggleFavorite = {
                viewModel.toggleFavorite(perfume.id ?: "")
            },
            onDelete = {
                viewModel.deletePerfume(perfume.id ?: "")
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
private fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            Cornflower.copy(alpha = 0.25f)
        } else {
            Color.White.copy(alpha = 0.25f)
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) {
                Cornflower.copy(alpha = 0.5f)
            } else {
                Color.White.copy(alpha = 0.4f)
            }
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) Cornflower else TextSecondary
        )
    }
}

@Composable
private fun MyPerfumesTab(
    wardrobe: List<PerfumeCloud>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onPerfumeClick: (PerfumeCloud) -> Unit
) {
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
            items(wardrobe, key = { it.id ?: it.name }) { perfume ->
                val isFavorite = favoriteIds.contains(perfume.id ?: "")

                PerfumeCard(
                    perfume = perfume,
                    isFavorite = isFavorite,
                    onToggleFavorite = {
                        onToggleFavorite(perfume.id ?: "")
                    },
                    onClick = { onPerfumeClick(perfume) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SavedLayeringsTab(
    savedLayerings: List<Triple<LayeringRecommendation, PerfumeCloud?, PerfumeCloud?>>,
    isLoading: Boolean,
    onRemove: (String) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Cornflower)
        }
    } else if (savedLayerings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyLayeringsContent()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(savedLayerings) { (layering, base, layer) ->
                if (base != null && layer != null) {
                    SavedLayeringCard(
                        layering = layering,
                        basePerfume = base,
                        layerPerfume = layer,
                        onRemove = { onRemove(layering.id ?: "") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
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
private fun EmptyLayeringsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )

        Text(
            text = "No saved layerings yet",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Save your favorite layering combinations from AI Recommendations",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SavedLayeringCard(
    layering: LayeringRecommendation,
    basePerfume: PerfumeCloud,
    layerPerfume: PerfumeCloud,
    onRemove: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.25f),
        borderColor = Color.White.copy(alpha = 0.4f),
        cornerRadius = 20.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val name = layering.reasoning.lines().firstOrNull() ?: "Layering Combination"
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Taupe
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassChip(text = basePerfume.brand)
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                GlassChip(text = layerPerfume.brand)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${basePerfume.name} + ${layerPerfume.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val description = layering.reasoning.lines().drop(1).joinToString("\n").trim()
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassBadge(
                text = layering.occasion,
                backgroundColor = Cornflower.copy(alpha = 0.15f),
                borderColor = Cornflower.copy(alpha = 0.3f),
                textColor = Cornflower
            )
        }
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
                                        IceBlue.copy(alpha = 0.2f),
                                        Periwinkle.copy(alpha = 0.2f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = IceBlue.copy(alpha = 0.4f)
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
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
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
                    color = Cornflower,
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
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
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
                        "Delete Perfume?",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    Text(
                        "Are you sure you want to remove ${perfume.brand} ${perfume.name} from your wardrobe?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedGlassButton(
                            onClick = { showDeleteConfirmation = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }

                        GlassButton(
                            onClick = {
                                showDeleteConfirmation = false
                                onDelete()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            gradient = Brush.horizontalGradient(
                                colors = listOf(Cornflower, Cornflower.copy(alpha = 0.8f))
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", color = Color.White)
                        }
                    }
                }
            }
        }
    } else {
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
                                color = Cornflower,
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
                                    contentDescription = "Toggle Favorite",
                                    tint = if (isFavorite) Cornflower else TextSecondary
                                )
                            }

                            IconButton(
                                onClick = { showDeleteConfirmation = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Taupe
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
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
                        NotesRow("Top Notes", perfume.topNotes.split(",").map { it.trim() }, VibrantPurple)
                        NotesRow("Middle Notes", perfume.middleNotes.split(",").map { it.trim() }, Cornflower)
                        NotesRow("Base Notes", perfume.baseNotes.split(",").map { it.trim() }, SkyBlue)
                    }
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
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Get AI Recommendation",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // SENOPATI Badge
                    GlassBadge(
                        text = "powered by SENOPATI",
                        backgroundColor = Cornflower.copy(alpha = 0.15f),
                        borderColor = Cornflower.copy(alpha = 0.3f),
                        textColor = Cornflower
                    )
                }

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
                            cursorColor = IceBlue
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
                    backgroundColor = IceBlue.copy(alpha = 0.15f),
                    borderColor = IceBlue.copy(alpha = 0.3f),
                    textColor = IceBlue
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
                    tint = Cornflower
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