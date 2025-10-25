package com.atelierversace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

sealed class Screen(val route: String, val title: String) {
    object ScentLens : Screen("scent_lens", "Scan")
    object Wardrobe : Screen("wardrobe", "Wardrobe")
    object Discovery : Screen("discovery", "Discover")
    object Wishlist : Screen("wishlist", "Wishlist")
}

@Composable
fun AtelierVersaceApp(
    scentLensViewModel: ScentLensViewModel,
    wardrobeViewModel: WardrobeViewModel,
    discoveryViewModel: DiscoveryViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(36.dp)
                        ),
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(36.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        // Wardrobe Button
                        NavigationButton(
                            icon = Icons.Filled.Home,
                            label = "Wardrobe",
                            selected = currentDestination?.hierarchy?.any {
                                it.route == Screen.Wardrobe.route
                            } == true,
                            onClick = {
                                navController.navigate(Screen.Wardrobe.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        // Scan Button (Center)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentDestination?.hierarchy?.any {
                                            it.route == Screen.ScentLens.route
                                        } == true) {
                                        Color(0xFF6B4EFF)
                                    } else {
                                        Color.White.copy(alpha = 0.5f)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    navController.navigate(Screen.ScentLens.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_camera),
                                    contentDescription = "Scan",
                                    tint = if (currentDestination?.hierarchy?.any {
                                            it.route == Screen.ScentLens.route
                                        } == true) {
                                        Color.White
                                    } else {
                                        Color(0xFF8E8E93)
                                    }
                                )
                            }
                        }

                        // Discovery Button
                        NavigationButton(
                            icon = Icons.Filled.Search,
                            label = "Discover",
                            selected = currentDestination?.hierarchy?.any {
                                it.route == Screen.Discovery.route
                            } == true,
                            onClick = {
                                navController.navigate(Screen.Discovery.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        // Wishlist Button
                        NavigationButton(
                            icon = Icons.Filled.Favorite,
                            label = "Wishlist",
                            selected = currentDestination?.hierarchy?.any {
                                it.route == Screen.Wishlist.route
                            } == true,
                            onClick = {
                                navController.navigate(Screen.Wishlist.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
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
                        navController.navigate(Screen.Wardrobe.route)
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

@Composable
private fun NavigationButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color(0xFF6B4EFF) else Color(0xFF8E8E93),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
