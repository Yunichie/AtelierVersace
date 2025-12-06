package com.atelierversace.ui.discovery

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*

@Composable
fun DiscoveryScreen(viewModel: DiscoveryViewModel) {
    val discoveryState by viewModel.discoveryState.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()

    LaunchedEffect(Unit) {
        val authRepo = com.atelierversace.data.repository.AuthRepository()
        val user = authRepo.getCurrentUser()
        user?.let {
            println("DEBUG - Initializing DiscoveryScreen with userId: ${it.id}")
            viewModel.initialize(it.id)
        }
    }

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
        CenteredGradientBackground {
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
                                fontSize = 32.sp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        VibrantPurple,
                                        ElectricSapphire,
                                        Cornflower
                                    )
                                )
                            ),
                            textAlign = TextAlign.Center,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        GlassBadge(
                            text = "powered by SENOPATI",
                            backgroundColor = ElectricSapphire.copy(alpha = 0.15f),
                            borderColor = ElectricSapphire.copy(alpha = 0.3f),
                            textColor = ElectricSapphire
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        GlassCard(
                            onClick = { isSearchExpanded = true },
                            modifier = Modifier.padding(horizontal = 40.dp).height(56.dp),
                            backgroundColor = Color.White.copy(alpha = 0.25f),
                            borderColor = Color.White.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Cornflower,
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Spacer(modifier = Modifier.height(16.dp))

                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = Color.White.copy(alpha = 0.25f),
                                    borderColor = Color.White.copy(alpha = 0.4f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Cornflower,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        TextField(
                                            value = query,
                                            onValueChange = { query = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = {
                                                Text(
                                                    "describe your perfect scent...",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextSecondary.copy(alpha = 0.7f)
                                                )
                                            },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                cursorColor = Cornflower
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

                                GlassButton(
                                    onClick = { viewModel.searchPerfumes(query) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Discover",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
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
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    color = Cornflower,
                                                    modifier = Modifier.size(48.dp),
                                                    strokeWidth = 3.dp
                                                )
                                                Text(
                                                    "Finding your perfect scent...",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }

                                is DiscoveryState.Success -> {
                                    items(state.recommendations) { profile ->
                                        val isInWishlist = wishlistItems.contains("${profile.brand}|${profile.name}")

                                        EnhancedResultCard(
                                            profile = profile,
                                            isInWishlist = isInWishlist,
                                            onClick = { selectedProfile = profile },
                                            onToggleWishlist = { viewModel.toggleWishlist(profile) }
                                        )
                                    }
                                }

                                is DiscoveryState.Error -> {
                                    item {
                                        GlassCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            backgroundColor = Color.White.copy(alpha = 0.25f),
                                            borderColor = Color.White.copy(alpha = 0.4f)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = Cornflower,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                    Text(
                                                        text = state.message,
                                                        color = Cornflower,
                                                        textAlign = TextAlign.Center,
                                                        style = MaterialTheme.typography.bodyMedium
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
}

@Composable
private fun EnhancedResultCard(
    profile: PersonaProfile,
    isInWishlist: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.2f),
        borderColor = Color.White.copy(alpha = 0.4f),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (profile.brand.isNotEmpty() && profile.name.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        GlassBadge(
                            text = profile.brand,
                            backgroundColor = Cornflower.copy(alpha = 0.15f),
                            borderColor = Cornflower.copy(alpha = 0.3f),
                            textColor = Cornflower
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                    }

                    GlassIconButton(
                        onClick = onToggleWishlist,
                        size = 40.dp,
                        isActive = isInWishlist,
                        activeColor = Periwinkle
                    ) {
                        Icon(
                            imageVector = if (isInWishlist) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = null,
                            tint = Periwinkle,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            GlassDivider()

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassIconContainer(
                    backgroundColor = Cornflower.copy(alpha = 0.15f),
                    borderColor = Cornflower.copy(alpha = 0.3f),
                    size = 36.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Cornflower,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = profile.analogy,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cornflower,
                    modifier = Modifier.weight(1f)
                )
            }

            // Core Feeling
            Text(
                text = profile.coreFeeling,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Periwinkle
            )

            // Local Context
            Text(
                text = profile.localContext,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            if (profile.topNotes.isNotEmpty()) {
                GlassDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profile.topNotes.take(3).forEach { note ->
                        GlassChip(text = note)
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
            .background(Brush.verticalGradient(colors = listOf(SoftPeriwinkle, Color(0xFFF8F7FF))))
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
                        onClick = onToggleWishlist,
                        isActive = isInWishlist,
                        activeColor = Periwinkle
                    ) {
                        Icon(
                            imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Toggle Wishlist",
                            tint = if (isInWishlist) Periwinkle else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Badge
                GlassBadge(
                    text = profile.brand,
                    backgroundColor = Cornflower.copy(alpha = 0.15f),
                    borderColor = Cornflower.copy(alpha = 0.3f),
                    textColor = Cornflower
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = profile.name,
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
                            backgroundColor = Cornflower.copy(alpha = 0.15f),
                            borderColor = Cornflower.copy(alpha = 0.3f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Cornflower,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = profile.analogy,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Core Feeling
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
                            text = profile.coreFeeling,
                            style = MaterialTheme.typography.titleMedium,
                            color = Periwinkle,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Local Context
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
                    color = Cornflower
                )

                Spacer(modifier = Modifier.height(12.dp))

                NotesCard(
                    title = "Middle Notes",
                    description = "Heart of the fragrance, lasts 3-5 hours",
                    notes = profile.middleNotes,
                    color = Periwinkle
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
                    GlassChip(text = note)
                }
            }
        }
    }
}