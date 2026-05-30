package ru.aptechka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.aptechka.ui.navigation.Screen
import ru.aptechka.ui.screens.add.AddDrugScreen
import ru.aptechka.ui.screens.expiry.ExpiryScreen
import ru.aptechka.ui.screens.meddetail.MedDetailScreen
import ru.aptechka.ui.screens.scanner.ScannerScreen
import ru.aptechka.ui.screens.kits.KitDetailScreen
import ru.aptechka.ui.screens.kits.KitsScreen
import ru.aptechka.ui.screens.settings.SettingsScreen
import ru.aptechka.ui.screens.shopping.ShoppingScreen
import ru.aptechka.ui.theme.AptechkaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AptechkaTheme {
                AppNavHost()
            }
        }
    }
}

private data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector,
)

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Kits,     R.string.nav_kits,     Icons.Outlined.Home),
        BottomNavItem(Screen.Expiry,   R.string.nav_expiry,   Icons.Outlined.Schedule),
        BottomNavItem(Screen.Shopping, R.string.nav_shopping, Icons.Outlined.ShoppingCart),
        BottomNavItem(Screen.Settings, R.string.nav_settings, Icons.Outlined.Settings),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val topLevelRoutes = bottomNavItems.map { it.screen.route }
    val showBottomBar  = currentDestination?.route in topLevelRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter   = slideInVertically(initialOffsetY = { it }),
                exit    = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController   = navController,
            startDestination = Screen.Kits.route,
            modifier        = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Kits.route) {
                KitsScreen(navController = navController)
            }

            composable(
                route     = Screen.KitDetail.route,
                arguments = listOf(
                    navArgument("kitId") { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val kitId = backStackEntry.arguments?.getLong("kitId") ?: return@composable
                // Pass kit name/color/icon through savedStateHandle or re-query
                KitDetailScreen(
                    kitId         = kitId,
                    navController = navController,
                )
            }

            composable(
                route     = Screen.AddDrug.route,
                arguments = listOf(
                    navArgument("kitId") { type = NavType.LongType },
                    navArgument("catalogId") { type = NavType.LongType; defaultValue = -1L },
                ),
            ) { backStackEntry ->
                val kitId = backStackEntry.arguments?.getLong("kitId") ?: return@composable
                val catalogId = backStackEntry.arguments?.getLong("catalogId") ?: -1L
                AddDrugScreen(
                    kitId         = kitId,
                    catalogId     = catalogId,
                    navController = navController,
                )
            }

            composable(
                route     = Screen.MedDetail.route,
                arguments = listOf(
                    navArgument("drugId") { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val drugId = backStackEntry.arguments?.getLong("drugId") ?: return@composable
                MedDetailScreen(
                    drugId        = drugId,
                    navController = navController,
                )
            }

            composable(
                route     = Screen.Scanner.route,
                arguments = listOf(
                    navArgument("kitId") { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val kitId = backStackEntry.arguments?.getLong("kitId") ?: return@composable
                ScannerScreen(
                    kitId         = kitId,
                    navController = navController,
                )
            }

            composable(Screen.Expiry.route) {
                ExpiryScreen()
            }

            composable(Screen.Shopping.route) {
                ShoppingScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
