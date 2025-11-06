package com.atelierversace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.atelierversace.ui.ai_recommendations.AIRecommendationsScreen
import com.atelierversace.ui.ai_recommendations.AIRecommendationsViewModel
import com.atelierversace.ui.auth.AuthScreen
import com.atelierversace.ui.auth.AuthState
import com.atelierversace.ui.auth.AuthViewModel
import com.atelierversace.ui.discovery.DiscoveryScreen
import com.atelierversace.ui.discovery.DiscoveryViewModel
import com.atelierversace.ui.profile.ProfileScreen
import com.atelierversace.ui.profile.ProfileViewModel
import com.atelierversace.ui.scent_lens.ScentLensScreen
import com.atelierversace.ui.scent_lens.ScentLensViewModel
import com.atelierversace.ui.wardrobe.WardrobeScreen
import com.atelierversace.ui.wardrobe.WardrobeViewModel
import com.atelierversace.ui.wishlist.WishlistScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object ScentLens : Screen("scent_lens")
    object Wardrobe : Screen("wardrobe")
    object Discovery : Screen("discovery")
    object Wishlist : Screen("wishlist")
    object AIRecommendations : Screen("ai_recommendations")
    object Profile : Screen("profile")
}

@Composable
fun AtelierVersaceApp(
    authViewModel: AuthViewModel,
    scentLensViewModel: ScentLensViewModel,
    wardrobeViewModel: WardrobeViewModel,
    discoveryViewModel: DiscoveryViewModel,
    aiRecommendationsViewModel: AIRecommendationsViewModel,
    profileViewModel: ProfileViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val navController = rememberNavController()

    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Unauthenticated, is AuthState.Error -> {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                }
            )
        }
        is AuthState.Authenticated -> {
            MainAppContent(
                navController = navController,
                authViewModel = authViewModel,
                scentLensViewModel = scentLensViewModel,
                wardrobeViewModel = wardrobeViewModel,
                discoveryViewModel = discoveryViewModel,
                aiRecommendationsViewModel = aiRecommendationsViewModel,
                profileViewModel = profileViewModel
            )
        }
    }
}

@Composable
private fun MainAppContent(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel,
    scentLensViewModel: ScentLensViewModel,
    wardrobeViewModel: WardrobeViewModel,
    discoveryViewModel: DiscoveryViewModel,
    aiRecommendationsViewModel: AIRecommendationsViewModel,
    profileViewModel: ProfileViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.AIRecommendations.route,
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

            composable(Screen.AIRecommendations.route) {
                AIRecommendationsScreen(viewModel = aiRecommendationsViewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onSignOut = {
                        authViewModel.signOut()
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.8f),
                                Color.White.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(36.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp)
                    .background(Color.Transparent),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Screen.AIRecommendations,
                    Screen.ScentLens,
                    Screen.Wardrobe,
                    Screen.Discovery,
                    Screen.Wishlist,
                    Screen.Profile
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
                            is Screen.AIRecommendations -> Icon(
                                painterResource(id = R.drawable.auto_awesome_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )

                            is Screen.ScentLens -> Icon(
                                painterResource(id = R.drawable.scent_lens_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )

                            is Screen.Wardrobe -> Icon(
                                painterResource(id = R.drawable.wardrobe_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )

                            is Screen.Discovery -> Icon(
                                painterResource(id = R.drawable.search_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )

                            is Screen.Wishlist -> Icon(
                                painterResource(id = R.drawable.heart_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )

                            is Screen.Profile -> Icon(
                                painterResource(id = R.drawable.person_24),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}