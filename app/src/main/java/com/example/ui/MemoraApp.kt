package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MemoraApp(viewModel: MemoraViewModel) {
    val navController = rememberNavController()

    val items = listOf(
        "home" to Icons.Default.Home,
        "search" to Icons.Default.Search,
        "graph" to Icons.Default.AutoGraph,
        "ai" to Icons.Default.SmartToy,
        "profile" to Icons.Default.Person
    )
    val labels = listOf("Home", "Search", "Universe", "AI", "Profile")

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isMainScreen = items.any { it.first == currentDestination?.route }

            if (isMainScreen && currentDestination?.route != "loading") {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.second, contentDescription = labels[index]) },
                            label = { Text(labels[index]) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.first } == true,
                            onClick = {
                                navController.navigate(item.first) {
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
            startDestination = "loading",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("loading") {
                LoadingScreen(onLoadingComplete = {
                    navController.navigate("home") {
                        popUpTo("loading") { inclusive = true }
                    }
                })
            }
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate("add_note") },
                    onGraphClick = { navController.navigate("graph") }
                )
            }
            composable("search") {
                SearchScreen(
                    viewModel = viewModel,
                    onNoteClick = { /* Navigate to note detail */ }
                )
            }
            composable("graph") {
                GraphScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("ai") {
                AiScreen(viewModel = viewModel)
            }
            composable("profile") {
                ProfileScreen(viewModel = viewModel)
            }
            composable("add_note") {
                AddNoteScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
