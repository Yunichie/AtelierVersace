package com.atelierversace.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atelierversace.data.remote.UserProfile
import com.atelierversace.data.remote.AIPersonalization
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSignOut: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val personalization by viewModel.personalization.collectAsState()
    val wardrobeCount by viewModel.wardrobeCount.collectAsState()
    val wishlistCount by viewModel.wishlistCount.collectAsState()
    val favoritesCount by viewModel.favoritesCount.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Cream, Color(0xFFF8F7FF), Color(0xFFF5F5F5))
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Profile Header
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.25f),
                    borderColor = Color.White.copy(alpha = 0.5f),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            SkyBlue.copy(alpha = 0.3f),
                                            SkyBlue.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                                .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = SkyBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = userProfile?.displayName ?: "User",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )

                        Text(
                            text = userProfile?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                icon = Icons.Default.Favorite,
                                count = wardrobeCount,
                                label = "Wardrobe",
                                color = SkyBlue
                            )
                            StatItem(
                                icon = Icons.Default.FavoriteBorder,
                                count = wishlistCount,
                                label = "Wishlist",
                                color = LightPeriwinkle
                            )
                            StatItem(
                                icon = Icons.Default.Star,
                                count = favoritesCount,
                                label = "Favorites",
                                color = Taupe
                            )
                        }
                    }
                }
            }

            // AI Personalization Stats
            item {
                Text(
                    text = "AI Personalization",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
            }

            if (personalization != null) {
                // Style Profile
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.25f),
                        borderColor = Color.White.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassIconContainer(
                                backgroundColor = SkyBlue.copy(alpha = 0.15f),
                                borderColor = SkyBlue.copy(alpha = 0.3f),
                                size = 48.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = SkyBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Style Profile",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    personalization!!.styleProfile,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = SkyBlue
                                )
                            }
                        }
                    }
                }

                // Preferred Brands
                if (personalization!!.preferredBrands.isNotEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.25f),
                            borderColor = Color.White.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    GlassIconContainer(
                                        backgroundColor = LightPeriwinkle.copy(alpha = 0.15f),
                                        borderColor = LightPeriwinkle.copy(alpha = 0.3f),
                                        size = 40.dp
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Store,
                                            contentDescription = null,
                                            tint = LightPeriwinkle,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Text(
                                        "Favorite Brands",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = TextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    personalization!!.preferredBrands.take(3).forEach { brand ->
                                        GlassChip(text = brand, selected = true)
                                    }
                                }
                            }
                        }
                    }
                }

                // Preferred Notes
                if (personalization!!.preferredNotes.isNotEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.25f),
                            borderColor = Color.White.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    GlassIconContainer(
                                        backgroundColor = Taupe.copy(alpha = 0.15f),
                                        borderColor = Taupe.copy(alpha = 0.3f),
                                        size = 40.dp
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Spa,
                                            contentDescription = null,
                                            tint = Taupe,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Text(
                                        "Preferred Notes",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = TextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    personalization!!.preferredNotes.chunked(3).forEach { row ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            row.forEach { note ->
                                                GlassChip(text = note)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Intensity Preference
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.25f),
                        borderColor = Color.White.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassIconContainer(
                                backgroundColor = SkyBlue.copy(alpha = 0.15f),
                                borderColor = SkyBlue.copy(alpha = 0.3f),
                                size = 48.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = SkyBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Intensity Preference",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    personalization!!.intensityPreference,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = SkyBlue
                                )
                            }
                        }
                    }
                }
            } else {
                // No personalization yet
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.2f),
                        borderColor = Color.White.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = TextSecondary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Building your AI profile...",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Add perfumes to your wardrobe to help the AI learn your preferences",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Settings Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
            }

            // Sign Out
            item {
                GlassCard(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.25f),
                    borderColor = Taupe.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Taupe,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Sign Out",
                            style = MaterialTheme.typography.titleMedium,
                            color = Taupe,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Sign Out Confirmation
    if (showSignOutDialog) {
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
                        "Sign Out?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Text(
                        "Are you sure you want to sign out?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedGlassButton(
                            onClick = { showSignOutDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }

                        GlassButton(
                            onClick = {
                                showSignOutDialog = false
                                onSignOut()
                            },
                            modifier = Modifier.weight(1f),
                            gradient = Brush.horizontalGradient(
                                colors = listOf(Taupe, Taupe.copy(alpha = 0.8f))
                            )
                        ) {
                            Text("Sign Out", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}