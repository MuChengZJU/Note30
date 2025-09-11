package com.example.note30

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val recordRepository = ServiceLocator.provideRecordRepository(application)
        val recordViewModel = RecordViewModel(recordRepository)
        val historyViewModel = HistoryViewModel(recordRepository)
        
        setContent {
            Note30App(recordViewModel, historyViewModel)
        }
    }
}

@Composable
fun Note30App(recordViewModel: RecordViewModel, historyViewModel: HistoryViewModel) {
    val navController = rememberNavController()
    var currentScreen by remember { mutableStateOf(Screen.Record) }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    label = { Text("记录") },
                    selected = currentScreen == Screen.Record,
                    onClick = {
                        currentScreen = Screen.Record
                        navController.navigate("record") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("历史") },
                    selected = currentScreen == Screen.History,
                    onClick = {
                        currentScreen = Screen.History
                        navController.navigate("history") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "record", modifier = Modifier.padding(innerPadding)) {
            composable("record") { RecordScreen(navController, recordViewModel) }
            composable("history") { HistoryScreen(navController, historyViewModel) }
        }
    }
}

sealed class Screen {
    object Record : Screen()
    object History : Screen()
}