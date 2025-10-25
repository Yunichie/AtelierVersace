package com.atelierversace.ui.discovery

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.ui.components.GlassCard
import com.atelierversace.ui.theme.*

@Composable
fun DiscoveryScreen(viewModel: DiscoveryViewModel) {
    val discoveryState by viewModel.discoveryState.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()

    var query by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf<PersonaProfile?>(null) }

    if (selectedProfile != null) {
        DiscoveryDetailScreen(
            profile = selectedProfile!!,
            isInWishlist = wishlistItems.contains("${selectedProfile!!.brand}|${selectedProfile!!.name}"),
            onBack = { selectedProfile = null },
            onToggleWishlist = {
                viewModel.toggleWishlist(selectedProfile!!)
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Cream, Color(0xFFF8F7FF), Color(0xFFF5F5F5))
                    )
                )
        ) {
            AnimatedContent(
                targetState = isSearchExpanded,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it / 2 }
                    ) togetherWith fadeOut(animationSpec = tween(200)) + slideOutVertically(
                        animationSpec = tween(200),
                        targetOffsetY = { -it / 2 }
                    )
                },
                label = "search_transition"
            ) { expanded ->
                if (!expanded) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "the perfect\nfragrance for you",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 32.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        GlassCard(
                            onClick = { isSearchExpanded = true },
                            modifier = Modifier.padding(horizontal = 40.dp).height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = SkyBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Find",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                } else {
                    // Expanded State
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Spacer(modifier = Modifier.height(16.dp))

                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = SkyBlue,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        TextField(
                                            value = query,
                                            onValueChange = { query = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = {
                                                Text(
                                                    "search fragrances...",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextSecondary
                                                )
                                            },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                cursorColor = SkyBlue
                                            ),
                                            singleLine = false,
                                            maxLines = 3
                                        )

                                        IconButton(onClick = {
                                            isSearchExpanded = false
                                            query = ""
                                            viewModel.reset()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = TextSecondary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                GlassCard(
                                    onClick = { viewModel.searchPerfumes(query) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(SkyBlue, LightPeriwinkle)
                                                ),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(vertical = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Search",
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Content
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (val state = discoveryState) {
                                is DiscoveryState.Idle -> {}

                                is DiscoveryState.Loading -> {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = SkyBlue)
                                        }
                                    }
                                }

                                is DiscoveryState.Success -> {
                                    items(state.recommendations) { profile ->
                                        val isInWishlist = wishlistItems.contains("${profile.brand}|${profile.name}")

                                        GlassCard(
                                            onClick = { selectedProfile = profile },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                if (profile.brand.isNotEmpty() && profile.name.isNotEmpty()) {
                                                    Text(
                                                        text = profile.brand,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = TextSecondary
                                                    )
                                                    Text(
                                                        text = profile.name,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        color = TextPrimary
                                                    )
                                                }

                                                Text(
                                                    text = profile.analogy,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = SkyBlue
                                                )

                                                Text(
                                                    text = profile.coreFeeling,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = LightPeriwinkle
                                                )

                                                Text(
                                                    text = profile.localContext,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextSecondary
                                                )

                                                GlassCard(
                                                    onClick = { viewModel.toggleWishlist(profile) },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .border(
                                                                width = 1.5.dp,
                                                                color = if (isInWishlist)
                                                                    LightPeriwinkle.copy(alpha = 0.5f)
                                                                else
                                                                    LightPeriwinkle.copy(alpha = 0.3f),
                                                                shape = RoundedCornerShape(20.dp)
                                                            )
                                                            .background(
                                                                if (isInWishlist)
                                                                    LightPeriwinkle.copy(alpha = 0.2f)
                                                                else
                                                                    Color.Transparent,
                                                                shape = RoundedCornerShape(20.dp)
                                                            )
                                                            .padding(vertical = 12.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isInWishlist)
                                                                    Icons.Filled.Favorite
                                                                else
                                                                    Icons.Outlined.FavoriteBorder,
                                                                contentDescription = null,
                                                                tint = LightPeriwinkle,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                if (isInWishlist) "Added to Wishlist" else "Add to Wishlist",
                                                                color = LightPeriwinkle,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                is DiscoveryState.Error -> {
                                    item {
                                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = state.message,
                                                    color = Taupe,
                                                    textAlign = TextAlign.Center
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
        }
    }
}

@Composable
private fun DiscoveryDetailScreen(
    profile: PersonaProfile,
    isInWishlist: Boolean,
    onBack: () -> Unit,
    onToggleWishlist: () -> Unit
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

                    IconButton(onClick = onToggleWishlist) {
                        Icon(
                            imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Toggle Wishlist",
                            tint = if (isInWishlist) LightPeriwinkle else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brand and Name
                Text(
                    text = profile.brand,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Analogy Card
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
                            text = profile.analogy,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Core Feeling
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Core Feeling",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile.coreFeeling,
                            style = MaterialTheme.typography.titleMedium,
                            color = LightPeriwinkle,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Local Context
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Best For",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile.localContext,
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

                NotesCard(
                    title = "Top Notes",
                    description = "First impression, lasts 15-30 minutes",
                    notes = profile.topNotes,
                    color = SkyBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                NotesCard(
                    title = "Middle Notes",
                    description = "Heart of the fragrance, lasts 3-5 hours",
                    notes = profile.middleNotes,
                    color = LightPeriwinkle
                )

                Spacer(modifier = Modifier.height(12.dp))

                NotesCard(
                    title = "Base Notes",
                    description = "Long-lasting foundation, lasts 5-10+ hours",
                    notes = profile.baseNotes,
                    color = Taupe
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun NotesCard(
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
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
                    NoteChip(note)
                }
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
            color = TextPrimary
        )
    }
}