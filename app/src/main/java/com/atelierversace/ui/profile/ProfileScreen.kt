package com.atelierversace.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val personalization by viewModel.personalization.collectAsState()
    val wardrobeCount by viewModel.wardrobeCount.collectAsState()
    val wishlistCount by viewModel.wishlistCount.collectAsState()
    val favoritesCount by viewModel.favoritesCount.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            showImagePicker = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.uploadProfilePicture(it, context)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    viewModel.uploadProfilePicture(bitmap, context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    CenteredGradientBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.25f),
                    borderColor = VibrantPurple.copy(alpha = 0.5f),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            if (userProfile?.avatarUrl != null) {
                                AsyncImage(
                                    model = userProfile!!.avatarUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, brush = Brush.linearGradient(
                                            colors = listOf(
                                                VibrantPurple,
                                                ElectricSapphire,
                                                SoftPink
                                            )
                                        ), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    VibrantPurple.copy(alpha = 0.3f),
                                                    ElectricSapphire.copy(alpha = 0.2f),
                                                    SoftPink.copy(alpha = 0.1f)
                                                )
                                            )
                                        )
                                        .border(3.dp, brush = Brush.linearGradient(
                                            colors = listOf(
                                                VibrantPurple,
                                                ElectricSapphire,
                                                SoftPink
                                            )
                                        ), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp),
                                        tint = VibrantPurple
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    if (userProfile?.avatarUrl != null) {
                                        showRemoveConfirmation = true
                                    } else {
                                        if (hasCameraPermission) {
                                            showImagePicker = true
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                },
                                shape = CircleShape,
                                color = Cornflower,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isUploading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (userProfile?.avatarUrl != null)
                                                Icons.Default.Edit
                                            else
                                                Icons.Default.CameraAlt,
                                            contentDescription = "Change picture",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
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
                                color = Cornflower
                            )
                            StatItem(
                                icon = Icons.Default.FavoriteBorder,
                                count = wishlistCount,
                                label = "Wishlist",
                                color = SkyBlue
                            )
                            StatItem(
                                icon = Icons.Default.Star,
                                count = favoritesCount,
                                label = "Favorites",
                                color = VibrantPurple
                            )
                        }
                    }
                }
            }

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
                                backgroundColor = Cornflower.copy(alpha = 0.15f),
                                borderColor = Cornflower.copy(alpha = 0.3f),
                                size = 48.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Cornflower,
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
                                    color = Cornflower
                                )
                            }
                        }
                    }
                }

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
                                        backgroundColor = VibrantPurple.copy(alpha = 0.15f),
                                        borderColor = VibrantPurple.copy(alpha = 0.3f),
                                        size = 40.dp
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Store,
                                            contentDescription = null,
                                            tint = VibrantPurple,
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
                                        GlassChip(text = brand)
                                    }
                                }
                            }
                        }
                    }
                }

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
                                        backgroundColor = Cornflower.copy(alpha = 0.15f),
                                        borderColor = Cornflower.copy(alpha = 0.3f),
                                        size = 40.dp
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Spa,
                                            contentDescription = null,
                                            tint = Cornflower,
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
                                backgroundColor = VibrantPurple.copy(alpha = 0.15f),
                                borderColor = VibrantPurple.copy(alpha = 0.3f),
                                size = 48.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = VibrantPurple,
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
                                    color = VibrantPurple
                                )
                            }
                        }
                    }
                }
            } else {
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

    if (showImagePicker) {
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
                        "Change Profile Picture",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    GlassButton(
                        onClick = {
                            showImagePicker = false
                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo", color = Color.White)
                    }

                    OutlinedGlassButton(
                        onClick = {
                            showImagePicker = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Cornflower
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery", color = Cornflower)
                    }

                    TextButton(
                        onClick = { showImagePicker = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }

    if (showRemoveConfirmation) {
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
                        "Profile Picture",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    Text(
                        "What would you like to do?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    GlassButton(
                        onClick = {
                            showRemoveConfirmation = false
                            showImagePicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Picture", color = Color.White)
                    }

                    OutlinedGlassButton(
                        onClick = {
                            showRemoveConfirmation = false
                            viewModel.removeProfilePicture()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Cornflower
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove Picture", color = Cornflower)
                    }

                    TextButton(
                        onClick = { showRemoveConfirmation = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }

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
                                colors = listOf(Cornflower, Cornflower.copy(alpha = 0.8f))
                            )
                        ) {
                            Text("Sign Out", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    uploadError?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearUploadError()
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