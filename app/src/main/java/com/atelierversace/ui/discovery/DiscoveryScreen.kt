package com.atelierversace.ui.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atelierversace.ui.components.PersonaProfileCard

@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel
) {
    val discoveryState by viewModel.discoveryState.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()

    var query by remember { mutableStateOf("") }
    var showWishlist by remember { mutableStateOf(false) }

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
                    text = "Discovery Hub",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Describe the scent you're dreaming of...") },
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.searchPerfumes(query) },
                        modifier = Modifier.weight(1f),
                        enabled = query.isNotBlank()
                    ) {
                        Text("Search")
                    }

                    OutlinedButton(
                        onClick = { showWishlist = !showWishlist },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (showWishlist) "Hide Wishlist" else "Show Wishlist (${wishlist.size})")
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
            if (showWishlist) {
                item {
                    Text(
                        text = "Your Wishlist",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (wishlist.isEmpty()) {
                    item {
                        Text(
                            text = "No items in wishlist yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(wishlist) { perfume ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = perfume.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = perfume.brand,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = perfume.analogy,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = perfume.coreFeeling,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                when (val state = discoveryState) {
                    is DiscoveryState.Idle -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Describe your dream scent and let AI find it for you",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    is DiscoveryState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is DiscoveryState.Success -> {
                        item {
                            Text(
                                text = "Recommendations for you",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        items(state.recommendations) { profile ->
                            PersonaProfileCard(
                                profile = profile,
                                onAddToWishlist = {
                                    viewModel.addToWishlist(profile)
                                }
                            )
                        }
                    }

                    is DiscoveryState.Error -> {
                        item {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
