package com.atelierversace.ui.wardrobe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
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
import com.atelierversace.ui.components.GlassCard

@Composable
fun WardrobeScreen(viewModel: WardrobeViewModel) {
    val wardrobe by viewModel.wardrobe.collectAsState()
    val recommendationState by viewModel.recommendationState.collectAsState()

    var showOccasionDialog by remember { mutableStateOf(false) }
    var showRecommendationDialog by remember { mutableStateOf(false) }
    var selectedPerfume by remember { mutableStateOf<Perfume?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F7FF),
                        Color(0xFFF5F5F5)
                    )
                )
            )
    ) {
        if (selectedPerfume != null) {
            // Detail View
            PerfumeDetailScreen(
                perfume = selectedPerfume!!,
                onBack = { selectedPerfume = null },
                onToggleFavorite = { /* TODO: Toggle favorite */ }
            )
        } else {
            // Grid View
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Wardrobe",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 36.sp
                            ),
                            color = Color(0xFF2D2D2D)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
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
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFFE0E0E0)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Your wardrobe is empty",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF2D2D2D),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Scan a perfume to get started!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF8E8E93),
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
                            GlassCard(
                                onClick = { selectedPerfume = perfume },
                                modifier = Modifier.aspectRatio(0.75f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(
                                                Color.White.copy(alpha = 0.5f),
                                                RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        AsyncImage(
                                            model = perfume.imageUri,
                                            contentDescription = perfume.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    IconButton(
                                        onClick = { /* Toggle favorite */ },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Favorite",
                                            tint = Color(0xFFE0E0E0),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = perfume.brand,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF8E8E93)
                                    )

                                    Text(
                                        text = perfume.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 2,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF2D2D2D)
                                    )

                                    Text(
                                        text = perfume.analogy,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF8E8E93),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Occasion Dialog
    if (showOccasionDialog) {
        OccasionDialog(
            onDismiss = { showOccasionDialog = false },
            onSelect = { occasion: String ->
                showOccasionDialog = false
                viewModel.getRecommendation(occasion)
                showRecommendationDialog = true
            }
        )
    }

    // Recommendation Dialog
    if (showRecommendationDialog) {
        RecommendationDialog(
            state = recommendationState,
            onDismiss = {
                showRecommendationDialog = false
                viewModel.resetRecommendation()
            }
        )
    }
}

@Composable
private fun PerfumeDetailScreen(
    perfume: Perfume,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F7FF),
                        Color(0xFFF5F5F5)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Header with Back and Favorite buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF2D2D2D)
                        )
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Color(0xFFE0E0E0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Perfume Image
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = perfume.imageUri,
                            contentDescription = perfume.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Brand and Name
                Text(
                    text = perfume.brand,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = perfume.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF2D2D2D)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Analogy Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF6B4EFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = perfume.analogy,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2D2D2D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Scent Profile Section
                Text(
                    text = "Scent Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF2D2D2D)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Top Notes
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        Color(0xFF6B4EFF),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Top Notes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF2D2D2D)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "First impression, lasts 15-30 minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NoteChip("Rosewood")
                            NoteChip("Cardamom")
                            NoteChip("Chinese Pepper")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Middle Notes
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        Color(0xFF6B4EFF),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Middle Notes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF2D2D2D)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Heart of the fragrance, lasts 3-5 hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NoteChip("Oud")
                            NoteChip("Sandalwood")
                            NoteChip("Vetiver")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Base Notes
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        Color(0xFF6B4EFF),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Base Notes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF2D2D2D)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Long-lasting foundation, lasts 5-10+ hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NoteChip("Tonka Bean")
                            NoteChip("Vanilla")
                            NoteChip("Amber")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun NoteChip(note: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Text(
            text = note,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF2D2D2D)
        )
    }
}

@Composable
private fun OccasionDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val occasions = listOf("Work", "Casual", "Date Night", "Formal Event")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Occasion",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF2D2D2D)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                occasions.forEach { occasion ->
                    GlassCard(
                        onClick = { onSelect(occasion) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                occasion,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF2D2D2D)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = Color(0xFF8E8E93)
                )
            }
        },
        containerColor = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun RecommendationDialog(
    state: RecommendationState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (state) {
                    is RecommendationState.Loading -> "Finding your perfect scent..."
                    is RecommendationState.Success -> "Your Aura Today"
                    is RecommendationState.Error -> "Oops!"
                    else -> "Recommendation"
                },
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF2D2D2D)
            )
        },
        text = {
            when (state) {
                is RecommendationState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF6B4EFF)
                        )
                    }
                }
                is RecommendationState.Success -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AsyncImage(
                                    model = state.perfume.imageUri,
                                    contentDescription = state.perfume.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Fit
                                )

                                Text(
                                    text = state.perfume.brand,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF8E8E93)
                                )

                                Text(
                                    text = state.perfume.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color(0xFF2D2D2D)
                                )

                                Text(
                                    text = state.reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6B4EFF)
                                )
                            }
                        }
                    }
                }
                is RecommendationState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF6B9D),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {}
            }
        },
        confirmButton = {
            GlassCard(
                onClick = onDismiss
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFF6B4EFF),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        "Close",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        containerColor = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp)
    )
}