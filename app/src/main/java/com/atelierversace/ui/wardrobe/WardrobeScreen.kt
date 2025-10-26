package com.atelierversace.ui.wardrobe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.atelierversace.data.model.Perfume
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*

@Composable
fun WardrobeScreen(viewModel: WardrobeViewModel) {
    val wardrobe by viewModel.wardrobe.collectAsState()
    val recommendationState by viewModel.recommendationState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    var showRecommendationInput by remember { mutableStateOf(false) }
    var selectedPerfume by remember { mutableStateOf<Perfume?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Cream, Color(0xFFF8F7FF), Color(0xFFF5F5F5))))
    ) {
        if (selectedPerfume != null) {
            PerfumeDetailScreen(
                perfume = selectedPerfume!!,
                isFavorite = viewModel.isFavorite(selectedPerfume!!.id),
                onBack = { selectedPerfume = null },
                onToggleFavorite = { viewModel.toggleFavorite(selectedPerfume!!.id) }
            )
        } else {
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

                                if (wardrobe.isNotEmpty()) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SkyBlue.copy(alpha = 0.2f),
                                        border = BorderStroke(1.5.dp, SkyBlue.copy(alpha = 0.3f)),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = "${wardrobe.size}",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = SkyBlue
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (wardrobe.isEmpty())
                                    "Start building your collection"
                                else
                                    "Your personal fragrance collection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            if (wardrobe.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                GlassButton(
                                    onClick = { showRecommendationInput = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Get AI Recommendation",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Perfume Grid
                if (wardrobe.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
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
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = TextSecondary.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = "Your wardrobe is empty",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Scan a perfume to get started!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(wardrobe) { perfume ->
                            EnhancedPerfumeGridItem(
                                perfume = perfume,
                                isFavorite = viewModel.isFavorite(perfume.id),
                                onClick = { selectedPerfume = perfume },
                                onFavoriteClick = { viewModel.toggleFavorite(perfume.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRecommendationInput) {
        RecommendationInputDialog(
            onDismiss = { showRecommendationInput = false },
            onSubmit = { query ->
                showRecommendationInput = false
                viewModel.getRecommendation(query)
            }
        )
    }

    if (recommendationState is RecommendationState.Loading ||
        recommendationState is RecommendationState.Success ||
        recommendationState is RecommendationState.Error) {
        RecommendationDialog(
            state = recommendationState,
            onDismiss = {
                viewModel.resetRecommendation()
            }
        )
    }
}

@Composable
private fun RecommendationInputDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

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
            cornerRadius = 28.dp
        ) {
            Column(
                modifier = Modifier.padding(28.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassIconContainer(
                        backgroundColor = SkyBlue.copy(alpha = 0.15f),
                        borderColor = SkyBlue.copy(alpha = 0.3f),
                        size = 48.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SkyBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "AI Recommendation",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Describe your mood, occasion, or what you're looking for",
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
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "e.g., something fresh for work, romantic evening scent...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = SkyBlue
                        ),
                        minLines = 3,
                        maxLines = 5
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedGlassButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        borderColor = TextSecondary.copy(alpha = 0.3f)
                    ) {
                        Text(
                            "Cancel",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    GlassButton(
                        onClick = {
                            if (query.isNotBlank()) {
                                onSubmit(query)
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
                        Text(
                            "Recommend",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedPerfumeGridItem(
    perfume: Perfume,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.75f),
        backgroundColor = Color.White.copy(alpha = 0.2f),
        borderColor = Color.White.copy(alpha = 0.4f),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                if (perfume.imageUri.isNotEmpty()) {
                    AsyncImage(
                        model = perfume.imageUri,
                        contentDescription = perfume.name,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextSecondary.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = onFavoriteClick,
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isFavorite) {
                    LightPeriwinkle.copy(alpha = 0.25f)
                } else {
                    Color.White.copy(alpha = 0.2f)
                },
                border = BorderStroke(
                    1.dp,
                    if (isFavorite) {
                        LightPeriwinkle.copy(alpha = 0.5f)
                    } else {
                        Color.White.copy(alpha = 0.3f)
                    }
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isFavorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = "Favorite",
                        tint = if (isFavorite) {
                            LightPeriwinkle
                        } else {
                            TextSecondary.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SkyBlue.copy(alpha = 0.15f),
                border = BorderStroke(0.5.dp, SkyBlue.copy(alpha = 0.3f))
            ) {
                Text(
                    text = perfume.brand,
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBlue,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = perfume.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                textAlign = TextAlign.Center,
                color = TextPrimary
            )

            Text(
                text = perfume.analogy,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                textAlign = TextAlign.Center,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun PerfumeDetailScreen(
    perfume: Perfume,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Cream, Color(0xFFF8F7FF))))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(
                        onClick = onBack,
                        size = 48.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    GlassIconButton(
                        onClick = onToggleFavorite,
                        size = 48.dp,
                        isActive = isFavorite,
                        activeColor = LightPeriwinkle
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = "Favorite",
                            tint = if (isFavorite) LightPeriwinkle else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    backgroundColor = Color.White.copy(alpha = 0.25f),
                    borderColor = Color.White.copy(alpha = 0.4f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (perfume.imageUri.isNotEmpty()) {
                            AsyncImage(
                                model = perfume.imageUri,
                                contentDescription = perfume.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = TextSecondary.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                GlassBadge(
                    text = perfume.brand,
                    backgroundColor = SkyBlue.copy(alpha = 0.15f),
                    borderColor = SkyBlue.copy(alpha = 0.3f),
                    textColor = SkyBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = perfume.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.25f),
                    borderColor = Color.White.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassIconContainer(
                            backgroundColor = SkyBlue.copy(alpha = 0.15f),
                            borderColor = SkyBlue.copy(alpha = 0.3f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SkyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = perfume.analogy,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.25f),
                    borderColor = Color.White.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Core Feeling",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = perfume.coreFeeling,
                            style = MaterialTheme.typography.titleMedium,
                            color = LightPeriwinkle,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.25f),
                    borderColor = Color.White.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Best For",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = perfume.localContext,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Scent Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                val topNotes = perfume.topNotes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val middleNotes = perfume.middleNotes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val baseNotes = perfume.baseNotes.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                if (topNotes.isNotEmpty()) {
                    NotesCardDetail(
                        title = "Top Notes",
                        description = "First impression, lasts 15-30 minutes",
                        notes = topNotes,
                        color = SkyBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (middleNotes.isNotEmpty()) {
                    NotesCardDetail(
                        title = "Middle Notes",
                        description = "Heart of the fragrance, lasts 3-5 hours",
                        notes = middleNotes,
                        color = LightPeriwinkle
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (baseNotes.isNotEmpty()) {
                    NotesCardDetail(
                        title = "Base Notes",
                        description = "Long-lasting foundation, lasts 5-10+ hours",
                        notes = baseNotes,
                        color = Taupe
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun NotesCardDetail(
    title: String,
    description: String,
    notes: List<String>,
    color: Color
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.25f),
        borderColor = Color.White.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                notes.take(3).forEach { note ->
                    GlassChip(text = note)
                }
            }
        }
    }
}

@Composable
private fun RecommendationDialog(
    state: RecommendationState,
    onDismiss: () -> Unit
) {
    when (state) {
        is RecommendationState.Success -> {
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
                    cornerRadius = 28.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            SkyBlue.copy(alpha = 0.3f),
                                            SkyBlue.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                                .border(2.dp, SkyBlue.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = SkyBlue
                            )
                        }

                        Text(
                            text = "Your Perfect Match",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.4f),
                            borderColor = Color.White.copy(alpha = 0.6f),
                            cornerRadius = 20.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (state.perfume.imageUri.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.White.copy(alpha = 0.3f),
                                                        Color.White.copy(alpha = 0.2f)
                                                    )
                                                )
                                            )
                                            .border(
                                                1.dp,
                                                Color.White.copy(alpha = 0.4f),
                                                RoundedCornerShape(16.dp)
                                            )
                                    ) {
                                        AsyncImage(
                                            model = state.perfume.imageUri,
                                            contentDescription = state.perfume.name,
                                            modifier = Modifier.fillMaxSize().padding(12.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                GlassBadge(
                                    text = state.perfume.brand,
                                    backgroundColor = SkyBlue.copy(alpha = 0.15f),
                                    borderColor = SkyBlue.copy(alpha = 0.3f),
                                    textColor = SkyBlue
                                )

                                Text(
                                    text = state.perfume.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )

                                GlassDivider()

                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    GlassIconContainer(
                                        backgroundColor = SkyBlue.copy(alpha = 0.15f),
                                        borderColor = SkyBlue.copy(alpha = 0.3f),
                                        size = 36.dp
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WbSunny,
                                            contentDescription = null,
                                            tint = SkyBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = state.reason,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        GlassButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Perfect!",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
        is RecommendationState.Loading -> {
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
                    cornerRadius = 28.dp
                ) {
                    Column(
                        modifier = Modifier.padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            SkyBlue.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .border(2.dp, SkyBlue.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = SkyBlue,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.dp
                            )
                        }

                        Text(
                            "Analyzing your wardrobe...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            "Considering weather & your preferences",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        is RecommendationState.Error -> {
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
                            text = "Oops!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Taupe
                        )

                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        GlassButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Close",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        else -> {}
    }
}