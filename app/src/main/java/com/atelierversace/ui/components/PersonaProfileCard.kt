package com.atelierversace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atelierversace.data.model.PersonaProfile

@Composable
fun PersonaProfileCard(
    profile: PersonaProfile,
    onAddToWardrobe: (() -> Unit)? = null,
    onAddToWishlist: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Show brand and name if available
            if (profile.brand.isNotEmpty() && profile.name.isNotEmpty()) {
                Text(
                    text = "${profile.brand} ${profile.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = profile.analogy,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = profile.coreFeeling,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = profile.localContext,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                onAddToWardrobe?.let {
                    Button(
                        onClick = it,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add to Wardrobe")
                    }
                }

                onAddToWishlist?.let {
                    OutlinedButton(
                        onClick = it,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add to Wishlist")
                    }
                }
            }
        }
    }
}