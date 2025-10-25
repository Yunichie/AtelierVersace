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
import androidx.compose.material.icons.filled.*
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
import com.atelierversace.ui.theme.*

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
            .background(Brush.verticalGradient(colors = listOf(Cream, Color(0xFFF8F7FF))))
    ) {
        if (selectedPerfume != null) {
            PerfumeDetailScreen(
                perfume = selectedPerfume!!,
                onBack = { selectedPerfume = null },
                onToggleFavorite = { /* TODO */ }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Improved Header
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
                                        Cream.copy(alpha = 0.9f),
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
                                        color = SkyBlue.copy(alpha = 0.1f),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = "${wardrobe.size}",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = SkyBlue,
                                                fontWeight = FontWeight.Bold
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
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
                            GlassCard(
                                onClick = { selectedPerfume = perfume },
                                modifier = Modifier.aspectRatio(0.75f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
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
                                            modifier = Modifier.fillMaxSize().padding(8.dp),
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
                                        color = TextSecondary
                                    )

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
                    }
                }
            }
        }
    }

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
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
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

                GlassCard(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
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

                Text(
                    text = perfume.brand,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = perfume.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SkyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = perfume.analogy,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Scent Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Parse and display notes from perfume data
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
    GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                    NoteChipDetail(note)
                }
            }
        }
    }
}

@Composable
private fun NoteChipDetail(note: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Text(
            text = note,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
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
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                occasions.forEach { occasion ->
                    GlassCard(
                        onClick = { onSelect(occasion) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                occasion,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
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
                color = TextPrimary
            )
        },
        text = {
            when (state) {
                is RecommendationState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SkyBlue)
                    }
                }

                is RecommendationState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                                    color = TextSecondary
                                )

                                Text(
                                    text = state.perfume.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = TextPrimary
                                )

                                Text(
                                    text = state.reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SkyBlue
                                )
                            }
                        }
                    }
                }

                is RecommendationState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Taupe,
                        textAlign = TextAlign.Center
                    )
                }

                else -> {}
            }
        },
        confirmButton = {
            GlassCard(onClick = onDismiss) {
                Box(
                    modifier = Modifier
                        .background(SkyBlue, shape = RoundedCornerShape(20.dp))
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