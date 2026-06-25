package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.database.SimaDatabase
import com.example.data.repository.SimaRepository
import com.example.ui.SimaViewModel
import com.example.ui.SimaViewModelFactory
import com.example.ui.screens.DetailsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local persistence layer
        val database = SimaDatabase.getDatabase(applicationContext)
        val repository = SimaRepository(database.simaDao(), applicationContext)

        setContent {
            MyApplicationTheme {
                val viewModel: SimaViewModel = viewModel(
                    factory = SimaViewModelFactory(repository)
                )
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: SimaViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToScan = {
                    navController.navigate("scan")
                },
                onNavigateToDetails = { id ->
                    navController.navigate("details/$id")
                }
            )
        }

        composable("scan") {
            ScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartSearch = { uri ->
                    viewModel.selectedImageUri = uri
                    navController.navigate("loading")
                }
            )
        }

        composable("loading") {
            val uri = viewModel.selectedImageUri ?: ""
            LoadingScreen(
                viewModel = viewModel,
                imageUri = uri,
                onNavigateToDetails = { id ->
                    navController.navigate("details/$id") {
                        popUpTo("home")
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "details/{scanId}",
            arguments = listOf(
                navArgument("scanId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getInt("scanId") ?: 0
            DetailsScreen(
                viewModel = viewModel,
                scanId = scanId,
                onNavigateBack = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
