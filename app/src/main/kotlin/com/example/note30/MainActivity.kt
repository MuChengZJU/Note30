package com.example.note30

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Build Repository from Room
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = RecordRepository(db.recordDao())
        setContent { Note30App(repository) }
    }
}

private enum class Dest(val route: String, val label: String) {
    Record("record", "记录"),
    History("history", "历史")
}

@Composable
fun Note30App(repository: RecordRepository) {
    val navController = rememberNavController()
    val items = listOf(Dest.Record, Dest.History)
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { dest ->
                    BottomNavigationItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Dest.Record.route,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable(Dest.Record.route) {
                val vm = remember { RecordViewModel(repository) }
                RecordScreen(navController = navController, viewModel = vm)
            }
            composable(Dest.History.route) {
                val vm = remember { HistoryViewModel(repository) }
                HistoryScreen(navController = navController, viewModel = vm)
            }
        }
    }
}