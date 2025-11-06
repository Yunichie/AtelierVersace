package com.atelierversace.ui.wishlist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.ui.components.*
import com.atelierversace.ui.discovery.DiscoveryViewModel
import com.atelierversace.ui.theme.*

@Composable
fun WishlistScreen(viewModel: DiscoveryViewModel) {
    val wishlist by viewModel.wishlist.collectAsState()
    var selectedPerfume by remember { mutableStateOf<PerfumeCloud?>(null) }

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

                        Text(
                            text = "Wishlist",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${wishlist.size} perfumes you'd love to try",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (wishlist.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyWishlistContent()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(wishlist) { perfume ->
                        WishlistItemCard(
                            perfume = perfume,
                            onRemove = { viewModel.removeFromWishlist(perfume) },
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
        WishlistDetailDialog(
            perfume = perfume,
            onRemove = {
                viewModel.removeFromWishlist(perfume)
                selectedPerfume = null
            },
            onDismiss = { selectedPerfume = null }
        )
    }
}

@Composable
private fun EmptyWishlistContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )

        Text(
            text = "Your wishlist is empty",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Discover new perfumes and add them to your wishlist",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WishlistItemCard(
    perfume: PerfumeCloud,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.25f),
        borderColor = Color.White.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = SkyBlue.copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = SkyBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )

                Text(
                    text = perfume.coreFeeling,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2
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
    }
}

@Composable
private fun WishlistDetailDialog(
    perfume: PerfumeCloud,
    onRemove: () -> Unit,
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

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedGlassButton(
                        onClick = onRemove,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Taupe
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove", color = Taupe, fontSize = 14.sp)
                    }

                    GlassButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = Color.White, fontSize = 14.sp)
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