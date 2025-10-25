package com.atelierversace.ui.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.atelierversace.data.model.Perfume
import com.atelierversace.ui.components.GlassCard
import com.atelierversace.ui.discovery.DiscoveryViewModel

@Composable
fun WishlistScreen(viewModel: DiscoveryViewModel) {
    val wishlist by viewModel.wishlist.collectAsState()
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
            WishlistDetailScreen(
                perfume = selectedPerfume!!,
                onBack = { selectedPerfume = null },
                onRemove = {
                    viewModel.removeFromWishlist(selectedPerfume!!)
                    selectedPerfume = null
                }
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
                            text = "Wishlist",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 36.sp
                            ),
                            color = Color(0xFF2D2D2D)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
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
                                color = Color(0xFF2D2D2D),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add fragrances you love from the Discovery tab",
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
                        items(wishlist) { perfume ->
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
                                        // Placeholder for image
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = Color(0xFFE0E0E0)
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
                                            tint = Color(0xFFFF6B9D),
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

                // Header with Back and Remove buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_revert),
                            contentDescription = "Back",
                            tint = Color(0xFF2D2D2D)
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Color(0xFFFF6B9D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Perfume Image Placeholder
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
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFFE0E0E0)
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
                            painter = painterResource(android.R.drawable.ic_dialog_info),
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

                Spacer(modifier = Modifier.height(16.dp))

                // Core Feeling Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Core Feeling",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF8E8E93)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = perfume.coreFeeling,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF6B9D),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Local Context Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Best For",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF8E8E93)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = perfume.localContext,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2D2D2D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Remove from Wishlist?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2D2D2D)
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove ${perfume.name} from your wishlist?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8E8E93)
                )
            },
            confirmButton = {
                GlassCard(
                    onClick = {
                        showDeleteDialog = false
                        onRemove()
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Color(0xFFFF6B9D),
                                shape = RoundedCornerShape(20.dp)
                            )
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
                GlassCard(
                    onClick = { showDeleteDialog = false }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "Cancel",
                            color = Color(0xFF8E8E93),
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