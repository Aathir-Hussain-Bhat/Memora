package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MemoraApp(viewModel: MemoraViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_note") },
                onGraphClick = { navController.navigate("graph") }
            )
        }
        composable("add_note") {
            AddNoteScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("graph") {
            GraphScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
