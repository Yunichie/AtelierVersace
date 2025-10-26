package com.atelierversace.ui.wishlist

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
import com.atelierversace.data.model.Perfume
import com.atelierversace.ui.components.*
import com.atelierversace.ui.discovery.DiscoveryViewModel
import com.atelierversace.ui.theme.*

@Composable
fun WishlistScreen(viewModel: DiscoveryViewModel) {
    val wishlist by viewModel.wishlist.collectAsState()
    var selectedPerfume by remember { mutableStateOf<Perfume?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Cream, Color(0xFFF8F7FF))))
    ) {
        if (selectedPerfume != null) {
            WishlistDetailScreen(
                perfume = selectedPerfume!!,
                onBack = { selectedPerfume = null },
                onRemove = {
                    viewModel.removeFromWishlist(selectedPerfume!!)
                    selectedPerfume = null
                }
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
                                    text = "Wishlist",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 40.sp
                                    ),
                                    color = TextPrimary
                                )

                                if (wishlist.isNotEmpty()) {
                                    Surface(
                                        shape = CircleShape,
                                        color = LightPeriwinkle.copy(alpha = 0.2f),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = "${wishlist.size}",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = LightPeriwinkle,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (wishlist.isEmpty())
                                    "Fragrances you want to try"
                                else
                                    "Your dream fragrances",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Wishlist Grid
                if (wishlist.isEmpty()) {
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
                                text = "Your wishlist is empty",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add fragrances you love from the Discovery tab",
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
                        items(wishlist) { perfume ->
                            GlassCard(
                                onClick = { selectedPerfume = perfume },
                                modifier = Modifier.aspectRatio(0.75f),
                                backgroundColor = Color.White.copy(alpha = 0.2f),
                                borderColor = Color.White.copy(alpha = 0.4f)
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

                                    Spacer(modifier = Modifier.height(12.dp))

                                    IconButton(
                                        onClick = { viewModel.removeFromWishlist(perfume) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Remove",
                                            tint = LightPeriwinkle,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SkyBlue.copy(alpha = 0.15f)
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
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistDetailScreen(
    perfume: Perfume,
    onBack: () -> Unit,
    onRemove: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                    GlassIconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    GlassIconButton(
                        onClick = { showDeleteDialog = true },
                        activeColor = Taupe,
                        isActive = true
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Taupe
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    NotesCard(
                        title = "Top Notes",
                        description = "First impression, lasts 15-30 minutes",
                        notes = topNotes,
                        color = SkyBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (middleNotes.isNotEmpty()) {
                    NotesCard(
                        title = "Middle Notes",
                        description = "Heart of the fragrance, lasts 3-5 hours",
                        notes = middleNotes,
                        color = LightPeriwinkle
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (baseNotes.isNotEmpty()) {
                    NotesCard(
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Remove from Wishlist?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove ${perfume.name} from your wishlist?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                GlassCard(onClick = {
                    showDeleteDialog = false
                    onRemove()
                }) {
                    Box(
                        modifier = Modifier
                            .background(Taupe, shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "Remove",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            dismissButton = {
                GlassCard(onClick = { showDeleteDialog = false }) {
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                        Text(
                            "Cancel",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            containerColor = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun NotesCard(
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