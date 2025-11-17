package com.atelierversace.ui.ai_recommendations

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*
import com.atelierversace.utils.LayeringCombination

@Composable
fun AIRecommendationsScreen(viewModel: AIRecommendationsViewModel) {
    val todayRecommendation by viewModel.todayRecommendation.collectAsState()
    val layeringCombinations by viewModel.layeringCombinations.collectAsState()
    val savedLayerings by viewModel.savedLayerings.collectAsState()
    val occasionRecommendations by viewModel.occasionRecommendations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showOccasionInput by remember { mutableStateOf(false) }
    var selectedCombination by remember { mutableStateOf<LayeringCombination?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SoftPeriwinkle, Color(0xFFF8F7FF), Color(0xFFF5F5F5))
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI Stylist",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 40.sp
                                ),
                                color = TextPrimary
                            )

                            IconButton(
                                onClick = { viewModel.refreshRecommendations() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = IceBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Personalized recommendations just for you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TabChip(
                                text = "Today",
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            TabChip(
                                text = "Layering",
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    if (layeringCombinations.isEmpty()) {
                                        viewModel.generateLayering()
                                    }
                                }
                            )
                            TabChip(
                                text = "Occasions",
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
                },
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    0 -> TodayRecommendationContent(
                        recommendation = todayRecommendation,
                        isLoading = isLoading
                    )
                    1 -> LayeringContent(
                        combinations = layeringCombinations,
                        savedLayerings = savedLayerings,
                        isLoading = isLoading,
                        onSelectCombination = { selectedCombination = it },
                        onSaveLayering = { viewModel.saveLayering(it) },
                        onRemoveSaved = { viewModel.removeSavedLayering(it.id ?: "") }
                    )
                    2 -> OccasionsContent(
                        recommendations = occasionRecommendations,
                        isLoading = isLoading,
                        onRequestRecommendation = { showOccasionInput = true }
                    )
                }
            }
        }
    }

    if (showOccasionInput) {
        var occasion by remember { mutableStateOf("") }

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
                    Text(
                        "What's the occasion?",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.3f),
                        borderColor = Color.White.copy(alpha = 0.5f)
                    ) {
                        TextField(
                            value = occasion,
                            onValueChange = { occasion = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "e.g., romantic dinner, business meeting...",
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
                            onClick = { showOccasionInput = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }

                        GlassButton(
                            onClick = {
                                if (occasion.isNotBlank()) {
                                    viewModel.getOccasionRecommendation(occasion)
                                    showOccasionInput = false
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

    if (selectedCombination != null) {
        LayeringDetailDialog(
            combination = selectedCombination!!,
            isSaved = savedLayerings.any {
                it.basePerfumeId == selectedCombination!!.basePerfume.id &&
                        it.layerPerfumeId == selectedCombination!!.layerPerfume.id
            },
            onSave = {
                viewModel.saveLayering(selectedCombination!!)
                selectedCombination = null
            },
            onDismiss = { selectedCombination = null }
        )
    }

    errorMessage?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            GlassCard(
                modifier = Modifier.padding(horizontal = 32.dp),
                backgroundColor = Cornflower.copy(alpha = 0.9f),
                borderColor = Color.White.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        error,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
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
            IceBlue.copy(alpha = 0.25f)
        } else {
            Color.White.copy(alpha = 0.25f)
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) {
                IceBlue.copy(alpha = 0.5f)
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
            color = if (selected) IceBlue else TextSecondary
        )
    }
}

@Composable
private fun TodayRecommendationContent(
    recommendation: Triple<PerfumeCloud, String, String>?,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IceBlue)
                }
            }
        } else if (recommendation != null) {
            val (perfume, reason, layering) = recommendation

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.25f),
                    borderColor = Color.White.copy(alpha = 0.5f),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                IceBlue.copy(alpha = 0.3f),
                                                IceBlue.copy(alpha = 0.1f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = IceBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                "Today's Pick",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                        }

                        GlassDivider()

                        GlassBadge(
                            text = perfume.brand,
                            backgroundColor = IceBlue.copy(alpha = 0.15f),
                            borderColor = IceBlue.copy(alpha = 0.3f),
                            textColor = IceBlue
                        )

                        Text(
                            text = perfume.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )

                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )

                        if (layering != "No layering recommended") {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Periwinkle.copy(alpha = 0.15f),
                                borderColor = Periwinkle.copy(alpha = 0.3f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Layers,
                                            contentDescription = null,
                                            tint = Periwinkle,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Layering Suggestion",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Periwinkle,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = layering,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                EmptyState(
                    icon = Icons.Default.AutoAwesome,
                    message = "Add perfumes to get personalized recommendations"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun LayeringContent(
    combinations: List<LayeringCombination>,
    savedLayerings: List<com.atelierversace.data.remote.LayeringRecommendation>,
    isLoading: Boolean,
    onSelectCombination: (LayeringCombination) -> Unit,
    onSaveLayering: (LayeringCombination) -> Unit,
    onRemoveSaved: (com.atelierversace.data.remote.LayeringRecommendation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IceBlue)
                }
            }
        } else if (combinations.isNotEmpty()) {
            items(combinations) { combination ->
                val isSaved = savedLayerings.any {
                    it.basePerfumeId == combination.basePerfume.id &&
                            it.layerPerfumeId == combination.layerPerfume.id
                }

                LayeringCombinationCard(
                    combination = combination,
                    isSaved = isSaved,
                    onClick = { onSelectCombination(combination) },
                    onSaveToggle = {
                        if (isSaved) {
                            savedLayerings.find {
                                it.basePerfumeId == combination.basePerfume.id &&
                                        it.layerPerfumeId == combination.layerPerfume.id
                            }?.let { onRemoveSaved(it) }
                        } else {
                            onSaveLayering(combination)
                        }
                    }
                )
            }
        } else {
            item {
                EmptyState(
                    icon = Icons.Default.Layers,
                    message = "No layering combinations yet. Add more perfumes!"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun LayeringCombinationCard(
    combination: LayeringCombination,
    isSaved: Boolean,
    onClick: () -> Unit,
    onSaveToggle: () -> Unit
) {
    GlassCard(
        onClick = onClick,
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
                Text(
                    text = combination.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onSaveToggle,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove from saved" else "Save layering",
                            tint = if (isSaved) IceBlue else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = IceBlue.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${combination.harmonyScore}",
                                style = MaterialTheme.typography.labelLarge,
                                color = IceBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassChip(text = combination.basePerfume.brand)
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                GlassChip(text = combination.layerPerfume.brand)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = combination.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            GlassBadge(
                text = combination.occasion,
                backgroundColor = Periwinkle.copy(alpha = 0.15f),
                borderColor = Periwinkle.copy(alpha = 0.3f),
                textColor = Periwinkle
            )
        }
    }
}

@Composable
private fun OccasionsContent(
    recommendations: Map<String, Triple<PerfumeCloud, String, String>>,
    isLoading: Boolean,
    onRequestRecommendation: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassButton(
                onClick = onRequestRecommendation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request Recommendation", color = Color.White)
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IceBlue)
                }
            }
        } else if (recommendations.isNotEmpty()) {
            items(recommendations.entries.toList()) { (occasion, recommendation) ->
                val (perfume, reason, _) = recommendation
                OccasionRecommendationCard(
                    occasion = occasion,
                    perfume = perfume,
                    reason = reason
                )
            }
        } else {
            item {
                EmptyState(
                    icon = Icons.Default.Event,
                    message = "Request recommendations for specific occasions"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun OccasionRecommendationCard(
    occasion: String,
    perfume: PerfumeCloud,
    reason: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.25f),
        borderColor = Color.White.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            GlassBadge(
                text = occasion,
                backgroundColor = Cornflower.copy(alpha = 0.15f),
                borderColor = Cornflower.copy(alpha = 0.3f),
                textColor = Cornflower
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${perfume.brand} ${perfume.name}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LayeringDetailDialog(
    combination: LayeringCombination,
    isSaved: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.padding(24.dp),
            backgroundColor = Color.White.copy(alpha = 0.95f),
            borderColor = Color.White.copy(alpha = 0.6f),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        combination.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Base", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${combination.basePerfume.brand} ${combination.basePerfume.name}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Layer", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${combination.layerPerfume.brand} ${combination.layerPerfume.name}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassDivider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    combination.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassBadge(
                    text = "Best for: ${combination.occasion}",
                    backgroundColor = Periwinkle.copy(alpha = 0.15f),
                    borderColor = Periwinkle.copy(alpha = 0.3f),
                    textColor = Periwinkle
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isSaved) {
                        GlassButton(
                            onClick = onSave,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Layering", color = Color.White)
                        }
                    } else {
                        OutlinedGlassButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}