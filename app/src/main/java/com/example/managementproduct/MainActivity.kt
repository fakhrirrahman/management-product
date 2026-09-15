package com.example.managementproduct

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.managementproduct.ui.inventaris.InventarisScreen
import com.example.managementproduct.ui.inventaris.InventarisViewModel
import com.example.managementproduct.ui.kasir.KasirScreen
import com.example.managementproduct.ui.kasir.KasirViewModel
import com.example.managementproduct.ui.laporan.LaporanScreen
import com.example.managementproduct.ui.laporan.LaporanViewModel
import com.example.managementproduct.ui.theme.ManagementProductTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManagementProductTheme {
                KiosApp()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Kasir : Screen("kasir", "Kasir", Icons.Default.ShoppingCart)
    object Inventaris : Screen("inventaris", "Inventaris", Icons.Default.Inventory)
    object Laporan : Screen("laporan", "Laporan", Icons.Default.BarChart)
}

@Composable
fun KiosApp() {
    val navController = rememberNavController()
    val items = listOf(Screen.Kasir, Screen.Inventaris, Screen.Laporan)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
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
            startDestination = Screen.Kasir.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Kasir.route) {
                val viewModel: KasirViewModel = viewModel(factory = KasirViewModel.Factory)
                KasirScreen(viewModel = viewModel)
            }
            composable(Screen.Inventaris.route) {
                val viewModel: InventarisViewModel = viewModel(factory = InventarisViewModel.Factory)
                InventarisScreen(viewModel = viewModel)
            }
            composable(Screen.Laporan.route) {
                val viewModel: LaporanViewModel = viewModel(factory = LaporanViewModel.Factory)
                LaporanScreen(viewModel = viewModel)
            }
        }
    }
}