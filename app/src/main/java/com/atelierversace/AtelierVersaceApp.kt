package com.atelierversace

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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

sealed class Screen(val route: String, val title: String) {
    object ScentLens : Screen("scent_lens", "Scan")
    object Wardrobe : Screen("wardrobe", "Wardrobe")
    object Discovery : Screen("discovery", "Discover")
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
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                listOf(
                    Screen.ScentLens,
                    Screen.Wardrobe,
                    Screen.Discovery
                ).forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                is Screen.ScentLens -> Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_camera),
                                    contentDescription = null
                                )
                                is Screen.Wardrobe -> Icon(
                                    Icons.Filled.Home,
                                    contentDescription = null
                                )
                                is Screen.Discovery -> Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null
                                )
                            }
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ScentLens.route,
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
        }
    }
}
