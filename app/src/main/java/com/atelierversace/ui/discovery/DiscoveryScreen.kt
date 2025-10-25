package com.atelierversace.ui.discovery

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atelierversace.ui.components.GlassCard

@Composable
fun DiscoveryScreen(viewModel: DiscoveryViewModel) {
    val discoveryState by viewModel.discoveryState.collectAsState()

    var query by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F7FF),
                        Color(0xFFFFF5F9),
                        Color(0xFFF5F5F5)
                    )
                )
            )
    ) {
        AnimatedContent(
            targetState = isSearchExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) +
                        slideInVertically(
                            animationSpec = tween(300),
                            initialOffsetY = { it / 2 }
                        ) togetherWith
                        fadeOut(animationSpec = tween(200)) +
                        slideOutVertically(
                            animationSpec = tween(200),
                            targetOffsetY = { -it / 2 }
                        )
            },
            label = "search_transition"
        ) { expanded ->
            if (!expanded) {
                // Initial State - Center Content
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "the perfect\nfragrance for you",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 32.sp,
                            lineHeight = 40.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF2D2D2D)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    GlassCard(
                        onClick = { isSearchExpanded = true },
                        modifier = Modifier
                            .padding(horizontal = 40.dp)
                            .height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF6B4EFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Find",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF2D2D2D)
                            )
                        }
                    }
                }
            } else {
                // Expanded State - Search View
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Bar with Search
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF6B4EFF),
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
                                                color = Color(0xFF8E8E93)
                                            )
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            cursorColor = Color(0xFF6B4EFF)
                                        ),
                                        singleLine = false,
                                        maxLines = 3
                                    )

                                    IconButton(
                                        onClick = {
                                            isSearchExpanded = false
                                            query = ""
                                            viewModel.reset()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color(0xFF8E8E93)
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
                                                colors = listOf(
                                                    Color(0xFF6B4EFF),
                                                    Color(0xFF8B6EFF)
                                                )
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
                            is DiscoveryState.Idle -> {
                                // Empty state handled by animation
                            }

                            is DiscoveryState.Loading -> {
                                item {
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
                            }

                            is DiscoveryState.Success -> {
                                items(state.recommendations) { profile ->
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            if (profile.brand.isNotEmpty() && profile.name.isNotEmpty()) {
                                                Text(
                                                    text = profile.brand,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color(0xFF8E8E93)
                                                )
                                                Text(
                                                    text = profile.name,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = Color(0xFF2D2D2D)
                                                )
                                            }

                                            Text(
                                                text = profile.analogy,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF6B4EFF)
                                            )

                                            Text(
                                                text = profile.coreFeeling,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFFF6B9D)
                                            )

                                            Text(
                                                text = profile.localContext,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF8E8E93)
                                            )

                                            GlassCard(
                                                onClick = { viewModel.addToWishlist(profile) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(
                                                            width = 1.5.dp,
                                                            color = Color(0xFFFF6B9D).copy(alpha = 0.3f),
                                                            shape = RoundedCornerShape(20.dp)
                                                        )
                                                        .padding(vertical = 12.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Favorite,
                                                            contentDescription = null,
                                                            tint = Color(0xFFFF6B9D),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            "Add to Wishlist",
                                                            color = Color(0xFFFF6B9D),
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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = state.message,
                                                color = Color(0xFFFF6B9D),
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

        // Bottom Navigation Bar placeholder
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
        )
    }
}