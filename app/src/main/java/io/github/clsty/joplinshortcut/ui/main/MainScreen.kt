package io.github.clsty.joplinshortcut.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import io.github.clsty.joplinshortcut.R
import io.github.clsty.joplinshortcut.ui.favorites.FavoritesScreen
import io.github.clsty.joplinshortcut.ui.manage.ManageScreen
import io.github.clsty.joplinshortcut.ui.notes.NotesScreen
import io.github.clsty.joplinshortcut.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Notes : Screen("notes")
    object Favorites : Screen("favorites")
    object Manage : Screen("manage")
    object Settings : Screen("settings")
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Triple(Screen.Notes, Icons.Default.Notes, R.string.nav_notes),
        Triple(Screen.Favorites, Icons.Default.Star, R.string.nav_favorites),
        Triple(Screen.Manage, Icons.Default.Dashboard, R.string.nav_manage),
        Triple(Screen.Settings, Icons.Default.Settings, R.string.nav_settings),
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { (screen, icon, labelRes) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(stringResource(labelRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            startDestination = Screen.Notes.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Notes.route) { NotesScreen() }
            composable(Screen.Favorites.route) { FavoritesScreen() }
            composable(Screen.Manage.route) { ManageScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
