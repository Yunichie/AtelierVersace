package com.atelierversace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.atelierversace.ui.scent_lens.ScentLensScreen
import com.atelierversace.ui.scent_lens.ScentLensViewModel
import com.atelierversace.ui.wardrobe.WardrobeScreen
import com.atelierversace.ui.wardrobe.WardrobeViewModel
import com.atelierversace.ui.discovery.DiscoveryScreen
import com.atelierversace.ui.discovery.DiscoveryViewModel
import com.atelierversace.ui.wishlist.WishlistScreen

sealed class Screen(val route: String) {
    object ScentLens : Screen("scent_lens")
    object Wardrobe : Screen("wardrobe")
    object Discovery : Screen("discovery")
    object Wishlist : Screen("wishlist")
}

@Composable
fun AtelierVersaceApp(
    scentLensViewModel: ScentLensViewModel,
    wardrobeViewModel: WardrobeViewModel,
    discoveryViewModel: DiscoveryViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp, start = 32.dp, end = 32.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.5f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    listOf(
                        Screen.ScentLens,
                        Screen.Wardrobe,
                        Screen.Discovery,
                        Screen.Wishlist
                    ).forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        Surface(
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) {
                                Color.White.copy(alpha = 0.35f)
                            } else {
                                Color.Transparent
                            },
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.5f)
                                )
                            } else null
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val iconTint = if (isSelected) {
                                    Color(0xFF7D97FE)
                                } else {
                                    Color(0xFF8E8E93).copy(0.7f)
                                }

                                when (screen) {
                                    is Screen.ScentLens -> Icon(
                                        painterResource(id = R.drawable.scent_lens_24),
                                        contentDescription = "Scent Lens",
                                        modifier = Modifier.size(28.dp),
                                        tint = iconTint
                                    )

                                    is Screen.Wardrobe -> Icon(
                                        painterResource(id = R.drawable.wardrobe_24),
                                        contentDescription = "Wardrobe",
                                        modifier = Modifier.size(28.dp),
                                        tint = iconTint
                                    )

                                    is Screen.Discovery -> Icon(
                                        painterResource(id = R.drawable.search_24),
                                        contentDescription = "Discovery",
                                        modifier = Modifier.size(28.dp),
                                        tint = iconTint
                                    )

                                    is Screen.Wishlist -> Icon(
                                        painterResource(id = R.drawable.heart_24),
                                        contentDescription = "Wishlist",
                                        modifier = Modifier.size(28.dp),
                                        tint = iconTint
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Discovery.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ScentLens.route) {
                ScentLensScreen(
                    viewModel = scentLensViewModel,
                    onNavigateToWardrobe = {
                        navController.navigate(Screen.Wardrobe.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }

            composable(Screen.Wardrobe.route) {
                WardrobeScreen(viewModel = wardrobeViewModel)
            }

            composable(Screen.Discovery.route) {
                DiscoveryScreen(viewModel = discoveryViewModel)
            }

            composable(Screen.Wishlist.route) {
                WishlistScreen(viewModel = discoveryViewModel)
            }
        }
    }
}