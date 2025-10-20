package com.atelierversace.ui.wardrobe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.atelierversace.ui.components.PerfumeGridItem

@Composable
fun WardrobeScreen(
    viewModel: WardrobeViewModel
) {
    val wardrobe by viewModel.wardrobe.collectAsState()
    val recommendationState by viewModel.recommendationState.collectAsState()

    var showOccasionDialog by remember { mutableStateOf(false) }
    var showRecommendationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "My Wardrobe",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showOccasionDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("What's My Aura Today?")
                }
            }
        }

        // Perfume Grid
        if (wardrobe.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your wardrobe is empty.\nScan a perfume to get started!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wardrobe) { perfume ->
                    PerfumeGridItem(
                        perfume = perfume,
                        onClick = { /* Show details */ }
                    )
                }
            }
        }
    }

    // Occasion Selection Dialog
    if (showOccasionDialog) {
        OccasionDialog(
            onDismiss = { showOccasionDialog = false },
            onSelect = { occasion ->
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
fun OccasionDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val occasions = listOf("Work", "Casual", "Date Night", "Formal Event")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Occasion") },
        text = {
            Column {
                occasions.forEach { occasion ->
                    TextButton(
                        onClick = { onSelect(occasion) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(occasion)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RecommendationDialog(
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
                }
            )
        },
        text = {
            when (state) {
                is RecommendationState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is RecommendationState.Success -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = state.perfume.imageUri,
                            contentDescription = state.perfume.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Text(
                            text = "${state.perfume.brand} ${state.perfume.name}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = state.reason,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is RecommendationState.Error -> {
                    Text(state.message)
                }
                else -> {}
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}