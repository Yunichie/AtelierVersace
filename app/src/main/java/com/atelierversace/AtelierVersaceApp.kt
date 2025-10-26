package com.atelierversace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.atelierversace.ui.discovery.DiscoveryScreen
import com.atelierversace.ui.discovery.DiscoveryViewModel
import com.atelierversace.ui.scent_lens.ScentLensScreen
import com.atelierversace.ui.scent_lens.ScentLensViewModel
import com.atelierversace.ui.wardrobe.WardrobeScreen
import com.atelierversace.ui.wardrobe.WardrobeViewModel
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Discovery.route,
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 36.dp, end = 36.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .background(Color.Transparent),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Screen.ScentLens,
                    Screen.Wardrobe,
                    Screen.Discovery,
                    Screen.Wishlist
                ).forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    IconButton(
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        val iconTint = if(isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        when(screen) {
                            is Screen.ScentLens -> Icon(
                                painterResource(id = R.drawable.scent_lens_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(28.dp),
                            )

                            is Screen.Wardrobe -> Icon(
                                painterResource(id = R.drawable.wardrobe_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(28.dp)
                            )

                            is Screen.Discovery -> Icon(
                                painterResource(id = R.drawable.search_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(28.dp)
                            )

                            is Screen.Wishlist -> Icon(
                                painterResource(id = R.drawable.heart_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}